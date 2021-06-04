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

package com.fordlabs.hungrymoose.testing;

import com.fordlabs.hungrymoose.model.Request;
import com.fordlabs.hungrymoose.model.Response;
import com.fordlabs.hungrymoose.validator.ContentTypeValidator;
import com.fordlabs.hungrymoose.validator.body.BodyValidator;
import com.fordlabs.hungrymoose.validator.body.json.JsonResponseValidator;
import com.fordlabs.hungrymoose.validator.body.string.StringBodyValidator;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.io.IOException;

import static com.fordlabs.hungrymoose.requestbuilder.RequestClient.getResponse;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static org.hamcrest.Matchers.is;

class TestCase {

    private final Request request;
    private final Response expectedResponse;
    private final BodyValidator jsonResponseValidator = new JsonResponseValidator();
    private final BodyValidator defaultBodyValidator = new StringBodyValidator();
    private final Class<?> testClass;

    public TestCase(final Request request, final Response expectedResponse, final Class<?> testClass) {
        this.request = request;
        this.expectedResponse = expectedResponse;
        this.testClass = testClass;
    }

    public void runTest(final UriUnderTest serverUnderTest) throws Exception {
        verifyResponse(getResponse(serverUnderTest, this.request));
    }

    protected void verifyResponse(final HttpResponse response) throws IOException {
        String contentType = ContentTypeValidator.validate(this.expectedResponse, response);

        final String responseBody = getResponseBody(response);
        assertStatusLine(responseBody, response.getStatusLine().getStatusCode(), is(this.expectedResponse.getStatusCode().value()));

        assertResponseBody(responseBody, contentType);
    }

    private String getResponseBody(final HttpResponse response) throws IOException {
        if (response.getEntity() == null) {
            return "";
        } else {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    private void assertStatusLine(final String reason, final int actual, final Matcher<Integer> matcher) {
        if (!matcher.matches(actual)) {
            final Description description = new StringDescription();
            description
                    .appendText("\nExpected: ")
                    .appendDescriptionOf(matcher)
                    .appendText("\n     but: ");
            matcher.describeMismatch(actual, description);
            description.appendText("\n     " + reason);
            throw new AssertionError(description.toString());
        }
    }

    void assertResponseBody(final String actualResponseBody, final String actualResponseContentType) {
        if (this.expectedResponse.getBody().isEmpty()) return;
        final String expectedResponseBody = this.expectedResponse.getBody();
        getBodyValidator(actualResponseContentType).validateBody(actualResponseBody, expectedResponseBody, this.testClass);
    }

    private BodyValidator getBodyValidator(final String contentType) {
        if (contentType.contains(JSON_UTF_8.subtype())) {
            return this.jsonResponseValidator;
        } else {
            return this.defaultBodyValidator;
        }
    }
}
