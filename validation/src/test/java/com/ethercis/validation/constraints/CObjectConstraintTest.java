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

package com.ethercis.validation.constraints;

import com.ethercis.validation.wrappers.CArchetypeConstraint;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.schemas.v1.*;
import org.openehr.terminology.SimpleTerminologyService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by christian on 7/18/2016.
 */
public class CObjectConstraintTest {

//    private ElementWrapper wrapDataValue(String typeName, Object... keyvalues) throws Exception {
//
//        Map<String, Object> attributes = new HashMap<>();
//
//        String key = null;
//        for (int i = 0;i < keyvalues.length;  i++){
//            if ((i % 2) == 0){
//                key = (String)keyvalues[i];
//            }
//            else {
//                if (key == null)
//                    throw new IllegalArgumentException("Unbalanced key value pairs in DataValue attributes");
//                attributes.put(key, keyvalues[i]);
//                key = null;
//            }
//        }
//
//        //generate an example for typeName
//        Class clazz = VBeanUtil.findInstrumentalizedClass(typeName);
//        Map<String, Object> valueMap = new HashMap<>();
//        valueMap.put(CompositionSerializer.TAG_VALUE, attributes);
//        DataValue instance = (DataValue)VBeanUtil.getInstance(clazz, valueMap);
//
//
//        Element element = new Element("at0000", "place_holder", instance);
//        I_VBeanWrapper wrapped = (I_VBeanWrapper) VBeanUtil.wrapObject(instance);
//        ElementWrapper elementWrapper = new ElementWrapper(element, null);
//        elementWrapper.setWrappedValue(wrapped);
//        elementWrapper.setDirtyBit(true);
//
//        return elementWrapper;
//    }

    //@Test
    public void testValidateElement() throws Exception {
        String elementConstraint =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>ELEMENT</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>0</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id>at0004</v1:node_id>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>name</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_COMPLEX_OBJECT\">\n" +
                        "      <v1:rm_type_name>DV_CODED_TEXT</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "        <v1:rm_attribute_name>defining_code</v1:rm_attribute_name>\n" +
                        "        <v1:existence>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:upper_included>true</v1:upper_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "          <v1:lower>1</v1:lower>\n" +
                        "          <v1:upper>1</v1:upper>\n" +
                        "        </v1:existence>\n" +
                        "        <v1:children xsi:type=\"C_CODE_PHRASE\">\n" +
                        "          <v1:rm_type_name>CODE_PHRASE</v1:rm_type_name>\n" +
                        "          <v1:occurrences>\n" +
                        "            <v1:lower_included>true</v1:lower_included>\n" +
                        "            <v1:upper_included>true</v1:upper_included>\n" +
                        "            <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "            <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "            <v1:lower>1</v1:lower>\n" +
                        "            <v1:upper>1</v1:upper>\n" +
                        "          </v1:occurrences>\n" +
                        "          <v1:node_id/>\n" +
                        "          <v1:terminology_id>\n" +
                        "            <v1:value>local</v1:value>\n" +
                        "          </v1:terminology_id>\n" +
                        "          <v1:code_list>at1026</v1:code_list>\n" +
                        "          <v1:code_list>at1027</v1:code_list>\n" +
                        "        </v1:children>\n" +
                        "      </v1:attributes>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>value</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_DV_QUANTITY\">\n" +
                        "      <v1:rm_type_name>DV_QUANTITY</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:property>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>openehr</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>382</v1:code_string>\n" +
                        "      </v1:property>\n" +
                        "      <v1:list>\n" +
                        "        <v1:magnitude>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>true</v1:upper_unbounded>\n" +
                        "          <v1:lower>0</v1:lower>\n" +
                        "        </v1:magnitude>\n" +
                        "        <v1:precision>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:upper_included>true</v1:upper_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "          <v1:lower>0</v1:lower>\n" +
                        "          <v1:upper>0</v1:upper>\n" +
                        "        </v1:precision>\n" +
                        "        <v1:units>/min</v1:units>\n" +
                        "      </v1:list>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        CCOMPLEXOBJECT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(elementConstraint);
        assertNotNull(archetypeconstraint);
        DvCodedText dvCodedText = new DvCodedText("myName", "local", "at1026");
        DvQuantity dvQuantity = new DvQuantity("/min", 120D, 0);
        Element element = new Element("test", dvCodedText, dvQuantity);

        new CArchetypeConstraint(null).validate("test", element, archetypeconstraint);
    }

