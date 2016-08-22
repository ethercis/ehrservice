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


import com.ethercis.validation.Utils;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.schemas.v1.CPRIMITIVE;
import org.openehr.schemas.v1.CPRIMITIVEOBJECT;
import org.openehr.schemas.v1.CTIME;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by christian on 7/22/2016.
 */
public class CTime extends CConstraint implements I_CTypeValidate {
    protected CTime(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, CPRIMITIVE cprimitive) throws Exception {

        if (!(aValue instanceof String))
            ValidationException.raise(path, "INTERNAL: Time validation expects a String argument", "SYS01");

        CTIME ctime = (CTIME)cprimitive;
        String dvTimeStr = (String)aValue;

        //check pattern if any
        Utils.checkDateTimeSyntax(path, dvTimeStr, ctime.isSetPattern() ? ctime.getPattern() : null);

        //range check
        DvTime time = new DvTime(dvTimeStr);

        if (ctime.isSetRange())
            IntervalComparator.isWithinBoundaries(time.getDateTime(), ctime.getRange());

        validateTimeZone(path, time, ctime);
    }

    public void validateTimeZone(String path, DvTime dvTime, CTIME ctime) throws Exception {
        if (ctime.isSetTimezoneValidity() && ctime.getTimezoneValidity().equals(new BigInteger("1001")) && dvTime.getDateTime().getZone() == null){
            ValidationException.raise(path, "Time zone is mandatory", "DATE02");
        }
        if (ctime.isSetTimezoneValidity() && ctime.getTimezoneValidity().equals(new BigInteger("1003")) && dvTime.getDateTime().getZone() != null){
            ValidationException.raise(path, "Time zone is not allowed", "DATE03");
        }
    }
}
