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

import com.ethercis.aql.compiler.OrderAttribute;
import com.ethercis.aql.compiler.QueryParser;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christian on 9/23/2016.
 */
public class OrderByBinder {

    private QueryParser queryParser;

    public OrderByBinder(QueryParser queryParser){
        this.queryParser = queryParser;
    }

    public boolean hasOrderBy(){
        if (queryParser.getOrderAttributes() == null)
            return false;
        return !queryParser.getOrderAttributes().isEmpty();
    }

    public List<SortField<?>> getOrderByFields() {
        if (queryParser.getOrderAttributes().isEmpty())
            return null;

        List<SortField<?>> orderFields = new ArrayList<>();

        for (OrderAttribute orderAttribute : queryParser.getOrderAttributes()) {
            String identifier = orderAttribute.getVariableDefinition().getIdentifier();
            SortField<Object> field = null;
            if (identifier != null) {
                if (orderAttribute.getDirection() != null) {
                    switch (orderAttribute.getDirection()) {
                        case ASC:
                            field = DSL.field(identifier).asc();
                            break;
                        case DESC:
                            field = DSL.field(identifier).desc() ;
                            break;
                    }
                } else //default to ASCENDING
                    field = DSL.field(identifier).asc();

            } else
                throw new IllegalArgumentException("Could not resolve field in ORDER BY clause:" + orderAttribute.getVariableDefinition());
            orderFields.add(field);
        }
        return orderFields;
    }
}
