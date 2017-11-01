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
package com.ethercis.dao.access.jooq;

import com.ethercis.dao.access.interfaces.*;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.jooq.pg.tables.records.*;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.terminology.TerminologyService;

import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by Christian Chevalley on 4/9/2015.
 */
public class ContextAccess extends DataAccess implements I_ContextAccess {

    private EventContextRecord eventContextRecord;
    TimeZone zonedStartTime; //used to preserve actual timezone to save in the DB

    private PreparedStatement updateStatement;

    private List<ParticipationRecord> participations = new ArrayList<>();
    private static Logger log = LogManager.getLogger(ContextAccess.class);
    final static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    public ContextAccess(DSLContext context, EventContext eventContext) throws Exception {
        super(context, null);
        this.context = context;
        eventContextRecord = context.newRecord(EVENT_CONTEXT);
        setRecordFields(UUID.randomUUID(), eventContext);
    }

    public ContextAccess(I_DomainAccess domainAccess){
        super(domainAccess.getContext(), domainAccess.getKnowledgeManager());
    }

    public ContextAccess(){
        super(null, null);
    }

    @Override
    public void setRecordFields(UUID id, EventContext eventContext) throws Exception {
        zonedStartTime = eventContext.getStartTime().getDateTime().getZone().toTimeZone();
        eventContextRecord.setStartTimeTzid(eventContext.getStartTime().getDateTime().getZone().getID());
        eventContextRecord.setStartTime(new Timestamp(eventContext.getStartTime().getDateTime().getMillis()));
        if (eventContext.getEndTime() != null) {
            eventContextRecord.setEndTime(new Timestamp(eventContext.getEndTime().getDateTime().getMillis()));
            eventContextRecord.setEndTimeTzid(eventContext.getEndTime().getDateTime().getZone().getID());
        }
        eventContextRecord.setId(id != null ? id : UUID.randomUUID());

        //Health care facility
//        UUID healthcareFacilityId = I_PartyIdentifiedAccess.getOrCreateParty(this, eventContext.getHealthCareFacility().getName(), eventContext.getHealthCareFacility().getIdentifiers());
        if (eventContext.getHealthCareFacility() != null) {
            UUID healthcareFacilityId = I_PartyIdentifiedAccess.getOrCreateParty(this, eventContext.getHealthCareFacility());

            eventContextRecord.setFacility(healthcareFacilityId);
        }

        //location
        if (eventContext.getLocation() != null)
            eventContextRecord.setLocation(eventContext.getLocation());

        //TODO: retrieveInstanceByNamedSubject program details from other context if any
//        setting = eventContext.getSetting().getCode();
        Integer settingCode = Integer.parseInt(eventContext.getSetting().getDefiningCode().getCodeString());

        if (settingCode != null)
            eventContextRecord.setSetting(ConceptAccess.fetchConceptUUID(this, settingCode, "en"));

        if (eventContext.getParticipations() != null) {
            for (Participation participation : eventContext.getParticipations()) {
                ParticipationRecord participationRecord = context.newRecord(PARTICIPATION);
                participationRecord.setEventContext(eventContextRecord.getId());
                participationRecord.setFunction(participation.getFunction().getValue());
                participationRecord.setMode(participation.getMode().toString());
                if (participation.getTime() != null && participation.getTime().getInterval().getLower() != null) {
                    participationRecord.setStartTime(new Timestamp(participation.getTime().getInterval().getLower().getDateTime().getMillis()));
                    participationRecord.setStartTimeTzid(participation.getTime().getInterval().getLower().getDateTime().getZone().getID());
                }

                PartyIdentified performer; //only PartyIdentified performer is supported now

                PartyProxy setPerformer = participation.getPerformer();

                if (!(setPerformer instanceof PartyIdentified)) {
                    log.warn("Set performer is using unsupported type:" + setPerformer.toString());
                    break;
                }

                performer = (PartyIdentified) setPerformer;
//                String codeId = ((GenericID)performer.getExternalRef().getId()).getValue();
//                String scheme = ((GenericID)performer.getExternalRef().getId()).getScheme();
//                String namespace = performer.getExternalRef().getNamespace();
//                String type = performer.getExternalRef().getType();
                UUID performerUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, performer);
                //get the performer
                participationRecord.setPerformer(performerUuid);
                participations.add(participationRecord);
            }
        }

