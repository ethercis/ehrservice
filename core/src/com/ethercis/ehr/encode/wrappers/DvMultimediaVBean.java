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
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.terminology.SimpleTerminologyService;

import java.util.HashMap;
import java.util.Map;

public class DvMultimediaVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvMultimediaVBean(DvMultimedia m) {
		this.adaptee = m;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object>map = new HashMap<String, Object>();
		map.put("uri", ((DvMultimedia)adaptee).getUri().toString());
		return map;
	}

    @Override
    public DvMultimedia parse(String value, String... defaults) {
        adaptee = ((DvMultimedia)adaptee).parse(value);
        return (DvMultimedia) adaptee;
    }

    public static DvMultimedia getInstance(Map<String, Object> attributes){
        String alternateText;
        CodePhrase mediaType;
        CodePhrase compressionAlgorithm;
        CodePhrase integrityCheckAlgorithm;
        CodePhrase charSet;
        CodePhrase language;
        DvMultimedia thumbnail;
        DvURI uri;
        String terminologyId;
        String codeString;

        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvMultimedia) return (DvMultimedia)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            alternateText = (String) valueMap.get("alternateText");

            terminologyId = ((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("mediaType")).get("terminologyId"))).get("value").toString();
            codeString = (String)((LinkedTreeMap) valueMap.get("mediaType")).get("codeString");
            mediaType = new CodePhrase(terminologyId, codeString);

            terminologyId = ((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("compressionAlgorithm")).get("terminologyId"))).get("value").toString();
            codeString = (String)((LinkedTreeMap) valueMap.get("compressionAlgorithm")).get("codeString");
            compressionAlgorithm = new CodePhrase(terminologyId, codeString);

            terminologyId = ((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("integrityCheckAlgorithm")).get("terminologyId"))).get("value").toString();
            codeString = (String)((LinkedTreeMap)valueMap.get("integrityCheckAlgorithm")).get("codeString");
            integrityCheckAlgorithm = new CodePhrase(terminologyId, codeString);

            terminologyId = ((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("charset")).get("terminologyId"))).get("value").toString();
            codeString = (String)((LinkedTreeMap)valueMap.get("charset")).get("codeString");
            charSet = new CodePhrase(terminologyId, codeString);

            terminologyId = ((LinkedTreeMap)(((LinkedTreeMap)valueMap.get("language")).get("terminologyId"))).get("value").toString();
            codeString = (String)((LinkedTreeMap)valueMap.get("language")).get("codeString");
            language = new CodePhrase(terminologyId, codeString);

            thumbnail = null;

            uri = new DvURI((String)((LinkedTreeMap) valueMap.get("uri")).get("value"));

            TerminologyService terminologyService = SimpleTerminologyService.getInstance();

            return new DvMultimedia(charSet, language, alternateText, mediaType, compressionAlgorithm, null, integrityCheckAlgorithm, thumbnail, uri, null, terminologyService);
        }

//        Object value = attributes.get("value");
//
//        if (!(value instanceof String))
//            throw new IllegalArgumentException("Value is not a String ");
//
//        DvMultimedia object = new DvMultimedia((String)value);
//
//        return object;
        throw new IllegalArgumentException("Could not get instance");
    }

}
