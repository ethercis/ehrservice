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

import com.ethercis.aql.compiler.QueryParser;
import com.ethercis.aql.sql.QueryProcessor;
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

/**
 * Created by christian on 4/1/2016.
 */
public class QueryParserTest {

    protected DSLContext context;
    protected Connection connection;
    protected static String serverNodeId = System.getProperty("server.node.name") == null ? "test-server" : System.getProperty("server.node.name");

    @Before
    public void setUp(){

        SQLDialect dialect = SQLDialect.valueOf("POSTGRES");
        String url = "jdbc:postgresql://localhost:5434/ethercis";
//        String url = "jdbc:postgresql://192.168.2.113:5432/ethercis";
        String login = "postgres";
        String password = "postgres";

        try {
            this.connection = DriverManager.getConnection(url, login, password);
        }
        catch (SQLException e){
            throw new IllegalArgumentException("SQL exception occurred while connecting:"+e);
        }

        if (connection == null)
            throw new IllegalArgumentException("Could not connect to DB");

        this.context = DSL.using(connection, dialect);

    }

    @Test
    public void testDump() {

        String query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
                "FROM EHR [ehr_id/value='1234'] \n" +
                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.encounter.v1] \n" +
                "CONTAINS OBSERVATION o [openEHR-EHR-OBSERVATION.blood_pressure.v1]\n" +
                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

        QueryParser queryParser = new QueryParser(context, query);

        System.out.println(queryParser.dump());
    }

    @Test
    public void testWalk() throws SQLException {
//        QueryProcessor queryProcessor = new QueryProcessor(testDomainAccess);

        String query = null;

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='1234'] \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.encounter.v1] \n" +
//                "CONTAINS OBSERVATION o [openEHR-EHR-OBSERVATION.blood_pressure.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR e CONTAINS ((COMPOSITION c1\n" +
//                "       CONTAINS ACTION a [openEHR-EHR-ACTION.medication.v1]\n" +
//                "           CONTAINS ITEM_TREE it [openEHR-EHR-ITEM_TREE.medication.v1]) AND\n" +
//                "       (COMPOSITION c2 CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1]))\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='1234'] \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.encounter.v1] \n" +
//                "CONTAINS (\n" +
//                "OBSERVATION o [openEHR-EHR-OBSERVATION.laboratory-hba1c.v1] \n" +
//                " AND OBSERVATION o1 [openEHR-EHR-OBSERVATION.laboratory-glucose.v1]\n" +
//                " OR OBSERVATION o2 [openEHR-EHR-OBSERVATION.laboratory-cellulose.v1]\n" +
//                " XOR OBSERVATION o3 [openEHR-EHR-OBSERVATION.laboratory-mixomatose.v1]\n" +
//                ")"+
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

////        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='1234'] \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.encounter.v1] \n" +
//                "CONTAINS (\n" +
//                "OBSERVATION o [openEHR-EHR-OBSERVATION.laboratory-hba1c.v1] AND \n" +
//                "OBSERVATION o1 [openEHR-EHR-OBSERVATION.laboratory-glucose.v1]\n" +
//                "CONTAINS \n" +
//                "    (\n" +
//                "    ITEMTREE t1 [openEHR-EHR-ITEMTREE.tree-1.v0] OR\n" +
//                "    ITEMTREE t2 [openEHR-EHR-ITEMTREE.tree-2.v0]\n" +
//                "    CONTAINS (\n" +
//                "                CLUSTER cl1 [openEHR-EHR-CLUSTER.cluster-1.v0] \n" +
//                "                XOR\n" +
//                "                " +
//                "                   CLUSTER cl2 [openEHR-EHR-CLUSTER.cluster-2.v0]\n" +
//                "                       CONTAINS\n" +
//                "                           ITEM i1 [openEHR-EHR-ITEM.item-1.v0]\n" +
//                "                    " +
//                "             )\n" +
//                "    )\n" +
//                ")"+
//                "OR COMPOSITION c2 [openEHR-EHR-COMPOSITION.discharge.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='1234'] \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.referral.v1]\n" +
//                "AND COMPOSITION c1 [openEHR-EHR-COMPOSITION.referral.v1]\n" +
//                "OR COMPOSITION c1 [openEHR-EHR-COMPOSITION.car-service.v1]\n" +
//                "XOR COMPOSITION c1 [openEHR-EHR-COMPOSITION.sink-plumbing.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='13085d82-b75b-4c6f-bf3d-72172b260741']  \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.referral.v1]\n" +
//                "AND COMPOSITION c1 [openEHR-EHR-COMPOSITION.car-service.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic,\n" +
//                "o/data[at0002]/events[at0005] AS diastolic\n"+
//                "FROM EHR e \n" +
//                "CONTAINS COMPOSITION c [openEHR-EHR-COMPOSITION.encounter.v1] \n" +
//                "CONTAINS OBSERVATION o [openEHR-EHR-OBSERVATION.blood_pressure.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR e \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS (EVALUATION e1 [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n" +
//                " OR \n" +
//                "CLUSTER cl [openEHR-EHR-CLUSTER.laboratory-test-panel.v0]) \n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT " +
//                "c/uid/value,"+
//                "c/name/value,"+
//                "eval/data[at0001]/items[at0002]/value AS problem,\n" +
//                "eval/data[at0001]/items[at0002]/defining_code AS code,\n" +
//                "eval/data[at0001]/items[at0009]/value AS description,\n"+
//                "eval/data[at0001]/items[at0077]/value AS onset,\n"+
//                "eval/data[at0001] AS struct\n"+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT " +
//                "c/uid/value,"+
//                "c/name/value,"+
//                "eval/item_count as count, \n"+
//                "eval/data[at0001]/items[at0002]/value AS problem\n" +
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//        query = "SELECT " +
//                "c/uid/value,"+
//                "c/name/value,"+
//                "c/composer/name/value,"+
//                "c/composer/id/namespace,"+
//                "c/composer/id/ref,"+
//                "c/composer/id/scheme,"+
//                "c/composer/type "+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//         query = "SELECT " +
//                "c/context/start_time/value "+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

