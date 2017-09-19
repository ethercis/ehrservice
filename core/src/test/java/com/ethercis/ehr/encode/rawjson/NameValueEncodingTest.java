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

package com.ethercis.ehr.encode.rawjson;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by christian on 6/21/2017.
 */
public class NameValueEncodingTest {

    @Test
    public void testMake(){
        String testRawJson =
                "{\"name\":[{\"value\":\"Laboratory test panel\"}]," +
                        "\"items\":" +
                        "   [{\"name\":[{\"value\":\"Laboratory result\"}]," +
                        "\"items\":[{\"@class\":\"ELEMENT\"," +
                        "   \"archetype_node_id\":\"at0003\"," +
                        "   \"name\":{\"@class\":\"DV_TEXT\",\"value\":\"Comment\"}," +
                        "   \"value\":{\"@class\":\"DV_TEXT\",\"value\":\"this is free text\"}}," +
                        "   {\"@class\":\"ELEMENT\"," +
                        "   \"archetype_node_id\":\"at0001\"," +
                        "   \"name\":{\"@class\":\"DV_TEXT\",\"value\":\"Urea\"}," +
                        "   \"value\":{\"@class\":\"DV_QUANTITY\",\"magnitude\":3.6,\"units\":\"mmol/l\"}}]}," +
                        "   {\"name\":[{\"value\":\"Laboratory result #2\"}]," +
                        "   \"items\":[{\"@class\":\"ELEMENT\"," +
                        "   \"archetype_node_id\":\"at0001\"," +
                        "   \"name\":{\"@class\":\"DV_TEXT\",\"value\":\"Creatinine\"}," +
                        "   \"value\":{\"@class\":\"DV_QUANTITY\",\"magnitude\":97.0,\"units\":\"mmol/l\"}}]}," +
                        "   {\"name\":[{\"value\":\"Laboratory result #3\"}]," +
                        "   \"items\":[{\"@class\":\"ELEMENT\"," +
                        "   \"archetype_node_id\":\"at0001\"," +
                        "   \"name\":{\"@class\":\"DV_TEXT\",\"value\":\"Sodium\"}," +
                        "   \"value\":{\"@class\":\"DV_QUANTITY\",\"magnitude\":122.0,\"units\":\"mmol/l\"}}]}," +
                        "   {\"name\":[{\"value\":\"Laboratory result #4\"}]," +
                        "   \"items\":[{\"@class\":\"ELEMENT\",\"archetype_node_id\":\"at0001\"," +
                        "   \"name\":{\"@class\":\"DV_TEXT\",\"value\":\"Potassium\"}," +
                        "   \"value\":{\"@class\":\"DV_QUANTITY\"," +
                        "   \"magnitude\":6.1,\"units\":\"mmol/l\"}}]}]}";

        String made = new NameValueEncoding(testRawJson).make();
    }

}