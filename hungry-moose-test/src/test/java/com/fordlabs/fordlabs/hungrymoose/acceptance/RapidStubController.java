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

package com.fordlabs.fordlabs.hungrymoose.acceptance;

import com.fordlabs.hungrymoose.model.Header;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/rapid-stub")
public class RapidStubController {

    public static final Set<String> HEADERS_TO_IGNORE = ImmutableSet.of("content-length", "host", "connection", "user-agent", "accept-encoding");

    @RequestMapping(value = "/valid-json", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> validGetJson() {
        return new ResponseEntity<>(new JsonData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/valid-json", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> validPostJson(@RequestBody Object params) {
        return new ResponseEntity<>(new JsonData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/im-conflicted", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> throwConflictStatusCode() {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/knock-knock-headers", method = { RequestMethod.POST, RequestMethod.GET }, produces = "application/json")
    public ResponseEntity<Object> knockKnockHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        List<Header> headers = loadHeaders(new ArrayList<>(), headerNames, request);

        return new ResponseEntity<>(new JsonHeaderData(headers), HttpStatus.OK);
    }

    private List<Header> loadHeaders(List<Header> list, Enumeration<String> headerNames, HttpServletRequest request) {
        if (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!skipHeader(headerName)) {
                list.add(new Header(headerName, request.getHeader(headerName)));
            }
            return loadHeaders(list, headerNames, request);
        }
        return list;
    }

    private boolean skipHeader(String headerName) {
        return HEADERS_TO_IGNORE.contains(headerName);
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/api", method = RequestMethod.GET, produces = "text/yaml")
    public ResponseEntity<String> api() throws Exception {
        InputStream examplesInput = getClass().getClassLoader().getResourceAsStream("api/hungrymoose/spec.yaml");
        String content = IOUtils.toString(examplesInput);
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @Data
    public static class JsonData {
        private String data = "bar";
    }

    @Getter
    public static class JsonHeaderData {
        private final List<Header> headers;
        JsonHeaderData(List<Header> headers) {
            this.headers = headers;
        }
    }

}
