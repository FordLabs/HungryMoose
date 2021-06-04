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
import com.fordlabs.hungrymoose.parser.RequestLineParser;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.http.HttpHeaders;

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
            this.requestLine = RequestLineParser.parse(scanner.nextLine());
            this.headers = HeaderParser.parse(scanner);
            this.body = BodyParser.parse(scanner);
        }
    }

    public List<NameValuePair> getQueryParams(){
        return URLEncodedUtils.parse(this.requestLine.getUri(), Charset.defaultCharset());
    }
}
