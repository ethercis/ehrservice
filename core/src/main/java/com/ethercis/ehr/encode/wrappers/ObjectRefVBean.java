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
package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.DataValueAdapter;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.identification.TerminologyID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObjectRefVBean extends DataValueAdapter implements I_VBeanWrapper {

	public static final String NAME_SPACE = "nameSpace";
	public static final String TYPE = "type";
	public static final String OBJECT_ID = "objectId";

	public ObjectRefVBean(ObjectRef c) {
		this.adaptee = c;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(NAME_SPACE, ((ObjectRef)adaptee).getNamespace());
		map.put(TYPE, ((ObjectRef)adaptee).getType());
		map.put(OBJECT_ID, ((ObjectRef)adaptee).getId());
		return map;
	}

    @Override
    public ObjectRef parse(String value, String... defaults) {
        String[] definitions = value.split("::");
		if (definitions.length != 3)
			throw new IllegalArgumentException("Invalid encoded value for ObjectRef, must be:<namespace>::<type>::<id>");

		String nameSpace = definitions[0];
		String type = definitions[1];
		String id = definitions[2];

		return new ObjectRef(new HierObjectID(id), nameSpace, type);
    }

	public static ObjectRef getInstance(Map<String, Object> attributes){
		Object value = attributes.get(CompositionSerializer.TAG_VALUE);

		if (value == null)
			throw new IllegalArgumentException("No value in attributes");

		if (value instanceof ObjectRef) return (ObjectRef)value;

		if (value instanceof Map) {
			Map<String, Object> valueMap = (Map) value;

			String codeString = (String)valueMap.get(NAME_SPACE);
			String type = (String)valueMap.get(TYPE);
			String objectId = (String)valueMap.get(OBJECT_ID);

			return new ObjectRef(new HierObjectID(objectId), codeString, type);
		}
		throw new IllegalArgumentException("Could not get instance");
	}

	public static ObjectRef generate(){
		return new ObjectRef(new HierObjectID(UUID.randomUUID().toString()), "ethercis", "ID");
	}

}