//         query = "SELECT TOP 1" +
//                "c/uid/value,"+
//                "c/name/value,"+
//                "eval/data[at0001, '#1']/items[at0002]/value AS problem,\n" +
//                "eval/data[at0001, '#1']/items[at0002]/defining_code AS code,\n" +
//                "eval/data[at0001, '#1']/items[at0009]/value AS description,\n"+
//                "eval/data[at0001, '#1']/items[at0077]/value AS onset,\n"+
//                "eval/data[at0001, '#1'] AS struct\n"+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1] \n"+
//                "WHERE eval/data[at0001]/events[at0006]/data[at0003]/items[at0004]/magnitude > 140 AND \n" +
//                 "eval/data[at0001]/events[at0006]/data[at0003]/items[at0004]/magnitude < 80 OR \n"+
//                 "NOT eval/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value = \"1111\"" +
//                 "ORDER BY problem ASC";

//        query = "SELECT TOP 10" +
//                "c/uid/value,"+
//                "c/name/value,"+
//                "eval/data[at0001]/items[at0002]/value AS problem,\n" +
//                "eval/data[at0001]/items[at0002]/defining_code AS code,\n" +
//                "eval/data[at0001]/items[at0009]/value AS description,\n"+
//                "eval/data[at0001]/items[at0077]/value AS onset,\n"+
//                "eval/data[at0001] AS struct\n"+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1]" +
//                "order by problem ASC";

//        query = "SELECT c/uid/value,"+
//                "e/ehr_status/subject/external_ref/id/value AS subject_id,"+
//                "c/name/value,"+
//                "eval/data[at0001]/items[at0002]/value AS problem,\n" +
//                "eval/data[at0001]/items[at0002]/defining_code/code_string AS code,\n" +
//                "eval/data[at0001]/items[at0002]/defining_code/terminology_id/name AS code,\n" +
//                "eval/data[at0001]/items[at0009]/value AS description,\n"+
//                "eval/data[at0001]/items[at0077]/value AS onset,\n"+
//                "eval/data[at0001] AS struct\n"+
//                "FROM EHR e  \n" +
//                "CONTAINS COMPOSITION c \n" +
//                "CONTAINS EVALUATION eval [openEHR-EHR-EVALUATION.problem-diagnosis.v1]" +
//                "WHERE e/ehr_status/subject/external_ref/id/value = '9999999000'";

        // ======================= FROM IAN

        query = "select a/uid/value as uid, " +
                "a/context/start_time/value as date_created, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name, " +
                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken, " +
                "c/items[at0002]/items[at0001]/name as what, " +
                "c/items[at0002]/items[at0001]/value/magnitude as value, " +
                "c/items[at0002]/items[at0001]/value/units as units " +
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1] " +
                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0] " +
                "contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]"+
                "where a/name/value='Laboratory test report' "+
                "AND e/ehr_status/subject/external_ref/id/value = '9999999000'";

