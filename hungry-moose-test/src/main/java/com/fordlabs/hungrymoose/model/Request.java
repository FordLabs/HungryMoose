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

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Request {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final List<HttpMethod> HTTP_METHODS_WITHOUT_BODY = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE);
    private final String textRepresentation;
    private final HttpMethod method;
    private final URI uri;
    private final List<NameValuePair> queryParams;
    private final String body;
    private final String contentType;
    private final List<Header> headers;

    public Request(final String textRepresentation) {
        this.textRepresentation = textRepresentation;
        this.method = readHttpMethod(textRepresentation);
        this.uri = readURI(textRepresentation);
        this.headers = readHeaders(textRepresentation);

        this.queryParams = URLEncodedUtils.parse(this.uri, "UTF-8");

        if (requestBodyIsRequired()) {
            this.contentType = readContentType();
            this.body = readBody(textRepresentation);
        } else {
            this.contentType = "";
            this.body = "";
        }
    }

    private boolean requestBodyIsRequired() {
        return !HTTP_METHODS_WITHOUT_BODY.contains(this.method);
    }

    private List<Header> readHeaders(final String textRepresentation) {
        final Scanner scanner = moveScannerToString(textRepresentation, "HTTP/1.1");
        scanner.useDelimiter(Pattern.compile("\\n"));
        final List<Header> headers = Lists.newArrayList();
        loadHeader(headers, scanner);
        scanner.close();
        return headers;
    }

    private void loadHeader(final List<Header> headers, final Scanner scanner) {
        if (!scanner.hasNext()) {
            return;
        }
        final String line = scanner.next();
        if (StringUtils.isBlank(line)) {
            return;
        }

        String name = StringUtils.substringBefore(line, ":").trim();
        String value = StringUtils.substringAfter(line, ":").trim();

        headers.add(new Header(name, value));
        loadHeader(headers, scanner);
    }

    public List<NameValuePair> getQueryParams() {
        return this.queryParams;
    }

    public String getTextRepresentation() {
        return this.textRepresentation;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public URI getURI() {
        return this.uri;
    }

    public String getBody() {
        return this.body;
    }

    public String getContentType() {
        return this.contentType;
    }

    private String readContentType() {
        for (final Header header : this.headers) {
            if (CONTENT_TYPE.equals(header.getName())) {
                return header.getValue();
            }
        }
        throw new RuntimeException("Missing Content-Type");
    }

    private HttpMethod readHttpMethod(final String requestText) {
        final Scanner scanner = new Scanner(requestText);
        final String name = scanner.next();
        scanner.close();
        try {
            return HttpMethod.valueOf(name);
        } catch (final Exception e) {
            throw new RuntimeException("Invalid HttpMethod: " + name, e);
        }
    }

    private URI readURI(final String requestText) {
        final Scanner scanner = skipToURI(requestText);
        final String uriString = scanner.next();
        scanner.close();
        try {
            return new URI(uriString);
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI: " + uriString, e);
        }
    }

    private String readBody(final String requestText) {
        final Scanner scanner = skipToBody(requestText);

        final StringBuilder bodyBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            bodyBuilder.append(scanner.nextLine());
        }
        scanner.close();
        return bodyBuilder.toString();
    }

    private Scanner skipToBody(final String requestText) {
        final Scanner scanner = new Scanner(requestText);
        final Pattern defaultDelimiter = scanner.delimiter();
        scanner.useDelimiter(Pattern.compile("\\n\\n"));
        scanner.next();
        scanner.useDelimiter(defaultDelimiter);
        return scanner;
    }

    private Scanner moveScannerToString(final String requestString, final String matcher) {
        final Scanner scanner = new Scanner(requestString);
        while (scanner.hasNext()) {
            final String nextString = scanner.next();
            if (nextString.contains(matcher)) {
                return scanner;
            }
        }
        throw new RuntimeException("Missing " + matcher);
    }

    private Scanner skipToURI(final String requestText) {
        final Scanner scanner = new Scanner(requestText);
        scanner.next();
        return scanner;
    }

    public List<Header> getHeaders() {
        return this.headers;
    }

}
