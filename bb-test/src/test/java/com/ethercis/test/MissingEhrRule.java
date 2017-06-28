package com.ethercis.test;

import static org.junit.Assert.assertFalse;

import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.support.DataAccess;
import java.util.UUID;
import org.junit.rules.ExternalResource;

/**
 * Rule which makes sure a subject exists and this subject does not have an EHR.
 */
public final class MissingEhrRule extends ExternalResource {

    private final DataAccess dataAccess;
    private UUID subjectId;

    public MissingEhrRule(final DataAccess dataAccess) {
        super();
        this.dataAccess = dataAccess;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    @Override
    protected void before() throws Throwable {
        // EHR_STATUS#subject
        subjectId = I_PartyIdentifiedAccess.getOrCreatePartyByExternalRef(
                dataAccess, "Test user", "666", "test", "test", "test"
        );

        EhrTestUtils.ensureEhrIsMissing(dataAccess, subjectId);

        assertFalse(
                "Unable to delete EHR",
                I_EhrAccess.checkExist(dataAccess, subjectId)
        );
    }

}
