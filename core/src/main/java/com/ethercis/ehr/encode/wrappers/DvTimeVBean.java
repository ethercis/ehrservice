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
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;

import java.util.HashMap;
import java.util.Map;

public class DvTimeVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvTimeVBean(DvTime t) {
		this.adaptee = t;
	}

	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", ((DvTime)adaptee).toString(true));
		return map;
	}

    @Override
    public DvTime parse(String value, String... defaults) {
        adaptee = ((DvTime)adaptee).parse(value);
        return (DvTime)adaptee;
    }

    public static DvTime getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvTime)
            return (DvTime)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");

            DvTime object = new DvTime(actualValue);
            return object;
        } else if (value instanceof String){
            DvTime object = new DvTime((String)value);
            return object;
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvTime generate(){
        return new DvTime(1, null);
    }

    public static DvTime increment(DvTime date) {
        DvDuration duration = new DvDuration("PT1H"); //one hour
        return date.add(duration); //increment by one day
    }
}
