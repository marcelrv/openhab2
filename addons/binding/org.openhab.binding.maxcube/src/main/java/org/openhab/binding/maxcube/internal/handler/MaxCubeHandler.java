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

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.message.Device;
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
	private MaxCubeBridgeHandler bridgeHandler;

	private Device maxCubeDevice;	
	private String maxCubeDeviceSerial;

	public MaxCubeHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void initialize() {
		final String configDeviceId = getConfigAs(MaxCubeConfiguration.class).serialNumber;
		if (configDeviceId != null) {
			maxCubeDeviceSerial = configDeviceId;
		}
		if (maxCubeDeviceSerial != null){
			logger.debug("Initialized maxcube device handler for {}.", maxCubeDeviceSerial);}
		else {
			logger.debug("Initialized maxcube device missing serialNumber configuration... troubles ahead");
		}

		//testing only: fake updates
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


	private synchronized MaxCubeBridgeHandler getMaxCubeBridgeHandler() {
		if(this.bridgeHandler==null) {
			Bridge bridge = getBridge();
			if (bridge == null) {
				return null;
			}
			ThingHandler handler = bridge.getHandler();
			if (handler instanceof MaxCubeBridgeHandler) {
				this.bridgeHandler = (MaxCubeBridgeHandler) handler;
				//        	this.bridgeHandler.registerDeviceStatusListener(this);
			} else {
				return null;
			}
		}
		return this.bridgeHandler;
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
		if (maxCubeBridge == null) {
			logger.warn("maxCube LAN gateway bridge handler not found. Cannot handle command without bridge.");
			return;
		}

		if(channelUID.getId().equals(CHANNEL_SETTEMP)) {
			logger.warn("Setting of temp not implemented.");
		}
		else {
			logger.warn("Setting of channel {} not possible. Read-only", channelUID);
		}
	}
}
