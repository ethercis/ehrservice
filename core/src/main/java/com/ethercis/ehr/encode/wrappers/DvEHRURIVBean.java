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
import org.openehr.rm.datatypes.uri.DvEHRURI;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DvEHRURIVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvEHRURIVBean(DvEHRURI e) {
		this.adaptee = e;
	}

	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", adaptee.toString());
		return map;
	}

    @Override
    public DvEHRURI parse(String value, String... defaults) throws URISyntaxException {
//        if (!value.startsWith("ehr:"))
//            throw new IllegalArgumentException("EHR URI must be defined with scheme 'ehr'");
        DvEHRURI uri = new DvEHRURI(value);
        adaptee = uri;
        return (DvEHRURI) adaptee;
    }

    public static DvEHRURI getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvEHRURI)
            return (DvEHRURI)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");
            return new DvEHRURI(actualValue);
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvEHRURI generate(){
        return new DvEHRURI("http://java.sun.com/j2se/1.3/");
    }
}
