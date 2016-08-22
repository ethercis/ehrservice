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
package com.ethercis.ehr.encode.wrappers.constraints;


import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.Attribute;
import org.openehr.rm.RMObject;
import org.openehr.rm.datatypes.basic.DataValue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

public abstract class DataValueConstraints implements I_VBeanConstraints, Serializable {

    private static final long serialVersionUID = 1552222248255945403L;
    String description; //localized description
	String concept; //coded text (support::id)
	boolean initial = true; //initially created, false if edited
    Map<String, ConstraintAttribute> attributes = new HashMap<String, ConstraintAttribute>();
    transient RMObjectBuilder builder;

    private DataValue parent;
//    private ConstraintOccurrences occurences;

    private class ConstraintAttribute implements Serializable {
        private static final long serialVersionUID = -8328719040403521976L;
        private String name;
        private boolean required;
        private boolean system;
        private Class clazz;
//        ConstraintOccurrences existence;
//        List<ConstraintPrimitive> constraints;

        private ConstraintAttribute(String name, boolean required, boolean system, Class clazz) {
            this.name = name;
            this.required = required;
            this.system = system;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isSystem() {
            return system;
        }

        public Class getAttributeClass(){
            return clazz;
        }

    }


    private void setAttributesMap(Map<String, Attribute> attributesmap, Class clazz){

        for (Attribute attr: attributesmap.values()) {
            Field afield = null;
            try {
                afield = clazz.getDeclaredField(attr.name());
            } catch (NoSuchFieldException e) {
                ; //do nothing...
            }
            if (afield != null) { //it is defined at this class level (we need to identify the field class...)
                ConstraintAttribute def = new ConstraintAttribute(attr.name(),
                        attr.required(),
                        attr.system(),
                        afield == null ? null : afield.getType());
                attributes.put(attr.name(), def);
                //optimization
//                attributesmap.remove(attr.name());
            }
        }
    }
	
	public DataValueConstraints(RMObjectBuilder builder, DataValue parent) {
        this.parent = parent;
        this.builder = builder;
        Class clazz = parent.getClass(); //the actual class of this wrapped value (a Dv type, f.e. DvText)
        //retrieve the attribute map for this type
        Map<String, Attribute> attributesmap = builder.getAttributes(clazz);

        while (clazz != RMObject.class) {
            setAttributesMap(attributesmap, clazz);
            clazz = clazz.getSuperclass();
        }
	}


    public ConstraintAttribute getAttribute(String name){
        return attributes.get(name);
    }
	
	public void setConstraints(Archetype archetype, CAttribute attribute){
        this.initial = true;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}

    public boolean isAttributeRequired(String name){
        if (getAttribute(name) == null)
            return false;
        return getAttribute(name).isRequired();
    }

    public Class getAttributeClass(String name){
        if (getAttribute(name) == null)
            return null;
        return getAttribute(name).getAttributeClass();
    }

//    public boolean validate(DataValue value){
//        return true;
//    }

    public void setValue(DataValue val){
        parent = val;
    }

}
