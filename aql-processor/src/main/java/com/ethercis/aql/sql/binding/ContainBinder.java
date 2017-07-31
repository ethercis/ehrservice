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

import com.ethercis.aql.containment.Containment;
import com.ethercis.aql.containment.ContainmentSet;
import com.ethercis.aql.containment.Predicates;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Binds the SELECT, FROM, WHERE clauses to SQL expression
 * Binds nested sets containments to a SQL expression
 *
 * Build up the queries to map variables to path (e.g. COMPOSITION c1 [openEHR-EHR-COMPOSITION.referral.v1])
 * Created by christian on 4/19/2016.
 */
public class ContainBinder {
    Logger logger = LogManager.getLogger(ContainBinder.class);

    private List<ContainmentSet> nestedSets;
    private Predicates predicates;

    private final String EXCEPT_EXPRESSION = " EXCEPT ";
    private final String INTERSECT_EXPRESSION = " INTERSECT ";
    private final String UNION_EXPRESSION = " UNION ";

    private boolean useSimpleCompositionContainment = false; //true if check only if ehr contains any composition

    private String DEFAULT_CONTAIN_QUERY_TEMPLATE =
            "SELECT DISTINCT compo.ehr_id, comp_id AS composition_id, subltree(label, 0, 1) AS composition_archetype " +
            "FROM ehr.composition compo "+
            "INNER JOIN ("+
            " SELECT comp_id, containment.label "+
            "FROM ehr.containment ) AS contain " +
            "ON contain.comp_id = compo.id " +
            "WHERE contain.label ~";

    private String CONTAIN_SUBSELECT_TEMPLATE =
            "SELECT DISTINCT comp_id "+
            "FROM ehr.containment " +
            "WHERE label ~";

    private SelectField<?>[] selectFields = {
            CONTAINMENT.PATH
    } ;

    private Table<?> table = CONTAINMENT;

    private final static Character SINGLE_QUOTE = 0x27;
    private final static String RIGHT_WILDCARD = ".*";
    private final static String LEFT_WILDCARD = "*.";
    private final static String INNER_WILDCARD = ".*.";

    public ContainBinder(List<ContainmentSet> containmentSets) {
        this.nestedSets = containmentSets;
        //do some clean up (remove children pointing to empty parents...
//        int index = nestedSets.size() - 1;
        for (int index = nestedSets.size() - 1; index >= 0; index--){
            ContainmentSet containmentSet = nestedSets.get(index);
            if (containmentSet != null) {
                if (containmentSet.getParentSet() != null && containmentSet.getParentSet().isEmpty()) {
                    ContainmentSet adopter = containmentSet.getParentSet();

                    while (adopter != null && adopter.isEmpty()) {
                        nestedSets.remove(adopter);
                        adopter = adopter.getParentSet();
                    }
                    if (adopter.getContainmentList() != null) {
                        containmentSet.setParentSet(adopter);
                    }

                }
            }
        }
    }

    private Object lookAhead(ListOrderedSet<Object> containmentList, int cursor) {
        if (cursor + 1 >= containmentList.size())
            return null;
        return containmentList.get(cursor + 1);
    }

    private boolean isOperator(Object object) {
        return object instanceof ContainmentSet.OPERATOR;
    }

    private boolean isContainment(Object object) {
        return object instanceof Containment;
    }

    private boolean isLastItem(ListOrderedSet<Object> containmentList, int cursor) {
        return cursor + 1 >= containmentList.size();
    }

