package com.ethercis.rm;

import com.ethercis.ehr.building.OetBinding;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.I_RmBinding;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;

import openEHR.v1.template.TEMPLATE;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.text.DvText;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OetRmTest {

    //	ClusterController controller;
    I_KnowledgeCache knowledge;

    @Before
    public void setUp() throws Exception {
//		controller = new ClusterController();
//		ResourceService service = new ResourceService();
//		TestService.setnstart(service, controller, "ResourceService", "1.0",
//		new String[][] {
//				{},
//				{}
//			});
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        knowledge = new KnowledgeCache(null, props);
    }
//	archetype = loadArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v2.adl");
//	String templateId = "Blood_pressure";
//	
//	instance = generator.create(archetype, templateId, null,
//			GenerationStrategy.MINIMUM);
//	
//	assertTrue(instance instanceof Observation);
//	
//	Observation obs = (Observation) instance;
//	Archetyped details = obs.getArchetypeDetails();
//	assertNotNull(details);
//	assertEquals("wrong templateId", templateId, details.getTemplateId().toString());
	
	@Test
	public void testOET() throws Exception {
//		Logger.getRootLogger().setLevel(Level.DEBUG);
//		I_ResourceService service = (I_ResourceService) ClusterInfo.getRegisteredService(controller, "ResourceService", "1.0", new Object[] {null});
//		I_KnowledgeManager knowledge = service.getKnowledgeManager();
		
//		RmMapBinding binder = new RmMapBinding(controller);
		Archetype archetype = knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
		String templateId = "prescription";
		
		//build the archetype map manually...
		knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
		knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
		knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");
		
		
		Map<String, String> props = new HashMap<String, String>();
		props.put("archetypeId", "openEHR-EHR-COMPOSITION.prescription.v1");
		props.put("templateId", "prescription");
		
//		Locatable instance = binder.bindToRm(props);
//		SkeletonGenerator generator = SkeletonGenerator.getInstance();
//		Object instance = generator.create(archetype, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM_EMPTY);
		
		TEMPLATE prescription = knowledge.retrieveOpenehrTemplate("prescription");
		Flattener flattener = new Flattener();
		
		Archetype instance = flattener.toFlattenedArchetype(prescription, knowledge.getArchetypeMap());
		
		
		assertNotNull(instance);

        //add some specific values into the composition builder
        Map<SystemValue, Object> values = new HashMap<>();

        values.put(SystemValue.COMPOSER,  PartyIdentified.named("Ludwig"));

		//try to build an actual COMPOSITION from the instance...
		OetBinding generator = I_RmBinding.getInstance(values);
		Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);



		assertNotNull(composition);

//		Activity activity = (Activity) composition.itemAtPath("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]");
//		
//		assertNotNull(activity);
//		
//		Activity newact = (Activity) activity.clone();
		
		//increment activity id
		
		
//		assertNotNull(newact.parentPath(null));

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance();
		Map<String, Object>retmap = inspector.process(composition);
		MapUtils.debugPrint(new PrintStream(System.out), "DEBUG_STRUCTURE", retmap);
		
		String nameOfMedicinePath = 
				"/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]";
		
		ElementWrapper item = (ElementWrapper)composition.itemAtPath(nameOfMedicinePath);
		
		assertNotNull(item);
		
		//sets a value on this item...
		item.getAdaptedElement().setValue((DvText)DvText.parseValue("DV_TEXT,Vitamin C"));
		
		DvText value = (DvText)item.getAdaptedElement().getValue();
		
		assertEquals("Vitamin C", value.getValue());
		
//		Evaluation evaluation  = (Evaluation)instance;
//
//        String serialized = JsonUtil.serializeComposition(composition);
//
//        assertNotNull(serialized);
//
//        Composition rebuilt = JsonUtil.deserializeComposition(serialized);
//
//        assertNotNull(rebuilt);

		
		
	}

    @Test
    public void testOPT() throws Exception {
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        String templateId = "prescription.opt";

        //try to build an actual COMPOSITION from the instance...
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance();
        Map<String, Object>retmap = inspector.process(composition);
        MapUtils.debugPrint(new PrintStream(System.out), "DEBUG_STRUCTURE", retmap);

        String nameOfMedicinePath =
                "/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]";

        ElementWrapper item = (ElementWrapper)composition.itemAtPath(nameOfMedicinePath);

        assertNotNull(item);

        //sets a value on this item...
        item.getAdaptedElement().setValue(DvText.parseValue("DV_TEXT,Vitamin C"));

        DvText value = (DvText)item.getAdaptedElement().getValue();

        assertEquals("Vitamin C", value.getValue());

    }

}
