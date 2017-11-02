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

import com.ethercis.aql.containment.*;
import com.ethercis.aql.definition.FromEhrDefinition;
import com.ethercis.aql.definition.FromForeignDataDefinition;
import com.ethercis.aql.definition.I_FromEntityDefinition;
import com.ethercis.aql.parser.AqlBaseListener;
import com.ethercis.aql.parser.AqlParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * AQL compilation pass 1<br>
 * This pass consists in evaluating the CONTAINS clauses and generate an internal representation of the
 * contain expressions and operators. The internal representation is then translated into a SQL equivalent
 * from the containment index.
 * Created by christian on 4/1/2016.
 */
public class QueryCompilerPass1 extends AqlBaseListener {
    Logger logger = LogManager.getLogger(QueryCompilerPass1.class);
    private static int serial = 0;
    private static int fieldId = 0;

    ParseTreeProperty<Object> annotations = new ParseTreeProperty<>();

    IdentifierMapper identifierMapper = new IdentifierMapper();
    Containment currentContainment = null;
    AstContainment astContainment = null;

    Deque<ContainmentSet> containmentStack = new ArrayDeque<>();
    ContainmentSet closedContainmentSet = null;

    ContainmentSet rootContainmentSet;

    List<ContainmentSet> closedSetList = new ArrayList<>();

    private int setLevel = 0;
    Boolean inContainedSet = false; //reset each time we enter a group
    int containLevel = 0;

    @Override
    public void exitFromEHR(AqlParser.FromEHRContext context) {
        FromEhrDefinition fromEhrDefinition = new FromEhrDefinition();
        if (context.IDENTIFIER() != null) {
            visitFromExpressionChildren(fromEhrDefinition, context.children);
            String identifier = context.IDENTIFIER().getText();
            fromEhrDefinition.setIdentifier(identifier);
            if (!fromEhrDefinition.getEhrPredicates().isEmpty()) {
                fromEhrDefinition.getEhrPredicates().get(0).setIdentifier(identifier);
                identifierMapper.add(fromEhrDefinition.getEhrPredicates().get(0));
            } else
                identifierMapper.add(new FromEhrDefinition.EhrPredicate(identifier));
        }
        if (context.EHR() != null) {
            fromEhrDefinition.setIsEHR(true);
        }

        logger.debug("FromEHR");
    }

    private void visitFromExpressionChildren(I_FromEntityDefinition fromEntityDefinition, List<ParseTree> children){
        if (children.size() == 0)
            return;

        for (ParseTree node: children){

            if (node.getText().equals("[") || node.getText().equals("]"))
                continue;

            if (node instanceof AqlParser.StandardPredicateContext){
                AqlParser.StandardPredicateContext equalityContext = (AqlParser.StandardPredicateContext)node;
                if (equalityContext.getChildCount() > 0) {
                    if (equalityContext.getChildCount() == 3){
                        AqlParser.PredicateExprContext predicateExprContext = (AqlParser.PredicateExprContext)equalityContext.getChild(1);
                        AqlParser.PredicateAndContext predicateAndContext = (AqlParser.PredicateAndContext)predicateExprContext.getChild(0);
                        AqlParser.PredicateEqualityContext predicateEqualityContext = (AqlParser.PredicateEqualityContext)predicateAndContext.getChild(0);
                        if (predicateEqualityContext.getChildCount() != 3)
                            throw new IllegalArgumentException("Could not handle predicateEqualityContext:"+predicateAndContext.getText());
                        fromEntityDefinition.add(predicateEqualityContext.getChild(0).getText(), predicateEqualityContext.getChild(2).getText(), predicateEqualityContext.getChild(1).getText());
                    }
                }
            }
        }
    }

    private void visitJoinExpressionChildren(I_FromEntityDefinition fromEntityDefinition, List<ParseTree> children){
        if (children.size() == 0)
            return;

        for (ParseTree node: children){

            if (node.getText().equals("[") || node.getText().equals("]"))
                continue;

            if (node instanceof AqlParser.JoinPredicateContext){
                AqlParser.JoinPredicateContext joinContext = (AqlParser.JoinPredicateContext)node;
                if (joinContext.getChildCount() > 0) {
                    if (joinContext.getChildCount() == 4){
                        AqlParser.PredicateEqualityContext predicateEqualityContext = (AqlParser.PredicateEqualityContext)joinContext.getChild(2);
                        if (predicateEqualityContext.getChildCount() != 3)
                            throw new IllegalArgumentException("Could not handle predicateEqualityContext:"+predicateEqualityContext.getText());
                        fromEntityDefinition.add(predicateEqualityContext.getChild(0).getText(), predicateEqualityContext.getChild(2).getText(), predicateEqualityContext.getChild(1).getText());
                    }
                }
            }
        }
    }


