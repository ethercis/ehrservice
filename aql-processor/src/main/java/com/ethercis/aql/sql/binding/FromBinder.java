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

import com.ethercis.aql.compiler.QueryParser;
import com.ethercis.aql.sql.queryImpl.CompositionAttributeQuery;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * NOT USED IN THE CURRENT VERSION
 * Created by christian on 5/2/2016.
 */
public class FromBinder {

    public void addFromClause(SelectQuery<?> selectQuery, CompositionAttributeQuery compositionAttributeQuery, QueryParser queryParser){

        if (compositionAttributeQuery.containsEhrId()){
            selectQuery.addFrom(EHR);
        }
        if (queryParser.isUseSimpleCompositionContainment()){
            selectQuery.addJoin(I_JoinBinder.compositionRecordTable,
                    DSL.field(I_JoinBinder.compositionRecordTable.field(COMPOSITION.EHR_ID.getName(), UUID.class))
                        .eq(EHR.ID));
        }

    }
}
