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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {DocsController.class, HungryMooseDocsConfiguration.class})
@WebMvcTest(controllers = {DocsController.class}, properties = {"hungrymoose.docs.root: api/"})
@AutoConfigureMockMvc
class DocsControllerDefaultsTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:${hungrymoose.docs.root}")
    private File expectedRoot;

    private String expectedDocsUrlPrefix = "docs";

    @Test
    void rootTemplateIsReturned() throws Exception {
        mockMvc.perform(get('/' + expectedDocsUrlPrefix))
                .andExpect(status().isOk())
                .andExpect(view().name("rootDocTemplate"))
                .andExpect(model().attribute("specDocumentRoot", expectedRoot))
                .andExpect(model().attribute("specApiRoot", "/" + expectedDocsUrlPrefix));
    }

    @Test
    void scenarioTemplateIsReturned_WhenTopLevelScenarioNavigatedTo() throws Exception {
        mockMvc.perform(get('/' + expectedDocsUrlPrefix + "/Mushroom.yaml"))
                .andExpect(status().isOk())
                .andExpect(view().name("scenarioDocTemplate"))
                .andExpect(model().attribute("spec", new Spec(expectedRoot + "/Mushroom.yaml")))
                .andExpect(model().attribute("rootUrl", "/docs"));
    }

    @Test
    void scenarioTemplateIsReturned_WhenComplexLevelScenarioNavigatedTo() throws Exception {
        mockMvc.perform(get('/' + expectedDocsUrlPrefix + "/Animals/Fish and Sea Mammals/Bottle-Nosed Dolphin.yaml"))
                .andExpect(status().isOk())
                .andExpect(view().name("scenarioDocTemplate"))
                .andExpect(model().attribute("spec", new Spec(expectedRoot + "/Animals/Fish and Sea Mammals/Bottle-Nosed Dolphin.yaml")))
                .andExpect(model().attribute("rootUrl", "/docs"));
    }

    @Test
    void hungryMooseErrorPageIsReturned_WhenRequestedFileDoesNotExist() throws Exception {
        mockMvc.perform(get('/' + expectedDocsUrlPrefix + "/notavalidfile"))
                .andExpect(status().isOk())
                .andExpect(view().name("errorTemplate"));
    }

    @Test
    void hungryMooseErrorPageIsDisplayed_WhenParsingTheScenariosOfASpecFileFails() throws Exception {
        mockMvc.perform(get('/' + expectedDocsUrlPrefix + "/Animals/Platypus.yaml"))
                .andExpect(status().isOk())
                .andExpect(view().name("errorTemplate"));
    }
}