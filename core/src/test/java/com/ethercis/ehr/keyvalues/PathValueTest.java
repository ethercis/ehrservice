package com.ethercis.ehr.keyvalues;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.ContentHelper;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.FieldUtil;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.*;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.json.JsonUtil;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.identification.HierObjectID;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test ECISFLAT
 */
public class PathValueTest {
    I_KnowledgeCache knowledge;
    Map<String, Object> kvPairs = new HashMap<>();


    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "src/test/resources/knowledge/archetypes");
        props.put("knowledge.path.template", "src/test/resources/knowledge/templates");
        props.put("knowledge.path.opt", "src/test/resources/knowledge/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        FileReader fileReader = new FileReader("src/test/resources/samples/pathvalues_test1.json");

        kvPairs = FlatJsonUtil.inputStream2Map(fileReader);
    }

//    @Test
    public void testAssignment_OET() throws Exception {
        //build a composition from template
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.ecis_evaluation_test.v1");

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "ECIS EVALUATION TEST.oet");
        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);

        PathValue pathValue = new PathValue(knowledge, "ECIS EVALUATION TEST.oet", new Properties());

        composition = pathValue.assign(kvPairs);

        contentBuilder.importAsRM(composition);

        String entry = contentBuilder.getEntry();

        Map<String, String> testRetMap = new EcisFlattener().render(composition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    @Test
    public void testAssignment_OPT() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "ECIS EVALUATION TEST.opt");
//        Composition composition = contentBuilder.setCompositionParameters();generateNewComposition();
//
//        assertNotNull(composition);

        PathValue pathValue = new PathValue(knowledge, "UK AoMRC Outpatient Letter.opt", new Properties());

        kvPairs.clear();
        kvPairs.put("/context/other_context[at0001]/items[at0005]", "complete");
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.distribution.v1]/items[at0006]", "2015-10-21T10:00:00");
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0022]", "true");
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0014]", "2014-10-15::2022-12-31");

        //this is to test the flatten vacuum!
        kvPairs.put("/content[openEHR-EHR-SECTION.demographics_rcp.v1]/items[openEHR-EHR-ADMIN_ENTRY.key_contacts.v1]/data[at0001]/items[at0018]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]", "Joe Doe");
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-OBSERVATION.story.v1 and name/value='Story/History']/data[at0001]/events[at0002]/data[at0003]/items[at0004]", "big complain");
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-OBSERVATION.story.v1 and name/value='History since last contact']/data[at0001]/events[at0002]/data[at0003]/items[at0004]", "a long history");

//        //array...
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0002]", "does not sleep");
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0004]", "think too much");
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0005]", "think way too much");
//
//        //funny data types
//        //DvEHRURI
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-SECTION.adhoc.v1]/items[openEHR-EHR-SECTION.adhoc.v1 and name/value='Past medical history']/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/protocol[at0032]/items[at0035]", "http://home.cern/topics/higgs-boson");
//
//        //DvCodedText
        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-SECTION.adhoc.v1]/items[openEHR-EHR-SECTION.adhoc.v1 and name/value='Past medical history']/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/protocol[at0032]/items[openEHR-EHR-CLUSTER.problem_status.v1]/items[at0060]", "local::1|Past|");
//
//        //choice as Text
        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-INSTRUCTION.medication_order_uk.v1]/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_item.v1]/items[at0001]", "@1|Choice #1");

//        //choice as CodedText
        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-INSTRUCTION.medication_order_uk.v1]/activities[at0001]/description[at0002]/items[at0039]/items[at0041]", "@2|local::111|Continue Indefinitely|");

//        //interval in a choice (NOT SUPPORTED YET!!!)
//        kvPairs.put("/content[openEHR-EHR-SECTION.social_context_rcp.v1]/items[openEHR-EHR-EVALUATION.alcohol_use_summary.v1]/data[at0001]/items[at0024]", "10::12");

