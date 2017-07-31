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

import com.ethercis.aql.containment.IdentifierMapper;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.queryImpl.CompositionAttributeQuery;
import com.ethercis.aql.sql.queryImpl.I_QueryImpl;
import com.ethercis.aql.sql.queryImpl.JsonbEntryQuery;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.*;

/**
 * Bind the abstract WHERE clause parameters into a SQL expression
 * Created by christian on 5/20/2016.
 */
public class WhereBinder {

    private JsonbEntryQuery jsonbEntryQuery;
    private CompositionAttributeQuery compositionAttributeQuery;
    protected List whereClause;
    private IdentifierMapper mapper;
    private Condition initialCondition;
    private String sqlStatementRegexp = "(?i)(like|ilike|in|not in)"; //list of subquery and operators

    private enum Operator {OR, XOR, AND, NOT, EXISTS}

    public enum TagField {JSQUERY, SQLQUERY}

    public class TaggedStringBuffer implements I_TaggedStringBuffer {

        private StringBuffer stringBuffer;
        private TagField tagField;

        public TaggedStringBuffer(TagField tagField) {
            this.stringBuffer = new StringBuffer();
            this.tagField = tagField;
        }

        public TaggedStringBuffer() {
            this.stringBuffer = new StringBuffer();
//            this.tagField = tagField;
        }

        public TaggedStringBuffer(String string, TagField tagField) {
            this.stringBuffer = new StringBuffer();
            stringBuffer.append(string);
            this.tagField = tagField;
        }

        @Override
        public StringBuffer append(String string) {
            return stringBuffer.append(string);
        }

        @Override
        public String toString(){
            return stringBuffer.toString();
        }

        @Override
        public int length(){
            return stringBuffer.length();
        }

        @Override
        public TagField getTagField() {
            return tagField;
        }

        @Override
        public void setTagField(TagField tagField) {
            this.tagField = tagField;
        }
    }

    public WhereBinder(JsonbEntryQuery jsonbEntryQuery, CompositionAttributeQuery compositionAttributeQuery, List whereClause, IdentifierMapper mapper) {
        this.jsonbEntryQuery = jsonbEntryQuery;
        this.compositionAttributeQuery = compositionAttributeQuery;
        this.whereClause = whereClause;
        this.mapper = mapper;
    }

