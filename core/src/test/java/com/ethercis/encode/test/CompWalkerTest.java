package com.ethercis.encode.test;

import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_RmBinding;
import com.ethercis.ehr.building.OetBinding;
import com.ethercis.ehr.building.XMLBinding;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.json.writer.DvDateTimeAdapter;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.json.JsonUtil;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.ehr.util.MapInspector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import openEHR.v1.template.TEMPLATE;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CompWalkerTest {
    I_KnowledgeCache knowledge;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        knowledge = new KnowledgeCache(null, props);

        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");
    }


	@Test
	public void testXMLComposition() throws Exception {
		InputStream is = new FileInputStream(new File("core/resources/compositions/composition.xml"));
		CompositionDocument cd = CompositionDocument.Factory.parse(is);
		COMPOSITION comp = cd.getComposition();

		Object o = comp.getTerritory();
		
		XMLBinding binding = new XMLBinding();
		Object rmObj = binding.bindToRM(comp);
		
		assertNotNull(rmObj);
		assertEquals("Composition",rmObj.getClass().getSimpleName());
		assertNotNull(((Composition) rmObj).getContent());

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
		Map<String, Object>retmap = inspector.process((Composition) rmObj);
		MapUtils.debugPrint(new PrintStream(System.out), "DEBUG_STRUCTURE", retmap);
		
//		Gson json = new GsonBuilder().setPrettyPrinting().create();
//		String mapjson = json.toJson(retmap);
//		System.out.println(mapjson);
		

//		DataValueAdapter adapter = new DataValueAdapter();
		GsonBuilder builder = new GsonBuilder();
		
		//register TypeAdapters for all known datatypes we can serialize/parse...
//		adapter.setBuilderAdapters(builder);
//		builder.registerTypeAdapter(DataValue.class, adapter);
//		
		builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//		builder.registerTypeAdapter(DvQuantity.class, adapter);
//		builder.registerTypeAdapter(DvText.class, adapter);
		
		Gson gson = builder.setPrettyPrinting().create();
		String mapjson = gson.toJson(retmap);
		System.out.println(mapjson);
				
		//try to rebuild the value map
		Map<String, String> pathmap = new JsonUtil().flatten(mapjson);
		for (String path: pathmap.keySet()) {
			System.out.println(path+"="+pathmap.get(path));
		}
	}

    @Test
    public void testPathInComposition() throws Exception {
        String archetypeId = "openEHR-EHR-COMPOSITION.prescription.v1";
        String templateId = "prescription";

        TEMPLATE prescription = knowledge.retrieveOpenehrTemplate("prescription");

        Flattener flattener = new Flattener();

        Archetype instance = flattener.toFlattenedArchetype(prescription, knowledge.getArchetypeMap());

        assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        assertNotNull(composition);

        I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(composition);

        MapInspector mapInspector = new MapInspector();
        mapInspector.inspect(retmap);
        ArrayDeque<Map<String, Object>> stack = (ArrayDeque)mapInspector.getStack();

        //check the path as found in the map compared to the one generated by the Template Editor
        String nameOfMedicationPath = "/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]";

        Object[] stackarray = stack.toArray();

        for (Object o: stackarray){
            Map<String, Object> map = (Map)o;
            String name = (String) map.get(CompositionSerializer.TAG_NAME);
            if (name.equals("Denomination")){
                String path = (String) map.get(CompositionSerializer.TAG_PATH);
                assertEquals(nameOfMedicationPath, path);
            }

        }

        Map<String, String> testRetMap = new EcisFlattener().render(composition);
        assertNotNull(testRetMap);
    }

}
