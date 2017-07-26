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

package com.ethercis.transform.rawjson;

import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.util.MapInspector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by christian on 8/30/2016.
 */
public class RawJsonParserTest {

    @Test
    public void testParseRawJson() throws Exception {
//        RawJsonParser rawJsonParser = new RawJsonParser();
        //FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/ehr_status_raw.json");
        final InputStreamReader inputStreamReader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("ehr_status_raw.json"));
        String serialized = RawJsonParser.dbEncode(inputStreamReader);
        Assert.assertNotNull(serialized);
        System.out.println(serialized);

        GsonBuilder gsonBuilder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        Map<String, Object> retmap = gson.fromJson(serialized, TreeMap.class);
        Map<String, String> flatten = new EcisFlattener().generateEcisFlat(retmap);
        Assert.assertNotNull(flatten);
        System.out.println(gson.toJson(flatten, TreeMap.class));
    }

}