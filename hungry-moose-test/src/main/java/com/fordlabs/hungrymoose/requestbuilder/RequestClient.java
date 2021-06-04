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

package com.fordlabs.hungrymoose.requestbuilder;

import com.fordlabs.hungrymoose.model.HttpMethod;
import com.fordlabs.hungrymoose.model.Request;
import com.fordlabs.hungrymoose.testing.UriUnderTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.fordlabs.hungrymoose.model.HttpMethod.*;

public class RequestClient {

    private static final Map<HttpMethod, HttpRequestBuilder> httpRequestBuilders = new HashMap<>();

    static {
        httpRequestBuilders.put(GET, new GetHttpRequestBuilder());
        httpRequestBuilders.put(POST, new PostHttpRequestBuilder());
        httpRequestBuilders.put(PATCH, new PatchHttpRequestBuilder());
        httpRequestBuilders.put(PUT, new PutHttpRequestBuilder());
        httpRequestBuilders.put(DELETE, new DeleteHttpRequestBuilder());
    }

    public static HttpResponse getResponse(final UriUnderTest serverUnderTest, final Request request) throws Exception {
        final HttpRequestBase httpRequest = RequestClient.createRequest(serverUnderTest, request);
        return submitRequest(httpRequest);
    }

    private static HttpRequestBase createRequest(final UriUnderTest serverUnderTest, final Request request) throws Exception {
        final HttpRequestBuilder httpRequestBuilder = httpRequestBuilders.get(request.getRequestLine().getMethod());
        if (httpRequestBuilder == null) {
            throw new RuntimeException("Unsupported HTTP request method: " + request.getRequestLine().getMethod());
        }
        return httpRequestBuilder.buildHttpRequest(serverUnderTest, request);
    }

    private static HttpResponse submitRequest(final HttpRequestBase request) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(request);
    }
}
