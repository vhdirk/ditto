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

import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyBuilder;
import org.eclipse.ditto.model.policies.PolicyImports;
import org.eclipse.ditto.signals.events.policies.PolicyImportCreated;

/**
 * This strategy handles {@link org.eclipse.ditto.signals.events.policies.PolicyImportCreated} events.
 */
final class PolicyImportCreatedStrategy extends AbstractPolicyEventStrategy<PolicyImportCreated> {

    @Override
    protected PolicyBuilder applyEvent(final PolicyImportCreated pic, final Policy policy,
            final PolicyBuilder policyBuilder) {
        return policyBuilder.setImports(
                policy.getImports()
                        .map(policyImports -> policyImports.setPolicyImport(pic.getPolicyImport()))
                        .orElse(PolicyImports.newInstance(pic.getPolicyImport())));
    }
}