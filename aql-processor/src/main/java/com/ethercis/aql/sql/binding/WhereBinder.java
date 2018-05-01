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
import com.ethercis.aql.sql.queryImpl.value_field.ISODateTime;
import com.ethercis.ehr.encode.CompositionSerializer;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.*;

/**
 * Bind the abstract WHERE clause parameters into a SQL expression
 * Created by christian on 5/20/2016.
 */
public class WhereBinder {

    public static final String JSQUERY_EXPR_VALUE = "\"value\"";
    private JsonbEntryQuery jsonbEntryQuery;
    private CompositionAttributeQuery compositionAttributeQuery;
    protected final List whereClause;
    private IdentifierMapper mapper;
    private Condition initialCondition;
    private boolean isWholeComposition = false;
    String compositionName = null;
    private String sqlSetStatementRegexp = "(?i)(like|ilike|in|not in)"; //list of subquery and operators

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
        public void replaceLast(String previous, String newString) {
            int lastPos = stringBuffer.lastIndexOf(previous);
            if (lastPos >= 0) {
                stringBuffer.delete(lastPos, lastPos + previous.length());
                stringBuffer.insert(lastPos, newString);
            }
        }

        @Override
        public int lastIndexOf(String string) {
            return stringBuffer.lastIndexOf(string);
        }

        @Override
        public int indexOf(String string) {
            return stringBuffer.indexOf(string);
        }

        @Override
        public void replace(String previous, String newString) {
            int indexOf = stringBuffer.indexOf(previous);
            if (indexOf >= 0) {
                stringBuffer.delete(indexOf, indexOf + previous.length());
                stringBuffer.insert(indexOf, newString);
            }
        }

        @Override
        public String toString() {
            return stringBuffer.toString();
        }

        @Override
        public int length() {
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

        @Override
        public boolean startWith(String tag) {
            return stringBuffer.indexOf(tag) == 1; //starts with a quote!
        }
    }

    public WhereBinder(JsonbEntryQuery jsonbEntryQuery, CompositionAttributeQuery compositionAttributeQuery, List whereClause, IdentifierMapper mapper) {
        this.jsonbEntryQuery = jsonbEntryQuery;
        this.compositionAttributeQuery = compositionAttributeQuery;
        this.whereClause = whereClause;
        this.mapper = mapper;
        this.isWholeComposition = isWholeComposition;
    }

    private TaggedStringBuffer encodeWhereVariable(UUID comp_id, VariableDefinition variableDefinition, boolean forceSQL, String compositionName) {
        String identifier = variableDefinition.getIdentifier();
        String className = mapper.getClassName(identifier);
        if (className == null)
            throw new IllegalArgumentException("Could not bind identifier in WHERE clause:'" + identifier + "'");
        Field<?> field;
        if (forceSQL) {
            if (className.equals("COMPOSITION")) {
                field = compositionAttributeQuery.whereField(comp_id, identifier, variableDefinition);
            } else {
                field = jsonbEntryQuery.makeField(comp_id, identifier, variableDefinition, false, I_QueryImpl.Clause.SELECT);
            }
            if (field == null)
                return null;
            return new TaggedStringBuffer(field.toString(), TagField.SQLQUERY);

        } else {
            switch (className) {
                case "COMPOSITION":
                    if (variableDefinition.getPath().startsWith("content")) {
                        field = jsonbEntryQuery.whereField(comp_id, identifier, variableDefinition);
                        TaggedStringBuffer taggedStringBuffer = new TaggedStringBuffer(field.toString(), TagField.JSQUERY);
                        if (compositionName != null && taggedStringBuffer.startWith(CompositionSerializer.TAG_COMPOSITION)) {
                            //add the composition name into the composition predicate
                            taggedStringBuffer.replace("]", " and name/value='" + compositionName + "']");
                        }
                        return taggedStringBuffer;
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

    private TaggedStringBuffer buildWhereCondition(UUID comp_id, TaggedStringBuffer taggedBuffer, List item) {
        for (Object part : item) {
            if (part instanceof String)
                taggedBuffer.append((String) part);
            else if (part instanceof VariableDefinition) {
                //substitute the identifier
                TaggedStringBuffer taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) part, false, null);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
            } else if (part instanceof List) {
                TaggedStringBuffer taggedStringBuffer = buildWhereCondition(comp_id, taggedBuffer, (List) part);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
            }
        }
        return taggedBuffer;
    }

    private Condition wrapInCondition(Condition condition, TaggedStringBuffer taggedStringBuffer, Deque<Operator> operators) {
        //perform the condition query wrapping depending on the dialect jsquery or sql
        String wrapped;

        switch (taggedStringBuffer.getTagField()) {
            case JSQUERY:
                wrapped = JsonbEntryQuery.Jsquery_COMPOSITION_OPEN + taggedStringBuffer.toString() + JsonbEntryQuery.Jsquery_CLOSE;
                break;
            case SQLQUERY:
                wrapped = taggedStringBuffer.toString();
                break;

            default:
                throw new IllegalArgumentException("Uninitialized tag passed in query expression");
        }

        if (condition == null)
            condition = DSL.condition(wrapped);
        else {
            if (operators.isEmpty()) //assumes AND
                condition = condition.and(wrapped);
            else {
                Operator operator = operators.pop();
                switch (operator) {
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
                            switch (operator) {
                                case OR:
                                    condition = condition.orNot(condition1);
                                    break;
                                case AND:
                                    condition = condition.andNot(condition1);
                                    break;
                                default:
                                    condition = condition.andNot(condition1);
                            }
                        } else
                            condition = condition.andNot(condition1);
                        break;
                }
            }
        }

        return condition;
    }

    public Condition bind(UUID comp_id) {
        Deque<Operator> operators = new ArrayDeque<>();
        TaggedStringBuffer taggedBuffer = new TaggedStringBuffer();
        Condition condition = initialCondition;

//        List whereItems = new WhereResolver(whereClause).resolveDateCondition();
        List whereItems = whereClause;

        for (int cursor = 0; cursor < whereItems.size(); cursor++) {
            Object item = whereItems.get(cursor);
            if (item instanceof String) {
                switch ((String) item) {
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
                        ISODateTime isoDateTime = new ISODateTime(((String) item).replaceAll("'", ""));
                        if (isoDateTime.isValidDateTimeExpression()) {
                            Long timestamp = isoDateTime.toTimeStamp();
                            int lastValuePos = taggedBuffer.lastIndexOf(JSQUERY_EXPR_VALUE);
                            if (lastValuePos > 0) {
                                taggedBuffer.replaceLast(JSQUERY_EXPR_VALUE, "\"epoch_offset\"");
                            }
                            item = hackItem(taggedBuffer, timestamp.toString());
                            taggedBuffer.append((String) item);
                        } else {
                            item = hackItem(taggedBuffer, (String) item);
                            taggedBuffer.append((String) item);
                        }
                        break;

                }
            } else if (item instanceof Long) {
                item = hackItem(taggedBuffer, item.toString());
                taggedBuffer.append(item.toString());
            } else if (item instanceof VariableDefinition) {
                if (taggedBuffer.length() > 0) {
                    condition = wrapInCondition(condition, taggedBuffer, operators);
                    taggedBuffer = new TaggedStringBuffer();
                }
                //look ahead and check if followed by a sql operator
                TaggedStringBuffer taggedStringBuffer = null;
                if (isFollowedBySQLSetOperator(cursor))
                    taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) item, true, null);
                else {
                    if (((VariableDefinition) item).getPath() != null && isWholeComposition) {
                        //assume a composition
                        //look ahead for name/value condition (used to produce the composition root)
                        if (compositionName == null)
                            compositionName = compositionNameValue(((VariableDefinition) item).getIdentifier());

                        if (compositionName != null) {
                            taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) item, false, compositionName);
                        } else
                            throw new IllegalArgumentException("A composition name/value is required to resolve where statement when querying for a whole composition");
                    } else
                        taggedStringBuffer = encodeWhereVariable(comp_id, (VariableDefinition) item, false, null);
                }

