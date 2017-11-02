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
 * Mapping actions from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoDeviceAction {

    @SerializedName("command")
    @Expose
    private String command;
    @SerializedName("parameterType")
    @Expose
    private CommandParameterType CommandParameterType;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandParameterType getparameterType() {
        return CommandParameterType;
    }

    public void setparameterType(CommandParameterType type) {
        this.CommandParameterType = type;
    }

    public void setparameterType(String type) {
        this.CommandParameterType = org.openhab.binding.miio.internal.basic.CommandParameterType.fromString(type);
    }
}
