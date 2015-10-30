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
package com.ethercis.dao.access.support;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Christian Chevalley on 4/21/2015.
 */
public abstract class DataAccess implements I_DomainAccess {

    protected Connection connection;
    protected DSLContext context;
    protected I_KnowledgeCache knowledgeManager;

    protected static String serverNodeId = System.getProperty("server.node.name");

    public DataAccess(SQLDialect dialect, String DBURL, String login, String password, I_KnowledgeCache knowledgeManager) throws Exception {
        //setup connection
        setParameters(dialect, DBURL, login, password);
        this.knowledgeManager = knowledgeManager;
    }

    /**
     * setup a connection handler from properties
     * the following key/value pairs are allowed:
     * <ul>
     *     <li>sql_dialect - a valid string describing the sql dialect</li>
     *     <li>url - a jdbc URL to connect to the DB</li>
     *     <li>login - a valid login name</li>
     *     <li>password - password to authenticate</li>
     * </ul>
     * @param properties
     * @see SQLDialect
     * @see java.sql.DriverManager
     *
     */
    public DataAccess(Map<String, Object> properties) throws Exception {
        SQLDialect dialect = SQLDialect.valueOf((String)properties.get(I_DomainAccess.KEY_DIALECT));
        String url = (String)properties.get(I_DomainAccess.KEY_URL);
        String login = (String)properties.get(I_DomainAccess.KEY_LOGIN);
        String password = (String)properties.get(I_DomainAccess.KEY_PASSWORD);

        setParameters(dialect, url, login, password);

        knowledgeManager = (I_KnowledgeCache)properties.get(I_DomainAccess.KEY_KNOWLEDGE);
    }

    public DataAccess(DSLContext context, I_KnowledgeCache knowledgeManager){
        this.connection = context == null ? null : context.configuration().connectionProvider().acquire();
        this.context = context;
        this.knowledgeManager = knowledgeManager;
    }

    public DataAccess(I_DomainAccess domainAccess){
        this.connection = domainAccess.getConnection();
        this.context = domainAccess.getContext();
        this.knowledgeManager = domainAccess.getKnowledgeManager();
    }

    private void setParameters(SQLDialect dialect, String DBURL, String login, String password) throws Exception {
        try {
            this.connection = DriverManager.getConnection(DBURL, login, password);
        }
        catch (SQLException e){
            throw new IllegalArgumentException("SQL exception occurred while connecting:"+e);
        }

        if (connection == null)
            throw new IllegalArgumentException("Could not connect to DB");

        this.context = DSL.using(connection, dialect);

    }

    @Override
    public SQLDialect getDialect() {
        return context.dialect();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public DSLContext getContext() {
        return context;
    }

    @Override
    public I_KnowledgeCache getKnowledgeManager() {
        return knowledgeManager;
    }

    @Override
    public String getServerNodeId() {
        if (serverNodeId == null || serverNodeId.length() == 0)
            return "local.ethercis.com";
        return serverNodeId;
    }

}
