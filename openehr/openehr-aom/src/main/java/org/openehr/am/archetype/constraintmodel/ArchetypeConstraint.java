/*
 * component:   "openEHR Reference Implementation"
 * description: "Class ArchetypeConstraint"
 * keywords:    "archetype"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL: http://svn.openehr.org/ref_impl_java/TRUNK/libraries/src/java/org/openehr/am/archetype/constraintmodel/ArchetypeConstraint.java $"
 * revision:    "$LastChangedRevision: 2 $"
 * last_change: "$LastChangedDate: 2005-10-12 23:20:08 +0200 (Wed, 12 Oct 2005) $"
 */
package org.openehr.am.archetype.constraintmodel;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Purpose Archetype equivalent to LOCATABLE class in openEHR Common reference
 * model. Defines common constraints for any inheritor of LOCATABLE in any
 * reference model.
 *
 * @author Rong Chen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ARCHETYPE_CONSTRAINT"
)
@XmlSeeAlso({CObject.class, CAttribute.class})
public abstract class ArchetypeConstraint  implements Serializable{
    private static final long serialVersionUID = 5111385145081903850L;

    /**
     * Path separator in archetype path
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * Constructor
     *
     * @param anyAllowed
     */
    protected ArchetypeConstraint(boolean anyAllowed, String path) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path null");
        }
        this.anyAllowed = anyAllowed;
        this.path = path;
    }

    public ArchetypeConstraint()
    {
    }

    /**
     * Path of this node relative to root of archetype.
     *
     * @return path
     */
    public String path() {
        return path;
    }
    
    public void setPath(String path) {
    	this.path = path;
    }

    /**
     * True if this node is a valid archetype node.
     *
     * @return true if valid
     */
    public abstract boolean isValid();
    
    /**
     * Returns true if the constraint is a root node
     * 
     * @return
     */
    public boolean isRoot() {
    	return PATH_SEPARATOR.equals(path);
    }

    /**
     * True if the relative path exists at this node.
     *
     * @param path
     * @return true if has
     * @throws IllegalArgumentException if path null
     */
    public abstract boolean hasPath(String path);

    /**
     * True if constraints represented by other are narrower than this node.
     *
     * @param constraint
     * @return true if subset
     * @throws IllegalArgumentException if constraint null
     */
    public abstract boolean isSubsetOf(ArchetypeConstraint constraint);

    /**
     * True if any possible instance value of this type is considered valid
     *
     * @return anyAllowed
     */
    public boolean isAnyAllowed() {
        return anyAllowed;
    }
    
    public void setAnyAllowed(boolean anyAllowed) {
    	this.anyAllowed = anyAllowed;
    }

    /**
     * True if hide_on_form is set on the template
     * 
     * @return
     */
    public boolean isHiddenOnForm() {
    	return hiddenOnForm;
    }
    
    public String getAnnotation() {
		return annotation;
	}
    
    public void setAnnotation(String annotation) {
    	this.annotation = annotation;
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
     * Equals if two ArchetypeConstraint have same value
     *
     * @param o
     * @return true if equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!( o instanceof ArchetypeConstraint )) {
            return false;
        }

        final ArchetypeConstraint ac = (ArchetypeConstraint) o;

        return new EqualsBuilder()
                .append(anyAllowed, ac.anyAllowed)
                .append(path, ac.path)
       //         .append(hiddenOnForm, ac.hiddenOnForm)
                .isEquals();
    }

    /**
     * Return a hash code of this object
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(anyAllowed)
                .append(path)
         //       .append(hiddenOnForm)
                .toHashCode();
    }    

    /* fields */
    private boolean anyAllowed;
    private String path;
    
    // TODO experimental feature in ADL 1.5
    private boolean hiddenOnForm;    
    private String annotation;
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
 *  The Original Code is ArchetypeConstraint.java
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