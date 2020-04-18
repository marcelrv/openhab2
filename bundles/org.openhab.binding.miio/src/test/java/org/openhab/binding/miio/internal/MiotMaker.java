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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Ignore;
import org.openhab.binding.miio.internal.basic.CommandParameterType;
import org.openhab.binding.miio.internal.basic.DeviceMapping;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.miot.MiotDeviceDataDTO;
import org.openhab.binding.miio.internal.miot.PropertyDTO;
import org.openhab.binding.miio.internal.miot.ServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Support creation of the miot db files
 *
 * Run to make a miot database file
 *
 * Run in IDE with 'run as java application'
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotMaker.class);
    private static final String BASEDIR = "./src/main/resources/dbNew/";

    private static final String BASEURL = "http://miot-spec.org/miot-spec-v2/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser PARSER = new JsonParser();

    private static final HttpClient httpClient = new HttpClient();

    private String model;

    @Ignore
    public static void main(String[] args) {

        String model = "zhimi.airpurifier.ma4";
        LOGGER.info("Processing :{}", model);

        MiotMaker miiotMaker;
        try {
            miiotMaker = new MiotMaker(model);

            String urn = miiotMaker.getURN(model);
            if (urn == null) {
                LOGGER.info("Device not found in in miot specs :{}", model);
                return;
            }

            JsonElement urnData = miiotMaker.getUrnData(urn);
            LOGGER.info("{}", urn);
            LOGGER.info("{}", urnData.toString());
            MiIoBasicDevice device = miiotMaker.getDevice(urnData);
            LOGGER.info("Device: {}", device);
            miiotMaker.writeDevice(BASEDIR + model + "-miot.json", device);
            miiotMaker.writeEmuDevice(BASEDIR + model + "-miotEMU.json", device);

            LOGGER.info("finished");
            httpClient.stop();
        } catch (Exception e) {
            LOGGER.info("Failed to initiate http Client: {}", e.getMessage());
        }
    }

    private void writeEmuDevice(String path, MiIoBasicDevice device) {
        JsonObject usersJson = new JsonObject();
        JsonArray properties = new JsonArray();

        for (MiIoBasicChannel ch : device.getDevice().getChannels()) {
            JsonObject prop = new JsonObject();
            prop.addProperty("property", ch.getProperty());
            switch (ch.getType()) {
                case "Dimmer":
                case "Number":
                    prop.addProperty("fakeresponse", (int) (Math.random() * 100));
                    break;
                case "Switch":
                    prop.addProperty("fakeresponse", "true");
                    break;
                default:
                    prop.addProperty("fakeresponse", "normal");
                    break;

            }
            prop.addProperty("datatype", ch.getType());
            properties.add(prop);
        }

        usersJson.add("properties", properties);
        try (

                PrintWriter out = new PrintWriter(path)) {
            out.println(usersJson);
            LOGGER.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            LOGGER.info("Error writing file: {}", e.getMessage());
        }
    }

    private void writeDevice(String path, MiIoBasicDevice device) {
        String usersJson = GSON.toJson(device);
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(usersJson);
            LOGGER.info("Database file created:{}", path);
        } catch (FileNotFoundException e) {
            LOGGER.info("Error writing file: {}", e.getMessage());
        }

    }

    private MiIoBasicDevice getDevice(JsonElement urnData) {
        MiIoBasicDevice device = new MiIoBasicDevice();
        DeviceMapping deviceMapping = new DeviceMapping();
        MiotDeviceDataDTO miotDevice = GSON.fromJson(urnData, MiotDeviceDataDTO.class);
        List<MiIoBasicChannel> miIoBasicChannels = new ArrayList<>();
        deviceMapping.setPropertyMethod(MiIoCommand.GET_PROPERTIES.getCommand());
        deviceMapping.setMaxProperties(2);
        deviceMapping.setId(Arrays.asList(new String[] { model }));
        Set<String> propCheck = new HashSet<>();
        for (ServiceDTO service : miotDevice.services) {
            LOGGER.info("SID: {}, description: {}", service.siid, service.description);

            for (PropertyDTO property : service.properties) {
                LOGGER.info("siid: {}, description: {}, piid: {},description: {}", service.siid, service.description,
                        property.piid, property.description);
                if (property.access.contains("read")) {
                    MiIoBasicChannel miIoBasicChannel = new MiIoBasicChannel();
                    String prop = property.type;
                    prop = prop.substring(prop.indexOf("property:")).split(":")[1];
                    // miIoBasicChannel.setProperty(prop);
                    miIoBasicChannel.setFriendlyName(service.description + "-" + property.description);
                    miIoBasicChannel.setSiid(service.siid);
                    miIoBasicChannel.setPiid(property.piid);
                    // avoid duplicates and make camel case and avoid wrong names
                    String chanId = WordUtils.capitalizeFully(prop.replace("-", " ").replace(".", " ")).replace(" ",
                            "");

                    int cnt = 0;
                    while (propCheck.contains(chanId + Integer.toString(cnt))) {
                        cnt++;
                    }
                    propCheck.add(chanId.concat(Integer.toString(cnt)));

                    if (cnt > 0) {
                        chanId = chanId.concat(Integer.toString(cnt));
                        prop = prop.concat(Integer.toString(cnt));
                        LOGGER.warn("duplicate for property:{} - {} ({}", chanId, property.description, cnt);
                    }
                    miIoBasicChannel.setProperty(prop);
                    miIoBasicChannel.setChannel(chanId);
                    miIoBasicChannel.setChannelType("miot_" + property.format);

                    switch (property.format) {
                        case "bool":
                            miIoBasicChannel.setType("Switch");
                            break;
                        case "uint8":
                        case "int32":
                        case "float":
                            miIoBasicChannel.setType("Number");
                            break;
                        case "string":
                            miIoBasicChannel.setType("String");
                            break;
                        default:
                            miIoBasicChannel.setType("String");
                            LOGGER.info("no type mapping for {}", property.format);
                            break;
                    }
                    miIoBasicChannel.setRefresh(true);

                    if (property.access.contains("write")) {
                        List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
                        MiIoDeviceAction action = new MiIoDeviceAction();
                        action.setCommand("set_properties");
                        switch (property.format) {
                            case "bool":
                                action.setparameterType(CommandParameterType.ONOFFBOOL);
                                break;
                            case "uint8":
                            case "int32":
                            case "float":
                                action.setparameterType(CommandParameterType.NUMBER);
                                break;
                            case "string":
                                action.setparameterType(CommandParameterType.STRING);

                                break;
                            default:
                                action.setparameterType(CommandParameterType.STRING);
                                break;
                        }
                        miIoDeviceActions.add(action);
                        miIoBasicChannel.setActions(miIoDeviceActions);
                    }
                    miIoBasicChannels.add(miIoBasicChannel);
                } else {
                    LOGGER.info("No reading siid: {}, description: {}, piid: {},description: {}", service.siid,
                            service.description, property.piid, property.description);
                }
            }
        }
        deviceMapping.setChannels(miIoBasicChannels);
        device.setDevice(deviceMapping);
        return device;
    }

    private JsonElement getUrnData(String urn)
            throws InterruptedException, TimeoutException, ExecutionException, JsonParseException {
        ContentResponse response;
        response = httpClient.newRequest(BASEURL + "instance?type=" + urn).timeout(15, TimeUnit.SECONDS).send();
        JsonElement json = PARSER.parse(response.getContentAsString());
        return json;
    }

    MiotMaker(String model) throws Exception {
        httpClient.setFollowRedirects(false);
        httpClient.start();
        this.model = model;
    }

    private @Nullable String getURN(String model) {
        ContentResponse response;
        try {
            response = httpClient.newRequest(BASEURL + "instances?status=released").timeout(15, TimeUnit.SECONDS)
                    .send();
            JsonElement json = PARSER.parse(response.getContentAsString());
            urns data = GSON.fromJson(json, urns.class);
            for (ModelUrns devices : data.getInstances()) {
                if (devices.model.contentEquals(model)) {
                    return devices.type;
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOGGER.debug("Failed downloading models: {}", e.getMessage());
        } catch (JsonParseException e) {
            LOGGER.debug("Failed parsing downloading models: {}", e.getMessage());
        }
        return null;
    }

    public class urns {
        @SerializedName("instances")
        @Expose
        private List<ModelUrns> instances = Collections.emptyList();

        public List<ModelUrns> getInstances() {
            return instances;
        }
    }

    public class ModelUrns {
        @SerializedName("model")
        @Expose
        private String model = "";
        @SerializedName("version")
        @Expose
        private Integer version = 0;
        @SerializedName("type")
        @Expose
        private String type = "";
    }
}
