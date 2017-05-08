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

package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.handler.PvCompoHandler;
import com.ethercis.dao.access.interfaces.*;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.RmObjectHelper;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.dao.access.util.CompositionUtil;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.ehr.building.util.CompositionAttributesHelper;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.json.FlatJsonUtil;
import org.joda.time.DateTime;
import org.jooq.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class CompositionTest extends AccessTestCase {
    Map<String, Object> kvPairs = new HashMap<>();

    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;

    String templateId = "COLNEC Patient Blood Pressure.v1";

    long start, end;

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
        ehrIdUUID = TestHelper.createDummyEhr(testDomainAccess);
        systemUUID = TestHelper.createDummySystem(testDomainAccess);
        composerUUID = TestHelper.createDummyCommitter(testDomainAccess);
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/test/COLNEC_Blood_Pressure.v1.ecisflat.json");
//        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/pathvalues_test1.json");

        kvPairs = FlatJsonUtil.inputStream2Map(fileReader);

    }

    @After
    public void tearDown() throws Exception {
        //display the summary for this ehr Id
        Result<?> record = testDomainAccess.getContext()
                .select(EVENT_CONTEXT.OTHER_CONTEXT)
                .from(EVENT_CONTEXT
                        .join(COMPOSITION)
                                .on(EVENT_CONTEXT.COMPOSITION_ID.eq(COMPOSITION.ID))
                      )
                .where(COMPOSITION.EHR_ID.eq(ehrIdUUID)).fetch();

        I_EhrAccess.retrieveInstance(testDomainAccess, ehrIdUUID).delete();
        I_SystemAccess.delete(testDomainAccess, systemUUID);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerUUID).delete();
    }

    private UUID commitNewTestComposition() throws Exception {
        Integer changeCode = 276; //Any Event
        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
        Composition aComposition = RmObjectHelper.createDummyQualifiedCompositionWithParameters(
                templateId,
                composer,
                new CodePhrase("ISO_639-1", "en"),
                new CodePhrase("IANA_character-sets", "UTF-8"),
                new CodePhrase("ISO_3166-1", "GB"),
                ContextHelper.createNullContext());

        long startTime = System.nanoTime();

        //serialize other_context
//        CompositionSerializer compositionSerializer = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH, true);
//        Map<String, Object> otherContextMap  = compositionSerializer.processItem(aComposition.getContext().getOtherContext());
//
//        assertNotNull(otherContextMap);

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, "TEST", changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));

        assertNotNull(contributionAccess);

        //storeComposition a composition to add to this contribution
        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, aComposition, DateTime.now(), ehrIdUUID);
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, compositionAccess.getId(), aComposition);
        compositionAccess.addContent(entryAccess);

        contributionAccess.addComposition(compositionAccess);

        //commit this contribution
        return contributionAccess.commit();
    }
    /**
     * test selective updates: Context, Attributes, Content
     * @throws Exception
     */
    @Test
    public void testUpdateEcisFLATContext() throws Exception {

        PvCompoHandler pvCompoHandler = new PvCompoHandler(testDomainAccess, templateId, null);
        //commit this contribution
        start = System.nanoTime();
//        UUID uuid = commitNewTestComposition();
        UUID uuid = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
        uuid = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
        uuid = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
        end = System.nanoTime();
        System.out.println("INITIAL CREATION TIME:" + (end - start) / 1000000 + "[ms]");
        assertNotNull(uuid);

        start = System.nanoTime();
        //retrieveInstanceByNamedSubject the contribution
        I_CompositionAccess compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, uuid);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(1):" + (end - start) / 1000000 + "[ms]");
        assertNotNull(compositionAccess);
        System.out.println(CompositionUtil.dumpFlat(compositionAccess));

//        //update other_context
//        kvPairs.clear();
//        kvPairs.put("/context/other_context[at0001]/items[at0005]", "incomplete");
//        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.telecom_uk.v1]/items[at0002]", "unstructured telephone");
//
//        pvCompoHandler.updateComposition(kvPairs, null, null, null);
//        compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, uuid);
//        end = System.nanoTime();
//        System.out.println("RETRIEVAL TIME(1):" + (end - start) / 1000000 + "[ms]");
//        assertNotNull(compositionAccess);
//        System.out.println(CompositionUtil.dumpFlat(compositionAccess));

    }
}
