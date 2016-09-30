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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;

import java.util.HashMap;
import java.util.Map;

public class DvDateTimeVBean extends DataValueAdapter implements I_VBeanWrapper {
	final String format = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    static Logger log = LogManager.getLogger(DvDateTimeVBean.class);
	/**
	 * 
	 */

	public DvDateTimeVBean(DvDateTime time) {
		this.adaptee = time;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object>map = new HashMap<String, Object>();
		map.put("value", ((DvDateTime)adaptee).toString());
		return map;
	}

    @Override
    public DvDateTime parse(String value, String... defaults) {
        adaptee = ((DvDateTime)adaptee).parse(value);
        return ((DvDateTime)adaptee);
    }

    public static DvDateTime getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvDateTime)
            return (DvDateTime)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");

            DvDateTime object = new DvDateTime(actualValue);
            return object;
        } else if (value instanceof String){
            DvDateTime object = new DvDateTime((String)value);
            return object;
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvDateTime generate(){
        return new DvDateTime(new DateTime(0L).toString());
    }

    public static DvDateTime increment(DvDateTime date) {
        DvDuration duration = new DvDuration("P1D"); //one day
        return date.add(duration); //increment by one day
    }
}
