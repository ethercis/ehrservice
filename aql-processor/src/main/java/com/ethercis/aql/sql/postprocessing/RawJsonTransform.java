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

package com.ethercis.aql.sql.postprocessing;

import com.ethercis.aql.sql.QuerySteps;
import com.ethercis.aql.sql.binding.CompositionVersionedUuid;
import com.ethercis.aql.sql.binding.I_SelectBinder;
import com.ethercis.aql.sql.binding.JsonbBlockDef;
import com.ethercis.ehr.encode.rawjson.LightRawJsonEncoder;
import com.ethercis.ehr.encode.rawjson.RawJsonEncoder;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.google.gson.JsonSyntaxException;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

/**
 * Created by christian on 2/21/2017.
 */
public class RawJsonTransform implements I_RawJsonTransform {

    private final DSLContext context;

    public RawJsonTransform(DSLContext context) {
        this.context = context;
    }

    public static void toRawJson(Result<Record> result, Collection<QuerySteps> querySteps, I_KnowledgeCache knowledgeCache) {

        RawJsonEncoder rawJsonEncoder = new RawJsonEncoder(knowledgeCache);

        for (QuerySteps queryStep : querySteps) {
            if (queryStep.jsonColumnsSize() > 0) {
                for (int cursor = 0; cursor < result.size(); cursor++) {
                    Record record = result.get(cursor);
                    String templateIdNow = record.getValue(TEMPLATE_ID, String.class);
                    if (!templateIdNow.equals(queryStep.getTemplateId()))
                        continue;
                    for (JsonbBlockDef jsonbBlockDef : queryStep.getJsonColumns()) {
                        String jsonbOrigin = (String) record.getValue(jsonbBlockDef.getField());
                        if (jsonbOrigin == null)
                            continue;
                        String templateId = (String) record.getValue(TEMPLATE_ID);
                        String itemPath = jsonbBlockDef.getPath();
                        //apply the transformation
                        try {
//                            jsonbOrigin = "{\""+jsonbBlockDef.getField().getName()+"\":"+jsonbOrigin+"}";
                            String rawJson = new LightRawJsonEncoder(jsonbOrigin).encodeContentAsString(jsonbBlockDef.getField().getName());
//                            Object rawJson = rawJsonEncoder.encodeContentAsString(jsonbBlockDef.getField().getName(), templateId, jsonbOrigin, itemPath);
                            //debugging
                            if (jsonbOrigin.contains("@class"))
                                System.out.print("Hum...");
                            record.setValue(jsonbBlockDef.getField(), rawJson);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Could not encode raw json for template Id:" + templateId);
                        }
                    }
                }
            }
        }
    }

    public Result toRawJson(Result<Record> result) {

        Result resultSet = context.newResult(DSL.field(I_SelectBinder.DATA, String.class));

        for (int cursor = 0; cursor < result.size(); cursor++) {
            Record record = result.get(cursor);
            String jsonbOrigin = record.getValue(I_SelectBinder.DATA).toString();
            if (jsonbOrigin == null)
                continue;
            //apply the transformation
            try {
                Map rawJson = new LightRawJsonEncoder(jsonbOrigin).encodeContentAsMap(I_SelectBinder.DATA);
                //debugging
//                if (jsonbOrigin.contains("@class"))
//                    System.out.print("Hum...");
                //add the composition uuid
                rawJson.putAll(new CompositionVersionedUuid(record.getValue(I_SelectBinder.COMPOSITION_UID).toString()).toMap());
                Record newValue = context.newRecord(DSL.field(I_SelectBinder.DATA));
                newValue.setValue(DSL.field(I_SelectBinder.DATA), rawJson);
                resultSet.add(newValue);
                record.setValue(DSL.field(I_SelectBinder.DATA), rawJson);
            } catch (JsonSyntaxException js){
                System.out.print("duplicate key:"+js);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not encode raw json data"+e);
            }
        }
        return resultSet;
    }


    private static int columnIndex(List<Field> fields, String columnName) {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (field.getName().equals(columnName))
                return i;
        }
        return -1;
    }

    public static Result<Record> deleteNamedColumn(Result<Record> result, String columnName) {

        List<Field> fields = new ArrayList<>();

        fields.addAll(Arrays.asList(result.fields()));
        int ndx = columnIndex(fields, columnName);
        if (ndx >= 0) {
            fields.remove(ndx);
            Field[] arrayField = fields.toArray(new Field[]{});

            return result.into(arrayField);
        } else
            return result;
    }

//    public static Record cloneRecord(Record record, Collection<QuerySteps> querySteps){
//        List<Field> fields = new ArrayList<>();
//        List<String> jsonColumns = new ArrayList<>();
//
//        for (QuerySteps queryStep: querySteps) {
//            for (JsonbBlockDef jsonbBlockDef : queryStep.getJsonColumns()) {
//                jsonColumns.add(jsonbBlockDef.getField().getName());
//            }
//        }
//
//        fields.addAll(Arrays.asList(record.fields()));
//
//
//
//        Field[] arrayField = fields.toArray(new Field[]{});
//
//        Record newRecord = record.into(arrayField);
//        return newRecord;
//    }
}
