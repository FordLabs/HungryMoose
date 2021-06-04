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

import com.fordlabs.hungrymoose.model.Request;
import com.fordlabs.hungrymoose.testing.UriUnderTest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

public abstract class HttpRequestBuilder {

    protected abstract HttpRequestBase createRequest(final URI uri, final String body) throws Exception;

    public HttpRequestBase buildHttpRequest(final UriUnderTest serverUnderTest, final Request requestToTest) throws Exception {
        final URI uri = buildURI(serverUnderTest, requestToTest);
        final HttpRequestBase httpRequest = createRequest(uri, requestToTest.getBody());
        setHeaders(httpRequest, requestToTest.getHeaders());
        return httpRequest;
    }

    private URI buildURI(final UriUnderTest serverUnderTest, final Request requestToTest) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme(serverUnderTest.getTransferProtocol())
                .host(serverUnderTest.getHost())
                .port(serverUnderTest.getPort())
                .path(requestToTest.getRequestLine().getUri().getPath());

        addQueryParams(uriBuilder, requestToTest.getQueryParams());
        return uriBuilder.build().toUri();
    }

    private void addQueryParams(final UriComponentsBuilder uriBuilder, final List<NameValuePair> queryParams) {
        for (final NameValuePair queryParam : queryParams) {
            uriBuilder.queryParam(queryParam.getName(), queryParam.getValue());
        }
    }

    private void setHeaders(final HttpRequestBase httpRequest, final HttpHeaders headers) {
        for (var entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                httpRequest.setHeader(entry.getKey(), value);
            }
        }
    }
}
