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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.Attribute;

import com.google.gson.internal.Primitives;

public class FieldUtil {
	private static RMObjectBuilder builder = RMObjectBuilder.getInstance();
	
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
		Map<String, Object> retmap = new HashMap<String, Object>();
		
		for (Attribute attr: thismap.values()) {
			Field afield = null;
			try {
				afield = clazz.getDeclaredField(attr.name());
			} catch (NoSuchFieldException e) {
				; //do nothing, it is potentially left at the super class level (ex. accuracy for DvQuantity)
			}
			if (afield != null && attr.required()) {
				retmap.put(afield.getName(), afield.getType());
			}
		}
		
		return retmap;
	}
	
	public static Class<?> getFieldClass(Class<?> clazz, String name) throws SecurityException, NoSuchFieldException {

		Field field = clazz.getDeclaredField(name);
		
		return (field == null ? null : field.getType());
	}
}