    private int resolveUnallocated(Deque<ContainmentSet.OPERATOR> operatorStack) {

        if(predicates.atomicPredicates.size()>0) {
            //check what's in stack
            if (operatorStack.size() > 0) {
                ContainmentSet.OPERATOR operator = operatorStack.pop();
                int cursor = predicates.atomicPredicates.size() - 1;
                switch (operator) {
                    case AND:
                        Predicates.Details details = predicates.atomicPredicates.get(cursor);
//                        Predicates.Details details = predicates.new Details(item, null);
                        predicates.intersectPredicates.add(details);
                        predicates.atomicPredicates.remove(cursor);
                        break;
                    case OR:
                        details = predicates.atomicPredicates.get(cursor);
//                        Predicates.Details details = predicates.new Details(item, null);
                        predicates.unionPredicates.add(details);
                        predicates.atomicPredicates.remove(cursor);
                        break;
                    case XOR:
//                        item = predicates.atomicPredicates.get(indexLast).expression;
                        details = predicates.atomicPredicates.get(cursor);
                        predicates.exceptPredicates.add(details);
                        predicates.atomicPredicates.remove(cursor);
                        break;

                }
            }
        }

        return predicates.atomicPredicates.size();
}


    public String bind(){
//        StringBuffer containedClause = new StringBuffer();

        List<Predicates> predicates = new ArrayList<>();
        for (ContainmentSet containmentSet: nestedSets){
            predicates.add(bind(containmentSet));
        }

        //factor containment
        factor4LTree(predicates);

        //assemble an SQL statement
        String query = inlineSqlQuery(predicates);

        return query;
    }

    public SelectQuery bind(DSLContext context){
//        StringBuffer containedClause = new StringBuffer();

        List<Predicates> predicates = new ArrayList<>();
        for (ContainmentSet containmentSet: nestedSets){
            predicates.add(bind(containmentSet));
        }

        //factor containment
        factor4LTree(predicates);

        //assemble an SQL statement
        SelectQuery query = jooqQuery(context, predicates);

        return query;
    }

    private String assembleSetOp(List<String> pendingAtomics, List<Predicates.Details> details,  String opExpression, StringBuffer query){
        for (Predicates.Details definition : details) {
            if (definition != null && !definition.isVoid()) {
                if (pendingAtomics.size() > 0) {
                    query.append(pendingAtomics.get(0));
                    pendingAtomics.remove(0);
                }
//                query.append(opExpression+ DEFAULT_CONTAIN_QUERY_TEMPLATE + SINGLE_QUOTE + definition.expression + SINGLE_QUOTE);
                query.append(opExpression+ CONTAIN_SUBSELECT_TEMPLATE + SINGLE_QUOTE + definition.expression + SINGLE_QUOTE);
            } else if (pendingAtomics.size() > 1){
                //it is an intersect with the pending atomics...
                query.append(pendingAtomics.get(0));
                query.append(opExpression+pendingAtomics.get(1));
                pendingAtomics.remove(0);
                pendingAtomics.remove(0);
            }
            pendingAtomics.add(query.toString());
            query = new StringBuffer();
        }

        return query.toString();
    }

    private SelectQuery singularSelect(DSLContext context, Predicates.Details definition){
        SelectQuery selectQuery = context.selectQuery();

        if (definition.expression.equals("COMPOSITION%")){
            //check existence of a a composition for an EHR
//            Condition condition = DSL.condition(DSL.exists(context.select(DSL.value(1, Integer.class)).from(COMPOSITION).where()))
            useSimpleCompositionContainment = true;
            return null;
        }
        else {
            Condition condition = DSL.condition("label ~" + SINGLE_QUOTE + definition.expression + SINGLE_QUOTE);
            selectQuery.addConditions(condition);
            selectQuery.addFrom(CONTAINMENT);
            selectQuery.addDistinctOn(CONTAINMENT.COMP_ID);
            selectQuery.addSelect(CONTAINMENT.COMP_ID);
            return selectQuery;
        }
    }

