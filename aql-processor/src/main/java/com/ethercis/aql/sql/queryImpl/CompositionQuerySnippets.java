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

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * NOT USED
 * Created by christian on 5/9/2016.
 */
public class CompositionQuerySnippets {

    public static SelectQuery<?> content(DSLContext context, UUID compositionId){
        Field<?> skipHeaderExpression = DSL.field("json_object_keys("+COMP_EXPAND.ENTRY+"::json)");
//        SelectQuery<?> headerSubquery =  context.selectQuery();
//        headerSubquery.addSelect(skipHeaderExpression);
//        headerSubquery.addFrom(COMP_EXPAND);
//        headerSubquery.addConditions(COMP_EXPAND.COMPOSITION_ID.eq(compositionId));
        SelectQuery<?> selectQuery = context.selectQuery();
//        String select = COMP_EXPAND.ENTRY+"->("+headerSubquery.toString()+")";
//        Field<?> contentField = DSL.field(select).as("content");
        Field<?> contentField = DSL.field(COMP_EXPAND.ENTRY+"->json_object_keys("+COMP_EXPAND.ENTRY+"::json)").as("content");
        selectQuery.addSelect(COMP_EXPAND.fields());
        selectQuery.addSelect(contentField);
        selectQuery.addFrom(COMP_EXPAND);
        selectQuery.addConditions(COMP_EXPAND.COMPOSITION_ID.eq(compositionId));
        selectQuery.addLimit(1);

        return selectQuery;
    }
}
