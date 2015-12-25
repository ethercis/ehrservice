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
import org.openehr.rm.datatypes.basic.DvIdentifier;

import java.util.HashMap;
import java.util.Map;

public class DvIdentifierVBean extends DataValueAdapter implements I_VBeanWrapper {

	/**
	 * 
	 */

	public DvIdentifierVBean(DvIdentifier i) {
		this.adaptee = i;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("issuer", ((DvIdentifier)adaptee).getIssuer());
		map.put("assigner", ((DvIdentifier)adaptee).getAssigner());
		map.put("id", ((DvIdentifier)adaptee).getId());
		map.put("type", ((DvIdentifier)adaptee).getType());
		return map;
	}

    @Override
    public DvIdentifier parse(String value, String... defaults) {
        adaptee = ((DvIdentifier)adaptee).parse(value);
        return (DvIdentifier) adaptee;
    }
//    "/value": {
//        "id": "1",
//        "type": "1",
//        "issuer": "1",
//        "assigner": "1"
//    },
    public static DvIdentifier getInstance(Map<String, Object> attributes){
        Object value = getValue(attributes);

        if (value instanceof DvIdentifier)
            return (DvIdentifier)value;

        if (value instanceof Map){
            String id = (String) ((Map)value).get("id");
            String type = (String) ((Map)value).get("type");
            String issuer = (String) ((Map)value).get("issuer");
            String assigner = (String) ((Map)value).get("assigner");
            return new DvIdentifier(issuer, assigner, id, type);
        }

        if (!(value instanceof String))
            throw new IllegalArgumentException("Value is not a String ");

        return new DvIdentifierVBean(null).parse((String)value, null);
    }

    public static DvIdentifier generate(){
        return new DvIdentifier("dummy", "dummy", "1234", "dummy");
    }
}
