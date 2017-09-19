package com.ethercis.ehr.building;

import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.encode.*;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.json.JsonUtil;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.keyvalues.PathValue;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marand.thinkehr.jsonlib.CompositionConverter;
import com.marand.thinkehr.jsonlib.impl.CompositionConverterImpl;
import junit.framework.TestCase;
import openEHR.v1.template.TEMPLATE;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.am.template.FlattenerNew;
import org.openehr.jaxb.am.Template;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.support.identification.ObjectVersionID;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.ethercis.ehr.building.util.CompositionAttributesHelper.createComposer;

public class ContentBuilderTest extends TestCase {
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

   private String jsonMapToString(Map map){
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        String mapjson = gson.toJson(map);

        return mapjson;
    }

    @Test
         /**
          * generate a composition from an Operational Template (OPT)
          */
         public void testOPTGenerateComposition() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "ECIS EVALUATION TEST");
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "LCR Problem List.opt");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);
    }

    @Test
    /**
     * generate a composition from an Operational Template (OPT)
     */
    public void testSerializeComposition() throws Exception {
        String templateId = "COLNEC Personal Activity Action.v1";
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "ECIS EVALUATION TEST");
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);

        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, true);
        Map<String, Object> retmap = compositionSerializer.process(composition);
        String stringMap = jsonMapToString(retmap);

        assertNotNull(stringMap);
    }

    @Test
    public void testOPTGenerateLocatable() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "ECIS EVALUATION TEST");
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "person anonymised parent");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        Locatable generated = contentBuilder.generate();

        assertNotNull(generated);

        byte[] exportXml = contentBuilder.exportCanonicalXML(generated, true, true);

        assertNotNull(exportXml);

        InputStream is = new FileInputStream(new File("/Development/Dropbox/eCIS_Development/samples/other_details.xml"));
        Locatable itemStructure = I_ContentBuilder.parseOtherDetailsXml(is);

        //serialize other_details
        I_CompositionSerializer serializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, true);
        String encoded = serializer.dbEncode(CompositionSerializer.TAG_OTHER_DETAILS, itemStructure);
//        Map<String, Object> stringObjectMap = serializer.processItem(itemStructure);
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//        builder.registerTypeAdapter(DvDate.class, new DvDateAdapter());
//        builder.registerTypeAdapter(DvTime.class, new DvTimeAdapter());
////        builder.registerTypeAdapter(DvTime.class, new DvTimeAdapter());
//        Gson gson = builder.setPrettyPrinting().create();
//        String mapjson = gson.toJson(stringObjectMap);
        System.out.println("====================================================================================");
        System.out.println(new String(exportXml));



//        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH, true);
//        Map<String, Object>retmap = inspector.processItem(CompositionSerializer.TAG_OTHER_DETAILS, generated);

