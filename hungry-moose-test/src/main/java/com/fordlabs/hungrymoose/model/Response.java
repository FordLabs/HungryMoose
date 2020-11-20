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

package com.fordlabs.hungrymoose.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.text.MessageFormat;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Response {

    private final int statusCode;
    private final String reasonPhrase;
    private final HttpHeaders headers;
    private final String body;

    private static int readStatusCode(final String responseLine) {
        String responseCode = responseLine.substring(0, responseLine.indexOf(' '));
        try {
            return HttpStatus.valueOf(Integer.parseInt(responseCode)).value();
        } catch (Exception e) {
            throw new InvalidResponseException(String.format("'%s' is not a valid Status Code", responseCode));
        }
    }

    private static String readReasonPhrase(final String responseLine) {
        String reasonPhrase = responseLine.stripTrailing().substring(responseLine.indexOf(' ') + 1);
        try {
            String enumeratedReasonPhrase = reasonPhrase.replaceAll(" ", "_").toUpperCase();
            HttpStatus.valueOf(enumeratedReasonPhrase);
            return reasonPhrase;
        } catch (Exception e) {
            throw new InvalidResponseException(String.format("'%s' is not a valid Reason Phrase", reasonPhrase));
        }
    }

    private void checkCodeAndPhraseCompatibility() {
        if (!HttpStatus.valueOf(this.statusCode).getReasonPhrase().equals(this.reasonPhrase)) {
            throw new InvalidResponseException("Status Code and Reason Phrase do not match");
        }
    }

    private static HttpHeaders readHeaders(final String requestText) {
        final String topSection = splitTopAndBodySections(requestText)[0];
        final Scanner scanner = new Scanner(topSection).useDelimiter("\n");
        skipStatusLine(scanner);

        final HttpHeaders httpHeaders = new HttpHeaders();
        final Pattern headerPattern = Pattern.compile("(.+):(.+)");
        while (scanner.hasNext()) {
            try {
                scanner.next(headerPattern);
            } catch (final InputMismatchException e) {
                final String message = MessageFormat.format("Invalid HTTP header: \"{0}\". Expected a key:value pair", scanner.next());
                throw new InvalidResponseException(message, e);
            }
            final MatchResult match = scanner.match();
            final String key = match.group(1).trim();
            final String value = match.group(2).trim();
            httpHeaders.add(key, value);
        }
        scanner.close();

        return httpHeaders;
    }

    private static String readBody(final String requestText) {
        final String[] sections = splitTopAndBodySections(requestText);
        return sections.length > 1 ? StringUtils.strip(sections[1], "\n") : "";
    }

    private static void skipStatusLine(final Scanner scanner) {
        scanner.nextLine();
    }

    private static String[] splitTopAndBodySections(final String requestText) {
        return requestText.split("\n\n", 2);
    }

    @SuppressWarnings("unused")
    public Response(String textRepresentation) {
        try(Scanner scanner = new Scanner(textRepresentation)) {
            String requestLine = scanner.nextLine();
            this.statusCode = readStatusCode(requestLine);
            this.reasonPhrase = readReasonPhrase(requestLine);
            checkCodeAndPhraseCompatibility();
            this.headers = readHeaders(textRepresentation);
            this.body = readBody(textRepresentation);
        }
    }

    public Response(StatusLine statusLine, HttpHeaders headers, String body) {
        this.statusCode = 0;
        this.reasonPhrase = null;
        this.headers = headers;
        this.body = body;
    }

    public HttpStatus getStatusCode() {
        return HttpStatus.valueOf(this.statusCode);
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public MediaType getContentType() {
        return this.headers.getContentType();
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public String getBody() {
        return this.body;
    }
}
