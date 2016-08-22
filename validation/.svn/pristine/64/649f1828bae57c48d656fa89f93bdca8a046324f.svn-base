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
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.schemas.v1.CDATE;
import org.openehr.schemas.v1.CPRIMITIVE;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by christian on 7/23/2016.
 */
public class CDate extends CConstraint implements I_CTypeValidate {

    protected CDate(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, CPRIMITIVE cprimitive) throws Exception {
        if (!(aValue instanceof String))
            ValidationException.raise(path, "INTERNAL: Time validation expects a String argument", "DATE01");

        CDATE cdate = (CDATE)cprimitive;
        String dvDateStr = (String)aValue;

        //check pattern if any
        Utils.checkDateTimeSyntax(path, dvDateStr, cdate.isSetPattern() ? cdate.getPattern() : null);

        //range check
        DvDate date = new DvDate(dvDateStr);

        if (cdate.isSetRange())
            IntervalComparator.isWithinBoundaries(date.getDateTime(), cdate.getRange());

        validateTimeZone(path, date, cdate);
    }

    public void validateTimeZone(String path, DvDate dvDate, CDATE cdate) throws Exception {
        if (cdate.isSetTimezoneValidity() && cdate.getTimezoneValidity().equals(new BigInteger("1001")) && dvDate.getDateTime().getZone() == null){
            ValidationException.raise(path, "Time zone is mandatory", "DATE02");
        }
        if (cdate.isSetTimezoneValidity() && cdate.getTimezoneValidity().equals(new BigInteger("1003")) && dvDate.getDateTime().getZone() != null){
            ValidationException.raise(path, "Time zone is not allowed", "DATE03");
        }
    }
}
