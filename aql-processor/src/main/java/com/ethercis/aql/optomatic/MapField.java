/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
 * This file is part of Project Ethercis
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

package com.ethercis.aql.optomatic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by christian on 9/26/2017.
 */
public class MapField {

    private String path;
    private Map config;

    public MapField(String path) {
        this.path = path;
        readConfig();
    }

    private void readConfig(){

        String jsonConfig;
        try {
            jsonConfig = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read map config:"+path);
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        config = gson.fromJson(jsonConfig, Map.class);
    }
}
