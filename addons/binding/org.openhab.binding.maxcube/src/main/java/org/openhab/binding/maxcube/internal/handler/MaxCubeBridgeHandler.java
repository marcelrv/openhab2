/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.handler;


import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_BATTERY;

import org.osgi.service.cm.ConfigurationException;

import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_SETTEMP;
import static org.openhab.binding.maxcube.MaxCubeBinding.CHANNEL_VALVE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.internal.MaxCubeDiscover;
import org.openhab.binding.maxcube.internal.Utils;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceConfiguration;
import org.openhab.binding.maxcube.internal.message.C_Message;
import org.openhab.binding.maxcube.internal.message.DeviceInformation;
import org.openhab.binding.maxcube.internal.message.H_Message;
import org.openhab.binding.maxcube.internal.message.L_Message;
import org.openhab.binding.maxcube.internal.message.M_Message;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.MessageType;
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

	// private maxCubeBridge bridge = null;

	public MaxCubeBridgeHandler(Bridge maxcubeBridge) {
		super(maxcubeBridge);

	}

	private Logger logger = LoggerFactory.getLogger(MaxCubeBridgeHandler.class);


	/** The IP address of the MAX!Cube LAN gateway */
	private static String ip;

	/**
	 * The port of the MAX!Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	private static int port = 62910;

	/** The refresh interval which is used to poll given MAX!Cube */
	private static long refreshInterval = 10000;


	//private BridgeHeartbeatService bridgeHeartbeatService = new BridgeHeartbeatService();

	//   private MaxCubeBridge bridge = null;

	private int refresh = 60; // refresh every minute as default 
	ScheduledFuture<?> refreshJob;


	private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();
	private ArrayList<Device> devices = new ArrayList<Device>();

	
	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.warn("No bridge commands defined.");
	}


	@Override
	public void dispose() {
		logger.debug("Handler disposes. Unregistering listener.");
		/*        if (bridge != null) {
            bridgeHeartbeatService.unregisterBridgeStatusListener(this);
            bridgeHeartbeatService.dispose();

            bridge = null;
        }

		 */
	}

	@Override
	public void initialize() {
		logger.debug("Initializing MaxCube bridge handler.");

		Configuration config = getThing().getConfiguration();
		MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);

		ip = (String) config.get("ipAddress");
		logger.debug("Bridge IP {}.", ip);
		configuration.ipAddress =  ip;

		if (configuration.ipAddress == null) {
			try {
				ip = discoveryGatewayIp();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*  if (configuration.ipAddress != null) {
	        	if (bridge == null) {
	        		bridge = new HueBridge(configuration.ipAddress);
	        		bridge.setTimeout(5000);
	        	}
	        	this.lastBridgeConnectionState = isConnectionEstablished(bridge);
	        	onUpdate();
	        } else {
	            logger.warn("Cannot connect to hue bridge. IP address or user name not set.");
	        }
	    } 
		 */

		try {
			refresh = Integer.parseInt((String)config.get("refresh"));
		} catch(Exception e) {
			// let's ignore it and go for the default
		}


		if ( ip != null) {
			logger.info("MaxCube found IP {}.",ip );

		} else{
			logger.info("MaxCube Lan gateway not configured." );
		}

		/*if (configuration.ipAddress != null && configuration.port != null) {
        	if (bridge == null) {
        		bridge = new MaxCubeBridge(configuration.ipAddress);
        		bridge.setTimeout(5000);
        	}
        	bridgeHeartbeatService.initialize(bridge);
            bridgeHeartbeatService.registerBridgeStatusListener(this);
        } else {
            logger.warn("Cannot connect to hue bridge. IP address or user name not set.");
        }

		 */

		startAutomaticRefresh();
	}

	private void startAutomaticRefresh() {

		Runnable runnable = new Runnable() {
			public void run() {
				try {

					Socket socket = null;
					BufferedReader reader = null;

					if (ip == null) {
						logger.debug("Update prior to completion of interface IP configuration");
						return;
					}
					try {
						String raw = null;

						socket = new Socket(ip, port);
						reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

						boolean cont = true;
						while (cont) {
							raw = reader.readLine();
							if (raw == null) {
								cont = false;
								continue;
							}

							Message message;
							try {
								logger.debug("message block: '{}'",raw);
								message = processRawMessage(raw);

								message.debug(logger);
								processMessage (message);

							} catch (Exception e) {
								logger.info("Failed to process message received by MAX! protocol.");
								logger.debug(Utils.getStackTrace(e));
							}
							
							if (raw.startsWith("L:")) {
								socket.close();
								cont = false;}
						}

						
					} catch(Exception e) {
						logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
					}

				} catch(Exception e) {
					logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
				}}

		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
	}



	/**
	 * Discovers the MAX!CUbe LAN Gateway IP address.
	 * 
	 * @return the cube IP if available, a blank string otherwise.
	 * @throws ConfigurationException
	 */
	private String discoveryGatewayIp() throws ConfigurationException {
		String ip = MaxCubeDiscover.discoverIp();
		if (ip == null) {
			throw new ConfigurationException("maxcube:ip", "IP address for MAX!Cube must be set manually");
		} else {
			logger.info("Discovered MAX!Cube lan gateway at '{}'", ip);
		}
		return ip;
	}

	/**
	 * Processes the raw TCP data read from the MAX protocol, returning the
	 * corresponding Message.
	 * 
	 * @param raw
	 *            the raw data provided read from the MAX protocol
	 * @return the correct message for the given raw data
	 */
	private Message processRawMessage(String raw) {
		
		if (raw.startsWith("H:")) {
			return new H_Message(raw);
		} else if (raw.startsWith("M:")) {
			return new M_Message(raw);
		} else if (raw.startsWith("C:")) {
			return new C_Message(raw);
		} else if (raw.startsWith("L:")) {
			return new L_Message(raw);
		} else {
			logger.debug("Unknown message block: '{}'",raw);
		}
		return null;
	}

	/**
	 * Processes the message
	 * @param Message
	 *            the decoded message data
	 */
	private void processMessage (Message message){
	
		message.debug(logger);

		if (message != null) {
			if (message.getType() == MessageType.M) {
				M_Message msg = (M_Message) message;
				for (DeviceInformation di : msg.devices) {
					DeviceConfiguration c = null;
					for (DeviceConfiguration conf : configurations) {
						if (conf.getSerialNumber().equalsIgnoreCase(di.getSerialNumber())) {
							c = conf;
							break;
						}
					}

					if (c != null) {
						configurations.remove(c);
					}

					c = DeviceConfiguration.create(di);
					configurations.add(c);

					c.setRoomId(di.getRoomId());
				}
			} else if (message.getType() == MessageType.C) {
				DeviceConfiguration c = null;
				for (DeviceConfiguration conf : configurations) {
					if (conf.getSerialNumber().equalsIgnoreCase(((C_Message) message).getSerialNumber())) {
						c = conf;
						break;
					}
				}

				if (c == null) {
					configurations.add(DeviceConfiguration.create(message));
				} else {
					c.setValues((C_Message) message);
				}
			} else if (message.getType() == MessageType.L) {
				Collection<? extends Device> tempDevices = ((L_Message) message).getDevices(configurations);

				for (Device d : tempDevices) {
					Device existingDevice = findDevice(d.getSerialNumber(), devices);
					if (existingDevice == null) {
						devices.add(d);
					} else {
						devices.remove(existingDevice);
						devices.add(d);
					}
				}

				logger.debug("{} devices found.", devices.size());

		
	}
}
	}
	private Device findDevice(String serialNumber, ArrayList<Device> devices) {
		for (Device device : devices) {
			if (device.getSerialNumber().toUpperCase().equals(serialNumber)) {
				return device;
			}
		}
		return null;
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
	
}

	
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
@Override
public void onConnectionLost(HueBridge bridge) {
    logger.info("Bridge connection lost. Updating thing status to OFFLINE.");
    this.bridge = null;
    updateStatus(ThingStatus.OFFLINE);
}

@Override
public void onConnectionResumed(HueBridge bridge) {
    logger.info("Bridge connection resumed. Updating thing status to ONLINE.");
    this.bridge = bridge;
    updateStatus(ThingStatus.ONLINE);
}

@Override
public void onNotAuthenticated(HueBridge bridge) {
    HueBridgeConfiguration configuration = getConfigAs(HueBridgeConfiguration.class);
	try {
		bridge.authenticate(configuration.userName);
	} catch (Exception e) {
		logger.debug("Hue bridge is not authenticated - please add user '{}'.", configuration.userName);
	}
}

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
