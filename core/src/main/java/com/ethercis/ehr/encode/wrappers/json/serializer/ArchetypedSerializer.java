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
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.composition.content.entry.Observation;

import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class ArchetypedSerializer extends DvTypeSerializer<Archetyped> {

	private Gson gson = null;

	public ArchetypedSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public ArchetypedSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Archetyped archetyped, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, new ObjectSnakeCase(archetyped).camelToUpperSnake());
		jsonObject.add("archetype_id", jsonSerializationContext.serialize(archetyped.getArchetypeId()));
		jsonObject.add("rm_version", jsonSerializationContext.serialize(archetyped.getRmVersion()));
		return jsonObject;

	}
}
