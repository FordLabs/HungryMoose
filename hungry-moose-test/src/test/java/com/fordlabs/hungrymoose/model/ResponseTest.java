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
        final String responseLine = "400 Bad Request      \n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;
        Response actualResponse = new Response(responseLine);

        assertThat(actualResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void parse_withInvalidStatusCode_ThrowsInvalidResponseException() {
        final String responseLine = "9800 Ok\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("'9800' is not a valid Status Code");

        new Response(responseLine);
    }

    @Test
    public void parse_withInvalidReasonPhrase_ThrowsInvalidResponseException() {
        final String responseLine = "200 something dumb\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("'something dumb' is not a valid Reason Phrase");

        new Response(responseLine);
    }

    @Test
    public void parse_withCodeAndPhraseNotMatching_ThrowsInvalidResponseException() {
        final String responseLine = "200 Bad Request\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY;

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("Status Code and Reason Phrase do not match");

        new Response(responseLine);
    }

    @Test
    public void canParseContentType() {
        final Response response = new Response(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    public void canParseJsonBody() {
        final Response response = new Response(HEADER + "\n" + CONTENT_TYPE_JSON + "\n\n" + JSON_BODY);
        assertThat(response.getBody()).isEqualTo(JSON_BODY);
    }

    @Test
    public void canParseResponseWhereNoBodyIsExpected() {
        final Response response = new Response("200 OK" + "\n");

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getHeaders().getContentType()).isNull();
    }

    @Test
    public void canParseHeaders() {
        final String string = "200 OK" + "\n" +
                "Some-Header: Expected Value" + "\n" +
                "Another-Thing: 1st-value" + "\n" +
                "Another-Thing: 2nd-value with space  " + "\n" +
                "Content-Type: text/plain" + "\n" +
                "\n" +
                "Hi." + "\n";
        final Response response = new Response(string);

        assertThat(response.getHeaders().get("Some-Header")).containsExactly("Expected Value");
        assertThat(response.getHeaders().get("Another-Thing")).containsExactly("1st-value", "2nd-value with space");
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
    }

    @Test
    public void canParseBodyEvenIfMultipleHeadersArePresent() {
        final String string = "200 OK" + "\n" +
                "Content-Type: text/plain" + "\n" +
                "Another-Thing: 1st-value" + "\n" +
                "\n" +
                "Hello everybody.";
        final Response response = new Response(string);

        assertThat(response.getBody()).isEqualTo("Hello everybody.");
    }

    @Test
    public void surroundingNewlinesAreNotConsideredPartOfTheBody() {
        final String string = "200 OK" + "\n" +
                "\n" +
                "\n" +
                "The end of the line." +
                "\n";
        final Response response = new Response(string);

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
        final Response response = new Response(string);

        assertThat(response.getBody()).isEqualTo("It was the best of times...\n\n...it was the worst of times");
    }

    @Test
    public void throwsAnErrorIfHeadersAreMalformed() {
        final String string = "200 OK" + "\n" +
                "Content-Type: application/json" + "\n" +
                "bogus-bogus" + "\n" +
                "\n" +
                "Body goes here" +
                "\n";

        expectedException.expect(InvalidResponseException.class);
        expectedException.expectMessage("Invalid HTTP header: \"bogus-bogus\". " + "Expected a key:value pair");

        new Response(string);
    }
}
