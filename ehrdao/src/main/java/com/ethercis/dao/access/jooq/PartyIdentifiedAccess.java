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
import com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.jooq.pg.tables.records.IdentifierRecord;
import com.ethercis.jooq.pg.tables.records.PartyIdentifiedRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.PartyRef;

import java.sql.Timestamp;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.IDENTIFIER;
import static com.ethercis.jooq.pg.Tables.PARTY_IDENTIFIED;

/**
 * Created by Christian Chevalley on 4/10/2015.
 */
public class PartyIdentifiedAccess extends DataAccess implements I_PartyIdentifiedAccess
{

    private static Logger log = LogManager.getLogger(PartyIdentifiedAccess.class);
    private PartyIdentifiedRecord partyIdentifiedRecord;
    private Map<String, IdentifierRecord> identifiers;

//    private String partyName;

    public PartyIdentifiedAccess(DSLContext context, String partyName) {
        super(context, null);
        partyIdentifiedRecord = context.newRecord(PARTY_IDENTIFIED);
        partyIdentifiedRecord.setName(partyName);
    }

    public PartyIdentifiedAccess(DSLContext context){
        super(context, null);
    }

    public PartyIdentifiedAccess(I_DomainAccess domainAccess){
        super(domainAccess);
    }


    @Override
    public UUID commit(Timestamp transactionTime){
        partyIdentifiedRecord.store();

        if (identifiers != null) {
            for (IdentifierRecord identifierRecord : identifiers.values()) {
                identifierRecord.setParty(partyIdentifiedRecord.getId());
                context.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                        .values(identifierRecord.getParty(), identifierRecord.getIdValue(), identifierRecord.getIssuer(), identifierRecord.getAssigner(), identifierRecord.getTypeName())
                        .execute();
                log.debug("Create identifier for party:"+identifierRecord.getParty());
            }
        }

        log.debug("created party:"+partyIdentifiedRecord.getId());

        return partyIdentifiedRecord.getId();
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    public static I_PartyIdentifiedAccess retrieveInstance(I_DomainAccess domainAccess, UUID id){
        DSLContext context = domainAccess.getContext();
        PartyIdentifiedRecord record = context.fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(id));

        if (record == null)
            return null;

        PartyIdentifiedAccess partyIdentifiedAccess = new PartyIdentifiedAccess(context);
        partyIdentifiedAccess.partyIdentifiedRecord = record;

        Result<IdentifierRecord> identifierRecords = context.fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(partyIdentifiedAccess.partyIdentifiedRecord.getId()));

        for (IdentifierRecord identifierRecord: identifierRecords){
            if (partyIdentifiedAccess.identifiers == null){
                partyIdentifiedAccess.identifiers = new HashMap<>();
            }

            partyIdentifiedAccess.identifiers.put(makeMapKey(identifierRecord.getIdValue(), identifierRecord.getIssuer()), identifierRecord);
        }

        return partyIdentifiedAccess;
    }

    @Override
    public Boolean update(Timestamp transactionTime){

        int count = 0;

        if (partyIdentifiedRecord.changed()) {
            count += partyIdentifiedRecord.update();
        }

        for (IdentifierRecord identifierRecord: identifiers.values()){
            if (context.fetchExists(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(identifierRecord.getIdValue()).and(IDENTIFIER.ISSUER.eq(identifierRecord.getIssuer())))){
                //updateComposition this record
                count += context.update(IDENTIFIER)
                        .set(IDENTIFIER.ID_VALUE, identifierRecord.getIdValue())
                        .set(IDENTIFIER.ASSIGNER, identifierRecord.getAssigner())
                        .set(IDENTIFIER.ISSUER, identifierRecord.getIssuer())
                        .where(IDENTIFIER.ID_VALUE.eq(identifierRecord.getIdValue()).and(IDENTIFIER.ISSUER.eq(identifierRecord.getIssuer())))
                        .execute();
            }
            else //add it
                count += context.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                        .values(partyIdentifiedRecord.getId(), identifierRecord.getIdValue(), identifierRecord.getIssuer(), identifierRecord.getAssigner(), identifierRecord.getTypeName())
                        .execute();
        }


        return count > 0;
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        return update(transactionTime);
    }

    @Override
    public Boolean update() throws Exception {
        return null;
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        return null;
    }

    @Override
    public Integer delete(){
        int count = 0;
        //delete corresponding identifiers
        if (identifiers != null) {
            for (IdentifierRecord identifierRecord : identifiers.values()) {
                count += context.delete(IDENTIFIER).where(IDENTIFIER.PARTY.eq(partyIdentifiedRecord.getId())).execute();
            }
        }
        count += partyIdentifiedRecord.delete();
        return count;
    }

