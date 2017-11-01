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
import com.ethercis.jooq.pg.tables.EventContext;
import com.ethercis.jooq.pg.tables.Identifier;
import com.ethercis.jooq.pg.tables.Participation;
import com.ethercis.jooq.pg.tables.records.*;
import com.ethercis.ehr.util.EhrException;
import org.joda.time.DateTime;
import org.jooq.Result;
import org.jooq.Table;
import org.openehr.rm.composition.Composition;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;
import static com.ethercis.jooq.pg.Tables.EHR;
import static com.ethercis.jooq.pg.Tables.PARTY_IDENTIFIED;

/**
 * Composition Access Layer Interface<br>
 * Interface CRUD and specific methods
 */
public interface I_CompositionAccess extends I_SimpleCRUD<I_CompositionAccess, UUID> {

    //definitions of aliases used in joins
    String COMPOSITION_JOIN = "composition_join";
    String COMPOSER_JOIN = "composer_ref";
    String COMPOSER_ID = "composer_id";
    String FACILITY_JOIN = "facility_ref";
    String FACILITY_ID = "facility_id";
    String EVENT_CONTEXT_JOIN = "event_context_ref";
    String PARTICIPATION_JOIN = "participation_ref";
    String PERFORMER_JOIN = "performer_ref";
    String TERRITORY_JOIN = "territory_ref";
    String CONCEPT_JOIN = "concept_ref";

    String F_VERSION = "version";
    String F_COMPOSITION_ID = "composition_id";
    String F_ENTRY = "jsonb_entry";
    String F_ENTRY_TEMPLATE = "template_id";
    String F_LANGUAGE = "language";
    String F_TERRITORY = "territory";
    String F_TERRITORY_CODE = "territory_code";
    String F_COMPOSER_NAME = "composer_name";
    String F_COMPOSER_REF_VALUE = "composer_ref_value";
    String F_COMPOSER_REF_SCHEME = "composer_ref_scheme";
    String F_COMPOSER_REF_NAMESPACE = "composer_ref_namespace";
    String F_COMPOSER_REF_TYPE = "composer_ref_type";
    String F_COMPOSER_ID_VALUE = "composer_id_value";
    String F_COMPOSER_ID_ISSUER = "composer_id_issuer";
    String F_COMPOSER_ID_TYPE_NAME = "composer_id_type_name";
    String F_CONTEXT_START_TIME = "context_start_time";
    String F_CONTEXT_START_TIME_TZID = "context_start_time_tzid";
    String F_CONTEXT_END_TIME = "context_end_time";
    String F_CONTEXT_END_TIME_TZID = "context_end_time_tzid";
    String F_CONTEXT_LOCATION = "context_location";
    String F_CONTEXT_OTHER_CONTEXT = "context_other_context";
    String F_FACILITY_NAME = "facility_name";
    String F_FACILITY_REF_VALUE = "facility_ref_value";
    String F_FACILITY_REF_SCHEME = "facility_ref_scheme";
    String F_FACILITY_REF_NAMESPACE = "facility_ref_namespace";
    String F_FACILITY_REF_TYPE = "facility_ref_type";
    String F_FACILITY_ID_VALUE = "facility_id_value";
    String F_FACILITY_ID_ISSUER = "facility_id_issuer";
    String F_FACILITY_ID_TYPE_NAME = "facility_id_type_name";
    String F_PARTICIPATION_FUNCTION = "participation_function";
    String F_PARTICIPATION_MODE = "participation_mode";
    String F_PARTICIPATION_START_TIME = "participation_start_time";
    String F_PARTICIPATION_START_TIME_TZID = "participation_start_time_tzid";
    String F_PERFORMER_NAME = "performer_name";
    String F_PERFORMER_REF_VALUE = "performer_ref_value";
    String F_PERFORMER_REF_SCHEME = "performer_ref_scheme";
    String F_PERFORMER_REF_NAMESPACE = "performer_ref_namespace";
    String F_PERFORMER_REF_TYPE = "performer_ref_type";
    String F_PERFORMER_ID_VALUE = "performer_id_value";
    String F_PERFORMER_ID_ISSUER = "performer_id_issuer";
    String F_PERFORMER_ID_TYPE_NAME = "performer_id_type_name";
    String F_CONCEPT_ID = "concept_id";
    String F_CONCEPT_DESCRIPTION = "concept_description";
    

    Table<CompositionRecord> compositionRef = COMPOSITION.as(COMPOSITION_JOIN);
    Table<PartyIdentifiedRecord> composerRef = PARTY_IDENTIFIED.as(COMPOSER_JOIN);
    Table<IdentifierRecord> composerId = IDENTIFIER.as(COMPOSER_ID);
    Table<PartyIdentifiedRecord> facilityRef = PARTY_IDENTIFIED.as(FACILITY_JOIN);
    Table<IdentifierRecord> facilityId = IDENTIFIER.as(FACILITY_ID);
    Table<EventContextRecord> eventContextRef = EVENT_CONTEXT.as(EVENT_CONTEXT_JOIN);
    Table<ParticipationRecord> participationRef = PARTICIPATION.as(PARTICIPATION_JOIN);
    Table<PartyIdentifiedRecord> performerRef = PARTY_IDENTIFIED.as(PERFORMER_JOIN);
    Table<TerritoryRecord> territoryRef = TERRITORY.as(TERRITORY_JOIN);
    Table<ConceptRecord> conceptRef = CONCEPT.as(CONCEPT_JOIN);

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

    Boolean update(Timestamp timestamp, UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description, Boolean force) throws Exception;

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

    static I_CompositionAccess retrieveInstance2(I_DomainAccess domainAccess, UUID id) throws Exception {
        return CompositionAccess.retrieveInstance2(domainAccess, id);
    }

    /**
     * Create a new composition access instance from a composition record
     * @param domainAccess SQL context, knowledge
     * @param compositionRecord a valid CompositionRecord
     * @return the interface
     * @see com.ethercis.jooq.pg.tables.records.CompositionRecord
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

    /**
     * retrieve the number of versions for this composition or 1 if no history present
     * @param domainAccess
     * @param compositionId
     * @return
     * @throws Exception
     */

    static Integer getLastVersionNumber(I_DomainAccess domainAccess, UUID compositionId) throws Exception {
        return CompositionAccess.getLastVersionNumber(domainAccess, compositionId);
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

    void setCompositionRecord(Result<?> records);

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

    Integer getVersion();

    void updateCompositionData(Composition newComposition) throws EhrException;

    void setContext(org.openehr.rm.composition.EventContext historicalEventContext);

//    void setCommitted(boolean b);
}
