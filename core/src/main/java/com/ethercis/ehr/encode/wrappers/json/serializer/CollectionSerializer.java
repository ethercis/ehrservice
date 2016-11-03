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
import com.google.gson.*;
import org.openehr.rm.support.identification.TerminologyID;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class CollectionSerializer extends DvTypeSerializer<Collection<?>> {

	private Gson gson = null;

	public CollectionSerializer(AdapterType adapterType) {
		super(adapterType);
	}

	public CollectionSerializer() {
	}

	@Override
	public JsonElement serialize(Collection<?> collection, Type type, JsonSerializationContext jsonSerializationContext) {
		if (collection == null || collection.isEmpty())
			return JsonNull.INSTANCE;
		JsonArray array = new JsonArray();
		for (Object item: collection){
			if (item != null){
				JsonElement element = jsonSerializationContext.serialize(item);
				if (!element.equals(JsonNull.INSTANCE)) {
					array.add(element);
				}
			}
		}
		return array;
	}
}
