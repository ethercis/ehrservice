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

package com.ethercis.transform.rawjson;

import com.ethercis.ehr.encode.*;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.RMObjectBuildingException;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by christian on 8/30/2016.
 */
public class RawJsonParser {
    private static Logger logger = LogManager.getLogger(RawJsonParser.class);
    private static ItemStack itemStack = new ItemStack();
    private static String valueClass = null;
    private static int depth = 0;
    private static boolean isItems = false;
    private static RMObjectBuilder rmObjectBuilder = new RMObjectBuilder();

    public Map<String, Object> parseRawJson(Reader reader){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Converters.registerDateTime(gsonBuilder);
        Converters.registerDuration(gsonBuilder);
        gsonBuilder.registerTypeAdapter(Period.class, new JodaPeriodAdapter());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(reader, Map.class);
    }

    public static Map<String, Object> serialize(Map<String, Object> itemStructureRawMap) throws Exception {
        Map<String, Object> serialized = newPathMap();
        for (String key: itemStructureRawMap.keySet()) {
            Object item = itemStructureRawMap.get(key);
            if (item instanceof Map) {
                if (key.toLowerCase().equals("otherdetails"))
                    key = CompositionSerializer.TAG_OTHER_DETAILS.substring(1);
//                itemStack.pushStacks(CompositionSerializer.TAG_CLASS, ((Map<String, String>) item).get("@class"));
                itemStack.pushStacks("/"+key, key);
                serialized.put("/" + key, translate(item));
//                translate(item);
            }
            else if (ClassUtils.isPrimitiveOrWrapper(item.getClass()) || item.getClass().getPackage().getName().startsWith("java")){
                logger.debug(key + "->" + item.toString());
            }
            else
                throw new IllegalArgumentException("Cannot handle object of class:"+item.getClass());
        }
        return serialized;
    }

    public static Object translate(Object item) throws Exception {
        if (item instanceof Map) {
            Map<String, Object> treeMap = newPathMap();
            Map<String, Object> map = (Map<String, Object>)item;
//            Class clazz = rmObjectBuilder.retrieveRMType((String) map.get("@class"));
            String nodeId = (String) map.get("archetype_node_id");
            String name = map.containsKey("name") ? ((Map<String, String>) (map.get("name"))).get("value") : null;
            if (map.containsKey("items")){
                itemStack.pushStacks(encodeNodeId(CompositionSerializer.TAG_ITEMS, nodeId), name);
                treeMap.put(CompositionSerializer.TAG_CLASS, toRmType((String)map.get(I_DvTypeAdapter.TAG_CLASS_RAW_JSON)));
                treeMap.put(encodeNodeId(CompositionSerializer.TAG_ITEMS, nodeId), translate(map.get("items")));
            }else if (map.containsKey("data")){
                treeMap.put(CompositionSerializer.TAG_CLASS, toRmType((String)map.get(I_DvTypeAdapter.TAG_CLASS_RAW_JSON)));
                itemStack.pushStacks(encodeNodeId(CompositionSerializer.TAG_DATA, nodeId), name);
                treeMap.put(encodeNodeId(CompositionSerializer.TAG_DATA, nodeId), map.get("data"));
            }else if (map.containsKey("value")){
                treeMap.put(CompositionSerializer.TAG_CLASS, toRmType((String) map.get(I_DvTypeAdapter.TAG_CLASS_RAW_JSON)));
                itemStack.pushStacks(encodeNodeId(isItems ? CompositionSerializer.TAG_ITEMS : CompositionSerializer.TAG_DATA, nodeId), name);
//                translate(map.get("value"));
//                System.out.println("value-->" + map.get("value").toString());
//                System.out.println("path-->"+itemStack.pathStackDump());
                Object camelMap = encodeRM(map.get("value"));
                //set the result as a serialized Element
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put(CompositionSerializer.TAG_NAME, name);
                valueMap.put(CompositionSerializer.TAG_VALUE, camelMap);
                valueMap.put(CompositionSerializer.TAG_PATH, itemStack.pathStackDump());
                valueMap.put(CompositionSerializer.TAG_CLASS, valueClass);
                treeMap.put(encodeNodeId(isItems ? CompositionSerializer.TAG_ITEMS : CompositionSerializer.TAG_DATA, nodeId), valueMap);
            }
            itemStack.popStacks();
            return treeMap;
        }
        else if (item instanceof ArrayList){
            List<Object> list = new ArrayList<>();
            isItems = true;
            for (Object object: (ArrayList)item){
//                System.out.println("Array..."+itemStack.pathStackDump());
                list.add(translate(object));
            }
            isItems = false;
            return list;
        }
        return null;
    }

