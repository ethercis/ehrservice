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

package com.ethercis.aql.sql.queryImpl;

import com.ethercis.aql.definition.I_VariableDefinition;
import com.ethercis.aql.definition.VariableDefinition;
import org.jooq.Field;
import org.jooq.SelectQuery;

import java.util.UUID;

/**
 * Created by christian on 5/6/2016.
 */
public interface I_QueryImpl {

    boolean isJsonDataBlock();

    boolean isEhrIdFiltered();

    boolean isCompositionIdFiltered();

    boolean isContainsJqueryPath();

    boolean isUseEntry();

    String getJsonbItemPath();

    enum Clause {SELECT, WHERE, ORDERBY, FROM}

    Field<?> makeField(UUID compositionId, String identifier, I_VariableDefinition variableDefinition, boolean withAlias, Clause clause);

    Field<?> whereField(UUID compositionId, String identifier, I_VariableDefinition variableDefinition);
}
