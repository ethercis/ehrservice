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

import com.ethercis.aql.definition.I_VariableDefinition;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.PathResolver;
import com.ethercis.aql.sql.binding.I_JoinBinder;
import com.ethercis.ehr.encode.CompositionSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;

import org.openehr.rm.common.archetyped.Locatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;
/**
 * Generate an SQL field corresponding to a JSONB data value query
 * Created by christian on 5/6/2016.
 */
public class JsonbEntryQuery extends ObjectQuery implements I_QueryImpl {

//    //COMP_EXPAND (Composition query)
//    private static final String SELECT_COMPOSITION_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(select jsonb_object_keys("+COMP_EXPAND.ENTRY+"))";
////    private static final String SELECT_COMPOSITION_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(select json_object_keys("+COMP_EXPAND.ENTRY+"::json))";
////    private static final String SELECT_COMPOSITION_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(jsonb_object_keys("+COMP_EXPAND.ENTRY+"))";
//    private final static String JSONBSelector_COMPOSITION_OPEN = SELECT_COMPOSITION_CONTENT_MACRO +" #>> '{";
////    private final static String JSONBSelector_COMPOSITION_OPEN = SELECT_COMPOSITION_CONTENT_MACRO +" #> '{";
//    public final static String Jsquery_COMPOSITION_OPEN = SELECT_COMPOSITION_CONTENT_MACRO +" @@ '";

    //ENTRY
//    private static final String SELECT_COMPOSITION_CONTENT_MACRO = ENTRY.ENTRY.;
    //    private static final String SELECT_COMPOSITION_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(select json_object_keys("+COMP_EXPAND.ENTRY+"::json))";
//    private static final String SELECT_COMPOSITION_CONTENT_MACRO = COMP_EXPAND.ENTRY+"->(jsonb_object_keys("+COMP_EXPAND.ENTRY+"))";
    private final static String JSONBSelector_COMPOSITION_OPEN = ENTRY.ENTRY_ +" #>> '{";
    //    private final static String JSONBSelector_COMPOSITION_OPEN = SELECT_COMPOSITION_CONTENT_MACRO +" #> '{";
    public final static String Jsquery_COMPOSITION_OPEN = ENTRY.ENTRY_ +" @@ '";


    //OTHER_DETAILS (Ehr Status Query)
//    private static final String SELECT_EHR_OTHER_DETAILS_MACRO = STATUS.OTHER_DETAILS+"->('"+ CompositionSerializer.TAG_OTHER_DETAILS+"')";
    private static final String SELECT_EHR_OTHER_DETAILS_MACRO = I_JoinBinder.statusRecordTable.field(STATUS.OTHER_DETAILS)+"->('"+ CompositionSerializer.TAG_OTHER_DETAILS+"')";
    private final static String JSONBSelector_EHR_OTHER_DETAILS_OPEN = SELECT_EHR_OTHER_DETAILS_MACRO +" #>> '{";
//    private final static String JSONBSelector_EHR_OTHER_DETAILS_OPEN = SELECT_EHR_OTHER_DETAILS_MACRO +" #> '{";
    public final static String Jsquery_EHR_OTHER_DETAILS_OPEN = SELECT_EHR_OTHER_DETAILS_MACRO +" @@ '";

    //OTHER_CONTEXT (Composition context other_context Query)
    //TODO: make the prefix dependant on the actual passed argument (eg. context/other_context[at0001])
    private static final String SELECT_EHR_OTHER_CONTEXT_MACRO = EVENT_CONTEXT.OTHER_CONTEXT+"->('"+CompositionSerializer.TAG_OTHER_CONTEXT+"[at0001]"+"')";
    private final static String JSONBSelector_EHR_OTHER_CONTEXT_OPEN = SELECT_EHR_OTHER_CONTEXT_MACRO +" #>> '{";
//    private final static String JSONBSelector_EHR_OTHER_CONTEXT_OPEN = SELECT_EHR_OTHER_CONTEXT_MACRO +" #> '{";
    public final static String Jsquery_EHR_OTHER_CONTEXT_OPEN = SELECT_EHR_OTHER_CONTEXT_MACRO +" @@ '";

    public final static String matchNodePredicate = "/(content|protocol|data|description|instruction|items|activities|activity|composition|entry|evaluation|observation|action|at)\\[([(0-9)|(A-Z)|(a-z)|\\-|_|\\.]*)\\]";

    //Generic stuff
    private final static String JSONBSelector_CLOSE = "}'";
    public final static String Jsquery_CLOSE = " '::jsquery";
    private static final String namedItemPrefix = " and name/value='";
    public static final String TAG_COMPOSITION = "/composition";

    private static boolean useEntry = false;

    private String jsonbItemPath;

    public static final String TAG_ACTIVITIES = "/activities";
    public static final String TAG_EVENTS = "/events";

    private static final String listIdentifier[] = {
            "/content",
            "/items",
            TAG_ACTIVITIES,
            TAG_EVENTS
    };

    private static boolean containsJqueryPath = false; //true if at leas one AQL path is contained in expression
    private static boolean jsonDataBlock = false;

