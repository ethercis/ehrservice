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

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.openehr.schemas.v1.CCOMPLEXOBJECT;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by christian on 7/21/2016.
 */
public class DateTimeConstraintTest {

    @Test
    public void testDateTime() throws Exception {
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

        CCOMPLEXOBJECT ccomplexobject = CCOMPLEXOBJECT.Factory.parse(cdvDateTime);
        assertNotNull(ccomplexobject);

    }
}
