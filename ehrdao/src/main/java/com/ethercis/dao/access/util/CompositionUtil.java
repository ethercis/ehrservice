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

import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.build.SystemValue;
import org.openehr.rm.composition.Composition;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/9/2015.
 */
public class CompositionUtil {

    public static String dumpFlat(I_CompositionAccess compositionAccess) throws Exception {

        StringBuffer stringBuffer = new StringBuffer();

        for (I_EntryAccess entryAccess: compositionAccess.getContent()){
            Composition composition = entryAccess.getComposition();
            Map<String, String> testRetMap = new EcisFlattener().render(composition);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().disableHtmlEscaping().create();

            String jsonString = gson.toJson(testRetMap);

            stringBuffer.append(jsonString);
        }

        return stringBuffer.toString();
    }

    public static Map<String, String> dumpTemplateMap(I_KnowledgeCache knowledge, String templateId, CompositionSerializer.WalkerOutputMode mode) throws Exception {
        Map<SystemValue, Object> map = new HashMap<>();

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(map, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();
        Map<String, String> testRetMap = new EcisFlattener(true, mode).render(composition);
        return testRetMap;
    }

    public static void main(String[] args){
        Options options = new Options();
        Logger logger = LogManager.getLogger(CompositionUtil.class);

        options.addOption("ckm_archetype", true, "Path to archetypes repository");
        options.addOption("ckm_template", true, "Path to templates (OET) repository");
        options.addOption("ckm_opt", true, "Path to operational templates (OPT) repository");
        options.addOption("template", true, "Template Id to dump map from");

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);
            Properties properties = new Properties();

            if (commandLine.hasOption("ckm_archetype")) properties.put("knowledge.path.archetype", commandLine.getOptionValue("ckm_archetype", null));
            if (commandLine.hasOption("ckm_template")) properties.put("knowledge.path.template", commandLine.getOptionValue("ckm_template", null));
            if (commandLine.hasOption("ckm_opt")) properties.put("knowledge.path.opt", commandLine.getOptionValue("ckm_opt", null));
            properties.put("knowledge.forcecache", "true");

            String templateId = commandLine.getOptionValue("template", null);

            I_KnowledgeCache knowledge = new KnowledgeCache(null, properties);

            Map<String, String> map = CompositionUtil.dumpTemplateMap(knowledge, templateId, CompositionSerializer.WalkerOutputMode.EXPANDED);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();

            String jsonString = gson.toJson(map);
            System.out.println(jsonString);

            map = CompositionUtil.dumpTemplateMap(knowledge, templateId, CompositionSerializer.WalkerOutputMode.PATH);
            jsonString = gson.toJson(map);
            System.out.println(jsonString);

            System.exit(0);

        } catch (Exception e) {
            System.out.println("Could not dump template:"+e);
            System.exit(-1);
        }
    }

}
