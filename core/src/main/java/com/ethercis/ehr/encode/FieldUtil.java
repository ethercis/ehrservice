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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.Attribute;

import com.google.gson.internal.Primitives;

public class FieldUtil {
	private static RMObjectBuilder builder = RMObjectBuilder.getInstance();
	private static Logger logger = LogManager.getLogger(FieldUtil.class);
	
	private static void getClassScalarAttributes(Map<String, Object>map, Class<?> clazz) throws SecurityException, NoSuchFieldException {
		Map<String, Attribute> thismap = builder.getAttributes(clazz);
		
		for (Attribute attr: thismap.values()) {
			Field afield = null;
			try {
				afield = clazz.getDeclaredField(attr.name());
			} catch (NoSuchFieldException e) {
				; //do nothing, it is potentially left at the super class level (ex. accuracy for DvQuantity)
			}
			if (afield != null && (Primitives.isPrimitive(afield.getType()) || afield.getType() == String.class)) {
				map.put(afield.getName(), afield.getType());
			}
		}
	}
	
	public static Map<String, Object> getScalarAttributes(Class<?> clazz) throws Exception {
		Map<String, Object> retmap = new HashMap<String, Object>();
		
		getClassScalarAttributes(retmap, clazz);
		
		return retmap;
	}
	
	public static Map<String, Object> getRequiredAttributes(Class<?> clazz) throws SecurityException, NoSuchFieldException {
		Map<String, Attribute> thismap = builder.getAttributes(clazz);
		Map<String, Class<?>> classMap = builder.getAttributeTypes(clazz);
		Map<String, Object> retmap = new HashMap<String, Object>();
		
		for (Attribute attr: thismap.values()) {
			if (attr.required()) {
				//check if field is a java primitive or openehr datatype
				Class attrClass = classMap.get(attr.name());
				if (attrClass.getName().startsWith("java.lang"))
					retmap.put(attr.name(), attrClass.getName());
				else if (attrClass.getName().contains(RMObjectBuilder.OPENEHR_RM_PACKAGE))
					retmap.put(attr.name(), getRequiredAttributes(attrClass));
				else //assume primitive
					retmap.put(attr.name(), attrClass.getName());
			}
		}
		
		return retmap;
	}

	public static Map<String, Object> getAttributes(Object object) throws Exception {
		Class<?> clazz = object.getClass();
		Map<String, Attribute> thismap = builder.getAttributes(clazz);
		Map<String, Class<?>> classMap = builder.getAttributeTypes(clazz);
		Map<String, Object> retmap = new HashMap<String, Object>();

		for (Attribute attr: thismap.values()) {
			if (true) {
				boolean isRequired = attr.required();
				//check if field is a java primitive or openehr datatype
				Class attrClass = classMap.get(attr.name());
				if (attrClass.getName().startsWith("java.lang."))
					retmap.put(attr.name(), attrClass.getName());
				else if (attrClass.getName().contains(RMObjectBuilder.OPENEHR_RM_PACKAGE)) {
					//check if type is abstract (ex. DvOrdered) due to java type erasure
					if (Modifier.isAbstract(attrClass.getModifiers())){
						//check for getter and access the field to check its actual class

						String methodName = "get"+Character.toString(attr.name().charAt(0)).toUpperCase()+attr.name().substring(1);
						try {
							Method getter = object.getClass().getDeclaredMethod(methodName, null);
							Object field = getter.invoke(object, null);
							retmap.put(attr.name(), field.getClass());
						}
						catch (Exception e){
							logger.warn("Cannot handle field:"+attr.name());
						}
					}
					else
						retmap.put(attr.name(), attrClass.getName() /*getRequiredAttributes(attrClass)*/);
				}
				else //assume primitive
					retmap.put(attr.name(), attrClass.getName());
			}
		}

		return retmap;
	}

//	static int depth = -1;
	static Stack<String> keyStack = new Stack<>();

	public static Map<String, String> flatten(Map<String, Object> map){
		Map<String, String> retMap = new TreeMap<>();

		flatten(retMap, null, map);

		return retMap;
	}

	public static List<Object> flatten(Map<String, String> resultMap, String key, Map<String, Object> map){

		if (key != null)
			keyStack.push(key);

		List<Object> retlist = new ArrayList<>();

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value =  entry.getValue();

			if (value instanceof Map) {
				retlist.addAll(flatten(resultMap, entry.getKey(), (Map) entry.getValue()));
			} else {
				keyStack.push(entry.getKey());
				retlist.add(value);
				StringBuffer stringBuffer = new StringBuffer();

				Collections.list(keyStack.elements()).forEach(s1 -> stringBuffer.append(s1));

				resultMap.put(stringBuffer.toString(), entry.getValue().toString());
			}

			keyStack.pop();
		}
		return retlist;
	}

	public static Class<?> getFieldClass(Class<?> clazz, String name) throws SecurityException, NoSuchFieldException {

		Field field = clazz.getDeclaredField(name);
		
		return (field == null ? null : field.getType());
	}
}
