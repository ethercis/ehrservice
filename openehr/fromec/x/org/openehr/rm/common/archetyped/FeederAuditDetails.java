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
package org.openehr.rm.common.archetyped;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;

/**
 * Audit details for a feeder system. Instances of this class are
 * immutable
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public final class FeederAuditDetails extends RMObject {

    /**
     * Constrcuts a FeederAuditDetails
     *
     * @param systemId           not null
     * @param provider		   null if not present
     * @param timeCommitted      null if not present
     * @param location		   null if not present
     * @param time			   null if not present
     * @param subject			   null if not present
     * @param versionId		   null if not present
     * @throws IllegalArgumentException if systemId is null or empty
     */
    public FeederAuditDetails(String systemID, PartyIdentified provider,
    		PartyIdentified location, DvDateTime time, PartyProxy subject,
    		String versionID) {
        if (StringUtils.isEmpty(systemID)) {
            throw new IllegalArgumentException("null or empty systemId");
        }
        this.systemId = systemID;
        this.provider = provider;
        this.location = location;
        this.time = time;
        this.subject = subject;
        this.versionId = versionID;
    }

    /**
     * Identity of the system which handled the information item
     *
     * @return systemId
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * Identity of optional provider who created/committed/forwarded/handled the item
     *
     * @return provider
     */
    public PartyIdentified getProvider() {
        return provider;
    }

    /**
     * Identity of site/facility within an organisation which handled the item
     *
     * @return location
     */
    public PartyIdentified getLocation() {
        return location;
    }
    
    /**
     * Time of handling of the item
     *
     * @return time
     */
    public DvDateTime getTime() {
        return time;
    }

    /**
     * Identity for subject of the received information item
     *
     * @return subject
     */
    public PartyProxy getSubject() {
        return subject;
    }

    /**
     * Any identifier used in the system if available
     *
     * @return versionId
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Equals if have same values
     *
     * @param o
     * @return true if equals
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!( o instanceof FeederAuditDetails )) return false;

        final FeederAuditDetails fad = (FeederAuditDetails) o;

        return new EqualsBuilder()
                .append(systemId, fad.systemId)
                .append(provider, fad.provider)
                .append(location, fad.location)
                .append(time, fad.time)
                .append(subject, fad.subject)
                .append(versionId, fad.versionId)
                .isEquals();
    }

    /**
     * Return a hashcode of this object
     *
     * @return hashcode
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(systemId)
                .append(provider)
                .append(location)
                .append(time)
                .append(subject)
                .append(versionId)
                .toHashCode();
    }

    //POJO start
    FeederAuditDetails() {}
    
	void setProvider(PartyIdentified provider) {
		this.provider = provider;
	}

	void setSubject(PartyProxy subject) {
		this.subject = subject;
	}

	void setSystemId(String systemID) {
		this.systemId = systemID;
	}

	void setTime(DvDateTime time) {
		this.time = time;
	}

	void setVersionId(String versionID) {
		this.versionId = versionID;
	}
	
	void setLocation(PartyIdentified location) {
		this.location = location;
	}
    //POJO end
	
    /* fields */
    private String systemId;
    private PartyIdentified provider;
    private PartyIdentified location;
    private DvDateTime time;
    private PartyProxy subject;
    private String versionId;
    
}