//        //ordinal
        kvPairs.put("/content[openEHR-EHR-SECTION.examination_findings_rcp.v1]/items[openEHR-EHR-SECTION.vital_signs.v1]/items[openEHR-EHR-OBSERVATION.news_rcp_uk.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0006]", "1|SNOMED-CT::313267000|Stroke|");

        //interval (date)
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0014]", "2010-10-10::2011-11-11");

        //ANY
        kvPairs.put("/content[openEHR-EHR-SECTION.investigations_results_rcp.v1]/items[openEHR-EHR-OBSERVATION.lab_test.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0078]", "@DVQUANTITY|12345,kg");

        //CHOICE
        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-EVALUATION.exclusion-medication.v1]/data[at0001]/items[at0003.1]", "@1|local::111|Continue Indefinitely|");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-INSTRUCTION.medication_order_uk.v1]/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_item.v1]/items[at0001]"
        Composition composition = pathValue.assign(kvPairs);

        Map<String, String> testRetMap = new EcisFlattener().render(composition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    @Test
    public void testUpdate_OPT() throws Exception {
        String templateId = "ECIS EVALUATION TEST";
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);

        PathValue pathValue = new PathValue(contentBuilder, knowledge, templateId, new Properties());

        Map<String, Object> updateValues = new HashMap<>();

        updateValues.put("/context/end_time", "2014-09-28T11:18:17.352+07:00");
        updateValues.put("/context/participation|function", "Cantine");
        updateValues.put("/context/participation|name", "Joe");
        updateValues.put("/context/participation|identifier", "99999-222");
        updateValues.put("/context/participation|mode", "face-to-face communication::openehr::216");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0001]/items[at0003]", "at0004|Type 1|");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0001]/items[at0008]", "true");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0001]/items[at0007]", "2010-09-24");
        //adds another iteration of diabetes
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0002]/items[at0003]", "at0005|Type 2|");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0002]/items[at0008]", "true");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/data[at0001]/items[at0002]/items[openEHR-EHR-CLUSTER.diabetes.v1]/items[at0002]/items[at0007]", "2015-09-24");

        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:0|function", "Oncologist");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:0|identifier", "1345678");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:0|mode", "face-to-face communication::openehr::216");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:0|name", "Dr. Knock");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:1|function", "Oncologist");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:1|identifier", "999999-8");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:1|mode", "evil cabinet::openehr::216");
        updateValues.put("/content[openEHR-EHR-EVALUATION.verbal_examination.v1]/participation:1|name", "Dr. Caligari");

        boolean result = pathValue.update(composition, updateValues);

        assertTrue(result);
        Map<String, String> testRetMap = new EcisFlattener().render(composition);
        String jsonString = gson.toJson(testRetMap);
        System.out.println(jsonString);

        byte[] xmlbytes = contentBuilder.exportCanonicalXML(composition, true);

        System.out.print(new String(xmlbytes));

        //update context participations
        kvPairs.clear();
        kvPairs.put("/context/participation:0|function", "Oncologist");
        kvPairs.put("/context/participation:0|name", "Dr. Marcus Johnson");
        kvPairs.put("/context/participation:0|identifier", "1345678");
        kvPairs.put("/context/participation:0|mode", "face-to-face communication::openehr::216");
        kvPairs.put("/context/participation:1|function", "Pediatric");
        kvPairs.put("/context/participation:1|name", "Dr. Mabuse");
        kvPairs.put("/context/participation:1|identifier", "99999-123");
        kvPairs.put("/context/participation:1|mode", "face-to-face communication::openehr::216");

        result = pathValue.update(composition, kvPairs);
        assertTrue(result);
        testRetMap = new EcisFlattener().render(composition);
        jsonString = gson.toJson(testRetMap);
        System.out.println(jsonString);

    }

    @Test
    public void testSerializeArray() throws Exception {
//        String templateId = "COLNEC_history_of_past_illness.v0";
        String templateId = "IDCR - Adverse Reaction List.v1";
//        String templateId = "COLNEC Patient Blood Pressure.v0";

        //pre-warm
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        //pre-warm the composition cache
        content.generateNewComposition();

        PathValue pathValue = new PathValue(knowledge, templateId, new Properties());
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC_history_of_past_illness.v0.kvp.json");
        for (String testfile: new String[]{"ecisflat", "ecisflat2"}) {
            FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/adverse_reaction_list."+testfile+".json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/blood_pressure.ecisflat.json");
            Map<String, Object> valuePairs = FlatJsonUtil.inputStream2Map(fileReader);
            Composition composition = pathValue.assign(valuePairs);
            assertNotNull(composition);
            //serialize this composition  for persistence
            I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
            String dbEncoded = compositionSerializer.dbEncode(composition);
            System.out.print(dbEncoded);

            //rebuild the composition from the encoded structure
            I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
            Composition retrieved = contentBuilder.buildCompositionFromJson(dbEncoded);
            assertNotNull(retrieved);
            //export flat
            Map<String, String> testRetMap = new EcisFlattener().render(retrieved);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

            String jsonString = gson.toJson(testRetMap);

            System.out.println(jsonString);
        }
    }

    @Test
    public void testSerialize() throws Exception {
//        String templateId = "COLNEC_history_of_past_illness.v0";
//        String templateId = "COLNEC Medication";
//        String templateId = "COLNEC Care Plan.v1";
//        String templateId = "Vital Signs Encounter (Composition)";
//        String templateId = "COLNEC_Goal.v0";
//        String templateId = "IDCR Problem List.v1";
        String templateId = "RIPPLE - Minimal referral.v0";

        PathValue pathValue = new PathValue(knowledge, templateId, new Properties());
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/Vital Signs Encounter (Composition).raw.kvp.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC_Goals.v0.kvp.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC_history_of_past_illness.v0.kvp.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC_history_of_past_illness.v0.fault.ecisflat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/beam_instruction_test.ecisflat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC Care Plan.v1.post2.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/IDCR Problem List.v1.put.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/e8fe7d12-c1e7-46af-b9af-7a28be5a3bcf.put.json");
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/RIPPLE_minimal_referral.v0.kv.json");

        Map<String, Object> valuePairs = FlatJsonUtil.inputStream2Map(fileReader);
        Composition composition = pathValue.assign(valuePairs);
        assertNotNull(composition);

        ContentHelper compositionHelper = new ContentHelper();
//        compositionHelper.invalidateContent(composition);

        //serialize this composition  for persistence
        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
//        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
        String dbEncoded = compositionSerializer.dbEncode(composition);
        System.out.print(dbEncoded);

        Gson gson = EncodeUtil.getGsonBuilderInstance().setPrettyPrinting().create();
        Map rawEncoded = gson.fromJson(compositionSerializer.dbEncode(composition), Map.class);

        assertNotNull(rawEncoded);

        //rebuild the composition from the encoded structure
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
        Composition retrieved = contentBuilder.buildCompositionFromJson(dbEncoded);
        assertNotNull(retrieved);
        //export flat
        Map<String, String> testRetMap = new EcisFlattener().render(retrieved);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);

        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);
        Map<String, Object> flatMap = jsonCompositionConverter.fromComposition(templateId, retrieved);
        flatMap.put("ctx/language", "en");
        flatMap.put("ctx/territory", "GB");
