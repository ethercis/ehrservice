package com.ethercis.test;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import java.util.UUID;

public final class EhrTestUtils {

    private EhrTestUtils() {
    }

    /**
     * Make sure the given subject does not have an EHR.
     */
    public static void ensureEhrIsMissing(final I_DomainAccess dataAccess, final UUID subjectId)
            throws Exception {
        if (I_EhrAccess.checkExist(dataAccess, subjectId)) {
            final UUID uuid = I_EhrAccess.retrieveInstanceBySubject(dataAccess, subjectId);
            final I_EhrAccess existing = I_EhrAccess.retrieveInstance(dataAccess, uuid);
            existing.delete();
        }

    }

    /**
     * Make sure the given subject does have an EHR.
     */
    public static UUID ensureEhrExists(final I_DomainAccess dataAccess, final UUID subjectId)
            throws Exception {
        if (I_EhrAccess.checkExist(dataAccess, subjectId)) {
            return I_EhrAccess.retrieveInstanceBySubject(dataAccess, subjectId);
        } else {
            final I_EhrAccess instance =
                    I_EhrAccess.getInstance(dataAccess, subjectId, null, null, null);

            return instance.commit();
        }
    }
}
