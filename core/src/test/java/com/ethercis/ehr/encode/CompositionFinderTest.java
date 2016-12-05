package com.ethercis.ehr.encode;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import com.ethercis.ehr.building.XMLBinding;
import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class CompositionFinderTest extends TestCase {
    I_KnowledgeCache knowledge;

    @Before
    public void setUp() throws Exception {
//        Properties props = new Properties();
//        props.put("knowledge.path.archetypes", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
//        props.put("knowledge.path.templates", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
//        knowledge = new KnowledgeCache(RunTimeSingleton.instance(), props);
//
//        //build the archetype map manually... when deployed, the service populate its map from scanning the directory
//        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.prescription.v1");
//        knowledge.retrieveArchetype("openEHR-EHR-SECTION.medications.v1");
//        knowledge.retrieveArchetype("openEHR-EHR-INSTRUCTION.medication.v1");
//        knowledge.retrieveArchetype("openEHR-EHR-ITEM_TREE.medication_mod.v1");
    }

    @Test
    public void testJSONPathInComposition() throws Exception {
        InputStream is = new FileInputStream(new File("core/resources/compositions/composition.xml"));
        CompositionDocument cd = CompositionDocument.Factory.parse(is);
        COMPOSITION comp = cd.getComposition();

        Object o = comp.getTerritory();

        XMLBinding binding = new XMLBinding();
        Object rmObj = binding.bindToRM(comp);

        assertNotNull(rmObj);
        assertEquals("Composition",rmObj.getClass().getSimpleName());
        assertNotNull(((Composition) rmObj).getContent());

        Composition composition = ((Composition)rmObj);

        assertNotNull(composition);

//        CompositionFinder compositionFinder = new CompositionFinder();
//
//        compositionFinder.invalidateContent(composition);
    }
}