/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.concierge.starter.actors;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Collections;

import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.services.utils.cache.Cache;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.entry.Entry;
import org.eclipse.ditto.services.utils.cacheloaders.PolicyEnforcer;
import org.eclipse.ditto.services.utils.cluster.AbstractPubSubListenerActor;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * An actor which subscribes to Policy Events and updates caches when necessary.
 */
public final class PolicyCacheUpdateActor extends AbstractPubSubListenerActor {

    /**
     * The name of this actor.
     */
    public static final String ACTOR_NAME = "policyCacheUpdater";

    private final Cache<EntityIdWithResourceType, Entry<Policy>> policyCache;
    private final Cache<EntityIdWithResourceType, Entry<PolicyEnforcer>> policyEnforcerCache;

    @SuppressWarnings("unused") // called via props() reflection
    private PolicyCacheUpdateActor(
            final Cache<EntityIdWithResourceType, Entry<Policy>> policyCache,
            final Cache<EntityIdWithResourceType, Entry<PolicyEnforcer>> policyEnforcerCache,
            final ActorRef pubSubMediator) {

        super(pubSubMediator, Collections.singleton(PolicyEvent.TYPE_PREFIX));
        this.policyCache = policyCache;
        this.policyEnforcerCache = policyEnforcerCache;
    }

    /**
     * Create an Akka {@code Props} object for this actor.
     *
     * @param policyCache the policy cache.
     * @param policyEnforcerCache the policy-enforcer cache.
     * @param pubSubMediator Akka pub-sub mediator.
     * @return Akka {@code Props} object.
     * @throws java.lang.NullPointerException if any argument is {@code null}.
     */
    public static Props props(
            final Cache<EntityIdWithResourceType, Entry<Policy>> policyCache,
            final Cache<EntityIdWithResourceType, Entry<PolicyEnforcer>> policyEnforcerCache,
            final ActorRef pubSubMediator) {
        checkNotNull(policyCache, "policyCache");
        checkNotNull(policyEnforcerCache, "policyEnforcerCache");
        checkNotNull(pubSubMediator, "pubSubMediator");

        return Props.create(PolicyCacheUpdateActor.class, policyCache, policyEnforcerCache, pubSubMediator);
    }

    @Override
    protected Receive handleEvents() {
        return receiveBuilder()
                .match(PolicyEvent.class, this::handleEvent)
                .build();
    }

    private void handleEvent(final PolicyEvent<?> policyEvent) {
        final EntityIdWithResourceType key =
                EntityIdWithResourceType.of(PolicyCommand.RESOURCE_TYPE, policyEvent.getEntityId());
        policyCache.invalidate(key);
        policyEnforcerCache.invalidate(key);
    }
}
