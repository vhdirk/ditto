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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyImport;
import org.eclipse.ditto.services.policies.common.config.DefaultPolicyConfig;
import org.eclipse.ditto.services.policies.persistence.TestConstants;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImport;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImportResponse;
import org.eclipse.ditto.signals.events.policies.PolicyImportCreated;
import org.eclipse.ditto.signals.events.policies.PolicyImportModified;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.ConfigFactory;

/**
 * Unit test for {@link ModifyPolicyImportStrategy}.
 */
public final class ModifyPolicyImportStrategyTest extends AbstractPolicyCommandStrategyTest {

    private ModifyPolicyImportStrategy underTest;

    @Before
    public void setUp() {
        underTest = new ModifyPolicyImportStrategy(DefaultPolicyConfig.of(ConfigFactory.load("policy-test")));
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ModifyPolicyImportStrategy.class, areImmutable());
    }

    @Test
    public void createPolicyImport() {
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        final PolicyImport policyImport = TestConstants.Policy.policyImportWithId("newImport");

        final ModifyPolicyImport command = ModifyPolicyImport.of(TestConstants.Policy.POLICY_ID, policyImport,
                dittoHeaders);

        assertModificationResult(underTest, TestConstants.Policy.POLICY, command,
                PolicyImportCreated.class,
                event -> {
                    assertThat((CharSequence) event.getPolicyImport().getImportedPolicyId())
                            .isEqualTo(policyImport.getImportedPolicyId());
                },
                ModifyPolicyImportResponse.class,
                response -> {
                }
        );
    }

    @Test
    public void modifyPolicyImport() {
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        final Policy policyWithImport = TestConstants.Policy.POLICY;

        final PolicyImport policyImport = TestConstants.Policy.POLICY_IMPORT_WITH_ENTRIES;

        final ModifyPolicyImport command = ModifyPolicyImport.of(TestConstants.Policy.POLICY_ID, policyImport,
                dittoHeaders);

        assertModificationResult(underTest, policyWithImport, command,
                PolicyImportModified.class,
                event -> {
                    assertThat((CharSequence) event.getPolicyImport().getImportedPolicyId())
                            .isEqualTo(policyImport.getImportedPolicyId());
                },
                ModifyPolicyImportResponse.class,
                response -> {
                }
        );
    }
}
