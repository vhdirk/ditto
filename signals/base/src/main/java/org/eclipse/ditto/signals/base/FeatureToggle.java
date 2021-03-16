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
package org.eclipse.ditto.signals.base;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;

/**
 * Decides based on system properties whether certain features of Ditto are enabled or throws an
 * {@link UnsupportedSignalException} if a feature is disabled.
 */
public final class FeatureToggle {

    /**
     * System property name of the property defining whether the merge feature is enabled.
     */
    public static final String MERGE_THINGS_ENABLED = "ditto.devops.feature.merge-things-enabled";

    /**
     * System property name of the property defining whether the policy imports feature is enabled.
     */
    public static final String POLICY_IMPORTS_ENABLED = "ditto.devops.feature.policy-imports-enabled";

    /**
     * Resolves the system property {@value MERGE_THINGS_ENABLED}.
     */
    private static final boolean IS_MERGE_THINGS_ENABLED = resolveProperty(MERGE_THINGS_ENABLED);

    /**
     * Resolves the system property {@value POLICY_IMPORTS_ENABLED}.
     */
    private static final boolean IS_POLICY_IMPORTS_ENABLED = resolveProperty(POLICY_IMPORTS_ENABLED);

    private static boolean resolveProperty(final String propertyName) {
        final String propertyValue = System.getProperty(propertyName, Boolean.TRUE.toString());
        return !Boolean.FALSE.toString().equalsIgnoreCase(propertyValue);
    }

    private FeatureToggle() {
        throw new AssertionError();
    }

    /**
     * Checks if the merge feature is enabled based on the system property {@value MERGE_THINGS_ENABLED}.
     *
     * @param signal the name of the signal that was supposed to be processed
     * @param dittoHeaders headers used to build exception
     * @return the unmodified headers parameters
     * @throws UnsupportedSignalException if the system property
     * {@value MERGE_THINGS_ENABLED} resolves to {@code false}
     */
    public static DittoHeaders checkMergeFeatureEnabled(final String signal, final DittoHeaders dittoHeaders) {
        if (!IS_MERGE_THINGS_ENABLED) {
            throw UnsupportedSignalException
                    .newBuilder(signal)
                    .dittoHeaders(dittoHeaders)
                    .build();
        }
        return dittoHeaders;
    }

    /**
     * Checks if the policy imports feature is enabled based on the system property {@value POLICY_IMPORTS_ENABLED}.
     *
     * @param signal the name of the signal that was supposed to be processed
     * @param dittoHeaders headers used to build exception
     * @return the unmodified headers parameters
     * @throws UnsupportedSignalException if the system property
     * {@value MERGE_THINGS_ENABLED} resolves to {@code false}
     */
    public static DittoHeaders checkPolicyImportsFeatureEnabled(final String signal, final DittoHeaders dittoHeaders) {
        return checkPolicyImportsFeatureEnabled(signal, dittoHeaders, null);
    }

    /**
     * Checks if the policy imports feature is enabled based on the system property {@value POLICY_IMPORTS_ENABLED}.
     *
     * @param signal the name of the signal that was supposed to be processed
     * @param dittoHeaders headers used to build exception
     * @param description optional description why the feature is not enabled to provide more help/context for the
     * built exception if it is disabled.
     * @return the unmodified headers parameters
     * @throws UnsupportedSignalException if the system property
     * {@value MERGE_THINGS_ENABLED} resolves to {@code false}
     */
    public static DittoHeaders checkPolicyImportsFeatureEnabled(final String signal, final DittoHeaders dittoHeaders,
            @Nullable final String description) {
        if (!IS_POLICY_IMPORTS_ENABLED) {
            final UnsupportedSignalException.Builder builder = UnsupportedSignalException
                    .newBuilder(signal);
            if (null != description) {
                builder.description(description);
            }
            throw builder.dittoHeaders(dittoHeaders)
                    .build();
        }
        return dittoHeaders;
    }
}
