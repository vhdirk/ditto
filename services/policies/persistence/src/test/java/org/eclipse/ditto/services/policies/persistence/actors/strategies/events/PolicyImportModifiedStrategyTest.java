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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyImport;
import org.eclipse.ditto.services.policies.persistence.TestConstants;
import org.eclipse.ditto.signals.events.policies.PolicyImportModified;

/**
 * Tests {@link PolicyImportModifiedStrategy}.
 */
public class PolicyImportModifiedStrategyTest extends AbstractPolicyEventStrategyTest<PolicyImportModified> {

    private static final PolicyImport MODIFIED = TestConstants.Policy.POLICY_IMPORT_WITH_ENTRIES;

    @Override
    PolicyImportModifiedStrategy getStrategyUnderTest() {
        return new PolicyImportModifiedStrategy();
    }

    @Override
    PolicyImportModified getPolicyEvent(final Instant instant, final Policy policy) {
        final PolicyId policyId = policy.getEntityId().orElseThrow();
        return PolicyImportModified.of(policyId, MODIFIED, 10L, instant, DittoHeaders.empty());
    }

    @Override
    protected void additionalAssertions(final Policy policyWithEventApplied) {
        assertThat(policyWithEventApplied.getImports().get().getPolicyImport(MODIFIED.getImportedPolicyId())).contains(
                MODIFIED);
    }
}