    private String entry_root;

    public JsonbEntryQuery(DSLContext context, PathResolver pathResolver, List<I_VariableDefinition> definitions, String entry_root){
        super(context, pathResolver, definitions);
        this.entry_root = entry_root;
    }

    private static boolean isList(String predicate){
        if (predicate.equals(TAG_ACTIVITIES))
            return false;
        for (String identifier: listIdentifier)
            if (predicate.startsWith(identifier)) return true;
        return false;
    }

    public enum PATH_PART {IDENTIFIER_PATH_PART, VARIABLE_PATH_PART}

    public enum OTHER_ITEM {OTHER_DETAILS, OTHER_CONTEXT}

    //deals with special tree based entities
    private static void encodeTreeMapNodeId(List<String> jqueryPath, String nodeId){
        if (nodeId.startsWith(TAG_EVENTS)){
            //this is an exception since events are represented in an event tree
            jqueryPath.add(TAG_EVENTS);
        }
        else if (nodeId.startsWith(TAG_ACTIVITIES)){
            jqueryPath.add(TAG_ACTIVITIES);
        }
    }

    public static List<String> jqueryPath(PATH_PART path_part, String path, String defaultIndex){
        //CHC 160607: this offset (1 or 0) was required due to a bug in generating the containment table
        //from a PL/pgSQL script. this is no more required
//        int offset = (path_part == PATH_PART.IDENTIFIER_PATH_PART ? 1 : 0);
        if (path == null){ //partial path
            jsonDataBlock = true;
            return new ArrayList<>();
        }

        jsonDataBlock = false;
        int offset = 0;
        List<String> segments = Locatable.dividePathIntoSegments(path);
        List<String> jqueryPath = new ArrayList<>();
        String nodeId = null;
        for (int i = offset; i < segments.size(); i++) {
            nodeId = segments.get(i);
            if (nodeId.contains(namedItemPrefix)) {
                nodeId = nodeId.substring(0, nodeId.indexOf(namedItemPrefix)) + "]";
            } else if (nodeId.contains(",")) {
                nodeId = nodeId.substring(0, nodeId.indexOf(",")) + "]";
            }

            nodeId = "/" + nodeId;

            encodeTreeMapNodeId(jqueryPath, nodeId);

            jqueryPath.add(nodeId);

            if (isList(nodeId)) {
                if (path_part.equals(PATH_PART.VARIABLE_PATH_PART) && !(i == segments.size() - 1))
                    jqueryPath.add(defaultIndex);
                else if (path_part.equals(PATH_PART.IDENTIFIER_PATH_PART))
                    jqueryPath.add(defaultIndex);
            }
        }

//        String jquery = StringUtils.removeEnd(jqueryPath.toString(), ",");
        if (path_part.equals(PATH_PART.VARIABLE_PATH_PART)) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = jqueryPath.size() - 1; i >= 0; i--) {
                if (jqueryPath.get(i).matches("[0-9]*|#") || jqueryPath.get(i).contains("[at"))
                    break;
                String item = jqueryPath.remove(i);
                stringBuffer.insert(0, item);
            }
            nodeId = EntryAttributeMapper.map(stringBuffer.toString());
            if (nodeId != null)
                if (defaultIndex.equals("#")) { //jsquery
                    if (nodeId.contains(",")) {
                        String[] parts = nodeId.split(",");
                        jqueryPath.addAll(Arrays.asList(parts));
                    }
                    else {
                        jqueryPath.add(nodeId);
                    }
                }
                else {
                    jqueryPath.add(nodeId);
                }
//            if (StringUtils.endsWithAny(jquery, new String[]{"/value"}))
//                //append the value key
//                jquery += ",value";
        }

