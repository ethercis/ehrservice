package com.ethercis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.ethercis.dao.access.handler.PvCompoHandler;
import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.openehr.rm.composition.Composition;

public class CarePlanTest {

    private final DataAccess dataAccess = TestDataAccess.newInstance();

    public CarePlanTest() throws Exception {
    }

    @Test
    public void testCarePlan() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        Map<String, Object> updateValues = new HashMap<>();

        final HashMap<String, Object> values = new HashMap<>();
        values.put("/category", "openehr::433|event|");
        values.put("/territory", "GB");
        values.put("/language", "en");

        values.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0002]",
                "My Care Plan"
        );

        values.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/guideline",
                "local::test"
        );

        values.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0011]",
                "A test care plan"
        );

        values.put(
                "/content[openEHR-EHR-EVALUATION.careplan.v1]/data[at0001]/items[at0021]",
                "local::at0024|Active|"
        );

        values.put(
                "/content[openEHR-EHR-INSTRUCTION.procedure.v1]/activities[at0001]/description[at0002]/items[at0003]",
                "foo"
        );

        values.put(
                "/content[openEHR-EHR-INSTRUCTION.procedure.v1]/activities[at0001]/description[at0002]/items[at0012]",
                "Test name"
        );

        final PvCompoHandler handler = new PvCompoHandler(dataAccess, "careplan.v1", null);
        final UUID compositionId = handler.storeComposition(ehrId, values, null, null, null);

        final I_CompositionAccess access = I_CompositionAccess
                .retrieveInstance(dataAccess, compositionId);

        assertEquals("en", access.getLanguageCode());
        assertEquals(Integer.valueOf(826), access.getTerritoryCode());

        final List<I_EntryAccess> content = access.getContent();
        assertEquals(1, content.size());

        final Composition composition = content.get(0).getComposition();
        assertFalse(composition.isPersistent());

        final Map<String, String> stringStringMap = new EcisFlattener().render(composition);
    }
}