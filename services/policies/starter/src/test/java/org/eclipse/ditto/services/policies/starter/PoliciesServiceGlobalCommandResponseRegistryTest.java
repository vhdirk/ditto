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
package org.eclipse.ditto.services.policies.starter;

import org.eclipse.ditto.services.models.policies.commands.sudo.SudoRetrievePolicyResponse;
import org.eclipse.ditto.services.utils.health.RetrieveHealthResponse;
import org.eclipse.ditto.services.utils.test.GlobalCommandResponseRegistryTestCases;
import org.eclipse.ditto.signals.commands.cleanup.CleanupPersistenceResponse;
import org.eclipse.ditto.signals.commands.common.RetrieveConfigResponse;
import org.eclipse.ditto.signals.commands.common.purge.PurgeEntitiesResponse;
import org.eclipse.ditto.signals.commands.devops.RetrieveLoggerConfigResponse;
import org.eclipse.ditto.signals.commands.messages.SendClaimMessageResponse;
import org.eclipse.ditto.signals.commands.namespaces.PurgeNamespaceResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyErrorResponse;
import org.eclipse.ditto.signals.commands.policies.actions.ActivateTokenIntegrationResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeletePolicyImportResponse;
import org.eclipse.ditto.signals.commands.policies.modify.DeleteSubjectResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImportResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImportsResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveResourceResponse;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeaturePropertyResponse;
import org.eclipse.ditto.signals.commands.things.query.RetrieveFeatureResponse;
import org.eclipse.ditto.signals.commands.thingsearch.SearchErrorResponse;
import org.eclipse.ditto.signals.commands.thingsearch.query.QueryThingsResponse;

public final class PoliciesServiceGlobalCommandResponseRegistryTest extends GlobalCommandResponseRegistryTestCases {

    public PoliciesServiceGlobalCommandResponseRegistryTest() {
        super(
                SudoRetrievePolicyResponse.class,
                RetrieveFeatureResponse.class,
                ModifyFeaturePropertyResponse.class,
                SendClaimMessageResponse.class,
                PurgeNamespaceResponse.class,
                RetrieveResourceResponse.class,
                DeleteSubjectResponse.class,
                ActivateTokenIntegrationResponse.class,
                ThingErrorResponse.class,
                PolicyErrorResponse.class,
                RetrieveLoggerConfigResponse.class,
                CleanupPersistenceResponse.class,
                RetrieveConfigResponse.class,
                RetrieveHealthResponse.class,
                PurgeEntitiesResponse.class,
                ModifyPolicyImportResponse.class,
                ModifyPolicyImportsResponse.class,
                DeletePolicyImportResponse.class,

                // added due to ditto-model-placeholders
                QueryThingsResponse.class,
                SearchErrorResponse.class
        );
    }

}
