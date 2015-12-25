package com.ethercis.ehr.encode;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.TreeMapBinding;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.composition.Composition;

import java.io.FileReader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class TreeMapBindingTest extends TestCase {

    I_KnowledgeCache knowledge;
    Composition composition;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "LCR Problem List.opt");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        composition = contentBuilder.generateNewComposition();
    }

    @Test
    public void testTreeMapBinding() throws Exception {
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/ProblemList_1FLAT.json");

        Map<String, String> inputMap = FlatJsonUtil.inputStream2Map(fileReader);

        assertNotNull(inputMap);

        TreeMapBinding treeMapBinding = new TreeMapBinding(inputMap);

        treeMapBinding.process(composition);
    }
}