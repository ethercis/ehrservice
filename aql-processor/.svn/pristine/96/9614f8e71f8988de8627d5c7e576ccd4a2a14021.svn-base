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

import com.ethercis.aql.compiler.OrderAttribute;
import com.ethercis.aql.compiler.QueryParser;
import com.ethercis.aql.containment.IdentifierMapper;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.PathResolver;
import com.ethercis.aql.sql.queryImpl.CompositionAttributeQuery;
import com.ethercis.aql.sql.queryImpl.JsonbEntryQuery;
import org.apache.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.SelectQuery;

import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.COMP_EXPAND;

/**
 * Bind the abstract representation of a SELECT clause into a SQL expression
 * Created by christian on 5/4/2016.
 */
public class SelectBinder {

    Logger logger = Logger.getLogger(SelectBinder.class);

    private JsonbEntryQuery jsonbEntryQuery;
    private CompositionAttributeQuery compositionAttributeQuery;
    private PathResolver pathResolver;
    protected List<VariableDefinition> selectVariableDefinitions;
    private IdentifierMapper mapper;
    DSLContext context ;
    private WhereBinder whereBinder;


    public SelectBinder(DSLContext context, IdentifierMapper mapper, List<VariableDefinition> definitions, List whereClause, String serverNodeId) {
        this.context = context;
        this.pathResolver = new PathResolver(context, mapper);
        this.mapper = mapper;
        this.selectVariableDefinitions = definitions;
        this.jsonbEntryQuery = new JsonbEntryQuery(context, pathResolver, definitions);
        this.compositionAttributeQuery = new CompositionAttributeQuery(context, pathResolver, definitions, serverNodeId);
        this.whereBinder = new WhereBinder(jsonbEntryQuery, compositionAttributeQuery, whereClause, mapper);
    }

    public SelectBinder(DSLContext context, QueryParser parser, String serverNodeId) {
        this(context, parser.getIdentifierMapper(), parser.getVariables(), parser.getWhereClause(), serverNodeId);
    }


