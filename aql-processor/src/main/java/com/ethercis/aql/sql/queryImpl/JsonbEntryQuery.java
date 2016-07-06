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

package com.ethercis.aql.sql.queryImpl;

import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.PathResolver;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;

import org.openehr.rm.common.archetyped.Locatable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;
/**
 * Generate an SQL field corresponding to a JSONB data value query
 * Created by christian on 5/6/2016.
 */
public class JsonbEntryQuery extends ObjectQuery implements I_QueryImpl {

    private static final String SELECT_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(select json_object_keys("+COMP_EXPAND.ENTRY+"::json))";
    private final static String JSONBSelector_OPEN = SELECT_CONTENT_MACRO +" #>> '{";
    private final static String JSONBSelector_CLOSE = "}'";
    public final static String Jsquery_OPEN = SELECT_CONTENT_MACRO +" @@ '";
    public final static String Jsquery_CLOSE = " '::jsquery";
    private static final String namedItemPrefix = " and name/value='";

    private static final String listIdentifier[] = {
            "/content",
            "/items",
            "/activities",
            "/events"
    };

    public JsonbEntryQuery(DSLContext context, PathResolver pathResolver, List<VariableDefinition> definitions){
        super(context, pathResolver, definitions);
    }

    private static boolean isList(String predicate){
        if (predicate.equals("/activities"))
            return false;
        for (String identifier: listIdentifier)
            if (predicate.startsWith(identifier)) return true;
        return false;
    }

    private enum PATH_PART {IDENTIFIER_PATH_PART, VARIABLE_PATH_PART}

    //deals with special tree based entities
    private static void encodeTreeMapNodeId(List<String> jqueryPath, String nodeId){
        if (nodeId.startsWith("/events")){
            //this is an exception since events are represented in an event tree
            jqueryPath.add("/events");
        }
    }

    public static List<String> jqueryPath(PATH_PART path_part, String path, String defaultIndex){
        //CHC 160607: this offset (1 or 0) was required due to a bug in generating the containment table
        //from a PL/pgSQL script. this is no more required
//        int offset = (path_part == PATH_PART.IDENTIFIER_PATH_PART ? 1 : 0);
        int offset = 0;
        List<String> segments = Locatable.dividePathIntoSegments(path);
        List<String> jqueryPath = new ArrayList<>();
        String nodeId = null;
        for (int i = offset; i < segments.size(); i++){
            nodeId = segments.get(i);
            if (nodeId.contains(namedItemPrefix)){
                nodeId = nodeId.substring(0, nodeId.indexOf(namedItemPrefix))+"]";
            }
            else if (nodeId.contains(",")){
                nodeId = nodeId.substring(0, nodeId.indexOf(","))+"]";
            }

            nodeId = "/"+nodeId;

            encodeTreeMapNodeId(jqueryPath, nodeId);

            jqueryPath.add(nodeId);

            if (isList(nodeId)) {
                jqueryPath.add(defaultIndex);
            }

        }

//        String jquery = StringUtils.removeEnd(jqueryPath.toString(), ",");
        if (path_part.equals(PATH_PART.VARIABLE_PATH_PART)) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = jqueryPath.size() - 1; i >= 0; i--) {
                if (jqueryPath.get(i).matches("[0-9]*") || jqueryPath.get(i).contains("[at"))
                    break;
                String item = jqueryPath.remove(i);
                stringBuffer.insert(0, item);
            }
            nodeId = EntryAttributeMapper.map(stringBuffer.toString());
            if (nodeId != null)
                jqueryPath.add(nodeId);
//            if (StringUtils.endsWithAny(jquery, new String[]{"/value"}))
//                //append the value key
//                jquery += ",value";
        }

