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
import com.ethercis.aql.parser.AqlBaseVisitor;
import com.ethercis.aql.parser.AqlParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpret an AQL WHERE clause and set the result into a list of WHERE parts
 * Created by christian on 5/18/2016.
 */
public class WhereVisitor extends AqlBaseVisitor< List<Object>> {
    List whereExpression= new ArrayList<>();

//    @Override
//    public List visit(ParseTree tree){
//        return null;
////        return visitWhere(tree.getChild(0));
//    }

    @Override
    public List<Object> visitWhere(AqlParser.WhereContext ctx){
        visitIdentifiedExpr(ctx.identifiedExpr());
        return whereExpression;
    }

    @Override
    public List<Object> visitIdentifiedExpr(AqlParser.IdentifiedExprContext context){
//        List<Object> whereExpression = new ArrayList<>();
        for (ParseTree tree: context.children) {
            if (tree instanceof TerminalNodeImpl) {
                String what = tree.getText().trim();
                whereExpression.add(what);
            }
            else if (tree instanceof AqlParser.IdentifiedEqualityContext) {
                visitIdentifiedEquality((AqlParser.IdentifiedEqualityContext) tree);
            }
        }

        return whereExpression;
    }

//    @Override
//    public List<Object> visitIdentifiedExprAnd(AqlParser.IdentifiedExprAndContext context){
//        for (ParseTree tree: context.children) {
//            if (tree instanceof TerminalNodeImpl) {
//                String what = tree.getText().trim();
//                whereExpression.add(what);
//            }
//            else if (tree instanceof AqlParser.IdentifiedEqualityContext) {
//                visitIdentifiedEquality((AqlParser.IdentifiedEqualityContext) tree);
//            }
//            else if (tree instanceof AqlParser.IdentifiedExprContext) {
//                visitIdentifiedExpr((AqlParser.IdentifiedExprContext) tree);
//            }
//        }
//
//        return whereExpression;
//    }

    @Override
    public List<Object> visitIdentifiedEquality(AqlParser.IdentifiedEqualityContext context){
//        List<Object> whereExpression = new ArrayList<>();
        for (ParseTree tree: context.children){
            if (tree instanceof TerminalNodeImpl)
                whereExpression.add(((TerminalNodeImpl) tree).getSymbol().getText());
            else if (tree instanceof AqlParser.IdentifiedOperandContext){
                AqlParser.IdentifiedOperandContext operandContext = (AqlParser.IdentifiedOperandContext)tree;
                //translate/substitute operand
                for (ParseTree child: operandContext.children) {
                    if (child instanceof AqlParser.OperandContext){
                        whereExpression.add(child.getText());
                    }
                    else if (child instanceof AqlParser.IdentifiedPathContext) {
                        AqlParser.IdentifiedPathContext identifiedPathContext = (AqlParser.IdentifiedPathContext)child;
                        String path = identifiedPathContext.objectPath().getText();
                        String identifier = identifiedPathContext.IDENTIFIER().getText();
                        String alias = null;
                        VariableDefinition variable = new VariableDefinition(path, alias, identifier);
                        whereExpression.add(variable);
                    }
                }
            }
            else if (tree instanceof AqlParser.IdentifiedEqualityContext){
                visitIdentifiedEquality((AqlParser.IdentifiedEqualityContext)tree);
            }
        }

        return whereExpression;
    }

    public List getWhereExpression() {
        return whereExpression;
    }


}
