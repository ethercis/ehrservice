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
import com.ethercis.opt.ValuePoint;
import org.openehr.build.RMObjectBuildingException;
import org.openehr.schemas.v1.*;

import java.util.*;

/**
 * Created by christian on 1/31/2018.
 */
public class Duration {

    static final String type = "DV_DURATION";

    CCOMPLEXOBJECT ccomplexobject;
    Map<String, TermDefinition> termDef;

    public Duration(CCOMPLEXOBJECT ccomplexobject, Map<String, TermDefinition> termDef) {
        this.ccomplexobject = ccomplexobject;
        this.termDef = termDef;
    }

    public Map<String, Object> toMap(String name) throws NoSuchFieldException, RMObjectBuildingException {

        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(Constants.TYPE, type);
        attributeMap.put(Constants.ATTRIBUTE_NAME, name);
        attributeMap.put(Constants.MANDATORY_ATTRIBUTES, new ValuePoint(type).attributes());

        List valueList = new ArrayList<>();

        Map<String, Object> constraintsMap = new HashMap<>();

        attributeMap.put(Constants.CONSTRAINT, constraintsMap);

        List<Map<String, Object>> specMap = new ArrayList<>();

        for (CATTRIBUTE attribute : ccomplexobject.getAttributesArray()) {
            Map<String, Object> map = new HashMap<>();

            if (attribute.getRmAttributeName().equals(Constants.VALUE)) {
                for (COBJECT cobject : attribute.getChildrenArray()) {
                    if (cobject.getRmTypeName().equals("DURATION")) {
                        //get the range and set it in constraints
                        map.put(Constants.NAME, Constants.VALUE);
                        CPRIMITIVEOBJECT cprimitiveobject = (CPRIMITIVEOBJECT) cobject;
                        CDURATION cprimitive = (CDURATION) cprimitiveobject.getItem();
                        if (cprimitive.isSetRange()) {
                            if (cprimitive.getRange().isSetLower())
                                map.put(Constants.MIN, cprimitive.getRange().getLower());
                            if (cprimitive.getRange().isSetUpper())
                                map.put(Constants.MAX, cprimitive.getRange().getUpper());
                        }
                        if (cprimitive.isSetPattern()) {
                            map.put(Constants.PATTERN, cprimitive.getPattern());
                        }
                    }
                }
            }

            specMap.add(map);
        }

        if (!specMap.isEmpty())
            constraintsMap.put(Constants.VALUE_MAP, specMap);

        Map<String, Object> range = new HashMap<>();

        constraintsMap.put(Constants.OCCURRENCE, range);
        range.put(Constants.MIN_OP, ccomplexobject.getOccurrences().isSetLowerIncluded() ? ">=" : ">");
        range.put(Constants.MIN, ccomplexobject.getOccurrences().isSetLower() ? ccomplexobject.getOccurrences().getLower() : -1);
        range.put(Constants.MAX_OP, ccomplexobject.getOccurrences().isSetUpperIncluded() ? "<=" : "<");
        range.put(Constants.MAX, ccomplexobject.getOccurrences().isSetUpper() ? ccomplexobject.getOccurrences().getUpper() : -1);
        valueList.add(attributeMap);

        return attributeMap;
    }
}
