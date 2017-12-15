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
import com.ethercis.aql.definition.I_VariableDefinition;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.PathResolver;
import com.ethercis.aql.sql.postprocessing.I_RawJsonTransform;
import com.ethercis.aql.sql.queryImpl.CompositionAttributeQuery;
import com.ethercis.aql.sql.queryImpl.I_QueryImpl;
import com.ethercis.aql.sql.queryImpl.JsonbEntryQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Bind the abstract representation of a SELECT clause into a SQL expression
 * Created by christian on 5/4/2016.
 */
public class SelectBinder {

    Logger logger = LogManager.getLogger(SelectBinder.class);

    private JsonbEntryQuery jsonbEntryQuery;
    private CompositionAttributeQuery compositionAttributeQuery;
    private PathResolver pathResolver;
    protected List<I_VariableDefinition> selectVariableDefinitions;
    protected List<JsonbBlockDef> jsonDataBlock = new ArrayList<>();
    private IdentifierMapper mapper;
    DSLContext context ;
    private WhereBinder whereBinder;
    private boolean isWholeComposition = false;

    public enum OptimizationMode {TEMPLATE_BATCH, NONE}

    private OptimizationMode optimizationMode;

    public SelectBinder(DSLContext context, IdentifierMapper mapper, List<I_VariableDefinition> definitions, List whereClause, String serverNodeId, OptimizationMode mode, String entry_root) {
        this.context = context;
        this.pathResolver = new PathResolver(context, mapper);
        this.mapper = mapper;
        this.selectVariableDefinitions = definitions;
        this.jsonbEntryQuery = new JsonbEntryQuery(context, pathResolver, definitions, entry_root);
        this.compositionAttributeQuery = new CompositionAttributeQuery(context, pathResolver, definitions, serverNodeId);
        this.whereBinder = new WhereBinder(jsonbEntryQuery, compositionAttributeQuery, whereClause, mapper);
        this.optimizationMode = mode;
    }

    public SelectBinder(DSLContext context, QueryParser parser, String serverNodeId, OptimizationMode mode, String entry_root) {
        this(context, parser.getIdentifierMapper(), parser.getVariables(), parser.getWhereClause(), serverNodeId, mode, entry_root);
    }


