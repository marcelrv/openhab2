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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.miot.MiotParseException;
import org.openhab.binding.miio.internal.miot.MiotParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
public class MiotJsonFileCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotJsonFileCreator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String BASEDIR = "./src/main/resources/db/";
    private static final String FILENAME_EXTENSION = "-miot.json";
    private static final boolean overwriteFile = false;

    @Disabled
    public static void main(String[] args) {

        LinkedHashMap<String, String> checksums = new LinkedHashMap<>();

        LinkedHashSet<String> models = new LinkedHashSet<>();
        for (int i = 1; i <= 12; i++) {
            models.add("chuangmi.plug.212a0" + String.valueOf(i));
        }

        if (args.length > 0) {
            models.add(args[0]);
        }
        MiotParser miotParser;
        for (String model : models) {
            LOGGER.info("Processing: {}", model);
            try {
                miotParser = MiotParser.parse(model);
                LOGGER.info("urn: ", miotParser.getUrn());
                LOGGER.info("{}", miotParser.getUrnData());
                MiIoBasicDevice device = miotParser.getDevice();
                if (device != null) {
                    LOGGER.info("Device: {}", device);

                    String fileName = String.format("%s%s%s", BASEDIR, model, FILENAME_EXTENSION);
                    if (!overwriteFile) {
                        int counter = 0;
                        while (new File(fileName).isFile()) {
                            fileName = String.format("%s%s-%d%s", BASEDIR, model, counter, FILENAME_EXTENSION);
                            counter++;
                        }
                    }
                    miotParser.writeDevice(fileName, device);
                    String channelsJson = GSON.toJson(device.getDevice().getChannels()).toString();
                    checksums.put(model, checksumMD5(channelsJson));
                }
                LOGGER.info("finished");
            } catch (MiotParseException e) {
                LOGGER.info("Error processing model {}: {}", model, e.getMessage());
            } catch (Exception e) {
                LOGGER.info("Failed to initiate http Client: {}", e.getMessage());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> ch : checksums.entrySet()) {
            sb.append(ch.getValue());
            sb.append(" -->");
            sb.append(ch.getKey());
            sb.append("\r\n");
        }
        LOGGER.info("Checksums\r\n{}", sb.toString());
    }

    public static String checksumMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            return Utils.getHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "No MD5";
        }
    }

}
