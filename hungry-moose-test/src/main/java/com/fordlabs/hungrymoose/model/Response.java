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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fordlabs.hungrymoose.parser.BodyParser;
import com.fordlabs.hungrymoose.parser.HeaderParser;
import com.fordlabs.hungrymoose.parser.StatusLineParser;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Scanner;

@Getter
public class Response {

    private final HttpStatus statusCode;
    private final HttpHeaders headers;
    private final String body;

    @JsonCreator
    public static Response from(final String textRepresentation) {
        try(Scanner scanner = new Scanner(textRepresentation)) {
            var statusCode = StatusLineParser.parse(scanner.nextLine());
            var headers = HeaderParser.parse(scanner);
            var body = BodyParser.parse(scanner);
            return new Response(statusCode, headers, body);
        }
    }

    private Response(HttpStatus statusCode, HttpHeaders headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }
}
