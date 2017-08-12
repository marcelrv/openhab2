/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*  Mapping devices from json
*
* @author Marcel Verpaalen - Initial contribution
*/
public class DeviceMapping {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("properties")
    @Expose
    private List<MiIoBasicProperty> miIoBasicProperties = null;
    @SerializedName("actions")
    @Expose
    private List<MiIoDeviceAction> miIoDeviceActions = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<MiIoBasicProperty> getProperties() {
        return miIoBasicProperties;
    }

    public void setProperties(List<MiIoBasicProperty> miIoBasicProperties) {
        this.miIoBasicProperties = miIoBasicProperties;
    }

    public List<MiIoDeviceAction> getActions() {
        return miIoDeviceActions;
    }

    public void setActions(List<MiIoDeviceAction> miIoDeviceActions) {
        this.miIoDeviceActions = miIoDeviceActions;
    }
}