        //other context
        if (eventContext.getOtherContext() != null){
            //set up the JSONB field other_context
            I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
            eventContextRecord.setOtherContext(compositionSerializer.dbEncode(eventContext.getOtherContext()));
        }
    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {
        eventContextRecord.setSysTransaction(transactionTime);
//        UUID uuid = UUID.randomUUID();
        InsertQuery<?> insertQuery = context.insertQuery(EVENT_CONTEXT);
        insertQuery.addValue(EVENT_CONTEXT.ID, eventContextRecord.getId());
        insertQuery.addValue(EVENT_CONTEXT.COMPOSITION_ID, eventContextRecord.getCompositionId());
        insertQuery.addValue(EVENT_CONTEXT.START_TIME, eventContextRecord.getStartTime());
        insertQuery.addValue(EVENT_CONTEXT.START_TIME_TZID, eventContextRecord.getStartTimeTzid());
        insertQuery.addValue(EVENT_CONTEXT.END_TIME, eventContextRecord.getEndTime());
        insertQuery.addValue(EVENT_CONTEXT.END_TIME_TZID, eventContextRecord.getEndTimeTzid());
        insertQuery.addValue(EVENT_CONTEXT.FACILITY, eventContextRecord.getFacility());
        insertQuery.addValue(EVENT_CONTEXT.LOCATION, eventContextRecord.getLocation());
//        Field jsonbOtherContext = DSL.field(EVENT_CONTEXT.OTHER_CONTEXT+"::jsonb");
        if (eventContextRecord.getOtherContext() != null)
            insertQuery.addValue(EVENT_CONTEXT.OTHER_CONTEXT, (Object)DSL.field(DSL.val(eventContextRecord.getOtherContext())+"::jsonb"));
        insertQuery.addValue(EVENT_CONTEXT.SETTING, eventContextRecord.getSetting());
        insertQuery.addValue(EVENT_CONTEXT.SYS_TRANSACTION, eventContextRecord.getSysTransaction());

        Integer result = insertQuery.execute();

//        eventContextRecord.store();

        if (!participations.isEmpty()) {
            participations.forEach(participation -> {
                        participation.setEventContext(eventContextRecord.getId());
                        participation.setSysTransaction(transactionTime);
                        participation.store();
                    }
            );
        }
        
        return eventContextRecord.getId();
    }

