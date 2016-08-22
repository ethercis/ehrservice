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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.openehr.am.archetype.constraintmodel.primitive.CDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;

public class CDateVBean {
	
	public static String getDefault(CDate d) throws ParseException {
		
		if (d.hasAssignedValue()) 
			return d.assignedValue().getValue();
		
		if (d.hasAssumedValue()) 
			return ((DvDate)d.assumedValue()).getValue();
		
		if (d.hasDefaultValue()) {
			DvDate def = (DvDate)d.defaultValue();
			return def.getValue();
		}
		
		String pattern = d.getPattern();
		
		if (pattern == null)
			pattern = "yyyy-MM-dd"; //from DvDate...
		
		DateFormat fmt = new SimpleDateFormat(pattern);

		return fmt.format(fmt.parse("1970-01-01"));
	}
}
