package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.*;
import com.ethercis.dao.access.jooq.EhrAccess;
import com.ethercis.dao.access.jooq.PartyIdentifiedAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.RmObjectHelper;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.ehr.building.I_ContentBuilder;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Before;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.schemas.v1.ITEMSTRUCTURE;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.System;
import java.util.Map;
import java.util.UUID;

public class EhrAccessTest extends AccessTestCase {

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }


    public void _testCommit() throws Exception {

        //storeComposition a dummy ehr for a not less dummy patient
        EhrAccess ehrAccess = new EhrAccess(context, new PartyIdentifiedAccess(context).retrieve("Nurse 1"), null, null, null);

        UUID ehrUuid = ehrAccess.commit();

        assertNotNull(ehrUuid);
    }

    public void _testRetrieveBySubject() throws Exception {
        String identifier = "250-15489609345890393434-76";
        String issuer = "CNDA-INS-A";

        UUID ehrId = I_EhrAccess.retrieveInstanceBySubject(testDomainAccess, identifier, issuer);

        assertNotNull(ehrId);

        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        assertNotNull(ehrAccess);

        Map<String, String> idlist = I_EhrAccess.fetchSubjectIdentifiers(testDomainAccess, ehrId);

        assertFalse(idlist.isEmpty());

    }

//    public void testDeleteQuickNDirty(){
//        //test Ehr Delete, ... the hard coded way
//        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, UUID.fromString("bb650f95-a6f7-4513-9e9c-5defb32a096b"));
//        ehrAccess.delete();
//    }

    //Create Update Delete #1
    public void _testCUD_1() throws Exception {
        //storeComposition a new patient
        I_PartyIdentifiedAccess partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(testDomainAccess, "Randle McMurphy");
        UUID patientId = null;
        try {
            patientId = partyIdentifiedAccess.commit();
        } catch (Exception e) {
            Assert.fail("Could not storeComposition patient");
        }

        //storeComposition Ehr for Randle
        I_EhrAccess ehrAccess = I_EhrAccess.getInstance(testDomainAccess,
                patientId,
                I_SystemAccess.retrieveInstanceId(testDomainAccess, "1234|1234"),
                null,
                null);
        UUID ehrId = ehrAccess.commit();

        //updateComposition
        //1. parameters in status
        ehrAccess.setModifiable(false);
        ehrAccess.setQueryable(false);

        UUID newSystemId = I_SystemAccess.retrieveInstanceId(testDomainAccess, "4567|4567");
        ehrAccess.setSystem(newSystemId);

        ehrAccess.update();

        ehrAccess.reload();

        //check value changes
        assertFalse(ehrAccess.isModifiable());
        assertFalse(ehrAccess.isQueryable());

        assertEquals(ehrAccess.getSystemId(), newSystemId);

        //delete this ehr
        ehrAccess.delete();

        //gone?
        assertNull(I_EhrAccess.retrieveInstance(testDomainAccess, ehrId));

        //delete the dummy patient
        partyIdentifiedAccess.delete();

    }

    public void _testCUD_2() throws Exception {
        UUID systemId = TestHelper.createDummySystem(testDomainAccess);
        UUID patientId = TestHelper.createDummyPatient(testDomainAccess);
        UUID committerId = TestHelper.createDummyCommitter(testDomainAccess);

        UUID ehrId = TestHelper.createDummyEhr(testDomainAccess, patientId, systemId);

        //storeComposition a dummy contribution
        UUID contributionId = TestHelper.createDummyContribution(testDomainAccess, ehrId, systemId, committerId);
        UUID contributionVersionId = I_ContributionAccess.retrieveInstance(testDomainAccess, contributionId).getContributionVersionId();

        //check if deleting ehr effectively delete the contribution and its version record

        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        assertNotNull(ehrAccess);

        ehrAccess.delete();

        ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        assertNull(ehrAccess);

        //check for the contribution objects
        I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveInstance(testDomainAccess, contributionId);

        assertNull(contributionAccess);

        //delete the dummy parties
        assertEquals(1, (int)(I_SystemAccess.retrieveInstance(testDomainAccess, systemId).delete()));
        assertEquals(1, (int) (I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, patientId).delete()));
        assertEquals(1, (int) (I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, committerId).delete()));


    }

    public void testCUD_3() throws Exception {
        String description = "test contribution";
        String templateId = "section  observation test";
        UUID systemId = TestHelper.createDummySystem(testDomainAccess);
        UUID patientId = TestHelper.createDummyPatient(testDomainAccess);
        UUID committerId = TestHelper.createDummyCommitter(testDomainAccess);

        UUID ehrId = TestHelper.createDummyEhr(testDomainAccess, patientId, systemId);

        //storeComposition a dummy contribution
        UUID contributionId = TestHelper.createDummyContribution(testDomainAccess, ehrId, systemId, committerId);
        UUID contributionVersionId = I_ContributionAccess.retrieveInstance(testDomainAccess, contributionId).getContributionVersionId();

        //check if deleting ehr effectively delete the contribution and its version record

        I_EhrAccess ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);


        //storeComposition a composition
        //storeComposition a composition to add to this contribution
        UUID contextUUID = TestHelper.createDummyEventContext(testDomainAccess);
        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(testDomainAccess, "en", "TH", DateTime.now(), contextUUID, committerId, ehrId);

        Composition aComposition = RmObjectHelper.createDummyComposition(templateId);

        //add content
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, compositionAccess.getId(), aComposition);
        compositionAccess.addContent(entryAccess);
        compositionAccess.commit();

        //add composition to contribution
        I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveInstance(testDomainAccess, contributionId);
        contributionAccess.addComposition(compositionAccess);
        compositionAccess.update();

        //delete Ehr
        ehrAccess.delete();

        ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        assertNull(ehrAccess);

        I_SystemAccess.delete(testDomainAccess, systemId);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, patientId).delete();
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, committerId).delete();
    }

    /**
     * test other details in ehr status
     * @throws Exception
     */
    public void testCUD_4() throws Exception {
        String otherDetailsTemplateId = "person anonymised parent";
        UUID systemId = TestHelper.createDummySystem(testDomainAccess);
        UUID patientId = TestHelper.createDummyPatient(testDomainAccess);
        UUID committerId = TestHelper.createDummyCommitter(testDomainAccess);

        InputStream is = new FileInputStream(new File("/Development/Dropbox/eCIS_Development/samples/other_details.xml"));


        I_EhrAccess ehrAccess = I_EhrAccess.getInstance(testDomainAccess,
                patientId,
                systemId,
                null,
                null);

        Locatable otherDetails = I_ContentBuilder.parseOtherDetailsXml(is);

        ehrAccess.setOtherDetails(otherDetails, otherDetailsTemplateId);
        UUID ehrId = ehrAccess.commit();

        //check if deleting ehr effectively delete the contribution and its version record

        ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        //display the retrieved other_details
        String exportedXml = ehrAccess.exportOtherDetailsXml();
        if (exportedXml != null) {
            System.out.println(exportedXml);
        }

         //delete Ehr
        ehrAccess.delete();

        ehrAccess = I_EhrAccess.retrieveInstance(testDomainAccess, ehrId);

        assertNull(ehrAccess);

        I_SystemAccess.delete(testDomainAccess, systemId);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, patientId).delete();
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, committerId).delete();
    }
}