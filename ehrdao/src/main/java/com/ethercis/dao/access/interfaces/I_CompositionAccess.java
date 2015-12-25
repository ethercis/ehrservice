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

import com.ethercis.dao.access.jooq.CompositionAccess;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.dao.jooq.tables.records.CompositionRecord;
import com.ethercis.dao.jooq.tables.records.TerritoryRecord;
import com.ethercis.ehr.util.EhrException;
import org.joda.time.DateTime;
import org.jooq.Result;
import org.openehr.rm.composition.Composition;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.dao.jooq.Tables.LANGUAGE;
import static com.ethercis.dao.jooq.Tables.TERRITORY;

/**
 * Composition Access Layer Interface<br>
 * Interface CRUD and specific methods
 */
public interface I_CompositionAccess extends I_SimpleCRUD<I_CompositionAccess, UUID> {

    /**
     * Get a new Composition Access Instance
     * @param domain SQL context, knowledge
     * @param languageCode language used to retrieve language specific objects (defaulted to 'en')
     * @param territoryCode 2-letters territory code
     * @param dateCreated (DateTime) creation time, default Now()
     * @param eventContextId UUID of event context
     * @return I_CompositionAccess
     * @throws EhrException if creation failed
     */
    static I_CompositionAccess getNewInstance(I_DomainAccess domain, String languageCode, String territoryCode, DateTime dateCreated, UUID eventContextId, UUID composerId, UUID ehrId) throws EhrException {
        return new CompositionAccess(domain.getContext(), domain.getKnowledgeManager(), languageCode, territoryCode, dateCreated, eventContextId, composerId, ehrId);
    }

    /**
     * Get a new Composition Access Instance
     * @param domain SQL context, knowledge
     * @param composition a valid RM composition
     * @param dateCreated (DateTime) creation time, default Now()
     * @param ehrId the EHR holding this instance
     * @return I_CompositionAccess
     * @throws EhrException if creation failed
     */
    static I_CompositionAccess getNewInstance(I_DomainAccess domain, Composition composition, DateTime dateCreated, UUID ehrId) throws Exception {
        return new CompositionAccess(domain.getContext(), domain.getKnowledgeManager(), composition, dateCreated, ehrId);
    }

    UUID commit(UUID committerId, UUID systemId, ContributionDef.ContributionState state, String description) throws Exception;

    UUID commit(UUID committerId, UUID systemId, String description) throws Exception;

    Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception;

    Boolean update(Timestamp timestamp, UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception;

    Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description, Boolean force) throws Exception;

    Integer delete(UUID committerId, UUID systemId, String description) throws Exception;

    /**
     * Retrieve composition(s) for an identified version
     * @param id the version uuid
     * @return &gt; 0 if success
     */
    static I_CompositionAccess retrieveCompositionVersion(I_DomainAccess domainAccess, UUID id, int version) throws Exception {
        return CompositionAccess.retrieveCompositionVersion(domainAccess, id, version);
    }

    Timestamp getSysTransaction();

    /**
     * Retrieve a composition access instance from the persistence layer
     * @param domainAccess SQL context, knowledge
     * @param id a composition uuid
     * @return a valid I_CompositionAccess
     * @throws Exception if retrieval failed
     */
    static I_CompositionAccess retrieveInstance(I_DomainAccess domainAccess, UUID id) throws Exception {
        return CompositionAccess.retrieveInstance(domainAccess, id);
    }

    /**
     * Create a new composition access instance from a composition record
     * @param domainAccess SQL context, knowledge
     * @param compositionRecord a valid CompositionRecord
     * @return the interface
     * @see com.ethercis.dao.jooq.tables.records.CompositionRecord
     */
    static I_CompositionAccess getInstance(I_DomainAccess domainAccess, CompositionRecord compositionRecord) {
        return new CompositionAccess(domainAccess, compositionRecord);
    }

    /**
     * Retrieve a map of composition accesses for all compositions referrencing a contribution version
     * @param domainAccess SQL context, knowledge
     * @param contributionVersionId contribution version uuid
     * @return a map&lt;uuid, I_CompositionAccess&gt;
     */
    static Map<UUID, I_CompositionAccess> retrieveInstancesInContributionVersion(I_DomainAccess domainAccess, UUID contributionVersionId, Integer versionNumber){
        return CompositionAccess.retrieveCompositionsInContributionVersion(domainAccess, contributionVersionId, versionNumber);
    }

