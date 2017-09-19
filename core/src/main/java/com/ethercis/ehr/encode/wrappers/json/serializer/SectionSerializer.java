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
package com.ethercis.ehr.encode.wrappers.json.serializer;

import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.wrappers.ObjectSnakeCase;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.*;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;

import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class SectionSerializer extends DvTypeSerializer<Section> {

	private Gson gson = null;

	public SectionSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public SectionSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Section section, Type type, JsonSerializationContext jsonSerializationContext) {
		if (section.getItems().isEmpty())
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, new ObjectSnakeCase(section).camelToUpperSnake());
		jsonObject.add("name", jsonSerializationContext.serialize(section.getName()));
		jsonObject.addProperty("archetype_node_id", section.getArchetypeNodeId());
		jsonObject.add("archetype_details", jsonSerializationContext.serialize(section.getArchetypeDetails()));
		JsonArray array = new JsonArray();
		for (Object item: section.getItems()){
			if (item != null){
				JsonElement element = jsonSerializationContext.serialize(item);
				if (!element.equals(JsonNull.INSTANCE)) {
					array.add(element);
				}
			}
		}
		if (array.size() == 0)
			return JsonNull.INSTANCE;

		jsonObject.add("items", array);
		return jsonObject;
	}
}