//        //insert the template id
//        retmap.put("$TEMPLATE_ID$", "person anonymised parent");
//
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//        Gson gson = builder.setPrettyPrinting().create();
//        String mapjson = gson.toJson(retmap);
//
//        System.out.println("====================================================================================");
//        System.out.println(mapjson);
//
//        //use an anonymous contentbuilder
//
//        //retrieve the template id from the serialized map
//        Map structured = gson.fromJson(mapjson, Map.class);
//
//        contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, (String)structured.get("$TEMPLATE_ID$"));
//
//        //rebuild it from json
//        //get the archetype node id and name from the first line of the map
////        String otherDetailsHeader = mapjson.substring(mapjson.indexOf("/other_details["), mapjson.indexOf("]")+1).replace("\\u003d", "=").replace("\\u0027", "'");
////        String[] segments = otherDetailsHeader.split(" and name/value");
////        String archetypeNodeId = segments[0].substring("/other_details[".length());
////        String name = segments[1].substring(1, segments[1].lastIndexOf("'"));
//        Locatable locatable = contentBuilder.buildLocatableFromJson(mapjson);
//
//        assertNotNull(locatable);
//
//        String xml = new String(contentBuilder.exportCanonicalXML(locatable, true));
//
//        System.out.println("REBUILT ====================================================================================");
//        System.out.println(xml);

    }

    @Test
    public void testSetActionEntry() throws Exception {
        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");

        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.action_test.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ACTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication.v1");

//        LogManager.getRootLogger().setLevel(Level.DEBUG);
        String templateFileName = "action test.oet";

//        TEMPLATE prescription = knowledge.retrieveTemplate(templateId);
//
//
//        Flattener flattener = new Flattener();
//
//        Archetype instance = flattener.toFlattenedArchetype(prescription, knowledge.getArchetypeMap());
//        assertNotNull(instance);
//
//        //try to build an actual COMPOSITION from the instance...
//        DomainBuilder generator = DomainBuilder.getInstance();
//        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateFileName);
        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);


        I_ContentBuilder content = I_ContentBuilder.getInstance(knowledge, templateFileName);
        byte[] exportXml = content.exportCanonicalXML(composition, true);
        assertNotNull(exportXml);



        content.setEntryData(composition);

        Composition newComposition = content.buildCompositionFromJson(content.getEntry());
    }

    @Test
    public void testSetInstructionEntry() throws Exception {
        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");

//        Logger.getRootLogger().setLevel(Level.DEBUG);
        String templateId = "prescription";

        TEMPLATE prescription = knowledge.retrieveOpenehrTemplate(templateId);

        Flattener flattener = new Flattener();

        Archetype instance = flattener.toFlattenedArchetype(prescription, knowledge.getArchetypeMap());

        assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        assertNotNull(composition);

        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

        content.setEntryData(composition);

        Composition newComposition = content.buildCompositionFromJson(content.getEntry());
    }

//    @Test
    public void _testSetEvaluationEntry() throws Exception {
        //use the fat nurse form to check multiple entries management
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.ecis_evaluation_test.v1");

        String templateId = "ECIS EVALUATION TEST";

        TEMPLATE form = knowledge.retrieveOpenehrTemplate(templateId);

        FlattenerNew flattener = new FlattenerNew();

        Archetype instance = flattener.toFlattenedArchetype(form, knowledge.getArchetypeMap());

        assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        assertNotNull(composition);

        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

        content.setEntryData(composition);

        Composition newComposition = content.buildCompositionFromJson(content.getEntry());

        assertNotNull(newComposition);
    }

