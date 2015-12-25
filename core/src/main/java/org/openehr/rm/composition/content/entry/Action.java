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
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.terminology.TerminologyService;

/**
 * Used to record a clinical action that has been performed, which may have been ad hoc, 
 * or due to the execution of an Acitivity in an Instruction workflow. Every Action
 * corresponds to a careflow step of some kind or another.
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public final class Action extends CareEntry {
    
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
     * @param protocol
     * @param guidelineId
     * @param terminologyService
     */
    @FullConstructor
            public Action(@Attribute(name = "uid") UIDBasedID uid,
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
            @Attribute(name = "time", required = true) DvDateTime time,
            @Attribute(name = "description", required = true) ItemStructure description,
            @Attribute(name = "ismTransition", required = true) ISMTransition ismTransition,
            @Attribute(name = "instructionDetails") InstructionDetails instructionDetails,
            @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService
            ){
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent,
                language, encoding, subject, provider, workflowId, otherParticipations,
                protocol, guidelineId, terminologyService);
        if (time == null) {
            throw new IllegalArgumentException("null time");
        }
        if (description == null) {
            throw new IllegalArgumentException("null description");
        }
        if (ismTransition == null) {
            throw new IllegalArgumentException("null ismTransition");
        }
        this.time = time;
        this.description = description;
        this.ismTransition = ismTransition;
        this.instructionDetails = instructionDetails;
    }
    
    /**
     * Description of the activity to be performed, in the form of an
     * archetyped structure.
     *
     * @return description
     */
    public ItemStructure getDescription() {
        return description;
    }
    
    /**
     * Details of the Instruction that caused this ACtion to be performed,
     * if there was one.
     *
     * @return instructionDetails
     */
    public InstructionDetails getInstructionDetails() {
        return instructionDetails;
    }
    
    /**
     * Detials of transition in the Instruction state machine caused by
     * this Action.
     *
     * @return ismTransition
     */
    public ISMTransition getIsmTransition() {
        return ismTransition;
    }
    
    /**
     * Point in time at which this action completed.
     *
     * @return time
     */
    public DvDateTime getTime() {
        return time;
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
    Action() {
    }
    
    void setDescription(ItemStructure description) {
        this.description = description;
    }
    
    void setInstructionDetails(InstructionDetails instructionDetails) {
        this.instructionDetails = instructionDetails;
    }
    
    void setIsmTransition(ISMTransition ismTransition) {
        this.ismTransition = ismTransition;
    }
    
    public void setTime(DvDateTime time) {
        this.time = time;
    }
    //POJO end
    
    /* fields */
    private DvDateTime time;
    private ItemStructure description;
    private ISMTransition ismTransition;
    private InstructionDetails instructionDetails;
    
    /* static fields*/
    public static final String DESCRIPTION = "description";
}

