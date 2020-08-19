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

package com.fordlabs.hungrymoose.testing;

import com.fordlabs.hungrymoose.validator.body.json.JsonComparison;
import com.fordlabs.hungrymoose.validator.body.json.JsonComparison.ComparisonType;
import com.fordlabs.hungrymoose.validator.body.json.JsonResponseValidator;
import org.junit.Test;

public class JsonResponseValidatorTest {

    private static final String VALID_JSON = "{'data':'valid'}";
    private final JsonResponseValidator jsonResponseValidator = new JsonResponseValidator();

    @Test(expected = AssertionError.class)
    public void actualAndExpectedDoNotMatch() throws Exception {
        this.jsonResponseValidator.validateBody("{'data':'yes'}", "{'data':'no'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void actualAndExpectedMatch() throws Exception {
        this.jsonResponseValidator.validateBody(VALID_JSON, VALID_JSON, NoAnnotationsTestCase.class);
    }

    @Test
    public void matchForSimilarJson() throws Exception {
        this.jsonResponseValidator.validateBody("{'data'  :'value'}", "{'data':'value'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void shouldIgnoreWildcard() throws Exception {
        this.jsonResponseValidator.validateBody("{'data'  :'value'}", "{'data':'...'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void shouldIgnoreWildcardsAtAnyNestingLevel() throws Exception {
        final String expected = ""
                + "{                         "
                + "    'data': {             "
                + "        'info': {         "
                + "            'time': '...' "
                + "        }                 "
                + "    },                    "
                + "    'stuff': {            "
                + "        'thing1': 'value',"
                + "        'thing2': '...'   "
                + "    }                     "
                + "}                         ";

        final String actual = ""
                + "{                         "
                + "    'data': {             "
                + "        'info': {         "
                + "            'time': '123' "
                + "        }                 "
                + "    },                    "
                + "    'stuff': {            "
                + "        'thing1': 'value',"
                + "        'thing2': 'ABC'   "
                + "    }                     "
                + "}                         ";

        this.jsonResponseValidator.validateBody(actual, expected, NoAnnotationsTestCase.class);
    }

    @Test
    public void matchForSimilarJsonWithDifferentQuoteCharacters() throws Exception {
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value\"}", "{'data':'value'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void matchForValidJson() throws Exception {
        this.jsonResponseValidator.validateBody("{'data':'fire the \"laser\"'}", "{'data':'fire the \"laser\"'}", NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void usesStrictJsonComparisonWhenTestClassSpecifiesNothing() throws Exception {
        this.jsonResponseValidator
        .validateBody("{\"data\"  : [\"value1\", \"value2\"]}", "{\"data\"  : [\"value2\", \"value1\"]}", NoAnnotationsTestCase.class);
    }

    @Test
    public void allowsDifferencesInJsonOrderingWhenLooseJsonComparisonIsSpecified() throws Exception {
        this.jsonResponseValidator
        .validateBody("{\"data\"  : [\"value1\", \"value2\"]}", "{\"data\"  : [\"value2\", \"value1\"]}", LooseAnnotationTestCase.class);
    }

    @Test
    public void matchesJsonActualStringWithCrossPlatformCRLF() {
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value\\\\r\\\\nfoo\"}", "{'data':'value\\\\nfoo'}", NoAnnotationsTestCase.class);
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value\\r\\nfoo\"}", "{'data':'value\\nfoo'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void matchesJsonExpectedStringWithCrossPlatformCRLF() {
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value\\\\r\\\\nfoo\"}", "{'data':'value\\\\nfoo'}", NoAnnotationsTestCase.class);
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value\\r\\nfoo\"}", "{'data':'value\\nfoo'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void matchesJsonWithExtraSpaces() {
        this.jsonResponseValidator.validateBody("{\"data\"  :\"value  \\\\r\\\\nfoo\"}", "{'data':'value \\\\nfoo'}", NoAnnotationsTestCase.class);
        this.jsonResponseValidator.validateBody("{\"data \"  :\"value\\r\\nfoo\"}", "{'data':'value\\n foo'}", NoAnnotationsTestCase.class);
    }

    @Test
    public void matchesJsonWithExtraTabs() {
        this.jsonResponseValidator.validateBody("{\"data\" \t :\"value  \\\\r\\\\nfoo\"}", "{'data':'value\t\\\\nfoo'}", NoAnnotationsTestCase.class);
        this.jsonResponseValidator.validateBody("{\"data \"  :\"value\\r\\nfoo\"}", "{'data':'value\\n\tfoo'}", NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailForPartialMatch() throws Exception {
        this.jsonResponseValidator.validateBody("{'data':'one', 'anotherdata': 'two'}", "{'data':'one'}", NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void actualIsNotValidJson() throws Exception {
        this.jsonResponseValidator.validateBody("{'data';;;;;'value'}", VALID_JSON, NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void expectedIsNotValidJson() throws Exception {
        this.jsonResponseValidator.validateBody(VALID_JSON, "where are my brackets", NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchWhenEmpty() throws Exception {
        this.jsonResponseValidator.validateBody("", "", NoAnnotationsTestCase.class);
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchWhenNull() throws Exception {
        this.jsonResponseValidator.validateBody(null, null, NoAnnotationsTestCase.class);
    }

    @Test
    public void shouldMatchWhenBodyContainsOnlyElipses() throws Exception {
        this.jsonResponseValidator.validateBody("{asdasdwqd}", "{...}", NoAnnotationsTestCase.class);
    }

    private class NoAnnotationsTestCase {
        // Used to show that unannotated classes still use strict ordering
    }

    @JsonComparison(value = ComparisonType.LOOSE)
    private class LooseAnnotationTestCase {
        // Used to show that annotating a class with loose json comparison doesn't care about ordering
    }
}