//    @Test
    public void _testSetSectionObservationEntry() throws Exception {
        //use the fat nurse form to check multiple entries management
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.section_observation_test.v2");

        String templateId = "section  observation test";

        TEMPLATE form = knowledge.retrieveOpenehrTemplate(templateId);

        FlattenerNew flattener = new FlattenerNew();

        Archetype instance = flattener.toFlattenedArchetype(form, knowledge.getArchetypeMap());

        assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        assertNotNull(composition);

        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

        content.setEntryData(composition);

        Composition newComposition = content.buildCompositionFromJson(content.getEntry());

        assertNotNull(newComposition);
    }


    static String[] documentList = {

//            "IDCR Lab Order RAW1.xml"
//            "IDCR Procedures List_1 RAW.xml"
//            "IDCR Problem List.v1.xml"
//            "Vital_signs_TEST.xml"
            "IDCR-LabReportRAW1.xml",
            "RIPPLE_conformanceTesting_RAW.xml",
            "prescription_validation_test.xml"


    };

    String path = "\\Development\\Dropbox\\eCIS_Development\\samples\\";

    @Test
    public void testImportXMLComposition() throws Exception {

//        InputStream is = new FileInputStream(new File("\\Development\\Dropbox\\eCIS_Development\\samples\\IDCR Lab Order RAW1.xml"));
//        InputStream is = new FileInputStream(new File("\\Development\\Dropbox\\eCIS_Development\\samples\\IDCR Procedures List_1 RAW.xml"));
//        InputStream is = new FileInputStream(new File("\\Development\\Dropbox\\eCIS_Development\\samples\\IDCR Problem List.v1.xml"));
//        InputStream is = new FileInputStream(new File("\\Development\\Dropbox\\eCIS_Development\\samples\\Vital_signs_TEST.xml"));
//        InputStream is = new FileInputStream(new File("\\Development\\Dropbox\\eCIS_Development\\samples\\IDCR-LabReportRAW1.xml"));
//        InputStream is = new FileInputStream(new File("/TMP/CXML2495809321753426126.xml"));

        for (String document: documentList) {

            System.out.println("=============================================="+document);
            String documentPath = path + document;
            InputStream is = new FileInputStream(new File(documentPath));
            I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "IDCR - Laboratory Test Report.v0");
            //pre-warm the composition cache
            content.generateNewComposition();

            Composition composition = content.importCanonicalXML(is);
            assertNotNull(composition);

            content.setTemplateId(composition.getArchetypeDetails().getTemplateId().getValue());
            content.setEntryData(composition);
            String serialized = content.getEntry();

            Composition newComposition = content.buildCompositionFromJson(serialized);

            assertNotNull(newComposition);

            byte[] exportXml = content.exportCanonicalXML(newComposition, true);

            assertNotNull(exportXml);

            System.out.println("====================================================================================");
            System.out.println(newComposition.getArchetypeDetails().getTemplateId()+" ==============================");
            System.out.println(new String(exportXml));

            //reciprocal
//        StringReader reader = new StringReader(new String(exportXml));
            InputStream inputStream = IOUtils.toInputStream(new String(exportXml), "UTF-8");
            Composition importedComposition = content.importCanonicalXML(inputStream);

            assertNotNull(importedComposition);

            Map<String, String> testRetMap = new EcisFlattener().render(newComposition);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

            String jsonString = gson.toJson(testRetMap);

            System.out.println(jsonString);

            //storeComposition a temp file and write the exported XML into: C:\Users\<current_user>\AppData\Local\Temp
            File tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_", ".xml");
            FileUtils.writeStringToFile(tempfile, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + new String(exportXml));
        }

    }

    public void testBuildFromJson() throws Exception {
        String templateId = "IDCR - Laboratory Order.v0";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        StringBuffer sb = new StringBuffer();
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/ProblemList_2FLAT.json")).forEach(line -> sb.append(line));
        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/Laboratory_Order_faulty.json")).forEach(line -> sb.append(line));
//        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "IDCR Problem List.v1");
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);

        Composition newComposition = content.buildCompositionFromJson(sb.toString());

        assertNotNull(newComposition);

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

    @Test
    public void testOPTGenerateLocatable2() throws Exception {
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "ECIS EVALUATION TEST");
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "Weird Types 1");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "action test");

        Object generated = contentBuilder.generate();

        assertNotNull(generated);

        if (generated instanceof Composition) {
            EventContext context = ContextHelper.createDummyContext();
            PartyIdentified partyIdentified = createComposer("Composer", "ETHERCIS", "1234-5678");

            ((Composition) generated).setContext(context);
            ((Composition) generated).setComposer(partyIdentified);
            ((Composition) generated).setUid(new ObjectVersionID(UUID.randomUUID()+"::example.ethercis.com::1"));

            byte[] exportXml = contentBuilder.exportCanonicalXML((Composition)generated, true, true);

            assertNotNull(exportXml);

            System.out.println(new String(exportXml));

            Map<String, String> testRetMap = new EcisFlattener(true).render((Composition) generated);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

            String jsonString = gson.toJson(testRetMap);

            System.out.println(jsonString);
        } else if (generated instanceof Locatable){

            byte[] exportXml = contentBuilder.exportCanonicalXML((Locatable)generated, true, true);

            assertNotNull(exportXml);

            System.out.println(new String(exportXml));

            Map<String, String> testRetMap = new EcisFlattener(true).render((Locatable)generated);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

            String jsonString = gson.toJson(testRetMap);

            System.out.println(jsonString);

        }
    }

    private Map setQueryBody(){
        Map<String, String> kvPairs = new HashMap<>();

//        kvPairs.put("/context/health_care_facility|name", "Northumbria Community NHS");
//        kvPairs.put("/context/health_care_facility|identifier", "999999-345");
//        kvPairs.put("/context/start_time", "2015-09-28T10:18:17.352+07:00");
//        kvPairs.put("/context/end_time", "2015-09-28T11:18:17.352+07:00");
//        kvPairs.put("/context/participation|function", "Oncologist");
//        kvPairs.put("/context/participation|name", "Dr. Marcus Johnson");
//        kvPairs.put("/context/participation|identifier", "1345678");
//        kvPairs.put("/context/participation|mode", "openehr::216|face-to-face communication|");
//        kvPairs.put("/context/location", "local");
//        kvPairs.put("/context/setting", "openehr::227|emergency care|");
//        kvPairs.put("/composer|identifier", "1345678");
//        kvPairs.put("/composer|name", "Dr. Marcus Johnson");
//        kvPairs.put("/category", "openehr::433|event|");
//        kvPairs.put("/territory", "FR");
//        kvPairs.put("/language", "fr");
//
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:0", "Nurse|1345678::Jessica|openehr::216|face-to-face communication|");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:1", "Assistant|1345678::2.16.840.1.113883.2.1.4.3::NHS-UK::ANY::D. Mabuse|openehr::216|face-to-face communication|");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/action_archetype_id", "ZZZZZZZ\\.medication\\.v1");

