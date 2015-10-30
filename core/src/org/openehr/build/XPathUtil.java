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

package org.openehr.build;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openehr.rm.Attribute;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.representation.Element;

public class XPathUtil {

	public Set<String> extractXPaths(Locatable root) throws Exception {
		Set<String> set = new HashSet<String>();		
		buildPath(root, "", set);		
		return set;
	}

	private void buildPath(Object obj, String path, Set<String> paths) throws Exception {
		if(obj instanceof List) {
			List list = (List) obj;
			for(int i = 0, j = list.size(); i<j; i++) {
				Object o = list.get(i);
				buildPath(o, path + "[" + (i + 1) + "]", paths); // recurse
			}
		} else if(obj instanceof Element) { 
	
			paths.add(path);
	
		} else if(obj instanceof Locatable) {
	
			Locatable locatable = (Locatable) obj;
			if(locatable.isArchetypeRoot() && (!path.isEmpty())) {
				return;
			}					
	
			inspector.retrieveRMAttributes(obj.getClass().getSimpleName());
			Class<?> klass = obj.getClass();
			Map<String, Attribute> attributeMap = inspector.getAttributes(klass);
			for(String attributeName : attributeMap.keySet()) {
				Attribute attribute = attributeMap.get(attributeName);
		
				if(attribute.system()) {
					continue;
				}
		
				Method method = klass.getMethod("get" + 
					attributeName.substring(0, 1).toUpperCase() + 
					attributeName.substring(1), null);
		
				assert(method != null);
		
				Object value = method.invoke(obj, null);
				if(value != null) {
					buildPath(value, path + "/" + attributeName, paths); // recurse
				}
			}
		} 
	}

	public Map<String, Set<String>> extractPaths(Locatable root) throws Exception {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();		
		buildPaths(root, "", "", map);		
		return map;
	}

	private void buildPaths(Object obj, String aPath, String xPath, Map<String, Set<String>> paths) throws Exception {
		if(obj instanceof List) {
			List list = (List) obj;
			for(int i = 0, j = list.size(); i<j; i++) {
				Object o = list.get(i);
		
				assert (o instanceof Locatable);
				String nodeId = ((Locatable) o).getArchetypeNodeId();
		
				assert (nodeId != null && !nodeId.isEmpty());
		
				String currentAPath = aPath + "[" + nodeId + "]";
				String currentXPath = xPath + "[" + (i + 1) + "]";
		
				buildPaths(o, currentAPath, currentXPath, paths);  // recurse		
			}
		} else if(obj instanceof Element) { 
	
			Set<String> set = paths.get(aPath);
			if(set == null) {
				set = new HashSet<String>();
				paths.put(aPath, set);
			}
			set.add(xPath);
	
		} else if(obj instanceof Locatable) {
	
			inspector.retrieveRMAttributes(obj.getClass().getSimpleName());
			Class<?> klass = obj.getClass();
	
			Map<String, Attribute> attributeMap = inspector.getAttributes(klass);
			for(String attributeName : attributeMap.keySet()) {
				Attribute attribute = attributeMap.get(attributeName);
				if(attribute.system()) {
					continue;
				}				
		
				String methodName = "get" + 
					attributeName.substring(0, 1).toUpperCase() + 
					attributeName.substring(1);
		
				Method method = klass.getMethod(methodName, null);
		
				assert(method != null);
		
				Object value = method.invoke(obj, null);
		
				if(value != null && !methodName.equals("getParent")) {
					String nodeIdStr = "";
					if (value instanceof Locatable){
					nodeIdStr = "[" + ((Locatable)value).getArchetypeNodeId() + "]";
					}
					String currentAPath = aPath + "/" + attributeName+nodeIdStr;
					String currentXPath = xPath + "/" + attributeName;
					buildPaths(value, currentAPath, currentXPath, paths); // recurse
				}
			}
		}
	}

	public Set<String> extractRootXPaths(Locatable root) throws Exception {
		Set<String> set = new HashSet<String>();		
		buildRootPath(root, "", set);		
		return set;
	}

	private void buildRootPath(Object obj, String path, Set<String> paths) throws Exception {
		if(obj instanceof List) {
			List list = (List) obj;
			for(int i = 0, j = list.size(); i<j; i++) {
				Object o = list.get(i);
				buildRootPath(o, path + "[" + (i + 1) + "]", paths); // recurse
			}
		} else if(obj instanceof Locatable) {
			Locatable l = (Locatable) obj;
	
			if(l.isArchetypeRoot() && !path.isEmpty()) {
				paths.add(path);	
				return;
			} 						
			inspector.retrieveRMAttributes(obj.getClass().getSimpleName());
			Class<?> klass = obj.getClass();
			Map<String, Attribute> attributeMap = inspector.getAttributes(klass);
			for(String attributeName : attributeMap.keySet()) {
				Attribute attribute = attributeMap.get(attributeName);
		
				if(attribute.system()) {
					continue;
				}					
				String getterName = "get"+ 
					attributeName.substring(0, 1).toUpperCase() + 
					attributeName.substring(1);
				Method method = klass.getMethod(getterName, null); 
		
				assert(method != null);
		
				Object value = method.invoke(obj, null);
				if(value != null) {
					buildRootPath(value, path + "/" + attributeName, paths); // recurse
				}
			}
		}
	}

	private RMObjectBuilder inspector = RMObjectBuilder.getInstance();
}
