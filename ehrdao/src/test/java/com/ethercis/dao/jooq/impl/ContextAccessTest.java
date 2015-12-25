package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.I_ContextAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.TestHelper;
import org.junit.Before;
import org.junit.Test;

public class ContextAccessTest extends AccessTestCase {
    org.openehr.rm.composition.EventContext eventContext;

    @Before
    public void setUp() throws Exception {

        setupDomainAccess();

        eventContext = TestHelper.createDummyEventContext();
    }


    @Test
    public void testCreateCommitContext() throws Exception {
        I_ContextAccess contextAccess = I_ContextAccess.getInstance(testDomainAccess, eventContext);

        contextAccess.commit();

        org.openehr.rm.composition.EventContext eventContext = contextAccess.mapRmEventContext();

        assertNotNull(eventContext);
    }
}