//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='Name of medication']/timing", "before sleep");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='Name of medication']" +
//                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "aspirin");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='Name of medication #2']/timing", "lunch");
//        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='Name of medication #2']" +
//                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "Atorvastatin");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='SNOMED-CT::365761000|Sodium|']/timing", "lunch");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001 and name/value='SNOMED-CT::365761000|Sodium|']" +
                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "Sodium");

//        GsonBuilder builder = new GsonBuilder();
//        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        return kvPairs;
    }

    //test composition building with the above kv
    @Test
    public void testBuildFromJson2() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        String templateId = "prescription";

        TEMPLATE prescription = knowledge.retrieveOpenehrTemplate(templateId);

        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

//        content.setEntryData(composition);
        PathValue pathValue = new PathValue(knowledge, templateId, new Properties());

        Composition newComposition = pathValue.assign(setQueryBody());

        assertNotNull(newComposition);

        Map<String, String> testRetMap = new EcisFlattener().render(newComposition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    private Map setQueryBodyWeirdTypes() {
        Map<String, String> kvPairs = new HashMap<>();
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0001 and name/value='DvIntervalCount']", "0::1");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0002 and name/value='DvIntervalQuantity']", "0,kg::1,kg");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0003 and name/value='DvIntervalDateTime']", "1970-01-01T07:00:00.000+07:00::1970-01-02T07:00:00,000+07:00");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0004 and name/value='DvIntervalDateOnly']", "1970-01-01::1970-01-02");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0005 and name/value='DvIntervalTimeOnly']", "01::02");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0006 and name/value='ParsableHtml']", "text value");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0007 and name/value='URI']|name", "URI");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0007 and name/value='URI']|value", "http://www.ethercis.com/");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|name", "Proportion");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|value", "1,1,0");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|name", "Proportion");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|numerator", "1");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|denominator", "2");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|type", "FRACTION");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0008 and name/value='Proportion']|precision", "0");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0009 and name/value='Ordinal']|name", "Ordinal");
        kvPairs.put("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']/items[openEHR-EHR-CLUSTER.weird_types_1.v0 and name/value='Weird types 1']/items[at0009 and name/value='Ordinal']|value", "-1|openehr::at0010|one|");

        return kvPairs;
    }

    @Test
    public void testBuildFromJsonWeirdTypes() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        String templateId = "Weird Types 1";

        TEMPLATE weirdTypes = knowledge.retrieveOpenehrTemplate(templateId);

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
        Object generated = contentBuilder.generate();

