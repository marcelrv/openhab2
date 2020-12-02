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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.basic.DeviceMapping;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Support validating of the Json database files
 *
 *
 * Run in IDE with 'run as java application' than use the formatter to restore the original formaaating as used by OH
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.DataBaseValidator" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class DataBaseValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseValidator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // private static final JsonParser PARSER = new JsonParser();

    public static void main(String[] args) {
        DataBaseValidator databaseValidator = new DataBaseValidator();
        Map<String, MiIoBasicDevice> devicesList = databaseValidator.findDatabaseEntrys();
        LOGGER.info("Total Json files {}", devicesList.size());

        for (String d : devicesList.keySet()) {
            MiIoBasicDevice deviceFile = devicesList.get(d);

            LOGGER.debug("{} --> {}", d, devicesList.get(d));

            DeviceMapping device = deviceFile.getDevice();
            LOGGER.info("{} --> {}", d, device.getId().get(0));

            if (!device.getId().contains("dmaker.fan.p5")) {
                LOGGER.info("writing {} --> {}", d, device.getId().get(0));
                DataBaseValidator.writeDevice(d, deviceFile);
            }

        }
    }

    private Map<String, MiIoBasicDevice> findDatabaseEntrys() {
        HashMap<String, MiIoBasicDevice> arrayList = new HashMap<>();
        String path = "./src/main/resources/database/";
        File dir = new File(path);
        File[] filesList = dir.listFiles();
        for (File file : filesList) {
            if (file.isFile()) {
                try {
                    JsonObject deviceMapping = convertFileToJSON(path + file.getName());
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    @Nullable
                    MiIoBasicDevice devdb = gson.fromJson(deviceMapping, MiIoBasicDevice.class);
                    arrayList.put(file.getAbsolutePath(), devdb);
                } catch (FileNotFoundException | JsonParseException e) {
                    LOGGER.info("Error while searching  in database '{}': {}", file.getName(), e.getMessage());
                }
            }
        }
        return arrayList;
    }

    private JsonObject convertFileToJSON(String fileName)
            throws FileNotFoundException, JsonParseException, JsonIOException, JsonSyntaxException {
        // Read from File to String
        JsonObject jsonObject = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(fileName));
        jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;
    }

    private static void writeDevice(String path, MiIoBasicDevice device) {
        String usersJson = GSON.toJson(device);
        // for now still Windows endings
        usersJson = usersJson.replace("\n", "\r\n");
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(usersJson);
            LOGGER.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            LOGGER.info("Error writing file: {}", e.getMessage());
        }
    }
}
