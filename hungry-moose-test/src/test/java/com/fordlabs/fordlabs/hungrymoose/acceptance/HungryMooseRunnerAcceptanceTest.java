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

package com.fordlabs.fordlabs.hungrymoose.acceptance;

import com.fordlabs.hungrymoose.testing.ApplicationToTest;
import com.fordlabs.hungrymoose.testing.HungryMooseTestRunner;
import com.fordlabs.hungrymoose.testing.SpecsFromResourcePath;
import com.fordlabs.hungrymoose.testing.TestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@ApplicationToTest(RapidStubApplication.class)
@SpecsFromResourcePath("api/hungrymoose/spec.yaml")
public class HungryMooseRunnerAcceptanceTest {

    @BeforeAll
    static void beforeClass() {
        System.setProperty("target.port", "10897");
        HungryMooseTestRunner.from(HungryMooseRunnerAcceptanceTest.class).runApplication();
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    public void runHungryMooseTests(TestCase testCase) throws Exception {
        testCase.runTest();
    }

    static Stream<TestCase> getTestCases() {
        System.setProperty("target.port", "10897");
        HungryMooseTestRunner testRunner = HungryMooseTestRunner.from(HungryMooseRunnerAcceptanceTest.class);
        return testRunner.getTestCases().stream();
    }
}