//        content.setEntryData(composition);
        PathValue pathValue = new PathValue(contentBuilder, knowledge, templateId, new Properties());

        pathValue.assignItemStructure("/items[openEHR-EHR-ITEM_TREE.weird_type_1.v0 and name/value='Weird type 1']", (Locatable)generated, setQueryBodyWeirdTypes());

        assertNotNull(generated);

        Map<String, String> testRetMap = new EcisFlattener().render((Locatable) generated);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);
    }

    @Test
    public void testThinkEhrLib() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        JAXBContext context = JAXBContext.newInstance("org.openehr.jaxb.rm:org.openehr.jaxb.am");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(null); // disable schema validation
        JAXBContext unmarshalContext = JAXBContext.newInstance(org.openehr.jaxb.rm.Composition.class);
        Marshaller marshaller = unmarshalContext.createMarshaller();
        marshaller.setSchema(null);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//        NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
//            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//                if ("http://schemas.openehr.org/v1".equals(namespaceUri) && !requirePrefix)
//                    return "";
//                return "ns";
//            }
//        };
//        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", mapper);

        StringWriter stringWriter = new StringWriter();


        String templateId = "RIPPLE - Conformance Test template";
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);

        Composition generated = (Composition)contentBuilder.generate();

        EventContext compositionContext = ContextHelper.createDummyContext();
        PartyIdentified partyIdentified = createComposer("Composer", "ETHERCIS", "1234-5678");

        generated.setContext(compositionContext);
        generated.setComposer(partyIdentified);
        generated.setUid(new ObjectVersionID(UUID.randomUUID() + "::example.ethercis.com::1"));

        byte[] exportXml = contentBuilder.exportCanonicalXML(generated, true, true);

        String xmlized = new String(exportXml);

        JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(xmlized));
        org.openehr.jaxb.rm.Composition jaxbComposition = (org.openehr.jaxb.rm.Composition)jaxbElement.getValue();

        XmlOptions xmlOptions = new XmlOptions().setSaveSyntheticDocumentElement(new QName("http://schemas.openehr.org/v1","template"));
        String templateXml = ((OPERATIONALTEMPLATE)knowledge.retrieveTemplate(templateId)).xmlText(xmlOptions);

        //get the template
        jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(templateXml));
        Template jaxbTemplate = (Template)jaxbElement.getValue();

        CompositionConverter converter = new CompositionConverterImpl();
        Map<String, Object> map = converter.fromComposition(jaxbTemplate, jaxbComposition);

        assertNotNull(map);
        map.put("ctx/language", "en");
        map.put("ctx/territory", "GB");
//        map.put("ctx/composer_name", "Silvia Blake");
//        map.put("ctx/time", "2016-02-22T12:57:20.706Z");
//        map.put("ctx/id_namespace", "HOSPITAL-NS");
//        map.put("ctx/id_scheme", "HOSPITAL-NS");

        String outMap = JsonUtil.toJsonString(map);

        //rebuild from map
        org.openehr.jaxb.rm.Composition newComposition = converter.toComposition(jaxbTemplate, map);
        //convert it into an internal composition
        marshaller.marshal(new JAXBElement<>( new QName("http://schemas.openehr.org/v1", "composition"),
                org.openehr.jaxb.rm.Composition.class, null, newComposition), stringWriter);
        byte[] bytes = stringWriter.getBuffer().toString().getBytes();

        Composition lastComposition = contentBuilder.importCanonicalXML(new ByteArrayInputStream(bytes));
        assertNotNull(lastComposition);

    }

    @Test
    public void testThinkEhrLib2() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

