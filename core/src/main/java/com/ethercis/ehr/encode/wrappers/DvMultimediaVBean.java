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
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import com.google.gson.internal.LinkedTreeMap;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.terminology.TerminologyService;

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
        CodePhrase mediaType = null;
        CodePhrase compressionAlgorithm = null;
        CodePhrase integrityCheckAlgorithm = null;
        CodePhrase charSet = null;
        CodePhrase language = null;
        DvMultimedia thumbnail;
        DvURI uri = null;
        String terminologyId;
        String codeString;

        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvMultimedia) return (DvMultimedia)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            alternateText = (String) valueMap.get("alternateText");

            if (valueMap.containsKey("mediaType")) {
                Object mediaTypeEncoded = valueMap.get("mediaType");
                if (mediaTypeEncoded instanceof Map) {
                    terminologyId = ((LinkedTreeMap) (((LinkedTreeMap) mediaTypeEncoded).get("terminologyId"))).get("value").toString();
                    codeString = (String) ((LinkedTreeMap) mediaTypeEncoded).get("codeString");
                    mediaType = new CodePhrase(terminologyId, codeString);
                }
                else if (mediaTypeEncoded instanceof CodePhrase)
                    mediaType = (CodePhrase)mediaTypeEncoded;
                else
                    throw new IllegalArgumentException("mediaType type is not recognized:"+mediaTypeEncoded.toString());

            }

            if (valueMap.containsKey("compressionAlgorithm")) {
                Object compressionAlgorithmEncoded = valueMap.get("compressionAlgorithm");
                if (compressionAlgorithmEncoded instanceof Map) {
                    terminologyId = ((LinkedTreeMap) (((LinkedTreeMap) compressionAlgorithmEncoded).get("terminologyId"))).get("value").toString();
                    codeString = (String) ((LinkedTreeMap) compressionAlgorithmEncoded).get("codeString");
                    compressionAlgorithm = new CodePhrase(terminologyId, codeString);
                }
                else if (compressionAlgorithmEncoded instanceof CodePhrase)
                    compressionAlgorithm = (CodePhrase)compressionAlgorithmEncoded;
                else
                    throw new IllegalArgumentException("compressionAlgorithm type is not recognized:"+compressionAlgorithmEncoded.toString());
            }

            if (valueMap.containsKey("integrityCheckAlgorithm")) {
                Object integrityCheckAlgorithmEncoded = valueMap.get("integrityCheckAlgorithm");
                if (integrityCheckAlgorithmEncoded instanceof Map) {
                    terminologyId = ((LinkedTreeMap) (((LinkedTreeMap) integrityCheckAlgorithmEncoded).get("terminologyId"))).get("value").toString();
                    codeString = (String) ((LinkedTreeMap) integrityCheckAlgorithmEncoded).get("codeString");
                    integrityCheckAlgorithm = new CodePhrase(terminologyId, codeString);
                }
                else if (integrityCheckAlgorithmEncoded instanceof CodePhrase){
                    integrityCheckAlgorithm = (CodePhrase)integrityCheckAlgorithmEncoded;
                }
                else
                    throw new IllegalArgumentException("integrityCheckAlgorithm type is not recognized:"+integrityCheckAlgorithmEncoded.toString());
            }

            if (valueMap.containsKey("charset")) {
                Object charsetEncoded = valueMap.get("charset");
                if (charsetEncoded instanceof Map) {
                    terminologyId = ((LinkedTreeMap) (((LinkedTreeMap) charsetEncoded).get("terminologyId"))).get("value").toString();
                    codeString = (String) ((LinkedTreeMap) charsetEncoded).get("codeString");
                    charSet = new CodePhrase(terminologyId, codeString);
                }
                else if (charsetEncoded instanceof CodePhrase){
                    charSet = (CodePhrase)charsetEncoded;
                }
                else
                    throw new IllegalArgumentException("charset type is not recognized:"+charsetEncoded.toString());
            }

            if (valueMap.containsKey("language")) {
                Object languageEncoded = valueMap.get("language");
                if (languageEncoded instanceof Map) {
                    terminologyId = ((LinkedTreeMap) (((LinkedTreeMap) languageEncoded).get("terminologyId"))).get("value").toString();
                    codeString = (String) ((LinkedTreeMap) languageEncoded).get("codeString");
                    language = new CodePhrase(terminologyId, codeString);
                }
                else if (languageEncoded instanceof CodePhrase){
                    language = (CodePhrase)languageEncoded;
                }
                else
                    throw new IllegalArgumentException("language type is not recognized:"+languageEncoded.toString());

            }

            thumbnail = null;

            if (valueMap.containsKey("uri")) {
                Object uriEncoded = valueMap.get("uri");
                if (uriEncoded instanceof DvURI)
                    uri = (DvURI) uriEncoded;
                else if (uriEncoded instanceof Map)
                    uri = new DvURI((String) ((LinkedTreeMap) uriEncoded).get("value"));
                else
                    throw new IllegalArgumentException("uri type is not recognized:"+uriEncoded.toString());

            }
            TerminologyService terminologyService;
            try {
                terminologyService = TerminologyServiceWrapper.getInstance();
            } catch (Exception e){
                throw new IllegalArgumentException("Could not instantiate terminology service:"+e);
            }

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

    public static DvMultimedia generate(){
        CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
        CodePhrase language = new CodePhrase("ISO_639-1", "en");
        String alternateText = "alternative text";
        CodePhrase mediaType = new CodePhrase("IANA_media-types", "text/plain");
        CodePhrase compressionAlgorithm = new CodePhrase("openehr_compression_algorithms", "other");
        //byte[] integrityCheck = new byte[0];
        CodePhrase integrityCheckAlgorithm = new CodePhrase("openehr_integrity_check_algorithms", "SHA-1");
        DvMultimedia thumbnail = null;
        DvURI uri = new DvURI("www.iana.org");
        //byte[] data = new byte[0];
        TerminologyService terminologyService;
        try {
            terminologyService = TerminologyServiceWrapper.getInstance();
        } catch (Exception e){
            throw new IllegalArgumentException("Could not instantiate terminology service:"+e);
        }

        DvMultimedia dm = new DvMultimedia(charset, language, alternateText,
                mediaType, compressionAlgorithm, null,
                integrityCheckAlgorithm, thumbnail, uri, null, terminologyService);
        return dm;
    }

}
