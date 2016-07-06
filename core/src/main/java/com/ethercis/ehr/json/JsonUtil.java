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
package com.ethercis.ehr.json;

import com.ethercis.ehr.encode.JodaPeriodAdapter;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;
import org.joda.time.Period;
import org.openehr.rm.composition.Composition;

import java.io.IOException;
import java.util.*;

/**
 * experimental. used to identify the best strategy for JSON import/export of RM compositions.
 */
public class JsonUtil {
	//this map holds the path=value properties to pass to a builder 
	Map<String, String> pathmap;

    //used for FLAT JSON

    ;

    /**
	 * return the attributes map as a JSon structure
	 * @return
	 */
	public static String toJsonString(Object c) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Converters.registerDateTime(gsonBuilder);
		Converters.registerDuration(gsonBuilder);
		gsonBuilder.registerTypeAdapter(Period.class, new JodaPeriodAdapter());
		Gson json = gsonBuilder.setPrettyPrinting().create();
		return json.toJson(c);
	}

//    private static String serializeComposition(Composition composition) throws IOException {
////        Map<String, Object> map = new HashMap<>();
////        map.put(JsonWriter.PRETTY_PRINT, "true");
//////        String output = JsonWriter.objectToJson(composition, map);
////        String output = JsonWriter.objectToJson(composition);
//
//        return JSONValue.toJSONString(composition);
//
//    }


    /**
     * THIS IS NOT WORKING AS IT SEEMS THERE ARE ISSUES WITH SOME CONSTRUCTORS (ex. TemplateID)
     * @param jsonString
     * @return
     */
//    private static Composition deserializeComposition(String jsonString) throws ParseException {
//        //and back
//
//        Composition retrieved = (Composition) JSONValue.parseWithException(jsonString);
//
//        return retrieved;
//    }

	public static JsonElement toJson(Object c) {
		Gson json = new GsonBuilder().setPrettyPrinting().create();
		return json.toJsonTree(c);		
	}
	
	private int push(Stack<String> stack, String s, int floor){
//		log.debug("-- PUSH:" + s);
		stack.push(s);
//		log.debug("FLOOR:"+floor+"->"+s);
		return floor++;
	}
	
	private int pop(Stack<String> stack, int floor){
//		log.debug("-- POP:"+ (pathStack.isEmpty() ? "*empty*":pathStack.lastElement()));
		if (!stack.empty()){
			stack.pop();
			return floor--;
//			log.debug("FLOOR:"+floor);
		}
		
		return -1;
	}
	
	private String stackDump(Stack<String> stack){
		StringBuffer b = new StringBuffer();
		for (Object s: stack.toArray()) b.append((String)s);
		return b.toString();
	}
	
	public Map<String, String> flatten(String jsonString) throws JsonProcessingException, IOException{
		pathmap = new HashMap<>();
		int floor = 0;
		Stack<String> pathStack = new Stack<String>();

		JsonNode tree = new ObjectMapper().readTree(jsonString);
		
		addPath("", tree, pathStack, floor);
		
		return pathmap;
	}
	
	private void addPath(String currentPath, JsonNode node, Stack<String> stack, int floor) {
	     if (node.isObject()) {
	            ObjectNode objectNode = (ObjectNode) node;
	            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.getFields();
	            String pathPrefix = currentPath.isEmpty() ? "" : currentPath;

	            while (iter.hasNext()) {
	                Map.Entry<String, JsonNode> entry = iter.next();
	                addPath(pathPrefix + entry.getKey(), entry.getValue(), stack, floor);
	            }
	        } else if (node.isArray()) {
	            ArrayNode arrayNode = (ArrayNode) node;
	            for (int i = 0; i < arrayNode.size(); i++) {
	                addPath(currentPath + "[" + i + "]", arrayNode.get(i), stack, floor);
	            }
	        } else if (node.isValueNode()) {
	            ValueNode valueNode = (ValueNode) node;
//	            push(stack, currentPath, floor);
	            pathmap.put(currentPath, valueNode.asText());
	        }
//	     System.out.println(pathStackDump(stack));
//	     pop(stack, floor);
	}
}
