/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ethercis.aql;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by christian on 2/2/2017.
 */
public class QueryEngineTest {

    protected DSLContext context;
    protected Connection connection;
    protected static String serverNodeId = System.getProperty("server.node.name") == null ? "test-server" : System.getProperty("server.node.name");

    private QueryEngine queryEngine;

    private List<Record> records;

    @Before
    public void setUp() {

        SQLDialect dialect = SQLDialect.valueOf("POSTGRES");
//        String url = "jdbc:postgresql://localhost:5434/ethercis";
        String url = "jdbc:postgresql://" + System.getProperty("test.db.host") + ":" + System.getProperty("test.db.port") + "/" + System.getProperty("test.db.name");
//        String url = "jdbc:postgresql://192.168.2.108:5432/ethercis";
        String login = System.getProperty("test.db.user");
        String password = System.getProperty("test.db.password");
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "src/test/resources/dummy");
        props.put("knowledge.path.template", "src/test/resources/dummy");
        props.put("knowledge.path.opt", "src/test/resources/dummy");
        props.put("knowledge.forcecache", "true");
        I_KnowledgeCache knowledge;
        try {
            knowledge = new KnowledgeCache(null, props);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not set knowledge cache:" + e);
        }

        try {
            this.connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            throw new IllegalArgumentException("SQL exception occurred while connecting:" + e);
        }

        if (connection == null)
            throw new IllegalArgumentException("Could not connect to DB");

        this.context = DSL.using(connection, dialect);

        queryEngine = new QueryEngine(context, serverNodeId, knowledge);
    }


    @Test
    public void testQryTopOrder() throws Exception {
        String query = "select top 10 " +
                "a/uid/value,\n" +
                "a/name/value,\n" +
                "a/context/start_time/value\n" +
                "from EHR e \n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "orderby a/context/start_time/value DESC";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEhr1() throws Exception {
        String query = "select " +
                "e/ehr_id/value as ehr_id, " +
                "e/ehr_status/subject/external_ref/id/value as subject_id, " +
                "e/ehr_status/subject/external_ref/namespace as subject_namespace " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEhr2() throws Exception {
        String query = "select e/ehr_id/value from ehr e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] contains composition";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEhr3() throws Exception {
        String query = "select e/ehr_id/value from EHR e contains composition";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEhr4() throws Exception {
        String query = "select e/ehr_id/value, " +
                "e/ehr_status/subject/external_ref/id/value as subject_id, " +
                "e/ehr_status/subject/external_ref/namespace as subject_namespace " +
                " from EHR e";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }
//

    //ticket #25
    @Test
    public void testQryEhr5() throws Exception {
        String query = "select e/ehr_id/value as ehr_id, a/uid/value as uid,\n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as dateCreated,\n" +
                "  a/name/value as documentType,\n" +
                "  a/archetype_details/template_id/value as documentTemplate\n" +
                "from EHR e \n" +
                "contains COMPOSITION a " +
                "where a/name/value matches {'Discharge summary', 'Referral'} \n" +
                "and a/archetype_details/template_id/value matches {'iEHR - Healthlink - Referral.v0','iEHR - Healthlink - Discharge Sumary.v0'}\n" +
                "order by  a/context/start_time/value desc";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryConditional1() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/context/start_time/value as date_created, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken, " +
                "c/items[at0002]/items[at0001]/name/value as what, " +
                "c/items[at0002]/items[at0001]/value/magnitude as value, " +
                "c/items[at0002]/items[at0001]/value/units as units " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1] " +
                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0] " +
                "contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]" +
                "where a/name/value='Laboratory test report' " +
                "AND e/ehr_status/subject/external_ref/id/value = '9999999000'";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryConditional3() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/context/start_time/value as date_created, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken, " +
                "c/items[at0002]/items[at0001]/name/value as what, " +
                "c/items[at0002]/items[at0001]/value/magnitude as value, " +
                "c/items[at0002]/items[at0001]/value/units as units " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1] " +
                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0] " +
                "contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]"+
                "where a/name/value='Laboratory test report' "+
                "AND e/ehr_status/subject/external_ref/id/value matches {'9999999000'}";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryConditional2() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a_a/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-EVALUATION.medication_statement_uk.v1]/data[at0001]/items[openEHR-EHR-CLUSTER.medication_item.v1]/items[at0001]/value/value as name, " +
                "a_a/items[openEHR-EHR-SECTION.current_medication_rcp.v1]/items[openEHR-EHR-EVALUATION.medication_statement_uk.v1]/data[at0001]/items[openEHR-EHR-CLUSTER.medication_item.v1]/items[at0020]/value/value as dose_amount " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
                "contains SECTION a_a[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1] " +
                "where a/name/value='Current medication list' ";
