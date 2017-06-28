package com.ethercis.test;

import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.support.DataAccess;
import java.util.UUID;
import org.junit.rules.ExternalResource;

/**
 * Rule which makes sure a subject exists and this subject does not have an EHR.
 */
public final class ExistingEhrRule extends ExternalResource {

    private final DataAccess dataAccess;
    private UUID subjectId;
    private UUID ehrId;

    public ExistingEhrRule(final DataAccess dataAccess) {
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
                dataAccess, "Test user 2", "667", "test", "test", "test"
        );

        ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);
    }

    public UUID getEhrId() {
        return ehrId;
    }

}
