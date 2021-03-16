/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.enforcers.PolicyEnforcers;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyImportHelper;
import org.eclipse.ditto.model.policies.PolicyRevision;
import org.eclipse.ditto.services.utils.cache.Cache;
import org.eclipse.ditto.services.utils.cache.EntityIdWithResourceType;
import org.eclipse.ditto.services.utils.cache.entry.Entry;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;

/**
 * Loads a policy-enforcer by asking the policies shard-region-proxy.
 */
@Immutable
public final class PolicyEnforcerCacheLoader implements AsyncCacheLoader<EntityIdWithResourceType,
        Entry<PolicyEnforcer>> {

    private final Cache<EntityIdWithResourceType, Entry<Policy>> policyCache;

    /**
     * Constructor.
     *
     * @param policyCache policy cache to load policies from.
     */
    public PolicyEnforcerCacheLoader(final Cache<EntityIdWithResourceType, Entry<Policy>> policyCache) {
        this.policyCache = checkNotNull(policyCache, "policyCache");
    }

    @Override
    public CompletableFuture<Entry<PolicyEnforcer>> asyncLoad(final EntityIdWithResourceType key,
            final Executor executor) {
        return policyCache.get(key)
                .thenApply(optionalPolicyEntry -> optionalPolicyEntry
                        .filter(Entry::exists)
                        .map(entry -> {
                            final Policy initialPolicy = entry.getValueOrThrow();
                            final Set<PolicyEntry> mergedPolicyEntriesSet =
                                    PolicyImportHelper.mergeImportedPolicyEntries(initialPolicy,
                                            this::policyLoader);
                            final Policy mergedPolicy = initialPolicy.toBuilder()
                                    .setAll(mergedPolicyEntriesSet)
                                    .build();
                            final long revision = initialPolicy.getRevision().map(PolicyRevision::toLong)
                                    .orElseThrow(() -> new IllegalStateException("Bad loaded Policy: no revision"));
                            return Entry.of(revision, PolicyEnforcer.of(mergedPolicy,
                                    PolicyEnforcers.defaultEvaluator(mergedPolicyEntriesSet)));
                        })
                        .orElse(Entry.nonexistent())
                );
    }

    private Optional<Policy> policyLoader(final PolicyId policyId) {
        return policyCache.getBlocking(EntityIdWithResourceType.of(PolicyCommand.RESOURCE_TYPE, policyId))
                .filter(Entry::exists)
                .map(Entry::getValueOrThrow);
    }

}
