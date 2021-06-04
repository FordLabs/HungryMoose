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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.fordlabs.hungrymoose.model.HttpMethod.GET;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parse_withValidRequestLine_ReturnsRequestWithUriAndMethod() throws URISyntaxException {
        final String requestLine = "GET /someurl\n";
        Request actualRequest = new Request(requestLine);

        assertThat(actualRequest.getMethod()).isEqualTo(GET);
        assertThat(actualRequest.getUri()).isEqualTo(new URI("/someurl"));
    }

    @Test
    public void parse_withQueryParams_ReturnsRequestWithQueryParams() {
        final String requestLine = "GET /someurl?v1=1&v2=two\n";
        final List<NameValuePair> expectedQueryParameters = asList(
                new BasicNameValuePair("v1", "1"),
                new BasicNameValuePair("v2", "two")
            ) ;

        Request actualRequest = new Request(requestLine);

        assertThat(actualRequest.getQueryParams()).isEqualTo(expectedQueryParameters);
    }

    @Test
    public void parse_withInvalidHttpMethod_ThrowsInvalidRequestException() {
        final String requestLine = "BOGUS /someurl\n";

        expectedException.expect(InvalidRequestException.class);
        expectedException.expectMessage("'BOGUS' is not a valid HTTP method");

        new Request(requestLine);
    }

    @Test
    public void parse_withTooManyValuesInRequestLine_ThrowsInvalidRequestException() {
        final String requestLine = "GET /someurl and more phrases \n";

        expectedException.expect(InvalidRequestException.class);
        expectedException.expectMessage("Request line has too many values. Should contain only the HTTP Method and request URI");

        new Request(requestLine);
    }

    @Test
    public void parse_withInvalidUrl_ThrowsInvalidRequestException() {
        final String requestLine = "GET \\backslash \n";

        expectedException.expect(InvalidRequestException.class);
        expectedException.expectMessage("URL has an invalid format");

        new Request(requestLine);
    }

    @Test
    public void parse_withHeaders_ReturnsRequestWithHeaders() {
        String requestWithHeaders = "GET /someUrl\nContent-Type: application/json\nAuthorization:value\n\n";
        Request request = new Request(requestWithHeaders);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
    }

    @Test
    public void parse_withoutBlankLineAfterHeaders_ReturnsRequestWithHeaders() {
        String requestWithHeaders = "GET /someUrl\nContent-Type: application/json\nAuthorization:value\n";
        Request request = new Request(requestWithHeaders);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
    }

    @Test
    public void parse_withMalformedHeader_ThrowsInvalidRequestException() {
        String requestWithHeaders = "GET /someUrl\nContent-Type((( application/json\n";
        expectedException.expect(InvalidHeaderException.class);
        expectedException.expectMessage("Cannot parse header: Content-Type((( application/json");

        new Request(requestWithHeaders);
    }

    @Test
    public void parse_withBody_ReturnsRequestWithBody() {
        String requestString = "GET /someUrl\n\nBody\nContent\nExists\nHere\n\n";
        Request request = new Request(requestString);
        assertThat(request.getBody()).isEqualTo("Body\nContent\nExists\nHere");
    }

    @Test
    public void parse_withBodyAndHeaders_ReturnsRequestWithBodyAndHeaders() {
        String requestString = "GET /someUrl\nContent-Type: application/json\nAuthorization:value\n\nBody\nContent\nExists\nHere";
        Request request = new Request(requestString);
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().get("Authorization")).containsExactly("value");
        assertThat(request.getBody()).isEqualTo("Body\nContent\nExists\nHere");
    }
}
