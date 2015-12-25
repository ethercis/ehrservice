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
package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.DataValueAdapter;
import org.openehr.rm.datatypes.basic.DvBoolean;

import java.util.HashMap;
import java.util.Map;

public class DvBooleanVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvBooleanVBean(DvBoolean b) {
		this.adaptee = b;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object>map = new HashMap<String, Object>();
		map.put("value", ((DvBoolean) adaptee).toString());
		return map;
	}

    @Override
    public DvBoolean parse(String value, String... defaults) {
        this.adaptee = ((DvBoolean)adaptee).parse(value);
        return (DvBoolean)adaptee;
    }

    public static DvBoolean getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvBoolean)
            return (DvBoolean)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            Object passedValue = valueMap.get("value");
            Boolean actualValue;

            if (passedValue instanceof Boolean)
                actualValue = (Boolean)passedValue;
            else if (passedValue instanceof String)
                actualValue = Boolean.valueOf((String)passedValue);
            else
                throw new IllegalArgumentException("Could not parse object as Boolean:"+passedValue);

            return new DvBoolean(actualValue);
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvBoolean generate(){
        return new DvBoolean(true);
    }

}
