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

public class BMITest {

    private final DataAccess dataAccess = TestDataAccess.newInstance();

    public BMITest() throws Exception {
    }

    @Test
    public void testBmi() throws Exception {
        final UUID subjectId = SubjectTestUtils.ensureTestSubjectExists(dataAccess);
        final UUID ehrId = EhrTestUtils.ensureEhrExists(dataAccess, subjectId);

        Map<String, Object> updateValues = new HashMap<>();

        final HashMap<String, Object> values = new HashMap<>();
        values.put("/category", "openehr::433|event|");
        values.put("/territory", "GB");
        values.put("/language", "en");

        values.put(
                "/content[openEHR-EHR-OBSERVATION.body_mass_index.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value",
                "18.5,kg/m2");
        values.put(
                "/content[openEHR-EHR-OBSERVATION.body_weight.v1]/data[at0002]/events[at0003]/data[at0001]/items[at0004]|value",
                "65.2,kg");
        values.put(
                "/content[openEHR-EHR-OBSERVATION.height.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value",
                "167,cm");

        final PvCompoHandler handler = new PvCompoHandler(dataAccess, "BMI", null);
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

        assertEquals("18.5,kg/m2", stringStringMap
                .get("/content[openEHR-EHR-OBSERVATION.body_mass_index.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value"));
        assertEquals("65.2,kg", stringStringMap
                .get("/content[openEHR-EHR-OBSERVATION.body_weight.v1]/data[at0002]/events[at0003]/data[at0001]/items[at0004]|value"));
        assertEquals("167,cm", stringStringMap
                .get("/content[openEHR-EHR-OBSERVATION.height.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0004]|value"));
    }
}