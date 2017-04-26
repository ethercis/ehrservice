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

package com.ethercis.ehr.building;

import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.composition.Composition;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class SerializerDebug extends TestCase {
    //	ClusterController controller;
    I_KnowledgeCache knowledge;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);
    }

    @Test
    public void testBuildFromJson() throws Exception {
//        String templateId = "IDCR - Laboratory Order.v0";
//        String templateId = "IDCR - Laboratory Test Report.v0";
//        String templateId = "IDCR - Laboratory Test Report.v0";
        String templateId = "COLNEC Medication Action";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        StringBuffer sb = new StringBuffer();
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/ProblemList_2FLAT.json")).forEach(line -> sb.append(line));
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/Laboratory_Order_faulty.json")).forEach(line -> sb.append(line));
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/test/a601a3df-cfea-4cb8-9b82-5737522b52c4.db.json")).forEach(line -> sb.append(line));
        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/test/encoded_action.db.json")).forEach(line -> sb.append(line));
//        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "IDCR Problem List.v1");
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);

        Composition newComposition = content.buildCompositionFromJson(sb.toString());

        assertNotNull(newComposition);

        Composition newComposition2 = content.buildCompositionFromJson(sb.toString());

        //=================== FLAT JSON ==========================
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);
        Map<String, Object> map = jsonCompositionConverter.fromComposition(templateId, newComposition);

        assertNotNull(map);
        //==========================================================================================

        String xml = new String(content.exportCanonicalXML(newComposition, true));

        System.out.println(xml);

        Map<String, String> testRetMap = new EcisFlattener().render(newComposition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);

    }

}