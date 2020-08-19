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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ResponseTest {

    private static final String HEADER = "HTTP/1.1 400 Bad Request";
    private static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
    private static final String CONTENT_TYPE_XML = "Content-Type: application/xml";
    private static final String JSON_BODY = "{\"error\": \"Unknown catalogId\"}";

    @Test
    public void canParseHeader() throws Exception {
        final Response response = Response.parseResponse(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getHeader(), is(HEADER));
    }

    @Test
    public void canParseStatusCodeOK() throws Exception {
        final Response response = Response.parseResponse("HTTP/1.1 200 OK" + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void canParseStatusCodeNotFound() throws Exception {
        final Response response = Response.parseResponse("HTTP/1.1 404 Not Found" + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void canParseContentType() throws Exception {
        final Response response = Response.parseResponse(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON));
    }

    @Test
    public void canParseJsonBody() throws Exception {
        final Response response = Response.parseResponse(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getBody(), is(JSON_BODY));
    }

    @Test
    public void throwsAnErrorIfMissingStatusLine() throws Exception {
        try {
            Response.parseResponse(CONTENT_TYPE_JSON + "\n" + JSON_BODY);
            fail("why no boom?");
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), containsString("Invalid HTTP status line"));
        }
    }

    @Test
    public void canParseXMLBody() throws Exception {
        final String xmlBody = "<xmlRequest>\n<xmlType>value</xmlType>\n</xmlRequest>";
        final Response response = Response.parseResponse(HEADER + "\n" + CONTENT_TYPE_XML + "\n\n" + xmlBody);
        assertThat(response.getBody(), is(xmlBody));
    }

    @Test
    public void canParseXMLBodyWithWhiteSpaces() throws Exception {
        final String xmlBody = "\t     <xmlRequest><xmlType>  this is the value  </xmlType></xmlRequest>";
        final Response response = Response.parseResponse(HEADER + "\n" + CONTENT_TYPE_XML + "\n\n" + xmlBody);
        assertThat(response.getBody(), is(xmlBody));
    }

    @Test
    public void canParseResponseWhereNoBodyIsExpected() throws Exception {
        final Response response = Response.parseResponse("HTTP/1.1 200 OK" + "\n");

        assertThat(response.getBody(), is(""));
        assertThat(response.getContentType(), is(nullValue()));
    }

    @Test
    public void canParseHeaders() throws Exception {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.1 200 OK" + "\n");
        buffer.append("Some-Header: Expected Value" + "\n");
        buffer.append("Another-Thing: 1st-value" + "\n");
        buffer.append("Another-Thing: 2nd-value with space  " + "\n");
        buffer.append("Content-Type: text/plain" + "\n");
        buffer.append("\n");
        buffer.append("Hi." + "\n");
        final String string = buffer.toString();
        final Response response = Response.parseResponse(string);

        assertThat(response.getHeaders(), hasEntry("Some-Header", asList("Expected Value")));
        assertThat(response.getHeaders(), hasEntry("Another-Thing", asList("1st-value", "2nd-value with space")));
        assertThat(response.getHeaders().getContentType(), is(MediaType.TEXT_PLAIN));
    }

    @Test
    public void canParseBodyEvenIfMultipleHeadersArePresent() throws Exception {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.1 200 OK" + "\n");
        buffer.append("Content-Type: text/plain" + "\n");
        buffer.append("Another-Thing: 1st-value" + "\n");
        buffer.append("\n");
        buffer.append("Hello everybody.");
        final String string = buffer.toString();
        final Response response = Response.parseResponse(string);

        assertThat(response.getBody(), is("Hello everybody."));
    }

    @Test
    public void surroundingNewlinesAreNotConsideredPartOfTheBody() throws Exception {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.1 200 OK" + "\n");
        buffer.append("\n");
        buffer.append("\n");
        buffer.append("The end of the line.");
        buffer.append("\n");
        final String string = buffer.toString();
        final Response response = Response.parseResponse(string);

        assertThat(response.getBody(), is("The end of the line."));
    }

    @Test
    public void bodyIsAllowedToContainMultipleSequentialNewlines() throws Exception {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("HTTP/1.1 200 OK" + "\n");
        buffer.append("\n");
        buffer.append("\n");
        buffer.append("It was the best of times...");
        buffer.append("\n");
        buffer.append("\n");
        buffer.append("...it was the worst of times");
        final String string = buffer.toString();
        final Response response = Response.parseResponse(string);

        assertThat(response.getBody(), is("It was the best of times...\n\n...it was the worst of times"));
    }

    @Test
    public void throwsAnErrorIfHeadersAreMalformed() throws Exception {
        try {
            final StringBuffer buffer = new StringBuffer();
            buffer.append("HTTP/1.1 200 OK" + "\n");
            buffer.append("Content-Type: application/json" + "\n");
            buffer.append("bogus-bogus" + "\n");
            buffer.append("\n");
            buffer.append("Body goes here");
            buffer.append("\n");
            final String string = buffer.toString();
            Response.parseResponse(string);

            fail("need that boom BOOM!");
        } catch (final RuntimeException exception) {
            assertThat(exception.getMessage(), is("Invalid HTTP header: \"bogus-bogus\". " + "Expected a key:value pair"));
        }

    }
}
