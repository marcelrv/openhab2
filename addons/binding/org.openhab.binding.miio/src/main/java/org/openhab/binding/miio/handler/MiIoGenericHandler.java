/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.handler;

import static org.openhab.binding.miio.MiIoBindingConstants.CHANNEL_COMMAND;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoGenericHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoGenericHandler.class);

    public MiIoGenericHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            updateState(CHANNEL_COMMAND, new StringType(sendCommand(command.toString())));
        }
    }

    @Override
    protected synchronized void updateData() {
        logger.debug("Update connection '{}'", getThing().getUID().toString());
        if (!hasConnection()) {
            return;
        }
        try {
            if (updateNetwork()) {
                updateStatus(ThingStatus.ONLINE);
                if (!isIdentified) {
                    isIdentified = updateThingType(getJsonResultHelper(network.getValue()));
                }
            } else {
                disconnectedNoResponse();
            }
        } catch (Exception e) {
            logger.debug("Error while updating '{}'", getThing().getUID().toString(), e);
        }
    }

    @Override
    protected boolean initializeData() {
        this.miioCom = getConnection();
        if (miioCom != null) {
            updateStatus(ThingStatus.ONLINE);
        }
        initalizeNetworkCache();
        return true;
    }

}
