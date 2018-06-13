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
package com.ethercis.opt.query.util;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.ethercis.opt.query.I_IntrospectCache;
import com.ethercis.opt.query.I_QueryOptMetaData;
import com.ethercis.opt.query.IntrospectCache;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/9/2015.
 */
public class MetaDataCacheUtil {

    Logger logger = LogManager.getLogger(MetaDataCacheUtil.class);

    static Properties properties = new Properties();
    static I_KnowledgeCache knowledge;
    static DSLContext context;
    static Connection connection;
    static I_IntrospectCache introspectCache;

    public static void initRunTime() throws Exception {
        knowledge = new KnowledgeCache(null, properties);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        String userName = properties.getProperty("test.db.user");
        String password = properties.getProperty("test.db.password");
        String url = "jdbc:postgresql://" + properties.getProperty("test.db.host") + ":" + properties.getProperty("test.db.port") + "/" + properties.getProperty("test.db.name");
        connection = DriverManager.getConnection(url, userName, password);
        try  {
            context = DSL.using(connection, SQLDialect.POSTGRES);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        introspectCache = new IntrospectCache(context, knowledge);
    }

    public static void main(String[] args){
        Boolean silent = false;
        Options options = new Options();

        options.addOption("archetypes", true, "Path to archetypes repository");
        options.addOption("templates", true, "Path to templates (OET) repository");
        options.addOption("opt", true, "Path to operational templates (OPT) repository");
        options.addOption("login", true, "Logon id to use to connect to the DB (default: postgres)");
        options.addOption("password", true, "Password to use to connect to the DB  (default: postgres)");
        options.addOption("port", true, "Port the DB listens to (default: 5432)");
        options.addOption("host", true, "Host the DB listens on (default: localhost)");
        options.addOption("db_name", true, "DB name (default: ethercis)");
        options.addOption("silent", true, "doesn't ask user confirmation to proceed (default: false)");

        options.addOption("command", true, "Operation to complete: SYNCHRONIZE | INVALIDATE | STATUS");

        options.addOption("help", false, "DB name (default: ethercis)");


        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("help")){
                //display the list of valid options
                System.out.println("Usage:");
                for (Option option: options.getOptions()){
                    System.out.println(option.toString());
                }

                System.out.println("\nvalid command are:");

                System.out.println("SYNCHRONIZE: synchronize meta data cached in DB with templates defined in knowledge cache");
                System.out.println("INVALIDATE: clears meta data cached in DB");
                System.out.println("STATUS: gives the list of meta data cached in DB per template");

                System.exit(0);
            }

            if (commandLine.hasOption("archetypes")) properties.put("knowledge.path.archetype", commandLine.getOptionValue("archetypes", null));
            if (commandLine.hasOption("templates")) properties.put("knowledge.path.template", commandLine.getOptionValue("templates", null));
            if (commandLine.hasOption("opt")) properties.put("knowledge.path.opt", commandLine.getOptionValue("opt", null));

            if (commandLine.hasOption("login"))
                properties.put("test.db.user", commandLine.getOptionValue("login", "postgres"));
            else
                properties.put("test.db.user", "postgres");

            if (commandLine.hasOption("password"))
                properties.put("test.db.password", commandLine.getOptionValue("password", "postgres"));
            else
                properties.put("test.db.password", "postgres");

            if (commandLine.hasOption("port"))
                properties.put("test.db.port", commandLine.getOptionValue("port", "5432"));
            else
                properties.put("test.db.port", "5432");

            if (commandLine.hasOption("host"))
                properties.put("test.db.host", commandLine.getOptionValue("host", "localhost"));
            else
                properties.put("test.db.host", "localhost");

            if (commandLine.hasOption("name"))
                properties.put("test.db.name", commandLine.getOptionValue("name", "ethercis"));
            else
                properties.put("test.db.name", "ethercis");

            properties.put("knowledge.forcecache", "true");

            String command = commandLine.getOptionValue("command", null);

            if (commandLine.hasOption("silent")) silent = new Boolean(commandLine.getOptionValue("silent", "false"));


            System.out.println("Cache utility will perform command:"+command);
            System.out.println("Using the following parameters:");
            System.out.println("\ndb port:"+properties.get("test.db.port"));
            System.out.println("db host:"+properties.get("test.db.host"));
            System.out.println("db login:"+properties.get("test.db.user"));
            System.out.println("db password:"+properties.get("test.db.password"));
            System.out.println("db name:"+properties.get("test.db.name"));

            if (!silent) {
                System.out.println("\nAre you sure you want to perform this operation [y/n] ?");

                String reply = "n";

                Scanner scanner = new Scanner(System.in);

                while (!reply.toLowerCase().startsWith("y")) {

                    reply = scanner.next();

                    if (reply == null)
                        System.exit(0);

                    if (reply.toLowerCase().equals("n"))
                        System.exit(0);

                    if (!reply.toLowerCase().equals("y"))
                        System.out.println("Please reply 'y' or 'n'");
                }
            }
            System.out.println("Initializing runtime...");
            initRunTime();
            System.out.println("Done");

            switch (command.toUpperCase()){
                case "SYNCHRONIZE":
                    introspectCache.synchronize();
                    break;
                case "INVALIDATE":
                    introspectCache.invalidateDBCache();
                    break;

                case "STATUS":
                    introspectCache.load();
                    List<Map<String, String>> visitors = introspectCache.visitors();
                    System.out.println("\nMeta data cached for "+visitors.size()+" templates [uuid, template_id]\n");
                    for (Map<String, String> item: visitors){
                        System.out.println(item.get("uuid")+"\t"+item.get("templateId"));
                    }

                    Map<String, Collection<Map<String, String>>> templates = knowledge.listOperationalTemplates();
                    System.out.println("\n"+templates.get("templates").size()+" templates in Knowledge Cache:");
                    for (Map<String, String> item: templates.get("templates")){

                        System.out.println((item.get("ERROR") != null ? ">>>ERROR " : "")+item.get("uid")+"\t"+item.get("templateId")+"\t location:"+(item.get("path") == null ? "*UNDEF*":item.get("path")));
                    }
                    break;

                default:
                    System.out.println("Could not interpret command:"+command+", exiting");
                    System.exit(0);
            }


            System.exit(0);

        } catch (Exception e) {
            System.out.println("Could not start cache utility:"+e);
            System.exit(-1);
        }
    }

}
