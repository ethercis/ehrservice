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
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.*;
import org.openehr.rm.composition.content.entry.Action;
import org.openehr.rm.composition.content.entry.Instruction;

import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class InstructionSerializer extends DvTypeSerializer<Instruction> {

	private Gson gson = null;

	public InstructionSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public InstructionSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Instruction instruction, Type type, JsonSerializationContext jsonSerializationContext) {
		if (instruction.getActivities() == null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		SerializerUtil.setEntryAttributes(instruction, jsonObject, jsonSerializationContext);

		jsonObject.add("narrative", jsonSerializationContext.serialize(instruction.getNarrative()));
		jsonObject.add("expiry_time", jsonSerializationContext.serialize(instruction.getExpiryTime()));
		jsonObject.add("wf_definition", jsonSerializationContext.serialize(instruction.getWfDefinition()));

		jsonObject.add("activities", jsonSerializationContext.serialize(instruction.getActivities()));

		return jsonObject;

	}
}
