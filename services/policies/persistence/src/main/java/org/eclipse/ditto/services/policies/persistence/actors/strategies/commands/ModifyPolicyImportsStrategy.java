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

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyImports;
import org.eclipse.ditto.model.policies.PolicyTooLargeException;
import org.eclipse.ditto.services.models.policies.PoliciesValidator;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandSizeValidator;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImports;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImportsResponse;
import org.eclipse.ditto.signals.events.policies.PolicyImportsModified;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyImports} command.
 */
final class ModifyPolicyImportsStrategy extends AbstractPolicyCommandStrategy<ModifyPolicyImports, PolicyEvent<?>> {

    ModifyPolicyImportsStrategy(final PolicyConfig policyConfig) {
        super(ModifyPolicyImports.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy entity,
            final long nextRevision,
            final ModifyPolicyImports command,
            @Nullable final Metadata metadata) {

        final PolicyImports policyImports = command.getPolicyImports();
        final JsonObject policyImportsJson = policyImports.toJson();

        try {
            PolicyCommandSizeValidator.getInstance().ensureValidSize(
                    policyImportsJson::getUpperBoundForStringSize,
                    () -> policyImportsJson.toString().length(),
                    command::getDittoHeaders);
        } catch (final PolicyTooLargeException e) {
            return ResultFactory.newErrorResult(e, command);
        }

        final DittoHeadersBuilder<?, ?> adjustedHeadersBuilder = command.getDittoHeaders().toBuilder();
        final DittoHeaders adjustedHeaders = adjustedHeadersBuilder.build();
        final ModifyPolicyImports adjustedCommand = ModifyPolicyImports.of(command.getEntityId(),
                policyImports,
                adjustedHeaders);

        final PoliciesValidator validator = PoliciesValidator.newInstance(entity);

        if (validator.isValid()) {
            final PolicyId policyId = context.getState();
            final PolicyImportsModified policyImportsModified = PolicyImportsModified.of(policyId, policyImports,
                    nextRevision, getEventTimestamp(), adjustedHeaders);
            final WithDittoHeaders<?> response = appendETagHeaderIfProvided(adjustedCommand,
                    ModifyPolicyImportsResponse.modified(policyId, adjustedHeaders), entity);

            return ResultFactory.newMutationResult(adjustedCommand, policyImportsModified, response);
        } else {
            return ResultFactory.newErrorResult(
                    policyInvalid(context.getState(), validator.getReason().orElse(null), adjustedHeaders),
                    command);
        }
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final ModifyPolicyImports command,
            @Nullable final Policy previousEntity) {
        return Optional.ofNullable(previousEntity).map(Policy::getImports).flatMap(EntityTag::fromEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final ModifyPolicyImports command, @Nullable final Policy newEntity) {
        return Optional.of(command.getPolicyImports()).flatMap(EntityTag::fromEntity);
    }
}
