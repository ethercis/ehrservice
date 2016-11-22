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

package com.ethercis.aql.sql.binding;

import com.ethercis.jooq.pg.tables.records.CompositionRecord;
import com.ethercis.jooq.pg.tables.records.EhrRecord;
import com.ethercis.jooq.pg.tables.records.PartyIdentifiedRecord;
import com.ethercis.jooq.pg.tables.records.StatusRecord;
import org.jooq.Table;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by christian on 11/1/2016.
 */
public interface I_JoinBinder {
    String COMPOSITION_JOIN = "composition_join";
    String STATUS_JOIN = "status_join";
    String EHR_JOIN = "ehr_join";

    Table<CompositionRecord> compositionRecordTable = COMPOSITION.as(COMPOSITION_JOIN);
    Table<StatusRecord> statusRecordTable = STATUS.as(STATUS_JOIN);
    Table<EhrRecord> ehrRecordTable = EHR.as(EHR_JOIN);
    Table<PartyIdentifiedRecord> composerRef = PARTY_IDENTIFIED.as("composer_ref");
    Table<PartyIdentifiedRecord> subjectRef = PARTY_IDENTIFIED.as("subject_ref");
    Table<PartyIdentifiedRecord> facilityRef = PARTY_IDENTIFIED.as("facility_ref");
}
