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

package com.fordlabs.hungrymoose.validator.body.json;

import com.fordlabs.hungrymoose.validator.body.BodyValidator;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.springframework.core.annotation.AnnotationUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonResponseValidator implements BodyValidator {

    @Override
    public void validateBody(final String actual, final String expected, Class<?> testClass) {
        if (isBlank(actual) || isBlank(expected)) {
            throw new AssertionError("Json body required");
        }
        if (shouldIgnoreBody(expected)) {
            return;
        }

        final DefaultComparator comparator = new DefaultComparator(determineComparisonMode(testClass)) {

            @Override
            public void compareValues(final String prefix, final Object expectedValue, final Object actualValue, final JSONCompareResult result)
                    throws JSONException {
                if (expectedValue.equals("...")) {
                    result.passed();
                } else {
                    super.compareValues(prefix, expectedValue, actualValue, result);
                }
            }
        };

        try {
            JSONAssert.assertEquals(removeLineEndingsAndWhitespace(expected), removeLineEndingsAndWhitespace(actual), comparator);
        } catch (final JSONException e) {
            throw new AssertionError(buildMessage(actual, expected, e));
        }
    }

    private JSONCompareMode determineComparisonMode(Class<?> testClass) {
        JsonComparison jsonComparison = AnnotationUtils.findAnnotation(testClass, JsonComparison.class);
        if (jsonComparison == null) {
            return JSONCompareMode.STRICT;
        }
        return jsonComparison.value().getMode();
    }

    private boolean shouldIgnoreBody(final String expected) {
        return "{...}".equals(expected);
    }

    private String removeLineEndingsAndWhitespace(final String source) {
        return source.
                replace("\\\\r\\\\n", ""). //
                replace("\\r\\n", ""). //
                replace("\r\n", ""). //
                replace("\\\\n", ""). //
                replace("\\n", ""). //
                replace("\n", ""). //
                replace("\t", ""). //
                replace(" ", "");
    }

    private StringBuilder buildMessage(final String actualBody, final String expectedBody, final Exception e) {
        final StringBuilder exceptionMessage = new StringBuilder().append("\n");
        exceptionMessage.append("Incorrect JSON result").append("\n");
        exceptionMessage.append("expected json: " + expectedBody).append("\n");
        exceptionMessage.append("actual json: " + actualBody).append("\n");
        exceptionMessage.append("exception: " + e.getMessage());
        return exceptionMessage;
    }

}
