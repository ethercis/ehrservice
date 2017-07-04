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

package com.ethercis.validation.wrappers;

import com.ethercis.validation.Utils;
import org.apache.commons.lang.WordUtils;
import org.apache.xmlbeans.SchemaType;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.schemas.v1.*;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by christian on 7/23/2016.
 */
public class CAttribute extends CConstraint  implements I_CArchetypeConstraintValidate{

    boolean isAttributeResolved = false; //true if a getter or function has been found

    protected CAttribute(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, ARCHETYPECONSTRAINT archetypeconstraint) throws Exception {
//        if (!(archetypeconstraint instanceof CATTRIBUTE))
//            throw new IllegalArgumentException("INTERNAL: constraint is not a C_ATTRIBUTE");

        SchemaType type = I_CArchetypeConstraintValidate.findSchemaType(I_CArchetypeConstraintValidate.getXmlType(archetypeconstraint));

        CATTRIBUTE attribute = (CATTRIBUTE) archetypeconstraint.changeType(type);

        if (attribute.getRmAttributeName().equals("defining_code")){
            if (aValue instanceof DvCodedText)
            //process this DvText as a DvCodedText
                new CDvCodedText(localTerminologyLookup).validate(path, aValue, attribute);
            else if (aValue instanceof DvText)
                new CDvText(localTerminologyLookup).validate(path, aValue, attribute);
            return;
        }

        Object value = findAttribute(aValue, attribute.getRmAttributeName());

        if (!isAttributeResolved && value == null) {
            //check for a function
            value = getFunctionValue(aValue, attribute.getRmAttributeName());
            if (!isAttributeResolved)
                ValidationException.raise(path, "The following attribute:" + attribute.getRmAttributeName() + " is expected in object:" + aValue, "ATTR01");
        }

        if (value == null){
            if (IntervalComparator.isOptional(attribute.getExistence()))
                return;
            //resolved but missing
            ValidationException.raise(path, "Mandatory attribute has no value:" + attribute.getRmAttributeName(), "ATTR02");
        }
        //if value is an enum use its actual value
        if (value.getClass().isEnum()){
            value = getEnumValue(value);
        }

        if (attribute instanceof CSINGLEATTRIBUTE) {
            new CSingleAttribute(localTerminologyLookup).validate(path, value, (CSINGLEATTRIBUTE) attribute);
        }
        else if (attribute instanceof CMULTIPLEATTRIBUTE) {
            new CMultipleAttribute(localTerminologyLookup).validate(path, value, (CMULTIPLEATTRIBUTE) attribute);
        }
    }

    private Object findAttribute(Object object, String attribute){
        Object value;
        //locate the attribute to check
        if (object instanceof Locatable){
            return ((Locatable) object).itemAtPath("/"+attribute);
        }
        else if (object instanceof DataValue){
            return getAttributeValue(object, attribute);
        }
        return null;
    }

    private Object getAttributeValue(Object obj, String attribute) {
        Class rmClass = obj.getClass();
        Object value = null;
        Method getter = null;
        String getterName = "get" + Utils.snakeToCamel(attribute);

        try {
            getter = rmClass.getMethod(getterName, null);
            isAttributeResolved = true;
            value = getter.invoke(obj, null);

        } catch(Exception e) {
            isAttributeResolved = false;
        }
        return value;
    }

    private Object getFunctionValue(Object obj, String attribute) {
        Class rmClass = obj.getClass();
        Object value = null;
        Method function = null;
        String functionName = WordUtils.uncapitalize(Utils.snakeToCamel(attribute));

        try {
            function = rmClass.getMethod(functionName, null);
            isAttributeResolved = true;
            value = function.invoke(obj, null);

        } catch(Exception e) {
            isAttributeResolved = false;
        }
        return value;
    }

    private Object getEnumValue(Object obj) {
        Class rmClass = obj.getClass();
        Object value = null;

        try {
            Method getter = rmClass.getMethod("getValue", null);
            value = getter.invoke(obj, null);

        } catch(Exception e) {
            // TODO log as kernel warning
            // e.printStackTrace();
        }
        return value;
    }
}
