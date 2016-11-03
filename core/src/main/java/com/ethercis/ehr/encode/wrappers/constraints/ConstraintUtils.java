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

package com.ethercis.ehr.encode.wrappers.constraints;

import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.util.LocatableHelper;
import com.ethercis.validation.ConstraintMapper;
import com.ethercis.validation.ConstraintOccurrences;
import com.ethercis.validation.OptConstraintMapper;
import com.ethercis.validation.hardwired.CHistory;
import com.ethercis.validation.wrappers.CArchetypeConstraint;
import com.ethercis.validation.wrappers.IntervalComparator;
import com.ethercis.validation.wrappers.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Mostly here to avoid cyclic dependencies
 * Created by christian on 8/16/2016.
 */
public class ConstraintUtils {

    private boolean lenient = false;
    private ConstraintMapper constraintMapper;
    private Locatable locatable;

    private static Logger log = LogManager.getLogger(ConstraintUtils.class);

    public ConstraintUtils(Boolean lenient, Composition composition, ConstraintMapper constraintMapper){
        this.lenient = lenient;
        this.locatable = composition;
        this.constraintMapper = constraintMapper;
    }

    public ConstraintUtils(Boolean lenient, ItemStructure structure, ConstraintMapper constraintMapper){
        this.lenient = lenient;
        this.locatable = structure;
        this.constraintMapper = constraintMapper;
    }


    public void validateElementConstraints(Locatable locatable) {
        if (lenient) return;

        StringBuffer validationException = new StringBuffer();

        Iterator<Map.Entry<String, ConstraintMapper.ConstraintItem>> iterator = constraintMapper.getElementConstraintIterator();
        while (iterator.hasNext()){
            Map.Entry<String, ConstraintMapper.ConstraintItem> watch = iterator.next();
            String path = watch.getKey();

            Locatable item = (Locatable)locatable.itemAtPath(path);

            boolean isMandatory = ((OptConstraintMapper.OptConstraintItem) watch.getValue()).isMandatory();

            if (item == null && isMandatory){
                validationException.append("Validation error at "+path+", "+"Mandatory element missing, expected:"+((OptConstraintMapper.OptConstraintItem) watch.getValue()).occurrencesToString()+"\n");
            }
            if (item instanceof ElementWrapper) {
                if (watch.getValue() instanceof OptConstraintMapper.OptConstraintItem) {
                    if (isMandatory && !(((ElementWrapper) item).dirtyBitSet()))
                        validationException.append("Validation error at "+path+", "+"Mandatory element missing, expected:"+((OptConstraintMapper.OptConstraintItem) watch.getValue()).occurrencesToString()+"\n");
                }
            }
        }

        if (validationException.length() > 0)
            ValidationException.raise("", validationException.toString(), "ELM01");
    }


    /**
     * check if the parent container is optional
     * TODO: this is not complete and a bit of a hack
     * we assume a container is an archetype slot, we check up to this level to see if
     * the container is optional.
     * What should be done is to check if actually the container is referenced by
     * some values in the children.
     * @param path
     * @return
     */
    public boolean checkIsTransitivelyOptional(String path) {
        //level is not that elegant, but we can check one level up to see if we are optional...
        int count = 0;

        //traverse upward the path and check if a parent node is optional
        List<String> pathSegments = Locatable.dividePathIntoSegments(path);

        for (int i = pathSegments.size() - 1; i >= 0; i--){
            String checkPath = "/"+String.join("/", pathSegments.subList(0, i));
            if (constraintMapper.getOccurrencesMap().containsKey(checkPath)){
                ConstraintOccurrences occurrences = constraintMapper.getOccurrencesMap().get(checkPath);
                if (occurrences.isOptional()) {
                    //check if this optional node contains any datavalue element in its children
                    ElementCounter counter =  new ElementCounter();
                    counter.count(locatable.itemAtPath(checkPath));
                    Integer elementCount = counter.getCount();
                    if (elementCount > 0)
                        return false;
                    else
                        return true;
//                    if (pathSegments.get(i - 1).contains("openEHR-EHR"))
//                        break;
                }
            }
//            if ((i-1 >= 0) && pathSegments.get(i - 1).contains("openEHR-EHR")) //archetype container level, not optional
//                break;
//            if (++count >= level)
//                break;
        }

        // we have tried all parents node
        return true;
    }

