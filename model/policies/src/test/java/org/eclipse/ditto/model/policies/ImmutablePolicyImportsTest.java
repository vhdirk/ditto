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
package org.eclipse.ditto.model.policies;

import static org.eclipse.ditto.model.policies.assertions.DittoPolicyAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.assumingFields;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ditto.json.JsonObject;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutablePolicyImports}.
 */
public final class ImmutablePolicyImportsTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutablePolicyImports.class,
                areImmutable(),
                assumingFields("policyImports").areSafelyCopiedUnmodifiableCollectionsWithImmutableElements());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutablePolicyImports.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testToAndFromJson() {
        final Collection<PolicyImport> policyImportList = new ArrayList<>();

        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "firstPolicy"), false, EffectedImports.newInstance(null, null)));
        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "secondPolicy"), true,
                        EffectedImports.newInstance(Arrays.asList("includedLabel1"), null)));
        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "thirdPolicy"), false,
                        EffectedImports.newInstance(null, Arrays.asList("excludedLabel1"))));
        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "fourthPolicy"), false,
                        EffectedImports.newInstance(Arrays.asList("includedLabel1", "includedLabel2"), Arrays.asList("excludedLabel2"))));

        final ImmutablePolicyImports policyImports = ImmutablePolicyImports.of(policyImportList);

        final JsonObject policyImportsJson = policyImports.toJson();
        final PolicyImports PolicyImportsFromJson = ImmutablePolicyImports.fromJson(policyImportsJson);

        assertThat(policyImports).isEqualTo(PolicyImportsFromJson);
    }

    @Test(expected = IllegalArgumentException.class)
    public void importPoliciesWithSameIdsShouldFail() {
        final Collection<PolicyImport> policyImportList = new ArrayList<>();
        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "firstPolicy"), false,
                        EffectedImports.newInstance(null, null)));
        policyImportList.add(PolicyImport.newInstance(PolicyId.of("com.example", "firstPolicy"), true,
                        EffectedImports.newInstance(Arrays.asList("includedLabel1"), null)));

        ImmutablePolicyImports.of(policyImportList);
    }

}