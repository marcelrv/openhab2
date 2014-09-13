/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;

import com.google.common.collect.ImmutableSet;



/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new 
 * Max!Cube LAN gateway devices on the network
 * 
 * @author Marcel Verpaalen - Initial contribution
 * 
 */
public class MaxCubeBridgeDiscovery   implements DiscoveryService {


	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return ImmutableSet.copyOf(new ThingTypeUID[] { MaxCubeBinding.CubeBridge_THING_TYPE });
	}


	public DiscoveryResult createCubeResult() {
		String cubeSerialNumber = null;
		
		HashMap<String, String > discoverResults = new HashMap<String, String>(MaxCubeDiscover.DiscoverCube());
		if (discoverResults.containsKey(MaxCubeConfiguration.SERIAL_NUMBER)){
			cubeSerialNumber = discoverResults.get(MaxCubeConfiguration.SERIAL_NUMBER);
		}

		
		if(cubeSerialNumber!=null) {

			ThingUID uid = new ThingUID( MaxCubeBinding.CubeBridge_THING_TYPE, "MaxCube_" + cubeSerialNumber);
			if(uid!=null) {
				Map<String, Object> properties = new HashMap<>(1);
				properties.put(MaxCubeConfiguration.SERIAL_NUMBER,cubeSerialNumber);
				DiscoveryResult result = DiscoveryResultBuilder.create(uid)
						.withProperties(properties)
						.withLabel("MaxCube LAN Gateway")
						.build();
				return result;
			} 
		}	
		return null;
	}


	
	public void startScan() {
		DiscoveryResult newCube = createCubeResult();
		//thingDiscovered (newCube);
	}


	@Override
	public int getScanTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setBackgroundDiscoveryEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isBackgroundDiscoveryEnabled() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void startScan(ScanListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void abortScan() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addDiscoveryListener(DiscoveryListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeDiscoveryListener(DiscoveryListener listener) {
		// TODO Auto-generated method stub
		
	}

	
		
	}

