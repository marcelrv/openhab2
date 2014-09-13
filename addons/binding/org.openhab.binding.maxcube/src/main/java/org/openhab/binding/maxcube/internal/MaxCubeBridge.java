/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.maxcube.internal.discovery.MaxCubeDiscover;
import org.openhab.binding.maxcube.internal.message.C_Message;
import org.openhab.binding.maxcube.internal.message.Device;
import org.openhab.binding.maxcube.internal.message.DeviceConfiguration;
import org.openhab.binding.maxcube.internal.message.DeviceInformation;
import org.openhab.binding.maxcube.internal.message.H_Message;
import org.openhab.binding.maxcube.internal.message.L_Message;
import org.openhab.binding.maxcube.internal.message.M_Message;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.MessageType;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeBridge} is responsible for connecting to the Max!Cube Lan gateway and read the data for  
 * each connected device.
 * @author Marcel Verpaalen - Initial contribution. Based on OH1 version by Andreas Heil (info@aheil.de)
 */

public class MaxCubeBridge {

	private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();
	private ArrayList<Device> devices = new ArrayList<Device>();

	private Logger logger = LoggerFactory.getLogger(MaxCubeBridge.class);


	/** The IP address of the MAX!Cube LAN gateway */
	private String ipAddress;

	private boolean connectionEstablished = false;

	/**
	 * The port of the MAX!Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	private int port = 62910;

	public MaxCubeBridge (String ipAddress){ 
		this.ipAddress = ipAddress;
		if (ipAddress == null) {
			try {
				logger.info("Discover Max!Cube Lan interface.");
				this.ipAddress = discoveryGatewayIp();
			} catch (ConfigurationException e) {
				logger.warn("Cannot discover to Max!Cube Lan interface. Configure manually.");
			}
		}
	}

	/**
	 * Connects to the Max!Cube Lan gateway, reads and decodes the message 
	 * this updates device information for each connected Max!Cube device
	 */
	public void refreshData() {
		Message message;

		for (String raw : getRawMessage()){

			try {
				logger.debug("message block: '{}'",raw);
				message = processRawMessage(raw);
				message.debug(logger);
				processMessage (message);

			} catch (Exception e) {
				logger.info("Failed to process message received by MAX! protocol.");
				logger.debug(Utils.getStackTrace(e));
			}
		}


	}

	/**
	 * Connects to the Max!Cube Lan gateway and returns the read data 
	 * corresponding Message.
	 * 
	 * @return the raw message text as ArrayList of String 
	 */

	private ArrayList<String> getRawMessage() {
		//Fake a message for testing purposes
		if (ipAddress.equals( "fakeMessage")){
			connectionEstablished = true;
			logger.warn("Content based on faked data!!");
			return getRawFAKEMessage();
		}
		
		Socket socket = null;
		BufferedReader reader = null;
		ArrayList<String> rawMessage = new ArrayList<String> () ;

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
				rawMessage.add(raw);
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

		return rawMessage;
	}
	/**
	 * Processes the raw TCP data read from the MAX protocol, returning the
	 * corresponding Message.
	 * 
	 * @param raw
	 *            the raw data line read from the MAX protocol
	 * @return message
	 * 				the @Message for the given raw data
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

	/**
	 * Returns the MaxCube Device decoded during the last refreshData
	 * 
	 * @param serialNumber
	 *            the raw data line read from the MAX protocol
	 * @return device
	 * 				the {@link Device} information decoded in last refreshData
	 */

	public Device getDevice(String serialNumber) {
		return findDevice (serialNumber,devices);
	}

	/**
	 * Returns the MaxCube Devices Array decoded during the last refresh
	 * 
	 * @return devices
	 * 				the array of {@link Device} information decoded in the last refreshData
	 */
	public ArrayList<Device> getDevices() {
		return devices;
	}

	/**
	 * Returns true if the last connection to the Cube was successfull
	 *  
	 * @return device
	 * 				the {@link Device} information decoded in last refreshData
	 */
	public boolean isConnectionEstablished() {
		return connectionEstablished;
	}

	private ArrayList<String> getRawFAKEMessage(){ 
		ArrayList<String> rawMessage = new ArrayList<String> () ;
		rawMessage.add ("H:KEQ0565026,0b5951,0113,00000000,72aeba8b,03,32,0e090b,1127,03,0000");
		rawMessage.add ("M:00,01,VgICAQhiYWRrYW1lcgsNowIMU3R1ZGVlcmthbWVyB7bnAgILDaNLRVEwNTQ0MjQyEUJhZGthbWVyIFJhZGlhdG9yAQEHtudLRVEwMTQ1MTcyFVJhZGlhdG9yIFN0dWRlZXJrYW1lcgIB");
		rawMessage.add ("C:0b5951,7QtZUQATAf9LRVEwNTY1MDI2AQsABEAAAAAAAAAAAP///////////////////////////wsABEAAAAAAAAAAQf///////////////////////////2h0dHA6Ly9tYXguZXEtMy5kZTo4MC9jdWJlADAvbG9va3VwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAENFVAAACgADAAAOEENFU1QAAwACAAAcIA==");
		rawMessage.add ("C:0b0da3,0gsNowIBEABLRVEwNTQ0MjQyLCQ9CQcYAzAM/wBIYViRSP1ZFE0gTSBNIEUgRSBFIEUgRSBFIEhhWJFQ/VkVUSBRIFEgRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIEhQWFpI/lkUTSBNIE0gRSBFIEUgRSBFIEUgSFBYWkj+WRRNIE0gTSBFIEUgRSBFIEUgRSBIUFhaSP5ZFE0gTSBNIEUgRSBFIEUgRSBFIA==");
		rawMessage.add ("C:07b6e7,0ge25wECGP9LRVEwMTQ1MTcyKyE9CQcYAzAM/wBEflUaRSBFIEUgRSBFIEUgRSBFIEUgRSBFIER+VRpFIEUgRSBFIEUgRSBFIEUgRSBFIEUgRFRUcEjSVRJJIEkgSSBFIEUgRSBFIEUgRSBEVFRwSNJVEkkgSSBJIEUgRSBFIEUgRSBFIERUVG9U01URSSBJIEkgRSBFIEUgRSBFIEUgRFRUcEjSVRJJIEkgSSBFIEUgRSBFIEUgRSBEVFRwSNJVEkkgSSBJIEUgRSBFIEUgRSBFIA==");
		rawMessage.add ("L:CwsNowkSGQAJAAAACwe25wkSGWAvAAAA");
		return rawMessage;
	}
}
