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

package com.ethercis.aql.sql.binding;

import com.ethercis.aql.compiler.TopAttributes;
import com.ethercis.aql.definition.FromDefinition;
import org.apache.commons.lang.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ethercis.jooq.pg.Tables.COMPOSITION;
import static com.ethercis.jooq.pg.Tables.CONTAINMENT;

/**
 * NOT USED IN THE CURRENT VERSION
 * Created by christian on 5/2/2016.
 */
public class FromBinder {

    private Result<?> inSet; //this contains the list of identified composition IDs selected in the FROM/CONTAIN clause

    private static String selectCompositions =
            "SELECT composition.id AS comp_id FROM ehr.composition WHERE \n" +
            "composition.id IN (";

    private static String selectCompositionsWithEhrId =
            "SELECT composition.ehr_id, composition.id  AS comp_id FROM ehr.composition WHERE \n" +
                    "composition.id IN (";
    private static DSLContext context;

    private static Map<String, String> fieldMap() {

        return Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("ehr_id/value", "ehr_id ="))
                .collect(Collectors.toMap((e) -> e.getKey(), (e)->e.getValue())
        ));
    }

    public FromBinder(DSLContext context){
        this.context = context;
    }


    @Deprecated
    public static String _bind(String containClause, FromDefinition fromEhrStatements) {
        boolean hasEhrCondition = false;
        if (fromEhrStatements.isEHR()){
            StringBuffer statement = new StringBuffer();
            statement.append(containClause);
            statement.append(")");
//            statement.append(" AND (");
            StringBuffer whereEhrCondition = new StringBuffer();
            for (FromDefinition.EhrPredicate predicate: fromEhrStatements.getEhrPredicates()) {
                if (fieldMap().containsKey(predicate.getField())) {
                    hasEhrCondition = true;
                    whereEhrCondition.append(" " + fieldMap().get(predicate.getField()) + " ");


                    if (predicate.getValue() != null && predicate.getValue().length() > 0)
                        whereEhrCondition.append(" " + predicate.getValue() + " ");
                }
                    ; //TODO: manage statements with no ehr/value specified...

            }
            if (hasEhrCondition) {
                statement.append(" AND (");
                statement.append(whereEhrCondition);
                statement.append(")");
                statement.insert(0, selectCompositions);
            }
            else {
                if (fromEhrStatements.getIdentifier() != null) {
                    statement.insert(0, selectCompositionsWithEhrId);
                }
                else
                    statement.insert(0, selectCompositions);
            }

            return statement.toString();
        }
        return null;
    }

    public Select<?> bind(String containClause, FromDefinition fromEhrStatements) {
        List<Condition> conditions = new ArrayList<>();

        SelectField<?>[] fields = {
            COMPOSITION.EHR_ID,
            COMPOSITION.ID.as("comp_id")
        };

        if (fromEhrStatements.isEHR()) {
//            SelectQuery<?> select = DSL.select(containClause);
            inSet = context.fetch(containClause);
            conditions.add(COMPOSITION.ID.in(inSet));

            for (FromDefinition.EhrPredicate predicate : fromEhrStatements.getEhrPredicates()) {
                if (fieldMap().containsKey(predicate.getField()) && StringUtils.isNotEmpty(predicate.getValue())) {
                    conditions.add(COMPOSITION.EHR_ID.eq(UUID.fromString(predicate.getValue().replaceAll("'",""))));
                }
                ; //TODO: manage statements with no ehr/value specified...
            }

            return context.select(fields).from(COMPOSITION).where(conditions);
        }
        else
            throw new IllegalArgumentException("Not a FROM EHR statement. Not implemented yet.");
    }

    public Result<?> getInSet() {
        return inSet;
    }

    public Result<?> getInSet(String containClause) {
        inSet = context.fetch(containClause);
        return inSet;
    }

    public Result<?> getInSet(String containClause, TopAttributes topAttributes) {
        Select<?> select;
        Table<?> contained = DSL.table(containClause).asTable("comp");
        if (topAttributes != null)
            select = context.select(CONTAINMENT.fields()).from(contained).limit(topAttributes.getWindow());
        else
            select = context.select(CONTAINMENT.fields()).from(contained);
        inSet = select.fetch();
        return inSet;
    }
}