    @Deprecated
    public UUID commit_DEPRECATED(Timestamp transactionTime) throws Exception {
        eventContextRecord.setSysTransaction(transactionTime);

        //use a SQL query for storing since JSONB is not natively supported by jOOQ 3.5
        String sql = "INSERT INTO ehr.event_context (composition_id, start_time, start_time_tzid, end_time, end_time_tzid, facility, location, other_context, setting, sys_transaction) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)" +
                " RETURNING id";

        Connection connection = context.configuration().connectionProvider().acquire();
        PreparedStatement insertStatement = connection.prepareStatement(sql);
        insertStatement.setObject(1, eventContextRecord.getCompositionId());
        insertStatement.setObject(2, eventContextRecord.getStartTime());
        insertStatement.setObject(3, eventContextRecord.getStartTimeTzid());
//        insertStatement.setTimestamp(2, eventContextRecord.getStartTime(), Calendar.getInstance(TimeZone.getTimeZone(zonedStartTime.toZoneId())));
        insertStatement.setObject(4, eventContextRecord.getEndTime());
        insertStatement.setObject(5, eventContextRecord.getEndTimeTzid());
        insertStatement.setObject(6, eventContextRecord.getFacility());
        insertStatement.setObject(7, eventContextRecord.getLocation());
        insertStatement.setObject(8, eventContextRecord.getOtherContext());
        insertStatement.setObject(9, eventContextRecord.getSetting());
        insertStatement.setObject(10, eventContextRecord.getSysTransaction());

//        eventContextRecord.store();
        ResultSet resultSet = insertStatement.executeQuery();

        UUID uuid = null;
        String returnString  = null;
        if (resultSet != null && resultSet.next()){
            returnString = resultSet.getString(1);
        }
        if (returnString != null) {
            uuid = UUID.fromString(returnString);
            eventContextRecord.setId(uuid);
        }

        if (!participations.isEmpty()) {
            participations.forEach(participation -> {
                        participation.setEventContext(eventContextRecord.getId());
                        participation.setSysTransaction(transactionTime);
                        participation.store();
                    }
            );
        }

        connection.close();

        if (uuid != null)
            return uuid;
        else
            return eventContextRecord.getId();
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws SQLException {
        //updateComposition participations
        for (ParticipationRecord participationRecord: participations){
            participationRecord.setSysTransaction(transactionTime);
            if (participationRecord.changed()){
                //check if commit or updateComposition (exists or not...)
                if (getContext().fetchExists(PARTICIPATION, PARTICIPATION.ID.eq(participationRecord.getId()))) {
                    participationRecord.update();
                }
                else {
                    participationRecord.store();
                }
            }
        }
        //ignore the temporal field since it is maintained by an external trigger!
        eventContextRecord.changed(EVENT_CONTEXT.SYS_PERIOD, false);

        //ignore other_context for the time being...
//        eventContextRecord.changed(EVENT_CONTEXT.OTHER_CONTEXT, false);
        eventContextRecord.setSysTransaction(transactionTime);
        
        UpdateQuery<?> updateQuery = context.updateQuery(EVENT_CONTEXT);

        updateQuery.addValue(EVENT_CONTEXT.COMPOSITION_ID, eventContextRecord.getCompositionId());
        updateQuery.addValue(EVENT_CONTEXT.START_TIME, eventContextRecord.getStartTime());
        updateQuery.addValue(EVENT_CONTEXT.START_TIME_TZID, eventContextRecord.getStartTimeTzid());
        updateQuery.addValue(EVENT_CONTEXT.END_TIME, eventContextRecord.getEndTime());
        updateQuery.addValue(EVENT_CONTEXT.END_TIME_TZID, eventContextRecord.getEndTimeTzid());
        updateQuery.addValue(EVENT_CONTEXT.FACILITY, eventContextRecord.getFacility());
        updateQuery.addValue(EVENT_CONTEXT.LOCATION, eventContextRecord.getLocation());
//        Field jsonbOtherContext = DSL.field(EVENT_CONTEXT.OTHER_CONTEXT+"::jsonb");
        if (eventContextRecord.getOtherContext() != null)
            updateQuery.addValue(EVENT_CONTEXT.OTHER_CONTEXT, (Object) DSL.field(DSL.val(eventContextRecord.getOtherContext().toString())+"::jsonb"));
        updateQuery.addValue(EVENT_CONTEXT.SETTING, eventContextRecord.getSetting());
        updateQuery.addValue(EVENT_CONTEXT.SYS_TRANSACTION, eventContextRecord.getSysTransaction());
        updateQuery.addConditions(EVENT_CONTEXT.ID.eq(getId()));

        Boolean result = updateQuery.execute()  > 0;

        return result;
//        return eventContextRecord.update() > 0;
    }


    @Deprecated
    public Boolean update_deprecated(Timestamp transactionTime) throws SQLException {
        //updateComposition participations
        for (ParticipationRecord participationRecord: participations){
            participationRecord.setSysTransaction(transactionTime);
            if (participationRecord.changed()){
                //check if commit or updateComposition (exists or not...)
                if (getContext().fetchExists(PARTICIPATION, PARTICIPATION.ID.eq(participationRecord.getId()))) {
                    participationRecord.update();
                }
                else {
                    participationRecord.store();
                }
            }
        }
        //ignore the temporal field since it is maintained by an external trigger!
        eventContextRecord.changed(EVENT_CONTEXT.SYS_PERIOD, false);

        //ignore other_context for the time being...
//        eventContextRecord.changed(EVENT_CONTEXT.OTHER_CONTEXT, false);
        eventContextRecord.setSysTransaction(transactionTime);

        //for the time being use a straight SQL with typecast...
        String sql = "UPDATE ehr.event_context SET " +
                "composition_id = ?, " +
                "start_time = ?, " +
                "start_time_tzid = ?, " +
                "end_time = ?, " +
                "end_time_tzid = ?, " +
                "facility = ?, " +
                "location = ?, " +
                "other_context = ?::jsonb, " +
                "setting = ?, " +
                "sys_transaction = ? " +
                "WHERE id = ? ";

        Connection connection = context.configuration().connectionProvider().acquire();
        updateStatement = connection.prepareStatement(sql);
        updateStatement.setObject(1, eventContextRecord.getCompositionId());
        updateStatement.setObject(2, eventContextRecord.getStartTime());
        updateStatement.setObject(3, eventContextRecord.getStartTimeTzid());
        updateStatement.setObject(4, eventContextRecord.getEndTime());
        updateStatement.setObject(5, eventContextRecord.getEndTimeTzid());
        updateStatement.setObject(6, eventContextRecord.getFacility());
        updateStatement.setObject(7, eventContextRecord.getLocation());
        updateStatement.setObject(8, eventContextRecord.getOtherContext());
        updateStatement.setObject(9, eventContextRecord.getSetting());
        updateStatement.setObject(10, eventContextRecord.getSysTransaction());
        updateStatement.setObject(11, getId());

        Boolean result = updateStatement.execute();

        connection.close();

        return result;
//        return eventContextRecord.update() > 0;
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        if (force) {
            eventContextRecord.changed(true);
            //jOOQ limited support of TSTZRANGE, exclude sys_period from updateComposition!
            eventContextRecord.changed(EVENT_CONTEXT.SYS_PERIOD, false);

            for (ParticipationRecord participationRecord: participations){
                participationRecord.changed(true);
                //jOOQ limited support of TSTZRANGE, exclude sys_period from updateComposition!
                participationRecord.changed(PARTICIPATION.SYS_PERIOD, false);
            }
        }
        return update(transactionTime);
    }

    @Override
    public Boolean update() throws Exception {
        throw new IllegalArgumentException("INTERNAL: Invalid updateComposition call to updateComposition without Transaction time and/or force flag arguments");
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        throw new IllegalArgumentException("INTERNAL: Invalid updateComposition call to updateComposition without Transaction time and/or force flag arguments");
    }

    @Override
    public Integer delete() {
        Integer count = 0;
        //delete any cross reference participants if any
        //delete the participation record
        count += context.delete(PARTICIPATION).where(PARTICIPATION.EVENT_CONTEXT.eq(eventContextRecord.getId())).execute();

        count += eventContextRecord.delete();
        return count;
    }

//    @Override
//    public I_ContextAccess retrieve(UUID id) {
//        return retrieveInstance(this, id);
//    }

    public static I_ContextAccess retrieveInstance(I_DomainAccess domainAccess, UUID id){
        ContextAccess contextAccess = new ContextAccess(domainAccess);
        contextAccess.eventContextRecord = domainAccess.getContext().fetchOne(EVENT_CONTEXT, EVENT_CONTEXT.ID.eq(id));
        return contextAccess;
    }

    public static I_ContextAccess retrieveInstance(I_DomainAccess domainAccess, Result<?> records){
        ContextAccess contextAccess = new ContextAccess(domainAccess);
        EventContextRecord eventContextRecord = domainAccess.getContext().newRecord(EVENT_CONTEXT);
        eventContextRecord.setStartTime((Timestamp)records.getValue(0, I_CompositionAccess.F_CONTEXT_START_TIME));
        eventContextRecord.setStartTimeTzid((String)records.getValue(0, I_CompositionAccess.F_CONTEXT_START_TIME_TZID));
        eventContextRecord.setEndTime((Timestamp)records.getValue(0, I_CompositionAccess.F_CONTEXT_END_TIME));
        eventContextRecord.setEndTimeTzid((String)records.getValue(0, I_CompositionAccess.F_CONTEXT_END_TIME_TZID));
        eventContextRecord.setLocation((String)records.getValue(0, I_CompositionAccess.F_CONTEXT_LOCATION));
        eventContextRecord.setOtherContext(records.getValue(0, I_CompositionAccess.F_CONTEXT_OTHER_CONTEXT));

        return contextAccess;
    }

    private static DvCodedText decodeDvCodedText(String codedDvCodedText){
        String[] tokens = codedDvCodedText.split("::");
        if(tokens.length != 2) {
            throw new IllegalArgumentException("failed to parse DvCodedText \'" + codedDvCodedText + "\', wrong number of tokens.");
        } else {
            String[] tokens2 = tokens[1].split("\\|");
            if(tokens2.length != 2) {
                throw new IllegalArgumentException("failed to parse DvCodedText \'" + codedDvCodedText + "\', wrong number of tokens.");
            } else {
                return new DvCodedText(tokens2[1], tokens[0], tokens2[0]);
            }
        }
    }

    private static DvDateTime decodeDvDateTime(Timestamp timestamp, String timezone){
        if (timestamp == null) return null;
        DateTime codedDateTime;

        if (timezone != null)
            codedDateTime = new DateTime(timestamp, DateTimeZone.forID(timezone));
        else
            codedDateTime = new DateTime(timestamp);

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat);
        String convertedDateTime = dateTimeFormatter.print(codedDateTime);
        return new DvDateTime().parse(convertedDateTime);
    }

