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

import com.fordlabs.hungrymoose.parser.BodyParser;
import com.fordlabs.hungrymoose.parser.HeaderParser;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

@Getter
public class Request {

    private final RequestLine requestLine;
    private final HttpHeaders headers;
    private final String body;

    public Request(final String textRepresentation) {
        try(Scanner scanner = new Scanner(textRepresentation)) {
            String requestLine = scanner.nextLine();
            String[] splitRequestLine = requestLine.split(" ");
            if(splitRequestLine.length != 2) {
                throw new InvalidRequestException("Request line has too many values. Should contain only the HTTP Method and request URI");
            }
            this.requestLine = new RequestLine(parseHttpMethod(splitRequestLine[0]), parseUri(splitRequestLine[1]));
            this.headers = HeaderParser.parse(scanner);
            this.body = BodyParser.parse(scanner);
        }
    }

    public List<NameValuePair> getQueryParams(){
        return URLEncodedUtils.parse(this.requestLine.getUri(), Charset.defaultCharset());
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