    private static String encodeNodeId(String tag, String nodeId){
        return nodeId == null ? tag : tag+"["+nodeId+"]";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> newPathMap(){
        return MapUtils.predicatedMap(new TreeMap<String, Object>(), PredicateUtils.uniquePredicate(), null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> newMultiMap(){
        return MapUtils.multiValueMap(new HashMap<String, Object>());
    }

    //traverse the value map and change the keys to be camelCase
    private static Object encodeRM(Object object) throws RMObjectBuildingException {
        valueClass = null;
        depth = 0;

        return toRMconvention(object);
    }

    private static Object toRMconvention(Object object) throws RMObjectBuildingException {
        if (object instanceof Map){
            depth++;
            Map<String, Object> map = (Map<String, Object>)object;
//            List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
            Map<String, Object> rmMap = new HashMap<>();
            for (Map.Entry<String, Object> entry: map.entrySet()){
                //get the key
                String camelKey = fieldSnakeToCamel(entry.getKey());
                Object item = entry.getValue();
                if (!(ClassUtils.isPrimitiveOrWrapper(item.getClass()) || item.getClass().getPackage().getName().startsWith("java"))){
                    rmMap.put(camelKey, toRMconvention(item));
                }
                else {
                    if (camelKey.equals(I_DvTypeAdapter.TAG_CLASS_RAW_JSON)){
                        if (depth == 1)
                            valueClass = toRmType((String) item);
                    }
                    else {
                        rmMap.put(camelKey, item);
                        rmMap.put(CompositionSerializer.TAG_CLASS, toRmType((String)map.get(I_DvTypeAdapter.TAG_CLASS_RAW_JSON)));
                    }
                }
            }
            return rmMap;
        }
        if (object instanceof List){
            List<Object> list = new ArrayList<>();
            for (Object item: (List<Object>)object){
                list.add(toRMconvention(item));
            }
            return list;
        }
        return null;
    }

    //copied from com.ethercis.validation.Utils.java, to avoid cycling redundancy
    public static String fieldSnakeToCamel(String snakeString){
        String retVal = snakeToCamel(snakeString);
        return  retVal.substring(0,1).toLowerCase()+retVal.substring(1);
    }

    public static String snakeToCamel(String snakeString){
        String retVal = Arrays.stream(snakeString.split("\\_"))
                .map(String::toLowerCase)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining());
        return  retVal;
    }

    public static String dbEncode(Reader reader) throws Exception {
        RawJsonParser rawJsonParser = new RawJsonParser();
        Map<String, Object> retmap = rawJsonParser.parseRawJson(reader);
        Map<String, Object> serialMap = rawJsonParser.serialize(retmap);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(serialMap);
    }

    public static String dbEncode(Map<String, Object> map) throws Exception {
        Map<String, Object> serialMap = serialize(map);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(serialMap);
    }

    private static String toRmType(String type) throws RMObjectBuildingException {
        //convert to camel case
        String classname = rmObjectBuilder.toClassName(type);
//        Class clazz = rmObjectBuilder.retrieveRMType(type);
        return classname;
    }
}
