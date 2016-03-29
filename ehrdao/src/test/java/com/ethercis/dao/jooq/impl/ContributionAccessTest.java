package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.*;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.RmObjectHelper;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.CompositionAttributesHelper;
import com.ethercis.ehr.building.util.ContextHelper;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ContributionAccessTest extends AccessTestCase {

    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;

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

    @Test
    public void testCommit() throws Exception {

        EventContext eventContext = ContextHelper.createDummyContext();
        I_ContextAccess contextAccess = I_ContextAccess.getInstance(testDomainAccess, eventContext);
        UUID contextUUID = contextAccess.commit();

        String description = "test contribution";
        String templateId = "section  observation test.oet";
        Integer changeCode = 276; //Any Event

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:"+(endtime - startTime));

        assertNotNull(contributionAccess);

        //storeComposition a composition to add to this contribution
        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, "en", "TH", DateTime.now(), contextUUID, composerUUID, ehrIdUUID);

        Composition aComposition = RmObjectHelper.createDummyComposition(templateId);

        //add content
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, compositionAccess.getId(), aComposition);
        compositionAccess.addContent(entryAccess);
        compositionAccess.setComposition(aComposition);

        contributionAccess.addComposition(compositionAccess);

        //commit this contribution
        UUID uuid = contributionAccess.commit();

        assertNotNull(uuid);

        //retrieveInstanceByNamedSubject the contribution
        I_ContributionAccess retrieved = I_ContributionAccess.retrieveInstance((I_DomainAccess) contributionAccess, uuid);

        assertNotNull(retrieved);

        //get the list of composition for this ehr
        Map<String, Map<String, String>> compositionMap = I_EhrAccess.getCompositionList(testDomainAccess, ehrIdUUID);
        assertNotNull(compositionMap);
    }

     @Test
    public void testCommitWithParameters() throws Exception {

        EventContext eventContext = ContextHelper.createDummyContext();
//        I_ContextAccess contextAccess = I_ContextAccess.getNewInstance(testDomainAccess, eventContext);
//        UUID contextUUID = contextAccess.commit();

        String description = "test contribution";
        String templateId = "section  observation test.oet";
        Integer changeCode = 276; //Any Event

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));

        assertNotNull(contributionAccess);


        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
        Composition aComposition = RmObjectHelper.createDummyCompositionWithParameters(templateId,
                composer,
                new CodePhrase("ISO_639-1", "en"),
                new CodePhrase("IANA_character-sets","UTF-8"),
                new CodePhrase("ISO_3166-1","GB"),
                eventContext);

        //storeComposition a composition to add to this contribution
        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, aComposition, DateTime.now(), ehrIdUUID);
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, compositionAccess.getId(), aComposition);
        compositionAccess.addContent(entryAccess);

        contributionAccess.addComposition(compositionAccess);

        //commit this contribution
        UUID uuid = contributionAccess.commit();

        assertNotNull(uuid);

        //retrieveInstanceByNamedSubject the contribution
        I_ContributionAccess retrieved = I_ContributionAccess.retrieveInstance((I_DomainAccess) contributionAccess, uuid);

        assertNotNull(retrieved);

        Set<UUID> ids = retrieved.getCompositionIds();
        UUID composerId = retrieved.getComposition(ids.toArray(new UUID[]{})[0]).getComposerId();
        String composerName = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerId).getPartyName();

        assertEquals("ludwig", composerName);
    }

    @Test
    public void testCommitOPTWithParameters() throws Exception {

        EventContext eventContext = ContextHelper.createDummyContext();
//        I_ContextAccess contextAccess = I_ContextAccess.getNewInstance(testDomainAccess, eventContext);
//        UUID contextUUID = contextAccess.commit();

        String description = "test contribution";
        String templateId = "prescription";
        Integer changeCode = 276; //Any Event

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));

        assertNotNull(contributionAccess);


        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
        Composition aComposition = RmObjectHelper.createDummyQualifiedCompositionWithParameters(
                testDomainAccess.getKnowledgeManager(),
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
        UUID uuid = contributionAccess.commit();

        assertNotNull(uuid);

        //retrieveInstanceByNamedSubject the contribution
        I_ContributionAccess retrieved = I_ContributionAccess.retrieveInstance((I_DomainAccess) contributionAccess, uuid);

        assertNotNull(retrieved);

        Set<UUID> ids = retrieved.getCompositionIds();
        UUID composerId = retrieved.getComposition(ids.toArray(new UUID[]{})[0]).getComposerId();
        String composerName = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerId).getPartyName();

        assertEquals("ludwig", composerName);
    }

    @Test
    public void testUpdateOPTWithParameters() throws Exception {

        EventContext eventContext = ContextHelper.createDummyContext();
//        I_ContextAccess contextAccess = I_ContextAccess.getNewInstance(testDomainAccess, eventContext);
//        UUID contextUUID = contextAccess.commit();

        String description = "test contribution";
        String templateId = "prescription.opt";
        Integer changeCode = 276; //Any Event

        long startTime = System.nanoTime();

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);

        long endtime = System.nanoTime();

        System.out.println("Retrieve contribution elapsed:" + (endtime - startTime));

        assertNotNull(contributionAccess);


        PartyIdentified composer = CompositionAttributesHelper.createComposer("ludwig", "NHS-UK", "999999-9991");
        Composition aComposition = RmObjectHelper.createDummyQualifiedCompositionWithParameters(
                testDomainAccess.getKnowledgeManager(),
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
        UUID uuid = contributionAccess.commit();

        assertNotNull(uuid);

        //retrieveInstanceByNamedSubject the contribution
        I_ContributionAccess retrieved = I_ContributionAccess.retrieveInstance((I_DomainAccess) contributionAccess, uuid);

        assertNotNull(retrieved);

        Set<UUID> ids = retrieved.getCompositionIds();
        UUID retrievedCompositionId = ids.toArray(new UUID[]{})[0];
        UUID composerId = retrieved.getComposition(retrievedCompositionId).getComposerId();
        String composerName = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerId).getPartyName();

        assertEquals("ludwig", composerName);

        composer = CompositionAttributesHelper.createComposer("wolfgang", "NHS-UK", "999999-9992");
        UUID newComposerId = I_PartyIdentifiedAccess.getOrCreateParty(testDomainAccess, composer);

        retrieved.getComposition(retrievedCompositionId).setComposerId(newComposerId);
        Boolean result = retrieved.update(true);

        //2nd updateComposition...
        composer = CompositionAttributesHelper.createComposer("joan-sebastian", "NHS-UK", "999999-9993");
        newComposerId = I_PartyIdentifiedAccess.getOrCreateParty(testDomainAccess, composer);

        retrieved.getComposition(retrievedCompositionId).setComposerId(newComposerId);
        result = retrieved.update(true);

        //try to get the second version
        I_CompositionAccess historicalCompositionAccess1 = I_CompositionAccess.retrieveCompositionVersion(testDomainAccess, retrievedCompositionId, 2);

        assertNotNull(historicalCompositionAccess1);

    }

    @Test
    public void testInOutCanonicalXML() throws Exception {
        String description = "test contribution";
//        String templateId = "MDT Output Report.opt";
//        Integer changeCode = 276; //Any Event
//        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(testDomainAccess, ehrIdUUID, systemUUID, composerUUID, description, changeCode, ContributionDef.ContributionType.COMPOSITION, ContributionDef.ContributionState.INCOMPLETE);
//
//        assertNotNull(contributionAccess);

//        InputStream is = new FileInputStream(new File("/Development/Dropbox/eCIS_Development/samples/RIPPLE_conformanceTesting_RAW.xml"));
        InputStream is = new FileInputStream(new File("/Development/Dropbox/eCIS_Development/samples/IDCR-LabReportRAW1.xml"));
//        InputStream is = new FileInputStream(new File("/Development/Dropbox/eCIS_Development/samples/IDCR - Generic MDT RAW XML.xml"));
        I_ContentBuilder content = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, null);
        Composition composition = content.importCanonicalXML(is);
        content.setTemplateId(composition.getArchetypeDetails().getTemplateId().getValue());

        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, composition, DateTime.now(), ehrIdUUID);
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, composition.getArchetypeDetails().getTemplateId().getValue(), 0, compositionAccess.getId(), composition);
        compositionAccess.addContent(entryAccess);

