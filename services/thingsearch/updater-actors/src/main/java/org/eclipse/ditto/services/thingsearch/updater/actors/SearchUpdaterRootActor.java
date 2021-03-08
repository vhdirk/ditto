/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.thingsearch.updater.actors;

import javax.annotation.Nullable;

import org.eclipse.ditto.services.base.actors.StartChildActor;
import org.eclipse.ditto.services.thingsearch.common.config.SearchConfig;
import org.eclipse.ditto.services.thingsearch.common.config.UpdaterConfig;
import org.eclipse.ditto.services.thingsearch.common.util.RootSupervisorStrategyFactory;
import org.eclipse.ditto.services.thingsearch.persistence.read.ThingsSearchPersistence;
import org.eclipse.ditto.services.thingsearch.persistence.write.ThingsSearchUpdaterPersistence;
import org.eclipse.ditto.services.thingsearch.persistence.write.impl.MongoThingsSearchUpdaterPersistence;
import org.eclipse.ditto.services.thingsearch.persistence.write.streaming.ChangeQueueActor;
import org.eclipse.ditto.services.thingsearch.persistence.write.streaming.SearchUpdaterStream;
import org.eclipse.ditto.services.utils.akka.streaming.TimestampPersistence;
import org.eclipse.ditto.services.utils.cluster.ClusterUtil;
import org.eclipse.ditto.services.utils.cluster.DistPubSubAccess;
import org.eclipse.ditto.services.utils.cluster.config.ClusterConfig;
import org.eclipse.ditto.services.utils.health.RetrieveHealth;
import org.eclipse.ditto.services.utils.namespaces.BlockedNamespaces;
import org.eclipse.ditto.services.utils.persistence.mongo.DittoMongoClient;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoClientWrapper;
import org.eclipse.ditto.services.utils.persistence.mongo.config.MongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.monitoring.KamonCommandListener;
import org.eclipse.ditto.services.utils.persistence.mongo.monitoring.KamonConnectionPoolListener;
import org.eclipse.ditto.services.utils.pubsub.DistributedAcks;
import org.eclipse.ditto.services.utils.pubsub.DistributedSub;
import org.eclipse.ditto.services.utils.pubsub.ThingEventPubSubFactory;
import org.eclipse.ditto.signals.commands.devops.RetrieveStatisticsDetails;

import com.mongodb.event.CommandListener;
import com.mongodb.event.ConnectionPoolListener;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.KillSwitch;

/**
 * Our "Parent" Actor which takes care of supervision of all other Actors in our system.
 * Child of {@code SearchRootActor}.
 */
public final class SearchUpdaterRootActor extends AbstractActor {

    /**
     * The name of this Actor in the ActorSystem.
     */
    public static final String ACTOR_NAME = "searchUpdaterRoot";

    /**
     * The main cluster role of the cluster member where this actor and its children start.
     */
    public static final String CLUSTER_ROLE = "things-search";

    private static final String KAMON_METRICS_PREFIX = "updater";

    private static final String SEARCH_ROLE = "things-search";

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SupervisorStrategy supervisorStrategy = RootSupervisorStrategyFactory.createStrategy(log);

    private final KillSwitch updaterStreamKillSwitch;
    private final KillSwitch updaterStreamWithAcknowledgementsKillSwitch;
    private final ActorRef thingsUpdaterActor;
    private final ActorRef backgroundSyncActorProxy;
    private final DittoMongoClient dittoMongoClient;

