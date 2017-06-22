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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openehr.rm.RMObject;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;

/**
 * The Link type defines a logical relationship between two items,
 * such as two ENTRYs or an ENTRY and a COMPOSITION. Links can be
 * used across compositions, and across EHRs. Links can potentially
 * be used between interior (ie non archetype root) nodes, although
 * this probably should be prevented in archetypes. Multiple LINKs
 * can be attached to the root object of any archetyped structure to
 * give the effect of a 1->N link.
 *
 * Instances of this class are immutable
 * 
 * @author Rong Chen
 * @version 1.0
 */
public final class Link extends RMObject {

    /**
     * Constructs a Link by meaning, type and target
     *
     * @param meaning   not null
     * @param type  not null
     * @param target    not null
     * @throws IllegalArgumentException if any null argument
     */
    public Link(DvText meaning, DvText type, DvEHRURI target) {
        if(meaning == null) {
            throw new IllegalArgumentException("null meaning");
        }
        if(type == null) {
            throw new IllegalArgumentException("null type");
        }
        if(target == null) {
            throw new IllegalArgumentException("null target");
        }
        this.meaning = meaning;
        this.type = type;
        this.target = target;
    }

    /**
     * Used to describe the relationship, usually in clinical terms,
     * such as  in response to  (the relationship between test
     * results and an order),  follow-up to  and so on. Such
     * relationships can represent any clinically meaningful
     * connection between pieces of information.
     *
     * @return meaning
     */
    public DvText getMeaning() {
        return meaning;
    }

    /**
     * The type attribute is used to indicate a clinical or
     * domain-level meaning for the kind of link, for example
     * "problem"  or "issue". If type values are designed
     * appropriately, they can be used by the requestor of
     * EHR extracts to categorise links which must be followed and
     * which can be broken when the extract is created.
     *
     * @return type
     */
    public DvText getType() {
        return type;
    }

    /**
     * the logical "to" object in the link relation, as per the
     * linguistic sense of the meaning attribute.
     *
     * @return target
     */
    public DvEHRURI getTarget() {
        return target;
    }

    /**
     * Two links equal if both have same values for meaning,
     * type and target
     *
     * @param o
     * @return true if equals
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!( o instanceof Link )) return false;

        final Link link = (Link) o;

        return new EqualsBuilder()
        .append(meaning, link.meaning)
        .append(type, link.type)
        .append(target, link.target)
        .isEquals();
    }

    /**
     * Return a hash code of this link
     *
     * @return hash code
     */
    public int hashCode() {
        return new HashCodeBuilder(7, 29)
        .append(meaning)
        .append(type)
        .append(target)
        .toHashCode();        
    }

    // POJO start
    Link() {
    }

    void setMeaning(DvText meaning) {
        this.meaning = meaning;
    }

    void setType(DvText type) {
        this.type = type;
    }

    void setTarget(DvEHRURI target) {
        this.target = target;
    }
    // POJO end

    /* fields */
    private DvText meaning;
    private DvText type;
    private DvEHRURI target;
}

