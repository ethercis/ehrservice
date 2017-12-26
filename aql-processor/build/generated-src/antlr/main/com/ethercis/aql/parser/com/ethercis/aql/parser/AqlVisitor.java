// Generated from com/ethercis/aql/parser/Aql.g4 by ANTLR 4.5.3
package com.ethercis.aql.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AqlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AqlParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(AqlParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#queryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpr(AqlParser.QueryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(AqlParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#topExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopExpr(AqlParser.TopExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(AqlParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#extension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtension(AqlParser.ExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#where}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere(AqlParser.WhereContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#orderBy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderBy(AqlParser.OrderByContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#limit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit(AqlParser.LimitContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#offset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset(AqlParser.OffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#orderBySeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderBySeq(AqlParser.OrderBySeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#orderByExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByExpr(AqlParser.OrderByExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#selectExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpr(AqlParser.SelectExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#stdExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStdExpression(AqlParser.StdExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom(AqlParser.FromContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#fromEHR}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromEHR(AqlParser.FromEHRContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#fromForeignData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromForeignData(AqlParser.FromForeignDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#fromExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromExpr(AqlParser.FromExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#containsExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainsExpression(AqlParser.ContainsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#containExpressionBool}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainExpressionBool(AqlParser.ContainExpressionBoolContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#contains}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContains(AqlParser.ContainsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#identifiedExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifiedExpr(AqlParser.IdentifiedExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#identifiedEquality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifiedEquality(AqlParser.IdentifiedEqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#identifiedOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifiedOperand(AqlParser.IdentifiedOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#identifiedPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifiedPath(AqlParser.IdentifiedPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(AqlParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#nodePredicateOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodePredicateOr(AqlParser.NodePredicateOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#nodePredicateAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodePredicateAnd(AqlParser.NodePredicateAndContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#nodePredicateComparable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodePredicateComparable(AqlParser.NodePredicateComparableContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#nodePredicateRegEx}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodePredicateRegEx(AqlParser.NodePredicateRegExContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#matchesOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchesOperand(AqlParser.MatchesOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#valueListItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueListItems(AqlParser.ValueListItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#versionpredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersionpredicate(AqlParser.VersionpredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#versionpredicateOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersionpredicateOptions(AqlParser.VersionpredicateOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#standardPredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandardPredicate(AqlParser.StandardPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#joinPredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinPredicate(AqlParser.JoinPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#predicateExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateExpr(AqlParser.PredicateExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#predicateAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateAnd(AqlParser.PredicateAndContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#predicateEquality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateEquality(AqlParser.PredicateEqualityContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#predicateOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateOperand(AqlParser.PredicateOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#operand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperand(AqlParser.OperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#objectPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectPath(AqlParser.ObjectPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#pathPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathPart(AqlParser.PathPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#classExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassExpr(AqlParser.ClassExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#simpleClassExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleClassExpr(AqlParser.SimpleClassExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#archetypedClassExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArchetypedClassExpr(AqlParser.ArchetypedClassExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#versionedClassExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersionedClassExpr(AqlParser.VersionedClassExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link AqlParser#versionClassExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersionClassExpr(AqlParser.VersionClassExprContext ctx);
}