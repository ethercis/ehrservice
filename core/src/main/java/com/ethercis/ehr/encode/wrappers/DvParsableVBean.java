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
import org.openehr.rm.datatypes.encapsulated.DvParsable;

import java.util.HashMap;
import java.util.Map;

public class DvParsableVBean extends DataValueAdapter implements I_VBeanWrapper {

    public DvParsableVBean(DvParsable p) {
        this.adaptee = p;
    }

    @Override
    public Map<String, Object> getFieldMap() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        if (((DvParsable) adaptee).getCharset() != null)
            map.put("charset", ((DvParsable) adaptee).getCharset().toString());
        else
            map.put("charset", "");

        if (((DvParsable) adaptee).getLanguage() != null)
            map.put("language", ((DvParsable) adaptee).getLanguage().toString());
        else
            map.put("language", "");

        map.put("formalism", ((DvParsable) adaptee).getFormalism());
        return map;
    }

    @Override
    public DvParsable parse(String value, String... defaults) {
//        adaptee = ((DvParsable)adaptee).parse(value);
        adaptee = new DvParsable(value, "formalism");
        return (DvParsable)adaptee;
    }

    public static DvParsable getInstance(Map<String, Object> attributes) {
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvParsable) return (DvParsable)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;

            String actualValue = (String)valueMap.get("value");
            String formalism = (String)valueMap.get("formalism");

            return new DvParsable(actualValue, formalism);
        }
        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvParsable generate(){
        return new DvParsable("dummy", "formalism");
    }
}
