package org.eclipse.ditto.services.utils.persistence.mongo.streaming;
/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.eclipse.ditto.services.utils.persistence.mongo.DittoMongoClient;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoClientWrapper;
import org.eclipse.ditto.services.utils.test.mongo.MongoDbResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.testkit.javadsl.TestKit;

/**
 * Tests {@link MongoTimestampPersistence}.
 */
public final class MongoSearchSyncPersistenceIT {

    private static MongoDbResource mongoResource;
    private static DittoMongoClient mongoClient;
    private static final String KNOWN_COLLECTION = "knownCollection";

    private ActorSystem actorSystem;
    private ActorMaterializer materializer;
    private MongoTimestampPersistence syncPersistence;

    @BeforeClass
    public static void startMongoResource() {
        mongoResource = new MongoDbResource("localhost");
        mongoResource.start();
        mongoClient = MongoClientWrapper.getBuilder()
                .hostnameAndPort(mongoResource.getBindIp(), mongoResource.getPort())
                .defaultDatabaseName("testSearchDB")
                .connectionPoolMaxSize(100)
                .connectionPoolMaxWaitQueueSize(500_000)
                .connectionPoolMaxWaitTime(Duration.ofSeconds(30))
                .build();
    }

    @AfterClass
    public static void stopMongoResource() {
        try {
            if (null != mongoClient) {
                mongoClient.close();
            }
            if (null != mongoResource) {
                mongoResource.stop();
            }
        } catch (final IllegalStateException e) {
            System.err.println("IllegalStateException during shutdown of MongoDB: " + e.getMessage());
        }
    }

    @Before
    public void setUp() {
        final Config config = ConfigFactory.load("test");
        actorSystem = ActorSystem.create("AkkaTestSystem", config);
        actorSystem = ActorSystem.create("actors");
        materializer = ActorMaterializer.create(actorSystem);
        syncPersistence = MongoTimestampPersistence.initializedInstance(KNOWN_COLLECTION, mongoClient, materializer);
    }

    @After
    public void after() {
        if (null != mongoClient) {
            runBlocking(Source.fromPublisher(mongoClient.getCollection(KNOWN_COLLECTION).drop()));
        }
        if (null != actorSystem) {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }

    /**
     * Checks that an empty {@link Optional} is returned when the timestamp has not yet been persisted.
     */
    @Test
    public void retrieveFallbackForLastSuccessfulSyncTimestamp() {
        final Optional<Instant> actualTs = syncPersistence.getTimestamp();

        assertThat(actualTs).isEmpty();
    }

    /**
     * Checks updating and retrieving the timestamp afterwards.
     */
    @Test
    public void updateAndRetrieveLastSuccessfulSyncTimestamp() {
        final Instant ts = Instant.now();

        runBlocking(syncPersistence.setTimestamp(ts));

        final Optional<Instant> persistedTs = syncPersistence.getTimestamp();
        assertThat(persistedTs).hasValue(ts);
    }

    private void runBlocking(final Source<?, ?>... publishers) {
        Stream.of(publishers)
                .map(p -> p.runWith(Sink.ignore(), materializer))
                .map(CompletionStage::toCompletableFuture)
                .forEach(MongoSearchSyncPersistenceIT::finishCompletableFuture);
    }

    private static void finishCompletableFuture(final Future future) {
        try {
            future.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw mapAsRuntimeException(e);
        }
    }

    private static RuntimeException mapAsRuntimeException(final Throwable t) {
        // shortcut: RTEs can be returned as-is
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }

        // for ExecutionExceptions, extract the cause
        if (t instanceof ExecutionException && t.getCause() != null) {
            return mapAsRuntimeException(t.getCause());
        }

        // wrap non-RTEs as IllegalStateException
        return new IllegalStateException(t);
    }

}

