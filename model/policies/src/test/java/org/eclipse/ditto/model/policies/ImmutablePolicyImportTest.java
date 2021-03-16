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

import static org.eclipse.ditto.model.policies.TestConstants.Policy.PERMISSION_READ;
import static org.eclipse.ditto.model.policies.TestConstants.Policy.PERMISSION_WRITE;
import static org.eclipse.ditto.model.policies.TestConstants.Policy.RESOURCE_PATH;
import static org.eclipse.ditto.model.policies.TestConstants.Policy.RESOURCE_TYPE;
import static org.eclipse.ditto.model.policies.TestConstants.Policy.SUBJECT;
import static org.eclipse.ditto.model.policies.assertions.DittoPolicyAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.exceptions.DittoJsonException;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutablePolicyImport}.
 */
public final class ImmutablePolicyImportTest {

    private static final PolicyId IMPORTED_POLICY_ID = PolicyId.of("com.example", "importablePolicy");

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutablePolicyImport.class,
                areImmutable(),
                provided(PolicyId.class, EffectedImports.class).areAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutablePolicyImport.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testToAndFromJson() {
        final PolicyImport policyImport = ImmutablePolicyImport.of(IMPORTED_POLICY_ID,
                false,
                EffectedImports.newInstance(
                        List.of("IncludedPolicyImport1, IncludedPolicyImport2"),
                        List.of("ExcludedPolicyImport1, ExcludedPolicyImport2")));

        final JsonObject policyImportJson = policyImport.toJson();
        final PolicyImport policyImport1 = ImmutablePolicyImport.fromJson(policyImport.getImportedPolicyId(), policyImportJson);

        assertThat(policyImport).isEqualTo(policyImport1);
    }

    @Test(expected = NullPointerException.class)
    public void testFromJsonWithNullLabel() {
        ImmutablePolicyImport.fromJson(null, JsonFactory.newObjectBuilder()
                .set("protected", false)
                .set("included", JsonFactory.newArray())
                .set("excluded", JsonFactory.newArray())
                .build());
    }

    @Test(expected = DittoJsonException.class)
    public void testFromJsonEmptyWithPolicyId() {
        final JsonObject jsonObject = JsonFactory.newObjectBuilder().build();

        ImmutablePolicyImport.fromJson(IMPORTED_POLICY_ID, jsonObject);
    }

    @Test(expected = DittoJsonException.class)
    public void testFromJsonOnlySchemaVersion() {
        final JsonObject jsonObject = JsonFactory.newObjectBuilder()
                .set(JsonSchemaVersion.getJsonKey(), JsonSchemaVersion.V_2.toInt())
                .build();

        ImmutablePolicyImport.fromJson(IMPORTED_POLICY_ID, jsonObject);
    }
}
