package com.ethercis.dao.jooq.impl;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.building.I_RmBinding;
import com.ethercis.ehr.building.OetBinding;
import com.ethercis.ehr.encode.EncodeUtil;
import com.google.gson.Gson;
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

        String query = "select\n" +
                "  \"ehr\".\"comp_expand\".\"entry\"->(select json_object_keys(\"ehr\".\"comp_expand\".\"entry\"::json)) #>> '{/content[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1],0,/items[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1],0,/data[at0001],/items[at0002],0,/value,value}' as \"cause\",\n" +
                "  \"ehr\".\"comp_expand\".\"entry\"->(select json_object_keys(\"ehr\".\"comp_expand\".\"entry\"::json)) #>> '{/content[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1],0,/items[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1],0,/data[at0001],/items[at0009],0,/items[at0011],0,/value,value}' as \"reaction\",\n" +
                "  \"ehr\".\"comp_expand\".\"composition_id\"||'::'||'test-server'||'::'||(\n" +
                "    select (count(*) + 1)\n" +
                "    from \"ehr\".\"composition_history\"\n" +
                "    where \"ehr\".\"composition_history\".\"id\" = '052541fd-8c32-4ef6-a2f1-69252b47b789'\n" +
                "  ) as \"uid\"\n" +
                "from \"ehr\".\"comp_expand\"\n" +
                "where (\n" +
                "  (\"ehr\".\"comp_expand\".\"composition_name\"='Adverse reaction list')\n" +
                "  and (\"ehr\".\"comp_expand\".\"subject_externalref_id_value\"='9999999000')\n" +
                ");";

        //perform the query
        Map<String, Object> map = I_EntryAccess.queryJSON(testDomainAccess, query);
        assertNotNull(map);

        Gson gson = EncodeUtil.getGsonBuilderInstance().setPrettyPrinting().create();

        System.out.print(gson.toJson(map));

//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        new org.codehaus.jackson.map.ObjectMapper().writer().writeValue(byteArrayOutputStream, map);
//        String bodyContent = byteArrayOutputStream.toString();
//
//        System.out.println(bodyContent);
    }

    @Test
    public void testAqlQuery() throws Exception {
        //more fun...

        String query = "SELECT c/uid/value,"+
                "c/name/value,"+
                "eval/data[at0001]/items[at0002]/value AS problem,\n" +
                "eval/data[at0001]/items[at0002]/defining_code/code_string AS code,\n" +
                "eval/data[at0001]/items[at0002]/defining_code/terminology_id/name AS terminology,\n" +
                "eval/data[at0001]/items[at0009]/value AS description,\n"+
                "eval/data[at0001]/items[at0077]/value AS onset,\n"+
                "eval/data[at0001] AS struct\n"+
                "FROM EHR e  \n" +
                "CONTAINS COMPOSITION c \n" +
                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1]" +
                "WHERE c/uid/value = '08fd487b-765a-41b4-9501-334d48dc2b00::test::1'";

        //perform the query
        Map<String, Object> map = I_EntryAccess.queryAqlJson(testDomainAccess, query);

//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        new org.codehaus.jackson.map.ObjectMapper().writer().writeValue(byteArrayOutputStream, map);
//        String bodyContent = byteArrayOutputStream.toString();
//
//        System.out.println(bodyContent);
    }

}