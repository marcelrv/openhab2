/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoBasicProperty {

    @SerializedName("property")
    @Expose
    private String property;
    @SerializedName("friendlyName")
    @Expose
    private String friendlyName;
    @SerializedName("channel")
    @Expose
    private String channel;
    @SerializedName("channelType")
    @Expose
    private String channelType;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("refresh")
    @Expose
    private Boolean refresh;
    @SerializedName("ChannelGroup")
    @Expose
    private String channelGroup;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelType() {
        channelType = channelType == null ? channel : channelType;
        return channelType.isEmpty() ? channel : channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    public String getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public String toString() {
        return "[ Property = " + property + ", ChannelGroup = " + channelGroup + ", friendlyName = " + friendlyName
                + ", type = " + type + ", refresh = " + refresh + ", channel = " + channel + ", channelType = "
                + getChannelType() + "]";
    }
}
