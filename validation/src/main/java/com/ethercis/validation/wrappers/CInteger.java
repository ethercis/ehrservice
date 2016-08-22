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

import org.apache.commons.lang.ArrayUtils;
import org.openehr.schemas.v1.CINTEGER;
import org.openehr.schemas.v1.CPRIMITIVE;
import org.openehr.schemas.v1.CPRIMITIVEOBJECT;
import org.openehr.schemas.v1.IntervalOfInteger;

import java.util.Map;

/**
 * Created by christian on 7/23/2016.
 */
public class CInteger extends CConstraint  implements I_CTypeValidate {

    protected CInteger(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, CPRIMITIVE cprimitive) throws Exception {

        CINTEGER cinteger = (CINTEGER)cprimitive;
        Integer integer = (Integer)aValue;

        IntervalOfInteger intervalOfInteger = cinteger.getRange();
        if (intervalOfInteger != null)
            IntervalComparator.isWithinBoundaries(integer, intervalOfInteger);

        //check within value list if specified
        if (cinteger.sizeOfListArray() > 0 && !ArrayUtils.contains(cinteger.getListArray(), integer))
            ValidationException.raise(path, "Integer value does not match any values in constraint:" + integer, "INT01");
    }
}
