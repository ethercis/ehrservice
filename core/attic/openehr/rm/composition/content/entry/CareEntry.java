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
 * The abstract parent of all clinical Entry subtypes. A CareEntry defines 
 * protocol and guideline attributes for all clinical Entry subtypes.
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public abstract class CareEntry extends Entry {

    /**
     * Construct an Entry
     *
     * @param archetypeNodeId
     * @param name
     * @param subject
     * @param provider
     * @param protocol            null if unspecified
     * @param actID               null if unspecified
     * @param guidelineId         null if unspecified
     * @param otherParticipations null if unspecified
     * @throws IllegalArgumentException if archetypeNodeId or name null,
     *                                  or subject or provider null or invalid
     */
    protected CareEntry(UIDBasedID uid, String archetypeNodeId, DvText name,
                 Archetyped archetypeDetails, FeederAudit feederAudit,
                 Set<Link> links, Pathable parent, CodePhrase language,
                 CodePhrase encoding, PartyProxy subject, 
                 PartyProxy provider, ObjectRef workflowId,
                 List<Participation> otherParticipations,
                 ItemStructure protocol, ObjectRef guidelineId, 
                 TerminologyService terminologyService) {

        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent, 
        		language, encoding, subject, provider, workflowId, otherParticipations,
        		terminologyService);
        this.protocol = protocol;
        this.guidelineId = guidelineId;
    }

    /**
     * Optional external identifier of guideline creating this action 
     * if relevant
     * 
     * @return guidelineId
     */
    public ObjectRef getGuidelineId() {
    	return guidelineId;
    }
    
    /**
     * Description of the method the information in this entry was arrived at.
     * 
     * @return protocol
     */
    public ItemStructure getProtocol() {
    	return protocol;
    }
    
    //POJO start
    CareEntry() {
    }
    
	public void setGuidelineId(ObjectRef guidelineId) {
		this.guidelineId = guidelineId;
	}
	void setProtocol(ItemStructure protocol) {
		this.protocol = protocol;
	}
	//POJO end
	
    /* fields */        
   private ItemStructure protocol;
   private ObjectRef guidelineId;

   /* static fields */
   public static final String PROTOCOL = "protocol";
}

