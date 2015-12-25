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

import java.util.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openehr.rm.RMObject;

/**
 * Abstract parent of all classes whose instances are reachable by paths, and 
 * which know how to locate child object by paths. 
 * 
 * @author Rong Chen
 */
public abstract class Pathable extends RMObject {
	
	/**
	 * Creates a Pathable
	 * 
	 * @param parent null if not present
	 */
	public Pathable(Pathable parent) {
		this.parent = parent;
	}
	
	/**
	 * Sets the parent
	 * 
	 * @param parent
	 */
	protected void setParent(Pathable parent) {
		this.parent = parent;
	}
	
	/**
	 * Creates a pathable without parent
	 */
	public Pathable() {
		this(null);
	}
	
	/**
	 * Parent of this node in compositional hierarchy
	 * 
	 * @return parent or null if not specified
	 */
	public Pathable getParent() {
		return this.parent;
	}
	
	/**
     * The item at a path (relative to this item); only valid for unique paths,
     * i.e. paths that resolve to a single item.
     *
     * @param path not null and unique
     * @return the item
     * @throws IllegalArgumentException if path invalid
     */
    public abstract Object itemAtPath(String path);
    
    /**
     * List of items corresponding to a nonunique path.
     * 
     * @param path not null and not unique
     * @return the items
     */
    public abstract List<Object> itemsAtPath(String path);
    
    /**
     * The path to an item relative to the root of this archetyped structure.
     * 
     * @param item not null
     */
    public abstract String pathOfItem(Pathable item);

    /**
     * True if the path exists in the data with respect to the current item
     * 
     * @param path not null or empty
     * @return true if exists
     */
    public abstract boolean pathExists(String path);
    
    /**
     * True if the path corresponds to a single item in the data.
     * @param path not null and exists
     * @return true if unique
     */
    public abstract boolean pathUnique(String path);
    
    /**
     * Equals if two actors has same values
     *
     * @param o
     * @return equals if true
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!( o instanceof Pathable )) return false;
        
        final Pathable path = (Pathable) o;
        return new EqualsBuilder()
                .append(parent, path.parent)
                .isEquals();

    }

    /**
     * Return a hash code of this actor
     *
     * @return hash code
     */
    public int hashCode() {
        return new HashCodeBuilder(11, 29)
                .appendSuper(super.hashCode())
                .append(parent)
                .toHashCode();
    }
    
	private Pathable parent;
}