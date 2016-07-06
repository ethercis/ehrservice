package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_RmBinding;
import com.ethercis.ehr.building.OetBinding;
import junit.framework.Assert;
import openEHR.v1.template.TEMPLATE;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.FlattenerNew;
import org.openehr.rm.composition.Composition;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

public class EntryAccessTest extends AccessTestCase {
    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }

    public void testCreateObservation() throws Exception {
        String templateId = "section  observation test";
        FlattenerNew flattener = new FlattenerNew();


        TEMPLATE composition = knowledge.retrieveOpenehrTemplate(templateId);

        Archetype instance = flattener.toFlattenedArchetype(composition, knowledge.getArchetypeMap());

        Assert.assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Composition actualComposition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(testDomainAccess, templateId, 0, UUID.randomUUID(), actualComposition);

        UUID id  = entryAccess.commit(new Timestamp(DateTime.now().getMillis()));

        long startTime = System.nanoTime();
        //retrieveInstanceByNamedSubject the entry
        I_EntryAccess newEntryAccess = I_EntryAccess.retrieveInstance((I_DomainAccess) entryAccess, id);
        long endtime = System.nanoTime();
        System.out.println("Retrieve entry  elapsed [ms]:"+(endtime - startTime)/1000000);
        assertNotNull(newEntryAccess);

        //do a quick updateComposition...
        newEntryAccess.setSequence(1234);
        //not really a new composition, but test the updateComposition...

        newEntryAccess.setCompositionData(templateId, actualComposition);

        newEntryAccess.update();
    }

    @Test
    public void testQuery() throws Exception {
        //more fun...

        String query = "select * from (\n" +
                "select composition_id, entry.entry #>>  '{openEHR-EHR-COMPOSITION.section_observation_test.v2,\n" +
                "                          openEHR-EHR-SECTION.visual_acuity_simple_test.v1, 0,\n" +
                "                            at0025, 0,\n" +
                "                              openEHR-EHR-OBSERVATION.visual_acuity.v1, 0,\n" +
                "                                at0001, [events], \n" +
                "                                  at0002, 0, \n" +
                "                                     /time, /value, value}' AS event_time\n" +
                "  ,from ethercis.ehr.entry \n" +
                "\n" +
                "  where template_id LIKE 'section  observation test.oet'\n" +
                "                    and entry.entry #>>  '{openEHR-EHR-COMPOSITION.section_observation_test.v2,\n" +
                "                          openEHR-EHR-SECTION.visual_acuity_simple_test.v1, 0,\n" +
                "                            at0025, 0,\n" +
                "                              openEHR-EHR-OBSERVATION.visual_acuity.v1, 0,\n" +
                "                                at0001, [events], \n" +
                "                                  at0002, 0, \n" +
                "                                    at0003,\n" +
                "                                      at0053,\n" +
                "                                        at0028, 0,\n" +
                "                                          at0009, 0,\n" +
                "                                            /value, /value, value}' = '20/10') SNELLEN \n" +
                "\n" +
                "                    where event_time LIKE '2015-08-28T07%';";

        //perform the query
        Map<String, Object> map = I_EntryAccess.queryJSON(testDomainAccess, query);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new org.codehaus.jackson.map.ObjectMapper().writer().writeValue(byteArrayOutputStream, map);
        String bodyContent = byteArrayOutputStream.toString();

        System.out.println(bodyContent);
    }

    @Test
    public void testAqlQuery() throws Exception {
        //more fun...

        String query = "SELECT c/uid/value,"+
                "c/name/value,"+
                "eval/data[at0001]/items[at0002]/value AS problem,\n" +
                "eval/data[at0001]/items[at0002]/defining_code/code_string AS code,\n" +
                "eval/data[at0001]/items[at0002]/defining_code/terminology_id/name AS code,\n" +
                "eval/data[at0001]/items[at0009]/value AS description,\n"+
                "eval/data[at0001]/items[at0077]/value AS onset,\n"+
                "eval/data[at0001] AS struct\n"+
                "FROM EHR e  \n" +
                "CONTAINS COMPOSITION c \n" +
                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1]" +
                "WHERE c/uid/value = '08fd487b-765a-41b4-9501-334d48dc2b00::test::1'";

        //perform the query
        Map<String, Object> map = I_EntryAccess.queryAqlJson(testDomainAccess, query);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new org.codehaus.jackson.map.ObjectMapper().writer().writeValue(byteArrayOutputStream, map);
        String bodyContent = byteArrayOutputStream.toString();

        System.out.println(bodyContent);
    }

}