//        String jquery = StringUtils.join(jqueryPath.toArray(new String[] {}));

        useEntry = true;
        if (path_part.equals(PATH_PART.VARIABLE_PATH_PART) && jqueryPath.get(jqueryPath.size() - 1).matches(matchNodePredicate)) {
            jsonDataBlock = true;
        }
        return jqueryPath;
    }

    public static int retrieveIndex(String nodeId) {
        if (nodeId.contains("#")){
            Integer indexValue = Integer.valueOf((nodeId.split("#")[1]).split("']")[0]);
            return indexValue;
        }
        return 0;
    }


    public static Field<?> makeField(OTHER_ITEM type, String path, String alias, String variablePath, boolean withAlias){
        List<String> itemPathArray = new ArrayList<>();

        if (path != null)
            itemPathArray.addAll(jqueryPath(PATH_PART.IDENTIFIER_PATH_PART, path, "0"));
        itemPathArray.addAll(jqueryPath(PATH_PART.VARIABLE_PATH_PART, variablePath, "0"));

        resolveArrayIndex(itemPathArray);

        String itemPath = StringUtils.join(itemPathArray.toArray(new String[] {}), ",");

        itemPath = wrapQuery(itemPath, type.equals(OTHER_ITEM.OTHER_DETAILS) ? JSONBSelector_EHR_OTHER_DETAILS_OPEN : JSONBSelector_EHR_OTHER_CONTEXT_OPEN, JSONBSelector_CLOSE);

        if (itemPathArray.get(itemPathArray.size() - 1).contains("magnitude")){ //force explicit type cast for DvQuantity
            itemPath = "("+itemPath+")::float";
        }

        Field<?> fieldPathItem;
        if (withAlias) {
            if (StringUtils.isNotEmpty(alias))
                fieldPathItem = DSL.field(itemPath, String.class).as(alias);
            else {
                String tempAlias = "FIELD_" + getSerial();
                fieldPathItem = DSL.field(itemPath, String.class).as(tempAlias);
            }
        }
        else
            fieldPathItem = DSL.field(itemPath, String.class);

        containsJqueryPath = true;
        useEntry = true;
        return fieldPathItem;
    }


    @Override
    public Field<?> makeField(UUID compositionId, String identifier, I_VariableDefinition variableDefinition, boolean withAlias, Clause clause){
        if (entry_root == null) //case of (invalid) composition with null entry!
            return null;

        String path = pathResolver.pathOf(variableDefinition.getIdentifier());
        if (path == null) {
            //return a null field
            String cast = "";
            //TODO: explicit template based type cast will be implemented in a later release
            //force explicit type cast for DvQuantity
            if (variableDefinition.getPath().endsWith("magnitude"))
                cast = "::numeric";

            if (withAlias)
                return DSL.field(DSL.val((String)null)+cast).as(variableDefinition.getAlias());
            else
                return DSL.field(DSL.val((String)null)+cast);
//            throw new IllegalArgumentException("Could not resolve path for identifier:" + variableDefinition.getIdentifier());
        }
        String alias = variableDefinition.getAlias();

        List<String> itemPathArray = new ArrayList<>();
        itemPathArray.add(entry_root.replaceAll("'", "''"));
        if (!path.startsWith(TAG_COMPOSITION))
            itemPathArray.addAll(jqueryPath(PATH_PART.IDENTIFIER_PATH_PART, path, "0"));
        itemPathArray.addAll(jqueryPath(PATH_PART.VARIABLE_PATH_PART, variableDefinition.getPath(), "0"));

        resolveArrayIndex(itemPathArray);

        String itemPath = StringUtils.join(itemPathArray.toArray(new String[] {}), ",");


        itemPath = wrapQuery(itemPath, JSONBSelector_COMPOSITION_OPEN, JSONBSelector_CLOSE);

        //TODO: smarter typecast required (e.g. template based)
        if (itemPathArray.get(itemPathArray.size() - 1).contains("magnitude")){ //force explicit type cast for DvQuantity
            itemPath = "("+itemPath+")::numeric";
        }


        Field<?> fieldPathItem = null;
        if (withAlias) {
            if (StringUtils.isNotEmpty(alias))
                fieldPathItem = DSL.field(itemPath, String.class).as(alias);
            else {
                String tempAlias = "FIELD_" + getSerial();
                fieldPathItem = DSL.field(itemPath, String.class).as(tempAlias);
            }
        }
        else
            fieldPathItem = DSL.field(itemPath, String.class);

        containsJqueryPath = true;
        useEntry = true;

        if (isJsonDataBlock()){
            jsonbItemPath = toAqlPath(itemPathArray);
        }

        return fieldPathItem;
    }

    private String toAqlPath(List<String> itemPathArray) {
        List<String> aqlPath = new ArrayList<>();
        for (String path: itemPathArray){
            if (!path.startsWith(TAG_COMPOSITION) && !path.matches("[0-9]*")){
                aqlPath.add(path);
            }
        }
        return StringUtils.join(aqlPath.toArray(new String[]{}));
    }

    @Override
    public Field<?> whereField(UUID compositionId, String identifier, I_VariableDefinition variableDefinition){
        String path = pathResolver.pathOf(variableDefinition.getIdentifier());
        if (path == null)
            throw new IllegalArgumentException("Could not find a path for identifier:" + variableDefinition.getIdentifier());

        List<String> itemPathArray = new ArrayList<>();

        itemPathArray.add(entry_root.replaceAll("'", "''"));
        if (!path.startsWith(TAG_COMPOSITION))
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

//        itemPath = wrapQuery(itemPath, Jsquery_COMPOSITION_OPEN, Jsquery_CLOSE);
        Field<?> fieldPathItem = DSL.field(jsqueryPath.toString(), String.class);

        containsJqueryPath = true;
        useEntry = true;
        return fieldPathItem;
    }

    private static void resolveArrayIndex(List<String> itemPathArray) {

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


    private static String wrapQuery(String itemPath, String open, String close){
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

    @Override
    public boolean isEhrIdFiltered() {
        return false;
    }

    @Override
    public boolean isCompositionIdFiltered() {
        return false;
    }

    @Override
    public boolean isContainsJqueryPath() {
        return containsJqueryPath;
    }

    @Override
    public boolean isUseEntry() {
        return useEntry;
    }

    @Override
    public boolean isJsonDataBlock() {
        return jsonDataBlock;
    }

    @Override
    public String getJsonbItemPath() {
        return jsonbItemPath;
    }
}
