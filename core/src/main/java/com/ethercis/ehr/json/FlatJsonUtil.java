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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/3/2015.
 */
public class FlatJsonUtil {

    private static Logger log = LogManager.getLogger(FlatJsonUtil.class);

    private static final int DELIM_TREENODE = '/';
    private static final int DELIM_FIELD = '|';
    private static final int DELIM_ARRAYINDEX = ':';

    public static Map<String, Object> unflattenJSON(Map<String, String> flatJsonRecords){

        Map<String, Object> tree = new HashMap<>();

        for (String jsonKey: flatJsonRecords.keySet()){
            tree = unflattenEntry(tree, jsonKey, flatJsonRecords.get(jsonKey));
        }

        return tree;
    }

    public static Map inputStream2Map(Reader reader){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Converters.registerDateTime(gsonBuilder);
        Converters.registerDuration(gsonBuilder);
        gsonBuilder.registerTypeAdapter(Period.class, new JodaPeriodAdapter());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(reader, Map.class);
    }

    private static DELIM_TYPE checkFirstDelimiter(String string){
        int offset_node = string.indexOf(DELIM_TREENODE);
        int offset_field = string.indexOf(DELIM_FIELD);
        int offset_index = string.indexOf(DELIM_ARRAYINDEX);

        if (offset_node > 0 && (offset_field > 0 ? offset_node < offset_field : true) && (offset_index > 0 ? offset_node < offset_index : true))
            return DELIM_TYPE.TREENODE;
        else if (offset_field > 0 && (offset_node > 0 ? offset_field < offset_node : true) && (offset_index > 0 ? offset_field < offset_index : true))
            return DELIM_TYPE.FIELD;
        else if (offset_index > 0)
            return DELIM_TYPE.ARRAYINDEX;
        else
            return DELIM_TYPE.END;
    }

    private static String[] splitDelimitedString(DELIM_TYPE delimiter, String string){
        List<String> list = new ArrayList<>();
        switch (delimiter){
            case TREENODE:
                list.add(string.substring(0, string.indexOf(DELIM_TREENODE)));
                list.add(string.substring(string.indexOf(DELIM_TREENODE) + 1));
                break;
            case FIELD:
                list.add(string.substring(0, string.indexOf(DELIM_FIELD)));
                list.add(string.substring(string.indexOf(DELIM_FIELD) + 1));
                break;
            case ARRAYINDEX:
                list.add(string.substring(0, string.indexOf(DELIM_ARRAYINDEX)));
                list.add(string.substring(string.indexOf(DELIM_ARRAYINDEX) + 1));
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
            }
            else { //array item list
                List itemlist = (ArrayList) child.get(token.getValue());
                Integer index = token.getIndex();
                if (index > itemlist.size() - 1) { //new entry
                    child = new HashMap<>();
                    itemlist.add(child);
                } else {
                    if (index < 0) {
                        throw new IllegalArgumentException("Index is out of bound for an ArrayIndex");
                    }
                    child = (Map) itemlist.get(index);
                }
            }
            key = Token.nextFlow(key);
            token = Token.nextToken(key);
        }

        if (child != null) {
            if (token.isArray()){ //this is a new arraylist
                List itemlist = new ArrayList<>();
                child.put(token.getValue(), itemlist);
                key = Token.nextFlow(key);
                token = Token.nextToken(key);
                Map newmap = new HashMap<String, Object>();
                itemlist.add(newmap);
                child = newmap;

            }

            child.put(token.isLeaf() ? key : token.getValue(), token.isLeaf() ? value : attachTree(child, Token.nextFlow(key), value));
        }

        return tree;
    }

    private static Map<String, Object> attachTree(Map<String, Object> tree, String key, String value){
        Map<String, Object> subtree = null;
        String[] splitted = null;

        subtree = new HashMap<>();

        Token token = Token.nextToken(key);

        //get first delimiter
        switch (token.type){
            case TREENODE:
                subtree.put(token.value, attachTree(subtree, Token.nextFlow(key), value));
                break;
            case FIELD:
                subtree.put(token.value, attachTree(subtree, Token.nextFlow(key), value));
                break;
            case ARRAYINDEX:
                String keyValue = token.getValue();
//                key = Token.nextFlow(key);
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


    //convenient traversal methods
//    Map<String, Object> nextOccurence(Map<String, Object> tree){
//
//    }

    private static enum DELIM_TYPE {
        TREENODE, FIELD, ARRAYINDEX, END
    }

    private static enum NODE_TYPE { TREE, ARRAY, FIELD, LEAF}

    private static class Token {
        private String value;
        private DELIM_TYPE type;
        private NODE_TYPE node_type;
        private int index; //only for an array type

        public Token(String value, int index, DELIM_TYPE type){
            this.value = value;
            this.index = index;
            this.type = type;
            this.node_type = (type == DELIM_TYPE.TREENODE ? NODE_TYPE.TREE :
                                (type == DELIM_TYPE.FIELD ? NODE_TYPE.FIELD:
                                        (type == DELIM_TYPE.ARRAYINDEX ? NODE_TYPE.ARRAY : NODE_TYPE.LEAF)));
            log.debug("Token:"+value+", index:"+index+", type:"+type+", node_type:"+node_type);
        }

        public static Token nextToken(String flow){
            DELIM_TYPE tokenType = checkFirstDelimiter(flow);
            if (tokenType == DELIM_TYPE.END) {
                log.debug(flow+"->END");
                return new Token(null, -1, tokenType);
            }
            else {
                String splitDelimitedString[] = splitDelimitedString(tokenType, flow);
                log.debug(flow+"->"+tokenType.name()+":"+splitDelimitedString[0]);
                if (tokenType.compareTo(DELIM_TYPE.ARRAYINDEX) == 0){
                    //get the index value and skip to the next flow
                    String[] delimited = splitDelimitedString[1].split("[/|]");
                    Integer index = Integer.parseInt(delimited[0]);
                    return new Token(splitDelimitedString[0], index, tokenType);
                }
                else {
                    return new Token(splitDelimitedString[0], -1 , tokenType);
                }
            }
        }

        public static String nextFlow(String flow){
            DELIM_TYPE tokenType = checkFirstDelimiter(flow);
            String splitDelimitedString[] = splitDelimitedString(tokenType, flow);
            if (tokenType.compareTo(DELIM_TYPE.ARRAYINDEX) == 0){
                ///skip the index
                String[] delimited = splitDelimitedString[1].split("[/|]", 2);
                if (delimited.length > 1)
                    return delimited[1];
                else
                    return splitDelimitedString[0];
            }
            else {
                if (tokenType.compareTo(DELIM_TYPE.END) == 0){
                    return flow;
                }
                else
                    return splitDelimitedString[1];
            }
        }

        public String getValue(){return value;}
        public DELIM_TYPE getType(){return type;}
        public int getIndex() { return index;}

        public boolean isTree(){ return node_type == NODE_TYPE.TREE;}
        public boolean isArray() {return node_type == NODE_TYPE.ARRAY;}
        public boolean isField() {return node_type == NODE_TYPE.FIELD;}
        public boolean isLeaf() {return node_type == NODE_TYPE.LEAF;}
    }
}
