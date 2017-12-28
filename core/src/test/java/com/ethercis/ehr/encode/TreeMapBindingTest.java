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

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class TreeMapBindingTest extends TestCase {

    I_KnowledgeCache knowledge;
    Composition composition;
    private String resourcesRootPath;

    @Before
    public void setUp() throws Exception {

        setResourcesRootPath();

        Properties props = new Properties();
        props.put("knowledge.path.archetype", resourcesRootPath + "/knowledge/archetypes");
        props.put("knowledge.path.template", resourcesRootPath + "/knowledge/templates");
        props.put("knowledge.path.opt", resourcesRootPath + "/knowledge/operational_templates");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(knowledge, "LCR Problem List.opt");
//        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(I_ContentBuilder.OPT, knowledge, "action test");

        composition = contentBuilder.generateNewComposition();
    }

    private void setResourcesRootPath() {
        resourcesRootPath = getClass()
            .getClassLoader()
            .getResource(".")
            .getFile();
    }

    @Test
    public void testTreeMapBinding() throws Exception {
        File file =
            new File(
                getClass()
                    .getClassLoader()
                    .getResource("flat_json_input/IDCR Problem List.v1.FLAT.json")
                    .getFile());
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/ProblemList_1FLAT.json");
        FileReader fileReader = new FileReader(file);

        Map<String, String> inputMap = FlatJsonUtil.inputStream2Map(fileReader);

        assertNotNull(inputMap);

        TreeMapBinding treeMapBinding = new TreeMapBinding(inputMap);

        treeMapBinding.process(composition);
    }
}