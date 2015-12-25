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
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;

import java.util.List;
import java.util.Set;

/**
 * Purpose Entry subtype for all clinical data in the past or present,
 * ie which (by the time it is recorded) has already occurred.
 * OBSERVATION data is expressed using the class HISTORY<T>, which
 * guarantees that it is situated in time.
 * <p/>
 * Instances of this class are immutable.
 *
 * @author Rong Chen
 * @version 1.0
 */
public final class Observation extends CareEntry {

    /**
     * Construct an Observation
     *
     * @param uid
     * @param archetypeNodeId
     * @param name
     * @param subject
     * @param provider
     * @param protocol
     * @param act
     * @param guidelineId
     * @param otherParticipations
     * @param data
     * @param state              null if unspecified
     * @throws IllegalArgumentException if date null
     */
    @FullConstructor
            public Observation(@Attribute(name = "uid") UIDBasedID uid,
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
                               @Attribute(name = "data", required = true) History<? extends ItemStructure> data,
                               @Attribute(name = "state") History<? extends ItemStructure> state,
                               @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService
                               ) {

        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent,
                language, encoding, subject, provider, workflowId, otherParticipations, 
                protocol, guidelineId, terminologyService);

        if (data == null) {
            throw new IllegalArgumentException("null data");
        }
        this.data = data;
        this.state = state;
    }

    /**
     * Construct an Observation
     *
     * @param archetypeNodeId
     * @param name
     * @param subject
     * @param provider
     * @param data
     * @throws IllegalArgumentException if date null
     */
    public Observation(String archetypeNodeId, DvText name, Archetyped archetypeDetails,
                CodePhrase language, CodePhrase encoding,
                 PartyProxy subject, PartyProxy provider,
                 History<ItemStructure> data, TerminologyService terminologyService) {
        this(null, archetypeNodeId, name, archetypeDetails, null, null, null, language, encoding, 
                subject, provider, null, null, null, null, data, null, terminologyService);
    }

    /**
     * The data of this observation, in the form of a history of
     * values which may be of any complexity.
     *
     * @return data
     */
    public History<? extends ItemStructure> getData() {
        return data;
    }

    /**
     * The state of subject of this observation during the observation
     * process, in the form of a history of values which may be of any
     * complexity. Optional.
     *
     * @return state null if unspecified
     */
    public History<? extends ItemStructure> getState() {
        return state;
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
    Observation() {
    }

    void setData(History<? extends ItemStructure> data) {
        this.data = data;
    }

    void setState(History<? extends ItemStructure> state) {
        this.state = state;
    }
    // POJO end

    /* fields */
    private History<? extends ItemStructure> data;
    private History<? extends ItemStructure> state;
    
    /* static fields */
    public static final String DATA = "data";
    public static final String STATE = "state";	
}