//        map.put("ctx/composer_name", "Silvia Blake");
//        map.put("ctx/time", "2016-02-22T12:57:20.706Z");
//        map.put("ctx/id_namespace", "HOSPITAL-NS");
//        map.put("ctx/id_scheme", "HOSPITAL-NS");

        String outMap = JsonUtil.toJsonString(flatMap);
        System.out.println(outMap);


    }

    DataValue dataValue;

    @Test
    public void testCodePhrase() throws Exception {
        kvPairs.clear();
        kvPairs.put("codeString", "1111");
        kvPairs.put("terminologyId", "openehr");
        dataValue = (DataValue)PathValue.decodeValue("CodePhrase", FieldUtil.flatten(FieldUtil.getAttributes(CodePhraseVBean.generate())), kvPairs);
        assertEquals(((CodePhrase) dataValue).getCodeString(), "1111");
    }

    @Test
    public void testDvCount() throws Exception {
        kvPairs.clear();
        kvPairs.put("magnitude", "1");
        dataValue = (DataValue)PathValue.decodeValue("DvCount", FieldUtil.flatten(FieldUtil.getAttributes(DvCountVBean.generate())), kvPairs);
        assertEquals(((DvCount) dataValue).getMagnitude(), (Integer) 1);
    }

    @Test
    public void testDvBoolean() throws Exception {
        kvPairs.clear();
        kvPairs.put("value", "false");
        dataValue = (DataValue)PathValue.decodeValue("DvBoolean", FieldUtil.flatten(FieldUtil.getAttributes(DvBooleanVBean.generate())), kvPairs);
        assertEquals(((DvBoolean) dataValue).getValue(), false);
    }

    @Test
    public void testDvCodedText() throws Exception {
        kvPairs.clear();
        kvPairs.put("value", "");
        kvPairs.put("definingCode", "openehr::111");
        dataValue = (DataValue)PathValue.decodeValue("DvCodedText", FieldUtil.flatten(FieldUtil.getAttributes(DvCodedTextVBean.generate())), kvPairs);
        assertEquals(((DvCodedText) dataValue).getValue(), "");
    }

    @Test
    public void testDvDateTime() throws Exception {
        kvPairs.clear();
        String dateTime = new DateTime(DateTime.now()).toString();
        kvPairs.put("value", dateTime);
        dataValue = (DataValue)PathValue.decodeValue("DvDateTime", FieldUtil.flatten(FieldUtil.getAttributes(DvDateTimeVBean.generate())), kvPairs);
        assertEquals(((DvDateTime) dataValue).getValue(), dateTime);
    }

    @Test
    public void testDvDate() throws Exception {
        kvPairs.clear();
        String dateTime = "2010-10-10";
        kvPairs.put("value", dateTime);
        dataValue = (DataValue)PathValue.decodeValue("DvDate", FieldUtil.flatten(FieldUtil.getAttributes(DvDateTimeVBean.generate())), kvPairs);
        assertEquals(((DvDate) dataValue).getValue(), dateTime);
    }

    @Test
    public void testDvTime() throws Exception {
        kvPairs.clear();
        String dateTime = "10:10:10";
        kvPairs.put("value", dateTime);
        dataValue = (DataValue)PathValue.decodeValue("DvTime", FieldUtil.flatten(FieldUtil.getAttributes(DvDateTimeVBean.generate())), kvPairs);
        assertEquals(((DvTime) dataValue).getValue(), dateTime);
    }

    @Test
    public void testDvDuration() throws Exception {
        kvPairs.clear();
        String value = "P1Y1M1W1DT1H1M1S";
        kvPairs.put("value", value);
        dataValue = (DataValue)PathValue.decodeValue("DvDuration", FieldUtil.flatten(FieldUtil.getAttributes(DvDateTimeVBean.generate())), kvPairs);
        assertEquals(((DvDuration) dataValue).getValue(), value);
    }

    @Test
    public void testDvEHRURI() throws Exception {
        kvPairs.clear();
        String value = "compositions/87284370-2D4B-4e3d-A3F3-F303D2F4F34B/data%5Bat0001%5D/items%5Bat0001%5D/test";
        kvPairs.put("value", value);
        dataValue = (DataValue)PathValue.decodeValue("DvEHRURI", FieldUtil.flatten(FieldUtil.getAttributes(DvEHRURIVBean.generate())), kvPairs);
        assertEquals(((DvEHRURI)dataValue).getValue(), value);
    }

    @Test
    public void testDvIdentifier() throws Exception {
        kvPairs.clear();
        kvPairs.put("assigner", "dummyAssigner");
        kvPairs.put("id", "123.456.789");
        kvPairs.put("issuer", "dummyIssuer");
        kvPairs.put("type", "dummyType");
        dataValue = (DataValue)PathValue.decodeValue("DvIdentifier", FieldUtil.flatten(FieldUtil.getAttributes(DvIdentifierVBean.generate())), kvPairs);
        assertEquals(((DvIdentifier)dataValue).getAssigner(), kvPairs.get("assigner"));
        assertEquals(((DvIdentifier)dataValue).getId(), kvPairs.get("id"));
    }

    @Test
    public void testDvInterval() throws Exception {
        kvPairs.clear();
        kvPairs.put("lower", "2001-01-01");
        kvPairs.put("upper", "2010-10-10");
        dataValue = (DataValue)PathValue.decodeValue("DvInterval", FieldUtil.flatten(FieldUtil.getAttributes(DvIntervalVBean.createQualifiedInterval(DvDate.class))), kvPairs);
        assertEquals(((DvInterval)dataValue).getLower(), kvPairs.get("lower"));
        assertEquals(((DvInterval)dataValue).getUpper(), kvPairs.get("upper"));
    }

    @Test
    public void testDvMultimedia() throws Exception {
        kvPairs.clear();
        kvPairs.put("integrityCheckAlgorithm", "openehr_integrity_check_algorithms::SHA-1");
        kvPairs.put("mediaType", "IANA_media-types::text/plain");
        kvPairs.put("compressionAlgorithm", "openehr_compression_algorithms::other");
        kvPairs.put("uri", "file://test.ethercis.com/dir/file.jpg");
        dataValue = (DataValue)PathValue.decodeValue("DvMultimedia", FieldUtil.flatten(FieldUtil.getAttributes(DvMultimediaVBean.generate())), kvPairs);
        assertEquals(((DvMultimedia)dataValue).getIntegrityCheckAlgorithm().toString(), kvPairs.get("integrityCheckAlgorithm"));
        assertEquals(((DvMultimedia)dataValue).getMediaType().toString(), kvPairs.get("mediaType"));
    }

    @Test
    public void testDvOrdinal() throws Exception {
        kvPairs.clear();
        Integer value = 1;
        kvPairs.put("value", value+"");
        kvPairs.put("symbol", "openehr::111|symbol|");
        dataValue = (DataValue)PathValue.decodeValue("DvOrdinal", FieldUtil.flatten(FieldUtil.getAttributes(DvOrdinalVBean.generate())), kvPairs);
        assertEquals(((DvOrdinal) dataValue).getValue(), 1);
    }

    @Test
    public void testDvParsable() throws Exception {
        kvPairs.clear();
        kvPairs.put("value", "");
        kvPairs.put("formalism", "formalism");
        dataValue = (DataValue)PathValue.decodeValue("DvParsable", FieldUtil.flatten(FieldUtil.getAttributes(DvParsableVBean.generate())), kvPairs);
        assertEquals(((DvParsable) dataValue).getValue(), "");
    }

    @Test
    public void testDvProportion() throws Exception {
        kvPairs.clear();
        kvPairs.put("numerator", 4+"");
        kvPairs.put("denominator", 5+"");
        kvPairs.put("type", "FRACTION");
        kvPairs.put("precision", 0+"");

        dataValue = (DataValue)PathValue.decodeValue("DvProportion", FieldUtil.flatten(FieldUtil.getAttributes(DvProportionVBean.generate())), kvPairs);
        assertEquals(((DvProportion) dataValue).getNumerator(), 4, 0);
    }

    @Test
    public void testDvQuantity() throws Exception {
        kvPairs.clear();
        kvPairs.put("magnitude", 4.5+"");
        kvPairs.put("units", "kg");
        kvPairs.put("precision", 2+"");

        dataValue = (DataValue)PathValue.decodeValue("DvQuantity", FieldUtil.flatten(FieldUtil.getAttributes(DvQuantityVBean.generate())), kvPairs);
        assertEquals(((DvQuantity) dataValue).getMagnitude(), 4.5, 0);
    }

    @Test
    public void testDvText() throws Exception {
        kvPairs.clear();
        kvPairs.put("value", "");
        dataValue = (DataValue)PathValue.decodeValue("DvText", FieldUtil.flatten(FieldUtil.getAttributes(DvTextVBean.generate())), kvPairs);
        assertEquals(((DvText) dataValue).getValue(), "");
    }

    @Test
    public void testDvURI() throws Exception {
        kvPairs.clear();
        kvPairs.put("value", "file://myserver/test/file.extension");
        dataValue = (DataValue)PathValue.decodeValue("DvURI", FieldUtil.flatten(FieldUtil.getAttributes(DvURIVBean.generate())), kvPairs);
        assertEquals(((DvURI) dataValue).getValue(), "file://myserver/test/file.extension");
    }

    @Test
    public void testHierObjectID() throws Exception {
        String test = "c2bd0416-91b5-11e5-8994-feff819cdc9f";
        kvPairs.clear();
        kvPairs.put("value", test);
        HierObjectID objectID = (HierObjectID)PathValue.decodeValue("HierObjectID", FieldUtil.flatten(FieldUtil.getAttributes(HierObjectIDVBean.generate())), kvPairs);
        assertEquals(objectID.toString(), test);
    }

    @Test
    public void testAssignment_OPT_RALPH_20() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "ECIS EVALUATION TEST.opt");
