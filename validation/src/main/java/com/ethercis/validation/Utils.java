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

package com.ethercis.validation;

import com.ethercis.validation.wrappers.ValidationException;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by christian on 7/14/2016.
 */
public class Utils {

    private static String constraintsSuffix = "VConstraints";

    public static String INSTRUMENT_PACKAGE = "com.ethercis.validation.wrappers";

    public static String snakeToCamel(String snakeString){
        return  Arrays.stream(snakeString.split("\\_"))
                .map(String::toLowerCase)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining());
    }

    public static void checkDateTimeSyntax(String path, String dvDateStr, String pattern){
        //TODO: do a smarter test based on the supplied pattern...
//        if (!DvDateTime.isValidISO8601DateTime(dvDateStr))
//           ValidationException.raise(path, "Supplied date/time is not ISO8601 compatible:" + dvDateStr, "DATE04");

        //check pattern if any
        if (pattern != null){
            //check ISO8601 validity
//            try {
//
//                DvDateTimeParser.toDateTimeString(dvDateStr, cdatetime.getPattern());
//            } catch (Exception e) {
//                throw new ValidationException(path, "Supplied value does not match pattern:" + dvDateStr + " (expected:" + cdatetime.getPattern() + "), error:"+e);
//            }
        }
    }

    public static Class findConstraintClass(Object object) {
        String clazzname = object.getClass().getSimpleName();
        return findConstraintClass(clazzname);
    }

    public static Class findConstraintClass(String clazzname) {
        try {
            Class<?> instrClazz = Class.forName(INSTRUMENT_PACKAGE +".constraints."+clazzname+constraintsSuffix);
            return instrClazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void constraintValidate(String path, Object value, Object archetypeconstraint) throws Exception {
        //get the instrumentalized class
        Class instrument = findConstraintClass(value);
        if (instrument != null){
            //get matching valid method
            Method validate = instrument.getDeclaredMethod("validate", String.class, value.getClass(), archetypeconstraint.getClass().getInterfaces()[0]);
            validate.invoke(null, path, value, archetypeconstraint);
        }
    }

}
