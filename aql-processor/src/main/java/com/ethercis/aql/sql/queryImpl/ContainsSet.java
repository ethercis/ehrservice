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

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;

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

    public ContainsSet(String containsClause, DSLContext context) {
        this.containClause = containsClause;
        this.context = context;

        //jOOQ hack to support Postgres DISTINCT ON

        Field<?> distinctOnTemplate = DSL.field("DISTINCT ON({0}) {0}", ENTRY.TEMPLATE_ID.getDataType(), ENTRY.TEMPLATE_ID).as(ENTRY.TEMPLATE_ID);
        Field<?> entryKey = DSL.field("jsonb_object_keys("+ENTRY.ENTRY_+")").as(ENTRY_ROOT); //calculated field

        this.select = context
                .select(distinctOnTemplate, CONTAINMENT.COMP_ID, CONTAINMENT.LABEL, entryKey)
                .from(CONTAINMENT)
                .join(ENTRY)
                .on(CONTAINMENT.COMP_ID.eq(ENTRY.COMPOSITION_ID))
                .where(CONTAINMENT.COMP_ID.in(DSL.field(containClause)));
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