    @Override
    public EventContext mapRmEventContext(){

        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();

        //get the facility entry
        PartyIdentifiedRecord partyIdentifiedRecord = context.fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(eventContextRecord.getFacility()));
        //facility identifiers
        PartyIdentified healthCareFacility = null;

        if (partyIdentifiedRecord != null) {
            List<DvIdentifier> identifiers = new ArrayList<>();

            context.fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(partyIdentifiedRecord.getId())).forEach(record -> {
                DvIdentifier dvIdentifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
                identifiers.add(dvIdentifier);
            });

            //get PartyRef values from record
            PartyRef partyRef;
            if (partyIdentifiedRecord.getPartyRefValue() != null && partyIdentifiedRecord.getPartyRefScheme() != null) {
                GenericID genericID = new GenericID(partyIdentifiedRecord.getPartyRefValue(), partyIdentifiedRecord.getPartyRefScheme());
                partyRef = new PartyRef(genericID, partyIdentifiedRecord.getPartyRefNamespace(), partyIdentifiedRecord.getPartyRefType());
            } else {
                ObjectID objectID = new HierObjectID("ref");
                partyRef = new PartyRef(objectID, partyIdentifiedRecord.getPartyRefNamespace(), partyIdentifiedRecord.getPartyRefType());
            }
            healthCareFacility = new PartyIdentified(partyRef, partyIdentifiedRecord.getName(), identifiers.isEmpty() ? null : identifiers);
        }

        List<Participation> participationList = new ArrayList<>();
        //get the participations
        context.fetch(PARTICIPATION, PARTICIPATION.EVENT_CONTEXT.eq(eventContextRecord.getId())).forEach(record -> {
            //retrieve performer
            PartyProxy performer = I_PartyIdentifiedAccess.retrievePartyIdentified(this, record.getPerformer());

            DvInterval<DvDateTime> startTime = null;
            if (record.getStartTime() != null) { //start time null value is allowed for participation
                startTime = new DvInterval<>(decodeDvDateTime(record.getStartTime(), record.getStartTimeTzid()), null);
            }

            DvCodedText mode = decodeDvCodedText(record.getMode());
            Participation participation = new Participation(performer,
                                                new DvText(record.getFunction()),
                                                mode,
                                                startTime,
                                                terminologyService);

            participationList.add(participation);
        });

        DvCodedText concept ;

        //retrieve the setting
        UUID settingUuid = eventContextRecord.getSetting();

        ConceptRecord conceptRecord = context.fetchOne(CONCEPT, CONCEPT.ID.eq(settingUuid).and(CONCEPT.LANGUAGE.eq("en")));

        if (conceptRecord != null){
            concept = new DvCodedText(conceptRecord.getDescription(), new CodePhrase("openehr", conceptRecord.getConceptid().toString()));
        }
        else
        {
            concept = new DvCodedText("event", new CodePhrase("openehr", "433"));
        }

        return new EventContext(healthCareFacility,
                decodeDvDateTime(eventContextRecord.getStartTime(), eventContextRecord.getStartTimeTzid()),
                decodeDvDateTime(eventContextRecord.getEndTime(), eventContextRecord.getEndTimeTzid()),
                participationList.isEmpty() ? null: participationList,
                eventContextRecord.getLocation() == null ? null : eventContextRecord.getLocation(),
                concept,
                null, //other context...
                terminologyService);

    }


    public EventContext mapRmEventContext(Result<?> records){

        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();

        //get the facility entry
//        PartyIdentifiedRecord partyIdentifiedRecord = context.fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(eventContextRecord.getFacility()));
        //facility identifiers
        PartyIdentified healthCareFacility = null;

        if (records.size() > 0) {
            List<DvIdentifier> identifiers = new ArrayList<>();

//            context.fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(partyIdentifiedRecord.getId())).forEach(record -> {
//                DvIdentifier dvIdentifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
//                identifiers.add(dvIdentifier);
//            });

            //get PartyRef values from record
            healthCareFacility = I_PartyIdentifiedFacilityAccess.retrievePartyIdentified(records);
        }

        List<Participation> participationList = new ArrayList<>();
        //get the participations
        for (Record record: records){
            if (record.getValue(I_CompositionAccess.F_PARTICIPATION_FUNCTION) != null){
                //performer bit
                PartyProxy performer = I_PartyIdentifiedPerformerAccess.retrievePartyIdentified(record);
                DvInterval<DvDateTime> startTime = null;
                if (record.getValue(I_CompositionAccess.F_PARTICIPATION_START_TIME) != null) { //start time null value is allowed for participation
                    startTime = new DvInterval<>(decodeDvDateTime((Timestamp)record.getValue(I_CompositionAccess.F_PARTICIPATION_START_TIME), (String)record.getValue(I_CompositionAccess.F_PARTICIPATION_START_TIME_TZID)), null);
                }
                DvCodedText mode = decodeDvCodedText((String)record.getValue(I_CompositionAccess.F_PARTICIPATION_MODE));

                Participation participation = new Participation(performer,
                        new DvText((String)record.getValue(I_CompositionAccess.F_PARTICIPATION_FUNCTION)),
                        mode,
                        startTime,
                        terminologyService);

                participationList.add(participation);
            }
        }

        Integer conceptId = (Integer)records.getValue(0, I_CompositionAccess.F_CONCEPT_ID);
        String description = (String) records.getValue(0, I_CompositionAccess.F_CONCEPT_DESCRIPTION);

        DvCodedText concept = new DvCodedText(description, new CodePhrase("openehr", conceptId.toString()));

        //retrieve the setting
//        UUID settingUuid = eventContextRecord.getSetting();
//
//        ConceptRecord conceptRecord = context.fetchOne(CONCEPT, CONCEPT.ID.eq(settingUuid).and(CONCEPT.LANGUAGE.eq("en")));
//
//        if (conceptRecord != null){
//            concept = new DvCodedText(conceptRecord.getDescription(), new CodePhrase("openehr", conceptRecord.getConceptid().toString()));
//        }
//        else
//        {
//            concept = new DvCodedText("event", new CodePhrase("openehr", "433"));
//        }

        return new EventContext(healthCareFacility,
                decodeDvDateTime((Timestamp)records.getValue(0, I_CompositionAccess.F_CONTEXT_START_TIME), (String)records.getValue(0, I_CompositionAccess.F_CONTEXT_START_TIME_TZID)),
                decodeDvDateTime((Timestamp)records.getValue(0, I_CompositionAccess.F_CONTEXT_END_TIME), (String)records.getValue(0, I_CompositionAccess.F_CONTEXT_END_TIME_TZID)),
                participationList.isEmpty() ? null: participationList,
                records.getValue(0, I_CompositionAccess.F_CONTEXT_LOCATION) == null ? null : (String)records.getValue(0, I_CompositionAccess.F_CONTEXT_LOCATION),
                concept,
                null, //other context...
                terminologyService);

    }

    @Override
    public String getOtherContextJson(){
        if (eventContextRecord.getOtherContext() == null)
            return null;
        return ((PGobject) eventContextRecord.getOtherContext()).getValue();
    }

    public static EventContext retrieveHistoricalEventContext(I_DomainAccess domainAccess, UUID compositionId, Timestamp transactionTime){

        //use fetch any since duplicates are possible during tests...
        EventContextHistoryRecord eventContextHistoryRecord = domainAccess.getContext()
                .fetchAny(EVENT_CONTEXT_HISTORY, EVENT_CONTEXT_HISTORY.COMPOSITION_ID.eq(compositionId)
                        .and(EVENT_CONTEXT_HISTORY.SYS_TRANSACTION.eq(transactionTime)));

        if (eventContextHistoryRecord == null) return null; //no matching version for this composition


        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();

        //get the facility entry
        PartyIdentified healthCareFacility = null;

        if (eventContextHistoryRecord.getFacility() != null) {
            PartyIdentifiedRecord partyIdentifiedRecord = domainAccess.getContext()
                    .fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(eventContextHistoryRecord.getFacility()));
            //facility identifiers

            if (partyIdentifiedRecord != null) {
                List<DvIdentifier> identifiers = new ArrayList<>();

                domainAccess.getContext().fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(partyIdentifiedRecord.getId())).forEach(record -> {
                    DvIdentifier dvIdentifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
                    identifiers.add(dvIdentifier);
                });

                //get PartyRef values from record
                PartyRef partyRef;
                if (partyIdentifiedRecord.getPartyRefValue() != null && partyIdentifiedRecord.getPartyRefScheme() != null) {
                    GenericID genericID = new GenericID(partyIdentifiedRecord.getPartyRefValue(), partyIdentifiedRecord.getPartyRefScheme());
                    partyRef = new PartyRef(genericID, partyIdentifiedRecord.getPartyRefNamespace(), partyIdentifiedRecord.getPartyRefType());
                } else {
                    ObjectID objectID = new HierObjectID("ref");
                    partyRef = new PartyRef(objectID, partyIdentifiedRecord.getPartyRefNamespace(), partyIdentifiedRecord.getPartyRefType());
                }
                healthCareFacility = new PartyIdentified(partyRef, partyIdentifiedRecord.getName(), identifiers.isEmpty() ? null : identifiers);
            }
        }

        List<Participation> participationList = new ArrayList<>();
        //get the participations
        domainAccess.getContext().fetch(PARTICIPATION_HISTORY,
                PARTICIPATION_HISTORY.EVENT_CONTEXT.eq(eventContextHistoryRecord.getId())
                        .and(PARTICIPATION_HISTORY.SYS_TRANSACTION.eq(transactionTime)))
                .forEach(record -> {
                    //retrieve performer
                    PartyProxy performer = I_PartyIdentifiedAccess.retrievePartyIdentified(domainAccess, record.getPerformer());


                    DvInterval<DvDateTime> startTime = new DvInterval<>(decodeDvDateTime(record.getStartTime(), record.getStartTimeTzid()), null);
                    DvCodedText mode = decodeDvCodedText(record.getMode());
                    Participation participation = new Participation(performer,
                            new DvText(record.getFunction()),
                            mode,
                            startTime,
                            terminologyService);

                    participationList.add(participation);
                });

        DvCodedText concept ;

        //retrieve the setting
        UUID settingUuid = eventContextHistoryRecord.getSetting();

        ConceptRecord conceptRecord = domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.ID.eq(settingUuid).and(CONCEPT.LANGUAGE.eq("en")));

        if (conceptRecord != null){
            concept = new DvCodedText(conceptRecord.getDescription(), new CodePhrase("openehr", conceptRecord.getConceptid().toString()));
        }
        else
        {
            concept = new DvCodedText("event", new CodePhrase("openehr", "433"));
        }

        return new EventContext(healthCareFacility,
                decodeDvDateTime(eventContextHistoryRecord.getStartTime(), eventContextHistoryRecord.getStartTimeTzid()),
                decodeDvDateTime(eventContextHistoryRecord.getEndTime(), eventContextHistoryRecord.getEndTimeTzid()),
                participationList.isEmpty() ? null: participationList,
                eventContextHistoryRecord.getLocation() == null ? null : eventContextHistoryRecord.getLocation(),
                concept,
                null, //other context...
                terminologyService);

    }

    /**
     * commit an event context from a composition<br>
     * The commit action, involves the following sequence
     * <ul>
     *     <li>if the event performer does not exists</li>
     *     <ul>
     *         <li>storeComposition an entry in PARTY_IDENTIFIED</li>
     *         <li>storeComposition the identifiers entries as supplied in IDENTIFIER</li>
     *     </ul>
     *     <li>if performer exists retrieve its UUID from PARTY_IDENTIFIED</li>
     *     <li>storeComposition a participation record</li>
     *     <li>storeComposition an event context record</li>
     * </ul>
     * @param eventContext
     */
    private void commitEventContext(EventContext eventContext){

    }

    @Override
    public void setCompositionId(UUID compositionId){
        eventContextRecord.setCompositionId(compositionId);
    }

    @Override
    public UUID getId(){
        return eventContextRecord.getId();
    }

}
