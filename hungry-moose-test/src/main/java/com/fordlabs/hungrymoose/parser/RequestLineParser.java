/*
 *
 *  * Copyright (c) 2021 Ford Motor Company
 *  * All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.fordlabs.hungrymoose.parser;

import com.fordlabs.hungrymoose.model.HttpMethod;
import com.fordlabs.hungrymoose.model.InvalidRequestException;
import com.fordlabs.hungrymoose.model.RequestLine;

import java.net.URI;
import java.net.URISyntaxException;

public class RequestLineParser {

    public static RequestLine parse(final String requestLine) {
        String[] splitRequestLine = requestLine.split(" ");
        if(splitRequestLine.length != 2) {
            throw new InvalidRequestException("Request line has too many values. Should contain only the HTTP Method and request URI");
        }
        return new RequestLine(parseHttpMethod(splitRequestLine[0]), parseUri(splitRequestLine[1]));
    }

    private static HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch(IllegalArgumentException e) {
            throw new InvalidRequestException(String.format("'%s' is not a valid HTTP method", method));
        }
    }

    private static URI parseUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new InvalidRequestException("URL has an invalid format");
        }
    }

}