//                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
//                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    //this test fails since unspecified node ids are not supported
    @Test
    public void testQryInstruction() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/composer/name as author, " +
                "a/context/start_time/value as date_created, " +
                "a_a/activities/activities[at0001]/name/name as name_activities, " +
                "a_a/activities/activities[at0001]/timing/value as timing_activities, " +
                "a_a/activities/activities[at0001]/description[at0002]/items[at0070]/value/value as name, " +
                "a_a/activities/activities[at0001]/description[at0002]/items[at0109]/value/value as dose_amount, " +
                "a_a/activities/activities[at0001]/description[at0002]/items[at0055]/value/value as dose_timing, " +
                "a_a/activities/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value/value as start_date, " +
                "a_a/items/items/data[at0001]/items/items[at0001]/value/value as name, " +
                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/code_string as medication_code, " +
                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/terminology_id/value as medication_terminology, " +
                "a_a/items/items/data[at0001]/items/items[at0002]/value/defining_code/code_string as route, " +
                "a_a/items/items/data[at0001]/items/items[at0003]/value/value as dose_directions " +
                "from EHR e " +
                "contains COMPOSITION a " +
                "contains INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.medication_order.v0] " +
                "where a/name/value='Current medication list' " +
//                "and a/uid/value='" + medicationId + "' " +
//                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
                " AND e/ehr_status/subject/external_ref/id/value = '9999999000'";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertNotNull(records.get(0).getValue("name_activities"));
        System.out.print(records);
    }

    /**
     * TODO: see ticket #28, the expression a/context/start_time/value returns null
     *
     * @throws Exception
     */
    @Test
    public void testQryInstruction2() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/composer/name as author, " +
                "a/context/start_time/value as date_created, " +
                "a_a/activities[at0001]/name/value as name_activities, " +
                "a_a/activities[at0001]/timing/value as timing_activities, " +
                "a_a/activities[at0001]/description[at0002]/items[at0070]/value/value as name, " +
                "a_a/activities[at0001]/description[at0002]/items[at0109]/value/value as dose_amount, " +
                "a_a/activities[at0001]/description[at0002]/items[at0055]/value/value as dose_timing, " +
                "a_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value as start_date " +
                "from EHR e " +
                "contains COMPOSITION a " +
                "contains INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.medication_order.v0] " +
                "where a/name/value='Current medication list' " +
                " AND e/ehr_status/subject/external_ref/id/value = '9999999000'";
        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryInstruction3() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/composer/name as author, " +
                "a/context/start_time/value as date_created, " +
                "a_a/activities[at0001]/name/value as name_activities, " +
                "a_a/activities[at0001]/timing/value as timing_activities, " +
                "a_a/activities[at0001]/description[at0002]/items[at0070]/value/value as name, " +
                "a_a/activities[at0001]/description[at0002]/items[at0109]/value/value as dose_amount, " +
                "a_a/activities[at0001]/description[at0002]/items[at0055]/value/value as dose_timing, " +
                "a_a/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value as start_date " +
                "from EHR e " +
                "contains COMPOSITION a " +
                "contains INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.medication_order.v0] " +
                "where a/name/value='Current medication list' " +
                " AND e/ehr_status/subject/external_ref/id/value = '9999999000'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryAction1() throws Exception {
        String query = "select a/uid/value as uid, " +
                "a/composer/name as author, " +
                "e/ehr_status/subject/external_ref/id/value," +
                "a/context/start_time/value as date_submitted, " +
                "b_a/description[at0001]/items[at0002]/value/value as procedure_name, " +
                "b_a/description[at0001]/items[at0049, 'Procedure notes']/value/value as procedure_notes, " +
                "b_a/other_participations/performer/name as performer, " +
                "b_a/time/value as procedure_date, " +
                "b_a/ism_transition/careflow_step/value as status, " +
                "b_a/ism_transition/careflow_step/defining_code/code_string as status_code, " +
                "b_a/ism_transition/careflow_step/defining_code/terminology_id/value as terminology " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
                "contains ACTION b_a[openEHR-EHR-ACTION.procedure.v1] " +
                "where a/name/value='Procedures list' " +
//                "and a/uid/value='" + procedureId + "' " +
                "and e/ehr_status/subject/external_ref/id/value = '9999999000'";
