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
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.ehr.encode.*;
import com.ethercis.ehr.keyvalues.EcisFlattener;
import com.ethercis.jooq.pg.enums.ContributionDataType;
import com.ethercis.jooq.pg.tables.records.ContributionRecord;
import com.ethercis.jooq.pg.tables.records.EhrRecord;
import com.ethercis.jooq.pg.tables.records.IdentifierRecord;
import com.ethercis.jooq.pg.tables.records.StatusRecord;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.transform.rawjson.RawJsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.postgresql.util.PGobject;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by Christian Chevalley on 4/17/2015.
 */
public class EhrAccess extends DataAccess implements  I_EhrAccess {

    private static final Logger log = LogManager.getLogger(EhrAccess.class);
    private EhrRecord ehrRecord;
    private StatusRecord statusRecord = null;
    private boolean isNew = false;

    //holds the non serialized archetyped other_details structure
    private Locatable otherDetails = null;
    private String otherDetailsSerialized = null; //kind of a hack really, used to deal with RAW JSON without a template!
    private String otherDetailsTemplateId;

    private I_ContributionAccess contributionAccess = null; //locally referenced contribution associated to ehr transactions

    //set this variable to change the identification  mode in status
    private PARTY_MODE party_identifier = PARTY_MODE.EXTERNAL_REF;


    public enum PARTY_MODE {IDENTIFIER, EXTERNAL_REF}

    public EhrAccess(DSLContext context, UUID partyId, UUID systemId, UUID directoryId, UUID accessId) throws Exception {
        super(context, null);

        this.ehrRecord = context.newRecord(EHR);
        ehrRecord.setId(UUID.randomUUID());

        //retrieveInstanceByNamedSubject an existing status for this party (which should not occur
        if (!context.fetch(STATUS, STATUS.PARTY.eq(partyId)).isEmpty())
        {
            log.warn("This party is already associated to an EHR");
            throw new IllegalArgumentException("Party:"+partyId+" already associated to an EHR, please retrieveInstanceByNamedSubject the associated EHR for updates instead");
        }

        //storeComposition a new status
        statusRecord = context.newRecord(STATUS);
        statusRecord.setId(UUID.randomUUID());
        statusRecord.setIsModifiable(true);
        statusRecord.setIsQueryable(true);
        statusRecord.setParty(partyId);
        statusRecord.setEhrId(ehrRecord.getId());

        ehrRecord.setSystemId(systemId);
        ehrRecord.setDirectory(directoryId);
        ehrRecord.setAccess(accessId);

        if (ehrRecord.getSystemId() == null){ //storeComposition a default entry for the current system
            ehrRecord.setSystemId(I_SystemAccess.createOrRetrieveLocalSystem(this));
        }


        this.isNew = true;

        //associate a contribution with this composition
        contributionAccess =  I_ContributionAccess.getInstance(this, ehrRecord.getId());
        contributionAccess.setState(ContributionDef.ContributionState.COMPLETE);

    }


    public EhrAccess(I_DomainAccess domainAccess){
        super(domainAccess.getContext(), domainAccess.getKnowledgeManager());
        //associate a contribution with this composition
        contributionAccess =  I_ContributionAccess.getInstance(this, null);
        contributionAccess.setState(ContributionDef.ContributionState.COMPLETE);
    }

