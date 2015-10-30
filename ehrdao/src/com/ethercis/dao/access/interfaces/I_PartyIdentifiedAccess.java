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
package com.ethercis.dao.access.interfaces;

import com.ethercis.dao.access.jooq.PartyIdentifiedAccess;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ethercis.dao.jooq.Tables.IDENTIFIER;
import static com.ethercis.dao.jooq.Tables.PARTY_IDENTIFIED;

/**
 * Party Identified access layer
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_PartyIdentifiedAccess extends I_SimpleCRUD<I_PartyIdentifiedAccess, UUID> {

    /**
     * get a new access layer instance for a party name
     * @param domain SQL access
     * @param partyName a subject name or a dummy string...
     * @return
     */
     public static I_PartyIdentifiedAccess getInstance(I_DomainAccess domain, String partyName) {
        return new PartyIdentifiedAccess(domain.getContext(), partyName);
    }



    /**
     * retrieve an instance by its name<br>
     * should be used for test purpose as the subject should not be known in this context
     * @param domainAccess SQL access
     * @param partyName a subject name
     * @return
     */
    public static UUID retrievePartyIdByPartyName(I_DomainAccess domainAccess, String partyName){
        return PartyIdentifiedAccess.retrievePartyIdByPartyName(domainAccess, partyName);
    }

    /**
     * retrieve a party identified from its UUID
     * @param domainAccess SQL access
     * @param id UUID
     * @return an access layer instance
     */
    public static I_PartyIdentifiedAccess retrieveInstance(I_DomainAccess domainAccess, UUID id){
        return PartyIdentifiedAccess.retrieveInstance(domainAccess, id);
    }

    /**
     * delete an instance and all its identifiers
     * @param domainAccess SQL access
     * @param id UUID
     * @return number of records deleted
     */
    public static int deleteInstance(I_DomainAccess domainAccess, UUID id){
        domainAccess.getContext().delete(IDENTIFIER).where(IDENTIFIER.PARTY.eq(id)).execute();
        return domainAccess.getContext().delete(PARTY_IDENTIFIED).where(PARTY_IDENTIFIED.ID.eq(id)).execute();
    }

    /**
     * get the list of identifiers for a party
     * @param domainAccess
     * @param partyId
     * @return
     */
    public static List<DvIdentifier> getPartyIdentifiers(I_DomainAccess domainAccess, UUID partyId){
        List<DvIdentifier> resList = new ArrayList<>();
        domainAccess.getContext().selectFrom(IDENTIFIER).where(IDENTIFIER.PARTY.eq(partyId)).fetch().forEach(records -> {
            DvIdentifier identifier = new DvIdentifier(records.getIssuer(), records.getTypeName(), records.getIdValue(), records.getTypeName());
            resList.add(identifier);
        });

        return resList;
    }

    /**
     * retrieve an identified party from its identification code and issuer
     * @param domainAccess SQL access
     * @param idCode issued identification code
     * @param issuer authority issuing the code
     * @return UUID of identified party or null
     */
    public static UUID retrievePartyByIdentifier(I_DomainAccess domainAccess, String idCode, String issuer){
        UUID uuid = domainAccess.getContext().fetchOne(IDENTIFIER, IDENTIFIER.ID_VALUE.eq(idCode).and(IDENTIFIER.ISSUER.eq(issuer))).getParty();

        if (uuid == null){
            return null;
        }

        return uuid;

    }

    /**
     * add an identifier to a party
     * @param idCode a subject id code
     * @param issuer the authority issuing the subject Id Code (ex. NHS)
     * @param assigner the authority that assign the id to the identified item
     * @param typeName a descriptive literal following a conventional vocabulary (SSN, prescription  etc.)
     * @return number of record added
     */
    Integer addIdentifier(String idCode, String issuer, String assigner, String typeName);

    /**
     * delete a specific identifier for the current party
     * @param idCode the subject code
     * @param issuer the issuer id
     * @return number of record deleted
     */
    Integer deleteIdentifier(String idCode, String issuer);

    /**
     * get the party name
     * @return
     */
    String getPartyName();

    /**
     * set the party name
     * @param name
     */
    void setPartyName(String name);

    /**
     * get the list of identifier keys<br>
     * a key is formatted as 'code:issuer'
     * @return
     */
    String[] getIdentifiersKeySet();

    UUID getId();


    public static UUID getOrCreateParty(I_DomainAccess domainAccess, String name, String idCode, String issuer, String assigner, String typeName){
        return PartyIdentifiedAccess.getOrCreateParty(domainAccess, name, idCode, issuer, assigner, typeName);
    }

    public static UUID getOrCreateParty(I_DomainAccess domainAccess, String name, List<DvIdentifier> identifierList){
        return PartyIdentifiedAccess.getOrCreateParty(domainAccess, name, identifierList);
    }

    public static UUID getOrCreateParty(I_DomainAccess domainAccess, PartyIdentified partyIdentified){
        return PartyIdentifiedAccess.getOrCreateParty(domainAccess, partyIdentified);
    }

    public static org.openehr.rm.common.generic.PartyIdentified retrievePartyIdentified(I_DomainAccess domainAccess, UUID id){
        return PartyIdentifiedAccess.retrievePartyIdentified(domainAccess, id);
    }

    public static UUID findIdentifiedParty(I_DomainAccess domainAccess, List<DvIdentifier> identifierList){
        return PartyIdentifiedAccess.findIdentifiedParty(domainAccess.getContext(), identifierList);
    }
}
