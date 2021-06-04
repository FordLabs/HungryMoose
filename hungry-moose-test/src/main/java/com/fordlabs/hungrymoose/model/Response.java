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

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Getter
public class Response {

    private final HttpStatus statusCode;
    private final HttpHeaders headers;
    private final String body;

    public Response(String textRepresentation) {
        try(Scanner scanner = new Scanner(textRepresentation)) {
            this.statusCode = readStatusLine(scanner.nextLine());
            this.headers = parseHeaders(scanner);
            this.body = parseBody(scanner);
        }
    }

    private static HttpStatus readStatusLine(final String responseLine) {
        String[] splitResponseLine = responseLine.stripTrailing().split(" ", 2);
        HttpStatus responseCode = validateResponseCode(splitResponseLine[0]);
        String reasonPhrase = validateReasonPhrase(splitResponseLine[1]);
        checkCodeAndPhraseCompatibility(responseCode, reasonPhrase);
        return responseCode;
    }

    private static HttpStatus validateResponseCode(String responseCodeString) {
        try {
            return HttpStatus.valueOf(Integer.parseInt(responseCodeString));
        } catch (Exception e) {
            throw new InvalidResponseException(String.format("'%s' is not a valid Status Code", responseCodeString));
        }
    }

    private static String validateReasonPhrase(final String reasonPhraseString) {
        try {
            String enumeratedReasonPhrase = reasonPhraseString.replaceAll(" ", "_").toUpperCase();
            HttpStatus.valueOf(enumeratedReasonPhrase);
            return reasonPhraseString;
        } catch (Exception e) {
            throw new InvalidResponseException(String.format("'%s' is not a valid Reason Phrase", reasonPhraseString));
        }
    }

    private static void checkCodeAndPhraseCompatibility(HttpStatus statusCode, String reasonPhrase) {
        if (!statusCode.getReasonPhrase().equals(reasonPhrase)) {
            throw new InvalidResponseException("Status Code and Reason Phrase do not match");
        }
    }

    private static HttpHeaders parseHeaders(Scanner scanner) {
        HttpHeaders headers = new HttpHeaders();

        while(scanner.hasNextLine()) {
            String headerLine = scanner.nextLine();
            if(headerLine.isBlank()) {
                break;
            }
            else {
                Header header = parseHeader(headerLine);
                headers.add(header.getName(), header.getValue());
            }
        }

        return headers;
    }

    private static Header parseHeader(String headerLine) {
        try {
            String[] headerParts = headerLine.split(":");
            return new Header(headerParts[0].trim(), headerParts[1].trim());
        } catch (Exception e) {
            throw new InvalidResponseException("Cannot parse header: " + headerLine);
        }
    }

    private static String parseBody(final Scanner scanner) {
        StringBuilder bodyBuilder = new StringBuilder();
        while(scanner.hasNextLine()) {
            bodyBuilder.append(scanner.nextLine()).append("\n");
        }

        return bodyBuilder.toString().trim();
    }
}
