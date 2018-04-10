package com.ethercis.ehr.building;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.composition.Composition;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class OPTBuilderTest extends TestCase {
    //	ClusterController controller;
    I_KnowledgeCache knowledge;
    final String resourcePath = "src/test/resources";

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", resourcePath+"/shared_knowledge/archetypes");
        props.put("knowledge.path.template", resourcePath+"/shared_knowledge/templates");
        props.put("knowledge.path.opt", resourcePath+"/shared_knowledge/operational_templates");
        props.put("knowledge.cachelocatable", "true");
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
          I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "UK AoMRC Outpatient Letter");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "LCR Problem List.opt");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        Composition composition = contentBuilder.generateNewComposition();

        TestCase.assertNotNull(composition);

        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, true);

        Map serialized = compositionSerializer.process(composition);

        TestCase.assertNotNull(serialized);
    }

}