    /**
     * bind with path resolution depending on composition
     * @param comp_id
     * @return
     */
    public SelectQuery<?> bind(UUID comp_id){
        pathResolver.resolvePaths(comp_id);

        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        for (VariableDefinition variableDefinition: selectVariableDefinitions) {
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            SelectQuery<?> subSelect = context.selectQuery();
            switch (className){
                case "COMPOSITION":
                case "EHR":
                    field = compositionAttributeQuery.makeField(comp_id, identifier, variableDefinition, true);
//                    selectFields.add(compositionAttributeQuery.selectField(comp_id, identifier, variableDefinition));
                    break;
                default:
                    field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, true);
//                    selectFields.add(jsonbEntryQuery.selectField(comp_id, identifier, variableDefinition));
                    break;
            }
//            field = DSL.field(field);
            if (field == null)
                throw new IllegalArgumentException("Could not resolve field :"+variableDefinition);

            selectQuery.addSelect(field);
            jsonbEntryQuery.inc();
        }
        //add the from bit
//        selectQuery.addFrom(CompositionQuerySnippets.content(context, comp_id));
        selectQuery.addFrom(COMP_EXPAND);
        whereBinder.setInitialCondition(COMP_EXPAND.COMPOSITION_ID.eq(comp_id));
//        selectQuery.addConditions();
        selectQuery.addConditions(whereBinder.bind(comp_id));
//        return context.select(selectFields);
        return selectQuery;
    }

    /**
     * simple bind on composition  attributes only
     * @param inSet
     * @return
     */
    public SelectQuery<?> bind(Result<?> inSet){
        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        for (VariableDefinition variableDefinition: selectVariableDefinitions) {
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            SelectQuery<?> subSelect = context.selectQuery();
            switch (className){
                case "COMPOSITION":
                    field = compositionAttributeQuery.makeField(null, identifier, variableDefinition, true);
//                    selectFields.add(compositionAttributeQuery.selectField(comp_id, identifier, variableDefinition));
                    break;
                default:
                    throw new IllegalArgumentException("Bind with an IN set is only applicable to non JSON entries");
            }
//            field = DSL.field(field);
            selectQuery.addSelect(field);
            jsonbEntryQuery.inc();
        }
        //add the from bit
//        selectQuery.addFrom(CompositionQuerySnippets.content(context, comp_id));
        selectQuery.addFrom(COMP_EXPAND);
        whereBinder.setInitialCondition(COMP_EXPAND.COMPOSITION_ID.in(inSet));
//        selectQuery.addConditions();
        selectQuery.addConditions(whereBinder.bind(null));

//        if (topAttributes != null){
//            selectQuery.addLimit(topAttributes.getWindow());
//        }
//        return context.select(selectFields);
        return selectQuery;
    }

    private boolean isPathBasedField(VariableDefinition variableDefinition){
        String identifier = variableDefinition.getIdentifier();
        String className = mapper.getClassName(identifier);
        if (!className.equals("COMPOSITION"))
            return true;
        return false;
    }

    private boolean hasPathBasedField(List<OrderAttribute> orderAttributes){
        for (OrderAttribute orderAttribute: orderAttributes){
            VariableDefinition variableDefinition = orderAttribute.getVariableDefinition();
            if (isPathBasedField(variableDefinition))
                return true;
        }
        return false;
    }

    public SelectQuery<?> bind(SelectQuery<?> containQuery, Integer limit, List<OrderAttribute> orderAttributes){
        if (hasPathBasedField(orderAttributes))
            throw new IllegalArgumentException("Order by using a path based expression is not yet implemented");

        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        for (VariableDefinition variableDefinition: selectVariableDefinitions) {
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            switch (className){
                case "COMPOSITION":
                    field = compositionAttributeQuery.makeField(null, identifier, variableDefinition, true);
                    break;
                default:
                    throw new IllegalArgumentException("Bind with an IN set is only applicable to non JSON entries");
            }
            selectQuery.addSelect(field);
            jsonbEntryQuery.inc();
        }
        //add the from bit
        selectQuery.addFrom(COMP_EXPAND);
        whereBinder.setInitialCondition(COMP_EXPAND.COMPOSITION_ID.in(containQuery.asField()));
        selectQuery.addConditions(whereBinder.bind(null));

        if (limit != null)
            selectQuery.addLimit(limit);

        if (orderAttributes != null && !orderAttributes.isEmpty()) {
            for (OrderAttribute orderAttribute: orderAttributes){
                //assumes COMPOSITION fields
                Field<?> field = compositionAttributeQuery.makeField(null, null, orderAttribute.getVariableDefinition(), false);

                if (field != null){
                    if (orderAttribute.getDirection() != null) {
                        switch (orderAttribute.getDirection()) {
                            case ASC:
                                selectQuery.addOrderBy(field.asc());
                                break;
                            case DESC:
                                selectQuery.addOrderBy(field.desc());
                                break;
                        }
                    }
                    else //default to ASCENDING
                        selectQuery.addOrderBy(field.asc());
                }
                else
                    throw new IllegalArgumentException("Could not resolve field in ORDER BY clause:"+orderAttribute.getVariableDefinition());

            }
        }

        return selectQuery;
    }


    public void addOrderParameters(SelectQuery selectQuery, List<OrderAttribute> orderAttributes, UUID comp_id ){
        if (orderAttributes != null && !orderAttributes.isEmpty()) {
            for (OrderAttribute orderAttribute: orderAttributes){
                //assumes COMPOSITION fields
                VariableDefinition variableDefinition = orderAttribute.getVariableDefinition();
                String identifier = variableDefinition.getIdentifier();
                String className = mapper.getClassName(identifier);
                Field<?> field;
                switch (className){
                    case "COMPOSITION":
                        field = compositionAttributeQuery.makeField(comp_id, identifier, variableDefinition, true);
//                    selectFields.add(compositionAttributeQuery.selectField(comp_id, identifier, variableDefinition));
                        break;
                    default:
                        field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, true);
//                    selectFields.add(jsonbEntryQuery.selectField(comp_id, identifier, variableDefinition));
                        break;
                }

                if (field != null){
                    if (orderAttribute.getDirection() != null) {
                        switch (orderAttribute.getDirection()) {
                            case ASC:
                                selectQuery.addOrderBy(field.asc());
                                break;
                            case DESC:
                                selectQuery.addOrderBy(field.desc());
                                break;
                        }
                    }
                    else //default to ASCENDING
                        selectQuery.addOrderBy(field.asc());
                }
                else
                    throw new IllegalArgumentException("Could not resolve field in ORDER BY clause:"+orderAttribute.getVariableDefinition());

            }
        }
    }

    public PathResolver getPathResolver() {
        return pathResolver;
    }
}