    private void checkCardinality(Locatable structure, String path, ConstraintMapper.CardinalityItem cardinalityItem){
        Object locatable = structure.itemAtPath(path);

        ElementCounter counter =  new ElementCounter();
        counter.count(locatable);
        Integer childOccurrence = counter.getCount();

//            if (locatable instanceof List){
//                childOccurrence = ((List)locatable).size();
//            }
//            else
//                childOccurrence = 1;
        try {
            IntervalComparator.isWithinBoundaries(childOccurrence, cardinalityItem.getCardinality());
        } catch (Exception e){
            //check if this is optional (occurence)
            //TODO: check for a transitive optional existence in the path
            if (childOccurrence == 0 && !(cardinalityItem.getExistence().isOptional())){
                //check if a transitive optionality is specified

                if (!checkIsTransitivelyOptional(path))
                    ValidationException.raise(path, "Cardinality not matched, expected:" + IntervalComparator.toString(cardinalityItem.getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01");
            }
            else if (childOccurrence > 0 || !(cardinalityItem.getExistence().isOptional())) {
                ValidationException.raise(path, "Cardinality not matched, expected:" + IntervalComparator.toString(cardinalityItem.getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01");
            }
        }
    }

    private String encodeMessage(String path, String message, String code){
        return (path.isEmpty() ? "" :"Validation error at "+path+", ")+message;
    }

    public String validateCardinality() throws Exception {

        StringBuffer exceptions = new StringBuffer();

        if (lenient) return "";

        if (constraintMapper == null) return "";

        int valcount = 0;

        for (Map.Entry<String, ConstraintMapper.CardinalityItem> entry: constraintMapper.getCardinalityList().entrySet()){
            valcount++;
            //get the corresponding node
            Object item = locatable.itemAtPath(entry.getKey());

            ElementCounter counter =  new ElementCounter();
            counter.count(item);
            Integer childOccurrence = counter.getCount();

//            if (locatable instanceof List){
//                childOccurrence = ((List)locatable).size();
//            }
//            else
//                childOccurrence = 1;
            try {
                IntervalComparator.isWithinBoundaries(childOccurrence, entry.getValue().getCardinality());
            } catch (Exception e){
                //check if this is optional (occurence)
                //TODO: check for a transitive optional existence in the path
                if (childOccurrence == 0 && !(entry.getValue().getExistence().isOptional())){
                    //check if a transitive optionality is specified
                    Object path;
                    if (!checkIsTransitivelyOptional(entry.getKey())) {
                        exceptions.append(encodeMessage(entry.getKey(), "Cardinality not matched, expected:" + IntervalComparator.toString(entry.getValue().getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01"));
//                        ValidationException.raise(entry.getKey(), "Cardinality not matched, expected:" + IntervalComparator.toString(entry.getValue().getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01");
                    }
                }
                else if (childOccurrence > 0 || !(entry.getValue().getExistence().isOptional())) {
                    exceptions.append(encodeMessage(entry.getKey(), "Cardinality not matched, expected:" + IntervalComparator.toString(entry.getValue().getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01"));
//                    ValidationException.raise(entry.getKey(), "Cardinality not matched, expected:" + IntervalComparator.toString(entry.getValue().getCardinality().asInterval()) + ", actual:" + childOccurrence, "CAR01");
                }
            }
        }
//        if (exceptions.length() > 0){
//            ValidationException.raise("", exceptions.toString(), "CAR");
//        }
        log.debug("Validated "+valcount+" cardinality constraints");
        return exceptions.toString();
    }

    public void validateElementWrapper(String path, ElementWrapper referenceElement) throws Exception {
        //validate instantiated object
//        Element referenceElement = ((ElementWrapper)itemAtPath).getAdaptedElement();
//        Element testElement = new Element("*validation*", "test_value", (DataValue)object);
        if (lenient) return;

        ConstraintMapper.ConstraintItem constraint = constraintMapper.getConstraintItem(LocatableHelper.siblingPath(path));
        if (constraint == null){
            String tentativePath = LocatableHelper.simplifyPath(path);
            Object tentativeElement = locatable.itemAtPath(tentativePath);
            if (tentativeElement == null)
                log.debug("No constraint matching element (node could not be identified):"+tentativePath);
            else {
                //we should have an ElementWrapper here...
                if (tentativeElement instanceof ElementWrapper) {
                    constraint = constraintMapper.getConstraintItem(tentativePath);
                    if (constraint == null)
                        log.debug("No constraint matching element:" + tentativeElement);
                    else {
                        if (constraint instanceof OptConstraintMapper.OptConstraintItem) {

                            new CArchetypeConstraint(constraintMapper.getLocalTerminologyLookup()).validate(constraint.getPath(), referenceElement.getAdaptedElement(), ((OptConstraintMapper.OptConstraintItem) constraint).getConstraint());
                        }
                    }
                }
                else
                    log.debug("identified node is not an Element..."+tentativeElement);
            }

        }
        else {
            if (constraint instanceof OptConstraintMapper.OptConstraintItem) {
                new CArchetypeConstraint(constraintMapper.getLocalTerminologyLookup()).validate(constraint.getPath(), referenceElement.getAdaptedElement(), ((OptConstraintMapper.OptConstraintItem) constraint).getConstraint());
            }
        }

    }

    public void validateItem(String path, Object item) throws Exception {
        if (lenient || item == null) return;

        if (item instanceof History)
            new CHistory(constraintMapper).validate(LocatableHelper.simplifyPath(path), item);
        else if (item instanceof ElementWrapper)
            validateElementWrapper(path, (ElementWrapper) item);
        else
            throw new ValidationException(path, "Unhandled specific data type:"+item);
    }

    public String validateElements() throws Exception {
        if (lenient) return "";

        if (constraintMapper == null) return "";

        StringBuffer validationException = new StringBuffer();

        Iterator<Map.Entry<String, ConstraintMapper.ConstraintItem>> iterator = constraintMapper.getElementConstraintIterator();
        int count = 0;
        while (iterator.hasNext()){
            count++;
            //check Cardinality
            Map.Entry<String, ConstraintMapper.ConstraintItem> watch = iterator.next();
            String path = watch.getKey();

            Locatable item = (Locatable)locatable.itemAtPath(path);

            //if null, it has not be assigned potentially (example, unassigned protocol)
            ConstraintMapper.CardinalityItem cardinalityItem = constraintMapper.getCardinalityList().get(path);

            if (item == null) {
                if (((OptConstraintMapper.OptConstraintItem) watch.getValue()).isMandatory()){
                    if (!checkIsTransitivelyOptional(path))
                        validationException.append("Validation error at "+path+", "+"Mandatory element missing, expected:"+((OptConstraintMapper.OptConstraintItem) watch.getValue()).occurrencesToString()+"\n");
                    else
                        continue;
                }
                else
                    continue;
            }

            //get the cardinality if specified
            if (cardinalityItem != null)
                checkCardinality(item, path, cardinalityItem);

            //validate this element
            try {
                if (item instanceof ElementWrapper && !((ElementWrapper)item).dirtyBitSet())
                    continue;
                validateItem(path, item);
            } catch (Exception e){
                validationException.append(encodeMessage(path, e.getMessage(), "")+"\n");
            }
        }

//        if (validationException.length() > 0)
//            ValidationException.raise("", validationException.toString(), "ELM01");
        log.debug("Validated "+count+" elements");
        return validationException.toString();
    }

    public void validateLocatable() throws Exception {
        StringBuffer exceptions = new StringBuffer();
        exceptions.append(validateElements());
        exceptions.append(validateCardinality());

        if (exceptions.length() > 0)
            ValidationException.raise("", exceptions.toString(),"");
        else
            log.debug("Locatable successfully validated");
    }
}
