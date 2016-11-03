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
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public abstract class DvTypeSerializer<T> implements I_DvTypeAdapter, JsonSerializer<T> {

	protected AdapterType adapterType = AdapterType.PG_JSONB;

	public DvTypeSerializer(AdapterType adapterType) {
		super();
		this.adapterType = adapterType;
	}

	public DvTypeSerializer() {
		super();
		this.adapterType = AdapterType.PG_JSONB;
	}
}
