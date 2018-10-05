/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.signals.commands.policies.exceptions;

import java.net.URI;
import java.text.MessageFormat;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeExceptionBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.PolicyException;

/**
 * Thrown if a {@link org.eclipse.ditto.model.policies.Resource} could not be modified because the requester had
 * insufficient permissions.
 */
@Immutable
public final class ResourceNotModifiableException extends DittoRuntimeException implements PolicyException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = ERROR_CODE_PREFIX + "resource.notmodifiable";

    private static final String MESSAGE_TEMPLATE = "The Resource ''{0}'' of the PolicyEntry with Label ''{1}'' on" +
            " the Policy with ID ''{2}'' could not be modified as the requester had insufficient permissions.";

    private static final String DEFAULT_DESCRIPTION = "Check if the ID of the Policy, the Label of the PolicyEntry" +
            " and the path of your requested Resource was correct and you have sufficient permissions.";

    private static final long serialVersionUID = -3333742392554340824L;

    private ResourceNotModifiableException(final DittoHeaders dittoHeaders,
            final String message,
            final String description,
            final Throwable cause,
            final URI href) {

        super(ERROR_CODE, HttpStatusCode.FORBIDDEN, dittoHeaders, message, description, cause, href);
    }

    /**
     * A mutable builder for a {@code ResourceNotModifiableException}.
     *
     * @param policyId the identifier of the Policy.
     * @param label the Label of the PolicyEntry.
     * @param path the path of the Resource.
     * @return the builder.
     */
    public static Builder newBuilder(final String policyId, final CharSequence label, final CharSequence path) {
        return new Builder(policyId, label, path);
    }

    /**
     * Constructs a new {@code ResourceNotModifiableException} object with given message.
     *
     * @param message detail message. This message can be later retrieved by the {@link #getMessage()} method.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new ResourceNotModifiableException.
     */
    public static ResourceNotModifiableException fromMessage(final String message,
            final DittoHeaders dittoHeaders) {
        return new Builder()
                .dittoHeaders(dittoHeaders)
                .message(message)
                .build();
    }

    /**
     * Constructs a new {@code ResourceNotModifiableException} object with the exception message extracted from the
     * given JSON object.
     *
     * @param jsonObject the JSON to read the {@link JsonFields#MESSAGE} field from.
     * @param dittoHeaders the headers of the command which resulted in this exception.
     * @return the new ResourceNotModifiableException.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if the {@code jsonObject} does not have the {@link
     * JsonFields#MESSAGE} field.
     */
    public static ResourceNotModifiableException fromJson(final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        return fromMessage(readMessage(jsonObject), dittoHeaders);
    }

    /**
     * A mutable builder with a fluent API for a {@link ResourceNotModifiableException}.
     *
     */
    @NotThreadSafe
    public static final class Builder extends DittoRuntimeExceptionBuilder<ResourceNotModifiableException> {

        private Builder() {
            description(DEFAULT_DESCRIPTION);
        }

        private Builder(final String policyId, final CharSequence label, final CharSequence path) {
            description(DEFAULT_DESCRIPTION);
            message(MessageFormat.format(MESSAGE_TEMPLATE, path, label, policyId));
        }

        @Override
        protected ResourceNotModifiableException doBuild(final DittoHeaders dittoHeaders,
                final String message,
                final String description,
                final Throwable cause,
                final URI href) {

            return new ResourceNotModifiableException(dittoHeaders, message, description, cause, href);
        }

    }

}
