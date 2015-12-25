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
package org.openehr.rm.composition.content.navigation;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.archetyped.Pathable;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.datatypes.text.DvText;

import java.util.*;

/**
 * Represents a heading in a heading structure, or "section tree". * 
 *
 * @author Rong Chen
 * @version 1.0
 */
public final class Section extends ContentItem {

    /**
     * Constructs a Section
     *
     * @param uid
     * @param archetypeNodeId
     * @param name
     * @param archetypeDetails
     * @param feederAudit
     * @param links
     * @param items            null if not present
     * @throws IllegalArgumentException if items not null and empty
     */
    @FullConstructor
            public Section(@Attribute(name = "uid") UIDBasedID uid,
                           @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId,
                           @Attribute(name = "name", required = true) DvText name,
                           @Attribute(name = "archetypeDetails") Archetyped archetypeDetails,
                           @Attribute(name = "feederAudit") FeederAudit feederAudit,
                           @Attribute(name = "links") Set<Link> links,
                           @Attribute(name = "parent") Pathable parent,
                           @Attribute(name = "items") List<ContentItem> items) {
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit,
                links, parent);
        if (items != null && items.isEmpty()) {
            throw new IllegalArgumentException("empty items");
        }
        this.items = ( items == null ?
                null : new ArrayList<ContentItem>(items) );
    }

    /**
     * Constructs a Section
     *
     * @param archetypeNodeId
     * @param name
     * @param items
     */
    public Section(String archetypeNodeId, DvText name,
                   List<ContentItem> items) {
        this(null, archetypeNodeId, name, null, null, null, null, items);
    }

    /**
     * String The path to an item relative to the root of this
     * archetyped structure.
     *
     * @param item
     * @return path of given item
     */
    public String pathOfItem(Locatable item) {
        return null;  // todo: implement this method
    }

    /**
     * Ordered list of content items under this section, which may
     * include more Sections or Entries
     *
     * @return list of ContentItem or null if not present
     */
    public List<ContentItem> getItems() {
        return items;
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
    Section() {
    }

    void setItems(List<ContentItem> items) {
        this.items = items;
    }
    // POJO end

    /* fields */
    private List<ContentItem> items;
	
}

