/*
 * Copyright (c) 2021 Ford Motor Company
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fordlabs.hungrymoose.testing;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.net.ServerSocket;

public class UriUnderTestFactory {
    public static final String TARGET_HOST_ENVIRONMENT_KEY = "target.host";
    public static final String DEFAULT_HOST = "localhost";
    public static final String TARGET_PORT_ENVIRONMENT_KEY = "target.port";
    public static final int DEFAULT_PORT = 0;
    public static final String TARGET_PROTOCOL_ENVIRONMENT_KEY = "target.protocol";
    public static final String DEFAULT_PROTOCOL = "http";

    public static UriUnderTest getUriUnderTest() {
        return new UriUnderTest(getProtocol(), getHost(), getAvailablePort());
    }

    private static String getProtocol() {
        final String systemPropertyValue = getSystemProperty(TARGET_PROTOCOL_ENVIRONMENT_KEY);
        return systemPropertyValue == null ? DEFAULT_PROTOCOL : systemPropertyValue;
    }

    private static String getHost() {
        final String systemPropertyValue = getSystemProperty(TARGET_HOST_ENVIRONMENT_KEY);
        return systemPropertyValue == null ? DEFAULT_HOST : systemPropertyValue;
    }

    private static int getAvailablePort() {
        return getPortFromSystemProperty() != 0 ? getPortFromSystemProperty() : getNextAvailablePort();
    }

    private static int getPortFromSystemProperty() {
        final String systemPropertyValue = getSystemProperty(TARGET_PORT_ENVIRONMENT_KEY);
        return systemPropertyValue == null ? DEFAULT_PORT : NumberUtils.toInt(systemPropertyValue);
    }

    private static String getSystemProperty(final String key) {
        String envName = Preconditions.checkNotNull(key, "Environment Variable is required");
        if (envName.startsWith("${")) {
            envName = envName.replaceAll("\\$|\\{|\\}", "");
        }
        return System.getProperty(envName);
    }

    private static int getNextAvailablePort() {
        try {
            // ServerSocket returns the first available port if portRunning is 0
            final ServerSocket serverSocket = new ServerSocket(0);
            final int availablePort = serverSocket.getLocalPort();
            serverSocket.close();
            return availablePort;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