    @Test
    public void testDTDvQuantity() throws Exception {
        String CDvQuantity =
                "<xml-fragment xsi:type=\"C_DV_QUANTITY\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_QUANTITY</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:property>\n" +
                        "    <v1:terminology_id>\n" +
                        "      <v1:value>openehr</v1:value>\n" +
                        "    </v1:terminology_id>\n" +
                        "    <v1:code_string>382</v1:code_string>\n" +
                        "  </v1:property>\n" +
                        "  <v1:list>\n" +
                        "    <v1:magnitude>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>true</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "    </v1:magnitude>\n" +
                        "    <v1:precision>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>0</v1:upper>\n" +
                        "    </v1:precision>\n" +
                        "    <v1:units>/min</v1:units>\n" +
                        "  </v1:list>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CDVQUANTITY.Factory.parse(CDvQuantity);
        assertNotNull(archetypeconstraint);
        DvQuantity dvQuantity = new DvQuantity("/min", 120D, 0);

        new CArchetypeConstraint(null).validate("test", dvQuantity, archetypeconstraint);
    }

    //@Test
    public void testCCDvCodedText() throws Exception {
        String CDvCodedText =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_CODED_TEXT</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>defining_code</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_CODE_PHRASE\">\n" +
                        "      <v1:rm_type_name>CODE_PHRASE</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:terminology_id>\n" +
                        "        <v1:value>local</v1:value>\n" +
                        "      </v1:terminology_id>\n" +
                        "      <v1:code_list>at0006</v1:code_list>\n" +
                        "      <v1:code_list>at1028</v1:code_list>\n" +
                        "      <v1:code_list>at0007</v1:code_list>\n" +
                        "      <v1:code_list>at0008</v1:code_list>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(CDvCodedText);
        assertNotNull(archetypeconstraint);
        CodePhrase codePhrase = new CodePhrase("local", "at0008");
        DvCodedText dvCodedText = new DvCodedText("1234", codePhrase);

        new CArchetypeConstraint(null).validate("test", dvCodedText, archetypeconstraint);
    }

    @Test
    public void testCCDvIdentifier() throws Exception {
        String cdvIdentifier =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_IDENTIFIER</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvIdentifier);
        assertNotNull(archetypeconstraint);
        DvIdentifier identifier = new DvIdentifier("dummy", "dummy", "1234", "dummy");

        new CArchetypeConstraint(null).validate("test", identifier, archetypeconstraint);
    }

    @Test
    public void testCCDvDateTime() throws Exception {
        String cdvDateTime =
        "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
        "  <v1:rm_type_name>DV_DATE_TIME</v1:rm_type_name>\n" +
        "  <v1:occurrences>\n" +
        "    <v1:lower_included>true</v1:lower_included>\n" +
        "    <v1:upper_included>true</v1:upper_included>\n" +
        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
        "    <v1:lower>1</v1:lower>\n" +
        "    <v1:upper>1</v1:upper>\n" +
        "  </v1:occurrences>\n" +
        "  <v1:node_id/>\n" +
        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
        "    <v1:rm_attribute_name>value</v1:rm_attribute_name>\n" +
        "    <v1:existence>\n" +
        "      <v1:lower_included>true</v1:lower_included>\n" +
        "      <v1:upper_included>true</v1:upper_included>\n" +
        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
        "      <v1:lower>1</v1:lower>\n" +
        "      <v1:upper>1</v1:upper>\n" +
        "    </v1:existence>\n" +
        "    <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
        "      <v1:rm_type_name>DATE_TIME</v1:rm_type_name>\n" +
        "      <v1:occurrences>\n" +
        "        <v1:lower_included>true</v1:lower_included>\n" +
        "        <v1:upper_included>true</v1:upper_included>\n" +
        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
        "        <v1:lower>1</v1:lower>\n" +
        "        <v1:upper>1</v1:upper>\n" +
        "      </v1:occurrences>\n" +
        "      <v1:node_id/>\n" +
        "      <v1:item xsi:type=\"C_DATE_TIME\">\n" +
        "        <v1:pattern>yyyy-??-??T??:??:??</v1:pattern>\n" +
        "      </v1:item>\n" +
        "    </v1:children>\n" +
        "  </v1:attributes>\n" +
        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvDateTime);
        assertNotNull(archetypeconstraint);
        DvDateTime dateTime = new DvDateTime(12,12,12, 11, null);
