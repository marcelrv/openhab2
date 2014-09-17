/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCubeBridge;
import org.openhab.binding.maxcube.internal.discovery.MaxCubeBridgeDiscovery;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * {@link MaxCubeBridgeHandler} is the handler for a MaxCube Cube and connects it to
 * the framework. All {@link MaxCubeHandler}s use the {@link MaxCubeBridgeHandler}
 * to execute the actual commands.
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeHandler extends BaseBridgeHandler  {

	private static boolean ini = true; 
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
	private HashSet<String>  lastActiveDevices = new HashSet<String>();

	private boolean previousOnline = true;

	private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

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

		MaxCubeBridgeDiscovery test = new MaxCubeBridgeDiscovery ();
		test.startScan();

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
			if (bridge.getIp()==null) {
				bridge = null;
				updateStatus(ThingStatus.OFFLINE);
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

							devices = bridge.getDevices();
							for (Device di : devices){
								if (lastActiveDevices.contains(di.getSerialNumber())) {
									for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
										try {
											deviceStatusListener.onDeviceStateChanged(getThing().getUID(), di);
										} catch (Exception e) {
											logger.error(
													"An exception occurred while calling the DeviceStatusListener", e);
										}
									} }
								//New device, not seen before, pass to Discovery
								else {
									for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
										try {
											deviceStatusListener.onDeviceAdded(bridge, di);
										} catch (Exception e) {
											logger.error(
													"An exception occurred while calling the DeviceStatusListener", e);
										}
										lastActiveDevices.add (di.getSerialNumber());
									}
								}
							}
						}
					} else {
						if (previousOnline) onConnectionLost (bridge);
						initializeBridge() ;
					}


				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
	}

	public Device getDeviceById(String maxCubeDeviceSerial) {
		if (bridge !=null){
			bridge.getDevice (maxCubeDeviceSerial);
		}
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

	public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		if (deviceStatusListener == null) {
			throw new NullPointerException("It's not allowed to pass a null LightStatusListener.");
		}
		boolean result = deviceStatusListeners.add(deviceStatusListener);
		if (result) {
			// onUpdate();
		}
		return result;
	}

	public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
		boolean result = deviceStatusListeners.remove(deviceStatusListener);
		if (result) {
			//   onUpdate();
		}
		return result;
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
