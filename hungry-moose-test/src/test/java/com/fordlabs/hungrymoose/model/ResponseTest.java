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

package com.fordlabs.hungrymoose.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

public class ResponseTest {

    private static final String HEADER = "400 Bad Request";
    private static final String CONTENT_TYPE_JSON = "Content-Type: application/json";
    private static final String JSON_BODY = "{\"error\": \"Unknown catalogId\"}";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parse_withValidResponseLine_ReturnsRequestWithStatusCode() {
        final String responseLine = "400 Bad Request\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;
        Response actualResponse = Response.from(responseLine);

        assertThat(actualResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void parse_withPhraseWithTrailingSpaces_StillResolves() {
        final String responseLine = "400 Bad Request      \n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;
        Response actualResponse = Response.from(responseLine);

        assertThat(actualResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void parse_withInvalidStatusCode_ThrowsInvalidResponseException() {
        final String responseLine = "9800 Ok\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("'9800' is not a valid Status Code");

        Response.from(responseLine);
    }

    @Test
    public void parse_withInvalidReasonPhrase_ThrowsInvalidResponseException() {
        final String responseLine = "200 something dumb\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("'something dumb' is not a valid Reason Phrase");

        Response.from(responseLine);
    }

    @Test
    public void parse_withCodeAndPhraseNotMatching_ThrowsInvalidResponseException() {
        final String responseLine = "200 Bad Request\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("Status Code and Reason Phrase do not match");

        Response.from(responseLine);
    }

    @Test
    public void parse_withHeaders_CanReturnHeaders() {
        final String string = "200 OK" + "\n" +
                "Some-Header: Expected Value" + "\n" +
                "Another-Thing: 1st-value" + "\n" +
                "Another-Thing: 2nd-value with space  " + "\n" +
                "Content-Type: text/plain" + "\n" +
                "\n" +
                "Hi." + "\n";
        final Response response = Response.from(string);

        assertThat(response.getHeaders().get("Some-Header")).containsExactly("Expected Value");
        assertThat(response.getHeaders().get("Another-Thing")).containsExactly("1st-value", "2nd-value with space");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
    }

    @Test
    public void parse_withHeaders_CanParseContentType() {
        final Response response = Response.from(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    public void parse_withHeaders_ReturnsResponseWithHeaders() {
        String requestWithHeaders = "200 OK\nContent-Type: application/json\nAuthorization:value\n\n";
        Response request = Response.from(requestWithHeaders);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
    }

    @Test
    public void parse_withoutBlankLineAfterHeaders_ReturnsResponseWithHeaders() {
        String requestWithHeaders = "200 OK\nContent-Type: application/json\nAuthorization:value\n";
        Response request = Response.from(requestWithHeaders);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
    }

    @Test
    public void parse_withMalformedHeader_ThrowsInvalidResponseException() {
        String requestWithHeaders = "200 OK\nContent-Type((( application/json\n";
        expectedException.expect(InvalidHeaderException.class);
        expectedException.expectMessage("Cannot parse header: Content-Type((( application/json");

        Response.from(requestWithHeaders);
    }

    @Test
    public void canParseBodyEvenIfMultipleHeadersArePresent() {
        final String string = "200 OK" + "\n" +
                "Content-Type: text/plain" + "\n" +
                "Another-Thing: 1st-value" + "\n" +
                "\n" +
                "Hello everybody.";
        final Response response = Response.from(string);

        assertThat(response.getBody()).isEqualTo("Hello everybody.");
    }

    @Test
    public void surroundingNewlinesAreNotConsideredPartOfTheBody() {
        final String string = "200 OK" + "\n" +
                "\n" +
                "\n" +
                "The end of the line." +
                "\n";
        final Response response = Response.from(string);

        assertThat(response.getBody()).isEqualTo("The end of the line.");
    }

    @Test
    public void bodyIsAllowedToContainMultipleSequentialNewlines() {
        final String string = "200 OK" + "\n" +
                "\n" +
                "\n" +
                "It was the best of times..." +
                "\n" +
                "\n" +
                "...it was the worst of times";
        final Response response = Response.from(string);

        assertThat(response.getBody()).isEqualTo("It was the best of times...\n\n...it was the worst of times");
    }

    @Test
    public void parse_withBodyAndHeaders_ReturnsRequestWithBodyAndHeaders() {
        String requestString = "200 OK\nContent-Type: application/json\nAuthorization:value\n\nBody\nContent\nExists\nHere";
        Response request = Response.from(requestString);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
        assertThat(request.getBody()).isEqualTo("Body\nContent\nExists\nHere");
    }

    @Test
    public void parse_withBody_ReturnsResponseWithBody() {
        final Response response = Response.from(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getBody()).isEqualTo(JSON_BODY);
    }

    @Test
    public void parse_withNoBody_ReturnsResponseWithNoBody() {
        final Response response = Response.from("200 OK" + "\n");

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getHeaders().getContentType()).isNull();
    }
}