//        ElementWrapper elementWrapper = wrapDataValue("DvCodedText", "value", "1234", "definingCode", codePhrase);

        new CArchetypeConstraint(null).validate("test", dateTime, archetypeconstraint);
    }

    @Test
    public void testCCDvBoolean() throws Exception {
        String cdvBoolean =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_BOOLEAN</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>value</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "      <v1:rm_type_name>BOOLEAN</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:item xsi:type=\"C_BOOLEAN\">\n" +
                        "        <v1:true_valid>true</v1:true_valid>\n" +
                        "        <v1:false_valid>false</v1:false_valid>\n" +
                        "      </v1:item>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvBoolean);
        assertNotNull(archetypeconstraint);
        DvBoolean dvBoolean = new DvBoolean(false);

        try {
            new CArchetypeConstraint(null).validate("test", dvBoolean, archetypeconstraint);
            fail("false is not allowed");
        } catch (Exception e){
        }
    }

    @Test
    public void testCCDvIntervalDvQuantity() throws Exception {
        String cdvInterval =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_INTERVAL&lt;DV_QUANTITY></v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>upper</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_DV_QUANTITY\">\n" +
                        "      <v1:rm_type_name>DV_QUANTITY</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:property>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>openehr</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>122</v1:code_string>\n" +
                        "      </v1:property>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>cm</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>m</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>in</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>ft</v1:units>\n" +
                        "      </v1:list>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>lower</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_DV_QUANTITY\">\n" +
                        "      <v1:rm_type_name>DV_QUANTITY</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:property>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>openehr</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>122</v1:code_string>\n" +
                        "      </v1:property>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>cm</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>m</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>in</v1:units>\n" +
                        "      </v1:list>\n" +
                        "      <v1:list>\n" +
                        "        <v1:units>ft</v1:units>\n" +
                        "      </v1:list>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvInterval);
        assertNotNull(archetypeconstraint);
        DvInterval<DvQuantity> dvQuantityDvInterval = new DvInterval<>(new DvQuantity("in", 100D, 2), new DvQuantity("in", 200D, 2));

        new CArchetypeConstraint(null).validate("test", dvQuantityDvInterval, archetypeconstraint);
    }

    @Test
    public void testCCDvCount() throws Exception {
        String cdvCount =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_COUNT</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>magnitude</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "      <v1:rm_type_name>INTEGER</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:item xsi:type=\"C_INTEGER\">\n" +
                        "        <v1:range>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>true</v1:upper_unbounded>\n" +
                        "          <v1:lower>0</v1:lower>\n" +
                        "        </v1:range>\n" +
                        "      </v1:item>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvCount);
        assertNotNull(archetypeconstraint);
        DvCount count = new DvCount(10);

        new CArchetypeConstraint(null).validate("test", count, archetypeconstraint);
    }

    @Test
    public void testCCDvIntervalDvCount() throws Exception {
        String cdvInterval =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_INTERVAL&lt;DV_COUNT></v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>upper</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_COMPLEX_OBJECT\">\n" +
                        "      <v1:rm_type_name>DV_COUNT</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>lower</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_COMPLEX_OBJECT\">\n" +
                        "      <v1:rm_type_name>DV_COUNT</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";
        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvInterval);
        assertNotNull(archetypeconstraint);
        DvInterval<DvCount> dvIntervalDvCount = new DvInterval<>(new DvCount(10), new DvCount(20));

        new CArchetypeConstraint(null).validate("test", dvIntervalDvCount, archetypeconstraint);
    }

    @Test
    public void testCCDvProportion() throws Exception {
        String cdvProportion =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_PROPORTION</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>is_integral</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "      <v1:rm_type_name>BOOLEAN</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:item xsi:type=\"C_BOOLEAN\">\n" +
                        "        <v1:true_valid>true</v1:true_valid>\n" +
                        "        <v1:false_valid>true</v1:false_valid>\n" +
                        "      </v1:item>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>type</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "      <v1:rm_type_name>INTEGER</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:item xsi:type=\"C_INTEGER\">\n" +
                        "        <v1:list>0</v1:list>\n" +
                        "        <v1:list>2</v1:list>\n" +
                        "        <v1:list>3</v1:list>\n" +
                        "        <v1:list>4</v1:list>\n" +
                        "      </v1:item>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvProportion);
        assertNotNull(archetypeconstraint);
        DvProportion proportion = new DvProportion(5.4, 10.4, ProportionKind.RATIO, 2);

        new CArchetypeConstraint(null).validate("test", proportion, archetypeconstraint);
    }

    @Test
    public void testCCDvintervalDvDateTime() throws Exception {
        String cdvInterval =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_INTERVAL&lt;DV_DATE_TIME></v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>upper</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_COMPLEX_OBJECT\">\n" +
                        "      <v1:rm_type_name>DV_DATE_TIME</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "        <v1:rm_attribute_name>value</v1:rm_attribute_name>\n" +
                        "        <v1:existence>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:upper_included>true</v1:upper_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "          <v1:lower>1</v1:lower>\n" +
                        "          <v1:upper>1</v1:upper>\n" +
                        "        </v1:existence>\n" +
                        "        <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "          <v1:rm_type_name>DATE_TIME</v1:rm_type_name>\n" +
                        "          <v1:occurrences>\n" +
                        "            <v1:lower_included>true</v1:lower_included>\n" +
                        "            <v1:upper_included>true</v1:upper_included>\n" +
                        "            <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "            <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "            <v1:lower>1</v1:lower>\n" +
                        "            <v1:upper>1</v1:upper>\n" +
                        "          </v1:occurrences>\n" +
                        "          <v1:node_id/>\n" +
                        "          <v1:item xsi:type=\"C_DATE_TIME\">\n" +
                        "            <v1:pattern>yyyy-??-??T??:??:??</v1:pattern>\n" +
                        "          </v1:item>\n" +
                        "        </v1:children>\n" +
                        "      </v1:attributes>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>lower</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_COMPLEX_OBJECT\">\n" +
                        "      <v1:rm_type_name>DV_DATE_TIME</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "        <v1:rm_attribute_name>value</v1:rm_attribute_name>\n" +
                        "        <v1:existence>\n" +
                        "          <v1:lower_included>true</v1:lower_included>\n" +
                        "          <v1:upper_included>true</v1:upper_included>\n" +
                        "          <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "          <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "          <v1:lower>1</v1:lower>\n" +
                        "          <v1:upper>1</v1:upper>\n" +
                        "        </v1:existence>\n" +
                        "        <v1:children xsi:type=\"C_PRIMITIVE_OBJECT\">\n" +
                        "          <v1:rm_type_name>DATE_TIME</v1:rm_type_name>\n" +
                        "          <v1:occurrences>\n" +
                        "            <v1:lower_included>true</v1:lower_included>\n" +
                        "            <v1:upper_included>true</v1:upper_included>\n" +
                        "            <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "            <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "            <v1:lower>1</v1:lower>\n" +
                        "            <v1:upper>1</v1:upper>\n" +
                        "          </v1:occurrences>\n" +
                        "          <v1:node_id/>\n" +
                        "          <v1:item xsi:type=\"C_DATE_TIME\">\n" +
                        "            <v1:pattern>yyyy-??-??T??:??:??</v1:pattern>\n" +
                        "          </v1:item>\n" +
                        "        </v1:children>\n" +
                        "      </v1:attributes>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvInterval);
        assertNotNull(archetypeconstraint);
        DvInterval<DvDateTime> dvDateTimeDvInterval = new DvInterval<>(new DvDateTime(DateTime.now().toString()), null);

        new CArchetypeConstraint(null).validate("test", dvDateTimeDvInterval, archetypeconstraint);
    }

    @Test
    public void testCCDvDuration() throws Exception {
        String cdvDuration =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_DURATION</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvDuration);
        assertNotNull(archetypeconstraint);
        DvDuration dvDuration = new DvDuration(1, 1, 1, 1, 1, 1, 1, 0);

        new CArchetypeConstraint(null).validate("test", dvDuration, archetypeconstraint);
    }

    @Test
    public void testDTDvOrdinal() throws Exception {
        String cdvOrdinal =
                "<xml-fragment xsi:type=\"C_DV_ORDINAL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_ORDINAL</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>0</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0038</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>1</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0039</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>2</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0040</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>5</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0041</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>9</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0042</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "  <v1:list>\n" +
                        "    <v1:value>10</v1:value>\n" +
                        "    <v1:symbol>\n" +
                        "      <v1:value/>\n" +
                        "      <v1:defining_code>\n" +
                        "        <v1:terminology_id>\n" +
                        "          <v1:value>local</v1:value>\n" +
                        "        </v1:terminology_id>\n" +
                        "        <v1:code_string>at0043</v1:code_string>\n" +
                        "      </v1:defining_code>\n" +
                        "    </v1:symbol>\n" +
                        "  </v1:list>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvOrdinal);
        assertNotNull(archetypeconstraint);
        DvCodedText symbol  = new DvCodedText("blah", "local", "at0040");
        DvOrdinal dvOrdinal = new DvOrdinal(2, symbol);

        new CArchetypeConstraint(null).validate("test", dvOrdinal, archetypeconstraint);
    }

    @Test
    public void testCCDvMultimedia() throws Exception {
        String cdvMultimedia =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_MULTIMEDIA</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "  <v1:attributes xsi:type=\"C_SINGLE_ATTRIBUTE\">\n" +
                        "    <v1:rm_attribute_name>media_type</v1:rm_attribute_name>\n" +
                        "    <v1:existence>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>1</v1:lower>\n" +
                        "      <v1:upper>1</v1:upper>\n" +
                        "    </v1:existence>\n" +
                        "    <v1:children xsi:type=\"C_CODE_PHRASE\">\n" +
                        "      <v1:rm_type_name>CODE_PHRASE</v1:rm_type_name>\n" +
                        "      <v1:occurrences>\n" +
                        "        <v1:lower_included>true</v1:lower_included>\n" +
                        "        <v1:upper_included>true</v1:upper_included>\n" +
                        "        <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "        <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "        <v1:lower>1</v1:lower>\n" +
                        "        <v1:upper>1</v1:upper>\n" +
                        "      </v1:occurrences>\n" +
                        "      <v1:node_id/>\n" +
                        "      <v1:terminology_id>\n" +
                        "        <v1:value>IANA_media-types</v1:value>\n" +
                        "      </v1:terminology_id>\n" +
                        "      <v1:code_list>audio/DVI4</v1:code_list>\n" +
                        "      <v1:code_list>audio/G722</v1:code_list>\n" +
                        "      <v1:code_list>audio/G723</v1:code_list>\n" +
                        "      <v1:code_list>audio/G726-16</v1:code_list>\n" +
                        "      <v1:code_list>audio/G726-24</v1:code_list>\n" +
                        "      <v1:code_list>audio/G726-32</v1:code_list>\n" +
                        "      <v1:code_list>audio/G726-40</v1:code_list>\n" +
                        "      <v1:code_list>audio/G728</v1:code_list>\n" +
                        "      <v1:code_list>audio/L8</v1:code_list>\n" +
                        "      <v1:code_list>audio/L16</v1:code_list>\n" +
                        "      <v1:code_list>audio/LPC</v1:code_list>\n" +
                        "      <v1:code_list>audio/G729</v1:code_list>\n" +
                        "      <v1:code_list>audio/G729D</v1:code_list>\n" +
                        "      <v1:code_list>audio/G729E</v1:code_list>\n" +
                        "      <v1:code_list>audio/mpeg</v1:code_list>\n" +
                        "      <v1:code_list>audio/mpeg4-generic</v1:code_list>\n" +
                        "      <v1:code_list>audio/L20</v1:code_list>\n" +
                        "      <v1:code_list>audio/L24</v1:code_list>\n" +
                        "      <v1:code_list>audio/telephone-event</v1:code_list>\n" +
                        "      <v1:code_list>image/cgm</v1:code_list>\n" +
                        "      <v1:code_list>image/gif</v1:code_list>\n" +
                        "      <v1:code_list>image/png</v1:code_list>\n" +
                        "      <v1:code_list>image/tiff</v1:code_list>\n" +
                        "      <v1:code_list>image/jpeg</v1:code_list>\n" +
                        "      <v1:code_list>text/calendar</v1:code_list>\n" +
                        "      <v1:code_list>text/directory</v1:code_list>\n" +
                        "      <v1:code_list>text/html</v1:code_list>\n" +
                        "      <v1:code_list>text/plain</v1:code_list>\n" +
                        "      <v1:code_list>text/rtf</v1:code_list>\n" +
                        "      <v1:code_list>text/sgml</v1:code_list>\n" +
                        "      <v1:code_list>text/tab-separated-values</v1:code_list>\n" +
                        "      <v1:code_list>text/uri-list</v1:code_list>\n" +
                        "      <v1:code_list>text/xml</v1:code_list>\n" +
                        "      <v1:code_list>text/xml-external-parsed-entity</v1:code_list>\n" +
                        "      <v1:code_list>video/BT656</v1:code_list>\n" +
                        "      <v1:code_list>video/CelB</v1:code_list>\n" +
                        "      <v1:code_list>video/H261</v1:code_list>\n" +
                        "      <v1:code_list>video/H263</v1:code_list>\n" +
                        "      <v1:code_list>video/H263-1998</v1:code_list>\n" +
                        "      <v1:code_list>video/H263-2000</v1:code_list>\n" +
                        "      <v1:code_list>video/quicktime</v1:code_list>\n" +
                        "      <v1:code_list>application/msword</v1:code_list>\n" +
                        "      <v1:code_list>application/pdf</v1:code_list>\n" +
                        "      <v1:code_list>application/rtf</v1:code_list>\n" +
                        "      <v1:code_list>application/dicom</v1:code_list>\n" +
                        "    </v1:children>\n" +
                        "  </v1:attributes>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvMultimedia);
        assertNotNull(archetypeconstraint);
        CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
        CodePhrase language = new CodePhrase("ISO_639-1", "en");
        String alternateText = "alternative text";
        CodePhrase mediaType = new CodePhrase("IANA_media-types", "text/plain");
        CodePhrase compressionAlgorithm = new CodePhrase("openehr_compression_algorithms", "other");
        //byte[] integrityCheck = new byte[0];
        CodePhrase integrityCheckAlgorithm = new CodePhrase("openehr_integrity_check_algorithms", "SHA-1");
        DvMultimedia thumbnail = null;
        DvURI uri = new DvURI("www.iana.org");
        //byte[] data = new byte[0];
        TerminologyService terminologyService;
        try {
            terminologyService = SimpleTerminologyService.getInstance();
        } catch (Exception e){
            throw new IllegalArgumentException("Could not instantiate terminology service:"+e);
        }

        DvMultimedia multimedia = new DvMultimedia(charset, language, alternateText,
                mediaType, compressionAlgorithm, null,
                integrityCheckAlgorithm, thumbnail, uri, null, terminologyService);


        new CArchetypeConstraint(null).validate("test", multimedia, archetypeconstraint);
    }


    @Test
    public void testCCDvURI() throws Exception {
        String cdvURI =
                "<xml-fragment xsi:type=\"C_COMPLEX_OBJECT\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
                        "  <v1:rm_type_name>DV_URI</v1:rm_type_name>\n" +
                        "  <v1:occurrences>\n" +
                        "    <v1:lower_included>true</v1:lower_included>\n" +
                        "    <v1:upper_included>true</v1:upper_included>\n" +
                        "    <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "    <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "    <v1:lower>1</v1:lower>\n" +
                        "    <v1:upper>1</v1:upper>\n" +
                        "  </v1:occurrences>\n" +
                        "  <v1:node_id/>\n" +
                        "</xml-fragment>";

        ARCHETYPECONSTRAINT archetypeconstraint = CCOMPLEXOBJECT.Factory.parse(cdvURI);
        assertNotNull(archetypeconstraint);
        DvURI uri = new DvURI("http://java.sun.com/j2se/1.3/");

        new CArchetypeConstraint(null).validate("test", uri, archetypeconstraint);
    }

}