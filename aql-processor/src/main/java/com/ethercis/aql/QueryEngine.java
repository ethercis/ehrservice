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

package com.ethercis.aql;

import com.ethercis.aql.compiler.QueryParser;
import com.ethercis.aql.sql.QueryProcessor;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by christian on 2/2/2017.
 */
public class QueryEngine {

    DSLContext context;
    Logger logger = LogManager.getLogger(QueryEngine.class);
    String serverNodeId;
    I_KnowledgeCache knowledgeCache = null;

    public QueryEngine(DSLContext context, String serverNodeId) {
        this.context = context;
        this.serverNodeId = serverNodeId;
    }

    public QueryEngine(DSLContext context, String serverNodeId, I_KnowledgeCache knowledgeCache) {
        this.context = context;
        this.serverNodeId = serverNodeId;
        this.knowledgeCache = knowledgeCache;
    }

    public List<Record> perform(String query, boolean explain) throws SQLException {

//        QueryProcessor queryProcessor = new QueryProcessor(context);
        QueryProcessor queryProcessor = new QueryProcessor(context, knowledgeCache);
        QueryParser queryParser = new QueryParser(context, query);

        queryParser.pass1();
        queryParser.pass2();

        List<Record> records = (List<Record>)queryProcessor.execute(queryParser, serverNodeId, explain);

        return records;
    }

    public List<Record> perform(String query) throws SQLException {

        return perform(query, false);
    }
}
