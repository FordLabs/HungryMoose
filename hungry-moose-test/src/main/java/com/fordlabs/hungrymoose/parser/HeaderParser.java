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

import com.fordlabs.hungrymoose.model.Header;
import com.fordlabs.hungrymoose.model.InvalidHeaderException;
import org.springframework.http.HttpHeaders;

import java.util.Scanner;

public class HeaderParser {
    public static HttpHeaders parse(final Scanner scanner) {
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
            throw new InvalidHeaderException("Cannot parse header: " + headerLine);
        }
    }

}
