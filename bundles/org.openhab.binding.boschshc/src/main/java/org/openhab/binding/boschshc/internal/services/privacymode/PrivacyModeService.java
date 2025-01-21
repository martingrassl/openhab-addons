/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.boschshc.internal.services.privacymode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.privacymode.dto.PrivacyModeServiceState;

/**
 * Service to get and set the privacy mode of security cameras.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class PrivacyModeService extends BoschSHCService<PrivacyModeServiceState> {

    public PrivacyModeService() {
        super("PrivacyMode", PrivacyModeServiceState.class);
    }
}
