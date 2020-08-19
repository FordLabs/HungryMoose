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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@ConditionalOnMissingBean(DocsController.class)
public class DocsDefaultController {

    private final DocsService docsService;

    public DocsDefaultController(DocsService docsService) {
        this.docsService = docsService;
    }


    @GetMapping("/docs/**")
    public ModelAndView getSpecPageDefault(HttpServletRequest request) {
        return docsService.retrieveSpecTemplate(request, "docs");
    }

    @GetMapping(value = "/docs")
    public ModelAndView getDocsLandingPageDefault() {
        return docsService.retrieveLandingPageTemplate("docs");
    }
}