//        query = "select a/uid/value as uid, " +
//                "a_a/items/items/data[at0001]/items/items[at0001]/value/value as name, " +
//                "a_a/items/items/data[at0001]/items/items[at0020]/value/value as dose_amount " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
//                "contains SECTION a_a[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1] " +
//                "where a/name/value='Current medication list' ";
////                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
////                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'"

//        query = "select a/uid/value as uid, " +
//                "a/composer/name as author, " +
//                "a/context/start_time/value as date_created, " +
//                "a_a/activities/activities[at0001]/name/name as name_activities, " +
//                "a_a/activities/activities[at0001]/timing/value as timing_activities, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0070]/value/value as name, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0109]/value/value as dose_amount, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0055]/value/value as dose_timing, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value/value as start_date " +
////                "a_a/items/items/data[at0001]/items/items[at0001]/value/value as name, " +
////                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/code_string as medication_code, " +
////                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/terminology_id/value as medication_terminology, " +
////                "a_a/items/items/data[at0001]/items/items[at/0002]/value/defining_code/code_string as route, " +
////                "a_a/items/items/data[at0001]/items/items[at0003]/value/value as dose_directions, " +
//                "from EHR e " +
//                "contains COMPOSITION a " +
//                "contains INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.medication_order.v0] " +
//                "where a/name/value='Current medication list' " +
////                "and a/uid/value='" + medicationId + "' " +
////                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
//                " AND e/ehr_status/subject/external_ref/id/value = '9999999000'";

//        query = "select a/uid/value as uid, " +
//                "a/composer/name as author, " +
//                "a/context/start_time/value as date_created, " +
//                "a_a/activities/activities[at0001]/name/name as name_activities, " +
//                "a_a/activities/activities[at0001]/timing/value as timing_activities, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0070]/value/value as name, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0109]/value/value as dose_amount, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0055]/value/value as dose_timing, " +
//                "a_a/activities/activities[at0001]/description[at0002]/items[at0113]/items[at0012]/value/value/value as start_date " +
//                "from EHR e " +
//                "contains COMPOSITION a " +
//                "contains INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.medication_order.v0] " +
//                "where a/name/value='Current medication list' " +
//                " AND e/ehr_status/subject/external_ref/id/value = '9999999000'";

//        query =  "select a/uid/value as uid, " +
//                "a/composer/name as author, " +
//                "e/ehr_status/subject/external_ref/id/value,"+
//                "a/context/start_time/value as date_submitted, " +
//                "b_a/description[at0001]/items[at0002]/value/value as procedure_name, " +
//                "b_a/description[at0001]/items[at0049, 'Procedure notes']/value/value as procedure_notes, " +
//                "b_a/other_participations/performer/name as performer, " +
//                "b_a/time/value as procedure_date, " +
//                "b_a/ism_transition/careflow_step/value as status, " +
//                "b_a/ism_transition/careflow_step/defining_code/code_string as status_code, " +
//                "b_a/ism_transition/careflow_step/defining_code/terminology_id/value as terminology " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
//                "contains ACTION b_a[openEHR-EHR-ACTION.procedure.v1] " +
//                "where a/name/value='Procedures list' "+
////                "and a/uid/value='" + procedureId + "' " +
//                "and e/ehr_status/subject/external_ref/id/value = '9999999000'";
////                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'";

//        query = "select top 10 " +
//                "a/uid/value,\n" +
//                "a/name/value,\n" +
//                "a/context/start_time/value\n" +
//                "from EHR e \n" +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
//                "order by a/context/start_time/value DESC";

        //this one should raise an exception with a syntax error
//        query = "SELECT o/data[at0002]/events[at0003] AS systolic\n" +
//                "FROM EHR [ehr_id/value='1234'] \n" +
//                "CONTAINS COMPOSITION c [[openEHR-EHR-COMPOSITION.referral.v1]\n" +
//                "AND COMPOSITION c1 [openEHR-EHR-COMPOSITION.referral.v1]\n" +
//                "OR COMPOSITION c1 [openEHR-EHR-COMPOSITION.car-service.v1]\n" +
//                "XOR COMPOSITION c1 [openEHR-EHR-COMPOSITION.sink-plumbing.v1]\n" +
//                "WHERE o/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/value > 140";

        //============= RIPPLE CONFORMANCE TEMPLATE
