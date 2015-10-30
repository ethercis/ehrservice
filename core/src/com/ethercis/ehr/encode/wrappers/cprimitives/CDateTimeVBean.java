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
package com.ethercis.ehr.encode.wrappers.cprimitives;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openehr.am.archetype.constraintmodel.primitive.CDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;

public class CDateTimeVBean {

	public static String getDefault(CDateTime dt) {
		
		if (dt.hasAssignedValue()) 
			return dt.assignedValue().getValue();
		
		if (dt.hasAssumedValue()) 
			return ((DvDateTime)dt.assumedValue()).getValue();
		
		if (dt.hasDefaultValue()) {
			DvDateTime def = (DvDateTime) dt.defaultValue(); 
			return def.getValue();
		}
		
		String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"; //from DvDateTime...
		
		DateFormat fmt = new SimpleDateFormat(pattern);
		
		return fmt.format(new Date(0));
		
	}
}