//        contributionAccess.addComposition(compositionAccess);

        //commit this composition
        UUID uuid = compositionAccess.commit();

        assertNotNull(uuid);

        //=============== retrieve the composition and set it up as a canonical XML
        //retrieveInstanceByNamedSubject the contribution
        I_CompositionAccess retrieved = I_CompositionAccess.retrieveInstance((I_DomainAccess) compositionAccess, uuid);

        uuid = retrieved.getId();

        //get the first composition
//        I_CompositionAccess compositionAccess1 = retrieved.getComposition(compositionUuids.toArray(new UUID[]{})[0]);

        Composition retrievedComposition = retrieved.getContent().get(0).getComposition();

//        ElementWrapper elementWrapper = (ElementWrapper)retrievedComposition.itemAtPath("/content[openEHR-EHR-SECTION.history_rcp.v1]/items[openEHR-EHR-EVALUATION.reason_for_encounter.v1]/data[at0001]/items[at0004]");
//
//        //hack this wrapper and pretend it is a non modified value
//        elementWrapper.setDirtyBit(false);

        byte[] exportXml = content.exportCanonicalXML(retrievedComposition, true);

        assertNotNull(exportXml);

        System.out.println(new String(exportXml));

        //storeComposition a temp file and write the exported XML
        File tempfile = File.createTempFile("CXML", ".xml");

        FileUtils.writeStringToFile(tempfile, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + new String(exportXml));

        //do some (insignificant) update...
        retrieved.update(true);
        retrieved.update(true);
        retrieved.update(true);

        //find the last version number
        Integer version = I_CompositionAccess.getLastVersionNumber((I_DomainAccess)compositionAccess, uuid);

        assertEquals((Integer)4, version);

        //retrieve the first version
        I_CompositionAccess retrievedPrevious = I_CompositionAccess.retrieveCompositionVersion((I_DomainAccess) compositionAccess, uuid, 2);

        System.out.println(retrievedPrevious.getContributionAccess().getChangeTypeLitteral());

        uuid = retrievedPrevious.getId();

        assertNotNull(uuid);

        Composition retrievedPreviousVersion = retrievedPrevious.getContent().get(0).getComposition();


        //do an explicit delete
        retrieved.delete();

   }



}