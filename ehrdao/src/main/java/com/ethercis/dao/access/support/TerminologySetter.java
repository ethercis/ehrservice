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

import com.ethercis.jooq.pg.tables.records.ConceptRecord;
import com.ethercis.jooq.pg.tables.records.LanguageRecord;
import com.ethercis.jooq.pg.tables.records.TerritoryRecord;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
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
import java.util.UUID;
import java.util.Vector;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Utility class to read from terminology.xml and inject the data into tables Language, Concept and Territory
 *
 * @author Christian Chevalley
 */
public class TerminologySetter {

    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String TERMINOLOGY = "terminology";
    public static final String TERMSETTER = "termsetter";
    public static final String UPDATE = "updateComposition";
    private String dbURL;
    private String userName;
    private String password;
    private SQLDialect dialect;
    private String terminologyFilePath;

    //statistics for command line output
    private static int stat_insert_language = 0;
    private static int stat_update_language = 0;
    private static int stat_insert_territory = 0;
    private static int stat_update_territory = 0;
    private static int stat_insert_concept = 0;
    private static int stat_update_concept = 0;

    private static final Logger log = Logger.getLogger(TerminologySetter.class);

    /**
     * Create a new setter for terminology header
     * @param url JDBC url, for example: jdbc:postgresql://localhost:5432/ethercis
     * @param userName user name to use to connect to the DB
     * @param password password to use to connect to the DB
     * @param dialect SQL Dialect as defined in jOOQ SQLDialect
     * @param terminolgyFilePath Path to the XML file holding the data
     * @see org.jooq.SQLDialect
     * @see openEHR terminology schema: https://github.com/openEHR/specifications/blob/master/architecture/computable/terminology/terminology.xsd
     */
    public TerminologySetter(String url, String userName, String password, SQLDialect dialect, String terminolgyFilePath){
        this.dbURL = url;
        this.userName = userName;
        this.password = password;
        this.dialect = dialect;
        this.terminologyFilePath = terminolgyFilePath;
    }

    /**
     * Connect to the DB
     * @return a connection or null if failed
     * @throws SQLException if failed to connect
     */
    public Connection connectDB() throws SQLException {
        Connection connection;
        //connect to DB
        try {
            connection = DriverManager.getConnection(dbURL, userName, password);
        }
        catch (SQLException e) {
            log.error("Could not connect to DB:" + e);
            return null;
        }

        return connection;
    }

    /**
     * Populate the Tables using extracted data from the terminology file
     * @param connection a valid connection to the DB
     * @return true if success false otherwise
     * @throws FileNotFoundException if the terminology file could not be read
     */
    public boolean createTerminologyTables(Connection connection) throws FileNotFoundException {

        log.info("Creating terminology table...");
        File xml = new File(terminologyFilePath);
        InputStream is = new FileInputStream(xml);
        TerminologyReader reader = new TerminologyReader(is);
        reader.read();

        //populate the DB using jOOQ

        DSLContext dslContext = DSL.using(connection, dialect);

        //clean-up
        dslContext.delete(CONCEPT).execute(); //referential integrity!
        dslContext.delete(LANGUAGE).execute();

        createLanguages(reader, dslContext);
        createConcept(reader, dslContext);
        createTerritory(reader, dslContext);

        log.info("Creating terminology table...Done");

        return true;

    }

    /**
     * Populate the Tables using extracted data from the terminology file
     * @param connection a valid connection to the DB
     * @return true if success false otherwise
     * @throws FileNotFoundException if the terminology file could not be read
     */
    public boolean updateTerminologyTables(Connection connection) throws FileNotFoundException {

        log.info("Updating terminology table...");

        File xml = new File(terminologyFilePath);
        InputStream is = new FileInputStream(xml);
        TerminologyReader reader = new TerminologyReader(is);
        reader.read();

        //populate the DB using jOOQ

        DSLContext dslContext = DSL.using(connection, dialect);

        updateLanguages(reader, dslContext);
        updateConcept(reader, dslContext);
        updateTerritory(reader, dslContext);

        log.info("Updating terminology table...Done");

        return true;

    }

    /**
     * Populate the Language table
     * @param reader
     * @param context
     * @return
     */
    private boolean createLanguages(TerminologyReader reader, DSLContext context){

        log.info("Creating languages table...");

        //language table
        Hashtable<String, String> languages = reader.getLanguageTable();

        for (String code : languages.keySet()){
            String description = languages.get(code);
            context.insertInto(LANGUAGE).values(code, description).returning().fetchOne();
            stat_insert_language += 1;
        }
        log.info("Creating languages table...Done");

        return true;
    }

    /**
     * Populate the Language table
     * @param reader
     * @param context
     * @return
     */
    private boolean updateLanguages(TerminologyReader reader, DSLContext context){

        log.info("Updating languages table...");

        //language table
        Hashtable<String, String> languages = reader.getLanguageTable();

        for (String code : languages.keySet()){
            String description = languages.get(code);
            //updateComposition or storeComposition depending whether the record exists or not
            LanguageRecord record = context.fetchOne(LANGUAGE, LANGUAGE.CODE.eq(code));

            if (record != null){
                record.setDescription(description);
                record.update();
                stat_update_language += 1;
            }
            else {
                context.insertInto(LANGUAGE).values(code, description).returning().fetchOne();
                stat_insert_language += 1;
            }
        }

        log.info("Updating languages table...Done");

        return true;
    }

