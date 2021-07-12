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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HungryMooseJUnit4TestRunner extends Runner {
    private final HungryMooseTestRunner testRunner;
    private final Description suiteDescription;
    private final Method beforeClass;
    private final Method afterClass;
    private final Map<Description, TestCase> testCases;

    public HungryMooseJUnit4TestRunner(final Class<?> testClass) {
        this.testRunner = HungryMooseTestRunner.from(testClass);
        this.suiteDescription = Description.createSuiteDescription(testClass);
        this.testCases = setupTestCases(this.testRunner, this.suiteDescription, testClass);
        this.beforeClass = findMethodFromAnnotation(testClass, BeforeClass.class);
        this.afterClass = findMethodFromAnnotation(testClass, AfterClass.class);
    }

    private Map<Description, TestCase> setupTestCases(final HungryMooseTestRunner testRunner, final Description parentDescription, final Class<?> testClass) {
        final LinkedHashMap<Description, TestCase> result = new LinkedHashMap<>();
        for (final TestCase testCase : testRunner.getTestCases()) {
            final Description testDescription = Description.createTestDescription(testClass, testCase.getTestName());
            parentDescription.addChild(testDescription);
            result.put(testDescription, testCase);
        }
        return result;
    }

    private Method findMethodFromAnnotation(Class<?> testClass, Class<? extends Annotation> annotation) {
        Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(testClass);
        for (Method method : allDeclaredMethods) {
            if (method.isAnnotationPresent(annotation) && isJUnitClassMethod(method)) {
                return method;
            }
        }
        return null;
    }

    private boolean isJUnitClassMethod(Method method) {
        return Modifier.isStatic(method.getModifiers()) &&
                method.getReturnType().equals(Void.TYPE) &&
                method.getParameterTypes().length == 0 &&
                Modifier.isPublic(method.getModifiers());
    }

    @Override
    public Description getDescription() {
        return this.suiteDescription;
    }

    @Override
    public void run(final RunNotifier runNotifier) {
        this.testRunner.getTestContext().runApplication();
        runClassLevelMethod(runNotifier, this.beforeClass, "BeforeClass");
        runTestCases(runNotifier);
        runClassLevelMethod(runNotifier, this.afterClass, "AfterClass");
    }

    private void runTestCases(RunNotifier runNotifier) {
        for (final Entry<Description, TestCase> entry : this.testCases.entrySet()) {
            final Description testDescription = entry.getKey();
            final TestCase testCase = entry.getValue();
            runNotifier.fireTestStarted(testDescription);
            try {
                final Throwable[] hasThrowable = new Throwable[1];
                final Thread[] threads = new Thread[this.testRunner.getTestContext().getThreadCount()];
                for (int i = 0; i < this.testRunner.getTestContext().getThreadCount(); i++) {
                    final Thread thread = new Thread(() -> {
                        try {
                            testCase.runTest();
                        } catch (final Throwable e) {
                            synchronized (hasThrowable) {
                                hasThrowable[0] = e;
                            }
                        }
                    });
                    threads[i] = thread;
                    thread.start();
                }

                for (final Thread thread : threads) {
                    thread.join();
                }
                if (hasThrowable[0] != null) {
                    runNotifier.fireTestFailure(new Failure(testDescription, hasThrowable[0]));
                }

            } catch (final Throwable catchAllThrowablesOtherwiseTheTestRunnerMightFailSilently) {
                runNotifier.fireTestFailure(new Failure(testDescription, catchAllThrowablesOtherwiseTheTestRunnerMightFailSilently));
            }
            runNotifier.fireTestFinished(testDescription);
        }
    }

    private void runClassLevelMethod(RunNotifier runNotifier, Method methodToRun, String methodName) {
        try {
            if (methodToRun != null) {
                methodToRun.invoke(null);
            }
        } catch (Throwable e) {
            Description description = Description.createSuiteDescription(methodName);
            runNotifier.fireTestFailure(new Failure(description, e));
            this.suiteDescription.addChild(description);
            runNotifier.pleaseStop();
        }
    }
}