//                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'";


        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryInstruction4() throws Exception {

        String query = "select a/uid/value as uid, " +
                "a/composer/name as author, " +
                "b_a/other_participations/performer/name as performer, " +
                "b_a/uid/value/value as instruction_uid, " +
                "b_a/narrative/value/value as narrative, " +
                "b_a/activities[at0001]/name/name as name_activities, " +
                "b_a/activities[at0001]/timing/value as timing_activities, " +
                "b_a/activities[at0001]/action_archetype_id as archetype_activities " +
                "from ehr e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1] " +
                "contains instruction b_a[openEHR-EHR-INSTRUCTION.request-procedure.v1] ";
//                "where a/name/value='Encounter' " +
//                "and b_a/activities/activities[at0001]/name/name = 'Request' " +
//                "and b_a/narrative/value/value = 'Human readable instruction narrative'";


        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEvaluation1() throws Exception {
        String query = "select  b_a/data[at0001]/items[at0002]/value/value as Causative_agent,     " +
                "a/name/value as name, " +
                "b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code,     " +
                "b_a/protocol[at0042]/items[at0062]/value/value as RecordedDate,     " +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation,     " +
                "a/uid/value as compositionId " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    //TODO: matches operator support
//    @Test
    public void testQryOtherDetails() throws Exception {
        String query = "select count(e) as cnt from EHR e" +
                "  contains COMPOSITION a" +
                "  contains EVALUATION a_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1]" +
                "  where a_a/data[at0001]/items[at0002]/value/defining_code/code_string matches {'456'}" +
                "  and a_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value = 'SNOMED-CT'" +
                "  and e/ehr_status/other_details/items[openEHR-EHR-CLUSTER.person_anonymised_parent.v1]/items[at0014]/value/value >= 1940" +
                "  and e/ehr_status/other_details/items[openEHR-EHR-CLUSTER.person_anonymised_parent.v1]/items[at0014]/value/value <= 1950";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    //TODO: check join statement
    @Test
    public void testQryOtherDetails2() throws Exception {
        String query = "select " +
                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0012]/value/value as date," +
                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0009]/value/value as status," +
                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0002]/value/value/definingCode/terminologyId/value as code," +
                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0002]/value/name as name " +
                "from EHR e[ehr_id/value='f2724b93-c9a6-49ff-ac52-11c0bd53b9a1'] ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

