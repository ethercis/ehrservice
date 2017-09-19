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

import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.wrappers.ObjectSnakeCase;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.TermMapping;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * GSON adapter for DvDateTime
 * Required since JSON does not support natively a DateTime data type
 */
public class DvCodedTextAdapter extends DvTypeAdapter<DvCodedText> {

    private Gson gson;

    public DvCodedTextAdapter(AdapterType adapterType) {
        super(adapterType);
        gson = new GsonBuilder()
                .registerTypeAdapter(CodePhrase.class, new CodePhraseAdapter(adapterType))
                .setPrettyPrinting()
                .create();
    }

    public DvCodedTextAdapter() {
    }

    @Override
    public DvCodedText read(JsonReader arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(JsonWriter writer, DvCodedText dvalue) throws IOException {
        if (dvalue == null) {
            writer.nullValue();
            return;
        }

//		"value": "complete blood count",
//				"definingCode": {
//			"codeString": "26604007",
//					"terminologyId": {
//				"name": "SNOMED-CT",
//						"value": "SNOMED-CT"
//			}
//		}
        TermMappingAdapter termMappingAdapter = new TermMappingAdapter();

        if (adapterType == AdapterType.PG_JSONB) {
            writer.beginObject();
            writer.name("value").value(dvalue.getValue());
            writer.name("definingCode");
            writer.beginObject();
            writer.name("codeString").value(dvalue.getDefiningCode().getCodeString());
            writer.name("terminologyId");
            writer.beginObject();
            writer.name("name").value(dvalue.getDefiningCode().getTerminologyId().name());
            writer.name("value").value(dvalue.getDefiningCode().getTerminologyId().getValue());
            writer.endObject();
            writer.endObject();
            termMappingAdapter.write(writer, dvalue.getMappings());
            writer.endObject();
        } else if (adapterType == AdapterType.RAW_JSON) {
//			writer.beginObject(); //{
//			writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(EncodeUtil.camelToUpperSnake(dvalue));
//			writer.name("value").value(dvalue.getValue());
//			writer.name("defining_code");
//			writer.beginObject(); //{
//			CodePhrase codePhrase = dvalue.getDefiningCode();
//			writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(EncodeUtil.camelToUpperSnake(codePhrase));
//			writer.name("code_string").value(codePhrase.getCodeString());
//			writer.name("terminology_id");
//			writer.beginObject(); //{
//			TerminologyID terminologyID = codePhrase.getTerminologyId();
//			writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(EncodeUtil.camelToUpperSnake(terminologyID));
//			writer.name("value").value(terminologyID.getValue());
//			writer.endObject(); //}
//			writer.endObject(); //}
//			writer.endObject(); //}
            //===
            writer.beginObject(); //{
            writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(new ObjectSnakeCase(dvalue).camelToUpperSnake());
            writer.name("value").value(dvalue.getValue());
            CodePhrase codePhrase = dvalue.getDefiningCode();
            writer.name("defining_code").value(gson.toJson(codePhrase));
            writer.endObject(); //}
        }

    }

}
