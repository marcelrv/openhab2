/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.factory;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.maxcube.MaxCubeBinding;
import org.openhab.binding.maxcube.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.maxcube.config.MaxCubeConfiguration;
import org.openhab.binding.maxcube.internal.handler.MaxCubeBridgeHandler;
import org.openhab.binding.maxcube.internal.handler.MaxCubeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */

public class MaxCubeHandlerFactory extends BaseThingHandlerFactory {
    
	 private Logger logger = LoggerFactory.getLogger(MaxCubeHandlerFactory.class);
  //  private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(HeathingThermostat_THING_TYPE);
    
    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            ThingUID thingUID, ThingUID bridgeUID) {
    	
    	logger.debug("Thing createThing run");
        if (MaxCubeBinding.CubeBridge_THING_TYPE.equals(thingTypeUID)) {
            ThingUID CubeBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, CubeBridgeUID, null);
        }
        if (MaxCubeBinding.HeathingThermostat_THING_TYPE.equals(thingTypeUID)) {
            ThingUID thermostatUID = getThermostatUID(thingTypeUID, thingUID, configuration /*, bridgeUID */);
             return super.createThing(thingTypeUID, configuration, thermostatUID /*, bridgeUID*/);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID
                + " is not supported by the MaxCube binding.");
    }
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    	logger.debug("Thing supportsThingType run");
        return MaxCubeBinding.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String ipAddress = (String) configuration.get(MaxCubeBridgeConfiguration.IP_ADDRESS);
            thingUID = new ThingUID(thingTypeUID, ipAddress);
        }
        return thingUID;
    }

    private ThingUID getThermostatUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration /*, ThingUID bridgeUID */) {
         String SerialNumber = (String) configuration.get(MaxCubeConfiguration.SERIAL_NUMBER);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, "Device" + SerialNumber /*, bridgeUID.getId()*/);
        }
        return thingUID;
    }
    
    @Override
    protected ThingHandler createHandler(Thing thing) {
    	logger.debug("ThingHandler createHandler run");
        if (thing.getThingTypeUID().equals(MaxCubeBinding.CubeBridge_THING_TYPE)) {
           return new MaxCubeBridgeHandler((Bridge) thing);
        } else if (thing.getThingTypeUID().equals(MaxCubeBinding.HeathingThermostat_THING_TYPE)) {
            return new MaxCubeHandler(thing);
        } else {
            return null;
        }
    }

}