/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * The {@link MiIoLumiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoLumiHandler extends MiIoBasicHandler {

    private final Logger logger = LoggerFactory.getLogger(MiIoLumiHandler.class);
    private @Nullable MiIoGatewayHandler bridgeHandler;

    public MiIoLumiHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
            CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry,
            BasicChannelTypeProvider basicChannelTypeProvider) {
        super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        isIdentified = false;

        final MiIoBindingConfiguration config = this.configuration;
        if (config != null && config.deviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing required deviceId");
            logger.info("Missing required deviceId for {} {}", getThing().getUID(), getThing().getLabel());
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device bridge has been configured");
            logger.info("Missing Bridge for {} {}", getThing().getUID(), getThing().getLabel());
            return;
        } else {
            logger.info("Bridge for {} {} = {} {} ({})", getThing().getUID(), getThing().getLabel(),
                    bridge.getBridgeUID(), bridge.getLabel(), bridge.getHandler());
        }

        if (ThingStatus.ONLINE != bridge.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Nullable
    MiIoGatewayHandler getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                final MiIoGatewayHandler bridgeHandler = (MiIoGatewayHandler) bridge.getHandler();
                if (bridgeHandler != null) {
                    if (!bridgeHandler.childDevices.contains(this)) {
                        logger.warn(("Adding child device {} to bridge {}. We should not see this"),
                                getThing().getUID(), bridgeHandler.getThing().getUID());
                        bridgeHandler.childHandlerInitialized(this, getThing());
                    }
                    this.bridgeHandler = bridgeHandler;
                    return bridgeHandler;
                }
            }
        }
        return null;
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            if (!hasConnection() || skipUpdate() || miioCom == null) {
                // return;
            }
            checkChannelStructure();
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                return;
            }

            final MiIoBindingConfiguration config = this.configuration;
            final MiIoBasicDevice midevice = miioDevice;
            if (midevice != null && configuration != null && config != null) {
                refreshProperties(midevice, config.deviceId);
                // refreshCustomProperties(midevice);
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}': ", getThing().getUID().toString(), e);
        }
    }

    @Override
    protected void sendRefreshProperties(MiIoCommand command, JsonArray getPropString) {
        logger.debug("sending ChildDeviceprop {}", getPropString.toString());

        final MiIoGatewayHandler bridge = getBridgeHandler();
        if (bridge != null) {
            JsonArray para = new JsonArray();
            para.add(getPropString);
            bridge.sendCommand(command, para.toString(), getThing().getUID().getAsString());
        }
    }
}
