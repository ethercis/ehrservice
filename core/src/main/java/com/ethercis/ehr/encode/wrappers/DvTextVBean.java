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
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;

import java.util.HashMap;
import java.util.Map;

public class DvTextVBean extends DataValueAdapter implements I_EhrScapeWrapper<DvText> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4874680195663851191L;

	public DvTextVBean(DvText t) {

        this.adaptee = t;
	}
	
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> mapval = new HashMap<String, Object>();
		mapval.put("value", ((DvText)adaptee).getValue());
		return mapval;
	}

    @Override
    public DvText parse(String value, String... defaults) {
        adaptee = ((DvText)adaptee).parse(value);
        return (DvText)adaptee;
    }

    @Override
    public Map<String, Object> setMappedElementAttributes(Map<String, Object> attributes) {
        Map<String, Object> attributesMap = new HashMap<>();

        if (attributes.containsKey("value")){
            attributesMap.put("value", attributes.get("value"));
        }
        if (attributes.containsKey("code")){
            attributesMap.put("codeString", attributes.get("code"));
        }
        if (attributes.containsKey("terminology")){
            attributesMap.put("terminologyId", attributes.get("terminology"));
        }

        return attributesMap;
    }

    @Override
    public DvText createInstance(Map<String, Object> attributes) {
        CodePhrase encoding = null;
        DvText text = new DvText((String)attributes.get("value"));
        if (attributes.containsKey("codeString") && attributes.containsKey("terminologyId")){
            encoding = new CodePhrase((String)attributes.get("terminologyId"), (String)attributes.get("codeString"));
            text.setEncoding(encoding);
        }

        return text;
    }

    public static DvText getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvText) return (DvText)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");
            return new DvText(actualValue);
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvText generate(){
        return new DvText("dummy");
    }

}
