/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;

import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_BATTERY;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_SETTEMP;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_VALVE;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxCubeHandler extends BaseThingHandler {

	private String serialnr;
    private Logger logger = LoggerFactory.getLogger(MaxCubeHandler.class);
    private int refresh = 60; // refresh every minute as default 
    ScheduledFuture<?> refreshJob;
    
	public MaxCubeHandler(Thing thing) {
		super(thing);
	}

	@Override
    public void initialize() {
        logger.debug("Initializing Maxcube handler.");

        Configuration config = getThing().getConfiguration();

        serialnr = (String) config.get("serial");
        logger.debug("Item Serial,{}.", serialnr);
        
        try {
        	refresh = Integer.parseInt((String)config.get("refresh"));
        } catch(Exception e) {
        	// let's ignore it and go for the default
        }
        
        startAutomaticRefresh();
    }
	
	
private void startAutomaticRefresh() {
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					//updateWeatherData();
	                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE), (State) new DecimalType("15"));
	                updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY), (State) new DecimalType("10") );
	                updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), (State) new DecimalType("125"));
				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}
			}
		};
		
		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
	}



	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
        if(channelUID.getId().equals(CHANNEL_SETTEMP)) {
            // TODO: handle command
        }
	}
}