//        String document = documentList[5];
        String document = "RIPPLE_conformanceTesting_RAW.xml";
        System.out.println("=============================================="+document);
        String documentPath = path + document;
        InputStream is = new FileInputStream(new File(documentPath));
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "");
        Composition composition = content.importCanonicalXML(is);
        assertNotNull(composition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(composition);
        String stringified = jsonMapToString(retmap);
        File tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_RAWJSON", ".json");
        FileUtils.writeStringToFile(tempfile, stringified);


        String templateId = composition.getArchetypeDetails().getTemplateId().getValue();
        Map<String, Object> map = jsonCompositionConverter.fromComposition(templateId, composition);

        assertNotNull(map);
        map.put("ctx/language", "en");
        map.put("ctx/territory", "GB");
//        map.put("ctx/composer_name", "Silvia Blake");
//        map.put("ctx/time", "2016-02-22T12:57:20.706Z");
//        map.put("ctx/id_namespace", "HOSPITAL-NS");
//        map.put("ctx/id_scheme", "HOSPITAL-NS");

        String outMap = JsonUtil.toJsonString(map);

        //for REST API testing
        tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_FLATJSON", ".json");
        FileUtils.writeStringToFile(tempfile, outMap);

        //rebuild from string map
        Map newMap = FlatJsonUtil.inputStream2Map(new StringReader(outMap));


        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, newMap);
        assertNotNull(lastComposition);

        //storeComposition a temp file and write the exported XML into: C:\Users\<current_user>\AppData\Local\Temp
        byte[] exportXml = content.exportCanonicalXML(lastComposition, true);
        tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_ThinkEhrTest", ".xml");
        FileUtils.writeStringToFile(tempfile, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + new String(exportXml));

        System.out.println("-------> Test result written in:"+tempfile.getAbsolutePath());

    }

    @Test
    public void testThinkEhrLib3() throws Exception {
//        String templateId = "IDCR - Laboratory Order.v0";
//        String templateId = "LCR Medication List.v0";
//        String templateId = "IDCR - Immunisation summary.v0";
//        String templateId = "IDCR Problem List.v1";
//        String templateId = "IDCR - Relevant contacts.v0";
//        String templateId = "NCHCD - Clinical notes.v0";
//        String templateId = "IDCR - Problem List.v1";
        String templateId = "EHRN Yoga service.v0";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        //get a flat json test file
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/IDCR-LabReportRAW1_FLATJSON_JOSH2.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/LCR_Medication_List.v0.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/IDCR Problem List.v1.FLAT.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/IDCR_adverse_reaction_listv1.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/IDCR - Immunisation summary.v0.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_10.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_12.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/will_1.flat.json");
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/EHRN Yoga service.v0.flat.json");
        Map map = FlatJsonUtil.inputStream2Map(fileReader);
        //hack map for Marand's library
//        map.put("ctx/language", "en");
//        map.put("ctx/territory", "GB");
        //rebuild from map

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);

//        Composition lastComposition = jsonCompositionConverter.toComposition("IDCR - Adverse Reaction List.v1", map);
        assertNotNull(lastComposition);

        //we serialize the composition
        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, true);
        Map<String, Object>retmap = inspector.process(lastComposition);
        String stringMap = jsonMapToString(retmap);

        assertNotNull(stringMap);

        //rebuild the composition from the map
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);

        Composition newComposition = content.buildCompositionFromJson(stringMap);

        assertNotNull(newComposition);

        //storeComposition a temp file and write the exported XML into: C:\Users\<current_user>\AppData\Local\Temp
        content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "");
        byte[] exportXml = content.exportCanonicalXML(lastComposition, true, false);
        System.out.println(new String(exportXml));

    }

    @Test
    public void testContainmentMetaData() throws Exception {
//        String fileId = "RIPPLE_conformanceTesting_RAW";
//        String templateId = "RIPPLE - Conformance Test template";
        String fileId = "IDCR-LabReportRAW1";
        String templateId = "IDCR - Laboratory Test Report.v0";

//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        //get a flat json test file
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/"+fileId+"_FLATJSON.json");
        Map map = FlatJsonUtil.inputStream2Map(fileReader);
        //rebuild from map

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);
        assertNotNull(lastComposition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(lastComposition);
        Map<String, String> ltreeMap = inspector.getLtreeMap();

        FileWriter fileWriter = new FileWriter("/Development/Dropbox/eCIS_Development/samples/"+fileId+"_CONTAINS.txt");

        for (Map.Entry entry: ltreeMap.entrySet()){
            fileWriter.write(inspector.getTreeRootArchetype()+"."+entry.getKey().toString()); //labels
            fileWriter.write(",");
            fileWriter.write(entry.getValue().toString()); //path
            fileWriter.write("\n");
            fileWriter.flush();
        }

//        MapInspector mapInspector = new MapInspector();
//        mapInspector.inspect(retmap);
//        ArrayDeque<Map<String, Object>> stack = (ArrayDeque)mapInspector.getStack();

    }

    @Test
    public void testThinkEhrLibFaulty() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