    /**
     * bind with path resolution depending on composition
     * @param comp_id
     * @return
     */
    public SelectQuery<?> bind(String template_id, UUID comp_id, String label, String entry_root){
        pathResolver.resolvePaths(template_id, comp_id);

        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        boolean containsJsonDataBlock = false;

        for (I_VariableDefinition variableDefinition: selectVariableDefinitions) {
            if (variableDefinition.isFunction() || variableDefinition.isExtension()){
                continue;
            }
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            SelectQuery<?> subSelect = context.selectQuery();
            switch (className){
                case "COMPOSITION":
                    if (variableDefinition.getPath().startsWith("content")) {
                        field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
                        break;
                    }

                case "EHR":
                    field = compositionAttributeQuery.makeField(comp_id, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
//                    selectFields.add(compositionAttributeQuery.selectField(comp_id, identifier, variableDefinition));
                    break;
                default:
                    field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
                    containsJsonDataBlock  = containsJsonDataBlock | jsonbEntryQuery.isJsonDataBlock();
                    if (jsonbEntryQuery.isJsonDataBlock()){
                        //add this field to the list of column to format as RAW JSON
                        jsonDataBlock.add(new JsonbBlockDef(jsonbEntryQuery.getJsonbItemPath(), field));
                    }
//                    selectFields.add(jsonbEntryQuery.selectField(comp_id, identifier, variableDefinition));
                    break;
            }
//            field = DSL.field(field);
            if (field == null) {
                throw new IllegalArgumentException("Field expression is not supported or invalid :" + variableDefinition);
            }
            selectQuery.addSelect(field);
            jsonbEntryQuery.inc();
        }
        //add the from bit
//        selectQuery.addFrom(CompositionQuerySnippets.content(context, comp_id));
//        selectQuery.addFrom(COMP_EXPAND);
//        if (compositionAttributeQuery.containsEhrStatus()){ //add a join clause to get other_details
//            selectQuery.addJoin(STATUS, JoinType.JOIN, STATUS.EHR_ID.eq(COMP_EXPAND.EHR_ID));
//        }

        if (containsJsonDataBlock){
            //add a template column for transformation
            selectQuery.addSelect(ENTRY.TEMPLATE_ID.as(I_RawJsonTransform.TEMPLATE_ID));
        }

        if (optimizationMode.equals(OptimizationMode.NONE)) {
            whereBinder.setInitialCondition(I_JoinBinder.compositionRecordTable.field("id", UUID.class).in(comp_id));
//        whereBinder.setInitialCondition(COMP_EXPAND.COMPOSITION_ID.eq(UUID.fromString(DSL.param("comp_id"))));
//        selectQuery.addConditions();
            selectQuery.addConditions(whereBinder.bind(comp_id));
        }

//        completeFromClause(selectQuery); //as some variables (other_status) may be defined only in where...
//        return context.select(selectFields);
        return selectQuery;
    }

    public Condition getWhereConditions(UUID comp_id){
        Condition condition =  whereBinder.bind(comp_id);
        return condition;
    }

    /**
     * simple bind on composition  attributes only
     * @param inSet
     * @return
     */
    public SelectQuery<?> bind(Result<?> inSet){
        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        for (I_VariableDefinition variableDefinition: selectVariableDefinitions) {
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            SelectQuery<?> subSelect = context.selectQuery();
            switch (className){
                case "COMPOSITION":
                    field = compositionAttributeQuery.makeField(null, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
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
//        completeFromClause(selectQuery);
//        selectQuery.addFrom(COMP_EXPAND);
        whereBinder.setInitialCondition(I_JoinBinder.compositionRecordTable.field("id", UUID.class).in(inSet));
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
        if (orderAttributes!= null && hasPathBasedField(orderAttributes))
            throw new IllegalArgumentException("Order by using a path based expression is not yet implemented");

        jsonbEntryQuery.reset();

        SelectQuery<?> selectQuery = context.selectQuery();

        for (I_VariableDefinition variableDefinition: selectVariableDefinitions) {

            if (variableDefinition.getPath() == null){
                //check if it is a composition in mapper
                if (mapper.getClassName(variableDefinition.getIdentifier()).equals("COMPOSITION")){
                    //substitute this variable definition by a function definition
                    isWholeComposition = true;
                    continue;
                }

            }

            if (variableDefinition.isFunction() || variableDefinition.isExtension()){
                continue;
            }
            String identifier = variableDefinition.getIdentifier();
            String className = mapper.getClassName(identifier);
            Field<?> field;
            switch (className){
                case "COMPOSITION":
                    field = compositionAttributeQuery.makeField(null, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
                    break;
                case "EHR":
                    field = compositionAttributeQuery.makeField(null, identifier, variableDefinition, true, I_QueryImpl.Clause.SELECT);
                    //check for implicit where associated with the FROM EHR clause
//                    FromDefinition.EhrPredicate predicate = (FromDefinition.EhrPredicate) mapper.getContainer(identifier);
//                    whereBinder.whereClause.add();
                    break;
                default:
                    throw new IllegalArgumentException("Bind with an IN set is only applicable to non JSON entries");
            }
            selectQuery.addSelect(field);
            jsonbEntryQuery.inc();
        }
//        //add the from bit
//        completeFromClause(selectQuery);

        //add required joins

//        selectQuery.addFrom(COMP_EXPAND);
        if (containQuery != null) {
//            whereBinder.setInitialCondition(COMP_EXPAND.COMPOSITION_ID.in(containQuery.asField()));
            whereBinder.setInitialCondition(I_JoinBinder.compositionRecordTable.field("id", UUID.class).in(containQuery.asField()));
        }
        selectQuery.addConditions(whereBinder.bind(null));

        if (limit != null)
            selectQuery.addLimit(limit);

        if (orderAttributes != null && !orderAttributes.isEmpty()) {
            for (OrderAttribute orderAttribute: orderAttributes){
                //assumes COMPOSITION fields
                Field<?> field = compositionAttributeQuery.makeField(null, null, orderAttribute.getVariableDefinition(), false, I_QueryImpl.Clause.ORDERBY);

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
                        field = compositionAttributeQuery.makeField(comp_id, identifier, variableDefinition, true, I_QueryImpl.Clause.ORDERBY);
//                    selectFields.add(compositionAttributeQuery.selectField(comp_id, identifier, variableDefinition));
                        break;
                    default:
                        field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, true, I_QueryImpl.Clause.ORDERBY);
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

    public boolean hasEhrIdExpression(){
        return compositionAttributeQuery.containsEhrId();
    }

    public String getEhrIdAlias(){
        return compositionAttributeQuery.getEhrIdAlias();
    }

    public boolean isCompositionIdFiltered(){
        return compositionAttributeQuery.isCompositionIdFiltered();
    }

    public boolean isEhrIdFiltered(){
        return compositionAttributeQuery.isEhrIdFiltered();
    }

    public boolean containsJQueryPath(){
        return jsonbEntryQuery.isContainsJqueryPath();
    }

    public OptimizationMode getOptimizationMode() {
        return optimizationMode;
    }

    public CompositionAttributeQuery getCompositionAttributeQuery() {
        return compositionAttributeQuery;
    }

    public List<JsonbBlockDef> getJsonDataBlock() {
        return jsonDataBlock;
    }

    public boolean isWholeComposition() {
        return isWholeComposition;
    }
}
