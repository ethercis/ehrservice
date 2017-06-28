package com.ethercis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ethercis.dao.access.interfaces.I_ContributionAccess;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.LocatableBuilder;
import com.ethercis.ehr.building.OptContentBuilder;
import com.ethercis.ehr.keyvalues.PathValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.jooq.exception.DataAccessException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

public final class EhrTest {

    private final DataAccess dataAccess = TestDataAccess.newInstance();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public EhrTest() throws Exception {
    }

    /**
     * Test the normal flow.
     */
    @Test
    public void createEhr() throws Exception {
        // Normal flow for creating an EHR:
        //   1. parse the body as json (if exists)
        //   2. extract /otherDetails and /otherDetailsTemplateId from body (if exists)
        //   3. determine subject ID using
        //      * I_PartyIdentifiedAccess.retrievePartyByIdentifier or
        //      * I_PartyIdentifiedAccess.getOrCreatePartyByExternalRef
        //   4. determine system ID using
        //      * I_SystemAccess.retrieveInstanceId
        //      * keep it null for local system
        //   5. check if subject ID already has an EHR using I_EhrAccess.checkExist
        //   6. get a I_EhrAccess instance
        //   7. set otherDetails  using I_EhrAccess.setOtherDetails
        //   8. call I_EhrAccess.commit (but not the commit with a single Timestamp argument!)

        // subject must exist
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);

        // directory ID can be anything
        final UUID directoryId = UUID.randomUUID();

        // null means "use local system ID"
        final UUID systemId = null;

        // there does not seems to be a way to create an EHR_ACCESS
        final UUID accessId = null;

        //
        // create new EHR
        //

        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        I_EhrAccess ehr =
                I_EhrAccess.getInstance(dataAccess, subjectId, systemId, directoryId, accessId);



        assertTrue(ehr.isNew());

        final UUID ehrId = ehr.commit();

        assertNotNull("Unable to create EHR", ehrId);

