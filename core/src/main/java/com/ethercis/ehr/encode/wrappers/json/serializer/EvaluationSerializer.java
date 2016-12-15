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
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.LinkedTreeMap;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.composition.content.entry.Observation;
import org.openehr.rm.datastructure.itemstructure.ItemTree;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class EvaluationSerializer extends DvTypeSerializer<Evaluation> {

	private Gson gson = null;

	public EvaluationSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public EvaluationSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(Evaluation evaluation, Type type, JsonSerializationContext jsonSerializationContext) {
		if (evaluation.getData() == null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		SerializerUtil.setEntryAttributes(evaluation, jsonObject, jsonSerializationContext);

		JsonObject array = (JsonObject)jsonSerializationContext.serialize(evaluation.getData(), ItemTree.class);
//		if (((List)array).size() == 0)
//			return JsonNull.INSTANCE;
		if (SerializerUtil.isEmptyItemList(array))
			return JsonNull.INSTANCE;

		jsonObject.add("data", array);
		return jsonObject;

	}
}
