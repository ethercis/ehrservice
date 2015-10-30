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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/3/2015.
 */
public class FlatJsonUtil_OLD {

    private static final int DELIM_NODE = '/';
    private static final int DELIM_FIELD = '|';
    private static final int DELIM_INDEX = ':';

    private static DELIM_TYPE checkFirstDelimiter(String string){
        int offset_node = string.indexOf(DELIM_NODE);
        int offset_field = string.indexOf(DELIM_FIELD);
        int offset_index = string.indexOf(DELIM_INDEX);

        if (offset_node > 0 && (offset_field > 0 ? offset_node < offset_field : true) && (offset_index > 0 ? offset_node < offset_index : true))
            return DELIM_TYPE.NODE;
        else if (offset_field > 0 && (offset_node > 0 ? offset_field < offset_node : true) && (offset_index > 0 ? offset_field < offset_index : true))
            return DELIM_TYPE.FIELD;
        else if (offset_index > 0)
            return DELIM_TYPE.INDEX;
        else
            return DELIM_TYPE.END;
    }

    private static String[] splitDelimitedString(DELIM_TYPE delimiter, String string){
        List<String> list = new ArrayList<>();
        switch (delimiter){
            case NODE:
                list.add(string.substring(0, string.indexOf(DELIM_NODE)));
                list.add(string.substring(string.indexOf(DELIM_NODE) + 1));
                break;
            case FIELD:
                list.add(string.substring(0, string.indexOf(DELIM_FIELD)));
                list.add(string.substring(string.indexOf(DELIM_FIELD) + 1));
                break;
            case INDEX:
                list.add(string.substring(0, string.indexOf(DELIM_INDEX)));
                list.add(string.substring(string.indexOf(DELIM_INDEX) + 1));
                break;
        }
        return list.toArray(new String[]{});
    }

    //
    private static Map<String, Object> unflattenEntry(Map<String, Object> tree, String key, String value){

        if (tree.size() == 0)
            return attachTree(tree, key, value);

        Map<String, Object> child = tree;

        //get the first entry not in map
        Token token = Token.nextToken(key);
        while (token.type != DELIM_TYPE.END && child.containsKey(token.value)){
            if (!token.isArray()) {
                child = (Map) child.get(token.value);
                key = Token.nextFlow(key);
                token = Token.nextToken(key);
            }
            else { //array item list
                List itemlist = (ArrayList)child.get(token.getValue());
                key = Token.nextFlow(key);
                token = Token.nextToken(key);
                Integer index = Integer.valueOf(token.getValue());
                if (index > itemlist.size() - 1){ //new entry
                    child = new HashMap<>();
                    itemlist.add(child);
                    key = Token.nextFlow(key);
                    token = Token.nextToken(key);
                }
                else {
                    child = (Map) itemlist.get(index);
                    key = Token.nextFlow(key);
                    token = Token.nextToken(key);
                }
            }
        }

        if (child != null)
            child.put(token.isLeaf() ? key : token.getValue(), token.isLeaf() ? value : attachTree(child, Token.nextFlow(key), value));

        return tree;
    }

    private static Map<String, Object> attachTree(Map<String, Object> tree, String key, String value){
        Map<String, Object> subtree = null;
        String[] splitted = null;

        subtree = new HashMap<>();

        Token token = Token.nextToken(key);

        //get first delimiter
        switch (token.type){
            case NODE:
                subtree.put(token.value, attachTree(subtree, Token.nextFlow(key), value));
                break;
            case FIELD:
                subtree.put(token.value, attachTree(subtree, Token.nextFlow(key), value));
                break;
            case INDEX:
                //get the index value
                String keyValue = token.getValue();
                key = Token.nextFlow(key);

                //TODO: check index
//                token = Token.nextToken(key);
//                Integer index = Integer.valueOf(token.getValue());

                //insert a new Array into the subtree
                List<Object> array = new ArrayList<>();
                array.add(attachTree(subtree, Token.nextFlow(key), value));
                subtree.put(keyValue, array);
                break;
            case END:
                subtree.put(key, value);
                break;

        }

        return subtree;
    }

    public static Map<String, Object> unflattenJSON(Map<String, String> flatJsonMap){

        Map<String, Object> tree = new HashMap<>();

        for (String jsonKey: flatJsonMap.keySet()){
            tree = unflattenEntry(tree, jsonKey, flatJsonMap.get(jsonKey));
        }

        return tree;
    }

    //convenient traversal methods
//    Map<String, Object> nextOccurence(Map<String, Object> tree){
//
//    }

    private static enum DELIM_TYPE {
        NODE, FIELD, INDEX, END
    }

    private static enum NODE_TYPE { TREE, ARRAY, FIELD, LEAF}

    private static class Token {
        private String value;
        private DELIM_TYPE type;
        private NODE_TYPE node_type;

        public Token(String value, DELIM_TYPE type){
            this.value = value;
            this.type = type;
            this.node_type = (type == DELIM_TYPE.NODE ? NODE_TYPE.TREE :
                                (type == DELIM_TYPE.FIELD ? NODE_TYPE.FIELD:
                                        (type == DELIM_TYPE.INDEX ? NODE_TYPE.ARRAY : NODE_TYPE.LEAF)));
        }

        public static Token nextToken(String flow){
            DELIM_TYPE tokenType = checkFirstDelimiter(flow);
            if (tokenType == DELIM_TYPE.END)
                return new Token(null, tokenType);
            else {
                String splitDelimitedString[] = splitDelimitedString(tokenType, flow);
                return new Token(splitDelimitedString[0], tokenType);
            }
        }

        public static String nextFlow(String flow){
            DELIM_TYPE tokenType = checkFirstDelimiter(flow);
            String splitDelimitedString[] = splitDelimitedString(tokenType, flow);
            return splitDelimitedString[1];
        }

        public String getValue(){return value;}
        public DELIM_TYPE getType(){return type;}

        public boolean isTree(){ return node_type == NODE_TYPE.TREE;}
        public boolean isArray() {return node_type == NODE_TYPE.ARRAY;}
        public boolean isField() {return node_type == NODE_TYPE.FIELD;}
        public boolean isLeaf() {return node_type == NODE_TYPE.LEAF;}
    }
}
