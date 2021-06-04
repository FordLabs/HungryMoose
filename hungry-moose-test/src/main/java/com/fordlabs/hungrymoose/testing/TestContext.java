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

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.SpringApplication;

class TestContext {
    @Getter
    private final UriUnderTest applicationUri;
    private final Class<?> applicationClass;
    private final String[] activeProfiles;
    @Getter
    private final int threadCount;
    @Getter
    private final String specFileLocation;

    TestContext(UriUnderTest applicationUri, Class<?> applicationClass, String[] activeProfiles, int threadCount, String specFileLocation) {
        Validate.notNull(applicationUri, "applicationUri can't be null");
        Validate.notNull(applicationClass, "applicationClass can't be null");
        Validate.notNull(activeProfiles, "activeProfiles can't be null");
        Validate.isTrue(threadCount > 0, "Thread Count should be a positive number");
        Validate.notNull(specFileLocation, "specFileLocation can't be null");

        this.applicationUri = applicationUri;
        this.applicationClass = applicationClass;
        this.activeProfiles = activeProfiles;
        this.threadCount = threadCount;
        this.specFileLocation = specFileLocation;
    }

    void runApplication() {
        final SpringApplication springApplication = new SpringApplication(this.applicationClass);
        springApplication.setAdditionalProfiles(this.activeProfiles);
        springApplication.run("--server.port=" + applicationUri.getPort());
    }
}
