package se.acode.openehr.parser;

import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.ontology.QueryBindingItem;
import org.openehr.am.archetype.ontology.OntologyBinding;

import java.util.List;

/**
 * TermBindingTest
 *
 * @author Rong Chen
 * @version 1.0
 */
public class ConstraintBindingTest extends ParserTestBase {

    /**
     * Create new test case
     *
     * @param test
     * @throws Exception
     */
    public ConstraintBindingTest(String test) throws Exception {
        super(test);
    }

    /**
     * Verifies constraint binding by multiple terminolgies
     * 
     * @throws Exception
     */
    public void testConstraintBindingWithMultiTerminologies() throws Exception {
        ADLParser parser = new ADLParser(loadFromClasspath(
                "adl-test-entry.constraint_binding.test.adl"));
        Archetype archetype = parser.parse();
        List<OntologyBinding> list = archetype.getOntology().getConstraintBindingList();
        
        assertEquals("unexpected number of onotology binding", 2, list.size());

        // verify the first constraint binding
        OntologyBinding binding = list.get(0);
        assertEquals("unexpected binding terminology", "SNOMED_CT", binding.getTerminology());

        QueryBindingItem item = (QueryBindingItem) binding.getBindingList().get(0);

        assertEquals("unexpected local code", "ac0001", item.getCode());
        assertEquals("exexpected query", 
        		"http://terminology.org?terminology_id=snomed_ct&&has_relation=102002;with_target=128004", 
        		item.getQuery().getUrl());

        // verify the second constraint binding
        binding = list.get(1);
        assertEquals("unexpected binding terminology", "ICD10", binding.getTerminology());

        item = (QueryBindingItem) binding.getBindingList().get(0);

        assertEquals("unexpected local code", "ac0001", item.getCode());
        assertEquals("exexpected query", 
        		"http://terminology.org?terminology_id=icd10&&has_relation=a2;with_target=b19", 
        		item.getQuery().getUrl());
    }
}

