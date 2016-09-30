/*
 * Copyright (c) 2015 Christian Chevalley
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

package com.ethercis.aql.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 9/20/2016.
 */
public class FunctionDefinition {

    public class Definition {
        private List<String> parameters;
        private String alias;

        public Definition(List<String> parameters) {
            this.parameters = parameters;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public String getAlias() {
            return alias;
        }
    }

    //list of maps consisting of a function name (key) and a list of parameters (identifiers)
    private Map<String, Definition> functions = new HashMap<>();

    public void add(String name, List<String> identifiers){
        Definition definition = new Definition(identifiers);
        functions.put(name, definition);
    }

    public void setAlias(String function, String alias){
        Definition definition = functions.get(function);
        definition.setAlias(alias);
    }

    public List<String> getIdentifiers(String functionName){
        Definition definition = functions.get(functionName);
        return definition.getParameters();
    }

    public String getAlias(String functionName){
        Definition definition = functions.get(functionName);
        return definition.getAlias();
    }

    public int size(){
        return functions.size();
    }
}
