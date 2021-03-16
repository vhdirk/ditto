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
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.Arrays;

import org.eclipse.ditto.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link ImmutableEffectedImports}.
 */
public final class ImmutableEffectedImportsTest {

    private EffectedImports underTest = null;

    @Before
    public void setUp() {
        underTest = ImmutableEffectedImports.of(
            Arrays.asList("IncludedEntry1", "IncludedEntry2"), Arrays.asList("ExcludedEntry1", "ExcludedEntry2"));
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableEffectedImports.class,
                areImmutable(),
                provided(ImportedLabels.class).areAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableEffectedImports.class)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testToAndFromJson() {
        final JsonObject effectedImportsJson = underTest.toJson();
        final EffectedImports effectedImports1 = ImmutableEffectedImports.fromJson(effectedImportsJson);

        assertThat(underTest).isEqualTo(effectedImports1);
    }

    @Test
    public void testGetIncludedEntries() {
        assertThat(underTest.getIncludedImportedEntries()).isEqualTo(ImportedLabels.newInstance("IncludedEntry2",
                "IncludedEntry1"));

        assertThat(underTest.getIncludedImportedEntries()).isNotEqualTo(ImportedLabels.newInstance("ExcludedEntry1",
                "ExcludedEntry3"));
    }

    @Test
    public void testGetExcludedEntries() {
        assertThat(underTest.getExcludedImportedEntries())
                .isEqualTo(ImportedLabels.newInstance("ExcludedEntry2", "ExcludedEntry1"));

        assertThat(underTest.getExcludedImportedEntries())
                .isNotEqualTo(ImportedLabels.newInstance("IncludedEntry1", "IncludedEntry3"));
    }
}
