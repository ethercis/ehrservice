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
package com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw;
import com.b.a.c.T;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class LinkedTreeMapAdapter extends TypeAdapter<LinkedTreeMap> implements I_DvTypeAdapter {

	public static final String NAME = "name";
	public static final String AT_CLASS = "@class";
	protected AdapterType adapterType = AdapterType.DBJSON2RAWJSON;

	final static String matchNodePredicate =
			"/(content|protocol|events|data|description|instruction|items|activities|activity|composition|entry|evaluation|observation|action|at)\\[([(0-9)|(A-Z)|(a-z)|\\-|_|\\.]*)\\]";

	public LinkedTreeMapAdapter(AdapterType adapterType) {
		super();
		this.adapterType = adapterType;
	}

	public LinkedTreeMapAdapter() {
		super();
		this.adapterType = AdapterType.DBJSON2RAWJSON;
	}

//	@Override
	public LinkedTreeMap read(JsonReader arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public void write(JsonWriter writer, LinkedTreeMap map) throws IOException {
		writer.beginObject();
		for (Object entry: map.entrySet()){
			if (entry instanceof Map.Entry){
				String key = (String) ((Map.Entry) entry).getKey();
				Object value = ((Map.Entry) entry).getValue();
				if (value instanceof String) {
					if (key.equals(CompositionSerializer.TAG_CLASS)) {
						writer.name(AT_CLASS).value(EncodeUtil.camelToUpperSnake((String)value));
					} else if (key.equals(CompositionSerializer.TAG_PATH)) {
						//ignore
					} else if (key.equals(CompositionSerializer.TAG_NAME)) {
						writer.name(NAME).value((String)value);
					}
					else {
						writer.name(key).value((String)value);
					}
				}
				else if (value instanceof ArrayList){
					if (key.equals(CompositionSerializer.TAG_NAME)){
						//grab the name part
						LinkedTreeMap nameValue = (LinkedTreeMap) ((ArrayList)value).get(0);
						writer.name(NAME);
						new LinkedTreeMapAdapter().write(writer, nameValue);
					}
					else {
						String jsonKey = new RawJsonKey(key).toRawJson();

						if (key.matches(matchNodePredicate)){
							//grab the archetype_node_id and add it to all array value instance
							String archetypeNodeId = new NodeId(key).predicate();
							key = jsonKey;
							for (Object item: (ArrayList)value){
								if (item instanceof LinkedTreeMap){
									((LinkedTreeMap)item).put("archetype_node_id", archetypeNodeId);
								}
							}
						}
						writer.name(key);
						new ArrayListAdapter().write(writer, (ArrayList) value);
					}
				}
				else if (value instanceof LinkedTreeMap){
					LinkedTreeMap valueMap = (LinkedTreeMap)value;
					writer.name(new RawJsonKey(key).toRawJson());
					new LinkedTreeMapAdapter().write(writer, valueMap);
				}
				else if (value instanceof Double){
					writer.name(key).value((Double)value);
				}
				else if (value instanceof Long){
					writer.name(key).value((Long)value);
				}
				else if (value instanceof Number){
					writer.name(key).value((Number)value);
				}
				else if (value instanceof Boolean){
					writer.name(key).value((Boolean)value);
				}
				else
					throw new IllegalArgumentException("Could not handle value type for key:"+key+", value:"+value);


			}
		}
		writer.endObject();
		return;
	}

}
