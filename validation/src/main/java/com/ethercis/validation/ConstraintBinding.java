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

package com.ethercis.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.am.archetype.constraintmodel.*;
import org.openehr.rm.support.measurement.MeasurementService;
import org.openehr.schemas.v1.*;

import java.util.*;

/**
 * Created by christian on 7/12/2016.
 * Refactored for OetBinding
 */
public class ConstraintBinding {

    Object object; //object in Operational Template associated with constraint
    Logger logger = LogManager.getLogger(ConstraintBinding.class);
    protected MeasurementService measurementService = null;
    ArchetypeConstraint archetypeConstraint;

    public ConstraintBinding(Object o){
        this.object = o;
    }

    public Map<String, ArchetypeConstraint> bind(String path, CCOMPLEXOBJECT ccobj) throws Exception {

        logger.debug("create complex object " + ccobj.getRmTypeName());

        Map<String, Object> valueMap = new HashMap<>();

        String rmTypeName = ccobj.getRmTypeName();
        ConstraintOccurrences occurences = new ConstraintOccurrences(ccobj.getOccurrences());
        List<ArchetypeConstraint> attributes = new ArrayList<>();

        if(ccobj.getAttributesArray() != null && ccobj.getAttributesArray().length > 0) {
            for(CATTRIBUTE cattr : ccobj.getAttributesArray()) {
                attributes.add(bindAttribute(path, cattr));
            }

        }
        return null;
    }

    public ArchetypeConstraint bindAttribute(String path, CATTRIBUTE cattribute){
        if (cattribute instanceof CSINGLEATTRIBUTE){
            CSINGLEATTRIBUTE csingleattributeRef = (CSINGLEATTRIBUTE)cattribute;
            CSingleAttribute cSingleAttribute = new CSingleAttribute(path,
                                                    cattribute.getRmAttributeName(),
                                                    new ConstraintOccurrences(cattribute.getExistence()).getExistence());
            //add the children
            List<ArchetypeConstraint> alternatives = new ArrayList<>();
            for (COBJECT cobject: cattribute.getChildrenArray()){
                //generate the corresponding constraint
                alternatives.add(bindCObject(path, cobject));
            }
            return cSingleAttribute;

        }
        else if (cattribute instanceof CMULTIPLEATTRIBUTE){
            CMULTIPLEATTRIBUTE cmultipleattribute = (CMULTIPLEATTRIBUTE) cattribute;
            CARDINALITY ccardinality = cmultipleattribute.getCardinality();
            Cardinality cardinality = new Cardinality(ccardinality.getIsOrdered(), ccardinality.getIsUnique(), new ConstraintOccurrences(ccardinality.getInterval()).asInterval());
            CMultipleAttribute multipleAttribute = new CMultipleAttribute(
                                                        path,
                                                        cmultipleattribute.getRmAttributeName(),
                                                        new ConstraintOccurrences(cmultipleattribute.getExistence()).getExistence(),
                                                        cardinality,
                                                        null);
            //set children
            return multipleAttribute;
        }

        return null;

    }

    public ArchetypeConstraint bindCObject(String path, COBJECT cobject){
        if (cobject instanceof ARCHETYPESLOT){
            //do something
        }
        else if (cobject instanceof CONSTRAINTREF){
            //possibly do another thing
        }
        else if (cobject instanceof CCOMPLEXOBJECT){
            return bindCComplexObject(path, (CCOMPLEXOBJECT)cobject);
        }
        else if (cobject instanceof CPRIMITIVEOBJECT){
            return bindCPrimitiveObject(path, (CPRIMITIVEOBJECT)cobject);

        }
        else if (cobject instanceof CDOMAINTYPE){
            return bindCDomainType(path, (CDOMAINTYPE)cobject);

        }
        else if (cobject instanceof ARCHETYPEINTERNALREF){

        }
        return null;
    }

    public ArchetypeConstraint bindCComplexObject(String path, CCOMPLEXOBJECT ccomplexobject){
        CComplexObject cComplexObject = new CComplexObject(path, ccomplexobject.getRmTypeName(), new ConstraintOccurrences(ccomplexobject.getOccurrences()).asInterval(), null, null,  null);
        return cComplexObject;
    }

    public ArchetypeConstraint bindCPrimitiveObject(String path, CPRIMITIVEOBJECT ccomplexobject){
        CPrimitiveObject cPrimitiveObject = new CPrimitiveObject(path, new ConstraintOccurrences(ccomplexobject.getOccurrences()).asInterval(), null, null, null);
        return cPrimitiveObject;
    }

    public ArchetypeConstraint bindCDomainType(String path, CDOMAINTYPE ccomplexobject){
        //get the type name
//        String rmTypeName = ccomplexobject.getRmTypeName();
//        String className = Utils.snakeToCamel(rmTypeName);

        return null;
    }

