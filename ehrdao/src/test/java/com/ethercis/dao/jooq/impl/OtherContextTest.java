package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.handler.PvCompoHandler;
import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.dao.access.util.CompositionUtil;
import com.ethercis.ehr.json.FlatJsonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class OtherContextTest extends AccessTestCase {
    Map<String, Object> kvPairs = new HashMap<>();

    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;
    String description = "AOMRC GENERIC OUTPATIENT LETTER";
    String templateId = "UK AoMRC Outpatient Letter.opt";

    long start, end;

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
        ehrIdUUID = TestHelper.createDummyEhr(testDomainAccess);
        systemUUID = TestHelper.createDummySystem(testDomainAccess);
        composerUUID = TestHelper.createDummyCommitter(testDomainAccess);
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/AOMRC GENERIC OUTPATIENT LETTER.json");

        kvPairs = FlatJsonUtil.inputStream2Map(fileReader);

//        kvPairs.clear();
//        kvPairs.put("/context/health_care_facility|name", "Northumbria Community NHS");
//        kvPairs.put("/context/health_care_facility|identifier", "999999-345");
//        kvPairs.put("/context/start_time", "2015-09-28T10:18:17.352+07:00");
//        kvPairs.put("/context/end_time", "2015-09-28T11:18:17.352+07:00");
//        kvPairs.put("/context/participation|function", "Oncologist");
//        kvPairs.put("/context/participation|name", "Dr. Marcus Johnson");
//        kvPairs.put("/context/participation|identifier", "1345678");
//        kvPairs.put("/context/participation|mode", "face-to-face communication::openehr::216");
//        kvPairs.put("/context/location", "local");
//        kvPairs.put("/context/setting", "openehr::227|emergency care|");
//        kvPairs.put("/composer|identifier", "1345678");
//        kvPairs.put("/composer|name", "Dr. Marcus Johnson");
//        kvPairs.put("/category", "openehr::433|event|");
//        kvPairs.put("/territory", "FR");
//        kvPairs.put("/language", "fr");
//        kvPairs.put("/context/other_context[at0001]/items[at0005]", "complete");
//        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.distribution.v1]/items[at0006]", "2015-10-21T10:00:00");
//        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0022]", "true");
//        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0014]", "2014-10-15::2022-12-31");
//
//        //this is to test the flatten vacuum!
//        kvPairs.put("/content[openEHR-EHR-SECTION.demographics_rcp.v1]/items[openEHR-EHR-ADMIN_ENTRY.key_contacts.v1]/data[at0001]/items[at0018]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]", "Joe Doe");
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-OBSERVATION.story.v1 and name/value='Story/History']/data[at0001]/events[at0002]/data[at0003]/items[at0004]", "big complain");
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-OBSERVATION.story.v1 and name/value='History since last contact']/data[at0001]/events[at0002]/data[at0003]/items[at0004]", "a long history");
//
////        //array...
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0002]", "does not sleep");
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0004]", "think too much");
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0005]", "think way too much");
////
////        //funny data types
////        //DvEHRURI
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-SECTION.adhoc.v1]/items[openEHR-EHR-SECTION.adhoc.v1 and name/value='Past medical history']/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/protocol[at0032]/items[at0035]", "http://home.cern/topics/higgs-boson");
////
////        //DvCodedText
//        kvPairs.put("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-SECTION.adhoc.v1]/items[openEHR-EHR-SECTION.adhoc.v1 and name/value='Past medical history']/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/protocol[at0032]/items[openEHR-EHR-CLUSTER.problem_status.v1]/items[at0060]", "local::1|Past|");
////
////        //choice as Text
//        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-INSTRUCTION.medication_order_uk.v1]/activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_item.v1]/items[at0001]", "Choice #1");
//
////        //choice as CodedText
//        kvPairs.put("/content[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1]/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-INSTRUCTION.medication_order_uk.v1]/activities[at0001]/description[at0002]/items[at0039]/items[at0041]", "local:111|Continue Indefinitely|");
//
////        //interval in a choice (NOT SUPPORTED YET!!!)
////        kvPairs.put("/content[openEHR-EHR-SECTION.social_context_rcp.v1]/items[openEHR-EHR-EVALUATION.alcohol_use_summary.v1]/data[at0001]/items[at0024]", "10::12");
//
////        //ordinal
//        kvPairs.put("/content[openEHR-EHR-SECTION.examination_findings_rcp.v1]/items[openEHR-EHR-SECTION.vital_signs.v1]/items[openEHR-EHR-OBSERVATION.news_rcp_uk.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0006]", "1|SNOMED-CT::313267000|Stroke|");
//
//        //interval (date)
//        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0014]", "2010-10-10::2011-11-11");

    }

    @After
    public void tearDown() throws Exception {
        I_EhrAccess.retrieveInstance(testDomainAccess, ehrIdUUID).delete();
        I_SystemAccess.delete(testDomainAccess, systemUUID);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerUUID).delete();
    }

//    private UUID commitNewTestComposition() throws Exception {
//        Integer changeCode = 276; //Any Event
//        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
//        Composition aComposition = RmObjectHelper.createDummyQualifiedCompositionWithParameters(
//                templateId,
//                composer,
//                new CodePhrase("ISO_639-1", "en"),
//                new CodePhrase("IANA_character-sets", "UTF-8"),
//                new CodePhrase("ISO_3166-1", "GB"),
//                ContextHelper.createNullContext());
//
//        long startTime = System.nanoTime();
//
//        //serialize other_context
////        CompositionSerializer compositionSerializer = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH, true);
////        Map<String, Object> otherContextMap  = compositionSerializer.processItem(aComposition.getContext().getOtherContext());
////
////        assertNotNull(otherContextMap);
//
//        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);
//
//        long endtime = System.nanoTime();
//
//        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));
//
//        assertNotNull(contributionAccess);
//
//        //storeComposition a composition to add to this contribution
//        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, aComposition, DateTime.now(), ehrIdUUID);
//        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, compositionAccess.getId(), aComposition);
//        compositionAccess.addContent(entryAccess);
//
//        contributionAccess.addComposition(compositionAccess);
//
//        //commit this contribution
//        return contributionAccess.commit();
//    }
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

        //update other_context
        kvPairs.clear();
        kvPairs.put("/context/other_context[at0001]/items[at0005]", "incomplete");
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.telecom_uk.v1]/items[at0002]", "unstructured telephone");

        pvCompoHandler.updateComposition(kvPairs, null, null, null);
        compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, uuid);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(1):" + (end - start) / 1000000 + "[ms]");
        assertNotNull(compositionAccess);
        System.out.println(CompositionUtil.dumpFlat(compositionAccess));

    }
}
