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
import org.openehr.am.template.TermMap;
import org.openehr.rm.datatypes.text.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DvCodedTextVBean extends DataValueAdapter implements I_VBeanWrapper {

    private final class Limits {
    }

	/**
	 * DvCodedText is final for some reason...
	 * @param text
	 */
	public DvCodedTextVBean(DvCodedText text) {
		this.adaptee = text;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> mapval = new HashMap<String, Object>();
		
		mapval.put("definingCode", ((DvCodedText)adaptee).getDefiningCode().getTerminologyId()+"::"+((DvCodedText)adaptee).getDefiningCode().getCodeString());
		mapval.put("value", ((DvCodedText)adaptee).getValue());
		return mapval;
	}

    @Override
    public DvCodedText parse(String value, String... defaults) {
        if (!value.contains("::")){ //no terminology separator, use the supplied default
            if (defaults == null || defaults.length == 0)
                throw new IllegalArgumentException("DvCodedText could not parse value:"+value);

            value = defaults[0]+"::"+value;
        }
        adaptee = ((DvCodedText)adaptee).parse(value);
        return ((DvCodedText)adaptee);
    }

    public static DvCodedText getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvCodedText)
            return (DvCodedText)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;

            String actualValue = (String) valueMap.get("value");

            CodePhrase codePhrase;
            Object definingCode = valueMap.get("definingCode");

//            if (definingCode == null){
//                return (DvCodedText)new DvText(actualValue);
//            }

            if (definingCode instanceof Map) {
                Map<String, Object> defCode = (Map<String, Object>) valueMap.get("definingCode");

                String terminologyId = ((Map<String, String>) defCode.get("terminologyId")).get("value");
                String codeString = (String) defCode.get("codeString");
                codePhrase = new CodePhrase(terminologyId, codeString);
            }
            else if (definingCode instanceof CodePhrase)
                codePhrase = (CodePhrase)definingCode;
            else
                throw new IllegalArgumentException("Could not handle codephrase argument:"+definingCode);

            //mappings
            DvCodedText dvCodedText = new DvCodedText(actualValue, codePhrase);

            Object mappings = valueMap.get("mappings");
            if (mappings != null && mappings instanceof List){
                Map<String, Object> termMappingValueMap = new HashMap<>();
                List<TermMapping> termMappings = new ArrayList<>();
                for (Object termMap: (List)mappings){
                    termMappingValueMap.put(CompositionSerializer.TAG_VALUE, termMap);
                    TermMapping termMapping = TermMappingVBean.getInstance(termMappingValueMap);
                    termMappings.add(termMapping);
                }
                dvCodedText.setMappings(termMappings);
            }

            return dvCodedText;
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvCodedText generate(){
        return new DvCodedText("*generated*", "openehr", "1");
    }
}