    public Object bindValueConstraint(CComplexObject cComplexObject, CATTRIBUTE cattribute)
            throws Exception {

        logger.debug("create attribute " + cattribute.getRmAttributeName());

        COBJECT[] children = cattribute.getChildrenArray();
        if(cattribute instanceof CSINGLEATTRIBUTE) {

            logger.debug("single attribute..");

            if(children != null && children.length > 0) {
                // TODO first child is used for rm generation
                COBJECT cobj = children[0];
                bindConstraintObject(cComplexObject, cobj);
                return cComplexObject;
            } else {
                throw new Exception ("no child object..");
            }
        } else { // multiple c_attribute

            logger.debug("multiple attribute..");

            CMULTIPLEATTRIBUTE cma = (CMULTIPLEATTRIBUTE) cattribute;
            Collection container = new ArrayList<>();

            for(COBJECT cobj : children) {
                bindConstraintObject(cComplexObject, cobj);
                if(cComplexObject != null) {
                    container.add(cComplexObject);
                }
            }

            // TODO special rule to include first child
            if(container.isEmpty()) {

                logger.debug("add first child for empty container attribute");

                // disabled
                // container.add(bindConstraintObject(children.get(0), archetype));
            }

            return container;
        }

    }

    public void bindConstraintObject(CComplexObject cComplexObject, COBJECT cobj)
            throws Exception {

        logger.debug("create object with constraint " + cobj.getClass());

        if(cobj instanceof CCOMPLEXOBJECT) {
            CCOMPLEXOBJECT ccomplexobject = (CCOMPLEXOBJECT)cobj;
            //get the occurrences (IntervalOfInteger)
            setOccurences(cComplexObject, ccomplexobject);
            setContraintAttributes(cComplexObject, ccomplexobject);

        } else {
            // TODO skip archetype_slot etc, log.warn?
            logger.warn("Unresolved archetype slot, possibly the slot is not filled in the template, ignoring for now:" + cobj);
//            return null;
        }
    }

    private void setContraintAttributes(CComplexObject cComplexObject, CCOMPLEXOBJECT ccomplexobject){
        for (CATTRIBUTE cattribute: ccomplexobject.getAttributesArray()){
            if (cattribute instanceof CSINGLEATTRIBUTE) {
                CAttribute cAttribute = new CSingleAttribute();
                for (COBJECT cobject: ((CSINGLEATTRIBUTE)cattribute).getChildrenArray()){
                    CObject cObject = null;
                    if (cobject instanceof CPRIMITIVEOBJECT){
                        CPRIMITIVE cprimitive = ((CPRIMITIVEOBJECT) cobject).getItem();
                        if (cprimitive instanceof CSTRING){

                        }
                        cObject = new CPrimitiveObject();
                    }
                    else if (cobject instanceof CCOMPLEXOBJECT){
                        cObject = new CComplexObject();

                    }
                    else if (cobject instanceof CDOMAINTYPE){
                        //get type
                        ((CDOMAINTYPE)cobject).getRmTypeName();
                        //instantiate a corresponding specialized domain type
                    }
                    if (cObject != null)
                        cAttribute.addChild(cObject);
                }
            }
            else if (cattribute instanceof CMULTIPLEATTRIBUTE){
                CMultipleAttribute cMultipleAttribute = new CMultipleAttribute();
                CARDINALITY cardinality = ((CMULTIPLEATTRIBUTE) cattribute).getCardinality();
                cardinality.getInterval();

            }
            else
            {

            }
        }
    }

    private void setOccurences(CComplexObject cComplexObject, CCOMPLEXOBJECT ccomplexobject){
        org.openehr.rm.support.basic.Interval<Integer> integerInterval = new org.openehr.rm.support.basic.Interval<>();
        if (ccomplexobject.getOccurrences().isSetLower())
            integerInterval.setLower(ccomplexobject.getOccurrences().getLower());
        else
            integerInterval.setLower(0);

        if (ccomplexobject.getOccurrences().isSetUpper())
            integerInterval.setUpper(ccomplexobject.getOccurrences().getUpper());
        else
            integerInterval.setUpper(Integer.MAX_VALUE);

        integerInterval.setLowerIncluded(ccomplexobject.getOccurrences().getLowerIncluded());
        integerInterval.setUpperIncluded(ccomplexobject.getOccurrences().getUpperIncluded());

        cComplexObject.setOccurrences(integerInterval);
        if (ccomplexobject.getNodeId() != null && ccomplexobject.getNodeId().length() > 0)
            cComplexObject.setNodeId(ccomplexobject.getNodeId());
    }

}