//        query =  "select a/uid/value as uid, " +
//                "a/composer/name as author, " +
//                "b_a/other_participations/performer/name as performer, " +
//                "b_a/uid/value/value as instruction_uid, " +
//                "b_a/narrative/value/value as narrative, " +
//                "b_a/activities/activities[at0001]/name/name as name_activities, " +
//                "b_a/activities/activities[at0001]/timing/value as timing_activities, " +
//                "b_a/activities/activities[at0001]/action_archetype_id as archetype_activities " +
//                "from ehr e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1] " +
//                "contains instruction b_a[openEHR-EHR-INSTRUCTION.request-procedure.v1] " +
//                "where a/name/value='Encounter' " +
//                "and b_a/activities/activities[at0001]/name/name = 'Request' " +
//                "and b_a/narrative/value/value = 'Human readable instruction narrative'";
//
//        query = "select  b_a/data[at0001]/items[at0002]/value/value as Causative_agent,     " +
//                "b_a/data[at0001]/items[at0002]/value/defining_code/code_string as Causative_agent_code,     " +
//                "b_a/protocol[at0042]/items[at0062]/value/value as RecordedDate,     " +
//                "b_a/data[at0001]/items[at0009]/items[at0011]/value/value as Manifestation,     " +
//                "a/uid/value as compositionId " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
//                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
//                "where a/name/value='Allergies list'";

//        query = "select count(e) as cnt from EHR e" +
//                "  contains COMPOSITION a" +
//                "  contains EVALUATION a_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1]" +
//                "  where a_a/data[at0001]/items[at0002]/value/defining_code/code_string matches {'456'}" +
//                "  and a_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value = 'SNOMED-CT'" +
//                "  and e/ehr_status/other_details/items[openEHR-EHR-CLUSTER.person_anonymised_parent.v1]/items[at0014]/value/value >= 1940" +
//                "  and e/ehr_status/other_details/items[openEHR-EHR-CLUSTER.person_anonymised_parent.v1]/items[at0014]/value/value <= 1950";

//        query = "select " +
//                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0012]/value/value as date," +
//                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0009]/value/value as status," +
//                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0002]/value/value/definingCode/terminologyId/value as code," +
//                "e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0002]/value/name as name " +
//                "from EHR e[ehr_id/value='f2724b93-c9a6-49ff-ac52-11c0bd53b9a1'] "+
//                "where e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0012]/value/value != '' " +
//                "and e/ehr_status/other_details/items[at0001]/items[openEHR-EHR-CLUSTER.person_anoymised_parent.v0]/items[at0012]/value/value != ''";

//
//        query = "select e/ehr_id/value, " +
//                "c_a/items[at0053]/value/numerator as Percent_O2_numerator " +
//                "from EHR e contains COMPOSITION c contains CLUSTER c_a[openEHR-EHR-CLUSTER.person-name.v1]";
        
        //================================================================================
        query = "select" +
                "    b_a/data[at0001]/items[at0002]/value/value as cause," +
                "    b_a/data[at0001]/items[at0009]/items[at0011]/value/value as reaction," +
                "    a/uid/value as compositionId " +
                "from EHR e  " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] " +
                "where a/name/value='Adverse reaction list' " +
                "and e/ehr_status/subject/external_ref/id/value  = '9999999000' " +
                "and e/ehr_status/subject/external_ref/namespace = 'uk.nhs.nhs_number' ";


        //use to test array (itemlist) handling
        query = "select" +
                "   a/uid/value as uid,  " +
                "   a/context/start_time/value as date_created,  " +
                "   a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as" +
                "              test_name,  " +
                "               a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken,  " +
                "               c/items[at0002]/items[at0001]/name as what,  " +
                "               c/items[at0002]/items[at0001]/value/magnitude as value,  " +
                "               c/items[at0002]/items[at0001]/value/units as units  from EHR e  " +
                "               contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]  " +
                "               contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]  " +
                "               contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0] where  " +
                "                   a/name/value='Laboratory test report'  " +
                "                   AND e/ehr_status/subject/external_ref/id/value = '9999999000'" +
                "                   and c/items[at0002]/items[at0001]/name='Sodium'";