    private String serializeOtherDetails() throws Exception {
        I_CompositionSerializer serializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, true);
        return serializer.dbEncode(CompositionSerializer.TAG_OTHER_DETAILS, otherDetails);
//        Map<String, Object>retmap = serializer.processItem(CompositionSerializer.TAG_OTHER_DETAILS, otherDetails);
//        //the template id is encoded into the json structure
//        if (otherDetailsTemplateId != null)
//            retmap.put(TAG_TEMPLATE_ID, otherDetailsTemplateId);
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//        builder.registerTypeAdapter(DvDate.class, new DvDateAdapter());
//        builder.registerTypeAdapter(DvTime.class, new DvTimeAdapter());
//        builder.registerTypeAdapter(DvDuration.class, new DvDurationAdapter());
//        Gson gson = builder.setPrettyPrinting().create();
//        return gson.toJson(retmap);
    }

    @Override
    public void setAccess(UUID access) {
        ehrRecord.setAccess(access);
    }

    @Override
    public void setDirectory(UUID directory) {
        ehrRecord.setDirectory(directory);
    }

    @Override
    public void setSystem(UUID system) {
        ehrRecord.setSystemId(system);
    }

    @Override
    public void setModifiable(Boolean modifiable){
        statusRecord.setIsModifiable(modifiable);
    }

    @Override
    public void setQueryable(Boolean queryable){
        statusRecord.setIsQueryable(queryable);
    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {

        ehrRecord.setDateCreated(transactionTime);
//        ehrRecord.setDateCreatedTzid(transactionTime.toLocalDateTime().getZone().getID());
        ehrRecord.store();
        
        if (isNew && statusRecord != null) {
            UUID uuid = UUID.randomUUID();
            InsertQuery<?> insertQuery = context.insertQuery(STATUS);
            insertQuery.addValue(STATUS.ID, uuid);
            insertQuery.addValue(STATUS.EHR_ID, ehrRecord.getId());
            insertQuery.addValue(STATUS.IS_QUERYABLE, statusRecord.getIsQueryable());
            insertQuery.addValue(STATUS.IS_MODIFIABLE, statusRecord.getIsModifiable());
            insertQuery.addValue(STATUS.PARTY, statusRecord.getParty());
//            Field otherDetailsField = DSL.field(STATUS.OTHER_DETAILS+"::jsonb");
            if (otherDetails != null) {
                insertQuery.addValue(STATUS.OTHER_DETAILS, (Object)DSL.field(DSL.val(serializeOtherDetails())+"::jsonb"));
            }
            else if (otherDetailsSerialized != null)
                insertQuery.addValue(STATUS.OTHER_DETAILS, (Object)DSL.field(DSL.val(otherDetailsSerialized)+"::jsonb"));

            insertQuery.addValue(STATUS.SYS_TRANSACTION, transactionTime);

            Integer result = insertQuery.execute();

            if (result == 0)
                throw new IllegalArgumentException("Could not store Ehr Status");
//            statusRecord.store();
        }

        return ehrRecord.getId();
    }

    @Deprecated
    public UUID commit_deprecated(Timestamp transactionTime) throws Exception {

        ehrRecord.setDateCreated(transactionTime);
//        ehrRecord.setDateCreatedTzid(transactionTime.toLocalDateTime().getZone().getID());
        ehrRecord.store();

        if (isNew && statusRecord != null) {
            String sql = "INSERT INTO ehr.status (ehr_id, is_queryable, is_modifiable, party, other_details, sys_transaction) " +
                    "VALUES (?, ?, ?, ?, ?::jsonb, ?)" +
                    " RETURNING id";

            statusRecord.setSysTransaction(transactionTime);

            Connection connection = context.configuration().connectionProvider().acquire();
            PreparedStatement insertStatement = connection.prepareStatement(sql);
            insertStatement.setObject(1, ehrRecord.getId());
            insertStatement.setBoolean(2, statusRecord.getIsQueryable());
            insertStatement.setBoolean(3, statusRecord.getIsModifiable());
            insertStatement.setObject(4, statusRecord.getParty());

            if (otherDetails != null) {
                insertStatement.setObject(5, serializeOtherDetails());
            }
            else if (otherDetailsSerialized != null)
                insertStatement.setObject(5, otherDetailsSerialized);
            else
                insertStatement.setObject(5, null);

            insertStatement.setTimestamp(6, statusRecord.getSysTransaction());
            ResultSet resultSet = insertStatement.executeQuery();

            String retval  = null;

            if (resultSet != null && resultSet.next()){
                retval = resultSet.getString(1);
            }

            connection.close();

            if (retval == null)
                throw new IllegalArgumentException("Could not store Ehr Status");
//            statusRecord.store();
        }

        return ehrRecord.getId();
    }


    @Override
    public UUID commit() throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        //associate a contribution with this ehr
        UUID uuid = commit(timestamp);
        contributionAccess =  I_ContributionAccess.getInstance(this, uuid);
        contributionAccess.commit(timestamp, null, null, ContributionDataType.ehr, ContributionDef.ContributionState.COMPLETE, I_ConceptAccess.ContributionChangeType.creation, null);
        return uuid;
    }

    @Override
    public UUID commit(UUID committerId, UUID systemId, String description) throws Exception{
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        //associate a contribution with this ehr
        UUID uuid = commit(timestamp);
        contributionAccess =  I_ContributionAccess.getInstance(this, ehrRecord.getId());
        contributionAccess.commit(timestamp, committerId, systemId, ContributionDataType.ehr, ContributionDef.ContributionState.COMPLETE, I_ConceptAccess.ContributionChangeType.creation, description);
        return uuid;
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws Exception {
        return update(transactionTime, false);
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        boolean result = false;

        if (force || statusRecord.changed()){

            UpdateQuery<?> updateQuery = context.updateQuery(STATUS);
            updateQuery.addValue(STATUS.EHR_ID, ehrRecord.getId());
            updateQuery.addValue(STATUS.IS_QUERYABLE, statusRecord.getIsQueryable());
            updateQuery.addValue(STATUS.IS_MODIFIABLE, statusRecord.getIsModifiable());
            updateQuery.addValue(STATUS.PARTY, statusRecord.getParty());

//            Field otherDetailsField = DSL.field(STATUS.OTHER_DETAILS+"::jsonb");
            if (otherDetails != null) {
                updateQuery.addValue(STATUS.OTHER_DETAILS, (Object)DSL.field(DSL.val(serializeOtherDetails())+"::jsonb"));
            }
            else if (otherDetailsSerialized != null)
                updateQuery.addValue(STATUS.OTHER_DETAILS, (Object)DSL.field(DSL.val(otherDetailsSerialized)+"::jsonb"));

            updateQuery.addValue(STATUS.SYS_TRANSACTION, transactionTime);
            updateQuery.addConditions(STATUS.ID.eq(statusRecord.getId()));

            result |= updateQuery.execute() > 0;
        }

        if (force || ehrRecord.changed()){
            DateTime committedDateTime = DateTime.now();
            ehrRecord.setDateCreated(new Timestamp(committedDateTime.getMillis()));
            ehrRecord.setDateCreatedTzid(committedDateTime.getZone().getID());
            result |= ehrRecord.update() > 0;

        }

        return result;
    }

    @Deprecated
    public Boolean update_deprecated(Timestamp transactionTime, boolean force) throws Exception {
        boolean result = false;

        if (force || statusRecord.changed()){
            String sql = "UPDATE ehr.status SET " +
                    "is_queryable = ?, " +
                    "is_modifiable = ?," +
                    "party = ?, " +
                    "other_details = ?::jsonb, " +
                    "sys_transaction = ? " +
                    "WHERE id = ? ";

            statusRecord.setSysTransaction(transactionTime);
            Connection connection = context.configuration().connectionProvider().acquire();
            PreparedStatement updateStatement = connection.prepareStatement(sql);
            updateStatement.setBoolean(1, statusRecord.getIsQueryable());
            updateStatement.setBoolean(2, statusRecord.getIsModifiable());
            updateStatement.setObject(3, statusRecord.getParty());

            if (otherDetails != null) {
                updateStatement.setObject(4, serializeOtherDetails());
            }
            else if (otherDetailsSerialized != null)
                updateStatement.setObject(4, otherDetailsSerialized);

            updateStatement.setTimestamp(5, statusRecord.getSysTransaction());
            updateStatement.setObject(6, statusRecord.getId());

//            result |= statusRecord.update() > 0;
            result |= updateStatement.execute();

            connection.close();
        }

        if (force || ehrRecord.changed()){
            DateTime committedDateTime = DateTime.now();
            ehrRecord.setDateCreated(new Timestamp(committedDateTime.getMillis()));
            ehrRecord.setDateCreatedTzid(committedDateTime.getZone().getID());
            result |= ehrRecord.update() > 0;

        }

        return result;
    }

    @Override
    public Boolean update() throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, null, null, null, null, I_ConceptAccess.ContributionChangeType.modification, null);
        return update(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        return update(new Timestamp(DateTime.now().getMillis()), force);
    }

    @Override
    public Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp);
    }

    @Override
    public Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description, Boolean force) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp, force);
    }

    @Override
    public Integer delete() throws Exception {
        return delete(null, null, null);
    }

    @Override
    public Integer delete(UUID committerId, UUID systemId, String description) throws Exception {

        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, ContributionDef.ContributionState.DELETED, I_ConceptAccess.ContributionChangeType.deleted, description);
        int count = 0;

        count += ehrRecord.delete();
        count += statusRecord.delete();

        return count;
    }


    public static UUID retrieveInstanceByNamedSubject(I_DomainAccess domainAccess, String subjectname){
        Record record;
        DSLContext context = domainAccess.getContext();

        try {
            record = context.select(STATUS.EHR_ID).from(STATUS)
                        .where(STATUS.PARTY.eq
                                        (context.select(PARTY_IDENTIFIED.ID)
                                                        .from(PARTY_IDENTIFIED)
                                                        .where(PARTY_IDENTIFIED.NAME.eq(subjectname))
                                        )
                        ).fetchOne();
        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not retrieveInstanceByNamedSubject ehr for party:"+subjectname+" exception:"+e);
            throw new IllegalArgumentException("Could not retrieveInstanceByNamedSubject EHR for party:"+subjectname+" exception:"+e);
        }

        if (record.size() == 0){
            log.warn("Could not retrieveInstanceByNamedSubject ehr for party:"+subjectname);
            return null;
        }

        return (UUID)record.getValue(0);
    }

    public static UUID retrieveInstanceBySubject(I_DomainAccess domainAccess, UUID subjectUuid){
        Record record;
        DSLContext context = domainAccess.getContext();

        try {
            record = context.select(STATUS.EHR_ID).from(STATUS)
                    .where(STATUS.PARTY.eq
                                    (context.select(PARTY_IDENTIFIED.ID)
                                                    .from(PARTY_IDENTIFIED)
                                                    .where(PARTY_IDENTIFIED.ID.eq(subjectUuid))
                                    )
                    ).fetchOne();

        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not ehr for party:"+subjectUuid+" exception:"+e);
            throw new IllegalArgumentException("Could not EHR for party:"+subjectUuid+" exception:"+e);
        }

        if (record == null || record.size() == 0){
            log.warn("Could not retrieve ehr for party:"+subjectUuid);
            return null;
        }

        return (UUID)record.getValue(0);
    }

    public static UUID retrieveInstanceBySubject(I_DomainAccess domainAccess, String subjectId, String issuerSpace){
        Record record;
        DSLContext context = domainAccess.getContext();

        //get the corresponding party Id from the codification space provided by an issuer
        IdentifierRecord identifierRecord = context.fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(subjectId).and(IDENTIFIER.ISSUER.eq(issuerSpace)));

        if (identifierRecord == null)
            throw new IllegalArgumentException("Could not invalidateContent an identified party for code:"+subjectId+" issued by:"+issuerSpace);

        try {
            record = context.select(STATUS.EHR_ID).from(STATUS)
                    .where(STATUS.PARTY.eq
                                    (context.select(PARTY_IDENTIFIED.ID)
                                                    .from(PARTY_IDENTIFIED)
                                                    .where(PARTY_IDENTIFIED.ID.eq(identifierRecord.getParty()))
                                    )
                    ).fetchOne();

        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not ehr for party:"+subjectId+" exception:"+e);
            throw new IllegalArgumentException("Could not EHR for party:"+subjectId+" exception:"+e);
        }

        if (record == null || record.size() == 0){
            log.warn("Could not retrieve ehr for party:"+subjectId);
            return null;
        }

        return (UUID)record.getValue(0);
    }

    public static UUID retrieveInstanceBySubjectExternalRef(I_DomainAccess domainAccess, String subjectId, String issuerSpace){
        Record record;
        DSLContext context = domainAccess.getContext();

          try {
            record = context.select(STATUS.EHR_ID).from(STATUS)
                    .where(STATUS.PARTY.eq
                                    (context.select(PARTY_IDENTIFIED.ID)
                                                    .from(PARTY_IDENTIFIED)
                                                    .where(PARTY_IDENTIFIED.PARTY_REF_VALUE.eq(subjectId)
                                                    .and(PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.eq(issuerSpace)))
                                    )
                    ).fetchOne();

        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not ehr for party:"+subjectId+" exception:"+e);
            throw new IllegalArgumentException("Could not retrieve EHR for party:"+subjectId+" exception:"+e);
        }

        if (record == null || record.size() == 0){
            log.warn("Could not retrieve ehr for party:"+subjectId);
            return null;
        }

        return (UUID)record.getValue(0);
    }


    public UUID retrieve(String partyName){
        return retrieveInstanceByNamedSubject(this, partyName);
    }


    public I_EhrAccess retrieveByStatus(UUID status) {
        return retrieveInstanceByStatus(this, status);
    }


    public static I_EhrAccess retrieveInstanceByStatus(I_DomainAccess domainAccess, UUID status){
        EhrAccess ehrAccess = new EhrAccess(domainAccess);

        Record record;

        ehrAccess.statusRecord  = domainAccess.getContext().fetchOne(STATUS, STATUS.ID.eq(status));

        try {
            record = domainAccess.getContext().selectFrom(EHR)
                    .where(EHR.ID.eq(ehrAccess.statusRecord.getEhrId()))
                    .fetchOne();
        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not retrieveInstanceByNamedSubject ehr for status:"+status+" exception:"+e);
            throw new IllegalArgumentException("Could not retrieveInstanceByNamedSubject EHR for status:"+status+" exception:"+e);
        }

        if (record.size() == 0){
            log.warn("Could not retrieveInstanceByNamedSubject ehr for status:"+status);
            return null;
        }

        ehrAccess.ehrRecord = (EhrRecord)record;

        ehrAccess.isNew = false;

        return ehrAccess;
    }

    public static I_EhrAccess retrieveInstance(I_DomainAccess domainAccess, UUID ehrId) throws Exception {
        DSLContext context = domainAccess.getContext();
        EhrAccess ehrAccess = new EhrAccess(domainAccess);

        Record record;

        try {
            record = context.selectFrom(EHR)
                    .where(EHR.ID.eq(ehrId))
                    .fetchOne();
        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not retrieveInstanceByNamedSubject ehr for id:"+ehrId+" exception:"+e);
            throw new IllegalArgumentException("Could not retrieveInstanceByNamedSubject EHR for id:"+ehrId+" exception:"+e);
        }

        if (record == null || record.size() == 0){
            log.warn("Could not retrieveInstanceByNamedSubject ehr for id:"+ehrId);
            return null;
        }

        ehrAccess.ehrRecord = (EhrRecord)record;
        //retrieveInstanceByNamedSubject the corresponding status
        ehrAccess.statusRecord = context.fetchOne(STATUS, STATUS.EHR_ID.eq(ehrAccess.ehrRecord.getId()));

        //rebuild otherDetails
        if (ehrAccess.statusRecord.getOtherDetails() != null){
            String serialized = ((PGobject) ehrAccess.statusRecord.getOtherDetails()).getValue();

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.setPrettyPrinting().create();

            Map structured = gson.fromJson(serialized, Map.class);

            if (structured.containsKey(I_EhrAccess.TAG_TEMPLATE_ID)){
//                throw new IllegalArgumentException("Serialized other details does not contain its template Id");

                ehrAccess.otherDetailsTemplateId = (String) structured.get(I_EhrAccess.TAG_TEMPLATE_ID);

                //identify the template name
                I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, domainAccess.getKnowledgeManager(), ehrAccess.otherDetailsTemplateId);
                Locatable locatable = contentBuilder.buildLocatableFromJson(serialized);

                if (locatable instanceof ItemStructure)
                    ehrAccess.otherDetails = (ItemStructure) locatable;
                else
                    throw new IllegalArgumentException("Retrieved structure in other details is not an ItemStructure");
            }
            else {
                //could return other_status under ECISFLAT
//                Map<String, Object> retmap = gson.fromJson(serialized, TreeMap.class);
//                Map<String, String> flatten = EcisFlattener.generateEcisFlat(retmap);
                ehrAccess.otherDetailsSerialized = serialized;
//                log.warn("other_details without a templateId cannot be rebuilt... sorry");
            }
        }

        ehrAccess.isNew = false;

        //retrieve the current contribution for this ehr
        ContributionRecord contributionRecord = context.fetchOne(CONTRIBUTION, CONTRIBUTION.EHR_ID.eq(ehrAccess.ehrRecord.getId()).and(CONTRIBUTION.CONTRIBUTION_TYPE.eq(ContributionDataType.ehr)));
        if (contributionRecord == null)
            throw new IllegalArgumentException("DB inconsistency: could not find a related contribution for ehr="+ehrAccess.ehrRecord.getId());

        UUID contributionId = contributionRecord.getId();

        if (contributionId != null){
            ehrAccess.setContributionAccess(I_ContributionAccess.retrieveInstance(domainAccess, contributionId));
        }

        return ehrAccess;
    }

    @Override
    public UUID reload(){
        Record record;

        try {
            record = context.selectFrom(EHR)
                    .where(EHR.ID.eq(getId()))
                    .fetchOne();
        }
        catch (Exception e){ //possibly not unique for a party: this is not permitted!
            log.warn("Could not retrieveInstanceByNamedSubject ehr for id:"+ getId()+" exception:"+e);
            throw new IllegalArgumentException("Could not retrieveInstanceByNamedSubject EHR for id:"+ getId()+" exception:"+e);
        }

        if (record == null || record.size() == 0){
            log.warn("Could not retrieveInstanceByNamedSubject ehr for id:"+ getId());
            return null;
        }

        ehrRecord = (EhrRecord)record;
        //retrieveInstanceByNamedSubject the corresponding status
        statusRecord = context.fetchOne(STATUS, STATUS.EHR_ID.eq(ehrRecord.getId()));
        isNew = false;

        return getId();
    }

    public I_EhrAccess retrieve(UUID id) throws Exception {
        return retrieveInstance(this, id);
    }

    public  EhrRecord getEhrRecord() {
        return ehrRecord;
    }

    public  StatusRecord getStatusRecord() {
        return statusRecord;
    }

    public boolean isNew() {
        return isNew;
    }

    /**
     * getNewInstance the identifiers of the subject owner<br>
     * the identifiers are formatted as: "CODE:ISSUER"
     * @param ehrId
     * @return
     */
    public static Map<String, String> fetchSubjectIdentifiers(I_DomainAccess domainAccess, UUID ehrId) throws Exception {
        EhrAccess ehrAccess = (EhrAccess) retrieveInstance(domainAccess, ehrId);
        DSLContext context = domainAccess.getContext();

        if (ehrAccess == null)
            throw new IllegalArgumentException("No ehr found for id:" + ehrId);

        Map<String, String> idlist = MapUtils.multiValueMap(new HashMap<String, String>());

        //getNewInstance the corresponding subject Identifiers

        context.selectFrom(IDENTIFIER).
                where(IDENTIFIER.PARTY.eq(getParty(ehrAccess))).fetch()
                .forEach(record -> {
                    idlist.put("identifier_issuer", record.getIssuer());
                    idlist.put("identifier_id_value", record.getIdValue());
                });

        //get the list of ref attributes
        context.selectFrom(PARTY_IDENTIFIED)
                .where(PARTY_IDENTIFIED.ID.eq(getParty(ehrAccess)))
                .fetch()
                .forEach(record -> {
                    idlist.put("ref_name_space", record.getPartyRefNamespace());
                    idlist.put("id_value", record.getPartyRefValue());
                    idlist.put("ref_name_scheme", record.getPartyRefScheme());
                    idlist.put("ref_party_type", record.getPartyRefType());
                });

        return idlist;
    }

    public static Map<String, Map<String, String>> getCompositionList(I_DomainAccess domainAccess, UUID ehrId) throws Exception {
        EhrAccess ehrAccess = (EhrAccess) retrieveInstance(domainAccess, ehrId);
        DSLContext context = domainAccess.getContext();

        if (ehrAccess == null)
            throw new IllegalArgumentException("No ehr found for id:" + ehrId);

        Map<String, Map<String, String>> compositionlist = new HashMap<>();

        context.selectFrom(ENTRY).where(
                ENTRY.COMPOSITION_ID.eq(
                    context.select(COMPOSITION.ID).from(COMPOSITION).where(COMPOSITION.EHR_ID.eq(ehrId)))
                ).fetch().forEach(record -> {
            Map<String, String> details = new HashMap<>();
            details.put("composition_id", record.getCompositionId().toString());
            details.put("templateId", record.getTemplateId());
            details.put("date", record.getSysTransaction().toString());
            compositionlist.put("details", details);

        });

        return compositionlist;
    }

    public static UUID getParty(EhrAccess ehrAccess){
        return ehrAccess.getStatusRecord().getParty();
    }

    @Override
    public UUID getParty(){
        return statusRecord.getParty();
    }

    @Override
    public void setParty(UUID partyId){
        statusRecord.setParty(partyId);
    }

    @Override
    public UUID getId(){
        return ehrRecord.getId();
    }

    @Override
    public Boolean isModifiable(){
        return statusRecord.getIsModifiable();
    }

    @Override
    public Boolean isQueryable(){
        return statusRecord.getIsQueryable();
    }

    @Override
    public UUID getSystemId(){
        return ehrRecord.getSystemId();
    }

    @Override
    public UUID getStatusId(){
        return context.fetchOne(STATUS, STATUS.EHR_ID.eq(ehrRecord.getId())).getId();
    }

    @Override
    public UUID getDirectoryId(){
        return ehrRecord.getDirectory();
    }

    @Override
    public UUID getAccessId(){
        return ehrRecord.getAccess();
    }

    @Override
    public void setContributionAccess(I_ContributionAccess contributionAccess) {
        this.contributionAccess = contributionAccess;
    }
    
    @Override
    public void setOtherDetails(Locatable otherDetails, String templateId) {
        this.otherDetailsTemplateId = templateId;
        this.otherDetails = otherDetails;
    }

    @Override
    public void setOtherDetails(String otherDetails, String templateId){
        this.otherDetailsTemplateId = templateId;
        this.otherDetailsSerialized = otherDetails;
    }

    @Override
    public void setOtherDetails(Map otherDetails, String templateId) throws Exception {
        this.otherDetailsTemplateId = templateId;
        this.otherDetailsSerialized = RawJsonParser.dbEncode(otherDetails);
    }

    @Override
    public Locatable getOtherDetails(){
        return otherDetails;
    }

    @Override
    public String getOtherDetailsTemplateId() {
        return otherDetailsTemplateId;
    }

    @Override
    public String getOtherDetailsSerialized() {
        return otherDetailsSerialized;
    }

    public I_ContributionAccess getContributionAccess() {
        return contributionAccess;
    }

    @Override
    public String exportOtherDetailsXml() throws Exception {
        if (otherDetails != null) {
            I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(getKnowledgeManager(), otherDetailsTemplateId);
            String xml = new String(contentBuilder.exportCanonicalXML(otherDetails, true));
            //cosmetic
            xml = xml.replaceAll("frag:fragment", "items");
            xml = xml.replace("xmlns:frag=\"http://www.openuri.org/fragment\"", "");
            return xml;

        }
        else
            return null;
    }

    @Override
    public boolean isSetOtherDetails(){
        return otherDetails != null;
    }

    @Override
    public boolean isSetOtherDetailsSerialized(){
        return otherDetailsSerialized != null;
    }
}
