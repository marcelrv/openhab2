/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link windcentralerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class WindCentraleBindingConstants {

    public static final String BINDING_ID = "windcentrale";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MILL = new ThingTypeUID(BINDING_ID, "mill");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MILL);

    // List of all Channel IDs
    public final static String CHANNEL_WIND_SPEED = "windSpeed";
    public final static String CHANNEL_WIND_DIRECTION = "windDirection";
    public final static String CHANNEL_POWER_TOTAL = "powerAbsTot";
    public final static String CHANNEL_POWER_PER_WD = "powerAbsWd";
    public final static String CHANNEL_POWER_RELATIVE = "powerRel";
    public final static String CHANNEL_ENERGY = "kwh";
    public final static String CHANNEL_ENERGY_FC = "kwhForecast";
    public final static String CHANNEL_RUNTIME = "hoursRunThisYear";
    public final static String CHANNEL_RUNTIME_PER = "runPercentage";
    public final static String CHANNEL_LAST_UPDATE = "timestamp";

    public final static String PROPERTY_MILL_ID = "millId";
    public final static String PROPERTY_QTY_WINDDELEN = "wd";
    public final static String PROPERTY_REFRESH_INTERVAL = "refreshInterval";

}
