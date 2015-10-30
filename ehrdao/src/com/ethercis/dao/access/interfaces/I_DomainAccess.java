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
package com.ethercis.dao.access.interfaces;

import com.ethercis.dao.access.support.ServiceDataAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.util.Map;

/**
 * Helper to hold SQL context and knowledge cache reference
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_DomainAccess {
    /**
     * get jOOQ SQL dialect
     * @return SQLDialect
     * @see org.jooq.SQLDialect
     */
    SQLDialect getDialect();

    /**
     * get the JDBC connection to the DB
     * @return Connection
     * @see java.sql.Connection
     */
    Connection getConnection();

    /**
     * get the jOOQ DSL context to perform DB queries
     * @return DSLContext
     * @see org.jooq.DSLContext
     */
    DSLContext getContext();

    /**
     * get the interface to the current knowledge cache
     * @return I_KnowledgeCache
     * @see com.ethercis.ehr.knowledge.I_KnowledgeCache
     */
    I_KnowledgeCache getKnowledgeManager();


    static I_DomainAccess getInstance(Map<String, Object> properties) throws Exception {
        return new ServiceDataAccess(properties);
    }

    public static String KEY_DIALECT = "dialect";
    public static String KEY_URL = "url";
    public static String KEY_LOGIN = "login";
    public static String KEY_PASSWORD = "password";
    public static String KEY_KNOWLEDGE = "knowledge";


    String getServerNodeId();
}
