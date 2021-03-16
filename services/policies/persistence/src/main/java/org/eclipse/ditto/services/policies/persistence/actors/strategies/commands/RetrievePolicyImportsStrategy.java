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

import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyImports;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyImports;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyImportsResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyImports}.
 */
final class RetrievePolicyImportsStrategy extends AbstractPolicyQueryCommandStrategy<RetrievePolicyImports> {

    RetrievePolicyImportsStrategy(final PolicyConfig policyConfig) {
        super(RetrievePolicyImports.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy policy,
            final long nextRevision,
            final RetrievePolicyImports command,
            @Nullable final Metadata metadata) {

        final PolicyId policyId = context.getState();
        if (policy != null) {
            final Optional<PolicyImports> imports = policy.getImports();
            if (imports.isPresent()) {
                final WithDittoHeaders<?> response = appendETagHeaderIfProvided(command,
                        RetrievePolicyImportsResponse.of(policyId, imports.get(), command.getDittoHeaders()), policy);
                return ResultFactory.newQueryResult(command, response);
            }
        }
        return ResultFactory.newErrorResult(policyImportsNotFound(policyId, command.getDittoHeaders()), command);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final RetrievePolicyImports command, @Nullable final Policy newEntity) {
        return Optional.ofNullable(newEntity).map(Policy::getImports).flatMap(EntityTag::fromEntity);
    }
}
