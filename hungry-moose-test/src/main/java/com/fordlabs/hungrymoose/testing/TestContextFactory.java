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

import org.apache.commons.lang3.Validate;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;

class TestContextFactory {
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
        Validate.isTrue(annotation != null, "No testable class found. Are you missing the @ApplicationToTest annotation?");
        return annotation.value();
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
        Validate.isTrue(resourceLocation != null, "No spec file found. Are you missing the @SpecsFromResourcePath annotation?");
        return resourceLocation.value();
    }
}
