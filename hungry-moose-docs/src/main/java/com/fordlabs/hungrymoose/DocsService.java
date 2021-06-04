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
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
class DocsService {

    private File rootOfSpec;

    public DocsService(@Value("classpath:${hungrymoose.docs.root:api/}") File rootOfSpec) {
        this.rootOfSpec = rootOfSpec;
    }

    ModelAndView retrieveSpecTemplate(HttpServletRequest request, String rootEndpoint) {
        String fileNameAndLocation = getFileNameFromRequest(request, rootEndpoint);
        Spec spec = new Spec(rootOfSpec + fileNameAndLocation);

        if(!spec.exists()) {
            return new ModelAndView("errorTemplate");
        }

        try {
            ModelAndView scenarioDocTemplate = new ModelAndView("scenarioDocTemplate");
            scenarioDocTemplate.addObject("spec", spec);
            scenarioDocTemplate.addObject("scenarios", spec.getScenarios());
            scenarioDocTemplate.addObject("rootUrl", "/" + rootEndpoint);
            return scenarioDocTemplate;
        } catch (ScenarioParsingException e) {
            return new ModelAndView("errorTemplate");
        }
    }

    ModelAndView retrieveLandingPageTemplate(String endpoint) {
        ModelAndView rootDocumentTemplate = new ModelAndView("rootDocTemplate");
        rootDocumentTemplate.addObject("rootNode", Spec.from(rootOfSpec));
        rootDocumentTemplate.addObject("specDocumentRoot", rootOfSpec);
        rootDocumentTemplate.addObject("specApiRoot", "/" + endpoint);
        return rootDocumentTemplate;
    }

    private String getFileNameFromRequest(HttpServletRequest request, String rootUrlPath) {
        String fileNameAndLocation = request.getRequestURI().split(request.getContextPath() + "/" + rootUrlPath + "/")[1];
        return "/" + decodeUrl(fileNameAndLocation);
    }

    private String decodeUrl(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
