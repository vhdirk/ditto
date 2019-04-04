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
package org.eclipse.ditto.services.concierge.enforcement;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.eclipse.ditto.services.models.concierge.EntityId;
import org.eclipse.ditto.services.utils.akka.controlflow.WithSender;

import akka.actor.ActorRef;
import akka.event.DiagnosticLoggingAdapter;

/**
 * A message together with contextual information about the actor processing it.
 */
final class Contextual<T> implements WithSender<T> {

    private final T message;

    private final ActorRef self;

    private final ActorRef sender;

    private final ActorRef pubSubMediator;

    private final ActorRef conciergeForwarder;

    private final Executor enforcerExecutor;

    private final Duration askTimeout;

    private final DiagnosticLoggingAdapter log;

    @Nullable
    private final EntityId entityId;

    Contextual(final T message, final ActorRef self, final ActorRef sender,
            final ActorRef pubSubMediator, final ActorRef conciergeForwarder,
            final Executor enforcerExecutor, final Duration askTimeout, final DiagnosticLoggingAdapter log,
            @Nullable final EntityId entityId) {
        this.message = message;
        this.self = self;
        this.sender = sender;
        this.pubSubMediator = pubSubMediator;
        this.conciergeForwarder = conciergeForwarder;
        this.enforcerExecutor = enforcerExecutor;
        this.askTimeout = askTimeout;
        this.log = log;
        this.entityId = entityId;
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public ActorRef getSender() {
        return sender;
    }

    @Override
    public <S> Contextual<S> withMessage(final S message) {
        return withReceivedMessage(message, sender);
    }

    ActorRef getSelf() {
        return self;
    }

    ActorRef getPubSubMediator() {
        return pubSubMediator;
    }

    ActorRef getConciergeForwarder() {
        return conciergeForwarder;
    }

    Executor getEnforcerExecutor() {
        return enforcerExecutor;
    }

    Duration getAskTimeout() {
        return askTimeout;
    }

    DiagnosticLoggingAdapter getLog() {
        return log;
    }

    Optional<EntityId> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    <S> Optional<Contextual<S>> tryToMapMessage(final Function<T, Optional<S>> f) {
        return f.apply(getMessage()).map(this::withMessage);
    }

    <S> Contextual<S> withReceivedMessage(final S message, final ActorRef sender) {
        return new Contextual<>(message, self, sender, pubSubMediator, conciergeForwarder, enforcerExecutor, askTimeout,
                log, entityId);
    }

    @Override
    public String toString() {
        return String.format(
                "Contextual[message=%s,self=%s,sender=%s,pubSubMediator=%s,conciergeForwarder=%s,entityId=%s]",
                message.toString(),
                self.toString(),
                sender.toString(),
                pubSubMediator.toString(),
                conciergeForwarder.toString(),
                getEntityId().map(Object::toString).orElse("null"));
    }
}
