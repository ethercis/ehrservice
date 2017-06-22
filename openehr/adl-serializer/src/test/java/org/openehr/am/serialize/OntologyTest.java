/*
 * component:   "openEHR Reference Implementation"
 * description: "Class OntologyTest"
 * keywords:    "archetype"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004,2005,2006 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL:$"
 * revision:    "$LastChangedRevision: $"
 * last_change: "$LastChangedDate: $"
 */
package org.openehr.am.serialize;

import java.util.ArrayList;
import java.util.List;

import org.openehr.am.archetype.ontology.ArchetypeOntology;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.archetype.ontology.OntologyBinding;
import org.openehr.am.archetype.ontology.OntologyBindingItem;
import org.openehr.am.archetype.ontology.OntologyDefinitions;
import org.openehr.am.archetype.ontology.Query;
import org.openehr.am.archetype.ontology.QueryBindingItem;
import org.openehr.am.archetype.ontology.TermBindingItem;

/**
 * Simple ontology serialization test
 * 
 * @author Rong Chen
 */
public class OntologyTest extends SerializerTestBase {

	public OntologyTest(String test) {
		super(test);
	}

	public void testPrintOntology() throws Exception {
		ArchetypeTerm item = new ArchetypeTerm("at0001", "text at0001",
				"desc at0001");
		List<ArchetypeTerm> items = new ArrayList<ArchetypeTerm>();
		items.add(item);
		item = new ArchetypeTerm("at0002", "text at0002", "desc at0002");
		items.add(item);
		OntologyDefinitions definitions = new OntologyDefinitions("en", items);
		List<OntologyDefinitions> termDefinitionsList = 
			new ArrayList<OntologyDefinitions>();
		termDefinitionsList.add(definitions);

		item = new ArchetypeTerm("ac0001", "text ac0001", "desc ac0001");
		items = new ArrayList<ArchetypeTerm>();
		items.add(item);
		item = new ArchetypeTerm("ac0002", "text ac0002", "desc ac0002");
		items.add(item);
		definitions = new OntologyDefinitions("en", items);
		List<OntologyDefinitions> constraintDefinitionsList = 
			new ArrayList<OntologyDefinitions>();
		constraintDefinitionsList.add(definitions);

		List<String> terminologies = new ArrayList<String>();
		terminologies.add("local");

		List<String> terms = new ArrayList<String>();
		terms.add("[local::100000]");
		TermBindingItem termBindItem = new TermBindingItem("at0001", terms);
		List<OntologyBindingItem> termBindList = 
			new ArrayList<OntologyBindingItem>();
		termBindList.add(termBindItem);
		terms = new ArrayList<String>();
		terms.add("[local::200000]");
		termBindItem = new TermBindingItem("at0002", terms);
		termBindList.add(termBindItem);
		OntologyBinding ontologyBind = new OntologyBinding("local",
				termBindList);
		List<OntologyBinding> termBindingList = new ArrayList<OntologyBinding>();
		termBindingList.add(ontologyBind);

		Query query = new Query("http://terminology.org?terminology_id=" +
				"snomed_ct&&has_relation=[102002];with_target=[128004]");
		QueryBindingItem queryBindItem = new QueryBindingItem("ac0001", query);
		List<OntologyBindingItem> constraintBindList = 
			new ArrayList<OntologyBindingItem>();
		constraintBindList.add(queryBindItem);
		ontologyBind = new OntologyBinding("local", constraintBindList);
		List<OntologyBinding> constraintBindingList = 
			new ArrayList<OntologyBinding>();
		constraintBindingList.add(ontologyBind);

		ArchetypeOntology ontology = new ArchetypeOntology("en",
				terminologies, termDefinitionsList, constraintDefinitionsList,
				termBindingList, constraintBindingList);
		clean();
		outputter.printOntology(ontology, out);
		verifyByFile("ontology.adl");
	}
}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is OnotologyTest.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2004-2006
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s): Erik Sundvall
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */
