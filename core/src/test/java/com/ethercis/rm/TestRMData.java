package com.ethercis.rm;

import com.ethercis.ehr.building.*;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.LocatableHelper;
import com.ethercis.ehr.util.RMDataSerializer;
import openEHR.v1.template.TEMPLATE;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.ItemTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TestRMData {

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
	public void testBuilder() throws Exception {
//		I_ResourceService service = (I_ResourceService) ClusterInfo.getRegisteredService(controller, "ResourceService", "1.0", new Object[] {null});

//		RmMapBinding binder = new RmMapBinding(controller);
		Archetype archetype = knowledge.retrieveArchetype("openEHR-EHR-EVALUATION.evaluation_test_data_types.v1");
		String templateId = "test data types";
		
		//build the archetype map manually...
		knowledge.retrieveArchetype("openEHR-EHR-CLUSTER.cluster_test_data_types.v1");
		
		Map<String, String> props = new HashMap<String, String>();
		props.put("archetypeId", "openEHR-EHR-EVALUATION.evaluation_test_data_types.v1");
		props.put("templateId", "test data types");
		
		TEMPLATE testTemplate = knowledge.retrieveOpenehrTemplate("test data types");
		Flattener flattener = new Flattener();
		
		Archetype instance = flattener.toFlattenedArchetype(testTemplate, knowledge.getArchetypeMap());
		
		
		assertNotNull(instance);
		
		//try to build an actual COMPOSITION from the instance...
		OetBinding generator = I_RmBinding.getInstance();
		Evaluation eval = (Evaluation)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

		assertNotNull(eval);
		
//
//		Activity activity = (Activity) composition.itemAtPath("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]");
//		
//		assertNotNull(activity);
//		
//		Activity newact = (Activity) activity.clone();
		
		//increment activity id
		
		
//		assertNotNull(newact.parentPath(null));

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance();
        String mapjson = inspector.dbEncode(eval);
//		MapUtils.debugPrint(new PrintStream(System.out), "DEBUG_STRUCTURE", retmap);
//
//		GsonBuilder builder = new GsonBuilder();
//		builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//		Gson gson = builder.setPrettyPrinting().create();
//		String mapjson = gson.toJson(retmap);
		System.out.println(mapjson);

        //----------------------------------------
//        RMStructureHandler handler = new RMStructureHandler(ClusterController.instance(), eval);
//
//        DvProportion prop = new DvProportion(new Double(1), new Double(16), ProportionKind.FRACTION, 0);

//        handler.getElementWrapperAt("/data[at0001]/items[openEHR-EHR-CLUSTER.cluster_test_data_types.v1]/items[at0012]");

//        handler.setValueElementWrapper("/data[at0001]/items[openEHR-EHR-CLUSTER.cluster_test_data_types.v1]/items[at0012]", prop);
//
//        RMDataSerializer serializer = new RMDataSerializer(eval);
//        serializer.write2tempfile();
//        serializer.write2file("C:\\TMP\\rmdata_test.dat");
//
//        RMDataSerializer newinstance = RMDataSerializer.getInstance("C:\\TMP\\rmdata_test.dat");
//        Locatable loc = newinstance.getLocatable();
//
//        assertEquals("does not match", eval, loc);
		
		
	}


    @Test
    public void testSerializer() throws Exception {
        String templateId = "COLNEC_history_of_past_illness.v0";

//        I_ResourceService service = (I_ResourceService) ClusterInfo.getRegisteredService(controller, "ResourceService", "1.0", new Object[] {null});
        LocatableBuilder builder = new LocatableBuilder(knowledge);

        //build the archetype map manually...
//        knowledge = service.getKnowledgeManager();
//        knowledge.retrieveArchetype("openEHR-EHR-EVALUATION.evaluation_test_data_types.v1");
//        knowledge.retrieveArchetype("openEHR-EHR-CLUSTER.cluster_test_data_types.v1");

        Locatable obj = builder.createOetInstance(templateId, GenerationStrategy.MAXIMUM);
        assertNotNull(obj);

        //do some traversal

        Evaluation evaluation = (Evaluation)obj;

        Locatable item = ((ItemTree)evaluation.getData()).getItems().get(0);

        RMDataSerializer serializer = new RMDataSerializer(item);
//        serializer.write2tempfile();
        serializer.write2file("C:\\TMP\\rmdata_test.dat");

        RMDataSerializer newinstance = RMDataSerializer.getInstance("C:\\TMP\\rmdata_test.dat");
        Locatable loc = newinstance.getLocatable();

        assertEquals("does not match", obj, loc);

    }

    @Test
    public void testLocallySerialized() throws Exception {
        LocatableHelper locatableHelper = new LocatableHelper();
        String templateId = "section  observation test";

//        I_ResourceService service = (I_ResourceService) ClusterInfo.getRegisteredService(controller, "ResourceService", "1.0", new Object[] {null});
        LocatableBuilder builder = new LocatableBuilder(knowledge);

        //build the archetype map manually...
//        knowledge = service.getKnowledgeManager();
        knowledge.retrieveArchetype("openEHR-EHR-EVALUATION.evaluation_test_data_types.v1");
        knowledge.retrieveArchetype("openEHR-EHR-CLUSTER.cluster_test_data_types.v1");
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.section_observation_test.v2");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.visual_acuity_simple_test.v1");
        knowledge.retrieveArchetype("openEHR-EHR-OBSERVATION.visual_acuity.v1");

        Locatable obj = builder.createOetInstance(templateId, GenerationStrategy.MAXIMUM);
        assertNotNull(obj);

        //do some traversal

        Composition composition = (Composition)obj;

        //get the event series of the observation
        Object item = composition.itemAtPath("/content[openEHR-EHR-SECTION.visual_acuity_simple_test.v1 and name/value='Visual Acuity Simple Test']/items[at0025]/items[openEHR-EHR-OBSERVATION.visual_acuity.v1 and name/value='Visual Acuity']/data[at0001]/events[at0002 and name/value='Visual Acuity Measurement']");

        if (!(item instanceof PointEvent))
            fail("wrong item selected");

        History history = ((PointEvent)item).getParent();

        PointEvent newEvent = (PointEvent) locatableHelper.clone(((Locatable)item));

        locatableHelper.insertHistoryEvent(history, newEvent);

        Object newitem = composition.itemAtPath("/content[openEHR-EHR-SECTION.visual_acuity_simple_test.v1 and name/value='Visual Acuity Simple Test']/items[at0025]/items[openEHR-EHR-OBSERVATION.visual_acuity.v1 and name/value='Visual Acuity']/data[at0001]/events[at0003 and name/value='Visual Acuity Measurement']");

        assertNotNull(newitem);

        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

        content.setEntryData(composition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);

        Map retmap = inspector.process(composition);

        MapUtils.debugPrint(System.out, "COMPOSITION", retmap);

        //rebuild a new composition from the saved data set

//        Composition newComposition = (Composition)builder.createOetInstance(templateId, GenerationStrategy.MAXIMUM);

        I_ContentBuilder newContent = I_ContentBuilder.getInstance(null, I_ContentBuilder.OET, knowledge, templateId);

        Composition newComposition = newContent.buildCompositionFromJson(content.getEntry());

        assertNotNull(newComposition);

    }



}
