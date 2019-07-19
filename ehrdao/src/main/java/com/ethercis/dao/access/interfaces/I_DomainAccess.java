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

import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.dao.access.support.ServiceDataAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.opt.query.I_IntrospectCache;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.util.Map;

/**
 * Helper to hold SQL context and knowledge cache reference
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_DomainAccess {

    String PG_POOL = "PG_POOL";
    String DBCP2_POOL = "DBCP2_POOL";
    String KEY_MAX_IDLE = "max_idle";
    String KEY_MAX_ACTIVE = "max_active";
    String KEY_REMOVE_ABANDONNED = "remove_abandonned";
    String KEY_REMOVE_ABANDONNED_TIMEOUT = "remove_abandonned_timeout";
    String KEY_TEST_ON_BORROW = "test_on_borrow";
    String KEY_LOG_ABANDONNED = "log_abandonned";
    String KEY_AUTO_RECONNECT = "auto_reconnect";

    String KEY_DIALECT = "dialect";
    String KEY_URL = "url";
    String KEY_LOGIN = "login";
    String KEY_PASSWORD = "password";
    String KEY_KNOWLEDGE = "knowledge";
    String KEY_HOST = "host";
    String KEY_PORT = "port";
    String KEY_MAX_CONNECTION = "max_connection";
    String KEY_INITIAL_CONNECTIONS = "initial_connections";
    String KEY_CONNECTION_MODE = "connection_mode";
    String KEY_DATABASE = "database";
    String KEY_WAIT_MS = "wait_milliseconds";
    String KEY_SCHEMA = "schema";
    String KEY_SET_POOL_PREPARED_STATEMENTS = "set_pool_prepared_statement";
    String KEY_SET_MAX_PREPARED_STATEMENTS = "set_max_prepared_statements";
    String KEY_INTROSPECT_CACHE = "introspect";

    //added to deal with postgresql jdbc 42.2.2 (https://github.com/pgjdbc/pgjdbc)
    //as of v1.3 jdbc parameters can be passed prefixed with 'pgjdbc.' f.e. 'pgjdbc.ssl="true"'
    String KEY_PGJDBC_PREFIX = "pgjdbc";
    String[] pgjdbc_booleanArg = {"ssl", "allowEncodingChanges", "logUnclosedConnections", "tcpKeepAlive", "readOnly", "disableColumnSanitiser", "loadBalanceHosts", "reWriteBatchedInserts"};
    String[] pgjdbc_integerArg = {"sendBufferSize", "recvBufferSize", "prepareThreshold", "preparedStatementCacheQueries",
            "preparedStatementCacheSizeMiB", "defaultRowFetchSize", "loginTimeout", "connectTimeout",
            "socketTimeout", "hostRecheckSeconds"};


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
     * have JOOQ release the DB connection
     */
    void releaseConnection(Connection connection);

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

    I_IntrospectCache getIntrospectCache();


    static I_DomainAccess getInstance(Map<String, Object> properties) throws Exception {
        return new ServiceDataAccess(properties);
    }

    static I_DomainAccess getInstance(DataAccess dataAccess) throws Exception {
        return new ServiceDataAccess(dataAccess);
    }


    I_IntrospectCache initializeIntrospectCache() throws Exception;

    void setKnowledgeManager(I_KnowledgeCache knowledgeCache);

    String getServerNodeId();

    DataAccess getDataAccess();
}
