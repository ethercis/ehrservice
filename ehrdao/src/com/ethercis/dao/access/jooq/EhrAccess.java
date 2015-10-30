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

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EhrAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.dao.jooq.tables.records.EhrRecord;
import com.ethercis.dao.jooq.tables.records.IdentifierRecord;
import com.ethercis.dao.jooq.tables.records.StatusRecord;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.Timestamp;
import java.util.*;

import static com.ethercis.dao.jooq.Tables.*;

/**
 * Created by Christian Chevalley on 4/17/2015.
 */
public class EhrAccess extends DataAccess implements  I_EhrAccess {

    private static final Logger log = Logger.getLogger(EhrAccess.class);
    private static EhrRecord ehrRecord;
    private StatusRecord statusRecord = null;
    private static boolean isNew = false;

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

    }


    public EhrAccess(I_DomainAccess domainAccess){
        super(domainAccess.getContext(), domainAccess.getKnowledgeManager());
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
        ehrRecord.store();

        if (isNew && statusRecord != null) {
            statusRecord.setSysTransaction(transactionTime);
            statusRecord.store();
        }

        return ehrRecord.getId();
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws Exception {
        return update(transactionTime, false);
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        boolean result = false;

        if (force || statusRecord.changed()){
            statusRecord.setSysTransaction(transactionTime);
            result |= statusRecord.update() > 0;
        }

        if (force || ehrRecord.changed()){
            result |= ehrRecord.update() > 0;

        }

        return result;
    }

    @Override
    public Boolean update() throws Exception {
        return update(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        return update(new Timestamp(DateTime.now().getMillis()), force);
    }

    @Override
    public Integer delete() {

        int count = 0;

//        StatusRecord statusRecord = context.fetchOne(STATUS, STATUS.ID.eq(ehrRecord.getStatus()));
////        Result<ContributionRecord> contributionVersionRecords = context.fetch(CONTRIBUTION, CONTRIBUTION.EHR_ID.eq(ehrRecord.getId()));
//
//        //delete all contribution for this Ehr
//        context.fetch(CONTRIBUTION, CONTRIBUTION.EHR_ID.eq(ehrRecord.getId())).forEach( record -> {
//            try {
//                I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveInstance(this, record.getId());
//                contributionAccess.delete();
//            } catch (Exception e){
//                log.error("Could not retrieve contribution version:"+record.getId());
//            }
//        });

//        //delete all contribution_version records
//
//        count += context.delete(CONTRIBUTION_VERSION).where(CONTRIBUTION_VERSION.EHR_ID.eq(ehrRecord.getId())).execute();

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

    public static UUID retrieveInstanceBySubject(I_DomainAccess domainAccess, String subjectId, String issuerSpace){
        Record record;
        DSLContext context = domainAccess.getContext();

        //get the corresponding party Id from the codification space provided by an issuer
        IdentifierRecord identifierRecord = context.fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(subjectId).and(IDENTIFIER.ISSUER.eq(issuerSpace)));

        if (identifierRecord == null)
            throw new IllegalArgumentException("Could not process an identified party for code:"+subjectId+" issued by:"+issuerSpace);

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

    public static I_EhrAccess retrieveInstance(I_DomainAccess domainAccess, UUID ehrId){
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
        ehrAccess.statusRecord = context.fetchOne(STATUS, STATUS.EHR_ID.eq(ehrRecord.getId()));
        ehrAccess.isNew = false;

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

    public I_EhrAccess retrieve(UUID id){
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
    public static Map<String, String> fetchSubjectIdentifiers(I_DomainAccess domainAccess, UUID ehrId) {
        EhrAccess ehrAccess = (EhrAccess) retrieveInstance(domainAccess, ehrId);
        DSLContext context = domainAccess.getContext();

        if (ehrAccess == null)
            throw new IllegalArgumentException("No ehr found for id:" + ehrId);

        Map<String, String> idlist = new HashMap<>();

        //getNewInstance the corresponding subject Identifiers
        context.selectFrom(IDENTIFIER).
                where(IDENTIFIER.PARTY.eq(getParty(ehrAccess))).fetch()
                .forEach(record -> {
                    idlist.put(record.getIssuer(), record.getIdValue());
                });

        return idlist;
    }

    public static Map<String, Map<String, String>> getCompositionList(I_DomainAccess domainAccess, UUID ehrId) {
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
}
