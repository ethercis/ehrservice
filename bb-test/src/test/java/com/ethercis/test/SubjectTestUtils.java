package com.ethercis.test;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import java.util.UUID;

public final class SubjectTestUtils {

    private SubjectTestUtils() {
    }

    public static UUID ensureTestSubjectExists(final I_DomainAccess dataAccess) {
        return I_PartyIdentifiedAccess.getOrCreatePartyByExternalRef(
                dataAccess, "Test user 3", "668", "test", "test", "test"
        );
    }

}
