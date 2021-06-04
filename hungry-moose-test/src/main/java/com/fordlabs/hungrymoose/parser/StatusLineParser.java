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

import com.fordlabs.hungrymoose.model.InvalidResponseException;
import org.springframework.http.HttpStatus;

public class StatusLineParser {

    public static HttpStatus parse(final String responseLine) {
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

}
