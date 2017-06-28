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

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openehr.rm.RMObject;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.encapsulated.DvEncapsulated;

/**
 * Audit and other meta-data for systems in the feeder chain. Instances of this class are
 * immutable
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public final class FeederAudit extends RMObject {

   /**
     * Constrcuts a FeederAuditDetails
     *
     * @param originatingSystemAudit		not null
     * @param originatingSystemItemIds 	null if not specified
     * @param feederSystemAudit			null if not specified
     * @param feederSystemItemIds		null if not specified
     * @param originalContent			null if not specified
     * 
     * @throws IllegalArgumentException if originatingSystemAudit null,
     * 	originatingSystemItemIds or feederSystemItemIds empty
     */
    public FeederAudit(FeederAuditDetails originatingSystemAudit,
    		List<DvIdentifier> originatingSystemItemIDs,
    		FeederAuditDetails feederSystemAudit,
    		List<DvIdentifier> feederSystemItemIDs,
    		DvEncapsulated originalContent) {
        if (originatingSystemAudit == null) {
            throw new IllegalArgumentException("null originatingSystemAudit");
        }
        if (originatingSystemItemIDs != null && originatingSystemItemIDs.size() == 0) {
            throw new IllegalArgumentException("empty originatingSystemItemIds");
        }
        if (feederSystemItemIDs != null && feederSystemItemIDs.size() == 0) {
            throw new IllegalArgumentException("empty feederSystemItemIds");
        }
        this.originatingSystemAudit = originatingSystemAudit;
        this.originatingSystemItemIds = originatingSystemItemIDs;
        this.feederSystemAudit = feederSystemAudit;
        this.feederSystemItemIds = feederSystemItemIDs;
        this.originalContent = originalContent;
    }

    /**
     * Audit information for the information item from the originating system
     * 
     * @return originatingSystemAudit
     */
	public FeederAuditDetails getOriginatingSystemAudit() {
		return originatingSystemAudit;
	}

	/**
	 * Identifiers used for the item in the originating system
	 * 
	 * @return originatingSystemItemIds or null if not specified
	 */
	public List<DvIdentifier> getOriginatingSystemItemIds() {
		return originatingSystemItemIds;
	}
	
	/**
	 * Audit information for the information item from the feeder system,
	 * if different from the originating system
	 * 
	 * @return feederSystemAudit or null if not specified
	 */
    public FeederAuditDetails getFeederSystemAudit() {
		return feederSystemAudit;
	}

    /**
     * Identifiers used for the item in the feeder system, where the feeder
     * system is distinct from the originating system
     * 
     * @return feederSystemItemIds or null if not specified
     */
	public List<DvIdentifier> getFeederSystemItemIds() {
		return feederSystemItemIds;
	}
	
	/**
	 * Optional inline inclusion of or reference to original content 
	 * corresponding to the openEHR content at this node. Typically a URI 
	 * reference to a document or message in a persistent store associated 
	 * with the EHR.
	 * 
	 * @return originalContent or null if not specified
	 */
	public DvEncapsulated getOriginalContent() {
		return originalContent;
	}

    /**
     * Equals if have same values
     *
     * @param o
     * @return true if equals
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!( o instanceof FeederAudit )) return false;

        final FeederAudit fa = (FeederAudit) o;

        return new EqualsBuilder()
                .append(originatingSystemAudit, fa.originatingSystemAudit)
                .append(originatingSystemItemIds, fa.originatingSystemItemIds)
                .append(feederSystemAudit, fa.feederSystemAudit)
                .append(feederSystemItemIds, fa.feederSystemItemIds)
                .append(originalContent, fa.originalContent)
                .isEquals();
    }

    /**
     * Hashcode of this object
     *
     * @return hashcode
     */
    public int hashCode() {
        return new HashCodeBuilder(7,19)
                .append(originatingSystemAudit)
                .append(originatingSystemItemIds)
                .append(feederSystemAudit)
                .append(feederSystemItemIds)
                .append(originalContent)
                .toHashCode();
    }
    
	// POJO start
	FeederAudit() {		
	}
	
    void setOriginatingSystemAudit(FeederAuditDetails originatingSystemAudit) {
        this.originatingSystemAudit = originatingSystemAudit;
    }

    void setOriginatingSystemItemIds(List<DvIdentifier> originatingSystemItemIDs) {
        this.originatingSystemItemIds = originatingSystemItemIDs;
    }

    void setFeederSystemAudit(FeederAuditDetails feederSystemAudit) {
        this.feederSystemAudit = feederSystemAudit;
    }

    void setFeederSystemItemIds(List<DvIdentifier> feederSystemItemIDs) {
        this.feederSystemItemIds = feederSystemItemIDs;
    }

    // POJO end

    /* fields */
    private FeederAuditDetails originatingSystemAudit;
    private List<DvIdentifier> originatingSystemItemIds;
    private FeederAuditDetails feederSystemAudit;
    private List<DvIdentifier> feederSystemItemIds;
    private DvEncapsulated originalContent;
}