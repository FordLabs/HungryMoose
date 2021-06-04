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

package com.fordlabs.hungrymoose;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioTest {

    @Test
    void getScenarioId_IsLowercaseVersionOfName() {
        Scenario scenario = createScenario("TheScenario");
        assertThat(scenario.getScenarioId()).isEqualTo("thescenario");
    }

    @Test
    void getScenarioId_ReplacesSpacesWithDashes() {
        Scenario scenario = createScenario("the scenario");
        assertThat(scenario.getScenarioId()).isEqualTo("the-scenario");
    }

    @Test
    void getScenarioId_RemovesAsciiControlCharacters() {
        Scenario scenario = createScenario("rem\\ov>es <con{tr}ol characters");
        assertThat(scenario.getScenarioId()).isEqualTo("removes-control-characters");
    }

    @Test
    void getScenarioId_RemovesReservedCharacters() {
        Scenario scenario = createScenario("rem!*'()oves res;:@&=erved+$,/ ?#[]characters");
        assertThat(scenario.getScenarioId()).isEqualTo("removes-reserved-characters");
    }

    private Scenario createScenario(String theScenario) {
        return new Scenario(
                theScenario,
                "Some additional information",
                "this is a request",
                "this is a response");
    }
}
