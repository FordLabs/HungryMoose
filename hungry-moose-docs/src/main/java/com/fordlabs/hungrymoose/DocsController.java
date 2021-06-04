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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@ConditionalOnProperty(value = "hungrymoose.docs.endpoint")
public class DocsController {
    private final String endpoint;
    private final DocsService docsService;

    public DocsController(DocsService docsService, @Value("${hungrymoose.docs.endpoint}") String endpoint) {
        this.docsService = docsService;
        this.endpoint = endpoint;
    }

    @GetMapping(value = "/${hungrymoose.docs.endpoint}")
    public ModelAndView getDocsLandingPage() {
        return docsService.retrieveLandingPageTemplate(endpoint);
    }

    @GetMapping("/${hungrymoose.docs.endpoint}/**")
    public ModelAndView getSpecPage(HttpServletRequest request) {
        return docsService.retrieveSpecTemplate(request, endpoint);
    }
}
