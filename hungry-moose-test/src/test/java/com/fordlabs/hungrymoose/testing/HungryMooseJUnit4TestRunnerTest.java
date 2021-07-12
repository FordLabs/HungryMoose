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

package com.fordlabs.hungrymoose.testing;

import com.fordlabs.fordlabs.hungrymoose.acceptance.RapidStubApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.StoppedByUserException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class HungryMooseJUnit4TestRunnerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void firesTestFailureWhenAnAssertionFails() {
        HungryMooseJUnit4TestRunner runner = new HungryMooseJUnit4TestRunner(ThisTestShouldFailOrWeHaveBiggerProblems.class);
        Result result = runJUnit(runner);
        assertThat(result.getFailureCount(), is(1));
    }

    @Test
    public void throwsExceptionIfNoApplicationToTest() {
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage("No testable class found. Are you missing the @ApplicationToTest annotation?");
        new HungryMooseJUnit4TestRunner(RunnerWithoutApplicationToTest.class);
    }

    @Test
    public void throwsExceptionIfNoSpecFilePath() {
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage("No spec file found. Are you missing the @SpecsFromResourcePath annotation?");
        new HungryMooseJUnit4TestRunner(RunnerWithoutSpecFilePath.class);
    }
    
    @Test
    public void runsAnnotatedMethods() {
        HungryMooseJUnit4TestRunner runner = new HungryMooseJUnit4TestRunner(AnnotatedTest.class);
        runJUnit(runner);
        assertThat(AnnotatedTest.beforeClassCalled, is(true));
        assertThat(AnnotatedTest.afterClassCalled, is(true));
    }
    
    @Test
    public void runsFailingBeforeAnnotatedMethods() {
        HungryMooseJUnit4TestRunner runner = new HungryMooseJUnit4TestRunner(FailingBeforeAnnotatedTest.class);
        this.expectedException.expect(StoppedByUserException.class);
        runJUnit(runner);
    }
    
    @Test
    public void runsFailingAfterAnnotatedMethods() {
        HungryMooseJUnit4TestRunner runner = new HungryMooseJUnit4TestRunner(FailingAfterAnnotatedTest.class);
        Result result = runJUnit(runner);
        assertThat(result.getFailureCount(), is(1));
    }

    @ApplicationToTest(RapidStubApplication.class)
    @SpecsFromResourcePath("api/hungrymoose/annotated-spec.yaml")
    public static class AnnotatedTest {

        static boolean beforeClassCalled;
        static boolean afterClassCalled;

        @BeforeClass
        public static void beforeClass() {
            beforeClassCalled = true;
        }

        @AfterClass
        public static void afterClass() {
            afterClassCalled = true;
        }

    }

    @ApplicationToTest(RapidStubApplication.class)
    @SpecsFromResourcePath("api/hungrymoose/annotated-spec.yaml")
    public static class FailingBeforeAnnotatedTest {

        @BeforeClass
        public static void beforeClass() {
            throw new RuntimeException("Boom");
        }

    }

    @ApplicationToTest(RapidStubApplication.class)
    @SpecsFromResourcePath("api/hungrymoose/annotated-spec.yaml")
    public static class FailingAfterAnnotatedTest {
        
        @AfterClass
        public static void afterClass() {
            throw new RuntimeException("Boom");
        }
        
    }

    @ApplicationToTest(RapidStubApplication.class)
    @SpecsFromResourcePath("api/hungrymoose/failing-spec.yaml")
    public static class ThisTestShouldFailOrWeHaveBiggerProblems {
    }

    @SpecsFromResourcePath("api/hungrymoose/annotated-spec.yaml")
    static class RunnerWithoutApplicationToTest {
    }

    @ApplicationToTest(RapidStubApplication.class)
    static class RunnerWithoutSpecFilePath {
    }

    private Result runJUnit(HungryMooseJUnit4TestRunner runner) {
        return new JUnitCore().run(Request.runner(runner));
    }
    
}
