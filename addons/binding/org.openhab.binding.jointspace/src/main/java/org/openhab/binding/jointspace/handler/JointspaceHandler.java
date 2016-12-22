/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jointspace.handler;

import static org.openhab.binding.jointspace.JointspaceBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
import com.google.gson.JsonArray;
  import com.google.gson.JsonElement;
  import com.google.gson.JsonObject;
  import com.google.gson.JsonParser;
*/

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/* The {@link JointspaceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class JointspaceHandler extends BaseThingHandler {

    private String ipAddress;
    BigDecimal port = BigDecimal.ONE;
    private Logger logger = LoggerFactory.getLogger(JointspaceHandler.class);
    private ScheduledFuture<?> pollingJob;

    public JointspaceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Jointspace handler '{}'", getThing().getUID());

        try {
            Object param;

            ipAddress = (String) getConfig().get(PROPERTY_IP);

            param = getConfig().get(PROPERTY_PORT);
            if (param instanceof BigDecimal && param != null) {
                port = (BigDecimal) param;
            } else {
                port = BigDecimal.ONE;
            }

            int pollingPeriod = 30;
            param = getConfig().get(PROPERTY_REFRESH_INTERVAL);
            if (param instanceof BigDecimal && param != null) {
                pollingPeriod = ((BigDecimal) param).intValue();
            }

            updateProperty(Thing.PROPERTY_VENDOR, "Philips");
            updateProperty(Thing.PROPERTY_MODEL_ID, "WindMolen");
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, (ipAddress));

            pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    // updateData();
                }
            }, 0, pollingPeriod, TimeUnit.SECONDS);
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());

            UDN udn = (UDN) getConfig().get(UDN);
            RemoteDeviceIdentity rdi = null; // = new RemoteDeviceIdentity()
            RemoteDevice rd = new RemoteDevice(rdi).findDevice(udn);

            for (RemoteService s : rd.getServices()) {
                logger.debug(s.toString());
                for (Action<RemoteService> a : s.getActions()) {
                    logger.debug("{} - {}", s.toString(), a.getName());
                }
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());

        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Jointspace handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private synchronized void updateData() {
        logger.debug("Update Jointspace data '{}'", getThing().getUID());

        try {

            String getMillData = getMillData("system");
            JsonParser parser = new JsonParser();
            JsonObject tvData = (JsonObject) parser.parse(getMillData);
            logger.debug(tvData.toString());
            logger.debug("ELEMENTS");
            for (Entry<String, JsonElement> m : tvData.entrySet()) {
                logger.debug("key {} ; {}", m.getKey(), m.getValue());
                if (m.getKey().equals("featuring")) {
                    for (Entry<String, JsonElement> feat : ((JsonObject) m.getValue()).entrySet()) {
                        logger.debug("feature {} ; {}", feat.getKey(), feat.getValue());
                    }
                }
            }

            /*
             * updateState(CHANNEL_WIND_SPEED, new DecimalType(millData.get(CHANNEL_WIND_SPEED).getAsString()));
             * updateState(CHANNEL_WIND_DIRECTION, new StringType(millData.get(CHANNEL_WIND_DIRECTION).getAsString()));
             * updateState(CHANNEL_POWER_TOTAL, new DecimalType(millData.get(CHANNEL_POWER_TOTAL).getAsBigDecimal()));
             * updateState(CHANNEL_POWER_PER_WD,
             * new DecimalType(millData.get(CHANNEL_POWER_PER_WD).getAsBigDecimal().multiply(port)));
             * updateState(CHANNEL_POWER_RELATIVE,
             * new DecimalType(millData.get(CHANNEL_POWER_RELATIVE).getAsBigDecimal()));
             * updateState(CHANNEL_ENERGY, new DecimalType(millData.get(CHANNEL_ENERGY).getAsBigDecimal()));
             * updateState(CHANNEL_ENERGY_FC, new DecimalType(millData.get(CHANNEL_ENERGY_FC).getAsBigDecimal()));
             * updateState(CHANNEL_RUNTIME, new DecimalType(millData.get(CHANNEL_RUNTIME).getAsBigDecimal()));
             * updateState(CHANNEL_RUNTIME_PER, new DecimalType(millData.get(CHANNEL_RUNTIME_PER).getAsBigDecimal()));
             * updateState(CHANNEL_LAST_UPDATE, new DateTimeType(millData.get(CHANNEL_LAST_UPDATE).getAsString()));
             */
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Exception e) {
            logger.debug(e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private String getMillData(String parameter) throws IOException {

        String baseURL = "http://" + ipAddress + ":" + port;
        String urlString = baseURL + "/" + parameter;

        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            return "";// IOUtils.toString(connection.getInputStream());
        } catch (MalformedURLException e) {
            logger.debug("Constructed url '{}' is not valid: {}", urlString, e.getMessage());
            return null;
        }
    }
}
