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

package com.ethercis.ehr.encode.rawjson;

import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 6/21/2017.
 */
public class LightRawJsonEncoder {

    String jsonbOrigin;

    public LightRawJsonEncoder(String jsonbOrigin) {
        this.jsonbOrigin = jsonbOrigin;
    }

    public String encodeContentAsMap(String root){
//        Type listType = new TypeToken<ArrayList<ArrayList<Object>>>(){}.getType();
        GsonBuilder gsondb = EncodeUtil.getGsonBuilderInstance();
        if (jsonbOrigin.startsWith("[")){ //strip the expression as an array
            jsonbOrigin = jsonbOrigin.trim().substring(1, jsonbOrigin.length()-1);
        }
        Map<String, Object> fromDB = gsondb.create().fromJson(jsonbOrigin, Map.class);

        GsonBuilder gsonRaw = EncodeUtil.getGsonBuilderInstance(I_DvTypeAdapter.AdapterType.DBJSON2RAWJSON);
        String raw = gsonRaw.setPrettyPrinting().create().toJson(fromDB);

        return raw;
    }
}