//        String jquery = StringUtils.join(jqueryPath.toArray(new String[] {}));

        return jqueryPath;
    }

    private static int retrieveIndex(String nodeId) {
        if (nodeId.contains("#")){
            Integer indexValue = Integer.valueOf((nodeId.split("#")[1]).split("']")[0]);
            return indexValue;
        }
        return 0;
    }


    @Override
    public Field<?> makeField(UUID compositionId, String identifier, VariableDefinition variableDefinition, boolean withAlias){
        String path = pathResolver.pathOf(variableDefinition.getIdentifier());
        if (path == null)
            throw new IllegalArgumentException("Could not find a path for identifier:"+variableDefinition.getIdentifier());
        String alias = variableDefinition.getAlias();

        List<String> itemPathArray = new ArrayList<>();

        itemPathArray.addAll(jqueryPath(PATH_PART.IDENTIFIER_PATH_PART, path, "0"));
        itemPathArray.addAll(jqueryPath(PATH_PART.VARIABLE_PATH_PART, variableDefinition.getPath(), "0"));

        resolveArrayIndex(itemPathArray);

        String itemPath = StringUtils.join(itemPathArray.toArray(new String[] {}), ",");

        itemPath = wrapQuery(itemPath, JSONBSelector_OPEN, JSONBSelector_CLOSE);

        Field<?> fieldPathItem = null;
        if (withAlias) {
            if (StringUtils.isNotEmpty(alias))
                fieldPathItem = DSL.field(itemPath, String.class).as(alias);
            else {
                String tempAlias = "FIELD_" + getSerial();
                fieldPathItem = DSL.field(itemPath, String.class).as(tempAlias);
            }
        }
        return fieldPathItem;
    }

    @Override
    public Field<?> whereField(UUID compositionId, String identifier, VariableDefinition variableDefinition){
        String path = pathResolver.pathOf(variableDefinition.getIdentifier());
        if (path == null)
            throw new IllegalArgumentException("Could not find a path for identifier:"+variableDefinition.getIdentifier());

        List<String> itemPathArray = new ArrayList<>();

        itemPathArray.addAll(jqueryPath(PATH_PART.IDENTIFIER_PATH_PART, path, "#"));
        itemPathArray.addAll(jqueryPath(PATH_PART.VARIABLE_PATH_PART, variableDefinition.getPath(), "#"));

        StringBuffer jsqueryPath = new StringBuffer();

        for (int i = 0; i < itemPathArray.size(); i++){
            if (!itemPathArray.get(i).equals("#"))
                jsqueryPath.append("\""+itemPathArray.get(i)+"\"");
            else
                jsqueryPath.append(itemPathArray.get(i));
            if (i < itemPathArray.size() - 1)
                jsqueryPath.append(".");
        }
//        String itemPath = StringUtils.join(itemPathArray.toArray(new String[] {}), ".");

//        itemPath = wrapQuery(itemPath, Jsquery_OPEN, Jsquery_CLOSE);
        Field<?> fieldPathItem = DSL.field(jsqueryPath.toString(), String.class);
        return fieldPathItem;
    }

    private void resolveArrayIndex(List<String> itemPathArray) {

        for (int i=0; i < itemPathArray.size(); i++ ){
            String nodeId = itemPathArray.get(i);
            if (nodeId.contains("#")){
                Integer index = retrieveIndex(nodeId);
                //change the default index of the previous one
                if (i - 1 >= 0){
                   itemPathArray.set(i - 1, index.toString());
                }
                //remove to the name/value short cut part...
                String nodeIdTrimmed = nodeId.split(",")[0]+"]";
                itemPathArray.set(i, nodeIdTrimmed);
            }
        }
    }


    private String wrapQuery(String itemPath, String open, String close){
        if (itemPath.contains("/item_count")){
            //trim the last array index in the prefix
            //look ahead for an index expression: ','<nnn>','
            String[] segments = itemPath.split("(?=(,[0-9]*,))");
            //trim the last index expression
            String pathPart = StringUtils.join(ArrayUtils.subarray(segments, 0, segments.length-1));
            return "jsonb_array_length(content #> '{"+pathPart+"}')";
        }
        else
            return open +itemPath+ close;

    }


}
