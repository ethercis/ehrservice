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
import org.openehr.rm.datatypes.uri.DvURI;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DvURIVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvURIVBean(DvURI u) {
		this.adaptee = u;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", adaptee.toString());
		return map;
	}

	public static DvURI getInstance(Map<String, Object> attributes){
		Object value = attributes.get(CompositionSerializer.TAG_VALUE);

		if (value == null)
			throw new IllegalArgumentException("No value in attributes");

		if (value instanceof DvURI) return (DvURI)value;

		if (value instanceof Map) {
			Map<String, Object> valueMap = (Map) value;
			String actualValue = (String) valueMap.get("value");
			return new DvURI(actualValue);
		}

		throw new IllegalArgumentException("Could not get instance");
	}

    @Override
    public DvURI parse(String value, String... defaults) {
		//dvURI does not implement a parse method...
		URI uri = URI.create(value);
        adaptee = new DvURI(uri.toString());
        return (DvURI)adaptee;
    }

	public static DvURI generate(){
		return new DvURI("http://java.sun.com/j2se/1.3/");
	}
}
