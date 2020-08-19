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

package com.fordlabs.hungrymoose;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecTest {

    @Test
    void reportsNoChildrenWhenGivenANonexistentDirectory() {
        Spec actual = new Spec("/kjhsdfjgjsarbgkjer");
        assertThat(actual.getChildren()).isEmpty();
    }

    @Test
    void canReportZeroChildrenWhenGivenAnEmptyDirectory() {
        Spec actual = new Spec("src/test/resources/api/Alien");
        assertThat(actual.getChildren()).isEmpty();
    }

    @Test
    void canCorrectlyReportOneSpecInNoSubfolders() {
        Spec actual = new Spec("src/test/resources/api/Mushroom.yaml");
        assertThat(actual.getChildren()).isEmpty();
        assertThat(actual.isDirectory()).isFalse();
        assertThat(actual.getSpecName()).isEqualTo("Mushroom");
        assertThat(actual.getDirectoryName()).isEqualTo("api");
    }

    @Test
    void CanCorrectlyGetDirectoryName() {
        Spec actual = new Spec("src/test/resources/api/Animals/Fish and Sea Mammals/");
        assertThat(actual.getDirectoryName()).isEqualTo("Fish and Sea Mammals");
    }

    @Test
    void CanCorrectlyReportChildrenInSubfolder() {
        Spec actual = new Spec("src/test/resources/api/Animals/Fish and Sea Mammals/");
        Spec dolphinSpec = new Spec("src/test/resources/api/Animals/Fish and Sea Mammals/Bottle-Nosed Dolphin.yaml");
        Spec clownfishSpec = new Spec("src/test/resources/api/Animals/Fish and Sea Mammals/Clownfish.yaml");

        assertThat(actual.getChildren()).containsExactly(dolphinSpec, clownfishSpec);
    }

    @Test
    void CanCorrectlyReportMultipleSpecsAndSubfoldersAsChildren() {
        String pathname = "src/test/resources/api/Animals/";
        Spec actual = new Spec(pathname);
        Spec subfolder = new Spec(pathname + "Fish and Sea Mammals/");
        Spec grizzlySpec = new Spec(pathname + "Grizzly Bear.yaml");
        Spec platypusSpec = new Spec(pathname + "Platypus.yaml");

        assertThat(actual.getChildren()).containsExactly(subfolder, grizzlySpec, platypusSpec);
    }

    @Test
    void CanCorrectlyRetrieveListOfScenariosFromSpec() throws ScenarioParsingException, FileNotFoundException {
        String pathname = "src/test/resources/api/Animals/Fish and Sea Mammals/Clownfish.yaml";
        Spec specFromFile = new Spec(pathname);
        List<Scenario> actualScenarios = specFromFile.getScenarios();
        List<Scenario> expectedScenarios = Arrays.asList(
                new Scenario(
                        "A joke about the forest",
                        "Here comes a leg-slappingly-funny pun!",
                        "GET /what/do/you/call/a/bear/with/no/teeth HTTP/1.1\n",
                        "HTTP/1.1 200 OK\n\nA gummy bear.\n"),
                new Scenario(
                        "These don't even strictly have to be HTTP?",
                        null,
                        "Do you Yahoo?",
                        "Why yes I do Yahoo!")
        );

        assertThat(actualScenarios).isEqualTo(expectedScenarios);
    }

    @Test
    void CanCorrectlyReturnPathAfterRootPathAsUrlEncodedString() {
        Spec spec = new Spec("src/test/resources/api/Animals/Fish and Sea Mammals/Bottle-Nosed Dolphin.yaml");
        String expectedPathname = "Animals/Fish%20and%20Sea%20Mammals/Bottle-Nosed%20Dolphin.yaml";
        assertThat(spec.getUrlEncodedFilePath("api/")).isEqualTo(expectedPathname);
    }
}
