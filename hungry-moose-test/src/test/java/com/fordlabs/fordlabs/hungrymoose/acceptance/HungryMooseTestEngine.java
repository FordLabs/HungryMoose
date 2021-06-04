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

import org.junit.platform.engine.*;

public class HungryMooseTestEngine implements TestEngine {
    @Override
    public String getId() {
        return "hungry-moose";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        System.out.println("I guess you've DISCOVERED that I don't care what you found");
        return null;
    }

    @Override
    public void execute(ExecutionRequest request) {
        System.out.println("I would totally be running right now if I didn't have nothing to do");
    }
}
