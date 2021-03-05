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
package org.eclipse.ditto.services.things.starter;

import org.eclipse.ditto.services.models.things.DittoThingSnapshotTaken;
import org.eclipse.ditto.services.utils.persistentactors.EmptyEvent;
import org.eclipse.ditto.services.utils.test.GlobalEventRegistryTestCases;
import org.eclipse.ditto.signals.events.policies.PolicyModified;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;

public final class ThingsServiceGlobalEventRegistryTest extends GlobalEventRegistryTestCases {

    public ThingsServiceGlobalEventRegistryTest() {
        super(FeatureDeleted.class, DittoThingSnapshotTaken.class, PolicyModified.class, EmptyEvent.class);
    }

}
