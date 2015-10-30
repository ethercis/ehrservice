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
import com.google.gson.internal.LinkedTreeMap;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;

import java.util.HashMap;
import java.util.Map;

public class DvOrdinalVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvOrdinalVBean(DvOrdinal o) {
		this.adaptee = o;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value", ((DvOrdinal)adaptee).getValue());
		map.put("symbol", ((DvOrdinal)adaptee).getSymbol().toString());
		return map;
	}

    @Override
    public DvOrdinal parse(String value, String... defaults) {
        adaptee = ((DvOrdinal)adaptee).parse(value);
        return (DvOrdinal)adaptee;
    }

    public static DvOrdinal getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvOrdinal) return (DvOrdinal)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            Integer valueInt = ((Double) valueMap.get("value")).intValue();

            String terminologyId = (String)((LinkedTreeMap)(((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("symbol")).get("definingCode"))).get("terminologyId"))).get("value");
            String codeString = (String)(((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("symbol")).get("definingCode"))).get("codeString"));
            String definingCodeValue = (String)((LinkedTreeMap)valueMap.get("symbol")).get("value");
            CodePhrase codePhrase = new CodePhrase(terminologyId, codeString);
            DvCodedText codedSymbol = new DvCodedText(definingCodeValue, codePhrase);

            Integer limitsIndex = ((Double)valueMap.get("limitsIndex")).intValue();

            DvOrdinal object = new DvOrdinal(valueInt, codedSymbol);

            return object;
        }

        throw new IllegalArgumentException("Could not get instance");
    }
}
