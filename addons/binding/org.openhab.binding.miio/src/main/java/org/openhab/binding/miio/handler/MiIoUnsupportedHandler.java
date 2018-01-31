/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.handler;

import static org.openhab.binding.miio.MiIoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoUnsupportedHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoUnsupportedHandler extends MiIoAbstractHandler {
    private final Logger logger = LoggerFactory.getLogger(MiIoUnsupportedHandler.class);

    @NonNullByDefault
    public MiIoUnsupportedHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
            return;
        }
        if (channelUID.getId().equals(CHANNEL_POWER)) {
            if (command.equals(OnOffType.ON)) {
                sendCommand("set_power[\"on\"]");
            } else {
                sendCommand("set_power[\"off\"]");
            }
        }
        if (channelUID.getId().equals(CHANNEL_COMMAND)) {
            cmds.put(sendCommand(command.toString()), command.toString());
        }
        if (channelUID.getId().equals(CHANNEL_TESTCOMMANDS)) {
            executeExperimentalCommands();
        }
    }

    private void executeExperimentalCommands() {
        String[] testCommands = new String[0];
        switch (miDevice) {
            case POWERPLUG:
            case POWERPLUG2:
            case POWERSTRIP:
            case POWERSTRIP2:
                testCommands = new String[] { "miIO.info", "set_power[\"on\"]", "set_power[\"off\"]",
                        "get_prop[\"power\", \"temperature\", \"current\", \"mode\"]", "set_power_mode[\"green\"]",
                        "set_power_mode[\"normal\"]", "set_power[on]", "set_power[off]", };
                break;
            case YEELIGHT_C1:
            case YEELIGHT_L1:
            case YEELIGHT_M1:
                testCommands = new String[] { "miIO.info", "set_power[\"on\"]", "set_power[\"off\"]",
                        "get_prop[\"power\", \"bright\", \"ct\", \"rgb\"]", "set_bright[50, \"smooth\", 500]",
                        "start_cf[ 4, 2, \"1000, 2, 2700, 100, 500, 1,255, 10, 5000, 7, 0,0, 500, 2, 5000, 1\"]" };
                break;
            case VACUUM:
                testCommands = new String[] { "miIO.info", "get_current_sound", "get_map_v1", "get_serial_number",
                        "get_timezone" };
                break;
            case AIR_PURIFIERM:
            case AIR_PURIFIER1:
            case AIR_PURIFIER2:
            case AIR_PURIFIER3:
            case AIR_PURIFIER6:
                testCommands = new String[] { "miIO.info", "set_power[\"on\"]", "set_power[\"off\"]",
                        "get_prop[\"power\", \"mode\", \"temperature\", \"humidity\", \"aqi\"]", "set_mode[\"auto\"]",
                        "led", "favoriteLevel", "ledBrightness",
                        "get_prop[\"power\",\"mode\",\"favorite_level\",\"temp_dec\",\"humidity\",\"aqi\",\"bright\",\"filter1_life\",\"f1_hour_used\",\"use_time\",\"led\",\"led_b\",\"buzzer\"]" };
                break;

            default:
                testCommands = new String[] { "miIO.info" };
                break;
        }
        logger.info("Start Experimental Testing of commands for device '{}'. ", miDevice.toString());
        for (String c : testCommands) {
            logger.info("Test command '{}'. Response: '{}'", c, sendCommand(c));
        }
    }

    @Override
    protected synchronized void updateData() {
        if (skipUpdate()) {
            return;
        }
        logger.debug("Periodic update for '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID());
        try {
            refreshNetwork();
        } catch (Exception e) {
            logger.debug("Error while updating '{}' ({})", getThing().getUID().toString(), getThing().getThingTypeUID(),
                    e);
        }
    }
}
