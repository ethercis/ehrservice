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

import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.parser.AqlBaseListener;
import com.ethercis.aql.parser.AqlParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * AQL compilation pass 2<p>
 * This pass uses the results of pass 1 to:
 * <ul>
 *     <li>resolve AQL paths from symbols, example: c/items[at0002]/items[at0001]/value/value/magnitude
 *     <li>create the list of variables used in SELECT
 *     <li>create the list of ORDER BY expression parts
 *     <li>set the TOP clause if specified
 * </ul>
 * Created by christian on 4/1/2016.
 */
public class QueryCompilerPass2 extends AqlBaseListener {

    Logger logger = Logger.getLogger(QueryCompilerPass2.class);

    Deque<VariableDefinition> variableStack = new ArrayDeque<>();
    Deque<OrderAttribute> orderAttributes = null;

    TopAttributes topAttributes = null;

    @Override
    public void exitObjectPath(AqlParser.ObjectPathContext objectPathContext){
        logger.debug("Object Path->");
    }

    @Override
    public void exitIdentifiedPath(AqlParser.IdentifiedPathContext identifiedPathContext){
        logger.debug("Identified Path->");
    }

    @Override
    public void exitIdentifiedPathSeq(AqlParser.IdentifiedPathSeqContext identifiedPathSeqContext){
        logger.debug("IdentifiedPathSeq->");
        AqlParser.IdentifiedPathContext identifiedPathContext = identifiedPathSeqContext.identifiedPath();
        String path = identifiedPathContext.objectPath().getText();
        String identifier = identifiedPathContext.IDENTIFIER().getText();
        String alias = null;
        if (identifiedPathSeqContext.IDENTIFIER() != null)
            alias = identifiedPathSeqContext.IDENTIFIER().getText();
        VariableDefinition variableDefinition = new VariableDefinition(path, alias, identifier);
        variableStack.push(variableDefinition);
    }

    @Override
    public void exitIdentifiedExpr(AqlParser.IdentifiedExprContext identifiedExprContext){

    }

    @Override
    public void exitIdentifiedEquality(AqlParser.IdentifiedEqualityContext identifiedEqualityContext){

    }

    @Override
    public void exitTopExpr(AqlParser.TopExprContext context){
        Integer window = null;
        TopAttributes.TopDirection direction = null;
        if (context.TOP() != null){
            window = new Integer(context.INTEGER().getText());
            if (context.BACKWARD() != null)
                direction = TopAttributes.TopDirection.BACKWARD;
            else if (context.FORWARD() != null)
                direction = TopAttributes.TopDirection.FORWARD;
        }
        topAttributes = new TopAttributes(window, direction);
    }

    @Override
    public void exitOrderBy(AqlParser.OrderByContext context){

    }

    @Override
    public void exitOrderBySeq(AqlParser.OrderBySeqContext context){
        if (orderAttributes == null)
            orderAttributes = new ArrayDeque<>();

        for (ParseTree tree: context.children){
            if (tree instanceof AqlParser.OrderByExprContext){
                AqlParser.OrderByExprContext context1 = (AqlParser.OrderByExprContext)tree;
                AqlParser.IdentifiedPathContext identifiedPathContext = context1.identifiedPath();
                String path;
                if (identifiedPathContext.objectPath() != null)
                    path = identifiedPathContext.objectPath().getText();
                else
                    path = "$ALIAS$";
                String identifier = identifiedPathContext.IDENTIFIER().getText();
                OrderAttribute orderAttribute = new OrderAttribute(new VariableDefinition(path, null, identifier));
                if (context1.ASC() != null || context1.ASCENDING() != null)
                    orderAttribute.setDirection(OrderAttribute.OrderDirection.ASC);
                else if (context1.DESC() != null || context1.DESCENDING() != null)
                    orderAttribute.setDirection(OrderAttribute.OrderDirection.DESC);
                orderAttributes.push(orderAttribute);
            }
        }
    }

    public List<VariableDefinition> variables(){
        return new ArrayList<>(variableStack);
    }

    public TopAttributes getTopAttributes() {
        return topAttributes;
    }

    public List<OrderAttribute> getOrderAttributes(){
        if (orderAttributes == null)
            return null;
        return new ArrayList<>(orderAttributes);
    }
}
