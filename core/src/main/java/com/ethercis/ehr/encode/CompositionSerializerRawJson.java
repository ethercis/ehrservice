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

import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.text.DvCodedText;

import java.util.Map;

/**
 * Created by christian on 10/5/2016.
 */
public class CompositionSerializerRawJson extends CompositionSerializer {

    private Gson gson;



    public CompositionSerializerRawJson(boolean allElements) throws IllegalAccessException {
        super(allElements);
//        initTags();
        gson = EncodeUtil.getGsonBuilderInstance(I_DvTypeAdapter.AdapterType.RAW_JSON).setPrettyPrinting().create();
    }

    protected Object putObject(Object node, Map<String, Object> map, String key, Object addStructure) throws Exception {
        //CHC: 160602
        if (addStructure == null) return null;
        if (addStructure instanceof Map && ((Map) addStructure).size() == 0)
            return  null;

        Object retStructure = null;

        //some cosmetic
        if (key.equals(TAG_CLASS) && addStructure instanceof String){
            String className = (String)addStructure;
            String rawClassName;
            if (className.equals("DvURI"))
                rawClassName = "DV_URI";
            else if (className.equals("DvEHRURI"))
                rawClassName = "DV_EHR_URI";
            else
                rawClassName = EncodeUtil.camelToUpperSnake(className);

            retStructure = map.put(rawJsonKey(key), rawClassName);
        }
        else if (key.equals(TAG_PATH))
            return null;
        else {
            try {
//                if (node instanceof Item && addStructure instanceof Map){
//                    ((Map)addStructure).put("name", ((Item)node).getName().getValue());
//                }
                retStructure = map.put(rawJsonKey(key), addStructure);
            } catch (IllegalArgumentException e) {
                log.error("Ignoring duplicate key in path detected:" + rawJsonKey(key) + " path:" + itemStack.pathStackDump() + " Exception:" + e);
//			throw new Exception("duplicate key:"+key+", please fix the input structure");
            }
        }

        return retStructure;
    }

    private String rawJsonKey(String key){
        if (key.equals(TAG_CLASS))
            return I_DvTypeAdapter.TAG_CLASS_RAW_JSON;
        else
            return key.substring(1);
    }

    protected String getNodeTag(String prefix, Locatable node, Object container) {

        if (prefix.equals(TAG_ORIGIN) || prefix.equals(TAG_TIME) || prefix.equals(TAG_TIMING) || (prefix.equals(TAG_EVENTS) && node == null))
            return "[" + prefix.substring(1) + "]";
        else if (node == null)
            return "!!!INVALID NAMED for " + prefix + " !!!"; //comes from encodeNodeAttribute...
        else {
            if (node instanceof ElementWrapper) {
                return prefix;
//                ElementWrapper elementWrapper = (ElementWrapper) node;
//
//                return elementWrapper.getAdaptedElement().getName().getValue();
            } else {
//                if (container instanceof Map){
//                    ((Map)container).put("archetype_node_id", node.getArchetypeNodeId());
//                }
//                return node.getArchetypeNodeId();
                return prefix;
            }
        }
    }

    protected void compactEntry(Object node, Map<String, Object>target, String key, Map<String, Object>entry) throws Exception{
        //if entry is null, ignore, the dirty bit is not set...
        if (entry != null) {
            if (entry.keySet().size() == 1 && entry.get(TAG_VALUE) != null) {
                Object o = entry.get(TAG_VALUE);
                // TAG_VALUE is not required in the properties map representation
                putObject(null, target, rawJsonKey(key), o);
            } else
                putObject(null, target, rawJsonKey(key), entry); //unchanged and uncompacted
        }
    }

    protected Map<String, Object> setElementAttributesMap(Element element) throws Exception {
        Map<String, Object>ltree = newPathMap();

        if (element != null && element.getValue() != null && !element.getValue().toString().isEmpty()){
            log.debug(itemStack.pathStackDump()+"="+ element.getValue());
            Map<String, Object> valuemap = newPathMap();
            //VBeanUtil.setValueMap(valuemap, element.getValue());
//            putObject(element, valuemap, TAG_NAME, element.getName().getValue());

            ltree.put(I_DvTypeAdapter.TAG_CLASS_RAW_JSON, "ELEMENT");
            ltree.put(rawJsonKey(TAG_NAME), element.getName());
            ltree.put(rawJsonKey(TAG_ARCHETYPE_NODE_ID), element.getArchetypeNodeId());
            if (element.getName() instanceof DvCodedText) {
                DvCodedText dvCodedText = (DvCodedText)element.getName();
                if (dvCodedText.getDefiningCode() != null)
                    putObject(element, valuemap, rawJsonKey(TAG_DEFINING_CODE), dvCodedText.getDefiningCode());
            }

//            putObject(element, valuemap, TAG_CLASS, getCompositeClassName(element.getValue()));
//            putObject(valuemap, TAG_CLASS, element.getValue().getClass().getSimpleName());
            //assign the actual object to the value (instead of its field equivalent...)
           ltree.put(rawJsonKey(TAG_VALUE), element.getValue());
//
//            encodePathItem(valuemap, null);
//            if (tag_mode == WalkerOutputMode.PATH) {
//                putObject(valuemap, TAG_PATH, elementStack.pathStackDump());
//            }

//            ltree.put(TAG_VALUE, valuemap);
        }
        else
            throw new IllegalArgumentException("Invalid element detected in map");

        return ltree;
    }

//    private void initTags() throws IllegalAccessException {
//        TagSetter.setTagDefinition(this, TagSetter.DefinitionSet.RAW_JSON);
//    }

    @Override
    public String dbEncode(Locatable locatable) throws Exception {
        Map<String, Object> stringObjectMap = processItem(locatable);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance(I_DvTypeAdapter.AdapterType.RAW_JSON);
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(stringObjectMap);
    }

    @Override
    public String dbEncode(Composition composition) throws Exception {
        Map<String, Object> stringObjectMap = process(composition);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance(I_DvTypeAdapter.AdapterType.RAW_JSON);
        Gson gson = builder.setPrettyPrinting().create();
//        JsonElement jsonObject = gson.toJsonTree(composition);
        return gson.toJson(composition);
    }

    @Override
    public String dbEncode(String tag, Locatable locatable) throws Exception {
        Map<String, Object> stringObjectMap = processItem(tag, locatable);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance(I_DvTypeAdapter.AdapterType.RAW_JSON);
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(stringObjectMap);
    }
}
