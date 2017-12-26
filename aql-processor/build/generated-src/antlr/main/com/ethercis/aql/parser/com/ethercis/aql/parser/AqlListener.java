// Generated from com/ethercis/aql/parser/Aql.g4 by ANTLR 4.5.3
package com.ethercis.aql.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AqlParser}.
 */
public interface AqlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AqlParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(AqlParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(AqlParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#queryExpr}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpr(AqlParser.QueryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#queryExpr}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpr(AqlParser.QueryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(AqlParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(AqlParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#topExpr}.
	 * @param ctx the parse tree
	 */
	void enterTopExpr(AqlParser.TopExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#topExpr}.
	 * @param ctx the parse tree
	 */
	void exitTopExpr(AqlParser.TopExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(AqlParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(AqlParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#extension}.
	 * @param ctx the parse tree
	 */
	void enterExtension(AqlParser.ExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#extension}.
	 * @param ctx the parse tree
	 */
	void exitExtension(AqlParser.ExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#where}.
	 * @param ctx the parse tree
	 */
	void enterWhere(AqlParser.WhereContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#where}.
	 * @param ctx the parse tree
	 */
	void exitWhere(AqlParser.WhereContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#orderBy}.
	 * @param ctx the parse tree
	 */
	void enterOrderBy(AqlParser.OrderByContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#orderBy}.
	 * @param ctx the parse tree
	 */
	void exitOrderBy(AqlParser.OrderByContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#limit}.
	 * @param ctx the parse tree
	 */
	void enterLimit(AqlParser.LimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#limit}.
	 * @param ctx the parse tree
	 */
	void exitLimit(AqlParser.LimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#offset}.
	 * @param ctx the parse tree
	 */
	void enterOffset(AqlParser.OffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#offset}.
	 * @param ctx the parse tree
	 */
	void exitOffset(AqlParser.OffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#orderBySeq}.
	 * @param ctx the parse tree
	 */
	void enterOrderBySeq(AqlParser.OrderBySeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#orderBySeq}.
	 * @param ctx the parse tree
	 */
	void exitOrderBySeq(AqlParser.OrderBySeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#orderByExpr}.
	 * @param ctx the parse tree
	 */
	void enterOrderByExpr(AqlParser.OrderByExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#orderByExpr}.
	 * @param ctx the parse tree
	 */
	void exitOrderByExpr(AqlParser.OrderByExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#selectExpr}.
	 * @param ctx the parse tree
	 */
	void enterSelectExpr(AqlParser.SelectExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#selectExpr}.
	 * @param ctx the parse tree
	 */
	void exitSelectExpr(AqlParser.SelectExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#stdExpression}.
	 * @param ctx the parse tree
	 */
	void enterStdExpression(AqlParser.StdExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#stdExpression}.
	 * @param ctx the parse tree
	 */
	void exitStdExpression(AqlParser.StdExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#from}.
	 * @param ctx the parse tree
	 */
	void enterFrom(AqlParser.FromContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#from}.
	 * @param ctx the parse tree
	 */
	void exitFrom(AqlParser.FromContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#fromEHR}.
	 * @param ctx the parse tree
	 */
	void enterFromEHR(AqlParser.FromEHRContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#fromEHR}.
	 * @param ctx the parse tree
	 */
	void exitFromEHR(AqlParser.FromEHRContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#fromForeignData}.
	 * @param ctx the parse tree
	 */
	void enterFromForeignData(AqlParser.FromForeignDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#fromForeignData}.
	 * @param ctx the parse tree
	 */
	void exitFromForeignData(AqlParser.FromForeignDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#fromExpr}.
	 * @param ctx the parse tree
	 */
	void enterFromExpr(AqlParser.FromExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#fromExpr}.
	 * @param ctx the parse tree
	 */
	void exitFromExpr(AqlParser.FromExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#containsExpression}.
	 * @param ctx the parse tree
	 */
	void enterContainsExpression(AqlParser.ContainsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#containsExpression}.
	 * @param ctx the parse tree
	 */
	void exitContainsExpression(AqlParser.ContainsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#containExpressionBool}.
	 * @param ctx the parse tree
	 */
	void enterContainExpressionBool(AqlParser.ContainExpressionBoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#containExpressionBool}.
	 * @param ctx the parse tree
	 */
	void exitContainExpressionBool(AqlParser.ContainExpressionBoolContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#contains}.
	 * @param ctx the parse tree
	 */
	void enterContains(AqlParser.ContainsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#contains}.
	 * @param ctx the parse tree
	 */
	void exitContains(AqlParser.ContainsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#identifiedExpr}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiedExpr(AqlParser.IdentifiedExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#identifiedExpr}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiedExpr(AqlParser.IdentifiedExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#identifiedEquality}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiedEquality(AqlParser.IdentifiedEqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#identifiedEquality}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiedEquality(AqlParser.IdentifiedEqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#identifiedOperand}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiedOperand(AqlParser.IdentifiedOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#identifiedOperand}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiedOperand(AqlParser.IdentifiedOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#identifiedPath}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiedPath(AqlParser.IdentifiedPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#identifiedPath}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiedPath(AqlParser.IdentifiedPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(AqlParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(AqlParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#nodePredicateOr}.
	 * @param ctx the parse tree
	 */
	void enterNodePredicateOr(AqlParser.NodePredicateOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#nodePredicateOr}.
	 * @param ctx the parse tree
	 */
	void exitNodePredicateOr(AqlParser.NodePredicateOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#nodePredicateAnd}.
	 * @param ctx the parse tree
	 */
	void enterNodePredicateAnd(AqlParser.NodePredicateAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#nodePredicateAnd}.
	 * @param ctx the parse tree
	 */
	void exitNodePredicateAnd(AqlParser.NodePredicateAndContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#nodePredicateComparable}.
	 * @param ctx the parse tree
	 */
	void enterNodePredicateComparable(AqlParser.NodePredicateComparableContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#nodePredicateComparable}.
	 * @param ctx the parse tree
	 */
	void exitNodePredicateComparable(AqlParser.NodePredicateComparableContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#nodePredicateRegEx}.
	 * @param ctx the parse tree
	 */
	void enterNodePredicateRegEx(AqlParser.NodePredicateRegExContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#nodePredicateRegEx}.
	 * @param ctx the parse tree
	 */
	void exitNodePredicateRegEx(AqlParser.NodePredicateRegExContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#matchesOperand}.
	 * @param ctx the parse tree
	 */
	void enterMatchesOperand(AqlParser.MatchesOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#matchesOperand}.
	 * @param ctx the parse tree
	 */
	void exitMatchesOperand(AqlParser.MatchesOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#valueListItems}.
	 * @param ctx the parse tree
	 */
	void enterValueListItems(AqlParser.ValueListItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#valueListItems}.
	 * @param ctx the parse tree
	 */
	void exitValueListItems(AqlParser.ValueListItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#versionpredicate}.
	 * @param ctx the parse tree
	 */
	void enterVersionpredicate(AqlParser.VersionpredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#versionpredicate}.
	 * @param ctx the parse tree
	 */
	void exitVersionpredicate(AqlParser.VersionpredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#versionpredicateOptions}.
	 * @param ctx the parse tree
	 */
	void enterVersionpredicateOptions(AqlParser.VersionpredicateOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#versionpredicateOptions}.
	 * @param ctx the parse tree
	 */
	void exitVersionpredicateOptions(AqlParser.VersionpredicateOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#standardPredicate}.
	 * @param ctx the parse tree
	 */
	void enterStandardPredicate(AqlParser.StandardPredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#standardPredicate}.
	 * @param ctx the parse tree
	 */
	void exitStandardPredicate(AqlParser.StandardPredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#joinPredicate}.
	 * @param ctx the parse tree
	 */
	void enterJoinPredicate(AqlParser.JoinPredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#joinPredicate}.
	 * @param ctx the parse tree
	 */
	void exitJoinPredicate(AqlParser.JoinPredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#predicateExpr}.
	 * @param ctx the parse tree
	 */
	void enterPredicateExpr(AqlParser.PredicateExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#predicateExpr}.
	 * @param ctx the parse tree
	 */
	void exitPredicateExpr(AqlParser.PredicateExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#predicateAnd}.
	 * @param ctx the parse tree
	 */
	void enterPredicateAnd(AqlParser.PredicateAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#predicateAnd}.
	 * @param ctx the parse tree
	 */
	void exitPredicateAnd(AqlParser.PredicateAndContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#predicateEquality}.
	 * @param ctx the parse tree
	 */
	void enterPredicateEquality(AqlParser.PredicateEqualityContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#predicateEquality}.
	 * @param ctx the parse tree
	 */
	void exitPredicateEquality(AqlParser.PredicateEqualityContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#predicateOperand}.
	 * @param ctx the parse tree
	 */
	void enterPredicateOperand(AqlParser.PredicateOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#predicateOperand}.
	 * @param ctx the parse tree
	 */
	void exitPredicateOperand(AqlParser.PredicateOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#operand}.
	 * @param ctx the parse tree
	 */
	void enterOperand(AqlParser.OperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#operand}.
	 * @param ctx the parse tree
	 */
	void exitOperand(AqlParser.OperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#objectPath}.
	 * @param ctx the parse tree
	 */
	void enterObjectPath(AqlParser.ObjectPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#objectPath}.
	 * @param ctx the parse tree
	 */
	void exitObjectPath(AqlParser.ObjectPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#pathPart}.
	 * @param ctx the parse tree
	 */
	void enterPathPart(AqlParser.PathPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#pathPart}.
	 * @param ctx the parse tree
	 */
	void exitPathPart(AqlParser.PathPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#classExpr}.
	 * @param ctx the parse tree
	 */
	void enterClassExpr(AqlParser.ClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#classExpr}.
	 * @param ctx the parse tree
	 */
	void exitClassExpr(AqlParser.ClassExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#simpleClassExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleClassExpr(AqlParser.SimpleClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#simpleClassExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleClassExpr(AqlParser.SimpleClassExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#archetypedClassExpr}.
	 * @param ctx the parse tree
	 */
	void enterArchetypedClassExpr(AqlParser.ArchetypedClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#archetypedClassExpr}.
	 * @param ctx the parse tree
	 */
	void exitArchetypedClassExpr(AqlParser.ArchetypedClassExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#versionedClassExpr}.
	 * @param ctx the parse tree
	 */
	void enterVersionedClassExpr(AqlParser.VersionedClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#versionedClassExpr}.
	 * @param ctx the parse tree
	 */
	void exitVersionedClassExpr(AqlParser.VersionedClassExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link AqlParser#versionClassExpr}.
	 * @param ctx the parse tree
	 */
	void enterVersionClassExpr(AqlParser.VersionClassExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link AqlParser#versionClassExpr}.
	 * @param ctx the parse tree
	 */
	void exitVersionClassExpr(AqlParser.VersionClassExprContext ctx);
}