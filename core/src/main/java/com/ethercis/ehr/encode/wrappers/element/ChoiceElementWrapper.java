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

import com.ethercis.ehr.encode.DataValueAdapter;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.RMObjectBuildingException;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.basic.DataValue;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by christian on 11/16/2015.
 */
public class ChoiceElementWrapper extends ElementWrapper {

    private List<String> choiceRmClasses;
//    transient RMObjectBuilder builder;

    public ChoiceElementWrapper(Element element, CComplexObject ccobj, List<String> choiceRmClasses) throws Exception {
        super(element, ccobj);
        this.choiceRmClasses = choiceRmClasses;
//        this.builder = builder;
    }

    public Class getChoice(int i) throws RMObjectBuildingException {
        RMObjectBuilder builder = RMObjectBuilder.getInstance();
        if (i < 0 || i >= choiceRmClasses.size()) {
            throw new RMObjectBuildingException("The choice index ("+(i+1)+") is not within boundaries, max:"+choiceRmClasses.size());
//            return null;
        }

        String rmClass = choiceRmClasses.get(i);

        //get the corresponding class
        Class clazz = builder.retrieveRMType(rmClass);
        return clazz;
    }

    public I_VBeanWrapper getAdaptedValue(String valueToParse){
        //get the index value
        if (valueToParse.charAt(0) != '@')
            throw new IllegalArgumentException("value to parse must begin with character '@'"+valueToParse);

        String intString = valueToParse.substring(1, valueToParse.indexOf("|"));

        Class clazz;
        try {
            Integer index = Integer.parseInt(intString);
            clazz = getChoice(index - 1);
        } catch (Exception e){
            throw new IllegalArgumentException("Could not parse choice index with value:"+intString);
        }

        if (!DataValueAdapter.isValueClass(clazz))
            throw new IllegalArgumentException("invalid value class:"+clazz.getName());

        String instrumentalizedClass = clazz.getSimpleName();

        if (!VBeanUtil.isInstrumentalizedClass(instrumentalizedClass))
            throw new IllegalArgumentException("Not an intrumentalized class:"+instrumentalizedClass);

        //generate a dummy instance to allow parsing
        Class instrument = VBeanUtil.findInstrumentalizedClass(instrumentalizedClass);
        try {
            //TODO: handle properly DvInterval<DvOrdered> f.ex. "@4|20,kg/m2::25,kg/m2"
            Method generate = instrument.getMethod("generate", null);
            DataValue dummy = (DataValue)generate.invoke(null, null);
            DataValue dataValue = dummy.parse(valueToParse.substring(valueToParse.indexOf("|")+1));
            I_VBeanWrapper adapted = (I_VBeanWrapper) VBeanUtil.wrapObject(dataValue);
            setWrappedValue(adapted);
            return adapted;
        }
        catch (Exception e){
            throw new IllegalArgumentException("Could not generate object:"+e);
        }
    }
}