    private SelectQuery assembleSetOp(DSLContext context, List<SelectQuery> pendingAtomics, List<Predicates.Details> details,  String opExpression, SelectQuery query){
        for (Predicates.Details definition : details) {
            if (definition != null && !definition.isVoid()) {
                if (pendingAtomics.size() > 0) {
                    query.addFrom(pendingAtomics.get(0));
                    pendingAtomics.remove(0);
                }
                SelectQuery secondary = singularSelect(context, definition);
                switch (opExpression){
                    case INTERSECT_EXPRESSION:
                        query.intersect(secondary);
                        break;
                    case UNION_EXPRESSION:
                        query.unionAll(secondary);
                        break;
                    case EXCEPT_EXPRESSION:
                        query.exceptAll(secondary);
                        break;

                }
            } else if (pendingAtomics.size() > 1){
                //it is an intersect with the pending atomics...
                SelectQuery initial = pendingAtomics.get(0);
                SelectQuery secondary = pendingAtomics.get(1);
                switch (opExpression){
                    case INTERSECT_EXPRESSION:
                        initial.intersect(secondary);
                        break;
                    case UNION_EXPRESSION:
                        initial.unionAll(secondary);
                        break;
                    case EXCEPT_EXPRESSION:
                        initial.exceptAll(secondary);
                        break;

                }
                pendingAtomics.remove(0);
                pendingAtomics.remove(0);
            }
            pendingAtomics.add(query);
        }

        return query;
    }

    private String inlineSqlQuery(List<Predicates> predicatesList){
        StringBuffer query = new StringBuffer();
        List<String> pendingAtomics = new ArrayList<>();
        for (Predicates predicates: predicatesList) {
            if (predicates == null)
                continue;
            //start with the atomic predicate
            if (predicates.atomicPredicates.size() > 0) {
                for (Predicates.Details definition : predicates.atomicPredicates) {
//                    query.append(DEFAULT_CONTAIN_QUERY_TEMPLATE + SINGLE_QUOTE + definition.expression + SINGLE_QUOTE);
                    query.append(CONTAIN_SUBSELECT_TEMPLATE + SINGLE_QUOTE + definition.expression + SINGLE_QUOTE);
                    pendingAtomics.add(query.toString());
                    query = new StringBuffer();
                }
            }
            if (predicates.intersectPredicates.size() > 0) {
                assembleSetOp(pendingAtomics, predicates.intersectPredicates, INTERSECT_EXPRESSION, query);
                query = new StringBuffer();
            }
            if (predicates.unionPredicates.size() > 0) {
                assembleSetOp(pendingAtomics, predicates.unionPredicates, UNION_EXPRESSION, query);
                query = new StringBuffer();
            }
            if (predicates.exceptPredicates.size() > 0) {
                assembleSetOp(pendingAtomics, predicates.exceptPredicates, EXCEPT_EXPRESSION, query);
                query = new StringBuffer();
            }
        }
        if (!pendingAtomics.isEmpty()) {
            query.append(pendingAtomics.get(0));
            return query.toString();
        }
        return "";
    }

    private SelectQuery<?> jooqQuery(DSLContext context, List<Predicates> predicatesList){
        List<SelectQuery> pendingAtomics = new ArrayList<>();
        SelectQuery<?> selectQuery = null;
        for (Predicates predicates: predicatesList) {
            //start with the atomic predicate
            if (predicates == null)
                continue;
            if (predicates.atomicPredicates.size() > 0) {
                for (Predicates.Details definition : predicates.atomicPredicates) {
                    selectQuery = singularSelect(context, definition);
                    pendingAtomics.add(selectQuery);
                    selectQuery = context.selectQuery();
                }
            }
            if (predicates.intersectPredicates.size() > 0) {
                assembleSetOp(context, pendingAtomics, predicates.intersectPredicates, INTERSECT_EXPRESSION, selectQuery);
                selectQuery = context.selectQuery();
            }
            if (predicates.unionPredicates.size() > 0) {
                assembleSetOp(context, pendingAtomics, predicates.unionPredicates, UNION_EXPRESSION, selectQuery);
                selectQuery = context.selectQuery();
            }
            if (predicates.exceptPredicates.size() > 0) {
                assembleSetOp(context, pendingAtomics, predicates.exceptPredicates, EXCEPT_EXPRESSION, selectQuery);
                selectQuery = context.selectQuery();
            }
        }
        if (!pendingAtomics.isEmpty()) {
            if (pendingAtomics.get(0) != null)
                selectQuery.addFrom(pendingAtomics.get(0));
            else
                return null;
            return selectQuery;
        }
        return null;
    }


