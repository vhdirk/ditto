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
package org.eclipse.ditto.services.utils.cache;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.services.utils.cache.config.CacheConfig;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

/**
 * Creates a cache configured by a {@link org.eclipse.ditto.services.utils.cache.config.CacheConfig}.
 */
@Immutable
public final class CacheFactory {

    private CacheFactory() {
        throw new AssertionError();
    }

    /**
     * Create a new entity ID from the given  {@code resourceType} and {@code id}.
     *
     * @param resourceType the resource type.
     * @param id the entity ID.
     * @return the entity ID with resource type object.
     */
    public static EntityIdWithResourceType newEntityId(final String resourceType, final EntityId id) {
        return ImmutableEntityIdWithResourceType.of(resourceType, id);
    }

    /**
     * Create a new context for cache lookups with the provided {@code dittoHeaders} and {@code jsonFieldSelector}.
     *
     * @param dittoHeaders the DittoHeaders to use in the cache lookup context.
     * @param jsonFieldSelector the JsonFieldSelector to use in the cache lookup context.
     * @return the created context.
     */
    public static CacheLookupContext newCacheLookupContext(
            @Nullable final DittoHeaders dittoHeaders, @Nullable  final JsonFieldSelector jsonFieldSelector) {
        return ImmutableCacheLookupContext.of(dittoHeaders, jsonFieldSelector);
    }

    /**
     * Create a new entity ID from the given  {@code resourceType} and {@code id}.
     *
     * @param resourceType the resource type.
     * @param id the entity ID.
     * @param cacheLookupContext additional context information to use for the cache lookup.
     * @return the entity ID with resource type object.
     */
    public static EntityIdWithResourceType newEntityId(final String resourceType, final EntityId id,
            @Nullable final CacheLookupContext cacheLookupContext) {
        return ImmutableEntityIdWithResourceType.of(resourceType, id, cacheLookupContext);
    }

    /**
     * Deserialize entity ID with resource type from a string.
     *
     * @param string the string.
     * @return the entity ID with resource type.
     * @throws IllegalArgumentException if the string does not have the expected format.
     */
    public static EntityIdWithResourceType readEntityIdFrom(final String string) {
        return ImmutableEntityIdWithResourceType.readFrom(string);
    }

    /**
     * Creates a cache.
     *
     * @param cacheConfig the {@link org.eclipse.ditto.services.utils.cache.config.CacheConfig} which defines the cache's configuration.
     * @param cacheName the name of the cache or {@code null} if metrics should be disabled. Used as metric label.
     * @param executor the executor to use in the cache.
     * @param <K> the type of the cache keys.
     * @param <V> the type of the cache values.
     * @return the created cache.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static <K, V> Cache<K, V> createCache(final CacheConfig cacheConfig, @Nullable final String cacheName,
            final Executor executor) {

        return CaffeineCache.of(caffeine(cacheConfig, executor), cacheName);
    }

    /**
     * Creates a cache.
     *
     * @param cacheLoader the cache loader.
     * @param cacheConfig the the cache's configuration.
     * @param cacheName the name of the cache or {@code null} if metrics should be disabled. Used as metric label.
     * @param executor the executor to use in the cache.
     * @param <K> the type of the cache keys.
     * @param <V> the type of the cache values.
     * @return the created cache.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static <K, V> Cache<K, V> createCache(final AsyncCacheLoader<K, V> cacheLoader,
            final CacheConfig cacheConfig,
            @Nullable final String cacheName,
            final Executor executor) {

        checkNotNull(cacheLoader, "AsyncCacheLoader");

        return CaffeineCache.of(caffeine(cacheConfig, executor), cacheLoader, cacheName);
    }

    private static Caffeine<Object, Object> caffeine(final CacheConfig cacheConfig, final Executor executor) {
        checkNotNull(cacheConfig, "CacheConfig");
        checkNotNull(executor, "Executor");

        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        caffeine.maximumSize(cacheConfig.getMaximumSize());

        if (!cacheConfig.getExpireAfterCreate().isZero()) {
            // special case "expire-after-create" needs the following API invocation of Caffeine:
            caffeine.expireAfter(new Expiry<Object, Object>() {
                @Override
                public long expireAfterCreate(final Object key, final Object value, final long currentTime) {
                    return cacheConfig.getExpireAfterCreate().toNanos();
                }

                @Override
                public long expireAfterUpdate(final Object key, final Object value, final long currentTime,
                        final long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(final Object key, final Object value, final long currentTime,
                        final long currentDuration) {
                    return currentDuration;
                }
            });
        } else {
            caffeine.expireAfterWrite(cacheConfig.getExpireAfterWrite());
            caffeine.expireAfterAccess(cacheConfig.getExpireAfterAccess());
        }
        caffeine.executor(executor);
        return caffeine;
    }

}
