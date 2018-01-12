/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
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

package com.ethercis.ehr.building.util;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openehr.schemas.v1.ARCHETYPECONSTRAINT;

import javax.xml.namespace.QName;

/**
 * Created by christian on 1/12/2018.
 */
public class XmlBeansFix {

    public String getXmlType(XmlObject xmlObject){
        QName qName = new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi");
        XmlAnyTypeImpl attribute = (XmlAnyTypeImpl) xmlObject.selectAttribute(qName);
        if (attribute == null)
            return null;
        String attributeValue = attribute.getStringValue();
        if (attributeValue.contains(":"))
            return attributeValue.split(":")[1];
        else
            return attribute.getStringValue();
    }

    public SchemaType findSchemaType(String name){
        String ns = "http://schemas.openehr.org/v1";
        QName qName = new QName(ns, name);
        return XmlBeans.getContextTypeLoader().findType(qName);
    }
}
