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

import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.ethercis.ehr.encode.wrappers.json.writer.DvTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.openehr.rm.support.identification.TerminologyID;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class TerminologyIDSerializer extends DvTypeSerializer<TerminologyID> {

	private Gson gson = null;

	public TerminologyIDSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public TerminologyIDSerializer() {
	}

	@Override
	public JsonElement serialize(TerminologyID terminologyID, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, "TERMINOLOGY_ID");
		jsonObject.addProperty("value", terminologyID.getValue());
		return jsonObject;
	}
}
