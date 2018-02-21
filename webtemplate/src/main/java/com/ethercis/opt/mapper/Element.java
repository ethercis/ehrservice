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

package com.ethercis.opt.mapper;

import com.ethercis.opt.TermDefinition;
import org.openehr.schemas.v1.CATTRIBUTE;
import org.openehr.schemas.v1.CCOMPLEXOBJECT;
import org.openehr.schemas.v1.COBJECT;
import org.openehr.schemas.v1.IntervalOfInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 1/31/2018.
 */
public class Element {

    CCOMPLEXOBJECT ccomplexobject;
    Map<String, TermDefinition> termDef;

    public Element(CCOMPLEXOBJECT ccomplexobject, Map<String, TermDefinition> termDef) {
        this.ccomplexobject = ccomplexobject;
        this.termDef = termDef;
    }

    public Map<String, Object> toMap(String nodeId, String path, List<Map<String, Object>> embedded) {
        Map<String, Object> elementMap = new HashMap<>();

        Map validationMap = new HashMap<>();

        elementMap.put(Constants.AQL_PATH, path);

        if (validationMap.size() > 0)
            elementMap.put(Constants.VALIDATION, validationMap);

        elementMap.put(Constants.NODE_ID, nodeId);
        elementMap.put(Constants.NAME, termDef.get(nodeId).getValue());
        elementMap.put(Constants.DESCRIPTION, termDef.get(nodeId).getDescription());

        Map rangeMap = new HashMap<>();

        for (CATTRIBUTE cattribute : ccomplexobject.getAttributesArray()) {
            if (cattribute.getRmAttributeName().equals(Constants.VALUE)) {
                validationMap.put(Constants.OCCURRENCE, rangeMap);
                rangeMap.put(Constants.MIN_OP, cattribute.getExistence().isSetLowerIncluded() ? ">=" : ">");
                rangeMap.put(Constants.MIN, cattribute.getExistence().isSetLower() ? cattribute.getExistence().getLower() : -1);
                rangeMap.put(Constants.MAX_OP, cattribute.getExistence().isSetUpperIncluded() ? "<=" : "<");
                rangeMap.put(Constants.MAX, cattribute.getExistence().isSetUpper() ? cattribute.getExistence().getUpper() : -1);
            }
            if (cattribute.getChildrenArray().length > 0) {
                List<Map<String, Object>> children = new ArrayList<>();
                Map<String, Object> childrenMap = new HashMap<>();
                for (COBJECT cobj : cattribute.getChildrenArray()) { //element may 0..1 value point
                    //occurrence
                    IntervalOfInteger occurrence = cobj.getOccurrences();
                    rangeMap = new HashMap<>();
                    childrenMap.put(Constants.LIMITS, rangeMap);
                    rangeMap.put(Constants.MIN_OP, occurrence.isSetLowerIncluded() ? ">=" : ">");
                    rangeMap.put(Constants.MIN, occurrence.isSetLower() ? occurrence.getLower() : -1);
                    rangeMap.put(Constants.MAX_OP, occurrence.isSetUpperIncluded() ? "<=" : "<");
                    rangeMap.put(Constants.MAX, occurrence.isSetUpper() ? occurrence.getUpper() : -1);
                    elementMap.put(Constants.TYPE, cobj.getRmTypeName());
                    if (embedded != null && !embedded.isEmpty())
                        childrenMap.put(Constants.CONSTRAINT, embedded);
                    children.add(childrenMap);
                }
                if (children.size() > 0)
                    elementMap.put(Constants.CHILDREN, children);
            }
        }

        return elementMap;
    }
}
