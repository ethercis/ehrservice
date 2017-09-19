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
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.datastructure.itemstructure.ItemTree;

import java.lang.reflect.Type;
import java.util.List;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class CompositionRawSerializer extends DvTypeSerializer<Composition> {

	private Gson gson = null;

	public CompositionRawSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public CompositionRawSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Composition composition, Type type, JsonSerializationContext jsonSerializationContext) {
		if (composition.getContent().isEmpty())
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, new ObjectSnakeCase(composition).camelToUpperSnake());
		jsonObject.add("name", jsonSerializationContext.serialize(composition.getName()));
		jsonObject.addProperty("archetype_node_id", composition.getArchetypeNodeId());
		jsonObject.add("archetype_details", jsonSerializationContext.serialize(composition.getArchetypeDetails()));
		jsonObject.add("language", jsonSerializationContext.serialize(composition.getLanguage()));
		jsonObject.add("territory", jsonSerializationContext.serialize(composition.getTerritory()));
		jsonObject.add("context", jsonSerializationContext.serialize(composition.getContext()));
		jsonObject.add("composer", jsonSerializationContext.serialize(composition.getComposer()));
		JsonArray array = (JsonArray)jsonSerializationContext.serialize(composition.getContent(), List.class);
//		if (((List)array).size() == 0)
//			return JsonNull.INSTANCE;
		if (array.size() == 0)
			return JsonNull.INSTANCE;

		jsonObject.add("content", array);
		return jsonObject;

	}
}
