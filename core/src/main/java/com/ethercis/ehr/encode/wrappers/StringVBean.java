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

import java.util.Map;

/**
 * Created by christian on 11/10/2015.
 */
public class StringVBean extends DataValueAdapter implements I_VBeanWrapper {

    public StringVBean(String string) {

        this.adaptee = string;
    }

    @Override
    public Map<String, Object> getFieldMap() throws Exception {
        return null;
    }

    @Override
    public Object parse(String value, String... defaults) throws Exception {
        return value;
    }

    public static String getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof String) return (String)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");
            return actualValue;
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static String generate(){
        return "dummy";
    }


}