    private TaggedStringBuffer encodeWhereVariable(UUID comp_id, VariableDefinition variableDefinition, boolean forceSQL){
        String identifier = variableDefinition.getIdentifier();
        String className = mapper.getClassName(identifier);
        if (className == null)
            throw new IllegalArgumentException("Could not bind identifier in WHERE clause:'"+identifier+"'");
        Field<?> field;
        if (forceSQL){
            field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, false, I_QueryImpl.Clause.SELECT);
            if (field == null)
                return null;
            return new TaggedStringBuffer(field.toString(), TagField.SQLQUERY);
        }
        else {
            switch (className) {
                case "COMPOSITION":
                    if (variableDefinition.getPath().startsWith("content")) {
                        field = jsonbEntryQuery.whereField(comp_id, identifier, variableDefinition);
                        return new TaggedStringBuffer(field.toString(), TagField.JSQUERY);
                    }
                case "EHR":
                    field = compositionAttributeQuery.whereField(comp_id, identifier, variableDefinition);
                    if (field == null)
                        return null;
                    return new TaggedStringBuffer(field.toString(), TagField.SQLQUERY);

                default:
                    field = jsonbEntryQuery.whereField(comp_id, identifier, variableDefinition);
                    return new TaggedStringBuffer(field.toString(), TagField.JSQUERY);
            }
        }
    }

    private TaggedStringBuffer buildWhereCondition(UUID comp_id, TaggedStringBuffer taggedBuffer, List item){
        for (Object part: item) {
            if (part instanceof String)
                taggedBuffer.append((String)part);
            else if (part instanceof VariableDefinition) {
                //substitute the identifier
                TaggedStringBuffer taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) part, false);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
            }
            else if (part instanceof List){
                TaggedStringBuffer taggedStringBuffer = buildWhereCondition(comp_id, taggedBuffer, (List) part);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
            }
        }
        return taggedBuffer;
    }

    private Condition wrapInCondition(Condition condition, TaggedStringBuffer taggedStringBuffer, Deque<Operator> operators){
        //perform the condition query wrapping depending on the dialect jsquery or sql
        String wrapped;

        switch (taggedStringBuffer.getTagField()){
            case JSQUERY:
                wrapped = JsonbEntryQuery.Jsquery_COMPOSITION_OPEN + taggedStringBuffer.toString() + JsonbEntryQuery.Jsquery_CLOSE;
                break;
            case SQLQUERY:
                wrapped = taggedStringBuffer.toString();
                break;

            default:
                throw new IllegalArgumentException("Unitialized tag pass in query expression");
        }

        if (condition == null)
            condition = DSL.condition(wrapped);
        else {
            if (operators.isEmpty()) //assumes AND
                condition = condition.and(wrapped);
            else {
                Operator operator = operators.pop();
                switch (operator){
                    case OR:
                        condition = condition.or(wrapped);
                        break;
                    case XOR:
                        throw new IllegalArgumentException("XOR is not supported yet...");

                    case AND:
                        condition = condition.and(wrapped);
                        break;
                    case NOT:
                        Condition condition1 = DSL.condition(wrapped);
                        if (operators.size() > 0) {
                            operator = operators.pop();
                            switch (operator){
                                case OR:
                                    condition = condition.orNot(condition1);
                                    break;
                                case AND:
                                    condition = condition.andNot(condition1);
                                    break;
                                default:
                                    condition = condition.andNot(condition1);
                            }
                        }
                        else
                            condition = condition.andNot(condition1);
                        break;
                }
            }
        }

        return condition;
    }

    public Condition bind(UUID comp_id){
        Deque<Operator> operators = new ArrayDeque<>();
        TaggedStringBuffer taggedBuffer = new TaggedStringBuffer();
        Condition condition = initialCondition;

        for (int cursor = 0; cursor < whereClause.size(); cursor++){
            Object item = whereClause.get(cursor);
            if (item instanceof String) {
                switch ((String)item){
                    case "OR":
                    case "or":
                        operators.push(Operator.OR);
                        break;

                    case "XOR":
                    case "xor":
                        operators.push(Operator.XOR);
                        break;

                    case "AND":
                    case "and":
                        operators.push(Operator.AND);
                        break;

                    case "NOT":
                    case "not":
                        operators.push(Operator.NOT);
                        break;

                    default:
                        item = hackItem(taggedBuffer, (String) item);
                        taggedBuffer.append((String) item);
                        break;

                }
            }
            else if (item instanceof VariableDefinition){
                if (taggedBuffer.length() > 0) {
                    condition = wrapInCondition(condition, taggedBuffer, operators);
                    taggedBuffer = new TaggedStringBuffer();
                }
                //look ahead and check if followed by a sql operator
                TaggedStringBuffer taggedStringBuffer;
                if (followSQLOperator(cursor))
                    taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) item, true);
                else
                    taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) item, false);

                if (taggedStringBuffer != null) {
                    taggedBuffer.append(taggedStringBuffer.toString());
                    taggedBuffer.setTagField(taggedStringBuffer.getTagField());
                }
//                condition = wrapInCondition(condition, stringBuffer, operators);
            }
            else if (item instanceof List){
                TaggedStringBuffer taggedStringBuffer = buildWhereCondition(comp_id, taggedBuffer, (List) item);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
                condition = wrapInCondition(condition, taggedBuffer, operators);
            }

        }

        if (taggedBuffer.length() != 0){
            condition = wrapInCondition(condition, taggedBuffer, operators);
        }

        return condition;
    }


    //look ahead for a SQL operator
    private boolean followSQLOperator(int cursor){
        if (cursor < whereClause.size() - 1){
            Object nextToken = whereClause.get(cursor + 1);
            if (nextToken instanceof String && ((String)nextToken).matches(sqlStatementRegexp))
                return true;
        }
        return false;
    }


    //from AQL grammar
    Set<String> operators = new HashSet<String>(Arrays.asList("=", "!=", ">", ">=", "<",  "<=", "MATCHES", "EXISTS", "NOT", "(", ")", "{", "}"));

    //do some temporary hacking for unsupported features
    private Object hackItem(TaggedStringBuffer taggedBuffer, String item) {
        if (operators.contains(item.toUpperCase()))
            return item;
        if (taggedBuffer.toString().contains("composition_id")){
            if (item.contains("::"))
                return item.split("::")[0]+"'";
        }
        if (taggedBuffer.stringBuffer.indexOf("#>>") > 0){
            return item;
        }
        if (taggedBuffer.stringBuffer.indexOf("#") > 0 && item.contains("'")){ //conventionally double quote for jsquery
            return item.replaceAll("'", "\"");
        }
        return item;
    }

    public void setInitialCondition(Condition initialCondition) {
        this.initialCondition = initialCondition;
    }
}
