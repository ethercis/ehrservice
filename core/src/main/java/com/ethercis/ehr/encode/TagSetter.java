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

package com.ethercis.ehr.encode;

import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to set the tag corresponding to a serialization
 * Created by christian on 10/4/2016.
 */
public class TagSetter {

    public enum DefinitionSet {PG_JSONB, RAW_JSON}

    static final Map<String, String> PG_JSONB_MAP = new HashMap<String, String>() {
        {
            put("TAG_META","/meta");
            put("TAG_CONTENT","/content");
            put("TAG_PROTOCOL","/protocol");
            put("TAG_DATA","/data");
            put("TAG_STATE","/state");
            put("TAG_DESCRIPTION","/description");
            put("TAG_TIME","/time");
            put("TAG_WIDTH","/width");
            put("TAG_MATH_FUNCTION","/math_function");
            put("TAG_INSTRUCTION","/instruction");
            put("TAG_NARRATIVE","/narrative");
            put("TAG_ITEMS","/items");
            put("TAG_OTHER_CONTEXT","/context/other_context");
            put("TAG_ACTIVITIES","/activities");
            put("TAG_ACTIVITY","/activity");
            put("TAG_VALUE","/value");
            put("TAG_EVENTS","/events");
            put("TAG_ORIGIN","/origin");
            put("TAG_SUMMARY","/summary");
            put("TAG_TIMING","/timing");
            put("TAG_COMPOSITION","/composition");
            put("TAG_ENTRY","/entry");
            put("TAG_EVALUATION","/evaluation");
            put("TAG_OBSERVATION","/observation");
            put("TAG_ACTION","/action");
            put("TAG_ISM_TRANSITION","/ism_transition");
            put("TAG_CURRENT_STATE","/current_state");
            put("TAG_CAREFLOW_STEP","/careflow_step");
            put("TAG_TRANSITION","/transition");
            put("TAG_WORKFLOW_ID","/workflow_id");
            put("TAG_GUIDELINE_ID","/guideline_id");
            put("TAG_OTHER_PARTICIPATIONS","/other_participations");
            put("TAG_UID","/uid");
            put("TAG_OTHER_DETAILS","/other_details");
            put("TAG_INSTRUCTION_DETAILS","/instruction_details");
            put("TAG_ACTIVITY_ID","/action_id");
            put("TAG_INSTRUCTION_ID","/instruction_id");
            put("TAG_PATH","/$PATH$");
            put("TAG_CLASS","/$CLASS$");
            put("TAG_NAME","/name");
            put("TAG_DEFINING_CODE","/defining_code");
            put("INNER_CLASS_LIST","$INNER_CLASS_LIST$");
            put("TAG_ACTION_ARCHETYPE_ID","/action_archetype_id");
            put("TAG_ARCHETYPE_NODE_ID","/archetype_node_id");
        }
    };

    static final Map<String, String> RAW_JSON_MAP = new HashMap<String, String>() {
        {
            put("TAG_META","meta");
            put("TAG_CONTENT","content");
            put("TAG_PROTOCOL","protocol");
            put("TAG_DATA","data");
            put("TAG_STATE","state");
            put("TAG_DESCRIPTION","description");
            put("TAG_TIME","time");
            put("TAG_WIDTH","width");
            put("TAG_MATH_FUNCTION","math_function");
            put("TAG_INSTRUCTION","instruction");
            put("TAG_NARRATIVE","narrative");
            put("TAG_ITEMS","items");
            put("TAG_OTHER_CONTEXT","context/other_context");
            put("TAG_ACTIVITIES","activities");
            put("TAG_ACTIVITY","activity");
            put("TAG_VALUE","value");
            put("TAG_EVENTS","events");
            put("TAG_ORIGIN","origin");
            put("TAG_SUMMARY","summary");
            put("TAG_TIMING","timing");
            put("TAG_COMPOSITION","composition");
            put("TAG_ENTRY","entry");
            put("TAG_EVALUATION","evaluation");
            put("TAG_OBSERVATION","observation");
            put("TAG_ACTION","action");
            put("TAG_ISM_TRANSITION","ism_transition");
            put("TAG_CURRENT_STATE","current_state");
            put("TAG_CAREFLOW_STEP","careflow_step");
            put("TAG_TRANSITION","transition");
            put("TAG_WORKFLOW_ID","workflow_id");
            put("TAG_GUIDELINE_ID","guideline_id");
            put("TAG_OTHER_PARTICIPATIONS","other_participations");
            put("TAG_UID","uid");
            put("TAG_OTHER_DETAILS","other_details");
            put("TAG_INSTRUCTION_DETAILS","instruction_details");
            put("TAG_ACTIVITY_ID","action_id");
            put("TAG_INSTRUCTION_ID","instruction_id");
            put("TAG_PATH","/$PATH$");
            put("TAG_CLASS", I_DvTypeAdapter.TAG_CLASS_RAW_JSON);
            put("TAG_NAME","name");
            put("TAG_DEFINING_CODE","defining_code");
            put("INNER_CLASS_LIST","$INNER_CLASS_LIST$");
            put("TAG_ACTION_ARCHETYPE_ID","action_archetype_id");
            put("TAG_ARCHETYPE_NODE_ID","archetype_node_id");
        }
    };

    public static void setTagDefinition(CompositionSerializer compositionSerializer, DefinitionSet definitionSet) throws IllegalAccessException {
        for (Field field: compositionSerializer.getClass().getSuperclass().getDeclaredFields()){
            String id = field.getName();
            switch (definitionSet){
                case PG_JSONB:
                    if (PG_JSONB_MAP.containsKey(field.getName())){
                        field.setAccessible(false);
                        field.set(compositionSerializer, PG_JSONB_MAP.get(id));
                        field.setAccessible(true);
                    }
                    break;
                case RAW_JSON:
                    if (RAW_JSON_MAP.containsKey(field.getName())){
                        field.setAccessible(false);
                        field.set(compositionSerializer, RAW_JSON_MAP.get(id));
                        field.setAccessible(true);
                    }
                    break;
            }
        }
    }

}
