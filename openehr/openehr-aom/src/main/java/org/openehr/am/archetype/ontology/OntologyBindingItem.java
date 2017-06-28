/*
 * component:   "openEHR Reference Implementation"
 * description: "Class OntologyBindingItem"
 * keywords:    "archetype"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$Source: /usr/local/cvsroot/acode/openehr-kernel/src/java/org/openehr/am/archetype/ontology/OntologyBindingItem.java,v $"
 * revision:    "$Revision: 10 $"
 * last_change: "$Date: 2005-11-22 22:26:12 +0100 (Tue, 22 Nov 2005) $"
 */
package org.openehr.am.archetype.ontology;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Super class of QueryBindingItem and TermBindingItem
 *
 * @author Rong Chen
 * @version 1.0
 */
public class OntologyBindingItem  implements Serializable {

    public OntologyBindingItem(String code) {
        this.code = code;
    }

    /**
     * String representation of this object
     *
     * @return string form
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
    
    /**
     * Equals if two has have same values
     *
     * @param o
     * @return true if equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!( o instanceof OntologyBindingItem )) {
            return false;
        }

        final OntologyBindingItem obi = (OntologyBindingItem) o;

        return new EqualsBuilder()
                .append(code, obi.code)
                .isEquals();
    }

    /**
     * Return a hash code of this object
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
        .append(code)
        .toHashCode();
    }
	
    /* fields */
    String code;
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
 *  The Original Code is OntologyBindingItem.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2003-2004
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */