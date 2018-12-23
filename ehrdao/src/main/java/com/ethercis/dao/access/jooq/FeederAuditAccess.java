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

import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_FeederAuditAccess;
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.jooq.pg.tables.records.FeederAuditRecord;
import com.ethercis.jooq.pg.tables.records.FeederSystemAuditRecord;
import com.ethercis.jooq.pg.tables.records.OriginatingSystemAuditRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.FeederAuditDetails;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.encapsulated.DvParsable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by Christian Chevalley on 4/9/2015.
 */
public class FeederAuditAccess extends DataAccess implements I_FeederAuditAccess {

    private FeederAuditRecord feederAuditRecord;
    private static FeederAudit feederAudit;


    private static Logger log = LogManager.getLogger(FeederAuditAccess.class);


    public FeederAuditAccess(DSLContext context, FeederAudit feederAudit) throws Exception {
        super(context, null, null);
        this.context = context;
        feederAuditRecord = context.newRecord(FEEDER_AUDIT);
        this.feederAudit = feederAudit;
    }

    public FeederAuditAccess(I_DomainAccess domainAccess) {
        super(domainAccess);
    }

    public FeederAuditAccess() {
        super(null, null, null);
    }


    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {
        feederAuditRecord.setOriginatingSystemAudit(commitOriginatingSystemAudit(feederAudit.getOriginatingSystemAudit()));
        feederAuditRecord.setFeederSystemAudit(commitFeederSystemAudit(feederAudit.getFeederSystemAudit()));

        feederAuditRecord.setOriginalContent(((DvParsable) feederAudit.getOriginalContent()).getValue().getBytes());

        feederAuditRecord.store();

        //handle DvIdentifiers
        for (DvIdentifier identifier : feederAudit.getOriginatingSystemItemIds()) {
            commitOriginatingSystemItemIds(feederAuditRecord.getId(), identifier);
        }

        for (DvIdentifier identifier : feederAudit.getFeederSystemItemIds()) {
            commitFeederSystemItemIds(feederAuditRecord.getId(), identifier);
        }

        return feederAuditRecord.getId();
    }

    int commitOriginatingSystemItemIds(UUID parentUUID, DvIdentifier identifier) {
        //handle DvIdentifiers
        //push into the corresponding identifiers
        return context.insertInto(ORIGINATING_SYSTEM_ITEM_IDS,
                ORIGINATING_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID,
                ORIGINATING_SYSTEM_ITEM_IDS.ISSUER,
                ORIGINATING_SYSTEM_ITEM_IDS.ASSIGNER,
                ORIGINATING_SYSTEM_ITEM_IDS.ID_VALUE,
                ORIGINATING_SYSTEM_ITEM_IDS.TYPE_NAME
        ).values(
                parentUUID,
                identifier.getIssuer(),
                identifier.getAssigner(),
                identifier.getId(),
                identifier.getType()
        ).execute();
    }

    int commitFeederSystemItemIds(UUID parentUUID, DvIdentifier identifier) {
        //handle DvIdentifiers
        //push into the corresponding identifiers
        return context.insertInto(FEEDER_SYSTEM_ITEM_IDS,
                FEEDER_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID,
                FEEDER_SYSTEM_ITEM_IDS.ISSUER,
                FEEDER_SYSTEM_ITEM_IDS.ASSIGNER,
                FEEDER_SYSTEM_ITEM_IDS.ID_VALUE,
                FEEDER_SYSTEM_ITEM_IDS.TYPE_NAME
        ).values(
                parentUUID,
                identifier.getIssuer(),
                identifier.getAssigner(),
                identifier.getId(),
                identifier.getType()
        ).execute();
    }



    UUID commitOriginatingSystemAudit(FeederAuditDetails feederAuditDetails) {
        UUID id = UUID.randomUUID();
        UUID locationUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, feederAuditDetails.getLocation());
        UUID providerUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, feederAuditDetails.getProvider());
        UUID subjectUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, (PartyIdentified) feederAuditDetails.getSubject());

        int result = context.insertInto(ORIGINATING_SYSTEM_AUDIT,
                ORIGINATING_SYSTEM_AUDIT.ID,
                ORIGINATING_SYSTEM_AUDIT.SYSTEM_ID,
                ORIGINATING_SYSTEM_AUDIT.LOCATION,
                ORIGINATING_SYSTEM_AUDIT.SUBJECT,
                ORIGINATING_SYSTEM_AUDIT.PROVIDER,
                ORIGINATING_SYSTEM_AUDIT.TIME,
                ORIGINATING_SYSTEM_AUDIT.TIME_TZ,
                ORIGINATING_SYSTEM_AUDIT.VERSION_ID
        ).values(
                id,
                feederAuditDetails.getSystemId(),
                locationUuid,
                subjectUuid,
                providerUuid,
                new Timestamp(feederAuditDetails.getTime().getDateTime().getMillis()),
                feederAuditDetails.getTime().getDateTime().getZone().getID(),
                feederAuditDetails.getVersionId()
        ).execute();

        return id;
    }

    UUID commitFeederSystemAudit(FeederAuditDetails feederAuditDetails) {
        UUID id = UUID.randomUUID();
        UUID locationUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, feederAuditDetails.getLocation());
        UUID providerUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, feederAuditDetails.getProvider());
        UUID subjectUuid = I_PartyIdentifiedAccess.getOrCreateParty(this, (PartyIdentified) feederAuditDetails.getSubject());

        int result = context.insertInto(FEEDER_SYSTEM_AUDIT,
                FEEDER_SYSTEM_AUDIT.ID,
                FEEDER_SYSTEM_AUDIT.SYSTEM_ID,
                FEEDER_SYSTEM_AUDIT.LOCATION,
                FEEDER_SYSTEM_AUDIT.SUBJECT,
                FEEDER_SYSTEM_AUDIT.PROVIDER,
                FEEDER_SYSTEM_AUDIT.TIME,
                FEEDER_SYSTEM_AUDIT.TIME_TZ,
                FEEDER_SYSTEM_AUDIT.VERSION_ID
        ).values(
                id,
                feederAuditDetails.getSystemId(),
                locationUuid,
                subjectUuid,
                providerUuid,
                new Timestamp(feederAuditDetails.getTime().getDateTime().getMillis()),
                feederAuditDetails.getTime().getDateTime().getZone().getID(),
                feederAuditDetails.getVersionId()
        ).execute();

        return id;
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws SQLException {

        int count = 0;

//        count += context.delete(ORIGINATING_SYSTEM_AUDIT).where(ORIGINATING_SYSTEM_AUDIT.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();
//        count += context.delete(FEEDER_AUDIT_DETAILS).where(FEEDER_AUDIT_DETAILS.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();
//
//        //delete IDs
//        count += context.delete(ORIGINATING_SYSTEM_ITEM_IDS).where(ORIGINATING_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();
//        count += context.delete(FEEDER_SYSTEM_ITEM_IDS).where(FEEDER_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();
//
//        //recreate a new feederAuditRecord
//        FeederAuditRecord newFeederAuditRecord = context.newRecord(FEEDER_AUDIT);
//        newFeederAuditRecord.setId(feederAuditRecord.getId());
//        newFeederAuditRecord.setOriginalContent(feederAuditRecord.getOriginalContent());
//        newFeederAuditRecord.setFeederSystemAudit(feederAuditRecord.getFeederSystemAudit());

        Boolean result = false;

        return result;
//        return eventContextRecord.update() > 0;
    }


    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
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
        //delete any feederAuditDetails
        count += context.delete(ORIGINATING_SYSTEM_AUDIT).where(ORIGINATING_SYSTEM_AUDIT.ID.eq(feederAuditRecord.getOriginatingSystemAudit())).execute();
        count += context.delete(FEEDER_SYSTEM_AUDIT).where(FEEDER_SYSTEM_AUDIT.ID.eq(feederAuditRecord.getFeederSystemAudit())).execute();

        //delete IDs
        count += context.delete(ORIGINATING_SYSTEM_ITEM_IDS).where(ORIGINATING_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();
        count += context.delete(FEEDER_SYSTEM_ITEM_IDS).where(FEEDER_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(feederAuditRecord.getId())).execute();

        count += feederAuditRecord.delete();
        return count;
    }

//    @Override
//    public I_ContextAccess retrieve(UUID id) {
//        return retrieveInstance(this, id);
//    }

    public static I_FeederAuditAccess retrieveInstance(I_DomainAccess domainAccess, UUID id) {
        FeederAuditAccess feederAuditAccess = new FeederAuditAccess(domainAccess);
        feederAuditAccess.feederAuditRecord = domainAccess.getContext().fetchOne(FEEDER_AUDIT, FEEDER_AUDIT.ID.eq(id));
        return feederAuditAccess;
    }

    public static I_FeederAuditAccess retrieveInstance(I_DomainAccess domainAccess, Result<?> records) {
        FeederAuditAccess feederAuditAccess = new FeederAuditAccess(domainAccess);
        FeederAuditRecord feederAuditRecord = domainAccess.getContext().newRecord(FEEDER_AUDIT);
        feederAuditRecord.setOriginalContent((byte[]) records.getValue(0, I_CompositionAccess.F_FEEDER_AUDIT_ORIGINAL_CONTENT));
        feederAuditRecord.setFeederSystemAudit((UUID) records.getValue(0, I_CompositionAccess.F_FEEDER_AUDIT_FEEDER_SYSTEM_AUDIT));
        feederAuditRecord.setOriginatingSystemAudit((UUID) records.getValue(0, I_CompositionAccess.F_FEEDER_AUDIT_ORIGINATING_SYSTEM_AUDIT));
        return feederAuditAccess;
    }

    private FeederAuditDetails retrieveOriginatingSystemAudit(UUID auditDetailsUUID) {

        if (auditDetailsUUID == null)
            return null;

        OriginatingSystemAuditRecord originatingSystemAuditRecord = context.fetchOne(ORIGINATING_SYSTEM_AUDIT, ORIGINATING_SYSTEM_AUDIT.ID.eq(auditDetailsUUID));

        return new FeederAuditDetails(originatingSystemAuditRecord.getSystemId(),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, originatingSystemAuditRecord.getLocation()),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, originatingSystemAuditRecord.getSubject()),
                new DBDvDateTime(originatingSystemAuditRecord.getTime(), "UTC").decode(),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, originatingSystemAuditRecord.getProvider()),
                originatingSystemAuditRecord.getVersionId());
    }

    private FeederAuditDetails retrieveFeederSystemAuditRecord(UUID auditDetailsUUID) {

        if (auditDetailsUUID == null)
            return null;

        FeederSystemAuditRecord feederSystemAuditRecord = context.fetchOne(FEEDER_SYSTEM_AUDIT, FEEDER_SYSTEM_AUDIT.ID.eq(auditDetailsUUID));

        return new FeederAuditDetails(feederSystemAuditRecord.getSystemId(),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, feederSystemAuditRecord.getLocation()),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, feederSystemAuditRecord.getSubject()),
                new DBDvDateTime(feederSystemAuditRecord.getTime(), "UTC").decode(),
                I_PartyIdentifiedAccess.retrievePartyIdentified(this, feederSystemAuditRecord.getProvider()),
                feederSystemAuditRecord.getVersionId());
    }

    List<DvIdentifier> retrieveOriginatingSystemItemIds(UUID auditDetailsUUID) {

        if (auditDetailsUUID == null)
            return null;

        List<DvIdentifier> identifiers = new ArrayList<>();

        context.fetch(ORIGINATING_SYSTEM_ITEM_IDS, ORIGINATING_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(auditDetailsUUID)).forEach(record -> {
            DvIdentifier dvIdentifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
            identifiers.add(dvIdentifier);
        });

        return identifiers;
    }

    List<DvIdentifier> retrieveFeederSystemItemIds(UUID auditDetailsUUID) {

        if (auditDetailsUUID == null)
            return null;

        List<DvIdentifier> identifiers = new ArrayList<>();

        context.fetch(FEEDER_SYSTEM_ITEM_IDS, FEEDER_SYSTEM_ITEM_IDS.FEEDER_AUDIT_ID.eq(auditDetailsUUID)).forEach(record -> {
            DvIdentifier dvIdentifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
            identifiers.add(dvIdentifier);
        });

        return identifiers;
    }


    @Override
    public FeederAudit mapRmFeederAudit() {

        return new FeederAudit(retrieveOriginatingSystemAudit(feederAuditRecord.getOriginatingSystemAudit()),
                retrieveOriginatingSystemItemIds(feederAuditRecord.getId()),
                retrieveFeederSystemAuditRecord(feederAuditRecord.getFeederSystemAudit()),
                retrieveFeederSystemItemIds(feederAuditRecord.getId()),
                new DvParsable(new String(feederAuditRecord.getOriginalContent()), "BLOB"));
    }


    @Override
    public void setRecordFields(UUID id, FeederAudit feederAudit) throws Exception {
        this.feederAudit = feederAudit; //will be used to perform a simple store (e.g. no smart update)
        feederAuditRecord.setId(id);
    }


    @Override
    public void setCompositionId(UUID compositionId) {
        feederAuditRecord.setCompositionId(compositionId);
    }

    @Override
    public UUID getId() {
        return feederAuditRecord.getId();
    }

    @Override
    public DataAccess getDataAccess() {
        return this;
    }
}