    /**
     * check if a composition has a previous version in history
     * @param domainAccess
     * @param compositionId
     * @return
     */
    static boolean hasPreviousVersion(I_DomainAccess domainAccess, UUID compositionId){
        return CompositionAccess.hasPreviousVersion(domainAccess, compositionId);
    }

//	public Composition getLatestComposition(int patientId, String archetypeId);
//
//	public List<Composition> getCompositions(String systemId, int patientId);
//
//	public List<Composition> getCompositions(String systemId, int patientId, String archetypeId);
//
//	public List<Composition> findCompositions(String systemId, int patientId, String name, Date dateFrom, Date dateTo);

//    void createComposition(UUID ehrId, UUID composerId, UUID contextId, UUID contributionId, String languageCode, String territoryCode, DateTime created) throws EhrException;

    /**
     * get the composition Id
     * @return
     */
    UUID getId();

    /**
     * get the EHR id to which this composition belongs to
     * @return UUID
     */
    UUID getEhrid();

    /**
     * get the composer Id
     * @return UUID
     */
    UUID getComposerId();

    /**
     * get the event context id
     * @return UUID
     */
    UUID getContextId();

    /**
     * get the contribution version id
     * @return UUID
     */
    UUID getContributionVersionId();

    /**
     * get the language code for this composition (eg. 'en', 'fr' etc.)
     * @return
     */
    String getLanguageCode();

    /**
     * get the 2-letters country code
     * @return
     */
    Integer getTerritoryCode();

    /**
     * get the list of entry Ids for this composition
     * @return a list of entry UUIDs
     *
     */
    List<UUID> getContentIds() throws EhrException;

    /**
     * set the EHR id
     * @param ehrId UUID
     * @throws EhrException
     */
    void setEhrid(UUID ehrId) throws EhrException;

    /**
     * set the composer id
     * @param composerId UUID
     * @throws EhrException
     */
    void setComposerId(UUID composerId) throws EhrException;

    /**
     * set the event context id
     * @param contextId UUID
     * @throws EhrException
     */
    void setContextCompositionId(UUID contextId) throws EhrException, Exception;

    /**
     * set the language code
     * @param code String
     * @throws EhrException
     */
    void setLanguageCode(String code) throws EhrException;

    /**
     * set the 2-letters territory code
     * @param code String
     * @throws EhrException
     */
    void setTerritoryCode(Integer code) throws EhrException;

    /**
     * add an entry to the composition
     * @param entry I_EntryAccess instance
     * @return &gt;0 success
     * @throws EhrException
     */
    int addContent(I_EntryAccess entry) throws EhrException;

    /**
     * get the list of entries for this composition
     * @return the list of entry as I_EntryAccess
     * @throws EhrException
     * @see com.ethercis.dao.access.interfaces.I_EntryAccess
     */
    List<I_EntryAccess> getContent() throws EhrException;

    void setContent(List<I_EntryAccess> content);

    /**
     * delete content belonging to this composition
     * @return number of entries deleted
     * @throws EhrException
     */
    int deleteContent() throws EhrException;

    /**
     * set the contribution version id for this composition
     * @param contributionVersionId
     */
    void setContributionId(UUID contributionVersionId);

    void setCompositionRecord(CompositionRecord record);

    void setComposition(Composition composition);


    static Integer fetchTerritoryCode(I_DomainAccess domainAccess, String territoryAsString){
        Result<TerritoryRecord> result = domainAccess.getContext().selectFrom(TERRITORY).where(TERRITORY.TWOLETTER.equal(territoryAsString)).fetch();
        if (result.isEmpty())
            return -1;
        return result.get(0).getCode();
    }

    static boolean isValidLanguageCode(I_DomainAccess domainAccess, String languageCode){
        if (domainAccess.getContext().selectFrom(LANGUAGE).where(LANGUAGE.CODE.equal(languageCode)).fetch().isEmpty())
            return false;
        return true;
    }

    I_ContributionAccess getContributionAccess();

    void setContributionAccess(I_ContributionAccess contributionAccess);

//    void setCommitted(boolean b);
}