    @Override
    public void exitFromForeignData(AqlParser.FromForeignDataContext context){
        FromForeignDataDefinition fromForeignDataDefinition = new FromForeignDataDefinition(context.getChild(0).getText());
        if (context.IDENTIFIER() != null){
            visitJoinExpressionChildren(fromForeignDataDefinition, context.children);
            String identifier = context.IDENTIFIER().getText();
            fromForeignDataDefinition.setIdentifier(identifier);
            if (!fromForeignDataDefinition.getFDPredicates().isEmpty()) {
                fromForeignDataDefinition.getFDPredicates().get(0).setIdentifier(identifier);
                identifierMapper.add(fromForeignDataDefinition.getFDPredicates().get(0));
            }
            else
                identifierMapper.add(new FromForeignDataDefinition.NodePredicate(identifier));
        }
//        if (context.IDENTIFIER() != null) {
//            fromDefinition.setIsEHR(true);
//        }

        logger.debug("exitFromForeignData");
    }

    @Override
    public void exitStandardPredicate(AqlParser.StandardPredicateContext standardPredicateContext) {
        logger.debug("StandardPredicate");
    }

    @Override
    public void enterFromEHR(AqlParser.FromEHRContext context) {
        logger.debug("ENTER FromEHR");
    }

    @Override
    public void exitPredicateEquality(AqlParser.PredicateEqualityContext predicateEqualityContext) {
        if (predicateEqualityContext.predicateOperand().size() > 0) {
            List<AqlParser.PredicateOperandContext> predicateOperandContexts = predicateEqualityContext.predicateOperand();
            switch (predicateOperandContexts.size()) {
                case 1:
                    break;
                case 2:
//                    fromDefinition.add((predicateOperandContexts.get(0)).getText(), (predicateOperandContexts.get(1)).getText(), "=");
                    break;
                case 3: //this one is expected...
                    break;
            }
        }
    }

    @Override
    public void exitArchetypedClassExpr(AqlParser.ArchetypedClassExprContext archetypedClassExprContext) {
        //CHC, 160808: make classname case insensitive
        String className = archetypedClassExprContext.IDENTIFIER(0).getSymbol().getText().toUpperCase();

        String symbol = archetypedClassExprContext.IDENTIFIER(1).getSymbol().getText();

        String archetypeId = archetypedClassExprContext.ARCHETYPEID().getText();

        Containment containment = new Containment(className, symbol, archetypeId);
        identifierMapper.add(containment);

        //requires a CONTAINS expression!
        if (inContainedSet && containLevel > 0) {
            containment.setEnclosingContainment(currentContainment);
            inContainedSet = false;
        } else
            containment.setEnclosingContainment(currentContainment.enclosingContainment);

        astContainment = new AtomicContainment(containment, astContainment);

        if (astContainment.getEnclosing() != null) {
            AstContainment astEnclosing = astContainment.getEnclosing();
            if (astEnclosing instanceof AtomicContainment)
                ((AtomicContainment) astEnclosing).setChild(astContainment);
        }

        currentContainment = addContainment(containment);

        //debug stuff
        logger.debug(containment.toString());
        if (currentContainment.getEnclosingContainment() != null) {
            logger.debug("<-----" + currentContainment.getEnclosingContainment().toString());
        }

    }

    @Override
    public void enterContainExpressionBool(AqlParser.ContainExpressionBoolContext containExpressionBoolContext) {
        if (containExpressionBoolContext.OPEN_PAR() != null) {
            setLevel++;
            //add a new prefixcontainment on stack
            ContainmentSet containmentSet = new ContainmentSet(serial++, currentContainment);

            containmentStack.push(containmentSet);
            logger.debug("---- START GROUP:" + setLevel + " contained in:" + currentContainment);
            inContainedSet = true; //reset the containment sequencing
        }
    }

