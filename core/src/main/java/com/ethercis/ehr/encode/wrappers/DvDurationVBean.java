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

import com.ethercis.ehr.encode.DataValueAdapter;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;

import java.util.HashMap;
import java.util.Map;

public class DvDurationVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvDurationVBean(DvDuration d) {
		this.adaptee = d;
	}

	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", adaptee.toString());
		return map;
	}

    @Override
    public DvDuration parse(String value, String... defaults) {
        adaptee = ((DvDuration)adaptee).parse(value);
        return (DvDuration) adaptee;
    }

    public static DvDuration getInstance(Map<String, Object> attributes){
        Object value = getValue(attributes);

        if (value instanceof DvDuration)
            return (DvDuration)value;

        if (value instanceof Map){
            Map<String, Object> valueMap = (Map)value;
            String actualValue = (String)valueMap.get("value");
            return new DvDuration(actualValue);
        }

        if (!(value instanceof String))
            throw new IllegalArgumentException("Value is not a String ");

        DvDuration object = new DvDuration((String)value);

        return object;
    }

    public static DvDuration generate(){
        return new DvDuration(1, 1, 1, 1, 1, 1, 1, 0);
    }
}
