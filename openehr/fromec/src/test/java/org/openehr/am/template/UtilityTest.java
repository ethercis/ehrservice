	package org.openehr.am.template;

import org.openehr.am.archetype.constraintmodel.CComplexObject;

public class UtilityTest extends TemplateTestBase {

	public void testNextNodeId() throws Exception {
		assertEquals("at0001", flattenerNew.formatNodeId(1));
		assertEquals("at0010", flattenerNew.formatNodeId(10));
		assertEquals("at0100", flattenerNew.formatNodeId(100));
		assertEquals("at1000", flattenerNew.formatNodeId(1000));
		assertEquals("at9999", flattenerNew.formatNodeId(9999));
		assertEquals("at10000", flattenerNew.formatNodeId(10000));
		assertEquals("at100000", flattenerNew.formatNodeId(100000));
	}
	
	public void testParseNodeId() throws Exception {
		assertEquals(1, flattenerNew.parseNodeId("at0001"));
		assertEquals(10, flattenerNew.parseNodeId("at0010"));
		assertEquals(100, flattenerNew.parseNodeId("at0100"));
		assertEquals(1000, flattenerNew.parseNodeId("at1000"));
		assertEquals(9999, flattenerNew.parseNodeId("at9999"));
		assertEquals(10000, flattenerNew.parseNodeId("at10000"));
		assertEquals(1, flattenerNew.parseNodeId("at0001.1"));
		assertEquals(100, flattenerNew.parseNodeId("at0100.2"));
	}
	
	public void testFindLargestNodeId() throws Exception {
		archetype = loadArchetype("openEHR-EHR-SECTION.find_largest_node_id.v1.adl");
		assertEquals(4, flattenerNew.findLargestNodeId(archetype));
	}
	
	public void testFindLargestNodeId2() throws Exception {
		archetype = loadArchetype("openEHR-EHR-SECTION.find_largest_node_id_2.v1.adl");
		assertEquals(10, flattenerNew.findLargestNodeId(archetype));
	}
	
	public void testAdjustNodeIds() throws Exception {
		archetype = loadArchetype("openEHR-EHR-EVALUATION.structured_summary.v1.adl");
		expected = loadArchetype("openEHR-EHR-EVALUATION.adjusted_node_ids.v1.adl");
				
		CComplexObject root = archetype.getDefinition();
		long count = flattenerNew.adjustNodeIds(root, 10);
		
		assertEquals("Unexpected total adjusted nodeIds", 15, count);
		assertCComplexObjectEquals("failed to adjust nodeIds",
				expected.getDefinition(), root);		
	}	
}
