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

import org.openehr.am.archetype.constraintmodel.primitive.CBoolean;

public class CBooleanVBean {
	
	public static Boolean getDefault(CBoolean b) {

		if (b.hasAssignedValue()) 
			return new Boolean(b.assignedValue().getValue());
		
		if (b.hasAssumedValue()) 
			return new Boolean(b.assumedValue());
		
		if (b.hasDefaultValue()) 
			return new Boolean(b.defaultValue().getValue());
		
		if(b.isTrueValid()) {
			return new Boolean(true);
		} else {
			return new Boolean(false);
		}
	}
}
