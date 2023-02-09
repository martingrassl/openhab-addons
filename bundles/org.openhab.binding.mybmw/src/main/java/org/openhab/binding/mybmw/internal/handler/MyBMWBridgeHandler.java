/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.discovery.VehicleDiscovery;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWFileProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWHttpProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.MyBMWConfigurationChecker;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyBMWBridgeHandler} is responsible for handling commands, which
 * are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored, all discovery functionality moved to VehicleDiscovery
 */
@NonNullByDefault
public class MyBMWBridgeHandler extends BaseBridgeHandler {

    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String TEST = "test";
    private static final String TESTUSER = "testuser";

    private final Logger logger = LoggerFactory.getLogger(MyBMWBridgeHandler.class);

    private HttpClientFactory httpClientFactory;
    private Optional<MyBMWProxy> myBmwProxy = Optional.empty();
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
    private Optional<VehicleDiscovery> vehicleDiscovery = Optional.empty();
    private String localeLanguage;

    public MyBMWBridgeHandler(Bridge bridge, HttpClientFactory hcf, String language) {
        super(bridge);
        httpClientFactory = hcf;
        localeLanguage = language;
    }

    public void setVehicleDiscovery(VehicleDiscovery vehicleDiscovery) {
        logger.trace("xxxMyBMWBridgeHandler.setVehicleDiscovery");
        this.vehicleDiscovery = Optional.of(vehicleDiscovery);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
        logger.trace("xxxMyBMWBridgeHandler.handleCommand");
    }

    @Override
    public void initialize() {
        logger.trace("xxxMyBMWBridgeHandler.initialize");
        MyBMWBridgeConfiguration config = getConfigAs(MyBMWBridgeConfiguration.class);
        if (config.language.equals(Constants.LANGUAGE_AUTODETECT)) {
            config.language = localeLanguage;
        }
        if (!MyBMWConfigurationChecker.checkConfiguration(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            // there is no risk in this functionality as several steps have to happen to get the file proxy working:
            // 1. environment variable ENVIRONMENT has to be available
            // 2. username of the myBMW account must be set to "testuser" which is anyhow no valid username
            // 3. the jar file must contain the fingerprints which will only happen if it has been built with the
            // test-jar profile
            String environment = System.getenv(ENVIRONMENT);

            if (!(TEST.equals(environment) && TESTUSER.equals(config.userName))) {
                myBmwProxy = Optional.of(new MyBMWHttpProxy(httpClientFactory, config));
            } else {
                myBmwProxy = Optional.of(new MyBMWFileProxy(httpClientFactory, config));
            }
            initializerJob = Optional.of(scheduler.schedule(this::discoverVehicles, 2, TimeUnit.SECONDS));
        }
    }

    @Override
    public void dispose() {
        logger.trace("xxxMyBMWBridgeHandler.dispose");
        initializerJob.ifPresent(job -> job.cancel(true));
    }

    public void vehicleDiscoveryError() {
        logger.trace("xxxMyBMWBridgeHandler.vehicleDiscoveryError");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Request vehicles failed");
    }

    public void vehicleDiscoverySuccess() {
        logger.trace("xxxMyBMWBridgeHandler.vehicleDiscoverySuccess");
        updateStatus(ThingStatus.ONLINE);
    }

    private void discoverVehicles() {
        logger.trace("xxxMyBMWBridgeHandler.requestVehicles");

        MyBMWBridgeConfiguration config = getConfigAs(MyBMWBridgeConfiguration.class);

        myBmwProxy.ifPresent(proxy -> proxy.setBridgeConfiguration(config));

        vehicleDiscovery.ifPresent(discovery -> discovery.discoverVehicles());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        logger.trace("xxxMyBMWBridgeHandler.getServices");
        return Collections.singleton(VehicleDiscovery.class);
    }

    public Optional<MyBMWProxy> getMyBmwProxy() {
        logger.trace("xxxMyBMWBridgeHandler.getProxy");
        return myBmwProxy;
    }
}
