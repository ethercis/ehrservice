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


import com.ethercis.validation.Utils;
import org.junit.Test;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.schemas.v1.ARCHETYPECONSTRAINT;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by christian on 7/20/2016.
 */
public class DvQuantityVConstraintsTest {

    @Test
    public void testValidate() throws Exception {
        String cdvQuantity =
                "<xml-fragment xsi:type=\"C_DV_QUANTITY\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:v1=\"http://schemas.openehr.org/v1\">\n" +
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
                        "  <v1:list>\n" +
                        "    <v1:magnitude>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>true</v1:upper_unbounded>\n" +
                        "      <v1:lower>10</v1:lower>\n" +
                        "      <v1:upper>20</v1:upper>\n" +
                        "    </v1:magnitude>\n" +
                        "    <v1:precision>\n" +
                        "      <v1:lower_included>true</v1:lower_included>\n" +
                        "      <v1:upper_included>true</v1:upper_included>\n" +
                        "      <v1:lower_unbounded>false</v1:lower_unbounded>\n" +
                        "      <v1:upper_unbounded>false</v1:upper_unbounded>\n" +
                        "      <v1:lower>0</v1:lower>\n" +
                        "      <v1:upper>0</v1:upper>\n" +
                        "    </v1:precision>\n" +
                        "    <v1:units>/kg</v1:units>\n" +
                        "  </v1:list>\n" +
                        "    </xml-fragment>";

//        CDVQUANTITY cdvquantity = CDVQUANTITY.Factory.parse(cdvQuantity);
        ARCHETYPECONSTRAINT cdvquantity = ARCHETYPECONSTRAINT.Factory.parse(cdvQuantity);
        assertNotNull(cdvquantity);
//        DvQuantityVConstraints.validate("test", new DvQuantity("/min", 0D, 0), cdvquantity);
        Utils.constraintValidate("test", new DvQuantity("/min", -10D, 0), cdvquantity);
        assertTrue(true);
    }
}