//        Composition composition = contentBuilder.setCompositionParameters();generateNewComposition();
//
//        assertNotNull(composition);

        PathValue pathValue = new PathValue(knowledge, "careplan.v1", new Properties());

        kvPairs.clear();
        kvPairs.put("/category", "openehr::433|event|");
        kvPairs.put("/territory", "GB");
        kvPairs.put("/language", "en");

        kvPairs.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0002]",
                "My Care Plan"
        );

        kvPairs.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/guideline",
                "local::test"
        );

        kvPairs.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0011]",
                "A test care plan"
        );

        kvPairs.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0021]",
                "local::at0024|Active|"
        );

        kvPairs.put(
                "/content[openEHR-EHR-INSTRUCTION.procedure.v1]/activities[at0001]/description[at0002]/items[at0003]",
                "foo"
        );

        kvPairs.put(
                "/content[openEHR-EHR-INSTRUCTION.procedure.v1]/activities[at0001]/description[at0002]/items[at0012]",
                "Test name"
        );

        Composition composition = pathValue.assign(kvPairs);

        Map<String, String> testRetMap = new EcisFlattener().render(composition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    @Test
    public void testAssignment_OPT_RALPH_6() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "ECIS EVALUATION TEST.opt");
//        Composition composition = contentBuilder.setCompositionParameters();generateNewComposition();
//
//        assertNotNull(composition);

        PathValue pathValue = new PathValue(knowledge, "65d9e89a-81d8-4344-afbe-88508d42dcfc", new Properties());

        kvPairs.clear();
        kvPairs.put("/category", "openehr::433|event|");
        kvPairs.put("/territory", "GB");
        kvPairs.put("/language", "en");

        kvPairs.put(
                "/content[openEHR-EHR-OBSERVATION.laboratory_test.v0]/guideline_id",
                "roughspace::myType::65d9e89a-81d8-4344-afbe-88508d42dcfc"
        );

        Composition composition = pathValue.assign(kvPairs);

        Map<String, String> testRetMap = new EcisFlattener().render(composition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    @Test
    public void testAssignment_OPT_RALPH_BMI() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "ECIS EVALUATION TEST.opt");
//        Composition composition = contentBuilder.setCompositionParameters();generateNewComposition();
//
//        assertNotNull(composition);

        PathValue pathValue = new PathValue(knowledge, "BMI", new Properties());

        kvPairs.clear();
        kvPairs.put("/category", "openehr::433|event|");
        kvPairs.put("/territory", "GB");
        kvPairs.put("/language", "en");

        kvPairs.put(
                "/content[openEHR-EHR-OBSERVATION.body_mass_index.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value",
                "18.5,kg/m2");
        kvPairs.put(
                "/content[openEHR-EHR-OBSERVATION.body_weight.v1]/data[at0002]/events[at0003]/data[at0001]/items[at0004]|value",
                "65.2,kg");
        kvPairs.put(
                "/content[openEHR-EHR-OBSERVATION.height.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value",
                "167,cm");
        Composition composition = pathValue.assign(kvPairs);

        Map<String, String> testRetMap = new EcisFlattener().render(composition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }


}