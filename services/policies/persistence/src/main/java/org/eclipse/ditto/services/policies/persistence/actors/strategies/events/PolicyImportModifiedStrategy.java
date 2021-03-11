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

import org.eclipse.ditto.model.policies.PolicyBuilder;
import org.eclipse.ditto.model.policies.PolicyImports;
import org.eclipse.ditto.signals.events.policies.PolicyImportModified;

/**
 * This strategy handles {@link org.eclipse.ditto.signals.events.policies.PolicyImportModified} events.
 */
final class PolicyImportModifiedStrategy extends AbstractPolicyEventStrategy<PolicyImportModified> {

    @Override
    protected PolicyBuilder applyEvent(final PolicyImportModified pem, final PolicyBuilder policyBuilder) {
        // TODO: DVH: Verify if the builder already contains the previous imports
        return policyBuilder.setImports(PolicyImports.newInstance(pem.getPolicyImport()));
    }

}
