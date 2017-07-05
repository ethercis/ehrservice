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

package com.ethercis.ehr.rawjson;

import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.LocatableBuilder;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.rawjson.RawJsonEncoder;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import com.ethercis.ehr.util.MapInspector;
import com.ethercis.ehr.util.RMDataSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.datastructure.itemstructure.ItemTree;

import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by christian on 2/15/2017.
 */
public class TestRawJsonEncoding {
    //	ClusterController controller;
    I_KnowledgeCache knowledge;

    static final String FLAT_JSON_INPUT_DIRECTORY = "core/src/test/resources/flat_json_input";

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "core/src/test/resources/knowledge/archetypes");
        props.put("knowledge.path.template", "core/src/test/resources/knowledge/templates");
        props.put("knowledge.path.opt", "core/src/test/resources/knowledge/operational_templates");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);
    }

    private String jsonMapToString(Map map){
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        String mapjson = gson.toJson(map);

        return mapjson;
    }

    protected MapInspector getMapInspector(String jsonData) throws Exception {
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();

        Map<String, Object> retmap = gson.fromJson(jsonData, TreeMap.class);

        //traverse the tree
        MapInspector inspector = new MapInspector();
        inspector.inspect(retmap);
        return inspector;
    }

    @Test
    public void testSerializer() throws Exception {
        String templateId = "COLNEC Medication";
//        String templateId = "IDCR - Service Request.v0";
//        String templateId = "GEL - Generic Lab Report import.v0";

//        String templateId = "IDCR Problem List.v1";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        //get a flat json test file
        FileReader fileReader = new FileReader(FLAT_JSON_INPUT_DIRECTORY + "/COLNEC_Medication_FLAT.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/Ian-mail-27-01-17.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_26.flat.json");
        Map map = FlatJsonUtil.inputStream2Map(fileReader);

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);

        assertNotNull(lastComposition);

        //=== to get the path...
        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
//        String dbEncoded = compositionSerializer.dbEncode(lastComposition);
//        System.out.println(dbEncoded);

        //get a fragment of the encoding
        Map<String, Object> mapComposition = compositionSerializer.process(lastComposition);

//        Map<String, Object> fragment = (Map<String, Object>)mapComposition.get("/composition[openEHR-EHR-COMPOSITION.prescription.v1 and name/value='Prescription']");

        MultiValueMap valueMap =
        (MultiValueMap)
                ((Map)
                    ((ArrayList)
                        ((MultiValueMap)
                            ((Map)
                                ((ArrayList)
                                    ((MultiValueMap)
                                            ((ArrayList)
                                                    ((MultiValueMap)mapComposition.get("/composition[openEHR-EHR-COMPOSITION.prescription.v1 and name/value='Prescription']"))
                                                        .get("/content[openEHR-EHR-SECTION.medications.v1]"))
                                                            .get(0))
                                                                .get("/items[openEHR-EHR-INSTRUCTION.medication.v1]")).
                                                                    get(0))
                                                                        .get("/activities"))
                                                                            .get("/activities[at0001]"))
                                                                                .get(0))
                                                                                    .get("/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]");


        //ACTUAL AQL query simulation
        //this fragment will be returned by an AQL (or SQL query)
        String fragmentDBEncoded = jsonMapToString(valueMap);

//        MapInspector mapInspector = getMapInspector(fragmentDBEncoded);

        I_ContentBuilder content = I_ContentBuilder.getInstance(knowledge, templateId);
        //build a composition with the retrieved values
        Composition newComposition = content.buildCompositionFromJson(fragmentDBEncoded);

//        assignValuesFromStack(lastComposition, (ArrayDeque) mapInspector.getStack())

        String path = "/content[openEHR-EHR-SECTION.medications.v1 and name/value='Medications']" +
                "/items[openEHR-EHR-INSTRUCTION.medication.v1 and name/value='Medication order']" +
                "/activities[at0001 and name/value='Medication activity']" +
                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1 and name/value='Medication description']";
//                "/items[at0003 and name/value='Strength per dose unit']";

        //retrieved the locatable corresponding to the queried fragment
        Locatable item = (Locatable)newComposition.itemAtPath(path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        String stringMap = rawCompositionSerializer.dbEncode("fragment", item);

        assertNotNull(stringMap);
        System.out.print(stringMap);

    }

    @Test
    public void testSerializer2() throws Exception {
        String templateId = "COLNEC Medication";
//        String templateId = "IDCR - Service Request.v0";
//        String templateId = "GEL - Generic Lab Report import.v0";

//        String templateId = "IDCR Problem List.v1";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        //get a flat json test file
        FileReader fileReader = new FileReader(FLAT_JSON_INPUT_DIRECTORY + "/COLNEC_Medication_FLAT.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/Ian-mail-27-01-17.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_26.flat.json");
        Map map = FlatJsonUtil.inputStream2Map(fileReader);

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);

        assertNotNull(lastComposition);

        //=== to get the path...
        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
//        String dbEncoded = compositionSerializer.dbEncode(lastComposition);
//        System.out.println(dbEncoded);

        //get a fragment of the encoding
        Map<String, Object> mapComposition = compositionSerializer.process(lastComposition);

//        Map<String, Object> fragment = (Map<String, Object>)mapComposition.get("/composition[openEHR-EHR-COMPOSITION.prescription.v1 and name/value='Prescription']");

        MultiValueMap valueMap =
                (MultiValueMap)
                        ((Map)
                                ((ArrayList)
                                        ((MultiValueMap)
                                                ((Map)
                                                        ((ArrayList)
                                                                ((MultiValueMap)
                                                                        ((ArrayList)
                                                                                ((MultiValueMap)mapComposition.get("/composition[openEHR-EHR-COMPOSITION.prescription.v1 and name/value='Prescription']"))
                                                                                        .get("/content[openEHR-EHR-SECTION.medications.v1]"))
                                                                                .get(0))
                                                                        .get("/items[openEHR-EHR-INSTRUCTION.medication.v1]")).
                                                                get(0))
                                                        .get("/activities"))
                                                .get("/activities[at0001]"))
                                        .get(0))
                                .get("/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]");


        String path = "/content[openEHR-EHR-SECTION.medications.v1 and name/value='Medications']" +
                "/items[openEHR-EHR-INSTRUCTION.medication.v1 and name/value='Medication order']" +
                "/activities[at0001 and name/value='Medication activity']" +
                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1 and name/value='Medication description']";

        //ACTUAL AQL query simulation
        //this fragment will be returned by an AQL (or SQL query)
        String fragmentDBEncoded = jsonMapToString(valueMap);

        RawJsonEncoder rawJsonEncoder = new RawJsonEncoder(knowledge);
        String stringMap = rawJsonEncoder.encode(templateId, fragmentDBEncoded, path);

        assertNotNull(stringMap);
        System.out.print(stringMap);

    }
}
