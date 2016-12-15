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
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.*;
import org.openehr.jaxb.rm.DvText;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.identification.TerminologyID;

import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class ElementWrapperSerializer extends DvTypeSerializer<ElementWrapper> {

	private Gson gson;

	public ElementWrapperSerializer(AdapterType adapterType) {
		super(adapterType);
//		gson = new GsonBuilder()
//				.registerTypeAdapter(DvDateTime.class, new DvDateTimeSerializer(adapterType))
//				.registerTypeAdapter(DvDate.class, new DvDateSerializer(adapterType))
//				.registerTypeAdapter(DvTime.class, new DvTimeSerializer(adapterType))
//				.registerTypeAdapter(DvDuration.class, new DvDurationSerializer(adapterType))
//				.registerTypeAdapter(org.openehr.rm.datatypes.text.DvText.class, new DvTextSerializer(adapterType))
//				.registerTypeAdapter(DvCodedText.class, new DvCodedTextSerializer(adapterType))
//				.registerTypeAdapter(CodePhrase.class, new CodePhraseSerializer(adapterType))
//				.setPrettyPrinting()
//				.create();
	}

	public ElementWrapperSerializer() {
		super();
	}

	@Override
	public JsonElement serialize(ElementWrapper elementWrapper, Type type, JsonSerializationContext jsonSerializationContext) {
		//check the dirty bit
		if (!elementWrapper.dirtyBitSet())
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		Element element = elementWrapper.getAdaptedElement();
		jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, "ELEMENT");
		jsonObject.add("name", jsonSerializationContext.serialize(element.getName()));
		jsonObject.addProperty("archetype_node_id", element.getArchetypeNodeId());
		jsonObject.add("value", jsonSerializationContext.serialize(element.getValue()));
		return jsonObject;
	}
}
