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

package com.ethercis.ehr.util;

import com.ethercis.ehr.encode.CompositionSerializer;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by christian on 6/26/2017.
 */
public class PathItem {

    Map<String, Object> valueMap;

    public PathItem(Map<String, Object> valueMap) {
        this.valueMap = valueMap;
    }

    public String findPathValue(){
        String path = null;

        if (valueMap.containsKey(CompositionSerializer.TAG_PATH)){
            path = (String)valueMap.get(CompositionSerializer.TAG_PATH);
            //and strip the suffix
            path = path.substring(0, path.lastIndexOf("/"));
        }
        else {
            //traverse the map
            return traverseMap(valueMap, 1);
        }

        return path;
    }

    String traverseMap(Map<String, Object> map, int level){
        for (Object entry: map.values()){
            if (entry instanceof Map){
                Map<String, Object> valueMap = (Map)entry;
                if (valueMap.containsKey(CompositionSerializer.TAG_PATH)) {
                    String path = (String) valueMap.get(CompositionSerializer.TAG_PATH);
                    //trim the resulting path according to the recursion level
                    String[] splitPath = path.split("/");
                    String retPath = String.join("/", Arrays.copyOfRange(splitPath, 0, splitPath.length - level - 1));

                    return retPath;
                }
                else
                    return traverseMap(valueMap, level++);
            }
        }
        return null;
    }
}
