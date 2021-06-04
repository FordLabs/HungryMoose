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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;

class TestContextFactory {

    public static final String MISSING_SPEC_FILES_TEXT = "No spec file found. Are you missing the @SpecsFromResourcePath annotation?";
    public static final String MISSING_APPLICATION_TEXT = "No testable class found. Are you missing the @ApplicationToTest annotation?";

    static TestContext build(Class<?> testClass) {
        return new TestContext(
                UriUnderTestFactory.getUriUnderTest(),
                getApplicationToTest(testClass),
                getActiveProfiles(testClass),
                getThreadCount(testClass),
                getSpecFileLocation(testClass));
    }

    private static Class<?> getApplicationToTest(final Class<?> testClass) {
        final ApplicationToTest annotation = AnnotationUtils.findAnnotation(testClass, ApplicationToTest.class);
        if(annotation == null) throw new IllegalArgumentException(MISSING_APPLICATION_TEXT);
        else return annotation.value();
    }

    private static String[] getActiveProfiles(final Class<?> testClass) {
        final ActiveProfiles activeProfiles = AnnotationUtils.findAnnotation(testClass, ActiveProfiles.class);
        return activeProfiles == null ? new String[0] : activeProfiles.value();
    }

    private static int getThreadCount(final Class<?> testClass) {
        final ThreadCount count = AnnotationUtils.findAnnotation(testClass, ThreadCount.class);
        return count == null ? 1 : count.value();
    }

    private static String getSpecFileLocation(final Class<?> testClass) {
        final SpecsFromResourcePath resourceLocation = AnnotationUtils.findAnnotation(testClass, SpecsFromResourcePath.class);
        if(resourceLocation == null) throw new IllegalArgumentException(MISSING_SPEC_FILES_TEXT);
        else return resourceLocation.value();
    }

}
