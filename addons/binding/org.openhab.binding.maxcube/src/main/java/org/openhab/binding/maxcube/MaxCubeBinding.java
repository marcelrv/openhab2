/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Lists;

/**
 * The {@link MaxCubeBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxCubeBinding {

	 public static final String BINDING_ID = "maxcube";
	   
	 // List of main device types 
	   public static final String DEVICE_THERMOSTAT = "thermostat";
	   public static final String DEVICE_SWITCH = "switch";
	 
	    // List of all Thing Type UIDs
	    public final static ThingTypeUID HEATHINGTHERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_THERMOSTAT);
	    public final static ThingTypeUID SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_SWITCH);
	    
	 // List of all Thing Type UIDs
	    public final static ThingTypeUID CubeBridge_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");

	    // List of all Channel ids
	    public final static String CHANNEL_VALVE = "valve";
	    public final static String CHANNEL_BATTERY = "battery";
	    public final static String CHANNEL_MODE = "mode";
	    public final static String CHANNEL_ACTUALTEMP = "actual_temp";
	    public final static String CHANNEL_SETTEMP = "set_temp";
	    
	    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
	    		HEATHINGTHERMOSTAT_THING_TYPE, CubeBridge_THING_TYPE);

}
