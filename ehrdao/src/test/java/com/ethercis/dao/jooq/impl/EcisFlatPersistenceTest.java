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
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class EcisFlatPersistenceTest extends AccessTestCase {


    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;
    String description = "test contribution";
    String templateId = "prescription.opt";

    long start, end;

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
        ehrIdUUID = TestHelper.createDummyEhr(testDomainAccess);
        systemUUID = TestHelper.createDummySystem(testDomainAccess);
        composerUUID = TestHelper.createDummyCommitter(testDomainAccess);
    }

    @After
    public void tearDown() throws Exception {
        I_EhrAccess.retrieveInstance(testDomainAccess, ehrIdUUID).delete();
        I_SystemAccess.delete(testDomainAccess, systemUUID);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerUUID).delete();
    }

    private UUID commitNewTestComposition() throws Exception {
        EventContext eventContext = ContextHelper.createNullContext();
//        I_ContextAccess contextAccess = I_ContextAccess.getNewInstance(testDomainAccess, eventContext);
//        UUID contextUUID = contextAccess.commit();

        Integer changeCode = 276; //Any Event

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));

        assertNotNull(contributionAccess);


        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
        Composition aComposition = RmObjectHelper.createDummyQualifiedCompositionWithParameters(
                templateId,
                composer,
                new CodePhrase("ISO_639-1", "en"),
                new CodePhrase("IANA_character-sets", "UTF-8"),
                new CodePhrase("ISO_3166-1", "GB"),
                eventContext);

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

        //commit this contribution
        start = System.nanoTime();
        UUID uuid = commitNewTestComposition();
        end = System.nanoTime();
        System.out.println("INITIAL CREATION TIME:"+(end - start)/1000000+"[ms]");

        assertNotNull(uuid);

        start = System.nanoTime();
        //retrieveInstanceByNamedSubject the contribution
        I_ContributionAccess retrieved = I_ContributionAccess.retrieveInstance(testDomainAccess, uuid);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(1):"+(end - start)/1000000+"[ms]");
        assertNotNull(retrieved);

        //updateComposition its context
        Map<String, String> kvPairs = new HashMap<>();

        kvPairs.put("/context/health_care_facility|name", "Northumbria Community NHS");
        kvPairs.put("/context/health_care_facility|identifier", "999999-345");
        kvPairs.put("/context/start_time", "2015-09-28T10:18:17.352+07:00");
        kvPairs.put("/context/end_time", "2015-09-28T11:18:17.352+07:00");
        kvPairs.put("/context/participation|function", "Oncologist");
        kvPairs.put("/context/participation|name", "Dr. Marcus Johnson");
        kvPairs.put("/context/participation|identifier", "1345678");
        kvPairs.put("/context/participation|mode", "face-to-face communication::openehr::216");
        kvPairs.put("/context/location", "local");
        kvPairs.put("/context/setting", "openehr::227|emergency care|");
        kvPairs.put("/composer|identifier", "1345678");
        kvPairs.put("/composer|name", "Dr. Marcus Johnson");
        kvPairs.put("/category", "openehr::433|event|");
        kvPairs.put("/territory", "FR");
        kvPairs.put("/language", "fr");

        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:0", "Nurse|1345678::Jessica|face-to-face communication::openehr::216");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:1", "Assistant|1345678::2.16.840.1.113883.2.1.4.3::NHS-UK::ANY::D. Mabuse|face-to-face communication::openehr::216");

        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/timing", "before sleep");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]" +
                        "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "aspirin");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0002]/timing", "lunch");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0002]" +
                        "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "Atorvastatin");

        Set<UUID> ids = retrieved.getCompositionIds();
        UUID testCompositionUID = ids.toArray(new UUID[]{})[0];
        I_CompositionAccess compositionAccess = retrieved.getComposition(testCompositionUID);

        PvCompoHandler pvCompoHandler = new PvCompoHandler(testDomainAccess, compositionAccess, templateId, null);

        start = System.nanoTime();
        pvCompoHandler.updateComposition(kvPairs, null, null, null);
        end = System.nanoTime();
        System.out.println("UPDATE TIME:" + (end - start) / 1000000 + "[ms]");

        //dumpFlat the resulting composition
        //retrieve the composition after updateComposition!

        start = System.nanoTime();
        compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, testCompositionUID);
        String jsonFlat = CompositionUtil.dumpFlat(compositionAccess);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(2):" + (end - start) / 1000000 + "[ms]");

        System.out.println(jsonFlat);
    }

//    @Test
    public void _testCreateComposition(){
        Map<String, String> kvPairs = new HashMap<>();

        kvPairs.put("/context/health_care_facility|name", "Northumbria Community NHS");
        kvPairs.put("/context/health_care_facility|identifier", "999999-345");
        kvPairs.put("/context/start_time", "2015-09-28T10:18:17.352+07:00");
        kvPairs.put("/context/end_time", "2015-09-28T11:18:17.352+07:00");
        kvPairs.put("/context/participation|function", "Oncologist");
        kvPairs.put("/context/participation|name", "Dr. Marcus Johnson");
        kvPairs.put("/context/participation|identifier", "1345678");
        kvPairs.put("/context/participation|mode", "face-to-face communication::openehr::216");
        kvPairs.put("/context/location", "local");
        kvPairs.put("/context/setting", "openehr::227|emergency care|");
        kvPairs.put("/composer|identifier", "1345678");
        kvPairs.put("/composer|name", "Dr. Marcus Johnson");
        kvPairs.put("/category", "openehr::433|event|");
        kvPairs.put("/territory", "FR");
        kvPairs.put("/language", "fr");

        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:0", "Nurse|1345678::Jessica|face-to-face communication::openehr::216");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/participation:1", "Assistant|1345678::2.16.840.1.113883.2.1.4.3::NHS-UK::ANY::D. Mabuse|face-to-face communication::openehr::216");

        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]/timing", "before sleep");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001]" +
                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "aspirin");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0002]/timing", "lunch");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0002]" +
                "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "Atorvastatin");

        PvCompoHandler pvCompoHandler = new PvCompoHandler(testDomainAccess, templateId, null);


        try {
            start = System.nanoTime();
            UUID compositionId = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
            end = System.nanoTime();
            System.out.println("INITIAL CREATION TIME:"+(end - start)/1000000+"[ms]");
            assertNotNull(compositionId);

            start = System.nanoTime();
            I_CompositionAccess compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, compositionId);
            end = System.nanoTime();
            System.out.println("RETRIEVAL TIME(1):"+(end - start)/1000000+"[ms]");
            System.out.println(CompositionUtil.dumpFlat(compositionAccess));

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not storeComposition new composition:"+e);
        }
    }

}
