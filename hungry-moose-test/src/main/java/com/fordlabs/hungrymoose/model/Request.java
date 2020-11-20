/*
 * Copyright (c) 2020 Ford Motor Company
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

package com.fordlabs.hungrymoose.model;

import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Getter
public class Request {

    private final String textRepresentation;
    private final HttpMethod method;
    private final URI uri;
    private final List<NameValuePair> queryParams;
    private final List<Header> headers;
    private final String body;

    public Request(final String textRepresentation) {
        try(Scanner scanner = new Scanner(textRepresentation)) {
            String requestLine = scanner.nextLine();
            String[] splitRequestLine = requestLine.split(" ");
            if(splitRequestLine.length != 2) {
                throw new InvalidRequestException("Request line has too many values. Should contain only the HTTP Method and request URI");
            }
            this.textRepresentation = textRepresentation;
            this.method = parseHttpMethod(splitRequestLine[0]);
            this.uri = parseUri(splitRequestLine[1]);
            this.queryParams = URLEncodedUtils.parse(this.uri, Charset.defaultCharset());
            this.headers = parseHeaders(scanner);
            this.body = parseBody(scanner);
        }
    }

    private static HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch(IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("'%s' is not a valid HTTP method", method));
        }
    }

    private static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new InvalidRequestException("URL has an invalid format");
        }
    }

    private static List<Header> parseHeaders(Scanner requestScanner) {
        boolean parsingHeaders = true;
        List<Header> headers = new ArrayList<>();
        while(parsingHeaders) {
            if(requestScanner.hasNextLine()) {
                String headerLine = requestScanner.nextLine();
                if(headerLine.isBlank()) {
                    parsingHeaders = false;
                }
                else {
                    headers.add(parseHeader(headerLine));
                }
            } else {
                parsingHeaders = false;
            }
        }

        return headers;
    }

    private static Header parseHeader(String headerLine) {
        try {
            String[] headerParts = headerLine.split(":");
            return new Header(headerParts[0].trim(), headerParts[1].trim());
        } catch (Exception e) {
            throw new InvalidRequestException("Cannot parse header: " + headerLine);
        }
    }

    private static String parseBody(Scanner requestScanner) {
        boolean parsingBody = true;
        StringBuilder bodyBuilder = new StringBuilder();
        while(parsingBody) {
            if (requestScanner.hasNextLine()) {
                bodyBuilder.append(requestScanner.nextLine()).append("\n");
            } else {
                parsingBody = false;
            }
        }

        return bodyBuilder.toString().trim();
    }
}