    @Override
    public void exitContainExpressionBool(AqlParser.ContainExpressionBoolContext containExpressionBoolContext) {

        if (containExpressionBoolContext.CLOSE_PAR() != null) {
            logger.debug("---- CLOSING GROUP:" + setLevel);
            setLevel--;
            if (containmentStack.size() > 0) {
                closedContainmentSet = containmentStack.pop();
                if (!containmentStack.isEmpty())
                    closedContainmentSet.setParentSet(containmentStack.getFirst());
                else
                    closedContainmentSet.setParentSet(rootContainmentSet);
//                if (!closedContainmentSet.isEmpty())
                closedSetList.add(closedContainmentSet);
            } else if (rootContainmentSet != null) {
                rootContainmentSet.add(astContainment.getContainment());
                closedSetList.add(rootContainmentSet);
            } else
                throw new IllegalArgumentException("Invalid condition in boolean expression parsing");
        }
    }

    @Override
    public void exitContainsExpression(AqlParser.ContainsExpressionContext containsExpressionContext) {
        if (currentContainment != null)
            currentContainment = currentContainment.getEnclosingContainment();

        if (containsExpressionContext.AND() != null || containsExpressionContext.OR() != null || containsExpressionContext.XOR() != null) {
            String operator = containsExpressionContext.AND() != null ? "AND" :
                    containsExpressionContext.OR() != null ? "OR" :
                            containsExpressionContext.XOR() != null ? "XOR" : "*undef*";

            logger.debug(operator);
            if (containmentStack.size() > 0) {
                //get the current containment set
                ContainmentSet current = containmentStack.getFirst();
                if (current.size() > 0)
                    current.setOperator(operator);
                else {
                    logger.debug("Orphan operator:" + operator);
                    if (rootContainmentSet == null)
                        rootContainmentSet = new ContainmentSet(serial++, null);
                    rootContainmentSet.add(operator);
                }
            } else if (rootContainmentSet != null) {
                rootContainmentSet.setOperator(operator);
            }

        }

    }

    @Override
    public void enterContainsExpression(AqlParser.ContainsExpressionContext containsExpressionContext) {

        if (currentContainment == null) {
            currentContainment = new Containment(null);

        }
    }


    @Override
    public void exitSimpleClassExpr(AqlParser.SimpleClassExprContext simpleClassExprContext) {
        logger.debug("from exitSimpleClassExpr: ENTER");
        if (!simpleClassExprContext.IDENTIFIER().isEmpty()) {
            //CHC, 160808: make classname case insensitive
            String className = simpleClassExprContext.IDENTIFIER(0).getSymbol().getText().toUpperCase();
            String symbol;
            if (simpleClassExprContext.IDENTIFIER().size() > 0 && simpleClassExprContext.IDENTIFIER(1) != null)
                symbol = simpleClassExprContext.IDENTIFIER(1).getSymbol().getText();
            else
                symbol = className + "_" + (++fieldId);

            Containment containment = new Containment(className, symbol, "");
            if (/* inContainedSet && */ containLevel > 0)
                containment.setEnclosingContainment(currentContainment);

            currentContainment = addContainment(containment);
            identifierMapper.add(currentContainment);

            //debug stuff
            logger.debug(containment.toString());
            if (currentContainment.getEnclosingContainment() != null) {
                logger.debug("<-----" + currentContainment.getEnclosingContainment().toString());
            }
            containLevel++;
        }
    }

    @Override
    public void exitContains(AqlParser.ContainsContext containsContext) {
        if (containsContext.CONTAINS() != null) {
            logger.debug(containsContext.CONTAINS().getSymbol().getText());
        }
    }

    @Override
    public void enterContains(AqlParser.ContainsContext containsContext) {
        if (containsContext.CONTAINS() != null) {
            logger.debug("ENTER:" + containsContext.CONTAINS().getSymbol().getText());
            if (!inContainedSet) {
                inContainedSet = true;
                containLevel = 0;
            }
        }
    }

    @Override
    public void exitQuery(AqlParser.QueryContext queryContext) {
        annotations.put(queryContext, identifierMapper);
        //append the root containment in the set list
        closedSetList.add(rootContainmentSet);
    }


    public List<ContainmentSet> getClosedSetList() {
        return closedSetList;
    }


    private Containment addContainment(Containment containment) {
        if (containmentStack.size() > 0) {
            //add this containment in the containment list at the top of the stack
            ContainmentSet containmentSet = containmentStack.getFirst();
            containmentSet.add(containment);
        } else { //we are back to the root
            if (rootContainmentSet != null)
                rootContainmentSet.add(containment);
            else { //no root yet
                rootContainmentSet = new ContainmentSet(serial++, null);
                rootContainmentSet.add(containment);
            }
        }

        if (rootContainmentSet == null && setLevel == 0) {
            rootContainmentSet = new ContainmentSet(serial++, null);
            rootContainmentSet.add(containment);
        }

        containLevel++;

        return containment;
    }

}
