package com.ethercis.ehr.json;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.util.MapInspector;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JsonMapInspectorTest extends TestCase {

    final String resourcePath = "src/test/resources";

    @Ignore
    @Test
    public void testTraversal() throws FileNotFoundException {
        FileReader fileReader = new FileReader(resourcePath+"/samples/Medication List_1FLAT.json");

        Map<String, String> inputMap = FlatJsonUtil.inputStream2Map(fileReader);

        assertNotNull(inputMap);

        Map<String, Object> result = FlatJsonUtil.unflattenJSON(inputMap);

        TreeMapNode treeMapNode = new TreeMapNode(result);

        TreeMapNode resultNode = treeMapNode.nextChild("current_medication_list")
                                            .nextChild("medication_and_medical_devices").at(0)
                                            .nextChild("current_medication").at(0)
                                            .nextChild("medication_statement").at(0)
                                            .nextChild("medication_item")
                                            .nextChild("route").at(0);

        assertNotNull(resultNode);
        assertEquals(3, (int)resultNode.size());

        //check siblings
        TreeMapNode node = resultNode.nextChild("code");
        TreeMapNode sibling = node.nextSibling();

        assertTrue("value".equals(sibling.getCurrentKey().toString()) || "terminology".equals(sibling.getCurrentKey().toString()));

        sibling = sibling.nextSibling();

        assertTrue("value".equals(sibling.getCurrentKey().toString()) || "terminology".equals(sibling.getCurrentKey().toString()));

        sibling = sibling.nextSibling();

        assertNull(sibling);

        try {
            resultNode = treeMapNode.nextChild("current_medication_list")
                    .nextChild("medication_and_medical_devices").at(1);
            fail("IllegalArgumentException was expected here...");
        } catch (IllegalArgumentException e){
            ;
        }

        Map<String, Object> attributes = treeMapNode.nextChild("current_medication_list")
                .nextChild("medication_and_medical_devices").at(0)
                .nextChild("current_medication").at(0)
                .nextChild("medication_statement").at(0)
                .nextChild("medication_item")
                .nextChild("route").at(0).asAttributes();

        assertEquals(3, attributes.size());
    }

    @Ignore
    @Test
    public void testFindPath() throws FileNotFoundException {
        FileReader fileReader = new FileReader(resourcePath+"/samples/Medication List_1FLAT.json");

        Map<String, String> inputMap = FlatJsonUtil.inputStream2Map(fileReader);

        assertNotNull(inputMap);

        Map<String, Object> result = FlatJsonUtil.unflattenJSON(inputMap);

        Map attributes = TreeMapNode.findNode(result, "current_medication_list/medication_and_medical_devices:0/current_medication:0/medication_statement:0/medication_item/medication_name").asAttributes();

        assertEquals(3, attributes.size());

        String terminology = (String) TreeMapNode.findNode(result, "current_medication_list/medication_and_medical_devices:0/current_medication:0/medication_statement:0/medication_item/medication_name").asAttributes().get("terminology");

        assertEquals("SNOMED-CT", terminology);
    }

    @Test
    public void testTraversal_Other_Participations() throws Exception {
        FileReader fileReader = new FileReader(resourcePath+ "/samples/DB_serialized_2.json");

        MapInspector mapInspector = new MapInspector();

        mapInspector.inspect(fileReader);

        Collection collection = mapInspector.getStack();

        //check other_parcipations encoding
        String other_participation_1 = "/content[openEHR-EHR-SECTION.plan_requested_actions_rcp.v1]/items[openEHR-EHR-EVALUATION.recommendation.v1]/participation";
        String other_participation_2 = "/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-INSTRUCTION.request-referral.v1]/participation";
        String other_participation_3 = "/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/participation";

        int participation1=0;
        int participation2=0;
        int participation3=0;

        Iterator<Map> iterator = collection.iterator();

        while (iterator.hasNext()){
            Map<String, Object> def = iterator.next();

            if (def.containsKey(CompositionSerializer.TAG_PATH)){
                String path = (String) def.get(CompositionSerializer.TAG_PATH);

                if (path.startsWith(other_participation_1))
                    participation1 += 1;
                else if (path.startsWith(other_participation_2))
                    participation2 += 1;
                else if (path.startsWith(other_participation_3))
                    participation3 += 1;
            }
        }

        assertTrue(participation1 == 3 && participation2 == 3 && participation3 == 3);
        collection.toString();

    }

}