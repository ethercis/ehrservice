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

import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.composition.Composition;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by christian on 5/31/2016.
 */
public class MigrateEntryTest extends AccessTestCase {

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }


    @Test
    public void testMigrateEntries() throws Exception {
        MigrateEntry migrateEntry = new MigrateEntry(testDomainAccess);
        migrateEntry.migrateAll(false);

    }


    @Test
    public void testMigrateSingleEntry() throws Exception {

//        UUID uuid = UUID.fromString("2e3c7d66-76eb-4ff9-9afd-c87ecf820583");
        UUID uuid = UUID.fromString("453e7d14-763b-4ab5-8f37-8340f9f4c9f5");

        MigrateEntry migrateEntry = new MigrateEntry(testDomainAccess);
        String out = migrateEntry.migrateEntry(testDomainAccess, uuid, true);
        System.out.println(out);

    }

    @Test
    public void testMigrateSingleComposition() throws Exception {
//        UUID uuid = UUID.fromString("2e3c7d66-76eb-4ff9-9afd-c87ecf820583");
        UUID uuid = UUID.fromString("8bcf593c-893d-4325-8fcc-3fa5ca194f38");

        MigrateEntry migrateEntry = new MigrateEntry(testDomainAccess);
        String out = migrateEntry.migrateComposition(testDomainAccess, uuid, false);
        System.out.println(out);

    }

    @Test
    public void testMigrateXMLImport() throws Exception {
        String path = "src/test/resources/samples";
        String document = "RIPPLE_conformanceTesting_RAW.xml";

        MigrateEntry migrateEntry = new MigrateEntry(testDomainAccess);
        I_KnowledgeCache knowledge = testDomainAccess.getKnowledgeManager();
        //import the document
        String documentPath = path + "/" + document;
        InputStream is = new FileInputStream(new File(documentPath));
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, "");
        Composition composition = content.importCanonicalXML(is);
        assertNotNull(composition);
        String templateId = composition.getArchetypeDetails().getTemplateId().getValue();
        content.setTemplateId(templateId);
        content.setEntryData(composition);
        String serialized = content.getEntry();

        Composition composition1 = migrateEntry.migrate(serialized, templateId);
        String newJsonEntry = MigrateEntry.dumpSerialized(composition1);

        assertNotNull(newJsonEntry);

        MigrateEntry.saveUncommittedEntry(composition1);

    }
}