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
package com.ethercis.ehr.handler;

import com.ethercis.ehr.building.OetBinding;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_RmBinding;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import openEHR.v1.template.TEMPLATE;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.am.template.FlatteningException;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.basic.DataValue;

/**
 * Created by Christian Chevalley on 7/14/2014.
 */
public class RMStructureHandler {
    private Logger log = LogManager.getLogger(RMStructureHandler.class);
    private Locatable locatable;

    public RMStructureHandler(String templateId, I_KnowledgeCache knowledgeManager) throws Exception {
        this.locatable = newRMStructure(templateId, knowledgeManager);
    }

    public RMStructureHandler(Locatable l) throws Exception {
        this.locatable = l;
    }

    public ElementWrapper getElementWrapperAt(String path){
        Object obj = locatable.itemAtPath(path);

        if (obj == null || !(obj instanceof ElementWrapper) )
            return null;

        return (ElementWrapper)obj;
    }

    public void setValueElementWrapper(ElementWrapper wrapper, Object value) throws Exception {
        if (wrapper == null)
            throw new Exception("invalid Element wrapper (null)");

        if (wrapper.getConstraints() != null && (!(wrapper.getConstraints().validate((DataValue)value))))
        {
            throw new Exception("Value passed is not valid");
        }

        I_VBeanWrapper valueWrapper = wrapper.getWrappedValue();
        if (valueWrapper == null)
            throw new Exception("badly construct ElementWrapper without value...");

        valueWrapper.setAdaptee(value);
    }

    public void setValueElementWrapper(String path, Object value) throws Exception {
        if (!(value instanceof DataValue)){
            throw new Exception("value is not of DataValue type..."+value.getClass().getCanonicalName());
        }

        ElementWrapper wrapper = getElementWrapperAt(path);

        if (wrapper == null)
            throw new Exception("Path does not point to a valid Element wrapper:"+path);

        setValueElementWrapper(wrapper, value);
    }

    public DataValue getValueElementWrapper(String path) throws Exception {
        ElementWrapper wrapper = getElementWrapperAt(path);

        if (wrapper == null)
            throw new Exception("Path does not point to a valid Element wrapper:"+path);

        return (DataValue)wrapper.getWrappedValue().getAdaptee();

    }

    public Object cloneEntry(String path){

        Locatable obj = (Locatable) locatable.itemAtPath(path);

        if (obj == null) return null;

        return null; //TODO: implement cloning method fully...

    }


    public Locatable newRMStructure(String templateId, I_KnowledgeCache knowledge) throws Exception {

        TEMPLATE template;
        try {
            template = knowledge.retrieveOpenehrTemplate(templateId);
        } catch (Exception e){
            throw new Exception("Could not get template with exception:"+e);
        }

        Flattener flattener = new Flattener();
        Archetype instance;
        try {
            instance = flattener.toFlattenedArchetype(template, knowledge.getArchetypeMap());
        }
        catch (FlatteningException fe){
            throw new Exception("badly constructed template: "+fe);
        }

        if (instance == null){
            throw new Exception("Could not construct template: "+template);
        }

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator;
        try {
            generator = I_RmBinding.getInstance();
        } catch (Exception e){
            throw new Exception("could not create generator:"+e);
        }

        Locatable rmobject;

        try {
            rmobject = (Locatable) generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);
        } catch (Exception e){
            throw new Exception("could not create locatable:"+e);
        }


        return rmobject;
    }

}