    private void factorEnclosing(List<Predicates.Details> predicatesDetails){
        for (Predicates.Details details: predicatesDetails){
            //traverse the enclosing containment to prefix the expression
            Containment enclosure = details.containedIn;
            while (enclosure != null){
                if (StringUtils.isNotEmpty(enclosure.getArchetypeId())) {
                    String labelized = labelize(enclosure.getArchetypeId());
                    details.expression = labelized + ((labelized.length() > 0) ? INNER_WILDCARD : LEFT_WILDCARD) + details.expression;
                }
                else
                    details.expression = LEFT_WILDCARD + details.expression;
                enclosure = enclosure.enclosingContainment;
            }
        }
    }

    /**
     * Factor containments as an ltree expression
     * @param predicates
     */
    private void factor4LTree(List<Predicates> predicates){

        for (Predicates predicatesDetail: predicates){
            if (predicatesDetail != null) {
                factorEnclosing(predicatesDetail.atomicPredicates);
                factorEnclosing(predicatesDetail.unionPredicates);
                factorEnclosing(predicatesDetail.intersectPredicates);
                factorEnclosing(predicatesDetail.exceptPredicates);
            }
        }

    }

    public static String labelize(String archetypeId){
        return archetypeId.replaceAll("\\-", "_").replaceAll("\\.", "_");
    }

    private StringBuffer buildBooleanExpression(StringBuffer containedPredicateLtree, String operator, List<Predicates.Details> predicatesList, ContainmentSet inSet, Containment enclosing){
        if (predicates.atomicPredicates.size() == 2) {
            containedPredicateLtree.append(predicates.atomicPredicates.get(0));
            containedPredicateLtree.append(operator);
            containedPredicateLtree.append(predicates.atomicPredicates.get(1));
            predicates.atomicPredicates.remove(1);
            predicates.atomicPredicates.remove(0);
            predicates.atomicPredicates.add(predicates.new Details(containedPredicateLtree.toString(),inSet, enclosing));
        }
        else if (predicates.atomicPredicates.size() == 1){
            containedPredicateLtree.append(predicates.atomicPredicates.get(0));
            containedPredicateLtree.append(operator);
            predicates.atomicPredicates.remove(0);
            predicates.atomicPredicates.add(predicates.new Details(containedPredicateLtree.toString(), inSet, enclosing));
        }
        else if (predicates.atomicPredicates.size() == 0){
//            containedPredicateLtree.append(operator);
            predicatesList.add(predicates.new Details(containedPredicateLtree.toString(), inSet, enclosing));
        }

        return containedPredicateLtree;
    }