    @SuppressWarnings("unused")
    private SearchUpdaterRootActor(final SearchConfig searchConfig,
            final ActorRef pubSubMediator,
            final ThingsSearchPersistence thingsSearchPersistence,
            final TimestampPersistence backgroundSyncPersistence) {

        final ClusterConfig clusterConfig = searchConfig.getClusterConfig();
        final int numberOfShards = clusterConfig.getNumberOfShards();

        final ActorSystem actorSystem = getContext().getSystem();

        final MongoDbConfig mongoDbConfig = searchConfig.getMongoDbConfig();
        dittoMongoClient = MongoClientWrapper.getBuilder(mongoDbConfig)
                .addCommandListener(getCommandListenerOrNull(mongoDbConfig.getMonitoringConfig()))
                .addConnectionPoolListener(getConnectionPoolListenerOrNull(mongoDbConfig.getMonitoringConfig()))
                .build();

        final ShardRegionFactory shardRegionFactory = ShardRegionFactory.getInstance(actorSystem);
        final BlockedNamespaces blockedNamespaces = BlockedNamespaces.of(actorSystem);
        final ActorRef changeQueueActor = startChildActor(ChangeQueueActor.ACTOR_NAME, ChangeQueueActor.props());

        final Props thingUpdaterProps = ThingUpdater.props(pubSubMediator, changeQueueActor);

        final UpdaterConfig updaterConfig = searchConfig.getUpdaterConfig();
        if (!updaterConfig.isEventProcessingActive()) {
            log.warning("Event processing is disabled!");
        }

        final ActorRef thingsShard = shardRegionFactory.getThingsShardRegion(numberOfShards);
        final ActorRef policiesShard = shardRegionFactory.getPoliciesShardRegion(numberOfShards);
        final ActorRef updaterShard =
                shardRegionFactory.getSearchUpdaterShardRegion(numberOfShards, thingUpdaterProps, CLUSTER_ROLE);

        final SearchUpdaterStream searchUpdaterStream =
                SearchUpdaterStream.of(updaterConfig, actorSystem, thingsShard, policiesShard, updaterShard,
                        changeQueueActor, dittoMongoClient.getDefaultDatabase(), blockedNamespaces);
        updaterStreamKillSwitch = searchUpdaterStream.start(getContext(), false);
        updaterStreamWithAcknowledgementsKillSwitch = searchUpdaterStream.start(getContext(), true);

        final ThingsSearchUpdaterPersistence searchUpdaterPersistence =
                MongoThingsSearchUpdaterPersistence.of(dittoMongoClient.getDefaultDatabase());

        pubSubMediator.tell(DistPubSubAccess.put(getSelf()), getSelf());

        final DistributedSub thingEventSub =
                ThingEventPubSubFactory.shardIdOnly(getContext(), numberOfShards, DistributedAcks.empty())
                        .startDistributedSub();
        final Props thingsUpdaterProps =
                ThingsUpdater.props(thingEventSub, updaterShard,
                                        policiesShard, updaterConfig, blockedNamespaces,
                        pubSubMediator);

        thingsUpdaterActor = startChildActor(ThingsUpdater.ACTOR_NAME, thingsUpdaterProps);
        startClusterSingletonActor(NewEventForwarder.ACTOR_NAME,
                NewEventForwarder.props(thingEventSub, updaterShard, blockedNamespaces));

        // start policy event forwarder
        final Props policyEventForwarderProps =
                PolicyEventForwarder.props(pubSubMediator, thingsUpdaterActor, blockedNamespaces,
                        searchUpdaterPersistence);
        startChildActor(PolicyEventForwarder.ACTOR_NAME, policyEventForwarderProps);

        // start background sync actor as cluster singleton
        final Props backgroundSyncActorProps = BackgroundSyncActor.props(
                updaterConfig.getBackgroundSyncConfig(),
                pubSubMediator,
                thingsSearchPersistence,
                backgroundSyncPersistence,
                shardRegionFactory.getPoliciesShardRegion(numberOfShards),
                thingsUpdaterActor
        );
        backgroundSyncActorProxy =
                ClusterUtil.startSingletonProxy(getContext(), CLUSTER_ROLE,
                        startClusterSingletonActor(BackgroundSyncActor.ACTOR_NAME, backgroundSyncActorProps)
                );

        startChildActor(ThingsSearchPersistenceOperationsActor.ACTOR_NAME,
                ThingsSearchPersistenceOperationsActor.props(pubSubMediator, searchUpdaterPersistence,
                        searchConfig.getPersistenceOperationsConfig()));
    }

    @Nullable
    private static CommandListener getCommandListenerOrNull(final MongoDbConfig.MonitoringConfig monitoringConfig) {
        return monitoringConfig.isCommandsEnabled() ? new KamonCommandListener(KAMON_METRICS_PREFIX) : null;
    }

    @Nullable
    private static ConnectionPoolListener getConnectionPoolListenerOrNull(
            final MongoDbConfig.MonitoringConfig monitoringConfig) {

        return monitoringConfig.isConnectionPoolEnabled()
                ? new KamonConnectionPoolListener(KAMON_METRICS_PREFIX)
                : null;
    }

    /**
     * Creates Akka configuration object Props for this SearchUpdaterRootActor.
     *
     * @param searchConfig the configuration settings of the Things-Search service.
     * @param pubSubMediator the PubSub mediator Actor.
     * @param thingsSearchPersistence persistence to access the search index in read-only mode.
     * @param backgroundSyncPersistence persistence for background synchronization.
     * @return a Props object to create this actor.
     */
    public static Props props(final SearchConfig searchConfig,
            final ActorRef pubSubMediator,
            final ThingsSearchPersistence thingsSearchPersistence,
            final TimestampPersistence backgroundSyncPersistence) {

        return Props.create(SearchUpdaterRootActor.class, searchConfig, pubSubMediator, thingsSearchPersistence,
                backgroundSyncPersistence);
    }

    @Override
    public void postStop() throws Exception {
        updaterStreamKillSwitch.shutdown();
        updaterStreamWithAcknowledgementsKillSwitch.shutdown();
        dittoMongoClient.close();
        super.postStop();
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(RetrieveStatisticsDetails.class, cmd -> thingsUpdaterActor.forward(cmd, getContext()))
                .match(RetrieveHealth.class, cmd -> backgroundSyncActorProxy.forward(cmd, getContext()))
                .match(Status.Failure.class, f -> log.error(f.cause(), "Got failure: {}", f))
                .match(StartChildActor.class, this::startChildActor)
                .matchAny(m -> {
                    log.warning("Unknown message: {}", m);
                    unhandled(m);
                })
                .build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return supervisorStrategy;
    }

    private void startChildActor(final StartChildActor message) {
        startChildActor(message.getActorName(), message.getProps());
    }

    private ActorRef startChildActor(final String actorName, final Props props) {
        log.info("Starting child actor <{}>.", actorName);
        return getContext().actorOf(props, actorName);
    }

    private ActorRef startClusterSingletonActor(final String actorName, final Props props) {
        return ClusterUtil.startSingleton(getContext(), SEARCH_ROLE, actorName, props);
    }

}
