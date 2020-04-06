package org.openhab.binding.miio.internal;

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

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MiIoConsoleCommandExtension} class provides additional options through the console command line.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = ConsoleCommandExtension.class, immediate = true, configurationPid = "console.miio")
public class MiIoConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_SEND = "send";
    private static final String SUBCMD_LOGIN = "login";
    private CloudConnector cloudConnector;

    @Activate
    public MiIoConsoleCommandExtension(@Reference CloudConnector cloudConnector) {
        super("miio", "Xiaomi Cloud Commands.");
        this.cloudConnector = cloudConnector;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            switch (args[0]) {
                case SUBCMD_SEND:
                    sendCloudRequest(args, console);
                    break;
                case SUBCMD_LOGIN:
                    console.println(String.format("Xiaomi cloud login succeeded %b", cloudConnector.isConnected()));
                    break;
                default:
                    console.println(String.format("Unknown miio sub command '%s'", args[0]));
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void sendCloudRequest(String[] args, Console console) {
        if (args.length > 2) {
            try {
                console.println(String.format("Sending command %s - %s", args[1], args[2]));
                final String response = cloudConnector.sendRequest(args[1], args[2]);
                console.println(response);
            } catch (Exception e) {
                console.println(String.format("Error sending command %s - %s", args[1], args[2]));
            }
        } else {
            console.println("Specify path and content to send.");
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(
                new String[] { buildCommandUsage(SUBCMD_SEND + " <path> <json to submit>", "Send to the Xiaomi cloud"),
                        buildCommandUsage(SUBCMD_LOGIN, "Login") });
    }

}