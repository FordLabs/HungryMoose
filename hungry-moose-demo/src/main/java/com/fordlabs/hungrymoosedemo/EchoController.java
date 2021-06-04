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

package com.fordlabs.hungrymoosedemo;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static java.util.Set.of;

@RestController
public class EchoController {

    private static final Set<String> HEADERS_TO_IGNORE = of("content-length", "host", "connection", "user-agent", "accept-encoding");

    @RequestMapping(value = "/echo/**")
    public EchoResponse getRequest(@RequestHeader HttpHeaders headers, final HttpServletRequest request) throws Exception {
        return new EchoResponse(
                request.getRequestURI(),
                request.getMethod(),
                removeUnwantedHeadersFromRequest(headers),
                request.getReader().toString()
        );
    }

    private HttpHeaders removeUnwantedHeadersFromRequest(HttpHeaders headers) {
        HEADERS_TO_IGNORE.forEach(headers::remove);
        return headers;
    }

    public static class EchoResponse {
        public String uri;
        public String method;
        public HttpHeaders headers;
        public String body;

        public EchoResponse(String uri, String method, HttpHeaders headers, String body) {
            this.uri = uri;
            this.method = method;
            this.headers = headers;
            this.body = body;
        }
    }
}