//
////                " d/items[at0057]/items[at0064]/items[at0065]/value/value as medication " +
//
//        query = "select " +
//                " a/uid/value as composition_uid, " +
//                " a_i/narrative/value as narrative, " +
//                " a/context/start_time/value as start_time, " +
//                " a_i/activities[at0001]/timing/value as timing, " +
//                " d/items[at0001]/value/value as medication_name, " +
//                " d/items[at0003]/value/magnitude as medication_strength," +
//                " d/items[at0003]/value/units as medication_units," +
//                " d/items[at0057]/items[at0064]/items[at0065]/value/magnitude as offset, " +
//                " d/items[at0057]/items[at0064]/items[at0065]/value/units as units, " +
//                " d/items[at0057]/items[at0064]/items[at0067]/value/value as event" +
//                " from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9']" +
//                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.prescription.v1] " +
//                " contains INSTRUCTION a_i[openEHR-EHR-INSTRUCTION.medication.v1] " +
//                " contains DESCRIPTION d[openEHR-EHR-ITEM_TREE.medication_mod.v1]" +
//                " orderby start_time DESC ";
//
//
//        query = "select " +
//                "e/ehr_id/value as ehr_id, " +
//                "e/ehr_status/subject/external_ref/id/value as subject_id, "+
//                "e/ehr_status/subject/external_ref/namespace as subject_namespace "+
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
//                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] ";
////
//        query = "select a/uid/value as uid, a/context/start_time/value as date_created,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units," +
//                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude as rate," +
//                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units as rate_units" +
//                "       from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
//                "                  contains COMPOSITION a\n" +
//                "                  contains (OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
//                "                  AND OBSERVATION o_hr[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1])" +
//                "   where o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > 80 " +
//                "   ORDERBY systolic ASC";
//////
//        query = "select a/uid/value as comp_id, a/context/start_time/value as date_created,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic," +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units" +
//                "       from EHR e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
//                "                  contains COMPOSITION a\n" +
//                "                  contains OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
//                "    where o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > 0\n" +
//                "   ORDERBY date_created DESC";
//
       query = "select e/ehr_id/value from ehr e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] contains composition";
////
//        query = "select e/ehr_id/value from EHR e contains composition";
//
//        query = "select e/ehr_id/value, " +
//                "e/ehr_status/subject/external_ref/id/value as subject_id, "+
//                "e/ehr_status/subject/external_ref/namespace as subject_namespace "+
//                " from EHR e";
//
//        query = "select a/uid/value as uid, a/context/start_time/value as date_created,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude as systolic,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/units as systolic_units,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude as diastolic,\n" +
//                "   o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/units as diastolic_units,\n" +
//                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude as rate,\n" +
//                "   o_hr/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/units as rate_units\n" +
//                "   from EHR e \n" +
//                "                  contains COMPOSITION a\n" +
//                "                  contains (OBSERVATION o_bp[openEHR-EHR-OBSERVATION.blood_pressure.v1] \n" +
//                "                  AND OBSERVATION o_hr[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1])  \n" +
//                "                  where e/ehr_status/subject/external_ref/namespace='fr.asip.48221832'\n" +
//                "                  and e/ehr_status/subject/external_ref/id/value='fr.asip.48221832'\n" +
//                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude < 80\n" +
//                "                  and o_bp/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude > 130\n" +
//                "                  ORDERBY date_created ASC ";

//        query = "select a/uid/value as uid, \n" +
//                "a/composer/name as author, \n" +
//                "a/context/start_time/value as date_created, \n" +
//                "b_a/data[at0001]/items[at0002]/value/value as cause, \n" +
//                "b_a/data[at0001]/items[at0002]/value/defining_code/code_string as cause_code, \n" +
//                "b_a/data[at0001]/items[at0002]/value/defining_code/terminology_id/value as cause_terminology, \n" +
//                "b_a/data[at0001]/items[at0009]/items[at0011]/value/value as reaction, \n" +
//                "b_a/data[at0001]/items[at0009]/items[at0011]/value/defining_code/codeString as reaction_code, \n" +
//                "b_a/data[at0001]/items[at0009]/items[at0011]/value/terminology_id/value as reaction_terminology \n" +
//                "from EHR e [ehr_id/value = 'bb872277-40c4-44fb-8691-530be31e1ee9'] \n" +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1]\n" +
//                " contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1]\n" +
//                " where a/name/value='Adverse reaction list'";

//        query = "select a/uid/value as uid, a/composer/name\n" +
//                "as author, a/context/start_time/value as date_created,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/name/value as labResultName,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/name/defining_code/code_string as labResultCode,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/value/magnitude as labResultValue,\n" +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]/items[at0002]/items[at0001]/value/units as labResultUnits\n" +
//                "from EHR e [ehr_id/value = 'cd8abecd-9925-4313-86af-93aab4930eae']\n" +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
//                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0]\n" +
//                "where a/name/value='Laboratory test report'";

