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

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.archetyped.Pathable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;

import java.util.List;
import java.util.Set;

/**
 * Entry type for evaluation statements. Instances of this class are immutable.
 *
 * @author Rong Chen
 * @version 1.0
 */
public final class Evaluation extends CareEntry {

    /**
     * Create an Evaluation
     *
     * @param uid
     * @param archetypeNodeId
     * @param name
     * @param subject
     * @param provider
     * @param protocol           null if unspecified
     * @param actID              null if unspecified
     * @param guidelineId        null if unspecified
     * @param otherParticipations null if unspecified
     * @param data
     * @throws IllegalArgumentException if data null
     */
    @FullConstructor
            public Evaluation(@Attribute(name = "uid") UIDBasedID uid,
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
                              @Attribute(name = "protocol") ItemStructure protocol,
                              @Attribute(name = "guidelineId") ObjectRef guidelineId,
                              @Attribute(name = "data", required = true) ItemStructure data,
                              @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService) {
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links,
                parent, language, encoding, subject, provider, workflowId, 
                otherParticipations, protocol, guidelineId, terminologyService);

        if (data == null) {
            throw new IllegalArgumentException("null data");
        }
        this.data = data;
    }

    /**
     * The data of this evaluation, in the form of a spatial
     * data structure.
     *
     * @return data of this evaluation
     */
    public ItemStructure getData() {
        return data;
    }

    /**
     * String The path to an item relative to the root of this
     * archetyped structure.
     *
     * @param item
     * @return string path
     */
    public String pathOfItem(Locatable item) {
        return null;  // todo: implement this method
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

    // POJO start
    Evaluation() {
    }

    void setData(ItemStructure data) {
        this.data = data;
    }
    // POJO end

    /* fields */
    private ItemStructure data;
    
    /* static fields */
    public static final String DATA = "data";	
}

