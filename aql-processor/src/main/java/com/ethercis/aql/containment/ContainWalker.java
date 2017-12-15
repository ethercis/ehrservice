/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Expose public methods to check the existence of symbols, get the class in containment
 * Created by christian on 11/20/2017.
 */
public class ContainWalker {

    private List<ContainmentSet> nestedSets;

    public ContainWalker(List<ContainmentSet> nestedSets) {
        this.nestedSets = nestedSets;
    }

    public boolean exist(String symbol){
        for (ContainmentSet containmentSet: nestedSets){
            if (checkFor(containmentSet, symbol))
                return true;
        }

        return false;
    }

    public String classOf(String symbol){
        for (ContainmentSet containmentSet: nestedSets){
            String clazz = classFor(containmentSet, symbol);
            if (clazz != null)
                return clazz;
        }

        return null;
    }


    //recursive traversal
    private boolean checkFor(ContainmentSet containmentSet, String symbol){
        if (containmentSet == null)
            return false;

        for (int i = 0; i < containmentSet.getContainmentList().size(); i++){
            Object item = containmentSet.getContainmentList().get(i);
            if (item instanceof Containment){
                Containment containmentDefinition = ((Containment)item);
                if (containmentDefinition.getSymbol().equals(symbol))
                    return true;
            }
        }
        return false;
    }

    private String classFor(ContainmentSet containmentSet, String symbol){
        if (containmentSet == null)
            return null;

        for (int i = 0; i < containmentSet.getContainmentList().size(); i++){
            Object item = containmentSet.getContainmentList().get(i);
            if (item instanceof Containment){
                Containment containmentDefinition = ((Containment)item);
                if (containmentDefinition.getSymbol().equals(symbol)) {
                    return containmentDefinition.getClassName();
                }
            }
        }
        return null;
    }
}
