package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.datatypes.basic.DvIdentifier;

import java.util.List;
import java.util.UUID;

public class PartyIdentifiedAccessTest extends AccessTestCase {

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }

    @Test
    public void testPartyIdentifiedAccess() throws Exception {
//        UUID testpatient = TestHelper.createDummyPatient(testDomainAccess);
        I_PartyIdentifiedAccess partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(testDomainAccess, "TEST PARTY");
        partyIdentifiedAccess.addIdentifier("999999-345", "NHS-UK", "NHS-UK", "2.16.840.1.113883.2.1.4.3");

        UUID id = partyIdentifiedAccess.commit();

        //retrieveInstanceByNamedSubject new partyIdentified
        I_PartyIdentifiedAccess newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, id);

        assertEquals("TEST PARTY", newPartyIdentifiedAccess.getPartyName());

        //retrieve it using its identifier
        UUID retrieved = I_PartyIdentifiedAccess.retrievePartyByIdentifier(testDomainAccess, "999999-345", "NHS-UK");
        newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, retrieved);
        assertEquals("TEST PARTY", newPartyIdentifiedAccess.getPartyName());

        newPartyIdentifiedAccess.setPartyName("TEST UPDATE");
        assertTrue(newPartyIdentifiedAccess.update());

        newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, id);
        assertEquals("TEST UPDATE", newPartyIdentifiedAccess.getPartyName());

        //delete
        int count = I_PartyIdentifiedAccess.deleteInstance(testDomainAccess, id);
        assertEquals(1, count);
    }

    @Test
    public void testUpdate(){
//        I_GenericAccess partyIdentifiedAccess = new getNewInstance(DSL.using(connection, SQLDialect.POSTGRES));

    }

    public void testRetrieveIdentifiers() throws Exception {
        //storeComposition a new entry
        I_PartyIdentifiedAccess partyIdentifiedAccess = null;
        try {
            partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(testDomainAccess, "Patient 3");
            partyIdentifiedAccess.commit();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception "+e);
        }

        //add some identifiers
        partyIdentifiedAccess.addIdentifier("12345", "test issuer", "test assigner", "test id1");
        partyIdentifiedAccess.addIdentifier("56789", "test issuer", "test assigner", "test id2");
        try {
            partyIdentifiedAccess.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<DvIdentifier> result = I_PartyIdentifiedAccess.getPartyIdentifiers(testDomainAccess, partyIdentifiedAccess.getId());

        assertEquals(2, result.size());

        partyIdentifiedAccess.delete();

    }

    public void testCreateAndDelete() throws Exception {

        I_PartyIdentifiedAccess partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(testDomainAccess, "GUINEA PIG");

        partyIdentifiedAccess.addIdentifier("12345", "test issuer", "test assigner", "test id1");
        partyIdentifiedAccess.addIdentifier("56789", "test issuer", "test assigner", "test id2");

        UUID id = null;
        try {
            id = partyIdentifiedAccess.commit();
        } catch (Exception e) {
            Assert.fail("could not commit record");
        }

        //retrieve the instance
        I_PartyIdentifiedAccess newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, id);

        String[] keys = newPartyIdentifiedAccess.getIdentifiersKeySet();

        assertEquals(2, keys.length);

        //delete
        partyIdentifiedAccess.delete();

        //not there...
        newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, id);

        assertNull(newPartyIdentifiedAccess);
    }

    public void testGetOrCreate() throws Exception {
        UUID party = I_PartyIdentifiedAccess.getOrCreateParty(testDomainAccess, "TEST_PARTY", "12345", "testIssuer", "testAssigner", "testType");
        assertNotNull(party);

        //retrieve the instance
        I_PartyIdentifiedAccess newPartyIdentifiedAccess = I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, party);

        String[] keys = newPartyIdentifiedAccess.getIdentifiersKeySet();

        assertEquals(1, keys.length);

        newPartyIdentifiedAccess.delete();
    }
}