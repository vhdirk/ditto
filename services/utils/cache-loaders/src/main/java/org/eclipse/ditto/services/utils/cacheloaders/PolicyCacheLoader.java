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
package org.eclipse.ditto.services.utils.cacheloaders;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyImport;
import org.eclipse.ditto.model.policies.PolicyRevision;
import org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicyResponse;
import org.eclipse.ditto.services.utils.cache.CacheInvalidationListener;
import org.eclipse.ditto.services.utils.cache.CacheLookupContext;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.entry.Entry;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.exceptions.PolicyNotAccessibleException;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;

import akka.actor.ActorRef;

/**
 * Loads a policy by asking the policies shard-region-proxy.
 */
@Immutable
public final class PolicyCacheLoader
        implements AsyncCacheLoader<EntityIdWithResourceType, Entry<Policy>>,
        CacheInvalidationListener<EntityIdWithResourceType, Entry<Policy>> {

    private final ActorAskCacheLoader<Policy, Command<?>> delegate;
    private final Map<EntityIdWithResourceType, Set<EntityIdWithResourceType>> policyIdsUsedInImports;
    private final Set<Consumer<EntityIdWithResourceType>> invalidators;

    /**
     * Constructor.
     *
     * @param askTimeout the ask-timeout for communicating with the shard-region-proxy.
     * @param policiesShardRegionProxy the shard-region-proxy.
     */
    public PolicyCacheLoader(final Duration askTimeout, final ActorRef policiesShardRegionProxy) {
        requireNonNull(askTimeout);
        requireNonNull(policiesShardRegionProxy);

        final BiFunction<EntityId, CacheLookupContext, Command<?>> commandCreator =
                PolicyCommandFactory::sudoRetrievePolicy;

        final BiFunction<Object, CacheLookupContext, Entry<Policy>> responseTransformer =
                this::sudoRetrievePolicyResponseToPolicy;

        this.delegate =
                ActorAskCacheLoader.forShard(askTimeout, PolicyCommand.RESOURCE_TYPE, policiesShardRegionProxy,
                        commandCreator, responseTransformer);
        policyIdsUsedInImports = new HashMap<>();
        invalidators = new HashSet<>();
    }

    /**
     * Registers a Consumer to call for when {@link org.eclipse.ditto.services.utils.cache.Cache} entries are
     * invalidated.
     *
     * @param invalidator the Consumer to call for cache invalidation.
     */
    public void registerCacheInvalidator(final Consumer<EntityIdWithResourceType> invalidator) {
        invalidators.add(invalidator);
    }

    @Override
    public CompletableFuture<Entry<Policy>> asyncLoad(final EntityIdWithResourceType key,
            final Executor executor) {
        return delegate.asyncLoad(key, executor);
    }

    private Entry<Policy> sudoRetrievePolicyResponseToPolicy(final Object response,
            @Nullable final CacheLookupContext cacheLookupContext) {
        if (response instanceof SudoRetrievePolicyResponse) {
            final SudoRetrievePolicyResponse sudoRetrievePolicyResponse = (SudoRetrievePolicyResponse) response;
            final Policy policy = sudoRetrievePolicyResponse.getPolicy();
            handleInvalidationOfImportedPolicies(policy, cacheLookupContext);
            final long revision = policy.getRevision().map(PolicyRevision::toLong)
                    .orElseThrow(badPolicyResponse("no revision"));
            return Entry.of(revision, policy);
        } else if (response instanceof PolicyNotAccessibleException) {
            return Entry.nonexistent();
        } else {
            throw new IllegalStateException("expect SudoRetrievePolicyResponse, got: " + response);
        }
    }

    private void handleInvalidationOfImportedPolicies(final Policy policy,
            @Nullable final CacheLookupContext cacheLookupContext) {
        policy.getEntityId()
                .ifPresent(policyIdUsingImports -> {
                    final EntityIdWithResourceType policyIdUsingImportsWithResourceType =
                            EntityIdWithResourceType.of(PolicyCommand.RESOURCE_TYPE,
                                    policyIdUsingImports, cacheLookupContext);
                    policy.getImports().ifPresent(imports ->
                            imports.stream()
                                    .map(PolicyImport::getImportedPolicyId)
                                    .forEach(importedPolicyId -> {
                                        final EntityIdWithResourceType importedPolicyIdWithResourceType =
                                                EntityIdWithResourceType.of(PolicyCommand.RESOURCE_TYPE,
                                                        importedPolicyId, cacheLookupContext);
                                        final Set<EntityIdWithResourceType> alreadyUsedImports =
                                                policyIdsUsedInImports.getOrDefault(
                                                        importedPolicyIdWithResourceType, new HashSet<>());
                                        alreadyUsedImports.add(policyIdUsingImportsWithResourceType);
                                        policyIdsUsedInImports.put(importedPolicyIdWithResourceType,
                                                alreadyUsedImports);
                                    }));
                });
    }

    private static Supplier<RuntimeException> badPolicyResponse(final String message) {
        return () -> new IllegalStateException("Bad SudoRetrievePolicyResponse: " + message);
    }

    @Override
    public void onCacheEntryInvalidated(final EntityIdWithResourceType key, @Nullable final Entry<Policy> value) {
        Optional.ofNullable(policyIdsUsedInImports.get(key))
                .ifPresent(affectedPoliciesUsingImportedPolicyId ->
                        affectedPoliciesUsingImportedPolicyId.forEach(id ->
                                invalidators.forEach(ivd -> ivd.accept(id))
                        )
                );
    }
}
