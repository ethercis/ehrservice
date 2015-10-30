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
package com.ethercis.ehr.encode.wrappers;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Christian Chevalley on 4/18/2015.
 */
public class ArrayListAdapter extends TypeAdapter<ArrayList> {

    @Override
    public ArrayList read(JsonReader arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(JsonWriter writer, ArrayList arrayList) throws IOException {
        if (arrayList == null) {
            writer.nullValue();
            return;
        }

        GsonBuilder gsonBuilder = new GsonBuilder();

        writer.beginObject();
        writer.name("array_size").value(arrayList.size());
        writer.name("content").value(gsonBuilder.create().toJson(arrayList));
        writer.endObject();

    }
}
