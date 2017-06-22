/*
 * component:   "openEHR Reference Java Implementation"
 * description: "Class CDefinedObject"
 * keywords:    "archetype"
 *
 * author:      "Rong Chen <rong.acode@gmail.com>"
 * support:     "Rong Chen <rong.acode@gmail.com>"
 * copyright:   "Copyright (c) 2007 Cambio Healthcare System, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL$"
 * revision:    "$LastChangedRevision$"
 * last_change: "$LastChangedDate$"
 */
package org.openehr.am.archetype.constraintmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openehr.rm.support.basic.Interval;

/**
 * Abstract parent type of C_OBJECT subtypes that are defined by value, i.e. 
 * whose definitions are actually in the archetype rather than being by 
 * reference.
 * 
 * @author Rong Chen
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
		name = "C_DEFINED_OBJECT"
)
@XmlSeeAlso({CPrimitiveObject.class, CDomainType.class, CComplexObject.class})
public abstract class CDefinedObject extends CObject {

	public CDefinedObject()
	{
	}

	/**
	 * Create a CDefinedObject
	 * 
	 * @param anyAllowed
	 * @param path
	 * @param rmTypeName
	 * @param occurrences
	 * @param nodeID
	 * @param parent
	 * @param assumedValue
	 */
	protected CDefinedObject(boolean anyAllowed, String path, String rmTypeName,
			Interval<Integer> occurrences, String nodeID, CAttribute parent,
			Object assumedValue) {
		
		this(anyAllowed, path, rmTypeName, occurrences, nodeID, parent, 
				assumedValue, null);
	}
	
	/**
	 * Create a CDefinedObject
	 * 
	 * @param anyAllowed
	 * @param path
	 * @param rmTypeName
	 * @param occurrences
	 * @param nodeID
	 * @param parent
	 * @param assumedValue
	 * @param defaultValue
	 */	
	protected CDefinedObject(boolean anyAllowed, String path, String rmTypeName,
			Interval<Integer> occurrences, String nodeID, CAttribute parent,
			Object assumedValue, Object defaultValue) {
		super(anyAllowed, path, rmTypeName, occurrences, nodeID, parent);
		this.assumedValue = assumedValue;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * True if there is an assumed value
	 * 
	 * @return if there is an assumed value
	 */
	public boolean hasAssumedValue() {
		return assumedValue != null;
	}
	
	/**
	 * True if there is a default value
	 * 
	 * @return default value or null
	 */
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}	
	
	/**
     * Equals if two CObject has same values
     *
     * @param o
     * @return true if equals
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!( o instanceof CDefinedObject )) return false;

        final CDefinedObject cobj = (CDefinedObject) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(assumedValue, cobj.assumedValue)
                .append(defaultValue, cobj.defaultValue)
                .isEquals();
    }

    /**
     * Return a hash code of this object
     *
     * @return hash code
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .appendSuper(super.hashCode())
                .append(assumedValue)
                .append(defaultValue)
                .toHashCode();
    }

	/* fields */
	private Object assumedValue;
	private Object defaultValue;

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
 *  The Original Code is CDefinedObject.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2007
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