//    @Override
//    public I_PartyIdentifiedAccess retrieve(UUID id) throws Exception {
//        return retrieveInstance(this, id);
//    }

    public UUID retrieve(String partyName){
        return retrievePartyIdByPartyName(this, partyName);
    }

    public static UUID retrievePartyIdByPartyName(I_DomainAccess domainAccess, String partyName){
        if (domainAccess.getContext().fetchExists(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(partyName))) {
            UUID uuid = domainAccess.getContext().fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(partyName)).getId();
            return uuid;
        }


        return null;

    }


    private static String makeMapKey(String s1, String s2){
        return s1+":"+s2;
    }

    @Override
    public Integer addIdentifier(String value, String issuer, String assigner, String type){

        IdentifierRecord identifierRecord = context.newRecord(IDENTIFIER);
        identifierRecord.setIdValue(value);
        identifierRecord.setIssuer(issuer);
        identifierRecord.setAssigner(assigner);
        identifierRecord.setTypeName(type);

        if (identifiers == null){
            identifiers = new HashMap<>();
        }
        identifiers.put(makeMapKey(value, issuer), identifierRecord);

        return identifiers.size();
    }

    @Override
    public Integer deleteIdentifier(String idCode, String issuer){

        String key = makeMapKey(idCode, issuer);
        identifiers.remove(key);

        return context.delete(IDENTIFIER).where(IDENTIFIER.PARTY.eq(partyIdentifiedRecord.getId())
                .and(IDENTIFIER.ID_VALUE.eq(idCode))
                .and(IDENTIFIER.ISSUER.eq(issuer))).execute();
    }

    @Override
    public String getPartyName() {
        return partyIdentifiedRecord.getName();
    }

    @Override
    public void setPartyName(String name){
        partyIdentifiedRecord.setName(name);
    }

    @Override
    public String[] getIdentifiersKeySet(){
        return identifiers.keySet().toArray(new String[identifiers.size()]);
    }

    @Override
    public UUID getId(){
        return partyIdentifiedRecord.getId();
    }

    public static UUID getOrCreateParty(I_DomainAccess domainAccess, String name, String idCode, String issuer, String assigner, String typeName){
        DSLContext context1 = domainAccess.getContext();
        //check if it exists first with idCode and issuer
        if (context1.fetchExists(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(idCode).and(IDENTIFIER.ISSUER.eq(issuer))))
            return context1.fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(idCode).and(IDENTIFIER.ISSUER.eq(issuer))).getParty();

        //check if a party exists with the same name, if found, the identifier is just added to the list
        if (context1.fetchExists(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(name))){
            UUID partyIdentifiedUuid = context1.fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(name)).getId();
            //add identifier to the list
            if (idCode != null && issuer != null)
                context1.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                        .values(partyIdentifiedUuid, idCode, issuer, assigner, typeName)
                        .execute();
            return partyIdentifiedUuid;
        }
        else {
            //storeComposition a new party identified
            UUID partyIdentifiedUuid = context1
                    .insertInto(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME)
                    .values(name)
                    .returning(PARTY_IDENTIFIED.ID)
                    .fetchOne().getId();
            //and storeComposition the identifier
            if (idCode != null && issuer != null)
                context1.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                        .values(partyIdentifiedUuid, idCode, issuer, assigner, typeName)
                        .execute();
            return partyIdentifiedUuid;
        }
    }

    public static UUID findIdentifiedParty(DSLContext context, List<DvIdentifier> identifierList){

        if (identifierList == null)
            return null;

        for (DvIdentifier identifier: identifierList){
            if (context.fetchExists(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(identifier.getId()).and(IDENTIFIER.ISSUER.eq(identifier.getIssuer()))))
                return context.fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(identifier.getId()).and(IDENTIFIER.ISSUER.eq(identifier.getIssuer()))).getParty();
        }

        return null;
    }

    public static UUID findReferencedParty(DSLContext context, PartyRef partyRef){

        if (partyRef == null)
            return null;

        Object ref = partyRef.getId();

        if (ref instanceof GenericID) {

            GenericID genericID = (GenericID)ref;

            if (context.fetchExists(PARTY_IDENTIFIED,
                    PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.eq(partyRef.getNamespace())
//                            .and(PARTY_IDENTIFIED.PARTY_REF_SCHEME.eq(genericID.getScheme()))
                            .and(PARTY_IDENTIFIED.PARTY_REF_VALUE.eq(genericID.getValue())))) {

                return context.fetchOne(PARTY_IDENTIFIED,
                        PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.eq(partyRef.getNamespace())
//                                .and(PARTY_IDENTIFIED.PARTY_REF_SCHEME.eq(genericID.getScheme()))
                                .and(PARTY_IDENTIFIED.PARTY_REF_VALUE.eq(genericID.getValue()))).getId();
            }

//                return context.fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(identifier.getId()).and(IDENTIFIER.ISSUER.eq(identifier.getIssuer()))).getParty();
        }

        return null;
    }

    private static UUID findNamedParty(DSLContext context, String name){

        if (name == null)
            return null;

        if (context.fetchExists(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(name)))
            return context.fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME.eq(name)).getId();

        return null;
    }


    public static UUID getOrCreateParty(I_DomainAccess domainAccess, String name, List<DvIdentifier> identifierList){
        DSLContext context1 = domainAccess.getContext();
        //check if it exists first with idCode and issuer
        UUID identifiedParty = findIdentifiedParty(domainAccess.getContext(), identifierList);
        if (identifiedParty != null)
            return identifiedParty;

        //check if a party exists with the same name, if found, the identifier is just added to the list
        identifiedParty = findNamedParty(domainAccess.getContext(), name);
        if (identifiedParty != null)
            return identifiedParty;

        //storeComposition a new party identified
        UUID partyIdentifiedUuid = context1
                .insertInto(PARTY_IDENTIFIED, PARTY_IDENTIFIED.NAME)
                .values(name)
                .returning(PARTY_IDENTIFIED.ID)
                .fetchOne().getId();
        //and storeComposition the identifier
        if (identifierList != null) {
            for (DvIdentifier identifier : identifierList) {
                if (identifier.getId() != null && identifier.getIssuer() != null)
                    context1.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                            .values(partyIdentifiedUuid, identifier.getId(), identifier.getIssuer(), identifier.getAssigner(), identifier.getType())
                            .execute();
            }
        }
        return partyIdentifiedUuid;
    }

    public static UUID getOrCreateParty(I_DomainAccess domainAccess, PartyIdentified partyIdentified){
        DSLContext context1 = domainAccess.getContext();
        //check if it exists first with idCode and issuer
        //check with external ref if any

        UUID identifiedParty = null;

        PartyRef externalRef = partyIdentified.getExternalRef();
        List<DvIdentifier> identifierList = partyIdentified.getIdentifiers();
        GenericID genericID = null;

        if (externalRef != null){
            Object ref = externalRef.getId();

            if (ref instanceof GenericID){
                genericID = (GenericID)ref;
            }
            else if (ref instanceof HierObjectID)
                genericID = null;
            else
                log.warn("Passed partyIdentified does not contain a GenericID in external ref:"+partyIdentified.toString());

        }

        if (externalRef != null){
            identifiedParty = findReferencedParty(domainAccess.getContext(), externalRef);
        }
        else {

            if (identifierList != null && identifierList.size() > 0)
                identifiedParty = findIdentifiedParty(domainAccess.getContext(), identifierList);
        }
        if (identifiedParty != null)
            return identifiedParty;

        //store a new party identified
        UUID partyIdentifiedUuid = context1
                .insertInto(PARTY_IDENTIFIED,
                        PARTY_IDENTIFIED.NAME,
                        PARTY_IDENTIFIED.PARTY_REF_NAMESPACE,
                        PARTY_IDENTIFIED.PARTY_REF_VALUE,
                        PARTY_IDENTIFIED.PARTY_REF_SCHEME,
                        PARTY_IDENTIFIED.PARTY_REF_TYPE)
                .values(partyIdentified.getName(),
                        externalRef != null ? externalRef.getNamespace() : null ,
                        genericID != null ? genericID.getValue() : null,
                        genericID != null ? genericID.getScheme() : null,
                        externalRef != null ? externalRef.getType() : null)
                .returning(PARTY_IDENTIFIED.ID)
                .fetchOne().getId();
        //and store the identifier if any
        if (identifierList != null) {
            for (DvIdentifier identifier : identifierList) {
                if (identifier.getId() != null && identifier.getIssuer() != null)
                    context1.insertInto(IDENTIFIER, IDENTIFIER.PARTY, IDENTIFIER.ID_VALUE, IDENTIFIER.ISSUER, IDENTIFIER.ASSIGNER, IDENTIFIER.TYPE_NAME)
                            .values(partyIdentifiedUuid, identifier.getId(), identifier.getIssuer(), identifier.getAssigner(), identifier.getType())
                            .execute();
            }
        }
        return partyIdentifiedUuid;
    }

    public static org.openehr.rm.common.generic.PartyIdentified retrievePartyIdentified(I_DomainAccess domainAccess, UUID id){
        PartyRef partyRef = null;
        if (!(domainAccess.getContext().fetchExists(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(id))))
            return null;

        //rebuild an identified party
        List<DvIdentifier> identifierList = new ArrayList<>();

        domainAccess.getContext().fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(id)).forEach(record -> {
            DvIdentifier identifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
            identifierList.add(identifier);
        });

        PartyIdentifiedRecord identifiedRecord = domainAccess.getContext().fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(id));

        if (identifiedRecord.getPartyRefType() != null){
            if (identifiedRecord.getPartyRefValue() != null && identifiedRecord.getPartyRefScheme() != null) {
                GenericID genericID = new GenericID(identifiedRecord.getPartyRefValue(), identifiedRecord.getPartyRefScheme());
                partyRef = new PartyRef(genericID, identifiedRecord.getPartyRefNamespace(), identifiedRecord.getPartyRefType());
            }
            else
            {
                ObjectID objectID = new HierObjectID("ref");
                partyRef = new PartyRef(objectID, identifiedRecord.getPartyRefNamespace(), identifiedRecord.getPartyRefType());
            }
        }

        PartyIdentified partyIdentified = new PartyIdentified(partyRef,
                                                identifiedRecord.getName(),
                                                identifierList.isEmpty() ? null : identifierList);

        return partyIdentified;
    }

    public static org.openehr.rm.common.generic.PartyIdentified retrievePartyIdentified(String name, String ref_scheme, String ref_namespace, String ref_value, String ref_type){
        PartyRef partyRef = null;

        //rebuild an identified party
        List<DvIdentifier> identifierList = new ArrayList<>();

//        domainAccess.getContext().fetch(IDENTIFIER, IDENTIFIER.PARTY.eq(id)).forEach(record -> {
//            DvIdentifier identifier = new DvIdentifier(record.getIssuer(), record.getAssigner(), record.getIdValue(), record.getTypeName());
//            identifierList.add(identifier);
//        });

//        PartyIdentifiedRecord identifiedRecord = domainAccess.getContext().fetchOne(PARTY_IDENTIFIED, PARTY_IDENTIFIED.ID.eq(id));

        if (ref_type != null){
            if (ref_value != null && ref_scheme != null) {
                GenericID genericID = new GenericID(ref_value, ref_scheme);
                partyRef = new PartyRef(genericID, ref_namespace, ref_type);
            }
            else
            {
                ObjectID objectID = new HierObjectID("ref");
                partyRef = new PartyRef(objectID, ref_namespace, ref_type);
            }
        }

        if (name == null && partyRef == null)
            return null;

        PartyIdentified partyIdentified = new PartyIdentified(partyRef,
                name,
                identifierList.isEmpty() ? null : identifierList);

        return partyIdentified;
    }


}
