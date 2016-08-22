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

import org.joda.time.Duration;
import org.openehr.am.archetype.constraintmodel.primitive.CDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;

public class CDurationVBean {

	public static String getDefault(CDuration d) {

		if (d.hasAssignedValue()) {
			DvDuration def = d.assignedValue();
			return def.getValue();
		}

		if (d.hasAssumedValue()) 
			return ((DvDuration)d.assumedValue()).getValue();
		
		if (d.hasDefaultValue()) {
			DvDuration def = d.defaultValue();
			return def.getValue();
		}
		//ISO8601 string presentation: zero sec.
		return DvDuration.getInstance(Duration.ZERO.toString()).getValue();
		
		
	}
}
