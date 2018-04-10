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
package com.ethercis.ehr.encode.wrappers.json.writer;

import com.ethercis.ehr.encode.wrappers.ObjectSnakeCase;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.openehr.rm.datatypes.text.DvText;

import java.io.IOException;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class DvTextAdapter extends DvTypeAdapter<DvText> {

	public DvTextAdapter(AdapterType adapterType) {
		super(adapterType);
	}

	public DvTextAdapter() {
	}

	@Override
	public DvText read(JsonReader arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(JsonWriter writer, DvText dvalue) throws IOException {
		if (dvalue == null) {
			writer.nullValue();
			return;
		}

		if (adapterType==AdapterType.PG_JSONB) {
			TermMappingAdapter termMappingAdapter = new TermMappingAdapter();
			writer.beginObject();
			writer.name("value").value(dvalue.getValue());
			termMappingAdapter.write(writer, dvalue.getMappings());
			writer.endObject();
		}
		else if (adapterType==AdapterType.RAW_JSON){
			writer.beginObject();
			writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(new ObjectSnakeCase(dvalue).camelToUpperSnake());
			writer.name("value").value(dvalue.getValue());
			writer.endObject();

		}

	}
}
