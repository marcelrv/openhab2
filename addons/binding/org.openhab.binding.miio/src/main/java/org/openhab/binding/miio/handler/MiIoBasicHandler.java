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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.miio.internal.MiIoCommand;
import org.openhab.binding.miio.internal.basic.MiIoBasicProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link MiIoBasicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoBasicHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoBasicHandler.class);

    public MiIoBasicHandler(Thing thing) {
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
        // TODO: cleanup debug stuff & add handling types
        logger.debug("Locating action for channel {}:{}", channelUID.getId(), command);
        if (getActions() != null) {
            if (getActions().containsKey(channelUID.getId())) {
                String cmd = getActions().get(channelUID.getId()).getCommand();
                if (command instanceof OnOffType) {
                    cmd = cmd + "[\"" + command.toString().toLowerCase() + "\"]";
                }
                if (command instanceof DecimalType) {
                    cmd = cmd + "[" + command.toString().toLowerCase() + "]";
                }
                logger.debug(" sending command {}", cmd);
                sendCommand(cmd);
            } else {
                logger.debug("Channel Id {} not in mapping. Available:", channelUID.getId());
                for (String a : getActions().keySet()) {
                    logger.debug("entries: {} : {}", a, getActions().get(a));
                }

            }

        } else {
            logger.debug("Actions not leaded yet");
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
                // TODO: horribly inefficient refresh with each time creation of the list etc.. for testing only
                if (miioDevice != null) {
                    // build list of properties to be refreshed
                    JsonArray getPropString = new JsonArray();
                    List<MiIoBasicProperty> refreshList = new ArrayList<MiIoBasicProperty>();
                    for (MiIoBasicProperty miProperty : miioDevice.getDevice().getProperties()) {
                        if (miProperty.getRefresh()) {
                            refreshList.add(miProperty);
                            getPropString.add(miProperty.getProperty());
                        }
                    }
                    // get the data based on the datatype
                    String reply = null;
                    reply = sendCommand(MiIoCommand.GET_PROPERTY, getPropString.toString());
                    // mock data for testing
                    if (reply == null) {
                        reply = "{\"result\":[\"off\",\"idle\",59,16,10,\"on\",\"on\",\"off\",322,22],\"id\":14}";
                        logger.debug("No Reply using for testing mocked reply: {}", reply);
                    }

                    JsonArray res = ((JsonObject) parser.parse(reply)).get("result").getAsJsonArray();
                    // update the states
                    for (int i = 0; i < refreshList.size(); i++) {
                        if (refreshList.get(i).getType().equals("Number")) {
                            updateState(refreshList.get(i).getChannel(), new DecimalType(res.get(i).getAsBigDecimal()));
                        }
                        if (refreshList.get(i).getType().equals("String")) {
                            updateState(refreshList.get(i).getChannel(), new StringType(res.get(i).getAsString()));
                        }
                        if (refreshList.get(i).getType().equals("Switch")) {
                            updateState(refreshList.get(i).getChannel(),
                                    res.get(i).getAsString().equals("on") ? OnOffType.ON : OnOffType.OFF);
                        }
                    }
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
        initalizeNetworkCache();
        // For testing only.. this should load the possible properties & actions per device
        // NB, ones working properly, this action should be done once the type is known

        this.miioCom = getConnection();
        if (miioCom != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
        return true;
    }
}
