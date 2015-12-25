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

package com.ethercis.ehr.knowledge;

import com.ethercis.dao.access.support.AccessTestCase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.lang.System;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by christian on 11/11/2015.
 */
public class KnowledgeCacheTest extends AccessTestCase {

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }

    @Test
    public void testAddOperationalTemplate() throws Exception {

        String prescriptionFilePath = "prescription.opt";

        I_KnowledgeCache knowledgeCache = testDomainAccess.getKnowledgeManager();

        //read in a template into a string
        Path path = Paths.get(knowledgeCache.getOptPath()+"/"+prescriptionFilePath);

        byte[] content = Files.readAllBytes(path);

        String operationaltemplate = testDomainAccess.getKnowledgeManager().addOperationalTemplate(content);

        assertNotNull(operationaltemplate);

        Map listTemplate = knowledgeCache.listOperationalTemplates();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        String outcome = gson.toJson(listTemplate);

        System.out.println(outcome);
    }
}