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
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableEvent;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.events.base.EventJsonDeserializer;

/**
 * This event is emitted after a {@link org.eclipse.ditto.model.policies.PolicyImport} was deleted.
 *
 * @since 2.1.0
 */
@Immutable
@JsonParsableEvent(name = PolicyImportDeleted.NAME, typePrefix = PolicyImportDeleted.TYPE_PREFIX)
public final class PolicyImportDeleted extends AbstractPolicyEvent<PolicyImportDeleted>
        implements PolicyEvent<PolicyImportDeleted> {

    /**
     * Name of this event.
     */
    public static final String NAME = "policyImportDeleted";

    /**
     * Type of this event.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    static final JsonFieldDefinition<String> JSON_IMPORTED_POLICY_ID =
            JsonFactory.newStringFieldDefinition("importedPolicyId", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId importedPolicyId;

    private PolicyImportDeleted(final PolicyId policyId,
            final PolicyId importedPolicyId,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        super(TYPE, checkNotNull(policyId, "policyId"), revision, timestamp, dittoHeaders);
        this.importedPolicyId = checkNotNull(importedPolicyId, "importedPolicyId");
    }

    /**
     * Constructs a new {@code PolicyImportDeleted} object.
     *
     * @param policyId the identifier of the Policy to which the deleted entry belongs
     * @param importedPolicyId the id of the deleted {@link org.eclipse.ditto.model.policies.PolicyImport}
     * @param revision the revision of the Policy.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the created PolicyImportDeleted.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static PolicyImportDeleted of(final PolicyId policyId,
            final PolicyId importedPolicyId,
            final long revision,
            final DittoHeaders dittoHeaders) {

        return of(policyId, importedPolicyId, revision, null, dittoHeaders);
    }

    /**
     * Constructs a new {@code PolicyImportDeleted} object.
     *
     * @param policyId the identifier of the Policy to which the deleted entry belongs
     * @param importedPolicyId the id of the deleted {@link org.eclipse.ditto.model.policies.PolicyImport}
     * @param revision the revision of the Policy.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the created PolicyImportDeleted.
     * @throws NullPointerException if any argument but {@code timestamp} is {@code null}.
     */
    public static PolicyImportDeleted of(final PolicyId policyId,
            final PolicyId importedPolicyId,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders) {

        return new PolicyImportDeleted(policyId, importedPolicyId, revision, timestamp, dittoHeaders);
    }

    /**
     * Creates a new {@code PolicyImportDeleted} from a JSON string.
     *
     * @param jsonString the JSON string from which a new PolicyImportDeleted instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code PolicyImportDeleted} which was created from the given JSON string.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected 'PolicyImportDeleted'
     * format.
     */
    public static PolicyImportDeleted fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code PolicyImportDeleted} from a JSON object.
     *
     * @param jsonObject the JSON object from which a new PolicyImportDeleted instance is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the {@code PolicyDeleted} which was created from the given JSON object.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected 'PolicyImportDeleted'
     * format.
     */
    public static PolicyImportDeleted fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new EventJsonDeserializer<PolicyImportDeleted>(TYPE, jsonObject)
                .deserialize((revision, timestamp, metadata) -> {
                    final String extractedPolicyId = jsonObject.getValueOrThrow(JsonFields.POLICY_ID);
                    final PolicyId policyId = PolicyId.of(extractedPolicyId);
                    final PolicyId importedPolicyId = PolicyId.of(jsonObject.getValueOrThrow(JSON_IMPORTED_POLICY_ID));
                    return of(policyId, importedPolicyId, revision, timestamp, dittoHeaders);
                });
    }

    /**
     * Returns the {@link Label} of the deleted {@link org.eclipse.ditto.model.policies.PolicyImport}.
     *
     * @return the {@link Label} of the deleted {@link org.eclipse.ditto.model.policies.PolicyImport}.
     */
    public PolicyId getImportedPolicyId() {
        return importedPolicyId;
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/imports/" + importedPolicyId;
        return JsonPointer.of(path);
    }

    @Override
    public PolicyImportDeleted setRevision(final long revision) {
        return of(getPolicyEntityId(), importedPolicyId, revision, getTimestamp().orElse(null), getDittoHeaders());
    }

    @Override
    public PolicyImportDeleted setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(getPolicyEntityId(), importedPolicyId, getRevision(), getTimestamp().orElse(null), dittoHeaders);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(JSON_IMPORTED_POLICY_ID, importedPolicyId.toString(), predicate);
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(importedPolicyId);
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
        final PolicyImportDeleted that = (PolicyImportDeleted) o;
        return that.canEqual(this) &&
                Objects.equals(importedPolicyId, that.importedPolicyId) &&
                super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof PolicyImportDeleted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() +
                ", importedPolicyId=" + importedPolicyId +
                "]";
    }

}
