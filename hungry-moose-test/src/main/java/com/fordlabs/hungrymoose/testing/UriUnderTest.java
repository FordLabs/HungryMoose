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

import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
public class UriUnderTest {

    private final String transferProtocol;
    private final String host;
    private final int port;

    public UriUnderTest(final String transferProtocol, final String host, final int port) {
        this.transferProtocol = transferProtocol;
        this.host = host;
        this.port = validatePort(port);
    }

    @Override
    public String toString() {
        return UriComponentsBuilder.newInstance()
                .scheme(this.transferProtocol)
                .host(this.host)
                .port(this.port).build()
                .toUriString();
    }

    private int validatePort(int port) {
        if(port >= 0 && port <= 65535) {
            return port;
        } else {
            throw new IllegalArgumentException(String.format("Port %d not in range [0 - 65535]", port));
        }
    }
}
