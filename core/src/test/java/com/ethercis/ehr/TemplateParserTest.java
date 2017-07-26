package com.ethercis.ehr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import openEHR.v1.template.Archetyped;
import openEHR.v1.template.COMPOSITION;
import openEHR.v1.template.INSTRUCTION;
import openEHR.v1.template.ITEMTREE;
import openEHR.v1.template.SECTION;
import openEHR.v1.template.TEMPLATE;

import org.junit.Test;
import org.openehr.am.template.JaxbUtil;

public class TemplateParserTest {

	@Test
	public void testParseSimpleTemplate() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("prescription.oet");
		TEMPLATE template = (TEMPLATE) JaxbUtil.unmarshal(new StreamSource(is),TEMPLATE.class);


		assertEquals("template name wrong", "Prescription", template.getName());
		
		Archetyped def = template.getDefinition();
		
		assertTrue("type wrong", def instanceof COMPOSITION);
		
		COMPOSITION composition = (COMPOSITION) def;
		
		assertEquals("composition archetypeId wrong", 
				"openEHR-EHR-COMPOSITION.prescription.v1", 
				composition.getArchetypeId());
		
		assertTrue("type wrong", composition.getContentArray(0) instanceof SECTION);
		
		SECTION section = (SECTION) composition.getContentArray(0);
		
		assertEquals("section archetypeId wrong", 
				"openEHR-EHR-SECTION.medications.v1",
				section.getArchetypeId());
		
		assertEquals("section path wrong", "/content", section.getPath());		
		assertTrue("type wrong", section.getItemArray(0) instanceof INSTRUCTION);
		
		INSTRUCTION instruction = (INSTRUCTION) section.getItemArray(0);
		
		assertEquals("instruction archetypeId wrong",
				"openEHR-EHR-INSTRUCTION.medication.v1", instruction.getArchetypeId());
		
		assertEquals("instruction path wrong", "/items", instruction.getPath());
		
		assertTrue("itemTree type wrong", 
				instruction.getActivityDescriptionArray(0) instanceof ITEMTREE);
		
		ITEMTREE itemtree = (ITEMTREE) instruction.getActivityDescriptionArray(0);
		
		assertEquals("itemTree archetypeId wrong", 
				"openEHR-EHR-ITEM_TREE.medication_mod.v1", itemtree.getArchetypeId());
		
		assertEquals("itemTree path wrong", "/activities[at0001]/description",
				itemtree.getPath());		
	}	

}
