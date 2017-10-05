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

import com.ethercis.aql.containment.IdentifierMapper;
import com.ethercis.aql.definition.I_VariableDefinition;
import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.aql.sql.PathResolver;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Created by christian on 5/6/2016.
 */
public abstract class ObjectQuery {

    protected DSLContext context;
    protected List<I_VariableDefinition> variableDefinitions;
    protected PathResolver pathResolver;
    protected static int serial = 0; //used to alias fields for now.

    protected ObjectQuery(DSLContext context, PathResolver pathResolver, List<I_VariableDefinition> definitions){
        this.context = context;
        this.variableDefinitions = definitions;
        this.pathResolver = pathResolver;

    }

    public void reset(){
        serial = 0;
    }

    public void inc(){
        serial++;
    }

    public static int getSerial(){
        return serial;
    }
}
