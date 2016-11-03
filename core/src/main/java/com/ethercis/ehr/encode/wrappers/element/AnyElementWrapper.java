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

package com.ethercis.ehr.encode.wrappers.element;

import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.basic.DataValue;

import java.lang.reflect.Method;

/**
 * Created by christian on 11/16/2015.
 */
public class AnyElementWrapper extends ElementWrapper {

//    transient RMObjectBuilder builder;

    public AnyElementWrapper(Element element, CComplexObject ccobj) throws Exception {
        super(element, ccobj);
//        this.builder = builder;
    }

    public void setAdaptedElement(Element element){
        adaptedElement = element;
    }

    public I_VBeanWrapper getAdaptedValue(String valueToParse) {
        RMObjectBuilder builder = RMObjectBuilder.getInstance();
        //get the index value
        if (valueToParse.charAt(0) != '@')
            throw new IllegalArgumentException("value to parse must begin with character '@'");

        String classString = valueToParse.substring(1, valueToParse.indexOf("|"));
        Class clazz = null;
        try {
            clazz = builder.retrieveRMType(classString);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Could not resolve class:"+classString);
        }

        //generate a dummy instance to allow parsing
        Class instrument = VBeanUtil.findInstrumentalizedClass(clazz.getSimpleName());
        try {
            Method generate = instrument.getMethod("generate", null);
            DataValue dummy = (DataValue) generate.invoke(null, null);
            DataValue dataValue = dummy.parse(valueToParse.substring(valueToParse.indexOf("|") + 1));
            I_VBeanWrapper adapted = (I_VBeanWrapper) VBeanUtil.wrapObject(dataValue);
            setWrappedValue(adapted);
            return adapted;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not generate object:" + e);
        }
    }

}
