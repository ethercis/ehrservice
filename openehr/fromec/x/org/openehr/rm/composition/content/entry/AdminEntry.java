/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehr.rm.composition.content.entry;

import java.util.List;
import java.util.Set;
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;

import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Pathable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.terminology.TerminologyService;

/**
 * Entry subtype for administrative information, i.e. information about setting up 
 * the clinical process, but not itself clinically relevant. Archetypes will define
 * contained information
 * 
 * @author Yin Su Lim
 * @version 1.0
 */
public class AdminEntry extends Entry {

	/**
	 * @param uid
	 * @param archetypeNodeId
	 * @param name
	 * @param archetypeDetails
	 * @param feederAudit
	 * @param links
	 * @param parent
	 * @param language
	 * @param encoding
	 * @param subject
	 * @param provider
	 * @param workflowId
	 * @param otherParticipations
	 * @param terminologyService
	 */
    @FullConstructor
	public AdminEntry(@Attribute(name = "uid") UIDBasedID uid,
                       @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId,
                       @Attribute(name = "name", required = true) DvText name,
                       @Attribute(name = "archetypeDetails", required = true) Archetyped archetypeDetails,
                       @Attribute(name = "feederAudit") FeederAudit feederAudit,
                       @Attribute(name = "links") Set<Link> links,
                       @Attribute(name = "parent") Pathable parent,
                       @Attribute(name = "language", required = true) CodePhrase language,
                       @Attribute(name = "encoding", required = true) CodePhrase encoding, 
                       @Attribute(name = "subject", required = true) PartyProxy subject,
                       @Attribute(name = "provider") PartyProxy provider,
                       @Attribute(name = "workflowId") ObjectRef workflowId, 
                       @Attribute(name = "otherParticipations") List<Participation> otherParticipations, 
                       @Attribute(name = "data", required = true) ItemStructure data,
                       @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService) {
		super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent,
				language, encoding, subject, provider, workflowId, otherParticipations,
				terminologyService);
		if (data == null) {
			throw new IllegalArgumentException("null data");
		}
		this.data = data;
	}

    /**
     * Gets data of this adminEntry
     * 
     * @return data
     */
	public ItemStructure getData() {
		return data;
	}

	@Override
	public String pathOfItem(Pathable arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> itemsAtPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean pathExists(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pathUnique(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//POJO start
	AdminEntry() {
	}
	
	void setData(ItemStructure data) {
		this.data = data;
	}
	//POJO end
	
	/* field */
	private ItemStructure data;
	
}