//        query = "select a/uid/value as uid, a/composer/name as\n" +
//                "author, a/context/start_time/value as date_created,\n" +
//                "b_a/data[at0001]/items[at0003]/value/value as priority_place_of_care,\n" +
//                "b_a/data[at0001]/items[at0015]/value/value as priority_place_of_death,\n" +
//                "b_a/data[at0001]/items[at0029]/value/value as priority_comment,\n" +
//                "b_b/data[at0001]/items[at0003]/value/value as treatment_decision,\n" +
//                "b_b/data[at0001]/items[at0002]/value/value as treatment_date_of_decision,\n" +
//                "b_b/data[at0001]/items[at0021]/value/value as treatment_comment,\n" +
//                "b_c/data[at0001]/items[at0003]/value/value as cpr_decision,\n" +
//                "b_c/data[at0001]/items[at0002]/value/value as cpr_date_of_decision,\n" +
//                "b_c/data[at0001]/items[at0021]/value/value as cpr_comment from EHR e\n" +
//                "[ehr_id/value = 'cd8abecd-9925-4313-86af-93aab4930eae'] contains\n" +
//                "COMPOSITION a[openEHR-EHR-COMPOSITION.care_plan.v1] contains\n" +
//                "(EVALUATION b_a[openEHR-EHR-EVALUATION.care_preference_uk.v1] or\n" +
//                "EVALUATION b_b[openEHR-EHR-EVALUATION.advance_decision_refuse_treatment_uk.v1]\n" +
//                "or EVALUATION b_c[openEHR-EHR-EVALUATION.cpr_decision_uk.v1])\n" +
//                "where a/name/value='End of Life Patient Preferences'";
//
//        query = "select\n" +
//                " a/uid/value as uid,\n" +
//                " a/composer/name as author,\n" +
//                " a/context/start_time/value as date_created,\n" +
//                " b_a/description[at0001]/items[at0011]/value/value as service_team,\n" +
//                " b_a/description[at0001]/items[at0026]/value/lower/value as appointment_date,\n" +
//                " b_a/protocol[at0015]/items[openEHR-EHR-CLUSTER.address.v1]/items[at0001]/items[at0002]/value/value\n" +
//                "as location\n" +
//                "from EHR e\n" +
//                "  contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
//                "  contains ACTION b_a[openEHR-EHR-ACTION.referral_uk.v1]\n" +
//                "where a/name/value='Referral'";

//        query = "select\n" +
//                "    a/uid/value as uid,\n" +
//                "    a/archetype_details/template_id/value,\n" +
//                "    a/composer/name as author,\n" +
//                "    a/context/start_time/value as date_created,\n" +
//                "    b_a/data[at0001]/items[at0002]/value/value as note,\n" +
//                "    b_a/name/value as type\n" +
//                "from EHR e\n" +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1]\n" +
//                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.clinical_synopsis.v1]\n" +
//                "where a/name/value='Clinical Notes'";

//        query = "select     \n" +
//                "a/uid/value as uid,     \n" +
//                "a/context/start_time/value as meeting_date,\n" +
//                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/time/value as request_date,\n" +
//                "a_a/protocol[at0008]/items[at0011]/value/value as service_team,\n" +
//                "a_b/data[at0001]/items[at0004]/value/value as question,\n" +
//                "a_c/data[at0001]/items[at0002]/value/value as notes, \n" +
//                "a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string as careflow_step\n" +
//                "from EHR e \n" +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report.v1]\n" +
//                "contains ( \n" +
//                " INSTRUCTION a_a[openEHR-EHR-INSTRUCTION.request.v0]" +
//                " or EVALUATION a_b[openEHR-EHR-EVALUATION.reason_for_encounter.v1]" +
//                " or EVALUATION a_c[openEHR-EHR-EVALUATION.recommendation.v1])\n" +
//                "where a/name/value='MDT Output Report'" +
//                " and a/content[openEHR-EHR-SECTION.referral_details_rcp.v1]/items[openEHR-EHR-ACTION.referral_uk.v1]/ism_transition/careflow_step/defining_code/code_string = 'at0002'";

