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
import org.apache.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Objects;

public class ContentTypeValidator {

    private static final String EXPECTED_CONTENT_TYPE_MISSING = "Content-Type missing from expected response. Content-Type is required.";
    private static final String ACTUAL_CONTENT_TYPE_MISSING = "Content-Type missing from actual response.";
    private static final String MISMATCHED_CONTENT_TYPE = "Content-Type on actual not matching expected.%n Wanted: %s%n but found: %s";

    public static String validate(Response expectedResponse, HttpResponse actualResponse) {
        MediaType contentType = expectedResponse.getHeaders().getContentType();
        if ((!expectedResponse.getBody().isEmpty() && expectedResponse.getHeaders().getContentType() == null)) throw new AssertionError(EXPECTED_CONTENT_TYPE_MISSING);
        if(actualResponse.getEntity().getContentType() == null && expectedResponse.getHeaders().getContentType() != null) throw new AssertionError(ACTUAL_CONTENT_TYPE_MISSING);
        if (Objects.isNull(contentType)) return "";

        String expectedContentType = contentType.toString();
        String actualContentType = actualResponse.getEntity().getContentType().getValue();
        if(!actualContentType.equals(expectedContentType)) throw new AssertionError(String.format(MISMATCHED_CONTENT_TYPE, expectedContentType, actualContentType));
        return expectedContentType;
    }

}
