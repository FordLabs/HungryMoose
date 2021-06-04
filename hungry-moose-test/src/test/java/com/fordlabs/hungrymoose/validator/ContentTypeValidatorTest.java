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

package com.fordlabs.hungrymoose.validator;

import com.fordlabs.hungrymoose.model.Response;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentTypeValidatorTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateContentType_withMissingContentTypeInExpectedAndBodyNotEmpty_throwsException() {
        Response expectedResponse = createExpectedResponse(null, "notempty");
        HttpResponse actualResponse = createActualResponse("something/fake");

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Content-Type missing from expected response. Content-Type is required.");

        ContentTypeValidator.validate(expectedResponse, actualResponse);
    }

    @Test
    public void validateContentType_withMissingContentTypeAndEmptyBody_shouldIgnoreContentType() {
        Response expectedResponse = createExpectedResponse(null, "");
        HttpResponse actualResponse = createActualResponse("something/fake");

        ContentTypeValidator.validate(expectedResponse, actualResponse);

        assertEquals("", ContentTypeValidator.validate(expectedResponse, actualResponse));
    }

    @Test
    public void validateContentType_withNullActualContentTypeAndExistingExpectedContentType_throwsException() {
        Response expectedResponse = createExpectedResponse("application/json", "");
        HttpResponse actualResponse = createActualResponseWithNullContentType();

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Content-Type missing from actual response.");

        ContentTypeValidator.validate(expectedResponse, actualResponse);
    }

    @Test
    public void validateContentType_withNullActualAndExpectedContentType_shouldIgnoreContentType() {
        Response expectedResponse = createExpectedResponse(null, "");
        HttpResponse actualResponse = createActualResponseWithNullContentType();

        assertEquals("", ContentTypeValidator.validate(expectedResponse, actualResponse));
    }

    @Test
    public void validateContentType_whenContentTypesAreDifferent_throwsException() {
        Response expectedResponse = createExpectedResponse("application/json", "something");
        HttpResponse actualResponse = createActualResponse("text/html");

        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Content-Type on actual not matching expected.\n Wanted: application/json\n but found: text/html");

        ContentTypeValidator.validate(expectedResponse, actualResponse);
    }

    @Test
    public void validateContentType_whenContentTypesAreTheSame_passesWithNoIssue() {
        Response expectedResponse = createExpectedResponse("application/json", "something");
        HttpResponse actualResponse = createActualResponse("application/json");

        ContentTypeValidator.validate(expectedResponse, actualResponse);

        assertEquals("application/json", ContentTypeValidator.validate(expectedResponse, actualResponse));
    }

    private Response createExpectedResponse(String contentType, String bodyContent) {
        String responseLine = "200 OK\n";
        String contentTypeHeader = contentType == null ? "\n\n" : String.format("Content-Type:%s%n%n", contentType);

        return Response.from(responseLine + contentTypeHeader + bodyContent);
    }

    private HttpResponse createActualResponse(String contentType) {
        HttpResponse mockResponse = mock(HttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        Header headerMock = mock(Header.class);

        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContentType()).thenReturn(headerMock);
        when(headerMock.getValue()).thenReturn(contentType);

        return mockResponse;
    }

    private  HttpResponse createActualResponseWithNullContentType() {
        HttpResponse mockResponse = mock(HttpResponse.class);
        HttpEntity mockEntity = mock(HttpEntity.class);

        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContentType()).thenReturn(null);

        return mockResponse;
    }
}