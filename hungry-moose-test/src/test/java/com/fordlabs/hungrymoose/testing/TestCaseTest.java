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

package com.fordlabs.hungrymoose.testing;

import com.fordlabs.hungrymoose.model.Response;
import com.fordlabs.hungrymoose.validator.body.json.JsonResponseValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;

public class TestCaseTest {

    private static final String JSON = "json";
    private static final String STRING_COMPARE = "STRING_COMPARE";
    private static final String ACTUAL_BODY = "actualBody";
    private static final String EXPECTED_BODY = "expectedBody";

    private HttpResponse response;

    private final JsonResponseValidator jsonResponseValidator = mock(JsonResponseValidator.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        this.response.setEntity(new StringEntity(ACTUAL_BODY));
    }

    @Test
    public void assertBodyDelegatesToJsonValidatorForValidations() {
        Response expectedResponse = buildResponse("Content-Type: application/json", EXPECTED_BODY);
        TestCase testCase = new TestCase(null, expectedResponse, HungryMooseTestRunner.class);

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Unparsable JSON string: expectedBody");

        testCase.assertResponseBody(ACTUAL_BODY, JSON);
    }

    @Test
    public void continuesOnIfThereIsNoExpectedBody() {
        String expectedBody = "";

        Response expectedResponse = new Response("200 OK\n" + "Content-Type: application/json" + "\n\n" + expectedBody);
        TestCase testCase = new TestCase(null, expectedResponse, null);

        testCase.assertResponseBody(ACTUAL_BODY, JSON);

        verify(this.jsonResponseValidator, times(0)).validateBody(ACTUAL_BODY, expectedBody, null);
    }

    @Test
    public void checksForAGenericBodyByStringComparison() {
        Response expectedResponse = buildResponse("Content-Type: " + MediaType.valueOf("something/fake"), EXPECTED_BODY);
        TestCase testCase = new TestCase(null, expectedResponse, null);

        testCase.assertResponseBody(EXPECTED_BODY, STRING_COMPARE);
    }

    @Test(expected = AssertionError.class)
    public void failsWhenGenericBodyFailsStringComparison() {
        Response expectedResponse = buildResponse("Content-Type: application/json", EXPECTED_BODY);
        TestCase testCase = new TestCase(null, expectedResponse, null);

        testCase.assertResponseBody("Something else", STRING_COMPARE);
    }

    @Test(expected = AssertionError.class)
    public void assertStatusCodeWhenNoBody() throws Exception {
        Response expectedResponse = buildResponse("Content-Type: application/json", EXPECTED_BODY);
        TestCase testCase = new TestCase(null, expectedResponse, null);

        HttpResponse httpResponse = makeResponseEntityWithNullContentType();
        when(httpResponse.getStatusLine()).thenReturn(makeStatusLineWith500Code());
        testCase.verifyResponse(httpResponse);
    }

    @Test
    public void throwsMissingContentTypeErrorWhenExpectedBodyIsNotEmptyAndExpectedResponseHasNoContentType() throws Exception {
        Response expectedResponse = buildResponse("", EXPECTED_BODY);
        TestCase testCase = new TestCase(null, expectedResponse, null);

        this.expectedException.expect(AssertionError.class);
        this.expectedException.expectMessage("Content-Type missing from expected response. Content-Type is required.");

        HttpResponse httpResponse = mock(HttpResponse.class);
        testCase.verifyResponse(httpResponse);
    }

    @Test
    public void doesNotThrowMissingContentTypeErrorWhenExpectedBodyIsEmptyAndExpectedResponseHasNoContentType() throws Exception {
        Response expectedResponse = buildResponse("", "");
        TestCase testCase = new TestCase(null, expectedResponse, null);

        testCase.verifyResponse(this.response);
    }

    private HttpResponse makeResponseEntityWithNullContentType() {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final HttpEntity entity = Mockito.mock(HttpEntity.class);
        when(entity.getContentType()).thenReturn(null);
        when(httpResponse.getEntity()).thenReturn(entity);
        return httpResponse;
    }

    private BasicStatusLine makeStatusLineWith500Code() {
        return new BasicStatusLine(new ProtocolVersion("", 0, 0), 500, "");
    }

    private static Response buildResponse(String mediaType, String body) {
        return new Response("200 OK\n" + mediaType + "\n\n" + body);
    }
}
