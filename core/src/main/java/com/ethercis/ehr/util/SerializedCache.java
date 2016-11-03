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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christian on 10/25/2016.
 */
public class SerializedCache {

    private class TypedCache {
        private byte[] serialized;
        private Class clazz;

        public TypedCache(byte[] serialized, Class clazz) {
            this.serialized = serialized;
            this.clazz = clazz;
        }

        public byte[] getSerialized() {
            return serialized;
        }

        public Class getClazz() {
            return clazz;
        }
    }

    private Map<String, TypedCache> cache = new HashMap<>();
//    private static SerializedCache instance = new SerializedCache();

    public SerializedCache(){}

//    public SerializedCache getInstance(){
//        return instance;
//    }

    public byte[] getObject(String id){
        return cache.get(id).getSerialized();
    }

    public Class getClass(String id){
        return cache.get(id).getClazz();
    }

    public void put(String id, byte[] bytes, Class clazz){
        cache.put(id, new TypedCache(bytes, clazz));
    }

    public boolean containsKey(String id){
        return cache.containsKey(id);
    }

    public void invalidate(){
        cache.clear();
    }
}