//        query = "select a/uid/value as uid,\n" +
//                "a/composer/name as author,\n" +
//                "a/context/start_time/value as date_created,\n" +
//                "a_b/data[at0001]/origin/value as sample_time,\n" +
//                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name,\n" +
//                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/code_string as test_name_code,\n" +
//                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/terminology_id/value as test_name_terminology,\n" +
//                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status,\n" +
//                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion,\n" +
//                "a_a/items[at0002]/name/value as Laboratory_result_header,\n" +
//                "a_a/items[at0002]/items[at0001]/name/value as result_name,\n" +
//                "a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code,\n" +
//                "a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology,\n" +
//                "a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude,\n" +
//                "a_a/items[at0002]/items[at0001]/value/units as result_units,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded,\n" +
//                "a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
//                "from EHR e contains COMPOSITION a contains\n" +
//                "OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains\n" +
//                "CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

//        query = "select" +
//                "     a/composer/name as author," +
//                "     a/archetype_details/template_id/value," +
//                "     a/uid/value as uid," +
//                "     a_a/name/value as aa_name," +
//                "     a/context/start_time/value as date_created," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0121]/value/value as referral_to," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0062]/value/value as referral_reason," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0064]/value/value as clinical_summary," +
//                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.individual_person_uk.v1]/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]/value/value as referralFrom," +
//                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.organisation.v1, 'Receiver']/items[at0001]/value/value as referralTo," +
//                "     b_a/protocol[at0008]/items[at0011]/value/value as referral_ref," +
//                "     a_a/description[at0001]/items[at0011]/value/value as Service_name," +
//                "     a_a/description[at0001]/items[at0028]/value/value as Outcome," +
//                "     a_a/time/value as dateOfState," +
//                "     a_a/ism_transition/current_state/value as state," +
//                "     a_a/ism_transition/current_state/defining_code/code_string as stateCode," +
//                "     a_a/ism_transition/careflow_step/value as careflow," +
//                "     a_a/ism_transition/careflow_step/defining_code/code_string as careflowCode" +
//                "     from EHR e " +
//                "     contains COMPOSITION a[openEHR-EHR-COMPOSITION.request.v1] contains" +
//                "     (INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.request.v0] or ACTION a_a[openEHR-EHR-ACTION.service.v0])" +
//                "     ";

//        query = "select" +
//                "     a/composer/name as author," +
//                "     a/archetype_details/template_id/value," +
//                "     a/uid/value as uid," +
//                "     a/context/start_time/value as date_created," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0121]/value/value as referral_to," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0062]/value/value as referral_reason," +
//                "     b_a/activities[at0001]/description[at0009]/items[at0064]/value/value as clinical_summary," +
//                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.individual_person_uk.v1, 'Requestor']/items[openEHR-EHR-CLUSTER.person_name.v1]/items[at0001]/value/value as referralFrom," +
//                "     b_a/protocol[at0008]/items[openEHR-EHR-CLUSTER.organisation.v1, 'Receiver']/items[at0001]/value/value as referralTo," +
//                "     b_a/protocol[at0008]/items[at0011]/value/value as referral_ref," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/description[at0001]/items[at0011]/value/value as Service_name," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/description[at0001]/items[at0028]/value/value as Outcome," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/time/value as dateOfState," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/value as state," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/defining_code/code_string as stateCode," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/ism_transition/careflow_step/value as careflow," +
//                "     a/content/items[openEHR-EHR-ACTION.service.v0]/ism_transition/careflow_step/defining_code/code_string as careflowCode" +
//                "  from EHR e [ehr_id/value = 'cd8abecd-9925-4313-86af-93aab4930eae']" +
//                " contains COMPOSITION a[openEHR-EHR-COMPOSITION.request.v1] contains " +
//                "    INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.request.v0]" +
//                "     where a/content/items[openEHR-EHR-ACTION.service.v0]/ism_transition/current_state/defining_code/code_string = '526'";

