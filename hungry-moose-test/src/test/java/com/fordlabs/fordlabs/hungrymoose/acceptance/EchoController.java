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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Controller
@RequestMapping("/echo")
public class EchoController {

    @RequestMapping(value = "/**", produces = "application/json")
    public ResponseEntity<String> getRequest(final HttpServletRequest rawRequest) throws Exception {
        RequestDetails details = new RequestDetails(
                rawRequest.getRequestURI(),
                rawRequest.getMethod(),
                getHeadersFromRequest(rawRequest),
                IOUtils.toString(rawRequest.getReader())
        );

        String convertToJSON = convertToJSON(details);
        return new ResponseEntity<>(convertToJSON, HttpStatus.OK);
    }

    private HttpHeaders getHeadersFromRequest(final HttpServletRequest rawRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> headerNames = rawRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (RapidStubController.HEADERS_TO_IGNORE.contains(name)) {
                continue;
            }
            Enumeration<String> values = rawRequest.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                httpHeaders.add(name, value);
            }
        }
        return httpHeaders;
    }

    private String convertToJSON(Object object) throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
