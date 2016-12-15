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

import com.google.gson.*;
import org.openehr.rm.composition.content.entry.Observation;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.ItemTree;

import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class PointEventSerializer extends DvTypeSerializer<PointEvent> {

	private Gson gson = null;

	public PointEventSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public PointEventSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(PointEvent pointEvent, Type type, JsonSerializationContext jsonSerializationContext) {
		boolean emptyState = false;
		boolean emptyData = false;

		if (pointEvent.getData() == null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("time", jsonSerializationContext.serialize(pointEvent.getTime()));

		if (pointEvent.getState() != null && !((ItemTree)pointEvent.getState()).getItems().isEmpty()) {
			JsonObject stateSerialized = (JsonObject) jsonSerializationContext.serialize(pointEvent.getState());
			if (SerializerUtil.isEmptyItemList(stateSerialized))
				emptyState = true;
			else
				jsonObject.add("state", jsonSerializationContext.serialize(pointEvent.getState()));
		}
		else
			emptyState = true;

		if (pointEvent.getData() != null && !((ItemTree)pointEvent.getData()).getItems().isEmpty()) {
			JsonObject dataSerialized = (JsonObject) jsonSerializationContext.serialize(pointEvent.getData());
			if (SerializerUtil.isEmptyItemList(dataSerialized))
				emptyData = true;
			else
				jsonObject.add("data", dataSerialized);
		}
		else
			emptyData = true;

		if (emptyData && emptyState)
			return JsonNull.INSTANCE;

		return jsonObject;

	}
}