                if (taggedStringBuffer != null) {
                    taggedBuffer.append(taggedStringBuffer.toString());
                    taggedBuffer.setTagField(taggedStringBuffer.getTagField());
                }
                //check for a composition name if applicable
//                if (((VariableDefinition) item).getAlias())
//                condition = wrapInCondition(condition, stringBuffer, operators);
            } else if (item instanceof List) {
                TaggedStringBuffer taggedStringBuffer = buildWhereCondition(comp_id, taggedBuffer, (List) item);
                taggedBuffer.append(taggedStringBuffer.toString());
                taggedBuffer.setTagField(taggedStringBuffer.getTagField());
                condition = wrapInCondition(condition, taggedBuffer, operators);
            }

        }

        if (taggedBuffer.length() != 0) {
            condition = wrapInCondition(condition, taggedBuffer, operators);
        }

        return condition;
    }


    //look ahead for a SQL operator
    private boolean isFollowedBySQLSetOperator(int cursor) {
        if (cursor < whereClause.size() - 1) {
            Object nextToken = whereClause.get(cursor + 1);
            if (nextToken instanceof String && ((String) nextToken).matches(sqlSetStatementRegexp))
                return true;
        }
        return false;
    }

    //look ahead for a SQL operator
    private String compositionNameValue(String symbol) {

        String token = null;
        int lcursor; //skip the current variable

        for (lcursor = 0; lcursor < whereClause.size() - 1; lcursor++) {
            if (whereClause.get(lcursor) instanceof VariableDefinition
                    && (((VariableDefinition) whereClause.get(lcursor)).getIdentifier().equals(symbol))
                    && (((VariableDefinition) whereClause.get(lcursor)).getPath().equals("name/value"))
                    ) {
                Object nextToken = whereClause.get(lcursor + 2);
                if (nextToken instanceof String) {
                    token = (String) nextToken;
                    break;
                }
            }
        }

        return token;
    }


    //from AQL grammar
    Set<String> operators = new HashSet<String>(Arrays.asList("=", "!=", ">", ">=", "<", "<=", "MATCHES", "EXISTS", "NOT", "(", ")", "{", "}"));

    //do some temporary hacking for unsupported features
    private Object hackItem(TaggedStringBuffer taggedBuffer, String item) {
        if (operators.contains(item.toUpperCase()))
            return item;
        if (taggedBuffer.toString().contains(I_JoinBinder.COMPOSITION_JOIN)) {
            if (item.contains("::"))
                return item.split("::")[0] + "'";
        }
        if (taggedBuffer.stringBuffer.indexOf("#>>") > 0) {
            return item;
        }
        if (taggedBuffer.stringBuffer.indexOf("#") > 0 && item.contains("'")) { //conventionally double quote for jsquery
            return item.replaceAll("'", "\"");
        }
        return item;
    }

    public void setInitialCondition(Condition initialCondition) {
        this.initialCondition = initialCondition;
    }

    public void setIsWholeComposition() {
        isWholeComposition = true;
    }
}
