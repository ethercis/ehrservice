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
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.Action;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class ActionSerializer extends DvTypeSerializer<Action> {

	private Gson gson = null;

	public ActionSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public ActionSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Action action, Type type, JsonSerializationContext jsonSerializationContext) {
		if (action.getDescription() == null && action.getInstructionDetails() == null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		SerializerUtil.setEntryAttributes(action, jsonObject, jsonSerializationContext);

		jsonObject.add("time", jsonSerializationContext.serialize(action.getTime()));
		jsonObject.add("ism_transition", jsonSerializationContext.serialize(action.getIsmTransition()));
		jsonObject.add("instruction_details", jsonSerializationContext.serialize(action.getInstructionDetails()));
		jsonObject.add("description", jsonSerializationContext.serialize(action.getDescription()));
		return jsonObject;

	}
}
