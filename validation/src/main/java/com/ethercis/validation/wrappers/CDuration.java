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

package com.ethercis.validation.wrappers;

import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.schemas.v1.CDURATION;
import org.openehr.schemas.v1.CPRIMITIVE;
import org.openehr.schemas.v1.CPRIMITIVEOBJECT;

import java.util.Map;

/**
 * Created by christian on 7/23/2016.
 */
public class CDuration extends CConstraint implements I_CTypeValidate {

    protected CDuration(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, CPRIMITIVE cprimitive) throws Exception {
        if (!(aValue instanceof String))
            throw new ValidationException(path, "INTERNAL: Time validation expects a String argument");

        CDURATION cduration = (CDURATION)cprimitive;
        String dvDurationStr = (String)aValue;

        //check pattern if any
        //TODO: use a pattern matching test for duration
//        if (cduration.isSetPattern() && !dvDurationStr.matches(cduration.getPattern())){
//            throw new ValidationException(path, "Supplied value does not match pattern:"+dvDurationStr);
//        }

        //range check
        if (cduration.isSetRange())
            IntervalComparator.isWithinBoundaries(dvDurationStr, cduration.getRange());
    }
}
