/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jointspace.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteService;
import org.openhab.binding.jointspace.JointspaceBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsDiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author David Gräff - Initial contribution
 */
@NonNullByDefault
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class PhilipsDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(PhilipsDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(JointspaceBindingConstants.THING_TYPE_TV);
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(3);
        String label = device.getDetails().getManufacturerDetails().getManufacturer() + " - "
                + device.getDetails().getFriendlyName();

        try {
            label += " " + device.getDetails().getModelDetails().getModelName();
        } catch (Exception e) {
            // ignore and use the default label
        }

        for (RemoteService s : device.getServices()) {
            logger.debug(s.toString());

            for (Action<RemoteService> a : s.getActions()) {
                logger.debug("{} - {} ({})", s.toString(), a.getName(), a.getArguments().toString());
                if (a.getName().equals("GetFeatureList")) {
                    logger.debug(a.getOutputArgument("FeatureList").toString());
                    logger.debug(a.getFirstOutputArgument().toString());
                    logger.debug(a.getOutputArguments().toString());
                }
            }
        }

        properties.put(JointspaceBindingConstants.PROPERTY_IP, device.getIdentity().getDescriptorURL().getHost());

        properties.put(JointspaceBindingConstants.UDN, device.getIdentity().getUdn().getIdentifierString());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).build();

        logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                device.getDetails().getModelDetails().getModelName(),
                device.getIdentity().getUdn().getIdentifierString());
        return result;
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String modelName = device.getDetails().getModelDetails().getModelName();
        String friedlyName = device.getDetails().getFriendlyName();

        // logger.debug("UPNP discovery: {} MODEL: {} NAME {} ", manufacturer, modelName, friedlyName);

        if (manufacturer == null || modelName == null) {
            return null;
        }

        // UDN shouldn't contain '-' characters.
        String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");
        logger.debug("UPNP discovery: {} tyoe: {}     type {} ", udn, device.getType(), device.getType().getType());
        if (manufacturer.toUpperCase().contains("PHILIPS") && device.getType().getType().equals("MediaRenderer")) {

            logger.debug("Discovered a Philips TV '{}' model '{}' thing with UDN '{}'", friedlyName, modelName, udn);

            return new ThingUID(JointspaceBindingConstants.THING_TYPE_TV, udn);
        } else {
            return null;
        }
    }
}
