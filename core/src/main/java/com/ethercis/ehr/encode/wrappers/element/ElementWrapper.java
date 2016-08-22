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
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.rm.common.archetyped.Pathable;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;

import java.util.List;

/**
 * Created by Christian Chevalley on 7/8/2014.
 */
public class ElementWrapper extends Item {

    private I_VBeanWrapper wrappedValue = null;
    private DataValueConstraints constraints = null;
    protected Element adaptedElement;
//    private String valuepath;
//    private boolean isHidden = false;
//    private Integer minOccurrences;
//    private Integer maxOccurrences;
//    private boolean isMinIncluded = true;
//    private boolean isMaxIncluded = true;
//    private boolean isValueRequired = false;
//    private String valueClass;
    private boolean dirtyBit = false; //true if modified
    private CAttribute value_attributes;


    public ElementWrapper(Element element, CComplexObject ccobj) throws Exception {
        this.adaptedElement = element;
//        this.isHidden = ccobj != null ? ccobj.isHiddenOnForm() : false;
//        this.minOccurrences = ccobj != null ? ccobj.getOccurrences().getLower() : 0;
//        this.maxOccurrences = ccobj != null ? ccobj.getOccurrences().getUpper() : Integer.MAX_VALUE;
//        this.isMinIncluded = ccobj != null ? ccobj.getOccurrences().isLowerIncluded() : true;
//        this.isMaxIncluded = ccobj != null ? ccobj.getOccurrences().isUpperIncluded() : true;
//        this.valueClass = adaptedElement.getValue().getClass().getCanonicalName();
//        if (!(adaptee.getValue() instanceof I_VBeanWrapper))
//            throw new Exception("Internal error, non encapsulated data found with type:"+adaptee.getValue().getClass());
//        this.wrapped_value = (I_VBeanWrapper) adaptee.getValue();

//        CAttribute value_attribute = ccobj != null ? ccobj.getAttribute("value") : null;

//        if (value_attribute != null) {
//            this.valuepath = value_attribute.path();
//            this.isValueRequired = value_attribute.isRequired();
//        }

//        this.value_attributes = ccobj != null ? ccobj.getAttribute("value") : null;
        //retrofit name and archetypeNodeId to allow locatable methods.
        this.setArchetypeNodeId(element.getArchetypeNodeId());
        this.setName(element.getName());
    }


    public I_VBeanWrapper getWrappedValue() {
        return wrappedValue;
    }

    public void setWrappedValue(I_VBeanWrapper wrapped_value) {
        this.wrappedValue = wrapped_value;
    }

    public DataValueConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Archetype archetype, DataValueConstraints constraints) {
        this.constraints = constraints;
        //if the constraints is implemented (some types don't have explicit constraints)
        //TODO: check for constraints on multiple value elements
        if (constraints != null)
            constraints.setConstraints(archetype, value_attributes);
    }

    public Element getAdaptedElement() {
        return adaptedElement;
    }

    public static I_VBeanWrapper getAdaptedValue(ElementWrapper elementWrapper){
        I_VBeanWrapper adapted = elementWrapper.getWrappedValue();
        if (adapted == null) { //check for adapted Element instead
            Object dataValue = elementWrapper.getAdaptedElement().getValue();
            if (DataValueAdapter.isValueObject(dataValue)) {
                if (VBeanUtil.isInstrumentalized(dataValue)) {
                    adapted = (I_VBeanWrapper) VBeanUtil.wrapObject(dataValue);
                    elementWrapper.setWrappedValue(adapted);
                }
            }
        }
        return adapted;
    }

    @Override
    public List<Object> itemsAtPath(String path) {
        return null;
    }

    @Override
    public String pathOfItem(Pathable item) {
        return null;
    }

    @Override
    public boolean pathExists(String path) {
        return false;
    }

    @Override
    public boolean pathUnique(String path) {
        return false;
    }

//    public boolean isHidden() {
//        return isHidden;
//    }
//
//    public Integer getMinOccurrences() {
//        return minOccurrences;
//    }
//
//    public Integer getMaxOccurrences() {
//        return maxOccurrences;
//    }
//
//    public boolean isMinIncluded() {
//        return isMinIncluded;
//    }
//
//    public boolean isMaxIncluded() {
//        return isMaxIncluded;
//    }
//
//    public boolean isValueRequired() {
//        return isValueRequired;
//    }

    public boolean dirtyBitSet() {
        return dirtyBit;
    }

    public void setDirtyBit(boolean isChanged) {
        this.dirtyBit = isChanged;
    }
}
