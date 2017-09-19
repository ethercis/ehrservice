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
import com.ethercis.ehr.encode.wrappers.ObjectSnakeCase;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.*;
import org.openehr.rm.composition.content.entry.CareEntry;
import org.openehr.rm.composition.content.entry.Entry;

import java.util.Map;

/**
 * Created by christian on 10/6/2016.
 */
public class SerializerUtil {

    public static void setEntryAttributes(CareEntry entry, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext){
        jsonObject.addProperty(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, new ObjectSnakeCase(entry).camelToUpperSnake());
        jsonObject.add("name", jsonSerializationContext.serialize(entry.getName()));
        jsonObject.addProperty("archetype_node_id", entry.getArchetypeNodeId());
        jsonObject.add("archetype_details", jsonSerializationContext.serialize(entry.getArchetypeDetails()));

        jsonObject.add("other_participations", jsonSerializationContext.serialize(entry.getOtherParticipations()));

        if (entry.getSubject() != null && entry.getSubject().getExternalRef() != null)
            jsonObject.add("subject", jsonSerializationContext.serialize(entry.getSubject()));

        jsonObject.add("provider", jsonSerializationContext.serialize(entry.getProvider()));

        if (entry.getProtocol() != null) {
            JsonObject protocolSerialized = (JsonObject) jsonSerializationContext.serialize(entry.getProtocol());
            if (!SerializerUtil.isEmptyItemList(protocolSerialized))
                jsonObject.add("protocol", protocolSerialized);
        }


        jsonObject.add("guideline_id", jsonSerializationContext.serialize(entry.getGuidelineId()));
    }

    public static boolean isEmptyItemList(JsonObject jsonObject){
        for (Map.Entry<String, JsonElement> member: jsonObject.entrySet()){
            if (member.getKey().equals("items")){
                JsonArray jsonArray = (JsonArray)member.getValue();
                if (jsonArray.size() == 0)
                    return true;
            }
        }
        return false;
    }
}
