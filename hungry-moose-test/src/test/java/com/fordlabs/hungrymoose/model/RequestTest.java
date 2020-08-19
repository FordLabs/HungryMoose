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

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RequestTest {

    private static final String JSON_BODY = "{\"body\":\"value\"}";
    private static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
    private static final String CONTENT_TYPE_XML = "Content-Type: application/xml";

    @Test
    public void canDetermineTheHttpMethod_GET() throws Exception {
        final Request request = new Request("GET /someurl HTTP/1.1" + "\n" + CONTENT_TYPE_JSON + "\n" + JSON_BODY);
        assertThat(request.getMethod(), is(HttpMethod.GET));
    }

    @Test
    public void canDetermineTheHttpMethod_POST() throws Exception {
        final Request request = new Request("POST /someurl HTTP/1.1" + "\n" + CONTENT_TYPE_JSON + "\n" + JSON_BODY);
        assertThat(request.getMethod(), is(HttpMethod.POST));
    }

    @Test
    public void throwsAnErrorIfHttpMethodIsBogus() throws Exception {
        try {
            new Request("BOGUS /someurl HTTP/1.1");
            fail("why no go boom?");
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is("Invalid HttpMethod: BOGUS"));
        }
    }

    @Test
    public void canRecognizeASimpleURI() throws Exception {
        final Request request = new Request("POST /simpleURI HTTP/1.1" + "\n" + CONTENT_TYPE_XML + "\n" + JSON_BODY);
        assertThat(request.getURI(), is(new URI("/simpleURI")));
    }

    @Test
    public void canRecognizeAFunkyURI() throws Exception {
        final Request request = new Request("GET /some-funky/uri/with/slashes-and-dashes?andQuery=1&strings=2 HTTP/1.1" + "\n" + CONTENT_TYPE_JSON + "\n"
                + JSON_BODY);
        assertThat(request.getURI(), is(new URI("/some-funky/uri/with/slashes-and-dashes?andQuery=1&strings=2")));
    }

    @Test
    public void throwsAnErrorIfTheURIIsBogus() throws Exception {
        try {
            new Request("POST /\\huh??/#% HTTP/1.1");
            fail("why no boom?");
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is("Invalid URI: /\\huh??/#%"));
        }
    }

    @Test
    public void canReadXML() throws Exception {
        final StringBuilder requestHeaderBuilder = buildXmlHeader();
        final StringBuilder requestBody = new StringBuilder();
        requestBody.append("<request></request>");
        requestHeaderBuilder.append(requestBody.toString());

        final Request request = new Request(requestHeaderBuilder.toString());
        assertThat(request.getBody(), is("<request></request>"));

    }

    @Test
    public void canParseBody() throws Exception {
        final StringBuilder requestHeaderBuilder = buildJsonHeader();
        final StringBuilder requestBody = new StringBuilder();
        requestBody.append("Something");
        requestBody.append("OtherLineWith Stuff\n");
        requestBody.append("moreLines of Stuff");
        requestHeaderBuilder.append(requestBody.toString());

        final Request request = new Request(requestHeaderBuilder.toString());
        assertThat(request.getBody(), is(stripWhiteSpace(requestBody)));
    }

    @Test
    public void canParseContentType() throws Exception {
        final StringBuilder requestBuilder = buildJsonHeader();
        requestBuilder.append(JSON_BODY);

        final Request request = new Request(requestBuilder.toString());
        assertThat(request.getContentType(), is("application/json"));
    }

    @Test
    public void canParseHttpHeaders() throws Exception {
        final StringBuilder requestBuilder = buildCustomHeader(
                new Header("header1", "value1"),
                new Header("header2", "value2"));
        requestBuilder.append(JSON_BODY);

        final Request request = new Request(requestBuilder.toString());
        assertThat(request.getContentType(), is("application/json"));
        assertThat(request.getHeaders(), hasItem(new Header("header1", "value1")));
        assertThat(request.getHeaders(), hasItem(new Header("header2", "value2")));
    }

    @Test
    public void canParseHttpHeadersWithGet() throws Exception {
        final StringBuilder requestBuilder = buildCustomHeader("GET",
                new Header("header1", "value1"),
                new Header("header2", "value2"));

        final Request request = new Request(requestBuilder.toString());
        assertThat(request.getContentType(), is(""));
        assertThat(request.getHeaders(), hasItem(new Header("header1", "value1")));
        assertThat(request.getHeaders(), hasItem(new Header("header2", "value2")));
    }

    @Test
    public void canParseHttpHeadersWithTimestamps() throws Exception {
        String headerValueOne = "2015-01-01T05:05:05Z";
        String headerValueTwo = "2050-12-31T23:15:55Z";

        final StringBuilder requestBuilder = buildCustomHeader("GET",
                new Header("timestamp1", headerValueOne),
                new Header("timestamp2", headerValueTwo));

        final Request request = new Request(requestBuilder.toString());
        assertThat(request.getContentType(), is(""));
        assertThat(request.getHeaders(), hasItem(new Header("timestamp1", headerValueOne)));
        assertThat(request.getHeaders(), hasItem(new Header("timestamp2", headerValueTwo)));
    }

    @Test
    public void shouldParseRequestBody() throws Exception {
        final StringBuilder requestBuilder = buildCustomHeader(
                new Header("header1", "value1"),
                new Header("header2", "value2"));
        requestBuilder.append(JSON_BODY);

        final Request request = new Request(requestBuilder.toString());
        assertThat(request.getBody(), is(JSON_BODY));
    }

    @Test
    public void throwsAnErrorIfMissingContentType() throws Exception {
        try {
            new Request("POST /someurl HTTP/1.1" + JSON_BODY);
            fail("why no boom?");
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is("Missing Content-Type"));
        }
    }

    @Test
    public void getRequestThatHasNoBodyAndNoContentType() throws Exception {
        final Request request = new Request("GET /someurl HTTP/1.1");
        assertThat(request.getURI().toString(), is("/someurl"));
        assertThat(request.getBody(), is(""));
        assertThat(request.getMethod(), is(HttpMethod.GET));
    }

    @Test
    public void deleteRequestThatHasNoBodyAndNoContentType() throws Exception {
        final Request request = new Request("DELETE /someurl HTTP/1.1");
        assertThat(request.getURI().toString(), is("/someurl"));
        assertThat(request.getBody(), is(""));
        assertThat(request.getMethod(), is(HttpMethod.DELETE));
    }

    @Test
    public void validateBodyWhenNotHttpGet() throws Exception {
        final Request request = new Request("POST /someurl HTTP/1.1" + "\n" + CONTENT_TYPE_JSON + "\n" + JSON_BODY);
    }

    private String stripWhiteSpace(final StringBuilder requestBody) {
        final String responseBody = requestBody.toString();
        final String strippedResponseBody = responseBody.replace("\n", "");
        return strippedResponseBody.trim();
    }

    private StringBuilder buildJsonHeader() {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("POST /validation/is-compatible HTTP/1.1\n");
        requestBuilder.append("Content-Type: application/json\n");
        requestBuilder.append("\n");
        return requestBuilder;
    }

    private StringBuilder buildCustomHeader(final Header... headers) {
        return buildCustomHeader("POST", headers);
    }

    private StringBuilder buildCustomHeader(final String method, final Header... headers) {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(method.concat(" /validation/is-compatible HTTP/1.1\n"));
        requestBuilder.append("Content-Type: application/json\n");
        for (final Header header : headers) {
            requestBuilder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        requestBuilder.append("\n");
        return requestBuilder;
    }

    private StringBuilder buildXmlHeader() {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("POST /validation/is-compatible HTTP/1.1\n");
        requestBuilder.append("Content-Type: application/xml\n");
        requestBuilder.append("\n");
        return requestBuilder;
    }

}
