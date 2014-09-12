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
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
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
public class MaxCubeBridgeDiscovery  extends AbstractDiscoveryService {

	public MaxCubeBridgeDiscovery(Set<ThingTypeUID> supportedThingTypes,
			int timeout) throws IllegalArgumentException {
		super(supportedThingTypes, timeout);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return ImmutableSet.copyOf(new ThingTypeUID[] { MaxCubeBinding.CubeBridge_THING_TYPE });
	}


	public DiscoveryResult createCubeResult() {
		String cubeSerialNumber = null;
		ConcurrentHashMap<String, String > discoverResults = new ConcurrentHashMap<String, String>(MaxCubeDiscover.DiscoverCube());
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


	@Override
	protected void startScan() {
		DiscoveryResult newCube = createCubeResult();
		thingDiscovered (newCube);
	}

}