    private Predicates bind(ContainmentSet containmentSet){
        if (containmentSet == null)
            return null;
        this.predicates = new Predicates(containmentSet);
        Deque<ContainmentSet.OPERATOR> operatorStack = new ArrayDeque<>();

        StringBuffer containedPredicateLtree = new StringBuffer();
        for (int i = 0; i < containmentSet.getContainmentList().size(); i++){
            Object item = containmentSet.getContainmentList().get(i);
            if (isContainment(item)){
                Containment containmentDefinition = ((Containment)item);
                String archetypeId = containmentDefinition.getArchetypeId();
                if (archetypeId.length() != 0) {
                    containedPredicateLtree.append(labelize(archetypeId));
                }
                else { //use the classname
                    containedPredicateLtree.append(containmentDefinition.getClassName()+"%");
                }
                if (!isLastItem(containmentSet.getContainmentList(), i) && !isOperator(lookAhead(containmentSet.getContainmentList(), i))){
                    containedPredicateLtree.append(INNER_WILDCARD);
                }
//                else if (isLastItem(containmentSet.getContainmentList(), i) && i == 0) {//single item
//                    containedPredicateLtree.append(RIGHT_WILDCARD);
//                }
            }
            else if (isOperator(item)){
                ContainmentSet.OPERATOR operator = (ContainmentSet.OPERATOR)item;
                switch (operator){
                    case OR:
//                        containedPredicateLtree.append("|");
//                        break;
                        if (containedPredicateLtree.length() == 0){ //promote the atomicPredicates to intersectPredicates, it is a AND on two nested groups
                            containedPredicateLtree = buildBooleanExpression(containedPredicateLtree, EXCEPT_EXPRESSION, predicates.unionPredicates, containmentSet.getParentSet(), containmentSet.getEnclosing());
                        }
                        else {
                            predicates.unionPredicates.add(predicates.new Details(containedPredicateLtree.toString(), containmentSet.getParentSet(), containmentSet.getEnclosing()));
                            operatorStack.push(operator);
                        }
                        containedPredicateLtree = new StringBuffer();
                        break;
                    case XOR:
                        if (containedPredicateLtree.length() == 0){ //promote the atomicPredicates to intersectPredicates, it is a AND on two nested groups
                            containedPredicateLtree = buildBooleanExpression(containedPredicateLtree, EXCEPT_EXPRESSION, predicates.exceptPredicates, containmentSet.getParentSet(), containmentSet.getEnclosing());
                        }
                        else {
                            predicates.exceptPredicates.add(predicates.new Details(containedPredicateLtree.toString(), containmentSet.getParentSet(), containmentSet.getEnclosing()));
                            operatorStack.push(operator);
                        }
                        containedPredicateLtree = new StringBuffer();
                        break;
                    case AND:
                        if (containedPredicateLtree.length() == 0){ //promote the atomicPredicates to intersectPredicates, it is a AND on two nested groups
                            containedPredicateLtree = buildBooleanExpression(containedPredicateLtree, INTERSECT_EXPRESSION, predicates.intersectPredicates, containmentSet.getParentSet(), containmentSet.getEnclosing());
                        }
                        else {
                            predicates.atomicPredicates.add(predicates.new Details(containedPredicateLtree.toString(), containmentSet.getParentSet(), containmentSet.getEnclosing()));
                            operatorStack.push(operator);
                        }

                        containedPredicateLtree = new StringBuffer(); //reset the string buffer
//                            logger.warn("Operator is not supported:"+operator);
                        break;
                }
            }
            else {
                //unhandled object
            }
            int size = resolveUnallocated(operatorStack);
//            while (size != 0)
//                size = resolveUnallocated(operatorStack);
        }
//            containedClause.append(containedPredicateLtree);
        if (containedPredicateLtree.length() > 0)
            predicates.atomicPredicates.add(predicates.new Details(containedPredicateLtree.toString(), containmentSet.getParentSet(), containmentSet.getEnclosing()));

        return predicates;

    }

    public static String buildLquery(Containment containment){
        int depth = 0;
        StringBuffer lquery = new StringBuffer();
        //traverse up the containments and assemble the lquery expression

        String archetypeId = containment.getArchetypeId();
        if (archetypeId.isEmpty()) //use the class name
            lquery.append(containment.getClassName()+"%");
        else
            lquery.append(ContainBinder.labelize(archetypeId));

        Containment parent = containment.getEnclosingContainment();

        while (parent != null){
            depth++;
            if (parent.getClassName().equals("COMPOSITION")) { //COMPOSITION is not part of the label
                lquery.insert(0, LEFT_WILDCARD);
                break;
            }
            archetypeId = parent.getArchetypeId();
            if (archetypeId == null || archetypeId.isEmpty()) //substitute by the class name
                archetypeId = parent.getClassName()+"%";
            else
                archetypeId = ContainBinder.labelize(archetypeId);
            lquery.insert(0, archetypeId+INNER_WILDCARD);
            parent = parent.getEnclosingContainment();
        }

//        if (depth == 0)
//            lquery.append(RIGHT_WILDCARD);
        return lquery.toString();
    }

    public boolean isUseSimpleCompositionContainment() {
        return useSimpleCompositionContainment;
    }
}
