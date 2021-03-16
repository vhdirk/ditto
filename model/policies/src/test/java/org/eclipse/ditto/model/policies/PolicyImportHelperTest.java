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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.ditto.json.JsonPointer;
import org.junit.Test;

/**
 * Unit test for {@link PolicyImportHelper}.
 */
public final class PolicyImportHelperTest {


    private static final Label END_USER_LABEL = Label.of("EndUser");
    private static final JsonPointer END_USER_RESOURCE_1 = JsonPointer.of("foo/bar");
    private static final SubjectId END_USER_SUBJECT_ID_1 = SubjectId.newInstance(SubjectIssuer.GOOGLE, "myself");
    private static final SubjectType END_USER_SUBJECT_TYPE_1 = SubjectType.newInstance("endUserSubjectType1");
    private static final EffectedPermissions END_USER_EFFECTED_PERMISSIONS_1 = EffectedPermissions.newInstance(
            Permissions.newInstance(TestConstants.Policy.PERMISSION_READ),
            Permissions.newInstance(TestConstants.Policy.PERMISSION_WRITE));
    private static final Label SUPPORT_LABEL = Label.of("SupportGroup");

    private static final PolicyId IMPORTED_POLICY_ID = PolicyId.of("com.example", "myImportedPolicy");
    private static final PolicyId POLICY_ID = PolicyId.of("com.example", "myPolicy");

    private static PolicyEntry createPolicyEntry2() {
        return ImmutablePolicyEntry.of(SUPPORT_LABEL,
                Subjects.newInstance(Subject.newInstance(SubjectIssuer.GOOGLE, "someGroup")),
                Resources.newInstance(
                        Resource.newInstance(TestConstants.Policy.RESOURCE_TYPE, JsonPointer.empty(),
                                EffectedPermissions.newInstance(
                                        Permissions.newInstance(TestConstants.Policy.PERMISSION_READ,
                                                TestConstants.Policy.PERMISSION_WRITE),
                                        Permissions.none()))));
    }

    private static PolicyEntry createPolicyEntry1() {
        return ImmutablePolicyEntry.of(END_USER_LABEL,
                Subjects.newInstance(Subject.newInstance(END_USER_SUBJECT_ID_1, END_USER_SUBJECT_TYPE_1)),
                Resources.newInstance(Resource.newInstance(TestConstants.Policy.RESOURCE_TYPE, END_USER_RESOURCE_1,
                        END_USER_EFFECTED_PERMISSIONS_1)));
    }

    private static Policy createImportedPolicy() {
        final List<PolicyEntry> policyEntries = Arrays.asList(createPolicyEntry2());
        return ImmutablePolicy.of(
                IMPORTED_POLICY_ID, PolicyLifecycle.ACTIVE, PolicyRevision.newInstance(1), null, null,
                null, policyEntries);
    }


    private static Policy createPolicy(final Iterable<String> includedEntries, final Iterable<String> excludedEntries) {
        final List<PolicyEntry> policyEntries = Arrays.asList(createPolicyEntry1());
        return ImmutablePolicy.of(
                POLICY_ID, PolicyLifecycle.ACTIVE, PolicyRevision.newInstance(1), null, null,
                PolicyImports.newInstance(
                    PoliciesModelFactory.newPolicyImport(
                        IMPORTED_POLICY_ID, false, EffectedImports.newInstance(includedEntries,
                                excludedEntries))), policyEntries);
    }


    @Test
    public void importAllPolicyEntries() {

        final Policy importedPolicy = createImportedPolicy();
        final Function<PolicyId, Optional<Policy>> policyLoader = (policyId) -> {
            if (IMPORTED_POLICY_ID.equals(policyId)) {
                return Optional.of(importedPolicy);
            }
            return Optional.empty();
        };

        final Policy policy = createPolicy(null, null);

        final Set<PolicyEntry> entries = PolicyImportHelper.mergeImportedPolicyEntries(policy, policyLoader);

        assertThat(entries).isEqualTo(Arrays.asList(createPolicyEntry1(), createPolicyEntry2()));

    }
}
