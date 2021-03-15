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
package org.eclipse.ditto.signals.events.policies;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableEvent;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyImport;
import org.eclipse.ditto.model.policies.PolicyImports;
import org.eclipse.ditto.signals.events.base.EventJsonDeserializer;

/**
 * This event is emitted after all {@link PolicyImport}s were modified at once.
 *
 * @since 2.1.0
 */
@Immutable
@JsonParsableEvent(name = PolicyImportsModified.NAME, typePrefix = PolicyImportsModified.TYPE_PREFIX)
public final class PolicyImportsModified extends AbstractPolicyEvent<PolicyImportsModified> implements
        PolicyEvent<PolicyImportsModified> {

    /**
     * Name of this event.
     */
    public static final String NAME = "policyImportsModified";

    /**
     * Type of this event.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    public static final JsonFieldDefinition<JsonObject> JSON_POLICY_IMPORTS =
            JsonFactory.newJsonObjectFieldDefinition("policyImports", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyImports policyImports;

    private PolicyImportsModified(final PolicyId policyId,
            final PolicyImports policyImports,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        super(TYPE, checkNotNull(policyId, "policyId"), revision, timestamp, dittoHeaders);
        this.policyImports = checkNotNull(policyImports, "policyImports");
    }

    /**
     * Constructs a new {@code PolicyImportsModified} object indicating the modification of the entries.
     *
     * @param policyId the identifier of the Policy to which the modified import belongs
     * @param policyImports the modified {@link PolicyImport}s.
     * @param revision the revision of the Policy.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the created PolicyImportsModified.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static PolicyImportsModified of(final PolicyId policyId,
            final PolicyImports policyImports,
            final long revision,
            final DittoHeaders dittoHeaders) {

        return of(policyId, policyImports, revision, null, dittoHeaders);
    }

    /**
     * Constructs a new {@code PolicyImportsModified} object indicating the modification of the entries.
     *
     * @param policyId the identifier of the Policy to which the modified import belongs
     * @param policyImports the modified {@link PolicyImport}s.
     * @param revision the revision of the Policy.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the created PolicyImportsModified.
     * @throws NullPointerException if any argument but {@code timestamp} is {@code null}.
     */
    public static PolicyImportsModified of(final PolicyId policyId,
            final PolicyImports policyImports,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        return new PolicyImportsModified(policyId, policyImports, revision, timestamp, dittoHeaders);
    }

    /**
     * Creates a new {@code PolicyImportsModified} from a JSON string.
     *
     * @param jsonString the JSON string from which a new PolicyImportsModified instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code PolicyImportsModified} which was created from the given JSON string.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected 'PolicyImportsModified'
     * format.
     */
    public static PolicyImportsModified fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code PolicyImportsModified} from a JSON object.
     *
     * @param jsonObject the JSON object from which a new PolicyImportsModified instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code PolicyImportsModified} which was created from the given JSON object.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected 'PolicyImportsModified'
     * format.
     */
    public static PolicyImportsModified fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new EventJsonDeserializer<PolicyImportsModified>(TYPE, jsonObject)
                .deserialize((revision, timestamp, metadata) -> {
                    final String extractedPolicyId = jsonObject.getValueOrThrow(JsonFields.POLICY_ID);
                    final PolicyId policyId = PolicyId.of(extractedPolicyId);
                    final JsonObject policyImportsJsonObject = jsonObject.getValueOrThrow(JSON_POLICY_IMPORTS);
                    final PolicyImports extractedModifiedPolicyImport =
                            PoliciesModelFactory.newPolicyImports(policyImportsJsonObject);

                    return of(policyId, extractedModifiedPolicyImport, revision, timestamp, dittoHeaders);
                });
    }

    /**
     * Returns the modified {@link PolicyImport}s.
     *
     * @return the modified {@link PolicyImport}s.
     */
    public PolicyImports getPolicyImports() {
        return policyImports;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        final JsonObject jsonObject = policyImports.toJson();
        return Optional.of(jsonObject);
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/imports");
    }

    @Override
    public PolicyImportsModified setRevision(final long revision) {
        return of(getPolicyEntityId(), policyImports, revision, getTimestamp().orElse(null), getDittoHeaders());
    }

    @Override
    public PolicyImportsModified setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(getPolicyEntityId(), policyImports, getRevision(), getTimestamp().orElse(null), dittoHeaders);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(JSON_POLICY_IMPORTS, policyImports.toJson(), predicate);
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(policyImports);
        return result;
    }

    @SuppressWarnings("squid:MethodCyclomaticComplexity")
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        final PolicyImportsModified that = (PolicyImportsModified) o;
        return that.canEqual(this) &&
                Objects.equals(policyImports, that.policyImports) &&
                super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof PolicyImportsModified;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() +
                ", policyImports=" + policyImports +
                "]";
    }

}
