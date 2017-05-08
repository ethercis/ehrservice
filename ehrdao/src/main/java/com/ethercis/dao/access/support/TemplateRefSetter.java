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
import com.ethercis.dao.access.util.OptTemplateRef;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.jooq.pg.tables.records.ConceptRecord;
import com.ethercis.jooq.pg.tables.records.LanguageRecord;
import com.ethercis.jooq.pg.tables.records.TerritoryRecord;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Utility class to read from terminology.xml and inject the data into tables Language, Concept and Territory
 *
 * @author Christian Chevalley
 */
public class TemplateRefSetter {

    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String OPERATIONAL_TEMPLATE_PATH = "opt_path";
    private final String operationalTemplatePath;

    private String dbURL;
    private String userName;
    private String password;
    private SQLDialect dialect;

    //statistics for command line output
    private static int stat_insert_language = 0;
    private static int stat_update_language = 0;
    private static int stat_insert_territory = 0;
    private static int stat_update_territory = 0;
    private static int stat_insert_concept = 0;
    private static int stat_update_concept = 0;

    private static final Logger log = LogManager.getLogger(TemplateRefSetter.class);

    /**
     * Create a new setter for terminology header
     *
     * @param url                     JDBC url, for example: jdbc:postgresql://localhost:5432/ethercis
     * @param userName                user name to use to connect to the DB
     * @param password                password to use to connect to the DB
     * @param dialect                 SQL Dialect as defined in jOOQ SQLDialect
     * @param operationalTemplatePath Path to the XML file holding the data
     * @see SQLDialect
     * @see openEHR terminology schema: https://github.com/openEHR/specifications/blob/master/architecture/computable/terminology/terminology.xsd
     */
    public TemplateRefSetter(String url, String userName, String password, SQLDialect dialect, String operationalTemplatePath) {
        this.dbURL = url;
        this.userName = userName;
        this.password = password;
        this.dialect = dialect;
        this.operationalTemplatePath = operationalTemplatePath;
    }

    /**
     * Connect to the DB
     *
     * @return a connection or null if failed
     * @throws SQLException if failed to connect
     */
    public Connection connectDB() throws SQLException {
        Connection connection;
        //connect to DB
        try {
            connection = DriverManager.getConnection(dbURL, userName, password);
        } catch (SQLException e) {
            log.error("Could not connect to DB:" + e);
            return null;
        }

        return connection;
    }

    public I_KnowledgeCache knowledgeCache() {
        Properties props = new Properties();
//        props.put("knowledge.path.archetype", null);
//        props.put("knowledge.path.template", null);
        props.put("knowledge.path.opt", operationalTemplatePath);
        props.put("knowledge.forcecache", "true");
        I_KnowledgeCache knowledge;

        try {
            knowledge = new KnowledgeCache(null, props);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not set knowledge cache:" + e);
        }
        return knowledge;

    }

    /**
     * Populate the Tables using extracted data from the terminology file
     *
     * @param connection a valid connection to the DB
     * @return true if success false otherwise
     * @throws FileNotFoundException if the terminology file could not be read
     */
    public boolean createTemplateTable(Connection connection) throws Exception {

        log.info("Creating/updating template table...");

        //populate the DB using jOOQ
        OptTemplateRef optTemplateRef = new OptTemplateRef(DSL.using(connection, dialect), knowledgeCache());

        //clean-up
        optTemplateRef.deleteAll();
        optTemplateRef.upsert();

        log.info("Creating terminology table...Done");

        return true;

    }

    private static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("TemplateRefSetter", options);
    }

    /**
     * for using TerminologySetter as a standalone executable
     *
     * @param args
     */
    public static void main(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption(URL, true, "jdbc url to connect to the DB");
        options.addOption(LOGIN, true, "login id to connect to the DB");
        options.addOption(PASSWORD, true, "password to use to connect to the DB");
        options.addOption(OPERATIONAL_TEMPLATE_PATH, true, "operational templates repository");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        if (!(commandLine.hasOption(URL))) {
            usage(options);
            throw new IllegalArgumentException("Missing JDBC URL argument");
        }

        if (!(commandLine.hasOption(LOGIN))) {
            usage(options);
            throw new IllegalArgumentException("Missing login argument");
        }

        if (!(commandLine.hasOption(PASSWORD))) {
            usage(options);
            throw new IllegalArgumentException("Missing password argument");
        }

        if (!(commandLine.hasOption(OPERATIONAL_TEMPLATE_PATH))) {
            usage(options);
            throw new IllegalArgumentException("Missing operational template path argument");
        }

        TemplateRefSetter setter =
                new TemplateRefSetter(
                        commandLine.getOptionValue(URL),
                        commandLine.getOptionValue(LOGIN),
                        commandLine.getOptionValue(PASSWORD),
                        SQLDialect.POSTGRES,
                        commandLine.getOptionValue(OPERATIONAL_TEMPLATE_PATH));

        Connection connection;
        //setup connection
        try {

            connection = setter.connectDB();

        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not connect to DB using url:" + commandLine.getOptionValue(URL) + " exception:" + e);
        }

        if (connection == null)
            throw new IllegalArgumentException("Could not connect to DB using url:" + commandLine.getOptionValue(URL));

        try {
            log.info("Creating tables, please wait...");
            setter.createTemplateTable(connection);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not crete Template table, exception:" + e);

        }
    }

}
