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
import org.openehr.rm.support.identification.HierObjectID;

import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/3/2015.
 */
public class HierObjectIDVBean implements I_VBeanWrapper {
    @Override
    public Map<String, Object> getFieldMap() throws Exception {
        return null;
    }

    @Override
    public Object getAdaptee() {
        return null;
    }

    @Override
    public void setAdaptee(Object adaptee) {

    }

    @Override
    public Object parse(String value, String... defaults) {
        return null;
    }

    public static HierObjectID getInstance(Map<String, Object> attributes) {

        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof HierObjectID) return (HierObjectID)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            String actualValue = (String) valueMap.get("value");
            return new HierObjectID(actualValue);
        }
        throw new IllegalArgumentException("Could not get instance");
    }

    public static HierObjectID generate(){
        return new HierObjectID("dummy");
    }
}
