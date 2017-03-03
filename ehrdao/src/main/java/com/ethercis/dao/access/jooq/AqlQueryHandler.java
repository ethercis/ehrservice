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

package com.ethercis.dao.access.jooq;

import com.ethercis.aql.compiler.QueryParser;
import com.ethercis.aql.sql.QueryProcessor;
import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Record;
import org.jooq.SQLDialect;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 6/9/2016.
 */
public class AqlQueryHandler extends DataAccess {

    Logger logger = LogManager.getLogger(AqlQueryHandler.class);


    public AqlQueryHandler(SQLDialect dialect, String DBURL, String login, String password, I_KnowledgeCache knowledgeManager) throws Exception {
        super(dialect, DBURL, login, password, knowledgeManager);
    }

    public AqlQueryHandler(I_DomainAccess domainAccess) {
        super(domainAccess);
    }

    public AqlQueryHandler(Map<String, Object> properties) throws Exception {
        super(properties);
    }

    public List<Record> process(String query) throws SQLException {
        QueryProcessor queryProcessor = new QueryProcessor(getContext(), this.getKnowledgeManager());
        QueryParser queryParser = new QueryParser(getContext(), query);

        queryParser.pass1();
        queryParser.pass2();

        List<Record> records = (List<Record>) queryProcessor.execute(queryParser, getServerNodeId(), false);
        return records;
    }

    public List<Object> explain(String query) throws SQLException {
        QueryProcessor queryProcessor = new QueryProcessor(getContext());
        QueryParser queryParser = new QueryParser(getContext(), query);

        queryParser.pass1();
        queryParser.pass2();

        List explain = (List) queryProcessor.execute(queryParser, getServerNodeId(), true);
        return explain;
    }

    public String dump(String query) {
        QueryParser queryParser = new QueryParser(getContext(), query);
        return queryParser.dump();
    }
}
