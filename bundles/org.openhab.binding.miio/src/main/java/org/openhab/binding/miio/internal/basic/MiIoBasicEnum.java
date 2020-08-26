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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoBasicEnum {

    @SerializedName("id")
    @Expose
    private @Nullable String id;
    @SerializedName("values")
    @Expose
    private Map<String, Object> values = new HashMap<>();

    public final @Nullable String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final Map<String, Object> getValues() {
        return values;
    }

    public final void setValues(Map<String, Object> values) {
        this.values = values;
    }

}