//    @Test
//    public void testQryCluster() throws Exception {
//        String query = "select e/ehr_id/value, " +
//                "c_a/items[at0053]/value/numerator as Percent_O2_numerator " +
//                "from EHR e contains COMPOSITION c contains CLUSTER c_a[openEHR-EHR-CLUSTER.person-name.v1]";
//
//        records = queryEngine.perform(query);
//        assertNotNull(records);
//        assertFalse(records.isEmpty());
//        System.out.print(records);
//    }

    @Test
    public void testQryEvaluation3() throws Exception {
        String query = "select" +
                "    b_a/data[at0001]/items[at0002] as frag," +
                "    b_a/data[at0001]/items[at0009]/items[at0011]/value/value as reaction," +
                "    a/uid/value as compositionId " +
                "from EHR e  " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list' " +
                "and e/ehr_status/subject/external_ref/id/value  = '9999999000' " +
                "and e/ehr_status/subject/external_ref/namespace = 'uk.nhs.nhs_number' ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    //TODO: enable queries on embedded arrays
    @Test
    public void testQryCluster1() throws Exception {
        String query = "select" +
                "   a/uid/value as uid,  " +
                "   a/context/start_time/value as date_created,  " +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name, " +
                "               a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken,  " +
                "               c/items[at0002]/items[at0001]/name/value as what,  " +
                "               c/items[at0002]/items[at0001]/value/magnitude as value,  " +
                "               c/items[at0002]/items[at0001]/value/units as units  from EHR e  " +
                "               contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]  " +
                "               contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]  " +
                "               contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]  ";
//                "               where a/name/value='Laboratory test report'  " +
//                "                   AND e/ehr_status/subject/external_ref/id/value = '9999999000'" +
//                "                   and c/items[at0002]/items[at0001]/name='Sodium'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryDescription1() throws Exception {
        String query = "select " +
                " a/uid/value as composition_uid, " +
                " a_i/narrative/value as narrative, " +
                " a/context/start_time/value as start_time, " +
                " a_i/activities[at0001]/timing/value as timing, " +
                " d/items[at0001]/value/value as medication_name, " +
                " d/items[at0003]/value/magnitude as medication_strength," +
                " d/items[at0003]/value/units as medication_units," +
                " d/items[at0057]/items[at0064]/items[at0065]/value/magnitude as offset, " +
                " d/items[at0057]/items[at0064]/items[at0065]/value/units as units, " +
                " d/items[at0057]/items[at0064]/items[at0067]/value/value as event" +
//                " from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9']" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.prescription.v1] " +
                " contains INSTRUCTION a_i[openEHR-EHR-INSTRUCTION.medication.v1] " +
                " contains DESCRIPTION d[openEHR-EHR-ITEM_TREE.medication_mod.v1]" +
                " order by start_time DESC ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEvaluation2() throws Exception {
        String query = "select " +
                "e/ehr_id/value as ehr_id, " +
                "e/ehr_status/subject/external_ref/id/value as subject_id, " +
                "e/ehr_status/subject/external_ref/namespace as subject_namespace " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationAND() throws Exception {
        String query = "select a/uid/value as uid, a/context/start_time/value as date_created,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units," +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude as rate," +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units as rate_units" +
                "       from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
                "                  contains COMPOSITION a\n" +
                "                  contains (OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
                "                  AND OBSERVATION o_hr[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1])" +
                "   where o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > 80 " +
                "   ORDERBY systolic ASC";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationGT() throws Exception {
        String query = "select a/uid/value as comp_id, a/context/start_time/value as date_created,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic," +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units" +
                "       from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
                "                  contains COMPOSITION a\n" +
                "                  contains OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
                "    where o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > 131\n" +
                "   ORDERBY date_created DESC";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationGT_LT() throws Exception {
        String query = "select a/uid/value as uid, a/context/start_time/value as date_created,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units,\n" +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude as rate,\n" +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units as rate_units\n" +
                "   from EHR e \n" +
                "                  contains COMPOSITION a\n" +
                "                  contains (OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
                "                  AND OBSERVATION o_hr[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1])  \n" +
                "                  where e/ehr_status/subject/external_ref/namespace='fr.asip.48221832'\n" +
                "                  and e/ehr_status/subject/external_ref/id/value='fr.asip.48221832'\n" +
                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude < 80\n" +
                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude > 130\n" +
                "                  ORDER BY date_created ASC ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEvaluation4() throws Exception {
        String query = "select a/uid/value as uid, \n" +
                "a/composer/name as author, \n" +
                "a/context/start_time/value as date_created, \n" +
                "b_a/data[at0001]/items[at0002]/value/value as cause, \n" +
                "b_a/data[at0001]/items[at0002]/value/defining_code/code_string as cause_code, \n" +
                "b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as cause_terminology, \n" +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value/value as reaction, \n" +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/codeString as reaction_code, \n" +
                "b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/terminology_id/value as reaction_terminology \n" +
                "from EHR e [ehr_id/value = 'bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]\n" +
                " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]\n" +
                " where a/name/value='Adverse reaction list'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservation() throws Exception {
        String query = "select a/uid/value as uid, a/composer/name\n" +
                "as author, a/context/start_time/value as date_created,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/name/value as labResultName,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/name/defining_code/code_string as labResultCode,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/value/magnitude as labResultValue,\n" +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/value/units as labResultUnits\n" +
                "from EHR e [ehr_id/value = 'cd8abecd-9925-4313-86af-93aab4930eae']\n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]\n" +
                "where a/name/value='Laboratory test report'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEvaluationOR() throws Exception {
        String query = "select a/uid/value as uid, a/composer/name as\n" +
                "author, a/context/start_time/value as date_created,\n" +
                "b_a/data[at0001]/items[at0003]/value/value as priority_place_of_care,\n" +
                "b_a/data[at0001]/items[at0015]/value/value as priority_place_of_death,\n" +
                "b_a/data[at0001]/items[at0029]/value/value as priority_comment,\n" +
                "b_b/data[at0001]/items[at0003]/value/value as treatment_decision,\n" +
                "b_b/data[at0001]/items[at0002]/value/value as treatment_date_of_decision,\n" +
                "b_b/data[at0001]/items[at0021]/value/value as treatment_comment,\n" +
                "b_c/data[at0001]/items[at0003]/value/value as cpr_decision,\n" +
                "b_c/data[at0001]/items[at0002]/value/value as cpr_date_of_decision,\n" +
                "b_c/data[at0001]/items[at0021]/value/value as cpr_comment from EHR e\n" +
                "[ehr_id/value = 'cd8abecd-9925-4313-86af-93aab4930eae'] contains\n" +
                "COMPOSITION a[openEHR-EHR-COMPOSITION.care_plan.v1] contains\n" +
                "(EVALUATION b_a[openEHR-EHR-EVALUATION.care_preference_uk.v1] or\n" +
                "EVALUATION b_b[openEHR-EHR-EVALUATION.advance_decision_refuse_treatment_uk.v1]\n" +
                "or EVALUATION b_c[openEHR-EHR-EVALUATION.cpr_decision_uk.v1])\n" +
                "where a/name/value='End of Life Patient Preferences'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    //TODO: check description values...
    @Test
    public void testQryAction() throws Exception {
        String query = "select\n" +
                " a/uid/value as uid,\n" +
                " a/composer/name as author,\n" +
                " a/context/start_time/value as date_created,\n" +
                " b_a/description[at0001]/items[at0011]/value/value as service_team,\n" +
                " b_a/description[at0001]/items[at0026]/value/lower/value as appointment_date,\n" +
                " b_a/protocol[at0015]/items[openEHR-EHR-CLUSTER.address.v1]/items[at0001]/items[at0002]/value/value\n" +
                "as location\n" +
                "from EHR e\n" +
                "  contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
                "  contains ACTION b_a[openEHR-EHR-ACTION.referral_uk.v1]\n" +
                "where a/name/value='Referral'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertNotNull(records.get(0).getValue("service_team"));
        System.out.print(records);
    }

    //TODO: b_a/name/value returns null!
    @Test
    public void testQryEvaluation() throws Exception {
        String query = "select\n" +
                "    a/uid/value as uid,\n" +
                "    a/archetype_details/template_id/value,\n" +
                "    a/composer/name as author,\n" +
                "    a/context/start_time/value as date_created,\n" +
                "    b_a/data[at0001]/items[at0002]/value/value as note,\n" +
                "    b_a/name/value as type\n" +
                "from EHR e\n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.clinical_synopsis.v1]\n" +
                "where a/name/value='Clinical Notes'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertNotNull(records.get(0).getValue("type"));
        System.out.print(records);
    }

    @Test
    public void testQryEvaluationOR2() throws Exception {
        String query = "select     \n" +
                "a/uid/value as uid,     \n" +
                "a/context/start_time/value as meeting_date,\n" +
                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/time/value as request_date,\n" +
                "a_a/protocol[at0008]/items[at0011]/value/value as service_team,\n" +
                "a_b/data[at0001]/items[at0004]/value/value as question,\n" +
                "a_c/data[at0001]/items[at0002]/value/value as notes, \n" +
                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string as careflow_step\n" +
                "from EHR e \n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report.v1]\n" +
                "contains ( \n" +
                " INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.request.v0]" +
                " or EVALUATION a_b[openEHR-EHR-EVALUATION.reason_for_encounter.v1]" +
                " or EVALUATION a_c[openEHR-EHR-EVALUATION.recommendation.v1])\n" +
                "where a/name/value='MDT Output Report'" +
                " and a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string = 'at0002'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryEvaluationOR3() throws Exception {
        String query = "select     \n" +
                "a/uid/value as uid,     \n" +
                "a/context/start_time/value as meeting_date,\n" +
                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/time/value as request_date,\n" +
                "a_a/protocol[at0008]/items[at0011]/value/value as service_team,\n" +
                "a_b/data[at0001]/items[at0004]/value/value as question,\n" +
                "a_c/data[at0001]/items[at0002]/value/value as notes, \n" +
                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string as careflow_step\n" +
                "from EHR e \n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report.v1]\n" +
                "contains ( \n" +
                " INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.request.v0]" +
                " or EVALUATION a_b[openEHR-EHR-EVALUATION.reason_for_encounter.v1]" +
                " or EVALUATION a_c[openEHR-EHR-EVALUATION.recommendation.v1])\n" +
                "where a/name/value='MDT Output Report'" +
                " and a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string matches {'at0002','at0003'}";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryCluster2() throws Exception {
        String query = "select a/uid/value as uid,\n" +
                "a/composer/name as author,\n" +
                "a/context/start_time/value as date_created,\n" +
                "a_b/data[at0001]/origin/value as sample_time,\n" +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name,\n" +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/code_string as test_name_code,\n" +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/terminology_id/value as test_name_terminology,\n" +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status,\n" +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion,\n" +
                "a_a/items[at0002]/name/value as Laboratory_result_header,\n" +
                "a_a/items[at0002]/items[at0001]/name/value as result_name,\n" +
                "a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code,\n" +
                "a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology,\n" +
                "a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude,\n" +
                "a_a/items[at0002]/items[at0001]/value/units as result_units,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded,\n" +
                "a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
                "from EHR e contains COMPOSITION a contains\n" +
                "OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains\n" +
                "CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryInstructionORAction() throws Exception {
        String query = "select" +
                "     a/composer/name as author," +
                "     a/archetype_details/template_id/value," +
                "     a/uid/value as uid," +
                "     a_a/name/value as aa_name," +
                "     a/context/start_time/value as date_created," +
                "     b_a/activities[at0001]/description[at0009]/items[at0121]/value/value as referral_to," +
                "     b_a/activities[at0001]/description[at0009]/items[at0062]/value/value as referral_reason," +
                "     b_a/activities[at0001]/description[at0009]/items[at0064]/value/value as clinical_summary," +
                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.individual_person_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]/value/value as referralFrom," +
                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.organisation.v1, 'Receiver']/items[at0001]/value/value as referralTo," +
                "     b_a/protocol[at0008]/items[at0011]/value/value as referral_ref," +
                "     a_a/description[at0001]/items[at0011]/value/value as Service_name," +
                "     a_a/description[at0001]/items[at0028]/value/value as Outcome," +
                "     a_a/time/value as dateOfState," +
                "     a_a/ism_transition/current_state/value as state," +
                "     a_a/ism_transition/current_state/defining_code/code_string as stateCode," +
                "     a_a/ism_transition/careflow_step/value as careflow," +
                "     a_a/ism_transition/careflow_step/defining_code/code_string as careflowCode" +
                "     from EHR e " +
                "     contains COMPOSITION a[openEHR-EHR-COMPOSITION.request.v1] contains" +
                "     (INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.request.v0] or ACTION a_a[openEHR-EHR-ACTION.service.v0])" +
                "     ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryInstructionWHEREAction() throws Exception {
        String query = "select" +
                "     a/composer/name as author," +
                "     a/archetype_details/template_id/value," +
                "     a/uid/value as uid," +
                "     a/context/start_time/value as date_created," +
                "     b_a/activities[at0001]/description[at0009]/items[at0121]/value/value as referral_to," +
                "     b_a/activities[at0001]/description[at0009]/items[at0062]/value/value as referral_reason," +
                "     b_a/activities[at0001]/description[at0009]/items[at0064]/value/value as clinical_summary," +
                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.individual_person_uk.v1, 'Requestor']/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]/value/value as referralFrom," +
                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.organisation.v1, 'Receiver']/items[at0001]/value/value as referralTo," +
                "     b_a/protocol[at0008]/items[at0011]/value/value as referral_ref," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/description[at0001]/items[at0011]/value/value as Service_name," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/description[at0001]/items[at0028]/value/value as Outcome," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/time/value as dateOfState," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/value as state," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/defining_code/code_string as stateCode," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/ism_transition/careflow_step/value as careflow," +
                "     a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/ism_transition/careflow_step/defining_code/code_string as careflowCode" +
                "  from EHR e " +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.request.v1] contains " +
                "    INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.request.v0]" +
                "     where a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/defining_code/code_string = '526'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationCluster() throws Exception {
        String query = "select  " +
                " a/uid/value as uid," +
                " a/composer/name as author," +
                " a/context/start_time/value as date_created," +
                "     a_b/data[at0001]/origin/value as sample_time," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/code_string as test_name_code," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/terminology_id/value as test_name_terminology," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion," +
                "     a_a/items[at0002]/name/value as Laboratory_result_header," +
                "     a_a/items[at0002]/items[at0001]/name/value as result_name," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology," +
                "     a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude," +
                "     a_a/items[at0002]/items[at0001]/value/units as result_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included, " +
                "    a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
                "    from EHR e contains COMPOSITION a contains" +
                "     OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains" +
                "     CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryMatches() throws Exception {
        String query = "select " +
                "  a/uid/value as uid,\n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as dateCreated,\n" +
                "  a/name/value as documentType,\n" +
                "  a/archetype_details/template_id/value as documentTemplate\n" +
                "from EHR e \n" +
                "contains COMPOSITION a " +
                "where a/name/value matches {'Discharge summary', 'Referral'} \n" +
                "and a/archetype_details/template_id/value matches {'iEHR - Healthlink - Referral.v0','iEHR - Healthlink - Discharge Sumary.v0'}\n" +
                "order by  a/context/start_time/value desc";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryLike() throws Exception {
        String query = "select " +
                "  a/uid/value as uid,\n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as dateCreated,\n" +
                "  a/name/value as documentType,\n" +
                "  a/archetype_details/template_id/value as documentTemplate\n" +
                "from EHR e \n" +
                "contains COMPOSITION a " +
                "where a/name/value like 'REFE%' \n" +
                "order by  a/context/start_time/value desc";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }


    @Test
    public void testQryFragmentRaw() throws Exception {
        String query = "select  " +
                " a/uid/value as uid," +
                " a/composer/name as author," +
                " a/context/start_time/value as date_created," +
                "     a_b/data[at0001]/origin/value as sample_time," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]," +
                "     a_a/items[at0002]/name/value as Laboratory_result_header," +
                "     a_a/items[at0002]/items[at0001]/name/value as result_name," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology," +
                "     a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude," +
                "     a_a/items[at0002]/items[at0001]/value/units as result_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included, " +
                "    a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
                "    from EHR e contains COMPOSITION a contains" +
                "     OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains" +
                "     CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryCompositionRawJson() throws Exception {
        String query = "select c FROM EHR E CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }


    @Test
    public void testQryExperimental() throws Exception {
        String query = "select ecis_sql/demographics/test, " +
                " a/uid/value as uid," +
                " a/composer/name as author," +
                " a/context/start_time/value as date_created," +
                "     a_b/data[at0001]/origin/value as sample_time," +
                "     a_b/data[at0001]/events[at0002]/data[at0003]," +
                "     a_a/items[at0002]/name/value as Laboratory_result_header," +
                "     a_a/items[at0002]/items[at0001]/name/value as result_name," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code," +
                "     a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology," +
                "     a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude," +
                "     a_a/items[at0002]/items[at0001]/value/units as result_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper," +
                "         a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included, " +
                "    a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded," +
                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
                "    from EHR e contains COMPOSITION a contains" +
                "     OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains" +
                "     CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryOtherContext() throws Exception {
        String query =
                "select e/ehr_id/value as ehrId, " +
                        "a/context/other_context[at0001]/items[at0005]/value/magnitude as diagnosesCount,  " +
                        "a/context/other_context[at0001]/items[at0002]/value/magnitude as ordersCount, " +
                        "a/context/other_context[at0001]/items[at0006]/value/value as ordersDate, " +
                        "a/context/other_context[at0001]/items[at0004]/value/magnitude as resultsCount, " +
                        "a/context/other_context[at0001]/items[at0009]/value/value as resultsDate, " +
                        "a/context/other_context[at0001]/items[at0003]/value/magnitude as vitalsCount, " +
                        "a/context/other_context[at0001]/items[at0007]/value/value as vitalsDate " +
                        "from EHR e " +
                        "contains COMPOSITION a[openEHR-EHR-COMPOSITION.ripple_cache.v1] ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationGT_LT_Offset_limit() throws Exception {
        String query = "select a/uid/value as uid, a/context/start_time/value as date_created,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic,\n" +
                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units,\n" +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude as rate,\n" +
                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units as rate_units\n" +
                "   from EHR e \n" +
                "                  contains COMPOSITION a\n" +
                "                  contains (OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
                "                  AND OBSERVATION o_hr[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1])  \n" +
                "                  where e/ehr_status/subject/external_ref/namespace='fr.asip.48221832'\n" +
                "                  and e/ehr_status/subject/external_ref/id/value='fr.asip.48221832'\n" +
                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude < 80\n" +
                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude > 130\n" +
                "                  limit 1 \n" +
                "                  ORDER BY date_created DESC ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testQryObservationActionOR() throws Exception {
        String query = "SELECT c/uid/value as uid, \n" +
                "       c/composer/name as author, \n" +
                "       c/context/start_time/value as date_created, \n" +
                "       a_y/ism_transition/careflow_step/defining_code/code_string as ystate_code, \n" +
                "       a_y/ism_transition/careflow_step/value as ystate, " +
                "       a_y/ism_transition/careflow_step/definingCode/terminologyId/value as ystate_terminology, " +
                "       a_m/ism_transition/careflow_step/definingCode/codeString as mstate_code, " +
                "       a_m/ism_transition/careflow_step/value as mstate, " +
                "       a_m/ism_transition/careflow_step/definingCode/terminologyId/value as mstate_terminology " +
                "       FROM EHR e \n" +
                "           CONTAINS COMPOSITION c CONTAINS (ACTION a_y[openEHR-EHR-ACTION.service-yoga.v0] OR ACTION a_m[openEHR-EHR-ACTION.service-massage.v0])\n" +
                "           WHERE c/name/value='Encounter' ";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR38() throws Exception {
        String query = "select   a/uid/value as uid,   \n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as date_created," +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value as test_name," +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value as conclusion," +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value as status," +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value as sample_taken," +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0] as test_panel  \n" +
                "  from EHR e \n" +
                "  contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "  contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]  \n" +
                "  where a/name/value='Laboratory test report'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR43() throws Exception {
        String query = "select " +
                "   e/ehr_id/value as ehrId " +
                "   from EHR e " +
                "   contains COMPOSITION a " +
                "   contains EVALUATION a_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "   where a/name/value='Adverse reaction list' and a_a/data[at0001]/items[at0002]/value/value = 'peanuts'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR46() throws Exception {
        String query = "select a/ehr_id/value as EHRid," +
                " a/context/facility/name/value as organization" +
                " from EHR e contains COMPOSITION a[openEHR-EHR-COMPOSITION.review.v1]" +
                " contains (OBSERVATION b_a[openEHR-EHR-ADMIN_ENTRY.admission-extended.v1])" +
                " WHERE a/archetype_details/template_id/value='EHRN Episode details.v0'" +
                " AND a/context/start_time/value < '2018-01-01' AND a/context/start_time/value > '2017-09-01'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testDISTINCT() throws Exception {
        String query = "select distinct count(a/ehr_id/value) as ehr_count" +
                " from EHR e contains COMPOSITION a[openEHR-EHR-COMPOSITION.review.v1]" +
                " contains (OBSERVATION b_a[openEHR-EHR-ADMIN_ENTRY.admission-extended.v1])" +
                " WHERE a/archetype_details/template_id/value='EHRN Episode details.v0'" +
                " AND a/context/start_time/value < '2018-01-01' AND a/context/start_time/value > '2017-09-01'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testFunction1() throws Exception {
        String query = "select  \n" +
                "    max(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1055]/items[at0004]/value/magnitude) as max_systolic,\n" +
                "    max(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1054]/items[at0005]/value/magnitude) as max_diastolic,\n" +
                "    min(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1055]/items[at0004]/value/magnitude) as min_systolic,\n" +
                "    min(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1054]/items[at0005]/value/magnitude) as min_diastolic,\n" +
                "    avg(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1055]/items[at0004]/value/magnitude) as avg_systolic,\n" +
                "    avg(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1054]/items[at0005]/value/magnitude) as avg_diastolic\n" +
                "    from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9']\n" +
                "    contains COMPOSITION a contains OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1]\n";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testExtendedFrom1() throws Exception {
        String query = "select  \n" +
                "    max(o_bp/data[at0001]/events[at0006]/data[at0003]/items[at1055]/items[at0004]/value/magnitude) as max_systolic\n" +
                "    from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9']\n" +
                "       contains COMPOSITION a contains OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1],\n" +
                "    PERSON p[join on e/ehr_status/party/uuid=p/uuid]\n";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR58Func() throws Exception {
        String query = "select   split_part(a/uid/value, '::', 3) as uid,   \n" +
                "  initcap(a/composer/name)  as author,\n" +
                "  a/context/start_time/value as date_created" +
                "  from EHR e \n" +
                "  contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "  contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]  \n" +
                "  where a/name/value='Laboratory test report'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testExtension() throws Exception {
        String query = "select   \n" +
                "    _ext('http://aaaa.(aa)', '\"[11]11\"'),\n" +
                "    split_part(a/uid/value, '::', 3) as compo_version,   \n" +
                "  initcap(a/composer/name)  as author,\n" +
                "  a/context/start_time/value as date_created  from EHR e \n" +
                "  contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "  contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]  \n" +
                "  where a/name/value='Laboratory test report'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testDISTINCT_CR54() throws Exception {
        String query = "select distinct e/ehr_id/value as ehrId ," +
                " b_a/data[at0001]/items[at0.137]/items[at0.138]/value/value as Status " +
                "     from EHR e" +
                "     contains COMPOSITION a[openEHR-EHR-COMPOSITION.review.v1] " +
                "     contains (OBSERVATION b_a[openEHR-EHR-ADMIN_ENTRY.admission-extended.v1])";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR69() throws Exception {
        String query = "select \n" +
                "\ta_a/data[at0001]/items[at0009] as Manifestation \n" +
                "\tfrom EHR e contains COMPOSITION a contains EVALUATION a_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]\n" +
                "\twhere a/uid/value = 'b10d5ca1-1cfa-46c6-9dc2-e33890e758a3'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR72() throws Exception {
        String query = "select" +
                "     a/uid/value as uid," +
                "     a/composer/name as author," +
                "     a/context/start_time/value as date_created, b_a/time/value as time " +
                "from EHR e" +
                "    contains COMPOSITION a[openEHR-EHR-COMPOSITION.health_summary.v1]" +
                "    contains ACTION b_a[openEHR-EHR-ACTION.immunisation_procedure.v1]";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testRobProposal() throws Exception {
        String query = "select a as data " +
                " from EHR e[ehr_id/value='cd8abecd-9925-4313-86af-93aab4930eae']" +
                " contains COMPOSITION a [openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]" +
                " where a/name/value='Adverse reaction list'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR82() throws Exception {
        String query = "select " +
                "   e/ehr_id/value as ehrId, " +
                "   e/ehr_status/subject/external_ref/id/value as subjectId, " +
                "   e/ehr_status/subject/external_ref/namespace as subjectNamespace, " +
                "   a/uid/value as compositionId, " +
                "   b_a/data[at0001]/items[at0002]/value as Causative_agent" +
                " from EHR e " +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]" +
                " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]" +
                " where a/name/value='Adverse reaction list'";

        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }

    @Test
    public void testCR79() throws Exception {
        String query = "select" +
                " a/uid/value as uid," +
                " a/composer/name as author," +
                " a/context/start_time/value as date_created," +
                " b_a/data[at0001]/items[at0.137]/items[at0.136]/value/value as checkout," +
                " b_a/data[at0001]/items[at0.137]/items[at0071]/value/value as checkin," +
                " b_a/data[at0001]/items[at0.139]/items[at0.140]/value/value as package," +
                " b_a/data[at0001]/items[at0.139]/items[at0002.1]/value/value as class," +
                " c_a/data[at0001]/items[at0002]/value/value as reason," +
                " e/ehr_status/subject/external_ref/id/value as patientId" +
                " from EHR e" +
                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.review.v1]" +
                " contains (ADMIN_ENTRY b_a[openEHR-EHR-ADMIN_ENTRY.admission-extended.v1] or ADMIN_ENTRY c_a[openEHR-EHR-ADMIN_ENTRY.episode_preferences.v0])" +
                " where a/name/value='Review'" +
                " and a/context/facility/id/ref ='9adcfad54advadf4adf5ad4'" +
                " and (b_a/data[at0001]/items[at0.137]/items[at0071]/value/value ilike '2017-11%' or b_a/data[at0001]/items[at0.137]/items[at0.136]/value/value ilike '2017-11%')" +
                " ORDER BY date_created DESCENDING";


        records = queryEngine.perform(query);
        assertNotNull(records);
        assertFalse(records.isEmpty());
        System.out.print(records);
    }
}