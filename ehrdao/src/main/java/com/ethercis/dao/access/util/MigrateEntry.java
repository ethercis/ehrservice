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

package com.ethercis.dao.access.util;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.dao.access.support.DummyDataAccess;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.*;
import org.openehr.rm.composition.Composition;

import java.lang.System;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by christian on 5/30/2016.
 */
public class MigrateEntry {
    static Logger logger = LogManager.getLogger(MigrateEntry.class);
    protected static I_DomainAccess domainAccess;
    protected static DSLContext context;
    protected static I_KnowledgeCache knowledge;

    static boolean isInitialized = false;

    public MigrateEntry(Properties props) throws Exception {
        setupDomainAccess(props);
    }

    protected static void setupDomainAccess(Properties props) throws Exception {

        if (isInitialized) return;

        props.put("knowledge.forcecache", "true");
        props.put("knowledge.cachelocatable", "false");

        knowledge = new KnowledgeCache(null, props);

        Map<String, Object> properties = new HashMap<>();
        properties.put(I_DomainAccess.KEY_DIALECT, "POSTGRES");
        properties.put(I_DomainAccess.KEY_URL, "jdbc:postgresql://localhost:"+props.getOrDefault("db.port", 5434)+"/ethercis");
        properties.put(I_DomainAccess.KEY_URL, props.getOrDefault(I_DomainAccess.KEY_URL, properties.get(I_DomainAccess.KEY_URL)));
        properties.put(I_DomainAccess.KEY_LOGIN, "postgres");
        properties.put(I_DomainAccess.KEY_PASSWORD, "postgres");

        properties.put(I_DomainAccess.KEY_KNOWLEDGE, knowledge);

        try {
            domainAccess = new DummyDataAccess(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = domainAccess.getContext();

        isInitialized = true;

        logger.info("MIGRATING Composition at:"+properties.get("url"));
    }

    public static String dumpSerialized(Composition composition) throws Exception {
        I_CompositionSerializer serializer = I_CompositionSerializer.getInstance();
        return serializer.dbEncode(composition);
//        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
//        Map<String, Object> retMap = inspector.invalidateContent(composition);
//        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
////        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//        //choose this option to ease reading and debugging... but not for storing into DB
//        Gson gson = builder.setPrettyPrinting().create();
//        String serialized = gson.toJson(retMap);
//        return serialized;
    }

    public static UUID saveUncommittedEntry(Composition composition) throws Exception {
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(domainAccess, composition.getArchetypeDetails().getTemplateId().getValue(), 0, UUID.randomUUID(), composition);
        //grab a composition and use it to set the composition_id (ref. integrity)
        UUID composition_id = domainAccess.getContext().selectFrom(COMPOSITION).limit(1).fetchOne(COMPOSITION.ID);
        entryAccess.setCompositionId(composition_id);
        return entryAccess.commit(Timestamp.valueOf(LocalDateTime.now()));
    }

    public static  String migrateComposition(Properties properties, UUID compositionId, boolean debug) throws Exception {
        setupDomainAccess(properties);

        //get the entry id for the composition
        UUID entryId = context.select(ENTRY.ID).from(ENTRY).where(ENTRY.COMPOSITION_ID.eq(compositionId)).fetchOne(ENTRY.ID);

        if (entryId == null)
            throw new IllegalArgumentException("Could not retrieve composition:"+compositionId);

        return migrateEntry(properties, entryId, debug);
    }

    /**
     * Migrates a jsonb entry structure to the new format.
     * @param properties
     * @param entryId
     * @param debug
     * @return
     * @throws Exception
     */
    public static  String migrateEntry(Properties properties, UUID entryId, boolean debug) throws Exception {
        setupDomainAccess(properties);
//        System.setProperty("validation.lenient", "true");
        I_EntryAccess entryAccess = I_EntryAccess.retrieveInstance(domainAccess, entryId);

        if (entryAccess == null)
            throw new IllegalArgumentException("Could not retrieve entry id:"+entryId);

        String templateId = entryAccess.getTemplateId();

        if (entryAccess != null){
            String entry = entryAccess.getEntryJson();
            Composition composition = migrate(entry, templateId);
            if (debug){
                return dumpSerialized(composition);
            }
            else {
                entryAccess.setCompositionData(templateId, composition);
                //update this entry
                return entryAccess.update(Timestamp.valueOf(LocalDateTime.now()), true).toString();
            }
        }
        else
            throw new IllegalArgumentException("Could not retrieve entry:"+entryId);

    }

    public static Composition migrate(String jsonEntry, String templateId) throws Exception {
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        content.setLenient(true);
        Composition composition = content.buildCompositionFromJson(jsonEntry);
        return composition;
    }

    public static void migrateAll(boolean debug) throws Exception {
        Result<Record1<UUID>> result = context.select(ENTRY.ID).from(ENTRY).fetch();

        if (result == null)
            throw new IllegalArgumentException("Empty DB");

        System.out.println("Migrating "+result.size()+" entries ");

        int count = 0;
        for (Record record: result){
            UUID entryId = (UUID)record.getValue("id");
            try {
                String serialized = migrateEntry(null, entryId, debug);
                if (debug){
                    //check for any remaining default in serialized
                    if (serialized.contains("DEFAULT") || serialized.contains("1900-01-01")){
                        System.out.println("!!!=== DEFAULT FOUND IN " + entryId);
                        System.out.println(serialized);
                    }
                }

                ++count;
            } catch (Exception e){
                System.out.println("\nCould not migrate:"+entryId+" ,reason:"+e);
            }
            System.out.print(".");
        }

        System.out.println("Migrated " + count + " entries");
    }

    /**
     * Utility to convert legacy entries into a newer format
     * @param args
     */
    public static void main(String[] args){

        Options options = new Options();
        Logger logger = LogManager.getLogger(CompositionUtil.class);

        options.addOption("uuid", true, "UUID of entry to migrate");
        options.addOption("ckm_archetype", true, "Path to archetypes repository");
        options.addOption("ckm_template", true, "Path to templates (OET) repository");
        options.addOption("ckm_opt", true, "Path to operational templates (OPT) repository");
        options.addOption("port", true, "port # to bind to the DB (default:5432)");
        options.addOption("all", false, "if set migrate all entries (mutually exclusive with option uuid");
        options.addOption("debug", false, "if set simulate the migration but do not commit to the DB");

        System.out.println("Migrate json db entry into the new format");
        System.out.println("- set new default value convention in DataValue");
        System.out.println("- remove any name/value in node predicates");
        System.out.println("- convert Date value serialization into an AQL compatible form");
        System.out.println("Please make sure you have a backup of your DB before using this utility");
        System.out.println("Do you want to continue [y/N]?");

        Scanner scanner = new Scanner(System.in);

        String keyIn;

        while (true){
            keyIn = scanner.next();

            if (keyIn.toUpperCase().equals("Y"))
                break;
            else
                if (keyIn.toUpperCase().equals("N"))
                    System.exit(0);
        }

        try {
            boolean debug = false;

            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);
            Properties properties = new Properties();

            if (commandLine.hasOption("ckm_archetype")) properties.put("knowledge.path.archetype", commandLine.getOptionValue("ckm_archetype", null));
            if (commandLine.hasOption("ckm_template")) properties.put("knowledge.path.template", commandLine.getOptionValue("ckm_template", null));
            if (commandLine.hasOption("ckm_opt")) properties.put("knowledge.path.opt", commandLine.getOptionValue("ckm_opt", null));
            if (commandLine.hasOption("port")) properties.put("db.port", Integer.valueOf(commandLine.getOptionValue("port", "5432")));

            properties.put("knowledge.forcecache", "true");

            setupDomainAccess(properties);

            if (commandLine.hasOption("debug")) debug = true;

            if (commandLine.hasOption("all"))
                migrateAll(debug);
            else {
                UUID entryId = null;
                if (commandLine.hasOption("uuid"))
                    entryId = UUID.fromString(commandLine.getOptionValue("uuid", null));
                migrateEntry(properties, entryId, debug);

            }
            System.exit(0);

        } catch (Exception e) {
            System.out.println("Could not migrate entry:"+e);
            System.exit(-1);
        }
    }

}
