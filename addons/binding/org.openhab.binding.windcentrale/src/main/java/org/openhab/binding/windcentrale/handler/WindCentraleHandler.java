/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale.handler;

import static org.openhab.binding.windcentrale.WindCentraleBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
import com.google.gson.JsonArray;
  import com.google.gson.JsonElement;
  import com.google.gson.JsonObject;
  import com.google.gson.JsonParser;
*/

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/* The {@link WindCentraleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class WindCentraleHandler extends BaseThingHandler {

    private int millId = 131;
    BigDecimal wd = BigDecimal.ONE;
    private Logger logger = LoggerFactory.getLogger(WindCentraleHandler.class);
    private ScheduledFuture<?> pollingJob;

    public WindCentraleHandler(Thing thing) {
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
        logger.debug("Initializing WindCentrale handler '{}'", getThing().getUID());

        Object param;

        param = getConfig().get(PROPERTY_MILL_ID);
        if (param instanceof BigDecimal && param != null) {
            millId = ((BigDecimal) param).intValue();
        } else {
            millId = 1;
        }

        param = getConfig().get(PROPERTY_WD);
        if (param instanceof BigDecimal && param != null) {
            wd = (BigDecimal) param;
        } else {
            wd = BigDecimal.ONE;
        }

        int pollingPeriod = 30;
        param = getConfig().get(PROPERTY_REFRESH_INTERVAL);
        if (param instanceof BigDecimal && param != null) {
            pollingPeriod = ((BigDecimal) param).intValue();
        }

        updateProperty(Thing.PROPERTY_VENDOR, "WindCentrale");
        updateProperty(Thing.PROPERTY_MODEL_ID, "WindMolen");
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, Integer.toString(millId));

        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WindCentrale handler '{}'", getThing().getUID());
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private synchronized void updateData() {
        logger.debug("Update WindMolen data '{}'", getThing().getUID());

        try {

            //String getMillData = "{\"powerProducerId\":\"563fbecd-d6f8-4d5e-9c3a-5e8577c7e256\",\"windSpeed\":4.0,\"windDirection\":\"NW\",\"powerAbsTot\":141.0,\"powerAbsWd\":26.0,\"powerRel\":17.0,\"diameter\":29.0,\"rpm\":22.4,\"pulsating\":false,\"kwh\":1459683.0,\"kwhForecast\":2767000.0,\"hoursRunThisYear\":4606.0,\"runPercentage\":97.98130630630631,\"windSpeedForecast\":3.0,\"windDirectionForecast\":\"NW\",\"timestamp\":\"2016-07-14T11:30:08\"}";
            String getMillData = getMillData();
            JsonParser parser = new JsonParser();
            JsonObject millData = (JsonObject) parser.parse(getMillData);
            logger.debug(millData.toString());
            updateState(CHANNEL_WIND_SPEED, new DecimalType(millData.get(CHANNEL_WIND_SPEED).getAsString()));
            updateState(CHANNEL_WIND_DIRECTION, new StringType(millData.get(CHANNEL_WIND_DIRECTION).getAsString()));
            updateState(CHANNEL_POWER_TOTAL, new DecimalType(millData.get(CHANNEL_POWER_TOTAL).getAsBigDecimal()));
            updateState(CHANNEL_POWER_PER_WD,
                    new DecimalType(millData.get(CHANNEL_POWER_PER_WD).getAsBigDecimal().multiply(wd)));
            updateState(CHANNEL_POWER_RELATIVE,
                    new DecimalType(millData.get(CHANNEL_POWER_RELATIVE).getAsBigDecimal()));
            updateState(CHANNEL_ENERGY, new DecimalType(millData.get(CHANNEL_ENERGY).getAsBigDecimal()));
            updateState(CHANNEL_ENERGY_FC, new DecimalType(millData.get(CHANNEL_ENERGY_FC).getAsBigDecimal()));
            updateState(CHANNEL_RUNTIME, new DecimalType(millData.get(CHANNEL_RUNTIME).getAsBigDecimal()));
            updateState(CHANNEL_RUNTIME_PER, new DecimalType(millData.get(CHANNEL_RUNTIME_PER).getAsBigDecimal()));
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType(millData.get(CHANNEL_LAST_UPDATE).getAsString()));

            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }

        } catch (Exception e) {
            logger.debug(e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private String getMillData() throws IOException {

        String baseURL = "https://zep-api.windcentrale.nl/production/";
        String urlString = baseURL + millId + "/live?ignoreLoadingBar=true";
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            return IOUtils.toString(connection.getInputStream());
        } catch (MalformedURLException e) {
            logger.debug("Constructed url '{}' is not valid: {}", urlString, e.getMessage());
            return null;
        }
    }
}
