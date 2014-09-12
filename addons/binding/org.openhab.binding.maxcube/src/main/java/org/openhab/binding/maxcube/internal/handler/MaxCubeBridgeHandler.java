/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;


import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCubeBridge;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MaxCubeBridgeHandler} is the handler for a MaxCube Cube and connects it to
 * the framework. All {@link MaxCubeHandler}s use the {@link MaxCubeBridgeHandler}
 * to execute the actual commands.
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeHandler extends BaseBridgeHandler  {

	private MaxCubeBridge bridge = null;

	public MaxCubeBridgeHandler(Bridge br) {
		super(br);
	}

	private Logger logger = LoggerFactory.getLogger(MaxCubeBridgeHandler.class);

	/** The refresh interval which is used to poll given MAX!Cube */
	private long refreshInterval = 10000;
	ScheduledFuture<?> refreshJob;

	private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();
	private ArrayList<Device> devices = new ArrayList<Device>();

	private boolean previousOnline = true;

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.warn("No bridge commands defined.");
	}

	@Override
	public void dispose() {
		logger.debug("Handler disposes. Unregistering listener.");
		if (bridge != null) {
			bridge = null;
		}
	}

	@Override
	public void initialize() {
		logger.debug("Initializing MaxCube bridge handler.");

		MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);
		initializeBridge();

		if (configuration.refreshInterval != 0) {
			logger.debug("MaxCube refreshInterval {}.", configuration.refreshInterval);
			refreshInterval =  configuration.refreshInterval;}
		startAutomaticRefresh();
	}

	private void initializeBridge() {
		MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);

		if (bridge == null) {
			bridge = new MaxCubeBridge (configuration.ipAddress);
			if (configuration.port != 0) {
				logger.debug("MaxCube Port {}.", configuration.port);
				bridge.setPort ( configuration.port);
			}
		}		
	}

	private void startAutomaticRefresh() {

		Runnable runnable = new Runnable() {
			public void run() {
				try {

					if (bridge !=null){
						bridge.refreshData();
						if (bridge.isConnectionEstablished()){
							if ( previousOnline == false) {
								updateStatus(ThingStatus.ONLINE);
								previousOnline = bridge.isConnectionEstablished();
							}
							//process stuff
							devices = bridge.getDevices();
							logger.debug("Devices {}", devices);
						} else {
							if (previousOnline) onConnectionLost (bridge);
							initializeBridge() ;
						}
					}

				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
	}

	public Device getDeviceById(String maxCubeDeviceSerial) {
		bridge.getDevice (maxCubeDeviceSerial);
		return null;
	}

	public void onConnectionLost(MaxCubeBridge bridge) {
		logger.info("Bridge connection lost. Updating thing status to OFFLINE.");
		previousOnline = false;
		this.bridge = null;
		updateStatus(ThingStatus.OFFLINE);
	}

	public void onConnection(MaxCubeBridge bridge) {
		logger.info("Bridge connected. Updating thing status to ONLINE.");
		this.bridge = bridge;
		updateStatus(ThingStatus.ONLINE);
	}
}

/*
	private Device findThing(String serialNumber) {
		for (Thing thing : Things) {
			if (device.getSerialNumber().toUpperCase().equals(serialNumber)) {
				return device;
			}
		}
		return null;
	}
 */

/*
public MaxCubeDevice getDeviceById(String lightId) {
    List<Light> lights;
    try {
        lights = bridge.getLights();
        for (Light light : lights) {
            if (light.getId().equals(lightId)) {
                return light;
            }
        }
    } catch (IOException | ApiException e) {
        throw new RuntimeException(e);
    } catch (IllegalStateException e) {
        logger.trace("Error while accessing light: {}", e.getMessage());
    }
    return null;

}

 */

/*   public void updateLightState(Light light, StateUpdate stateUpdate) {

if (bridge != null) {
    try {
        bridge.setLightState(light, stateUpdate);
    } catch (IOException | ApiException e) {
        throw new RuntimeException(e);
    }
} else {
    logger.warn("No bridge connected or selected. Cannot set light state.");
}
}
 */



/*

public BridgeHeartbeatService getBridgeHeartbeatService() {
    if (bridgeHeartbeatService == null) {
        throw new RuntimeException("The heartbeat service for bridge " + bridge.getIPAddress()
                + " has not been initialized.");
    } else {
        return bridgeHeartbeatService;
    }
}

public Light getLightById(String lightId) {
    List<Light> lights;
    try {
        lights = bridge.getLights();
        for (Light light : lights) {
            if (light.getId().equals(lightId)) {
                return light;
            }
        }
    } catch (IOException | ApiException e) {
        throw new RuntimeException(e);
    }
    return null;

}
 */
