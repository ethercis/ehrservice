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

package com.ethercis.encode.test;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import org.junit.Before;
import org.openehr.rm.composition.Composition;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class CompositionEncodeDecodeTest extends TestCase {
    //	ClusterController controller;
    I_KnowledgeCache knowledge;
    static final String FLAT_JSON_INPUT_DIRECTORY = "src/test/resources/flat_json_input";
    static final String TEST_OUTPUT_DIRECTORY = "src/test/resources/test_output";
    static boolean regressionTest = true;
    private String resourcesRootPath;

    @Before
    public void setUp() throws Exception {

        setResourcesRootPath();

        Properties props = new Properties();
        props.put("knowledge.path.archetype", resourcesRootPath + "/knowledge/archetypes");
        props.put("knowledge.path.template", resourcesRootPath + "/knowledge/templates");
        props.put("knowledge.path.opt", resourcesRootPath + "/knowledge/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);
    }

    private void setResourcesRootPath() {
        resourcesRootPath = getClass()
                .getClassLoader()
                .getResource(".")
                .getFile();
    }

    String readTestOutputFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    String formatTestString(String aString) {
        return aString.replaceAll("\\n", "").replaceAll("\\r", "").trim();
    }

    private void testInputFlatJson(String templateId, String flatJsonTestInput, String dbEncodeOutput, String flatJsonTestOutput, String ecisflatJsonTestOutput) throws Exception {
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        String testOutputString;

        FileReader fileReader = new FileReader(FLAT_JSON_INPUT_DIRECTORY + "/" + flatJsonTestInput);

        Map map = FlatJsonUtil.inputStream2Map(fileReader);

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);

        assertNotNull(lastComposition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object> retmap = inspector.process(lastComposition);
        String jsonDbString = jsonMapToString(retmap);

        assertNotNull(jsonDbString);

        System.out.println("TemplateId:" + templateId);

        if (regressionTest) {
            testOutputString = readTestOutputFile(TEST_OUTPUT_DIRECTORY + "/" + dbEncodeOutput);
            assertEquals(formatTestString(testOutputString), formatTestString(jsonDbString));
        }


        //time to rebuild from serialization :)
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        Composition newComposition = content.buildCompositionFromJson(jsonDbString);

        assertNotNull(newComposition);


        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        //=================== FLAT JSON ==========================
        map = jsonCompositionConverter.fromComposition(templateId, newComposition);

        String flatJsonString = gson.toJson(map);

        if (regressionTest) {
            testOutputString = readTestOutputFile(TEST_OUTPUT_DIRECTORY + "/" + flatJsonTestOutput);
            assertEquals(formatTestString(testOutputString), formatTestString(flatJsonString));
        }


        //ECIS FLAT =============================================
        Map<String, String> testRetMap = new EcisFlattener().render(newComposition);

        String ecisFlatJsonString = gson.toJson(testRetMap);


        if (regressionTest) {
            testOutputString = readTestOutputFile(TEST_OUTPUT_DIRECTORY + "/" + ecisflatJsonTestOutput);
            assertEquals(formatTestString(testOutputString), formatTestString(ecisFlatJsonString));
        }

    }

    public void test_COLNEC_Medication() throws Exception {
        testInputFlatJson("COLNEC Medication", "COLNEC_Medication_FLAT.json", "COLNEC Medication.db.json", "COLNEC Medication.flat.json", "COLNEC Medication.ecisflat.json");
    }

    public void test_DiADeM_Assessment_v1() throws Exception {
        testInputFlatJson("DiADeM Assessment.v1", "DiADeM Assessment.v1.flat.json", "DiADeM Assessment.v1.db.json", "DiADeM Assessment.v1.flat.json", "DiADeM Assessment.v1.ecisflat.json");
    }

    public void test_IDCR_Adverse_Reaction_List_v1() throws Exception {
        testInputFlatJson("IDCR - Adverse Reaction List.v1", "IDCR - Adverse Reaction List.v1.flat.json", "IDCR - Adverse Reaction List.v1.db.json", "IDCR - Adverse Reaction List.v1.flat.json", "IDCR - Adverse Reaction List.v1.ecisflat.json");
    }

    public void test_IDCR_Immunisation_summary_v0() throws Exception {
        testInputFlatJson("IDCR - Immunisation summary.v0", "IDCR - Immunisation summary.v0.flat.json", "IDCR - Immunisation summary.v0.db.json", "IDCR - Immunisation summary.v0.flat.json", "IDCR - Immunisation summary.v0.ecisflat.json");
    }

    public void test_IDCR_Laboratory_Order_v0() throws Exception {
        testInputFlatJson("IDCR - Laboratory Order.v0", "IDCR - Laboratory Order.v0.FLAT.json", "IDCR - Laboratory Order.v0.db.json", "IDCR - Laboratory Order.v0.flat.json", "IDCR - Laboratory Order.v0.ecisflat.json");
    }

    public void test_IDCR_Relevant_contacts_v0() throws Exception {
        testInputFlatJson("IDCR - Relevant contacts.v0", "IDCR - Relevant contacts.v0.flat.json", "IDCR - Relevant contacts.v0.db.json", "IDCR - Relevant contacts.v0.flat.json", "IDCR - Relevant contacts.v0.ecisflat.json");
    }

    public void test_IDCR_Service_Request_v0() throws Exception {
        testInputFlatJson("IDCR - Service Request.v0", "IDCR - Service Request.v0.flat.json", "IDCR - Service Request.v0.db.json", "IDCR - Service Request.v0.flat.json", "IDCR - Service Request.v0.ecisflat.json");
    }

    public void test_IDCR_Problem_List_v1() throws Exception {
        testInputFlatJson("IDCR Problem List.v1", "IDCR Problem List.v1.FLAT.json", "IDCR Problem List.v1.db.json", "IDCR Problem List.v1.flat.json", "IDCR Problem List.v1.ecisflat.json");
    }

    public void test_IDCR_Problem_List_v1_2() throws Exception {
        testInputFlatJson("IDCR Problem List.v1", "IDCR Problem List.v1.2.flat.json", "IDCR Problem List.v1.2.db.json", "IDCR Problem List.v1.2.flat.json", "IDCR Problem List.v1.2.ecisflat.json");
    }

    public void test_LCR_Medication_List_v0() throws Exception {
        testInputFlatJson("LCR Medication List.v0", "LCR_Medication_List.v0.flat.json", "LCR Medication List.v0.db.json", "LCR Medication List.v0.flat.json", "LCR Medication List.v0.ecisflat.json");
    }

    public void test_NCHCD_Clinical_notes_v0() throws Exception {
        testInputFlatJson("NCHCD - Clinical notes.v0", "NCHCD - Clinical notes.v0.flat.json", "NCHCD - Clinical notes.v0.db.json", "NCHCD - Clinical notes.v0.flat.json", "NCHCD - Clinical notes.v0.ecisflat.json");
    }

    public void test_Ripple_Dashboard_Cache_v1() throws Exception {
//        regressionTest = false;
        testInputFlatJson("Ripple Dashboard Cache.v1", "ripple_dashboard_cache.flat.json", "Ripple Dashboard Cache.v1.db.json", "Ripple Dashboard Cache.v1.flat.json", "Ripple Dashboard Cache.v1.ecisflat.json");
    }

    private String jsonMapToString(Map map) {
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        String mapjson = gson.toJson(map);

        return mapjson;
    }
}