//        query = "select  " +
//                " a/uid/value as uid," +
//                " a/composer/name as author," +
//                " a/context/start_time/value as date_created," +
//                "     a_b/data[at0001]/origin/value as sample_time," +
//                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name," +
//                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/code_string as test_name_code," +
//                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/terminology_id/value as test_name_terminology," +
//                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0073]/value/value as status," +
//                "     a_b/data[at0001]/events[at0002]/data[at0003]/items[at0057]/value/value as conclusion," +
//                "     a_a/items[at0002]/name/value as Laboratory_result_header," +
//                "     a_a/items[at0002]/items[at0001]/name/value as result_name," +
//                "     a_a/items[at0002]/items[at0001]/name/defining_code/code_string as result_name_code," +
//                "     a_a/items[at0002]/items[at0001]/name/defining_code/terminology_id/value as result_name_terminology," +
//                "     a_a/items[at0002]/items[at0001]/value/magnitude as result_magnitude," +
//                "     a_a/items[at0002]/items[at0001]/value/units as result_units," +
//                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower/magnitude as normal_range_lower," +
//                "         a_a/items[at0002]/items[at0001]/value/normal_range/lower/units as normal_range_lower_units," +
//                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper/magnitude as normal_range_upper," +
//                "         a_a/items[at0002]/items[at0001]/value/normal_range/upper/units as normal_range_upper_units," +
//                "     a_a/items[at0002]/items[at0001]/value/normal_range/lower_included as lower_included," +
//                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_included as upper_included, " +
//                "    a_a/items[at0002]/items[at0001]/value/normal_range/lower_unbounded as lower_unbounded," +
//                "     a_a/items[at0002]/items[at0001]/value/normal_range/upper_unbounded as upper_unbounded " +
//                "    from EHR e contains COMPOSITION a contains" +
//                "     OBSERVATION a_b[openEHR-EHR-OBSERVATION.laboratory_test.v0] contains" +
//                "     CLUSTER a_a[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]";

        query = "select e/ehr_id/value as ehr_id, a/uid/value as uid,\n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as dateCreated,\n" +
                "  a/name/value as documentType,\n" +
                "  a/archetype_details/template_id/value as documentTemplate\n" +
                "from EHR e \n" +
                "contains COMPOSITION a "+
//                "where a/name/value matches {'Discharge summary', 'Referral'} \n" +
//                "and a/archetype_details/template_id/value matches {'iEHR - Healthlink - Referral.v0','iEHR - Healthlink - Discharge Sumary.v0'}\n" +
                "orderby  a/context/start_time/value desc";

        QueryProcessor queryProcessor = new QueryProcessor(context);
        QueryParser queryParser = new QueryParser(context, query);

        queryParser.pass1();
        queryParser.pass2();

        List<Record> records = (List<Record>)queryProcessor.execute(queryParser, serverNodeId, false);
//        String records = (String)queryProcessor.execute(queryParser, serverNodeId, true);

        if (records != null && !records.isEmpty()){
            System.out.println(records);
        }

    }

    @Test
    public void testWalkNoPathExpr() throws SQLException {
//        QueryProcessor queryProcessor = new QueryProcessor(testDomainAccess);

        String query = null;


        query = "select top 10 " +
                "a/uid/value,\n" +
                "a/name/value,\n" +
                "a/context/start_time/value\n" +
                "from EHR e \n" +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1]\n" +
                "orderby a/context/start_time/value DESC";

//
        query = "select " +
                "e/ehr_id/value as ehr_id, " +
                "e/ehr_status/subject/external_ref/id/value as subject_id, "+
                "e/ehr_status/subject/external_ref/namespace as subject_namespace "+
                "from EHR e " +
                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.adverse_reaction_list.v1] " +
                "contains EVALUATION b_a[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] ";

//
        query = "select e/ehr_id/value from ehr e[ehr_id/value='bb872277-40c4-44fb-8691-530be31e1ee9'] contains composition";
////
        query = "select e/ehr_id/value from EHR e contains composition";
//
        query = "select e/ehr_id/value, " +
                "e/ehr_status/subject/external_ref/id/value as subject_id, "+
                "e/ehr_status/subject/external_ref/namespace as subject_namespace "+
                " from EHR e";
//

        query = "select e/ehr_id/value as ehr_id, a/uid/value as uid,\n" +
                "  a/composer/name as author,\n" +
                "  a/context/start_time/value as dateCreated,\n" +
                "  a/name/value as documentType,\n" +
                "  a/archetype_details/template_id/value as documentTemplate\n" +
                "from EHR e \n" +
                "contains COMPOSITION a "+
//                "where a/name/value matches {'Discharge summary', 'Referral'} \n" +
//                "and a/archetype_details/template_id/value matches {'iEHR - Healthlink - Referral.v0','iEHR - Healthlink - Discharge Sumary.v0'}\n" +
                "orderby  a/context/start_time/value desc";

        QueryProcessor queryProcessor = new QueryProcessor(context);
        QueryParser queryParser = new QueryParser(context, query);

        queryParser.pass1();
        queryParser.pass2();

        List<Record> records = (List<Record>)queryProcessor.execute(queryParser, serverNodeId, false);
//        String records = (String)queryProcessor.execute(queryParser, serverNodeId, true);

        if (records != null && !records.isEmpty()){
            System.out.println(records);
        }

    }

}