        //
        // retrieve and validate EHR
        //

        ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);

        assertFalse(ehr.isNew());

        //
        // validate things stored in the EHR_STATUS
        //

        assertEquals(subjectId, ehr.getParty());
        assertTrue(ehr.isModifiable());
        assertTrue(ehr.isQueryable());

        //
        // validate EHR
        //

        assertEquals(I_SystemAccess.createOrRetrieveLocalSystem(dataAccess), ehr.getSystemId());
        assertEquals(directoryId, ehr.getDirectoryId());
        assertEquals(accessId, ehr.getAccessId());
        assertNull(ehr.getOtherDetails());
        assertNull(ehr.getOtherDetailsSerialized());
        assertNull(ehr.getOtherDetailsTemplateId());

        final Map<String, String> stringStringMap = I_EhrAccess
                .fetchSubjectIdentifiers(dataAccess, ehrId);

        System.err.print(stringStringMap);
    }

    @Test
    public void storeRandomStuffInOtherDetails() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        I_EhrAccess ehr =
                I_EhrAccess.getInstance(dataAccess, subjectId, null, null, null);

        ehr.setOtherDetails("{ \"foo\": \"bar\" }", "does not matter");

        final UUID ehrId = ehr.commit();

        ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);

        assertFalse(ehr.isSetOtherDetails());
        assertTrue(ehr.isSetOtherDetailsSerialized());
        assertNull(ehr.getOtherDetails());

        // this one is suprising: as other_details is set using some random JSON string, when read
        // the templateId is not detected and therefore set to null
        assertNull(ehr.getOtherDetailsTemplateId());

        assertEquals("{\"foo\": \"bar\"}", ehr.getOtherDetailsSerialized());
    }

    //@Test
    public void storeProperDataInOtherDetails() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        I_EhrAccess ehr =
                I_EhrAccess.getInstance(dataAccess, subjectId, null, null, null);

        final PathValue pathValue = new PathValue(dataAccess.getKnowledgeManager(),
                "EHR_STATUS test", new Properties());

        final HashMap<String, Object> map = new HashMap<>();
        map.put("/items[at0001]", "hello");
        final Composition assign = pathValue.assign(map);

        // TODO ????

    }

    /**
     * Invalid EHR ID.
     */
    @Test
    public void incorrectEhrId() throws Exception {
        final I_EhrAccess ehr = I_EhrAccess.retrieveInstance(dataAccess, UUID.randomUUID());

        assertNull(ehr);
    }


    /**
     * Create a new EHR for a subject which already has an EHR.
     */
    @Test
    public void createAnotherEhr() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("already associated to an EHR");

        final I_EhrAccess ehrAccess =
                I_EhrAccess.getInstance(dataAccess, subjectId, null, null, null);
    }

    /**
     * Invalid system ID.
     */
    @Test
    public void incorrectSystemId() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        final I_EhrAccess ehrAccess =
                I_EhrAccess.getInstance(dataAccess, subjectId, UUID.randomUUID(), null, null);

        exception.expect(DataAccessException.class);
        exception.expectMessage("is not present in table \"system\"");
        ehrAccess.commit();
    }

    /**
     * Invalid access ID.
     */
    @Test
    public void incorrectAccessId() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        final I_EhrAccess ehrAccess =
                I_EhrAccess.getInstance(dataAccess, subjectId, null, null, UUID.randomUUID());

        exception.expect(DataAccessException.class);
        exception.expectMessage("is not present in table \"access\"");
        ehrAccess.commit();
    }

    /**
     * Invalid subject ID.
     */
    @Test
    public void incorrectSubjectId() throws Exception {
        final UUID subjectId = UUID.randomUUID();
        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        final I_EhrAccess ehrAccess =
                I_EhrAccess.getInstance(dataAccess, subjectId, null, null, null);

        exception.expect(DataAccessException.class);
        exception.expectMessage("is not present in table \"party_identified\"");
        ehrAccess.commit();
    }

    /**
     * Delete an EHR.
     */
    @Test
    public void delete() throws Exception {

        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        final I_EhrAccess ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);
        final Integer result = ehr.delete();

        assertFalse(I_EhrAccess.checkExist(dataAccess, ehrId));

        // deleting again does not cause harm
        ehr.delete();

    }

    /**
     * This tests a bug where multiple I_EhrAccess instances all shared the same memory objects
     * making it impossible to access more than a single EHR simultaneously.
     *
     * T2623
     */
    @Test
    public void retrieveTwoEhrsAtOnce() throws Exception {
        //
        // create an empty EHR for subject 1
        //

        final UUID dir1 = UUID.randomUUID();
        final UUID user1 = I_PartyIdentifiedAccess
                .getOrCreatePartyByExternalRef(dataAccess, "User 1", "1000", "test",
                        getClass().getSimpleName(), "test");
        EhrTestUtils.ensureEhrIsMissing(dataAccess, user1);
        final UUID ehrId1 = I_EhrAccess.getInstance(dataAccess, user1, null, dir1, null).commit();

        //
        // create an empty EHR for subject 2
        //

        final UUID dir2 = UUID.randomUUID();
        final UUID user2 = I_PartyIdentifiedAccess
                .getOrCreatePartyByExternalRef(dataAccess, "User 2", "1001", "test",
                        getClass().getSimpleName(), "test");
        EhrTestUtils.ensureEhrIsMissing(dataAccess, user2);
        final UUID ehrId2 = I_EhrAccess.getInstance(dataAccess, user2, null, dir2, null).commit();

        //
        // retrieve those EHRs
        //

        final I_EhrAccess ehr1 = I_EhrAccess.retrieveInstance(dataAccess, ehrId1);
        final I_EhrAccess ehr2 = I_EhrAccess.retrieveInstance(dataAccess, ehrId2);

        // these used to fail because of the bug, ehr1 used the same data as ehr2
        assertEquals(user1, ehr1.getParty());
        assertEquals(dir1, ehr1.getDirectoryId());
        assertEquals(ehrId1, ehr1.getId());

        assertEquals(user2, ehr2.getParty());
        assertEquals(dir2, ehr2.getDirectoryId());
        assertEquals(ehrId2, ehr2.getId());
    }

    @Test
    public void modifyEhr() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        //
        // make some changes
        //

        I_EhrAccess ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);

        ehr.setQueryable(false);
        ehr.setModifiable(false);

        final UUID directory = UUID.randomUUID();
        ehr.setDirectory(directory);

        ehr.update();

        //
        // make sure the changes are stored
        //

        ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);

        assertFalse(ehr.isModifiable());
        assertFalse(ehr.isQueryable());
        assertEquals(directory, ehr.getDirectoryId());
    }

    /**
     * Committing an existing EHR will not update it but create a new one causing database
     * corruption due to a single subject having multiple EHRs.
     *
     * This test is currently disabled as it is not fixed. To recover from this, run
     * `./gradlew flywayClean flywayMigrate`.
     *
     * T2625
     */
    public void doubleCommit() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        I_EhrAccess ehr = I_EhrAccess.retrieveInstance(dataAccess, ehrId);
        assertFalse(ehr.isNew());

        ehr.commit();

        I_EhrAccess.retrieveInstance(dataAccess, ehrId);
    }
}
