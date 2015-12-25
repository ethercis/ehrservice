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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* ETHERCIS Project ehrservice
* Created by Christian Chevalley on 8/6/2015.
*/
public class ParentedHashMap<K, V> implements Map {
    Map<K,V> hashMap ;
    Map<K,V> parentMap;

    public ParentedHashMap(Map parent){
        this.parentMap = parent;
        this.hashMap = new HashMap<>();
    }

    public Map getParent(){
        return parentMap;
    }

    public Map getMap(){
        return hashMap;
    }

    public void put(Map parent, K key, V value){
        this.parentMap = parent;
        hashMap.put(key, value);
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return hashMap.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return hashMap.put((K)key, (V)value);
    }

    @Override
    public Object remove(Object key) {
        return hashMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        hashMap.putAll(m);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public Set keySet() {
        return hashMap.keySet();
    }

    @Override
    public Collection values() {
        return hashMap.values();
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return hashMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return hashMap.equals(o);
    }

    @Override
    public int hashCode() {
        return hashMap.hashCode();
    }
}
