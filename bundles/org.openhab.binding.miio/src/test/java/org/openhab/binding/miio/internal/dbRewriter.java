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

package org.openhab.binding.miio.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Ignore;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Support creation of the miio readme doc
 *
 * Run after adding devices or changing database entries of basic devices
 *
 * Run in IDE with 'run as java application'
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class dbRewriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(dbRewriter.class);
    private static final JsonParser parser = new JsonParser();

    @Ignore
    public static void main(String[] args) {
        dbRewriter rm = new dbRewriter();
        LOGGER.info("## Updating");
        rm.updateDatabase();
        LOGGER.info("## Done");
    }

    private void updateDatabase() {
        List<MiIoBasicDevice> arrayList = new ArrayList<>();
        String path = "./src/main/resources/database/";
        File dir = new File(path);
        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isFile()) {
                try {
                    JsonObject deviceMapping = convertFileToJSON(path + file.getName());
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    @Nullable
                    MiIoBasicDevice devdb = gson.fromJson(deviceMapping, MiIoBasicDevice.class);
                    if (devdb != null) {
                        arrayList.add(devdb);
                        Map<String, String> idd = new HashMap<>();
                        // List<MiIoIDsDTO> i = devdb.getDevice().getIds();
                        for (String id : devdb.getDevice().getId()) {
                            // MiIoIDsDTO newId = new MiIoIDsDTO();
                            // newId.setId(id);
                            // newId.setDescription(MiIoDevices.getType(id).getDescription());
                            // i.add(newId);
                            idd.put(id, MiIoDevices.getType(id).getDescription());
                        }
                        // devdb.getDevice().setIds(i);
                        devdb.getDevice().setIdd(idd);

                        for (MiIoBasicChannel ch : devdb.getDevice().getChannels()) {

                            if (!ch.getActions().isEmpty()) {
                                MiIoDeviceAction action = ch.getActions().get(0);
                                String preCommandPara1 = action.getPreCommandParameter1();
                                preCommandPara1 = ((preCommandPara1 != null && !preCommandPara1.isEmpty())
                                        ? preCommandPara1 + ","
                                        : "").replace("\"", "");
                                ;
                                String para1 = action.getParameter1();
                                String para2 = action.getParameter2();
                                String para3 = action.getParameter3();
                                String para = "" + (para1 != null ? "," + para1 : "")
                                        + (para2 != null ? "," + para2 : "") + (para3 != null ? "," + para3 : "");

                                if (!(preCommandPara1 + para).isEmpty()) {
                                    String paString = "[" + preCommandPara1 + "\"$value$\"" + para + "]";
                                    LOGGER.info(paString);
                                    JsonArray paramms = (JsonArray) parser.parse(paString);
                                    action.setParameters(paramms);
                                }
                            }
                        }

                        String usersJson = gson.toJson(devdb);

                        try (PrintWriter out = new PrintWriter(path + "../dbNew/" + file.getName())) {
                            out.println(usersJson);
                        }
                    }

                } catch (Exception e) {
                    LOGGER.debug("Error while searching  in database '{}': {}", file.getName(), e.getMessage());
                    LOGGER.info(e.getMessage());
                }
            }
        }

    }

    JsonObject convertFileToJSON(String fileName) {
        // Read from File to String
        JsonObject jsonObject = new JsonObject();

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(new FileReader(fileName));
            jsonObject = jsonElement.getAsJsonObject();
        } catch (FileNotFoundException e) {
            //
        }
        return jsonObject;
    }
}
