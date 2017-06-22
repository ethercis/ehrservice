package se.acode.openehr.parser;

import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.ontology.TermBindingItem;
import org.openehr.am.archetype.ontology.OntologyBinding;

/**
 * TermBindingTest
 *
 * @author Rong Chen
 * @version 1.0
 */
public class TermBindingTest extends ParserTestBase {

    /**
     * Create new test case
     *
     * @param test
     * @throws Exception
     */
    public TermBindingTest(String test) throws Exception {
        super(test);
    }

    /**
     * Verifies term binding by multiple terminolgies
     * 
     * @throws Exception
     */
    public void testTermBindingWithMultiTerminologies() throws Exception {
        ADLParser parser = new ADLParser(loadFromClasspath(
                "adl-test-entry.term_binding.test.adl"));
        Archetype archetype = parser.parse();

        // verify the first term binding
        OntologyBinding binding = archetype.getOntology().getTermBindingList().get(0);
        assertEquals("wrong binding terminology", "SNOMED_CT", binding.getTerminology());

        TermBindingItem item = (TermBindingItem) binding.getBindingList().get(0);

        assertEquals("wrong local code", "at0000", item.getCode());
        assertEquals("wrong terms size", 1, item.getTerms().size());
        assertEquals("wrong term", "[snomed_ct::1000339]", item.getTerms().get(0));

        // verify the second term binding
        binding = archetype.getOntology().getTermBindingList().get(1);
        assertEquals("wrong binding terminology", "ICD10", binding.getTerminology());

        item = (TermBindingItem) binding.getBindingList().get(0);

        assertEquals("wrong local code", "at0000", item.getCode());
        assertEquals("wrong terms size", 2, item.getTerms().size());
        assertEquals("wrong 1st term", "[icd10::1000]", item.getTerms().get(0));
        assertEquals("wrong 2nd term", "[icd10::1001]", item.getTerms().get(1));
    }
    
    public void testPathBasedBinding() throws Exception {
    	ADLParser parser = new ADLParser(loadFromClasspath(
        	"adl-test-entry.term_binding2.test.adl"));
    	Archetype archetype = parser.parse();

    	OntologyBinding binding = archetype.getOntology().getTermBindingList().get(0);
        assertEquals("wrong binding terminology", "LNC205", binding.getTerminology());

        TermBindingItem item = (TermBindingItem) binding.getBindingList().get(0);

        assertEquals("wrong local code path", 
        		"/data[at0002]/events[at0003]/data[at0001]/item[at0004]", 
        		item.getCode());
        assertEquals("wrong terms size", 1, item.getTerms().size());
        assertEquals("wrong term", "[LNC205::8310-5]", item.getTerms().get(0));

    }
	
	public void testPathBasedBindingWithinInternalReference() throws Exception {
    	ADLParser parser = new ADLParser(loadFromClasspath(
        	"openEHR-EHR-OBSERVATION.test_internal_ref_binding.v1.adl"));
    	Archetype archetype = parser.parse();

    	OntologyBinding binding = archetype.getOntology().getTermBindingList().get(0);
        assertEquals("wrong binding terminology", "DDB00", binding.getTerminology());

       
        TermBindingItem item1 = (TermBindingItem) binding.getBindingList().get(0); 
        assertEquals("wrong terms size", 1, item1.getTerms().size());

        assertEquals("wrong local code path", 
        		"/data[at0001]/events[at0002]/data[at0003]/items[at0004]", 
        		item1.getCode());
        assertEquals("wrong term", "[DDB00::12345]", item1.getTerms().get(0));

		TermBindingItem item2 = (TermBindingItem) binding.getBindingList().get(1); 
        assertEquals("wrong terms size", 1, item2.getTerms().size());

		assertEquals("wrong local code path", 
        		"/data[at0001]/events[at0005]/data[at0003]/items[at0004]", 
        		item2.getCode());
        assertEquals("wrong term", "[DDB00::98765]", item2.getTerms().get(0));
		
		assertTrue(archetype.physicalPaths().contains("/data[at0001]/events[at0002]/data[at0003]/items[at0004]"));
		assertTrue(archetype.physicalPaths().contains("/data[at0001]/events[at0005]/data[at0003]/items[at0004]")); // path within an archetype internal ref. Must be included in the physical paths!
		assertFalse(archetype.physicalPaths().contains("/data[at0001]/events[at9999]/data[at0003]/items[at0004]"));

    }
}

