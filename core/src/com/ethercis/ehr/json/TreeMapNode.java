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

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to perform various traversal into a tree map structure for example from a json structure
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/13/2015.
 */
public class TreeMapNode implements I_TreeMapNode {

    private final Object treeNode;
    private final Object root;
    private Object currentNode;
    private Object parentNode;
    private Object currentKey; //key or index depending on the node

    public TreeMapNode(Object mapNode){
        this.treeNode = mapNode;
        this.root = mapNode;
        this.currentNode = mapNode;
        this.parentNode = null;
    }

    public TreeMapNode(Object node, Object parent){
        this.treeNode = node;
        this.root = node;
        this.currentNode = node;
        this.parentNode = parent;
    }

    public TreeMapNode(Object parent, Object currentNode, Object currentKey){
        this.treeNode = currentNode;
        this.root = currentNode;
        this.currentNode = currentNode;
        this.parentNode = parent;
        this.currentKey = currentKey;
    }


    public void setCurrentNode(Object currentNode) {
        this.currentNode = currentNode;
    }

    public void setParentNode(Object parentNode) {
        this.parentNode = parentNode;
    }

    public void setCurrentKey(Object currentKey) {
        this.currentKey = currentKey;
    }

    /**
     * return a treemap node or a collection of attributes or an arraylist depending on the case
     * @return
     */
    @Override
    public TreeMapNode nextChild(Object key) throws IllegalArgumentException {
        if (currentNode instanceof Map){
            Object object = ((Map) currentNode).get(key);
            TreeMapNode treeMapNode = new TreeMapNode(currentNode, object, key);
            return treeMapNode;
        }
        else if (currentNode instanceof List){
            Object object = ((List) currentNode).get((Integer)key);
            TreeMapNode treeMapNode = new TreeMapNode(currentNode, object, key);
            return treeMapNode;
        }
        else if (currentNode == null)
            return null; //termination
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }

    public TreeMapNode at(int index){
        if (currentNode instanceof List){
            if (index >= ((List) currentNode).size())
                throw new IllegalArgumentException("Index is out of bound:"+index);

            Object object = ((List) currentNode).get(index);
            TreeMapNode treeMapNode = new TreeMapNode(parentNode, object, index);
            return treeMapNode;
        }

        throw new IllegalArgumentException("Current node is not an array:"+ currentNode.toString());
    }

    @Override
    public TreeMapNode getChild(Object key) throws IllegalArgumentException {
        if (currentNode instanceof Map){
            return new TreeMapNode(currentNode, ((Map) currentNode).get(key), key);
//            return  ((Map) currentNode).get(key);
        }
        else if (currentNode instanceof List){
            return new TreeMapNode(currentNode,((List) currentNode).get((Integer)key), key );
//            return  ((List) currentNode).get((Integer)key);
        }
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }

    @Override
    public boolean hasChild(Object key) throws IllegalArgumentException {
        if (currentNode instanceof Map){
            return ((Map) currentNode).containsKey(key);
//            return  ((Map) currentNode).get(key);
        }
        else if (currentNode instanceof List){
            return (Integer)key < ((List)currentNode).size();
//            return  ((List) currentNode).get((Integer)key);
        }
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }


    @Override
    public Integer childSize(Object key){
        if (currentNode instanceof Map){
            Object object = ((Map) currentNode).get(key);
            return size(object);
        }
        else if (currentNode instanceof List){
            Object object = ((List) currentNode).get((Integer)key);
            return size(object);
        }
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }

    @Override
    public Map<String, Object> asAttributes(){
        if (currentNode instanceof Map){
            return  (Map) currentNode;
        }
        throw new IllegalArgumentException("Inconsistent map, could not retrieve key:"+currentKey); //should not happen, current key is not in this map!!!
    }

    @Override
    public Integer size(){
        return size(currentNode);
    }

    @Override
    public TreeMapNode nextSibling(){
        if (parentNode == null)
            return null;

        if (parentNode instanceof Map){
            Map currentMap = (Map) parentNode;
            Set<Object> keyset = currentMap.keySet();
            Object[] keyArray = keyset.toArray();
            for (int i = 0; i < keyArray.length; i++){
                if (keyArray[i].equals(currentKey)){
                    //skip to next if any
                    int j = i+1;
                    if (j >= keyArray.length)
                        return null;
                    else {
                        return new TreeMapNode(parentNode, currentMap.get(keyArray[j]), keyArray[j]);
//                        currentKey = keyArray[j];
//                        currentNode = currentMap.get(currentKey);
//                        return currentNode;
                    }
                }
            }
            throw new IllegalArgumentException("Inconsistent map, could not retrieve key:"+currentKey); //should not happen, current key is not in this map!!!
        }
        else if (parentNode instanceof List){
            List currentList = (List)parentNode;
            Integer currentIndex = (Integer)currentKey + 1;

            if (currentIndex < currentList.size()){
                return new TreeMapNode(parentNode, currentIndex, currentList.get(currentIndex));
//                currentKey = currentIndex;
//                currentNode = currentList.get(currentIndex);
//                return currentNode;
            }
            return null;
        }
        throw new IllegalArgumentException("Unsupported currentNode type:"+currentNode);
    }

    @Override
    public int size(Object object) throws IllegalArgumentException{
        if (object instanceof Map)
            return ((Map) object).values().size();
        else if (object instanceof List)
            return ((List) object).size();
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }


    @Override
    public boolean hasChild() throws IllegalArgumentException{
        if (currentNode instanceof Map)
            return ((Map) currentNode).values().size() > 0;
        else if (currentNode instanceof List)
            return ((List) currentNode).size() > 0;
        else
            throw new IllegalArgumentException("Unsupported type in tree:"+ currentNode.toString());
    }

    public static TreeMapNode findNode(Map<String, Object> mapTree, String path){
        TreeMapNode treeMapNode = new TreeMapNode(mapTree);

        String[] segments = path.split("[/:]");

        for (String key: segments){

            if (StringUtils.isNumeric(key)){
                Integer index = Integer.parseInt(key);
                treeMapNode = treeMapNode.at(index);
            }
            else {
                treeMapNode = treeMapNode.nextChild(key);
            }
        }

        return treeMapNode;
    }

    @Override
    public boolean isTreeNode(){
        return (currentNode instanceof Map);
    }

    @Override
    public boolean isArray(){
        return (currentNode instanceof List);
    }

    @Override
    public boolean hasParent(){
        return (parentNode != null);
    }

    public boolean contains(Object key) {
        if (currentNode instanceof Map)
            return ((Map)currentNode).containsKey(key);
        else if (currentNode instanceof List)
            return ((List)currentNode).size() > ((Integer)key);
        else
            return false;
    }

    @Override
    public Object getCurrentNode(){
        return currentNode;
    }

    @Override
    public TreeMapNode getCurrentTree(){
        return this;
    }

    @Override
    public Object getCurrentKey(){
        return currentKey;
    }


}
