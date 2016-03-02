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

import java.util.HashMap;
import java.util.Map;

public class DvDateVBean extends DataValueAdapter implements I_VBeanWrapper {

	/**
	 * 
	 */

	public DvDateVBean(DvDate d) {
		this.adaptee = d;
	}

	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", ((DvDate)adaptee).toString(true));
		return map;
	}

    @Override
    public DvDate parse(String value, String... defaults) {
        adaptee = ((DvDate)adaptee).parse(value);
        return ((DvDate)adaptee);
    }

    public static DvDate getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvDate) return (DvDate)value;

        if (value instanceof Map){
            Map<String, Object> valueMap = (Map)value;
//            Boolean dayKnown = (Boolean)valueMap.get("dayKnown");
//            Boolean monthKnown = (Boolean)valueMap.get("monthKnown");
//            Boolean isPartial = (Boolean)valueMap.get("dayKnown");
            String actualValue = (String)valueMap.get("value");

            return new DvDate(actualValue);
        }
        if (value instanceof String){
            DvDate object = new DvDate((String)value);
            return object;
        }

//        Object value = getValue(attributes);
//
//        if (!(value instanceof String))
//            throw new IllegalArgumentException("Value is not a String ");
//
//        DvDate object = new DvDate((String)value);

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvDate generate(){
        return new DvDate(1970,1,1);
    }

    public static DvDate increment(DvDate date) {
        DvDuration duration = new DvDuration("P1D"); //one day
        return date.add(duration); //increment by one day
    }
}
