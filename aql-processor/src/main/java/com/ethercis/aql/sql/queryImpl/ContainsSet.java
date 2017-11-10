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
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;

import static com.ethercis.jooq.pg.Tables.CONTAINMENT;
import static com.ethercis.jooq.pg.Tables.ENTRY;

/**
 * Created by christian on 10/19/2016.
 */
public class ContainsSet {

    public static final String ENTRY_ROOT = "entry_root";

    private String containClause;
    private DSLContext context;
    private Select<?> select;

//    public ContainsSet(String containsClause, DSLContext context) {
//        this.containClause = containsClause;
//        this.context = context;
//
//        //jOOQ hack to support Postgres DISTINCT ON
//
//        Field<?> distinctOnTemplate = DSL.field("DISTINCT ON({0}) {0}", ENTRY.TEMPLATE_ID.getDataType(), ENTRY.TEMPLATE_ID).as(ENTRY.TEMPLATE_ID);
//        //(select root_json_key from jsonb_object_keys("ehr"."entry"."entry") root_json_key where root_json_key like '/composition%') as
////        Query jsonbRootKeySelect = DSL.query("select root_json_key from jsonb_object_keys(" + ENTRY.ENTRY_ + ") root_json_key where root_json_key like '/composition%')");
//        Field<?> entryKey = DSL.field("(select root_json_key from jsonb_object_keys(" + ENTRY.ENTRY_ + ") root_json_key where root_json_key like '/composition%')").as(ENTRY_ROOT);
////        Field<?> entryKey = DSL.field("jsonb_object_keys("+ENTRY.ENTRY_+")").as(ENTRY_ROOT); //calculated field
//
//        this.select = context
//                .select(distinctOnTemplate, CONTAINMENT.COMP_ID, CONTAINMENT.LABEL, entryKey)
//                .from(CONTAINMENT)
//                .join(ENTRY)
//                .on(CONTAINMENT.COMP_ID.eq(ENTRY.COMPOSITION_ID))
//                .where(CONTAINMENT.COMP_ID.in(DSL.field(containClause)));
//    }

    public ContainsSet(String containsClause, DSLContext context, List<VariableDefinition> whereFilter) {
        this.containClause = containsClause;
        this.context = context;

        //jOOQ hack to support Postgres DISTINCT ON

        Field<?> distinctOnTemplate = DSL.field("DISTINCT ON({0}) {0}", ENTRY.TEMPLATE_ID.getDataType(), ENTRY.TEMPLATE_ID).as(ENTRY.TEMPLATE_ID);
        //(select root_json_key from jsonb_object_keys("ehr"."entry"."entry") root_json_key where root_json_key like '/composition%') as
//        Query jsonbRootKeySelect = DSL.query("select root_json_key from jsonb_object_keys(" + ENTRY.ENTRY_ + ") root_json_key where root_json_key like '/composition%')");
//        Field<?> entryKey = DSL.field("(select root_json_key from jsonb_object_keys(" + ENTRY.ENTRY_ + ") root_json_key where root_json_key like '/composition%')").as(ENTRY_ROOT);
//        Field<?> entryKey = DSL.field("jsonb_object_keys("+ENTRY.ENTRY_+")").as(ENTRY_ROOT); //calculated field

//        Field<?> entryKey = DSL.field("'/composition['" +
//                "||\"ehr\".\"entry\".\"archetype_id\"" +
//                "||' and name/value='''" +
//                "||trim ('\"' from (\"ehr\".\"entry\".\"entry\" #> '{/name,0,value}')::text)" +
//                "||''']'").as(ENTRY_ROOT);

        //too costly
        Field<?> entryKey = DSL.field("'/composition' ||" +
                                        "substr(split_part(\"ehr\".\"entry\".\"entry\"::text, '/composition', 2), " +
                                        "0, " +
                                        "strpos(split_part(\"ehr\".\"entry\".\"entry\"::text, '/composition', 2), ']\":')+1)").as(ENTRY_ROOT);



        //temporary hack, extract only the where clause from the containment clause...

//        Condition filter;
//        boolean hasEhrFilter = false;
//
//        for (VariableDefinition variableDefinition: whereFilter){
//            if (variableDefinition.getPath().equals("EHR")){
//                filter = DSL.condition(variableDefinition.toString());
//                hasEhrFilter = true;
//            }
//        }

        String whereClause = containsClause.substring(containsClause.indexOf("WHERE")+5);

        this.select = context
                .select(distinctOnTemplate, CONTAINMENT.LABEL, CONTAINMENT.PATH, ENTRY.ENTRY_.as(ENTRY_ROOT))
                .from(CONTAINMENT)
                .join(ENTRY)
                .on(CONTAINMENT.COMP_ID.eq(ENTRY.COMPOSITION_ID))
                .where(whereClause);
    }

    public Result<?> getInSet() {
        return select.fetch();
    }

    public Select<?> getSelect() {
        //could not find the way to clone an existing select
        return  context
                .selectDistinct(CONTAINMENT.COMP_ID)
                .from(CONTAINMENT)
                .join(ENTRY)
                .on(CONTAINMENT.COMP_ID.eq(ENTRY.COMPOSITION_ID))
                .where(CONTAINMENT.COMP_ID.in(DSL.field(containClause)));
    }
}
