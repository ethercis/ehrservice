package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.I_FeederAuditAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.jooq.pg.tables.FeederAudit;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.common.archetyped.FeederAuditDetails;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.encapsulated.DvEncapsulated;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeederAuditTest extends AccessTestCase {
    FeederAudit feederAudit;

    @Before
    public void setUp() throws Exception {

        setupDomainAccess();
    }


    org.openehr.rm.common.archetyped.FeederAudit dummyFeederAudit() {
        List<DvIdentifier> originatingSystemIds = new ArrayList<>();
        List<DvIdentifier> feederSystemItemIds = new ArrayList<>();

        String aUid = "dfa84168-05d3-11e9-8eb2-f2801f1b9fd1";

        for (int i = 0; i < 4; i++) {
            originatingSystemIds.add(new DvIdentifier("o-issuer-" + i, "o-assigner" + i, aUid, "o-type" + i));
            feederSystemItemIds.add(new DvIdentifier("f-issuer-" + i, "f-assigner" + i, aUid, "f-type" + i));
        }

        FeederAuditDetails originatingSystemAudit = new FeederAuditDetails(
                "originatingSystemAudit",
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "provider", null),
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "location", null),
                new DvDateTime("2018-12-18T11:08:00Z"),
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "subject", null),
                "version-1");

        FeederAuditDetails feederSystemAudit = new FeederAuditDetails(
                "feederSystemAudit",
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "provider", null),
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "location", null),
                new DvDateTime("2018-12-18T11:08:00Z"),
                new PartyIdentified((new PartyRef(new HierObjectID(aUid), "type")), "subject", null),
                "version-1");

        DvEncapsulated originalContent = new DvParsable("a_value", "a_formalism111");

        return new org.openehr.rm.common.archetyped.FeederAudit(
                originatingSystemAudit,
                originatingSystemIds,
                feederSystemAudit,
                feederSystemItemIds,
                originalContent);
     }

    @Test
    public void testCreateCommitFeederAudit() throws Exception {
        I_FeederAuditAccess feederAuditAccess = I_FeederAuditAccess.getInstance(testDomainAccess, dummyFeederAudit());

        UUID uid = feederAuditAccess.commit();

        I_FeederAuditAccess retrieved = I_FeederAuditAccess.retrieveInstance(testDomainAccess, uid);

        assertNotNull(retrieved);

        org.openehr.rm.common.archetyped.FeederAudit feederAudit = retrieved.mapRmFeederAudit();

        assertNotNull(feederAudit);
        //house keeping
        retrieved.delete();
    }
}