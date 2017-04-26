package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.handler.PvCompoHandler;
import com.ethercis.dao.access.interfaces.*;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.RmObjectHelper;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.dao.access.util.CompositionUtil;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.CompositionAttributesHelper;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.encode.*;
import com.ethercis.ehr.encode.wrappers.json.writer.DvDateAdapter;
import com.ethercis.ehr.encode.wrappers.json.writer.DvDateTimeAdapter;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class EcisFlatPersistenceTest extends AccessTestCase {


    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;
    String description = "test contribution";
    String templateId = "prescription";

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

        Integer contributionCode = 253; //Unknown

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, contributionCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

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
        compositionAccess.setContributionAccess(contributionAccess);

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
        Map<String, Object> kvPairs = new HashMap<>();

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

        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001, '#1']/timing", "before sleep");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001, '#1']" +
                        "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "aspirin");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001, '#2]/timing", "lunch");
        kvPairs.put("/content[openEHR-EHR-SECTION.medications.v1]/items[openEHR-EHR-INSTRUCTION.medication.v1]/activities[at0001, '#2']" +
                        "/description[openEHR-EHR-ITEM_TREE.medication_mod.v1]/items[at0001]", "Atorvastatin");

        Set<UUID> ids = retrieved.getCompositionIds();
        UUID testCompositionUID = ids.toArray(new UUID[]{})[0];
        I_CompositionAccess compositionAccess = retrieved.getComposition(testCompositionUID);

        compositionAccess.setContributionAccess(retrieved);

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

    @Test
    public void testCreateComposition(){
        Map<String, Object> kvPairs = new HashMap<>();

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

    //    @Test
    public void testCreateComposition2() throws Exception {
//        Map<String, String> kvPairs = new HashMap<>();
        String templateId = "COLNEC Health Risk Assessment.v1";
//        String templateId = "COLNEC Medication";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        StringBuffer sb = new StringBuffer();
        Files.readAllLines(Paths.get("C:\\Development\\Dropbox\\eCIS_Development\\test\\health_risk_assessment.ecisflat.json")).forEach(line -> sb.append(line));
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/ProblemList_2FLAT.json")).forEach(line -> sb.append(line));
//        Files.readAllLines(Paths.get("/Development/Dropbox/eCIS_Development/samples/Laboratory_Order_faulty.json")).forEach(line -> sb.append(line));
//        Files.readAllLines(Paths.get("/Development/Dropbox/COLNEC/colnec_medication.ecisflat.json"), Charset.defaultCharset()).forEach(line -> sb.append(line));

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
        builder.registerTypeAdapter(DvDate.class, new DvDateAdapter());
        Gson gson = builder.setPrettyPrinting().create();

        Map<String, Object> kvPairs = gson.fromJson(sb.toString(), HashMap.class);

        PvCompoHandler pvCompoHandler = new PvCompoHandler(testDomainAccess, templateId, null);
        UUID compositionId = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
        Composition composition = pvCompoHandler.assign(kvPairs);

        assertNotNull(composition);

        //serialize it

//        CompositionSerializer compositionSerializer = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
//        Map<String, Object> serialized = compositionSerializer.invalidateContent(composition);
//        String json = gson.toJson(serialized);
        I_CompositionSerializer serializer = I_CompositionSerializer.getInstance();
        System.out.println(serializer.dbEncode(composition));

        //and back to ECISFLAT
        Map<String, String> testRetMap = new EcisFlattener().render(composition);
        String json = gson.toJson(testRetMap);
        System.out.println(json);



    }

    //    @Test
    public void testTemplateExample() throws Exception {
//        Map<String, String> kvPairs = new HashMap<>();

        String templateId = "COLNEC Medication";
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        StringBuffer sb = new StringBuffer();
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();

        I_ContentBuilder contentBuilder  = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();

        assertNotNull(composition);

        //and back to ECISFLAT
        Map<String, String> testRetMap = new EcisFlattener(true).render(composition);
        String json = gson.toJson(testRetMap);
        System.out.println(json);



    }

    public void testRetrieveCompositionNew() throws Exception {
        UUID uuid = UUID.fromString("55032019-e6e4-4395-adce-e9f475b419c0");
        I_CompositionAccess compositionAccess = I_CompositionAccess.retrieveInstance2(testDomainAccess, uuid);

        List<I_EntryAccess> contents = compositionAccess.getContent();
        Composition composition = contents.get(0).getComposition();
        Map<String, String> testRetMap = new EcisFlattener().render(composition);
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(testRetMap);
        System.out.println(json);
    }


}