//        String document = "faulty_jaxb_itemtree.xml";
//        String document = "COLNEC-medication.xml";
        String document = "ticket_32.xml";
        System.out.println("=============================================="+document);
        String documentPath = "C:\\Development\\Dropbox\\eCIS_Development\\test\\" + document;
        InputStream is = new FileInputStream(new File(documentPath));
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "");
        Composition composition = content.importCanonicalXML(is);
        assertNotNull(composition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(composition);
        String stringified = jsonMapToString(retmap);
        File tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_RAWJSON", ".json");
        FileUtils.writeStringToFile(tempfile, stringified);


        String templateId = composition.getArchetypeDetails().getTemplateId().getValue();
        Map<String, Object> map = jsonCompositionConverter.fromComposition(templateId, composition);

        assertNotNull(map);

        map.put("ctx/language", "en");
        map.put("ctx/territory", "GB");
//        map.put("ctx/composer_name", "Silvia Blake");
//        map.put("ctx/time", "2016-02-22T12:57:20.706Z");
//        map.put("ctx/id_namespace", "HOSPITAL-NS");
//        map.put("ctx/id_scheme", "HOSPITAL-NS");

        String outMap = JsonUtil.toJsonString(map);

        //for REST API testing
        tempfile = File.createTempFile(document.substring(0, document.lastIndexOf("."))+"_FLATJSON", ".json");
        FileUtils.writeStringToFile(tempfile, outMap);

        //rebuild from string map
        Map newMap = FlatJsonUtil.inputStream2Map(new StringReader(outMap));

    }

    @Test
    public void testFlatJson() throws Exception {
//        String templateId = "COLNEC Medication";
//        String templateId = "IDCR - Service Request.v0";
//        String templateId = "GEL - Generic Lab Report import.v0";
//        String templateId = "DiADeM Assessment.v1";
//        String templateId = "Ripple Dashboard Cache.v1";
//        String templateId = "IDCR - Adverse Reaction List.v1";

//        String templateId = "IDCR Problem List.v1";
        String templateId = "Smart Growth Chart Data.v0";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        I_FlatJsonCompositionConverter jsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledge);

        //get a flat json test file
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/COLNEC_Medication_FLAT.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/Ian-mail-27-01-17.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_32.flat.json");
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/CR40.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ripple_dashboard_cache.flat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/ticket_37.flat.json");
        Map map = FlatJsonUtil.inputStream2Map(fileReader);

        Composition lastComposition = jsonCompositionConverter.toComposition(templateId, map);

        assertNotNull(lastComposition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(lastComposition);
        String stringified = jsonMapToString(retmap);

        assertNotNull(stringified);

        //time to rebuild from serialization :)
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        Composition newComposition = content.buildCompositionFromJson(stringified);

        assertNotNull(newComposition);


        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();
        String jsonString;

        //=================== FLAT JSON ==========================
        map = jsonCompositionConverter.fromComposition(templateId, newComposition);

        jsonString = gson.toJson(map);

        assertNotNull(map);

        System.out.println(jsonString);


        //ECIS FLAT =============================================
        Map<String, String> testRetMap = new EcisFlattener().render(newComposition);

        jsonString = gson.toJson(testRetMap);


        System.out.println(jsonString);

    }

    @Test
    public void testGenerateOtherContext() throws Exception {
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "Ripple Dashboard Cache.v1");

        Locatable generated = contentBuilder.generate();

        assertNotNull(generated);
     }
}