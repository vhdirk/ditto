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
import org.eclipse.ditto.signals.events.policies.PolicyImportCreated;

/**
 * Tests {@link PolicyImportCreatedStrategy}.
 */
public class PolicyImportCreatedStrategyTest extends AbstractPolicyEventStrategyTest<PolicyImportCreated> {

    private static final PolicyImport CREATED = TestConstants.Policy.policyImportWithId("created");

    @Override
    PolicyImportCreatedStrategy getStrategyUnderTest() {
        return new PolicyImportCreatedStrategy();
    }

    @Override
    PolicyImportCreated getPolicyEvent(final Instant instant, final Policy policy) {
        final PolicyId policyId = policy.getEntityId().orElseThrow();
        return PolicyImportCreated.of(policyId, CREATED, 10L, instant, DittoHeaders.empty());
    }

    @Override
    protected void additionalAssertions(final Policy policyWithEventApplied) {
        assertThat(policyWithEventApplied.getImports()
                .flatMap(policyImports -> policyImports.getPolicyImport(CREATED.getImportedPolicyId())))
                .contains(CREATED);
    }
}