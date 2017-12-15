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

package com.ethercis.aql.compiler;

import com.ethercis.aql.containment.ContainWalker;
import com.ethercis.aql.containment.Containment;
import com.ethercis.aql.containment.ContainmentSet;
import com.ethercis.aql.containment.IdentifierMapper;
import com.ethercis.aql.definition.FromEhrDefinition;
import com.ethercis.aql.definition.FunctionDefinition;
import com.ethercis.aql.definition.I_VariableDefinition;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.parser.AqlLexer;
import com.ethercis.aql.parser.AqlParser;
import com.ethercis.aql.sql.binding.ContainBinder;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Wrap the walkers for pass1 and pass2 as well as invoke the WHERE visitor<p>
 * The purpose of this class is to assemble all query parts from the AQL expression. The parts
 * are then passed to specific binders to translate and/or perform the query to
 * a backend.
 * Created by christian on 4/1/2016.
 */
public class QueryParser {

    private Logger log = LogManager.getLogger(QueryParser.class);

    //this is the list of nested sets from the CONTAINS expressions
    private List<ContainmentSet> nestedSets;

    private ANTLRInputStream antlrInputStream;
    private Lexer aqlLexer;
    private AqlParser aqlParser;
    private ParseTree parseTree;
    private String containClause;
    private SelectQuery<?> containQuery;
    private List<Object> whereClause;
    private IdentifierMapper identifierMapper;
    ParseTreeWalker walker = new ParseTreeWalker();
    List<I_VariableDefinition> variables;
    private boolean requiresContainResolution = false; //true if expression has CONTAINS
    private TopAttributes topAttributes;
    private List<OrderAttribute> orderAttributes;
    private FunctionDefinition functionDefinitions;
    private  boolean useSimpleCompositionContainment = false;

    private DSLContext context;
    private Integer limitAttribute;
    private Integer offsetAttribute;

    public QueryParser(DSLContext context, String query){
        this.antlrInputStream = new ANTLRInputStream(query);
        this.aqlLexer = new AqlLexer(antlrInputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(aqlLexer);
        this.aqlParser = new AqlParser(commonTokenStream);

        //define our own error listener (default one just display a message on System.err
        aqlLexer.removeErrorListeners();
        aqlLexer.addErrorListener(AqlErrorHandler.INSTANCE);
        aqlParser.removeErrorListeners();
        aqlParser.addErrorListener(AqlErrorHandler.INSTANCE);

        this.parseTree = aqlParser.query(); //begin parsing at query rule
        this.context = context;
    }

    public void dump(boolean debug){
        log.debug(parseTree.toStringTree(aqlParser));
    }

    public String dump(){
        return parseTree.toStringTree(aqlParser);
    }

    public void pass1(){
        QueryCompilerPass1 queryCompilerPass1 = new QueryCompilerPass1();
        walker.walk(queryCompilerPass1, parseTree);

        this.identifierMapper = (IdentifierMapper) queryCompilerPass1.annotations.get(parseTree);
//        log.debug(identifierMapper.dump());
        nestedSets = queryCompilerPass1.getClosedSetList();

        //check if path resolution is required (e.g. contains inner archetypes in composition(s)

        for (ContainmentSet containmentSet: nestedSets){
            if (containmentSet != null) {
                if (containmentSet.getContainmentList().size() > 1) {
                    requiresContainResolution = true;
                    break;
                } else if (containmentSet.getContainmentList().size() == 1 && !((Containment) containmentSet.getContainmentList().get(0)).getClassName().equals("COMPOSITION")) {
                    requiresContainResolution = true;
                    break;
                }
            }
        }

        //bind the nested sets to SQL (it should be an configuration btw)
        ContainBinder containBinder = new ContainBinder(nestedSets);
        this.containClause = containBinder.bind();

        containQuery = containBinder.bind(context);
        useSimpleCompositionContainment = containBinder.isUseSimpleCompositionContainment();
    }

    public void pass2(){
        QueryCompilerPass2 queryCompilerPass2 = new QueryCompilerPass2();
        walker.walk(queryCompilerPass2, parseTree);
        variables = queryCompilerPass2.variables();
        whereClause = visitWhere();
        //append any EHR predicates into the where clause list

        if (identifierMapper.hasEhrContainer())
            appendEhrPredicate(identifierMapper.getEhrContainer());

        topAttributes = queryCompilerPass2.getTopAttributes();
        orderAttributes = queryCompilerPass2.getOrderAttributes();
        limitAttribute = queryCompilerPass2.getLimitAttribute();
        offsetAttribute = queryCompilerPass2.getOffsetAttribute();
    }

    private List visitWhere(){
        WhereVisitor whereVisitor = new WhereVisitor();
        whereVisitor.visit(parseTree);
        return whereVisitor.getWhereExpression();
    }

    private void appendEhrPredicate(FromEhrDefinition.EhrPredicate ehrPredicate){
        if (ehrPredicate == null)
            return;

        //append field, operator and value to the where clause
        whereClause.add(new VariableDefinition(ehrPredicate.getField(), null, ehrPredicate.getIdentifier(), false));
        whereClause.add(ehrPredicate.getOperator());
        whereClause.add(ehrPredicate.getValue());
    }


    //for tests purpose
    public List<ContainmentSet> getNestedSets() {
        return nestedSets;
    }

    public String getContainClause() {
        return containClause;
    }

    public SelectQuery<?> getContainQuery() {
        return containQuery;
    }

    //    public Result<?> getInSet(){
//        return containQuery.fetch();
//    }

    public Result<?> getInSet() {
        return context
                .selectDistinct(CONTAINMENT.COMP_ID, ENTRY.TEMPLATE_ID)
                .from(CONTAINMENT)
                .join(ENTRY)
                .on(CONTAINMENT.COMP_ID.eq(ENTRY.COMPOSITION_ID))
                .where(CONTAINMENT.COMP_ID.in(DSL.field(containClause)))
                .fetch();
    }

    public Result<?> getInSet(Integer limit){
        if (limit != null){
            containQuery.addLimit(limit);
        }

        return containQuery.fetch();
    }

    public SelectQuery<?> getInSetExpression(){
        return containQuery;
    }

    public boolean hasContainsExpression() {
        return requiresContainResolution;
    }

    public List getWhereClause() {
        return whereClause;
    }

    public IdentifierMapper getIdentifierMapper(){
        return identifierMapper;
    }

    public List<I_VariableDefinition> getVariables() {
        return variables;
    }

    public TopAttributes getTopAttributes() {
        return topAttributes;
    }

    public List<OrderAttribute> getOrderAttributes() {
        return orderAttributes;
    }

    public Integer getLimitAttribute() {
        return limitAttribute;
    }

    public Integer getOffsetAttribute() {
        return offsetAttribute;
    }


    public boolean isUseSimpleCompositionContainment() {
        return useSimpleCompositionContainment;
    }
}
