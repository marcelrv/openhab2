/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;

import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Automatic UDP discovery of a MAX!Cube Lan Gateway on the local network. 
* 
* @author Marcel Verpaalen, based on UDP client code of Michiel De Mey 
* @since 1.4.0
*/
public final class MaxCubeDiscover {

	static Logger logger = LoggerFactory.getLogger(MaxCubeDiscover.class);
	
	/**
	* Automatic UDP discovery of a MAX!Cube
	* @return if the cube is found, returns the IP address as a string. Otherwise returns null
	*/
	public synchronized final static String discoverIp () {
		HashMap<String, String > discoverResults = new HashMap<String, String>(DiscoverCube());
		if (discoverResults.containsKey(MaxCubeBridgeConfiguration.IP_ADDRESS)){
			return discoverResults.get(MaxCubeBridgeConfiguration.IP_ADDRESS);
		} else {
		return null;
		}
	}
	
	/**
	* Automatic UDP discovery of a MAX!Cube
	* @return if the cube is found, returns the HashMap containing the details.
	*/
	public synchronized final static HashMap<String, String > DiscoverCube() {
		
		HashMap<String, String > discoverResults = new HashMap<String, String>();
		String maxCubeIP = null;
		String maxCubeName = null;
		String serialNumber = null;
		
		DatagramSocket bcSend = null;
		//Find the MaxCube using UDP broadcast
		try {
			bcSend = new DatagramSocket();
			bcSend.setBroadcast(true);

			byte[] sendData = "eQ3Max*\0**********I".getBytes();

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					// Send the broadcast package!
					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 23272);
						bcSend.send(sendPacket);
					} catch (Exception e) {
						logger.debug( "Error while sending request packet sent to: {} Interface: '{}' '{}'", broadcast.getHostAddress(),  networkInterface.getDisplayName(),  networkInterface.getName());

						logger.debug(e.getMessage());
						logger.debug(Utils.getStackTrace(e));
				}

				logger.debug( "Request packet sent to: {} Interface: {}", broadcast.getHostAddress(),  networkInterface.getDisplayName());
			}
		}

		} catch (IOException ex) {
			logger.debug(ex.toString());
		}
		try {
			bcSend.close();
		} catch (Exception e) {
			logger.debug(e.toString());
		}

		logger.debug( "Done looping over all network interfaces. Now waiting for a reply!");
		DatagramSocket bcReceipt = null;
		try{
		bcReceipt = new DatagramSocket(23272);
		bcReceipt.setReuseAddress(true);
		bcReceipt.setSoTimeout(10000);
		//Wait for a response
		byte[] recvBuf = new byte[15000];
		DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
		bcReceipt.receive(receivePacket);

		//We have a response
		logger.trace( "Broadcast response from server: {}", receivePacket.getAddress());

		//Check if the message is correct
		String message = new String(receivePacket.getData()).trim();
		if (message.startsWith("eQ3Max")) {
			maxCubeIP=receivePacket.getAddress().getHostAddress();
			maxCubeName=message.substring(0, 8);
			serialNumber=message.substring(8, 18);
			logger.debug("Found at: {}", maxCubeIP);
			logger.debug("Name    : {}", maxCubeName);
			logger.debug("Serial  : {}", serialNumber);
			logger.trace("Message : {}", message);
			
		} else {
			logger.info("No Max!Cube gateway found on network");
		}

		} catch (IOException ex) {
			logger.debug(ex.toString());
		}
		
		try {
			//Close the port!
			bcReceipt.close();
		} catch (Exception e) {
			// ignore error
		}
		discoverResults.put(MaxCubeBridgeConfiguration.IP_ADDRESS, maxCubeIP);
		discoverResults.put(MaxCubeConfiguration.FRIENDLY_NAME, maxCubeName);
		discoverResults.put(MaxCubeConfiguration.SERIAL_NUMBER, serialNumber);
		return discoverResults;
	}
}
