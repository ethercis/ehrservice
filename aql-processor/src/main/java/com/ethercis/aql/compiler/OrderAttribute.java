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
import org.jooq.Field;
import org.jooq.SelectQuery;

import java.util.List;
import java.util.UUID;

/**
 * Representation of the ORDER BY clause in an AQL query
 * Created by christian on 5/26/2016.
 */

public class OrderAttribute {
    public enum OrderDirection {ASC, DESC}

    OrderDirection direction;
    VariableDefinition variableDefinition;

    public OrderAttribute(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    public void setDirection(OrderDirection direction) {
        this.direction = direction;
    }

    public OrderDirection getDirection() {
        return direction;
    }

    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

}
