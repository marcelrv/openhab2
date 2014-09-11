package org.openhab.binding.maxcube.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.maxcube.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.maxcube.internal.message.C_Message;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceConfiguration;
import org.openhab.binding.maxcube.internal.message.DeviceInformation;
import org.openhab.binding.maxcube.internal.message.H_Message;
import org.openhab.binding.maxcube.internal.message.L_Message;
import org.openhab.binding.maxcube.internal.message.M_Message;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.osgi.service.cm.ConfigurationException;

public class MaxCubeBridge {

	private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();
	private ArrayList<Device> devices = new ArrayList<Device>();

	private Logger logger = LoggerFactory.getLogger(MaxCubeBridge.class);


	/** The IP address of the MAX!Cube LAN gateway */
	private String ipAddress;
	
	private boolean connectionEstablished = false;

	public boolean isConnectionEstablished() {
		return connectionEstablished;
	}

	/**
	 * The port of the MAX!Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	private int port = 62910;

	public MaxCubeBridge (String ipAddress){ 
		this.ipAddress = ipAddress;
		if (ipAddress == null) {
			try {
				this.ipAddress = discoveryGatewayIp();
			} catch (ConfigurationException e) {
				logger.warn("Cannot discover to Max!Cube Lan interface. Configure manually.");
			}
		}
	}

	public void refreshData() {
		Socket socket = null;
		BufferedReader reader = null;

		try {
			String raw = null;

			socket = new Socket(ipAddress, port);
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
					cont = false;
					connectionEstablished = true;
					
				}
			}
		 } catch (ConnectException  e) {
			 logger.debug("Connection timed out on {} port {}",ipAddress, port );
			 connectionEstablished = false;
		} catch(Exception e) {
			logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
			connectionEstablished = false;
		}

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

	public Device findDevice(String serialNumber, ArrayList<Device> devices) {
		for (Device device : devices) {
			if (device.getSerialNumber().toUpperCase().equals(serialNumber)) {
				return device;
			}
		}
		return null;
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
	
	public String getIp() {
		return ipAddress;
	}

	public void setIp(String ip) {
		this.ipAddress = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ArrayList<Device> getDevices() {
		return devices;
	}

}
