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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by christian on 9/9/2016.
 */
public class DvDurationAdapter extends DvTypeAdapter<DvDuration> {

    public DvDurationAdapter(AdapterType adapterType) {
        super(adapterType);
    }

    public DvDurationAdapter() {
    }

    @Override
    public void write(JsonWriter writer, DvDuration dvDuration) throws IOException {
        if (dvDuration == null) {
            writer.nullValue();
            return;
        }

        if (adapterType==AdapterType.PG_JSONB) {
            writer.beginObject();
            writer.name("value").value(dvDuration.getValue());
            writer.endObject();
        }
        else if (adapterType==AdapterType.RAW_JSON){
            writer.beginObject();
            writer.name(I_DvTypeAdapter.TAG_CLASS_RAW_JSON).value(new ObjectSnakeCase(dvDuration).camelToUpperSnake());
            writer.name("value").value(dvDuration.getValue());
            writer.endObject();
        }
    }

    @Override
    public DvDuration read(JsonReader jsonReader) throws IOException {
        return null;
    }

}
