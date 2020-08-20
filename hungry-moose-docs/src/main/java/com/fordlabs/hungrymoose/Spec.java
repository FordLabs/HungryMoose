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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Spec extends File {

    private static final long serialVersionUID = 2434952503855264076L;

    public Spec(String pathname) {
        super(pathname);
    }

    public static Spec from(File file) {
        return new Spec(file.getPath());
    }

    public String getSpecName() {
        return getName().substring(0, getName().indexOf('.'));
    }

    public String getDirectoryName() {
        if (isDirectory()) {
            return getName();
        } else {
            String[] pathParts = getPath().split("/");
            return pathParts[pathParts.length - 2];
        }
    }

    public List<Spec> getChildren() {
        if (isDirectory()) {
            return Arrays.stream(listFiles()).map(Spec::from).sorted().collect(toList());
        } else {
            return emptyList();
        }
    }

    public List<Scenario> getScenarios() throws ScenarioParsingException {
        try(FileInputStream fileStream = new FileInputStream(this)) {
            YAMLParser yamlParser = new YAMLFactory().createParser(fileStream);
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.readValues(yamlParser, new TypeReference<Scenario>(){}).readAll();
        }
        catch (Exception e) {
            throw new ScenarioParsingException(e);
        }
    }

    public String getUrlEncodedFilePath(String rootPath) {
        return getPath().split(rootPath)[1].replaceAll(" ", "%20");
    }
}
