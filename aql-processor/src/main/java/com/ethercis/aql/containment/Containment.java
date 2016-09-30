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

package com.ethercis.aql.containment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Container for containment definition<p>
 * This class is used to resolve a symbol in an AQL expression by associating its corresponding archetype Id,
 * class type (EHR, COMPOSITION etc.). Once resolved it holds the path for a given template.
 * </p>
 * Created by christian on 4/4/2016.
 */
public class Containment {

    public String symbol;
    public String archetypeId;
    public String className;
    public String path;
    public Containment enclosingContainment;

    public Containment(Containment enclosingContainment){
        this.enclosingContainment = enclosingContainment;
    }

    public Containment(String className, String symbol, String archetypeId){
        this.symbol = symbol;
        this.archetypeId = archetypeOnly(archetypeId);
        this.className = className;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getArchetypeId() {
        return archetypeId;
    }

    public String getPath() {
        return path;
    }

    public String getClassName() {
        return className;
    }

    public Containment getEnclosingContainment() {
        return enclosingContainment;
    }

    public void resolve(String ltreeExpression){
        //perform a query to the containment table...
        //return the corresponding containment map in the following format:
        // alias <archetypeId, path>
        return;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setArchetypeId(String archetypeId) {
        this.archetypeId = archetypeOnly(archetypeId);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setEnclosingContainment(Containment enclosingContainment) {
        this.enclosingContainment = enclosingContainment;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(className == null? "" : className);
        sb.append("::");
        sb.append(symbol == null ? "" : symbol);
        sb.append("::");
        sb.append(archetypeId == null ? "" : archetypeId);
        return sb.toString();
    }

    private String archetypeOnly(String archetypeId){
        if (archetypeId.length() > 0 && archetypeId.contains("["))
            return archetypeId.substring(0, archetypeId.indexOf("]")).substring(1);
        else
            return archetypeId;
    }
}
