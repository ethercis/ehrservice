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
import org.openehr.rm.composition.EventContext;

import java.lang.reflect.Type;
import java.util.List;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class EventContextSerializer extends DvTypeSerializer<EventContext> {

	private Gson gson = null;

	public EventContextSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public EventContextSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(EventContext eventContext, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, new ObjectSnakeCase(eventContext).camelToUpperSnake());
		jsonObject.add("start_time", jsonSerializationContext.serialize(eventContext.getStartTime()));
		jsonObject.add("end_time", jsonSerializationContext.serialize(eventContext.getEndTime()));
		jsonObject.addProperty("location", eventContext.getLocation());
		jsonObject.add("setting", jsonSerializationContext.serialize(eventContext.getSetting()));
		jsonObject.add("other_context", jsonSerializationContext.serialize(eventContext.getOtherContext()));
		jsonObject.add("health_care_facility", jsonSerializationContext.serialize(eventContext.getHealthCareFacility()));

		if (eventContext.getParticipations() != null && !eventContext.getParticipations().isEmpty()) {
			JsonArray array = (JsonArray) jsonSerializationContext.serialize(eventContext.getParticipations(), List.class);
			jsonObject.add("participations", array);
		}
		return jsonObject;

	}
}