    /**
     * Populate the Concept table
     * @param reader
     * @param context
     * @return
     */
    private boolean createConcept(TerminologyReader reader, DSLContext context){
        log.info("Creating concept table...");

        //language table
        Hashtable<String, Hashtable<Integer, String>> concept = reader.getConceptTable();


        for (String language : concept.keySet()){
            Hashtable<Integer, String> description = concept.get(language);
            for (Integer conceptid: description.keySet()) {
                String rubric = description.get(conceptid);
                //generate UUID for this entry
                UUID uuid = UUID.randomUUID();
                context.insertInto(CONCEPT).values(uuid, conceptid, language, rubric).returning().fetchOne();
                stat_insert_concept += 1;
            }
        }

        log.info("Creating concept table...Done");

        return true;
    }

    /**
     * Populate the Concept table
     * @param reader
     * @param context
     * @return
     */
    private boolean updateConcept(TerminologyReader reader, DSLContext context){
        log.info("Updating concept table...");

        //language table
        Hashtable<String, Hashtable<Integer, String>> concept = reader.getConceptTable();


        for (String language : concept.keySet()){
            Hashtable<Integer, String> description = concept.get(language);
            for (Integer conceptid: description.keySet()) {
                String rubric = description.get(conceptid);
                //check if exists already
                ConceptRecord record = context.fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(conceptid).and(CONCEPT.LANGUAGE.eq(language)));
                if (record != null){
                    record.setDescription(rubric);
                    record.update();
                    stat_update_concept += 1;
                }
                else {
                    //generate UUID for this entry
                    UUID uuid = UUID.randomUUID();
                    context.insertInto(CONCEPT).values(uuid, conceptid, language, rubric).returning().fetchOne();
                    stat_insert_concept += 1;
                }
            }
        }

        log.info("Updating concept table...Done");

        return true;
    }

    /**
     * Populate the Territory table
     * @param reader
     * @param context
     * @return
     */
    private boolean createTerritory(TerminologyReader reader, DSLContext context){
        log.info("Creating Territory table...");

        //language table
        Hashtable<Integer, Vector<String>> territory = reader.getTerritoryTable();

        //empty the table content to re-fill it with "fresh" data
        context.delete(TERRITORY).execute();

        for (Integer code : territory.keySet()){
            Vector<String> description = territory.get(code);
            context.insertInto(TERRITORY).values(code, description.get(0), description.get(1), description.get(2)).returning().fetchOne();
            stat_insert_territory += 1;
        }

        log.info("Creating Territory table...Done");

        return true;
    }

    /**
     * Populate the Territory table
     * @param reader
     * @param context
     * @return
     */
    private boolean updateTerritory(TerminologyReader reader, DSLContext context){
        log.info("Updating Territory table...");

        //language table
        Hashtable<Integer, Vector<String>> territory = reader.getTerritoryTable();

        for (Integer code : territory.keySet()){
            Vector<String> description = territory.get(code);
            //check for existence and updateComposition
            TerritoryRecord record = context.fetchOne(TERRITORY, TERRITORY.CODE.eq(code));

            if (record != null){
                record.setTwoletter(description.get(0));
                record.setThreeletter(description.get(1));
                record.setText(description.get(2));
                record.update();
                stat_update_territory += 1;
            }
            else {
                context.insertInto(TERRITORY).values(code, description.get(0), description.get(1), description.get(2)).returning().fetchOne();
                stat_insert_territory += 1;
            }
        }

        log.info("Updating Territory table...Done");

        return true;
    }

    private static void usage(Options options){
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp(TERMSETTER, options);
    }

    /**
     * for using TerminologySetter as a standalone executable
     * @param args
     */
    public static void main(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption(URL,true, "jdbc url to connect to the DB");
        options.addOption(LOGIN, true, "login id to connect to the DB");
        options.addOption(PASSWORD, true, "password to use to connect to the DB");
        options.addOption(TERMINOLOGY, true, "terminology file path");
        options.addOption(UPDATE, false, "perform an updateComposition of existing tables");

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

        if (!(commandLine.hasOption(TERMINOLOGY))) {
            usage(options);
            throw new IllegalArgumentException("Missing terminology argument");
        }

        TerminologySetter setter = new TerminologySetter(commandLine.getOptionValue(URL),
                                                         commandLine.getOptionValue(LOGIN),
                                                         commandLine.getOptionValue(PASSWORD),
                                                         SQLDialect.POSTGRES,
                                                         commandLine.getOptionValue(TERMINOLOGY));
        Connection connection;
        //setup connection
        try {

            connection = setter.connectDB();

        }
        catch (SQLException e){
            throw new IllegalArgumentException("Could not connect to DB using url:"+commandLine.getOptionValue(URL)+" exception:"+e);
        }

        if (connection == null)
            throw new IllegalArgumentException("Could not connect to DB using url:"+commandLine.getOptionValue(URL));

        try {
            if (commandLine.hasOption(UPDATE)) {
                log.info("Updating tables, please wait....");
                setter.updateTerminologyTables(connection);
            }
            else {
                log.info("Creating tables, please wait...");
                setter.createTerminologyTables(connection);
            }
        }
        catch (DataAccessException e){
            throw new IllegalArgumentException("Could not updateComposition table, exception:"+e);

        }
        catch (FileNotFoundException e){
            throw new IllegalArgumentException("Could not access terminology file:"+commandLine.getOptionValue(TERMINOLOGY)+" exception:"+e);

        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\nLanguages inserted  :"+stat_insert_language);
        stringBuffer.append("\nLanguages updated   :"+stat_update_language);
        stringBuffer.append("\nTerritories inserted:"+stat_insert_territory);
        stringBuffer.append("\nTerritories updated :"+stat_update_territory);
        stringBuffer.append("\nConcepts inserted   :"+stat_insert_concept);
        stringBuffer.append("\nConcepts updated    :" + stat_update_concept);

        log.info(stringBuffer);
    }

}
