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

package com.fordlabs.hungrymoose.testing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
@AllArgsConstructor
public class HungryMooseTestRunner {

    private final TestContext testContext;
    private final List<TestCase> testCases;

    public static HungryMooseTestRunner from(final Class<?> testClass) {
        TestContext context = TestContextFactory.build(testClass);
        return new HungryMooseTestRunner(context, setupTestCases(testClass, context));
    }

    public void runApplication() {
        this.getTestContext().runApplication();
    }

    private static List<TestCase> setupTestCases(final Class<?> testClass, final TestContext context) {
        return ScenarioParser.parse(testClass, context.getSpecFileLocation())
                .stream()
                .map(scenario -> new TestCase(scenario, context.getApplicationUri(), testClass))
                .collect(toList());
    }
}
