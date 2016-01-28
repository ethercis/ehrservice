package com.ethercis.ehr.building;

import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import openEHR.v1.template.TEMPLATE;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.am.template.FlattenerNew;
import org.openehr.rm.composition.Composition;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ContentBuilderTest extends TestCase {
    //	ClusterController controller;
    I_KnowledgeCache knowledge;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);
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
    public void testSetActionEntry() throws Exception {
        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");

        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.action_test.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ACTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication.v1");

        Logger.getRootLogger().setLevel(Level.DEBUG);
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

//    @Test
    public void _testSetInstructionEntry() throws Exception {
        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");

        Logger.getRootLogger().setLevel(Level.DEBUG);
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

            "IDCR Lab Order RAW1.xml" ,
            "IDCR Procedures List_1 RAW.xml",
            "IDCR Problem List.v1.xml",
            "Vital_signs_TEST.xml",
            "IDCR-LabReportRAW1.xml",
            "RIPPLE_conformanceTesting_RAW.xml"


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
            I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "");
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

            Map<String, String> testRetMap = EcisFlattener.renderFlat(newComposition);

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
        Logger.getRootLogger().setLevel(Level.DEBUG);
        StringBuffer sb = new StringBuffer();
        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/ProblemList_2FLAT.json")).forEach(line -> sb.append(line));
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "IDCR Problem List.v1");

        Composition newComposition = content.buildCompositionFromJson(sb.toString());

        assertNotNull(newComposition);

        String xml = new String(content.exportCanonicalXML(newComposition, true));

        System.out.println(xml);

        Map<String, String> testRetMap = EcisFlattener.renderFlat(newComposition);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

        String jsonString = gson.toJson(testRetMap);

        System.out.println(jsonString);

    }


}