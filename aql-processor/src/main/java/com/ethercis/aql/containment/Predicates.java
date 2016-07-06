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

package com.ethercis.aql.containment;

import com.ethercis.aql.containment.Containment;
import com.ethercis.aql.containment.ContainmentSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Container defining list of predicates with their associated operator (if any).
 * Created by christian on 4/23/2016.
 */
public class Predicates {

    ContainmentSet containmentSet;

    public class Details {
        public String expression;
        public Containment containedIn;
        public ContainmentSet inSet;

        public Details(String expression, ContainmentSet inSet, Containment enclosing) {
            this.expression = expression;
            this.containedIn = enclosing;
            this.inSet = inSet;
        }

        public boolean isVoid(){
            return (expression == null || expression.length() == 0);
        }
    }

    public Predicates(ContainmentSet containmentSet) {
        this.containmentSet = containmentSet;
    }

    public List<Details> intersectPredicates = new ArrayList<>();
    public List<Details> exceptPredicates = new ArrayList<>();
    public List<Details> unionPredicates = new ArrayList<>();
    public List<Details> atomicPredicates = new ArrayList<>();

    public ContainmentSet getContainmentSet() {
        return containmentSet;
    }

    public List<Details> getIntersectPredicates() {
        return intersectPredicates;
    }

    public List<Details> getExceptPredicates() {
        return exceptPredicates;
    }

    public List<Details> getUnionPredicates() {
        return unionPredicates;
    }

    public List<Details> getAtomicPredicates() {
        return atomicPredicates;
    }
}
