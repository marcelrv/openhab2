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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhab.binding.miio.internal.AllDeviceInstances.Xmdevices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Download the list of devices from miot list
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class CloudDeviceList {

    final static String url1b = "http://miot-spec.org/miot-spec-v2/instances?status=all";
    final String url1 = "http://miot-spec.org/miot-spec-v2/instances?status=released";
    final static String fn = "/temp/miotlist.json";
    final String url_device = "https://miot-spec.org/miot-spec-v2/instance?type=";

    final HashSet<String> btDevices = Stream.of("philips.light.nlight").collect(Collectors.toCollection(HashSet::new));

    // final String url_device =
    // "https://miot-spec.org/miot-spec-v2/instance?type=urn:miot-spec-v2:device:air-purifier:0000A007:zhimi-ma4:1";
    // protected static final Gson GSON = new GsonBuilder().create();
    private final static Logger logger = LoggerFactory.getLogger(CloudDeviceList.class);

    public static void main(String[] args) {
        CloudDeviceList cloudDeviceList = new CloudDeviceList();
        // cloudDeviceList.copyAlltoLocal();
        List<Instance> alldevs = cloudDeviceList.listAllDevices();
        // cloudDeviceList.bindingdevices(alldevs);
        cloudDeviceList.makeEnums(alldevs);
        // cloudDeviceList.checknames("urn:miot-spec-v2:device:air-purifier:0000A007:zhimi-ma4:1");
    }

    private void makeEnums(List<Instance> alldevs) {

        TreeMap<String, String> en = new TreeMap<String, String>();

        alldevs.forEach(dev -> {
            if (!dev.getModel().contains("philips")) {
                return;
            }
            if (btDevices.contains(dev.getModel())) {
                return;
            }
            MiIoDevices device = MiIoDevices.getType(dev.getModel());

            if (device.name().contentEquals(MiIoDevices.UNKNOWN.name())) {
                String des = "Light";// checknames(dev.getType());
                logger.info("{} - {} -> {}", dev.getModel(), device.getDescription(), des);
                en.put(dev.getModel(), des);
            } else {
                logger.info("Existng...{} - {} ", device.getModel(), device.getDescription());
            }

        });

        StringBuilder sb = new StringBuilder("List for inclusion in miio devices\r\n");
        for (String enu : en.keySet()) {

            sb.append(enu.toUpperCase().replace(".", "_"));
            sb.append("(\"");
            sb.append(enu);
            sb.append("\",");
            sb.append("\"");
            sb.append(en.get(enu));
            sb.append("\"");
            sb.append(",THING_TYPE_BASIC");
            sb.append("),\r\n");
        }
        logger.info(sb.toString());

        sb = new StringBuilder("List for getting apks\r\n");
        sb.append("String models[] = { ");
        for (String enu : en.keySet()) {
            sb.append("\"");
            sb.append(enu);
            sb.append("\",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("};\r\n");
        logger.info(sb.toString());
    }

    private void bindingdevices(List<Instance> alldevs) {

        Arrays.asList(MiIoDevices.values()).forEach(device -> {
            logger.debug("{} - {}", device.getModel(), device.getDescription());
            alldevs.forEach(dev -> {
                if (dev.getModel().equals(device.getModel())) {
                    logger.info("{} - {} -> {}", device.getModel(), device.getDescription(), checknames(dev.getType()));
                }
            });

        });
    }

    String checknames(String urn) {
        URL url;
        try {
            url = new URL(url_device + urn);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            // Gson GSON = new GsonBuilder().create();
            Xmdevices xmdevice = new Gson().fromJson(reader, Xmdevices.class);
            logger.debug("{} -> {}", xmdevice.getDescription(), xmdevice);
            return xmdevice.getDescription();
        } catch (IOException e) {
            logger.info("Error", e);
        }
        return "";
    }

    public void copyAlltoLocal() {
        try {
            URL url = new URL(url1);
            InputStream initialStream = url.openStream();
            File targetFile = new File(fn);
            FileUtils.copyInputStreamToFile(initialStream, targetFile);
            logger.info("saved file");
        } catch (MalformedURLException e) {
            logger.info("Error", e);
        } catch (IOException e) {
            logger.info("Error", e);
        }
    }

    public List<Instance> listAllDevices() {
        try {
            URL url = new URL(url1);
            // InputStreamReader reader = new InputStreamReader(url.openStream());
            InputStreamReader reader = new InputStreamReader(new File(fn).toURL().openStream());

            AllDeviceInstances dto = new Gson().fromJson(reader, AllDeviceInstances.class);
            logger.info("list");
            List<Instance> xmlist = dto.getInstances();

            xmlist.sort(Comparator.comparing(Instance::getModel));
            // xmlist.forEach(arg0);

            logger.info("Total device {}", xmlist.size());
            for (Instance i : xmlist) {
                // logger.info("{} ->> {}", i.getModel(), i.getType());
                logger.info("{}", i);
            }
            logger.info("Total device {}", xmlist.size());

            return xmlist;
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            logger.info("Error", e);

        }
        return Collections.<Instance>emptyList();
    }
}

/**
 * Download the list of devices from miot list
 *
 * @author Marcel Verpaalen - Initial contribution
 */

class Instance {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("type")
    @Expose
    private String type;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("model", model).append("version", version)
                .append("type", type).toString();
    }
}

/**
 * Download the list of devices from miot list
 *
 * @author Marcel Verpaalen - Initial contribution
 */
class AllDeviceInstances {

    @SerializedName("instances")
    @Expose
    private List<Instance> instances = null;

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instances", instances).toString();
    }

    class Xmdevices {

        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("services")
        @Expose
        private List<Object> services = null;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("type", type).append("description", description)
                    .append("services", services).toString();
        }
    }
}
