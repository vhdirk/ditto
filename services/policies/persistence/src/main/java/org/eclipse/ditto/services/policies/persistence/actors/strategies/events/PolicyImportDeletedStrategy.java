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
import org.eclipse.ditto.signals.events.policies.PolicyEntryDeleted;

/**
 * This strategy handles {@link org.eclipse.ditto.signals.events.policies.PolicyEntryDeleted} events.
 */
final class PolicyEntryDeletedStrategy extends AbstractPolicyEventStrategy<PolicyEntryDeleted> {

    @Override
    protected PolicyBuilder applyEvent(final PolicyEntryDeleted ped, final PolicyBuilder policyBuilder) {
        // TODO: DVH: Can I just create PolicyBuilder::getImports for this?
        return policyBuilder; //.setImports(PolicyImports.newInstance(pem.getPolicyImport()));

    }

}
