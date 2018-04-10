package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.handler.PvCompoHandler;
import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.dao.access.support.TestHelper;
import com.ethercis.dao.access.util.CompositionUtil;
import com.ethercis.ehr.json.FlatJsonUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class OtherContextTest extends AccessTestCase {
    Map<String, Object> kvPairs = new HashMap<>();

    private UUID ehrIdUUID;
    private UUID systemUUID;
    private UUID composerUUID;
    String templateId = "UK AoMRC Outpatient Letter";

    long start, end;

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
        ehrIdUUID = TestHelper.createDummyEhr(testDomainAccess);
        systemUUID = TestHelper.createDummySystem(testDomainAccess);
        composerUUID = TestHelper.createDummyCommitter(testDomainAccess);
        FileReader fileReader = new FileReader("src/test/resources/samples/AOMRC GENERIC OUTPATIENT LETTER.json");

        kvPairs = FlatJsonUtil.inputStream2Map(fileReader);

    }

    @After
    public void tearDown() throws Exception {
        I_EhrAccess.retrieveInstance(testDomainAccess, ehrIdUUID).delete();
        I_SystemAccess.delete(testDomainAccess, systemUUID);
        I_PartyIdentifiedAccess.retrieveInstance(testDomainAccess, composerUUID).delete();
    }


    /**
     * test selective updates: Context, Attributes, Content
     * @throws Exception
     */
    @Test
    public void testUpdateEcisFLATContext() throws Exception {

        PvCompoHandler pvCompoHandler = new PvCompoHandler(testDomainAccess, templateId, null);
        //commit this contribution
        start = System.nanoTime();
//        UUID uuid = commitNewTestComposition();
        UUID uuid = pvCompoHandler.storeComposition(ehrIdUUID, kvPairs);
        end = System.nanoTime();
        System.out.println("INITIAL CREATION TIME:" + (end - start) / 1000000 + "[ms]");
        assertNotNull(uuid);

        start = System.nanoTime();
        //retrieveInstanceByNamedSubject the contribution
        I_CompositionAccess compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, uuid);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(1):" + (end - start) / 1000000 + "[ms]");
        assertNotNull(compositionAccess);
        System.out.println(CompositionUtil.dumpFlat(compositionAccess));

        //update other_context
        kvPairs.clear();
        kvPairs.put("/context/other_context[at0001]/items[at0005]", "incomplete");
        kvPairs.put("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.individual_personal_uk.v1]/items[openEHR-EHR-CLUSTER.telecom_uk.v1]/items[at0002]", "unstructured telephone");

        pvCompoHandler.updateComposition(kvPairs, null, null, null);
        compositionAccess = I_CompositionAccess.retrieveInstance(testDomainAccess, uuid);
        end = System.nanoTime();
        System.out.println("RETRIEVAL TIME(1):" + (end - start) / 1000000 + "[ms]");
        assertNotNull(compositionAccess);
        System.out.println(CompositionUtil.dumpFlat(compositionAccess));

    }
}
