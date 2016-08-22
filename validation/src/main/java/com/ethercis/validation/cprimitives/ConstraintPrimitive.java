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

package com.ethercis.validation.cprimitives;


import com.ethercis.validation.ConstraintOccurrences;

/**
 * Created by christian on 7/14/2016.
 */
public class ConstraintPrimitive {
    ConstraintOccurrences occurences;
    String RmTypeName;
    String nodeId;

    public ConstraintPrimitive(ConstraintOccurrences occurences, String rmTypeName, String nodeId) {
        this.occurences = occurences;
        RmTypeName = rmTypeName;
        this.nodeId = nodeId;
    }

    public ConstraintPrimitive(){}

    public ConstraintOccurrences getOccurences() {
        return occurences;
    }

    public String getRmTypeName() {
        return RmTypeName;
    }

    public String getNodeId() {
        return nodeId;
    }
}
