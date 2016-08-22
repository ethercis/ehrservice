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
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.schemas.v1.CDATETIME;
import org.openehr.schemas.v1.CPRIMITIVE;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by christian on 7/23/2016.
 */
public class CDateTime extends CConstraint  implements I_CTypeValidate  {

    protected CDateTime(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, CPRIMITIVE cprimitive) throws Exception {
        if (!(aValue instanceof String))
            ValidationException.raise(path, "INTERNAL: Time validation expects a String argument", "SYS01");

        CDATETIME cdatetime = (CDATETIME)cprimitive;
        String dvDateStr = (String)aValue;

        Utils.checkDateTimeSyntax(path, dvDateStr, cdatetime.isSetPattern() ? cdatetime.getPattern() : null);

        //range check
        DvDateTime dateTime = new DvDateTime(dvDateStr);

        if (cdatetime.isSetRange())
            IntervalComparator.isWithinBoundaries(dateTime.getDateTime(), cdatetime.getRange());

        validateTimeZone(path, dateTime, cdatetime);
    }

    public void validateTimeZone(String path, DvDateTime dvDateTime, CDATETIME cdatetime) throws Exception {
        if (cdatetime.isSetTimezoneValidity() && cdatetime.getTimezoneValidity().equals(new BigInteger("1001")) && dvDateTime.getDateTime().getZone() == null){
            ValidationException.raise(path, "Time zone is mandatory", "DATE02");
        }
        if (cdatetime.isSetTimezoneValidity() && cdatetime.getTimezoneValidity().equals(new BigInteger("1003")) && dvDateTime.getDateTime().getZone() != null){
            ValidationException.raise(path, "Time zone is not allowed", "DATE03");
        }
    }
}
