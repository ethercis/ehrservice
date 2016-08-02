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

//        query = "select a/uid/value as uid, " +
//                "a/context/start_time/value as date_created, " +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value as test_name, " +
//                "a_a/data[at0001]/events[at0002]/data[at0003]/items[at0075]/value/value as sample_taken, " +
//                "c/items[at0002]/items[at0001]/value/name as what, " +
//                "c/items[at0002]/items[at0001]/value/value/magnitude as value, " +
//                "c/items[at0002]/items[at0001]/value/value/units as units " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.report-result.v1] " +
//                "contains OBSERVATION a_a[openEHR-EHR-OBSERVATION.laboratory_test.v0] " +
//                "contains CLUSTER c[openEHR-EHR-CLUSTER.laboratory_test_panel.v0]"+
//                "where a/name/value='Laboratory test report' "+
//                "AND e/ehr_status/subject/external_ref/id/value = '9999999000'";

//        query = "select a/uid/value as uid, " +
//                "a_a/items/items/data[at0001]/items/items[at0001]/value/value as name, " +
//                "a_a/items/items/data[at0001]/items/items[at0020]/value/value as dose_amount " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
//                "contains SECTION a_a[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1] " +
//                "where a/name/value='Current medication list' ";
//                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
//                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'"

//        query = "select a/uid/value as uid, " +
//                "a/composer/name as author, " +
//                "a/context/start_time/value as date_created, " +
//                "a_a/items/items/data[at0001]/items/items[at0001]/value/value as name, " +
//                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/code_string as medication_code, " +
//                "a_a/items/items/data[at0001]/items/items[at0001]/value/defining_code/terminology_id/value as medication_terminology, " +
//                "a_a/items/items/data[at0001]/items/items[at0002]/value/defining_code/code_string as route, " +
//                "a_a/items/items/data[at0001]/items/items[at0003]/value/value as dose_directions, " +
//                "a_a/items/items/data[at0001]/items/items[at0020]/value/value as dose_amount, " +
//                "a_a/items/items/data[at0001]/items/items[at0021]/value/value as dose_timing, " +
//                "a_a/items/items/data[at0001]/items/items[at0046]/items/value/value as start_date " +
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.care_summary.v0] " +
//                "contains SECTION a_a[openEHR-EHR-SECTION.medication_medical_devices_rcp.v1] " +
//                "where a/name/value='Current medication list' " +
////                "and a/uid/value='" + medicationId + "' " +
////                "and e/ehr_status/subject/external_ref/namespace = '" + namespace + "' " +
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
//                "and e/ehr_status/subject/external_ref/id/value = '" + patientId + "'";

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
//                "from EHR e " +
//                "contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1] " +
//                "contains INSTRUCTION b_a[openEHR-EHR-INSTRUCTION.request-procedure.v1] " +
//                "where a/name/value='Encounter' ";

        query = "select e/ehr_id/value from EHR e contains COMPOSITION";

        QueryProcessor queryProcessor = new QueryProcessor(context);
        QueryParser queryParser = new QueryParser(context, query);

        queryParser.pass1();
        queryParser.pass2();

        List<Record> records = queryProcessor.execute(queryParser, serverNodeId);

        if (records != null && !records.isEmpty()){
            System.out.println(records);
        }

    }

}