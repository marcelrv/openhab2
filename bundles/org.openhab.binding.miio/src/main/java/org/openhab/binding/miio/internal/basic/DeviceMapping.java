/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.miio.internal.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.MiIoCommand;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping devices from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class DeviceMapping {

    @SerializedName("id")
    @Expose
    private List<String> id = new ArrayList<>();
    @SerializedName("dids")
    @Expose
    private Map<String, String> dids = Collections.emptyMap();
    @SerializedName("propertyMethod")
    @Expose
    private @Nullable String propertyMethod;
    @SerializedName("maxProperties")
    @Expose
    private @Nullable Integer maxProperties;
    @SerializedName("deviceParameters")
    @Expose
    private @Nullable Map<String, Object> deviceParameters;
    @SerializedName("channels")
    @Expose
    private List<MiIoBasicChannel> miIoBasicChannels = new ArrayList<MiIoBasicChannel>();

    public Map<String, String> getDid() {
        return dids;
    }

    public void setDid(Map<String, String> did) {
        this.dids = did;
    }

    public List<String> getId() {
        return id;
    }

    public void setId(List<String> id) {
        this.id = id;
    }

    public String getPropertyMethod() {
        return propertyMethod != null ? propertyMethod : MiIoCommand.GET_PROPERTY.getCommand();
    }

    public void setPropertyMethod(String propertyMethod) {
        this.propertyMethod = propertyMethod;
    }

    public int getMaxProperties() {
        return maxProperties != null ? maxProperties : 5;
    }

    public void setMaxProperties(int maxProperties) {
        this.maxProperties = maxProperties;
    }

    public @Nullable Map<String, Object> getDeviceParameters() {
        return deviceParameters;
    }

    public void setDeviceParameters(Map<String, Object> deviceParameters) {
        this.deviceParameters = deviceParameters;
    }

    public List<MiIoBasicChannel> getChannels() {
        return miIoBasicChannels;
    }

    public void setChannels(List<MiIoBasicChannel> miIoBasicChannels) {
        this.miIoBasicChannels = miIoBasicChannels;
    }

}
