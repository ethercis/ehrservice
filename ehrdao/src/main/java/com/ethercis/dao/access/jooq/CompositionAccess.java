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
import com.ethercis.jooq.pg.enums.ContributionDataType;
import com.ethercis.jooq.pg.tables.records.CompositionRecord;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.EhrException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;

import java.sql.*;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;
/**
 * Created by Christian Chevalley on 4/2/2015.
 */
public class CompositionAccess extends DataAccess implements I_CompositionAccess {

    static Logger log = LogManager.getLogger(CompositionAccess.class);

    private DateTime dateCreated;

    private CompositionRecord compositionRecord;

    private I_ContributionAccess contributionAccess = null; //locally referenced contribution associated to this composition
    private Composition composition;

    private static Integer version = 1; //default current version, no history

    List<I_EntryAccess> content = new ArrayList<>();
    //CHC: 9/9/15 this flag has been removed since we now use temporal tables with history
//    private boolean committed = false; //used to identified an existing composition to be amended
//    private Boolean active;

    public CompositionAccess(DSLContext context, I_KnowledgeCache knowledgeManager, String languageCode, String territoryCode, DateTime dateCreated, UUID eventContextId, UUID composerId, UUID ehrId) throws EhrException{
        super(context, knowledgeManager);
        compositionRecord = context.newRecord(COMPOSITION);
        compositionRecord.setId(UUID.randomUUID());

        //check validity of codes
        Integer foundTerritoryCode = I_CompositionAccess.fetchTerritoryCode(this, territoryCode);

        if (foundTerritoryCode < 0)
            throw new EhrException(0, "Invalid two letter territory code");

        compositionRecord.setTerritory(foundTerritoryCode);

        if (languageCode == null) //defaulted to english
            languageCode = "en";
        else
            if (!(I_CompositionAccess.isValidLanguageCode(this, languageCode)))
                throw new EhrException(0, "Invalid language code");

        compositionRecord.setLanguage(languageCode);
        compositionRecord.setActive(true);
//        compositionRecord.setContext(eventContextId);
        compositionRecord.setComposer(composerId);
        compositionRecord.setEhrId(ehrId);

        if (dateCreated == null)
            this.dateCreated = DateTime.now();
        else
            this.dateCreated = dateCreated;

        //associate a contribution with this composition
        contributionAccess =  I_ContributionAccess.getInstance(this, compositionRecord.getEhrId());
        contributionAccess.setState(ContributionDef.ContributionState.COMPLETE);
    }

    public CompositionAccess(DSLContext context, I_KnowledgeCache knowledgeManager, Composition composition, DateTime dateCreated, UUID ehrId) throws Exception{
        super(context, knowledgeManager);

        this.composition = composition;

        String territoryCode = composition.getTerritory().getCodeString();
        String languageCode = composition.getLanguage().getCodeString();
//        PartyProxy composer = composition.getComposer();
//
//        if (!(composer instanceof PartyIdentified))
//            throw new IllegalArgumentException("Composer found in composition is not an IdenfiedParty and is not supported:"+composer.toString());
//
//
//        UUID composerId = I_PartyIdentifiedAccess.getOrCreateParty(this, (PartyIdentified) composer);

        UUID composerId = seekComposerId(composition.getComposer());

        compositionRecord = context.newRecord(COMPOSITION);
        compositionRecord.setId(UUID.randomUUID());

        //check validity of codes
//        Integer foundTerritoryCode = I_CompositionAccess.fetchTerritoryCode(this, territoryCode);
//
//        if (foundTerritoryCode < 0)
//            throw new EhrException(0, "Invalid two letter territory code");
//
//        compositionRecord.setTerritory(foundTerritoryCode);

        compositionRecord.setTerritory(seekTerritoryCode(territoryCode));

//        if (languageCode == null) //defaulted to english
//            languageCode = "en";
//        else
//        if (!(I_CompositionAccess.isValidLanguageCode(this, languageCode)))
//            throw new EhrException(0, "Invalid language code");

        compositionRecord.setLanguage(seekLanguageCode(languageCode));
        compositionRecord.setActive(true);
//        compositionRecord.setContext(eventContextId);
        compositionRecord.setComposer(composerId);
        compositionRecord.setEhrId(ehrId);

        if (dateCreated == null)
            this.dateCreated = DateTime.now();
        else
            this.dateCreated = dateCreated;

        //associate a contribution with this composition
        contributionAccess =  I_ContributionAccess.getInstance(this, compositionRecord.getEhrId());
        contributionAccess.setState(ContributionDef.ContributionState.COMPLETE);

    }

    private UUID seekComposerId(PartyProxy composer){
        if (!(composer instanceof PartyIdentified))
            throw new IllegalArgumentException("Composer found in composition is not an IdenfiedParty and is not supported:"+composer.toString());


        UUID composerId = I_PartyIdentifiedAccess.getOrCreateParty(this, (PartyIdentified) composer);

        return composerId;
    }

    private Integer seekTerritoryCode(String territoryCode) throws EhrException {
        Integer foundTerritoryCode = I_CompositionAccess.fetchTerritoryCode(this, territoryCode);

        if (foundTerritoryCode < 0)
            throw new EhrException(0, "Invalid two letter territory code");

       return foundTerritoryCode;
    }

    private String seekLanguageCode(String languageCode) throws EhrException {
        if (languageCode == null) //defaulted to english
            return "en";
        else
        if (!(I_CompositionAccess.isValidLanguageCode(this, languageCode)))
            throw new EhrException(0, "Invalid language code");

        return languageCode;
    }


    /**
     * constructor used to perform non static operation on instance
     * @param domainAccess
     * @param compositionRecord
     */
    public CompositionAccess(I_DomainAccess domainAccess, CompositionRecord compositionRecord){
        super(domainAccess);
        this.compositionRecord = compositionRecord;
        contributionAccess =  I_ContributionAccess.getInstance(this, compositionRecord.getEhrId());
        contributionAccess.setState(ContributionDef.ContributionState.COMPLETE);
    }

    public CompositionAccess(I_DomainAccess domainAccess){
        super(domainAccess);
    }

    @Override
    public UUID getEhrid() { return compositionRecord.getId();}

    @Override
    public UUID getComposerId() {
        return compositionRecord.getComposer();
    }

    @Override
    public UUID getContextId() {
        if (compositionRecord == null)
            return null;
        if (compositionRecord.getId() == null)
            return null;
        return context.fetchOne(EVENT_CONTEXT, EVENT_CONTEXT.COMPOSITION_ID.eq(compositionRecord.getId())).getId();
    }

    @Override
    public UUID getContributionVersionId() {
        return compositionRecord.getInContribution();
    }

    @Override
    public String getLanguageCode() {
        return compositionRecord.getLanguage();
    }

    @Override
    public Integer getTerritoryCode() {
        return compositionRecord.getTerritory();
    }

    @Override
    public List<I_EntryAccess> getContent() {
        return this.content;
    }

    @Override
    public void setContent(List<I_EntryAccess> content) {
        this.content = content;
    }

    @Override
    public void setEhrid(UUID ehrId) throws EhrException {
        compositionRecord.setEhrId(ehrId);
    }

    @Override
    public UUID getId(){return compositionRecord.getId();}

    @Override
    public void setComposerId(UUID composerId) throws EhrException {
        compositionRecord.setComposer(composerId);
    }

    @Override
    public void setContextCompositionId(UUID contextId) throws Exception {
        I_ContextAccess contextAccess = I_ContextAccess.retrieveInstance(this, contextId);
        contextAccess.setCompositionId(compositionRecord.getId());
        contextAccess.update(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public void setLanguageCode(String code) throws EhrException {
        compositionRecord.setLanguage(code);
    }

    @Override
    public void setTerritoryCode(Integer code) throws EhrException {
        compositionRecord.setTerritory(code);
    }

    @Override
    public void setContributionId(UUID contributionId) {
        compositionRecord.setInContribution(contributionId);
    }

    @Override
    public void setCompositionRecord(CompositionRecord record){
        this.compositionRecord = record;
    }

    @Override
    public void setCompositionRecord(Result<?> records){
        compositionRecord = context.newRecord(compositionRef);
        compositionRecord.setId((UUID) records.getValue(0, F_COMPOSITION_ID));
        compositionRecord.setLanguage((String)records.getValue(0, F_LANGUAGE));
//        compositionRecord.setTerritory((Integer)records.getValue(0, F_TERRITORY_CODE));
    }

    @Override
    public void setComposition(Composition composition) {
        this.composition = composition;
    }

//    @Override
//    public void setCommitted(boolean b) {
//        ;
//    }

    @Override
    public int addContent(I_EntryAccess entry) throws EhrException {
         entry.setCompositionId(compositionRecord.getId());
        content.add(entry);

        return content.size();
    }

    @Override
    public List<UUID> getContentIds() throws EhrException {
        List<UUID> entryList = new ArrayList<>();

        for (I_EntryAccess entryAccess: content){
            entryList.add(entryAccess.getId());
        }

        return entryList;
    }

    @Override
    public int deleteContent() throws EhrException {
        return context.delete(ENTRY).where(ENTRY.COMPOSITION_ID.eq(compositionRecord.getId())).execute();

    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {

        compositionRecord.setSysTransaction(transactionTime);
        compositionRecord.store();

        if (content.size() == 0)
            log.warn("Composition has no content:");

        try {
            for (I_EntryAccess entryAccess : content)
                entryAccess.commit(transactionTime);
        }
        catch (Exception exception){
            log.error("Problem in committing content, rolling back, exception:"+exception);
            throw new IllegalArgumentException("Could not commit content:"+exception);
        }

        EventContext eventContext = composition.getContext();
        I_ContextAccess contextAccess = I_ContextAccess.getInstance(this, eventContext);
        contextAccess.setCompositionId(compositionRecord.getId());
        contextAccess.commit(transactionTime);

        return compositionRecord.getId();
    }

    @Override
    public UUID commit() throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.commit(timestamp, null, null, ContributionDataType.composition, ContributionDef.ContributionState.COMPLETE, I_ConceptAccess.ContributionChangeType.creation, null);
        setContributionId(contributionAccess.getId());
        return commit(timestamp);
//        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public UUID commit(UUID committerId, UUID systemId, ContributionDef.ContributionState state, String description) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.commit(timestamp, committerId, systemId, ContributionDataType.composition, state, I_ConceptAccess.ContributionChangeType.creation, description);
        setContributionId(contributionAccess.getId());
        return commit(timestamp);
    }

    @Override
    public UUID commit(UUID committerId, UUID systemId, String description) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.commit(timestamp, committerId, systemId, ContributionDataType.composition, ContributionDef.ContributionState.COMPLETE, I_ConceptAccess.ContributionChangeType.creation, description);
        setContributionId(contributionAccess.getId());
        return commit(timestamp);
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws Exception {
        return update(transactionTime, false);
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        boolean result = false;

        if (force || compositionRecord.changed()) {
            //we assume the composition has been amended locally

            if (!compositionRecord.changed()) {
                compositionRecord.changed(true);
                //jOOQ limited support of TSTZRANGE, exclude sys_period from updateComposition!
                compositionRecord.changed(COMPOSITION.SYS_PERIOD, false);
            }

            compositionRecord.setSysTransaction(transactionTime);
            result |= compositionRecord.update() > 0;

            //updateComposition each entry if required
            for (I_EntryAccess entryAccess: content){
                entryAccess.update(transactionTime, true);
            }
//            context.fetch(ENTRY, ENTRY.COMPOSITION_ID.eq(compositionRecord.getId())).forEach(entry -> {
//                try {
//                    I_EntryAccess entryAccess = I_EntryAccess.retrieveInstance(this, entry.getId());
//                    entryAccess.updateComposition(transactionTime, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
        }

        if (force){ //updateComposition event context accordingly
            //retrieve context and force update
            I_ContextAccess contextAccess = I_ContextAccess.retrieveInstance(this, getContextId());
            contextAccess.update(transactionTime, force);
        }

        return result;
    }

    @Override
    public Boolean update() throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, null, null, null, null, I_ConceptAccess.ContributionChangeType.modification, null);
        return update(timestamp);
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, null, null, null, null, I_ConceptAccess.ContributionChangeType.modification, null);
        return update(timestamp, force);
    }

    @Override
    public Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp);
    }

    @Override
    public Boolean update(Timestamp timestamp, UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception {
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp);
    }

    @Override
    public Boolean update(Timestamp timestamp, UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description, Boolean force) throws Exception {
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp, force);
    }

    @Override
    public Boolean update(UUID committerId, UUID systemId, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description, Boolean force) throws Exception {
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, state, contributionChangeType, description);
        return update(timestamp, force);
    }

    /**
     * Delete a composition<br>
     * Delete by simulating an SQL CASCADE wherever appropriate<br>
     *     <ul>
     *         <li>delete entry first</li>
     *         <li>delete the corresponding version entry</li>
     *         <li>delete the composition record</li>
     *     </ul>
     * @return
     */
    @Override
    public Integer delete() throws Exception {

        return delete(null, null, null);

    }

    @Override
    public Integer delete(UUID committerId, UUID systemId, String description) throws Exception {

        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        contributionAccess.update(timestamp, committerId, systemId, null, ContributionDef.ContributionState.DELETED, I_ConceptAccess.ContributionChangeType.deleted, description);
        return compositionRecord.delete();
    }

    /**
     * delete a composition identified by its versioned Id
     * @param id the version id
     * @return
     */
    public static I_CompositionAccess retrieveCompositionVersion(I_DomainAccess domainAccess, UUID id, int version) throws Exception {

        if (version  < 1)
            throw new IllegalArgumentException("Version number must be > 0  please check your code");

//        //check if this version number matches the current version
//        Integer versionCount = domainAccess.getContext()
//                .selectCount()
//                .from(COMPOSITION_HISTORY)
//                .where(COMPOSITION_HISTORY.ID.eq(id)).fetchOne(0, int.class);

        if (getLastVersionNumber(domainAccess, id) == version){ //current version
            return retrieveInstance(domainAccess, id);
        }

        String versionQuery =
                "select row_id, in_contribution, ehr_id, language, territory, composer, sys_transaction from \n" +
                "  (select ROW_NUMBER() OVER (ORDER BY sys_transaction ASC ) AS row_id, * from ehr.composition_history " +
                        "WHERE id = ?) \n" +
                "    AS Version WHERE row_id = ?;";

        Connection connection = domainAccess.getContext().configuration().connectionProvider().acquire();
        PreparedStatement preparedStatement = connection.prepareStatement(versionQuery);
        preparedStatement.setObject(1, id);
        preparedStatement.setInt(2, version);

        ResultSet resultSet = preparedStatement.executeQuery();

        I_CompositionAccess compositionHistoryAccess = null;

        while(resultSet.next()){
            CompositionRecord compositionRecord1 = domainAccess.getContext().newRecord(COMPOSITION);
            compositionRecord1.setId(id);
            compositionRecord1.setInContribution(UUID.fromString(resultSet.getString("in_contribution")));
            compositionRecord1.setEhrId(UUID.fromString(resultSet.getString("ehr_id")));
            compositionRecord1.setLanguage(resultSet.getString("language"));
            compositionRecord1.setTerritory(resultSet.getInt("territory"));
            compositionRecord1.setComposer(UUID.fromString(resultSet.getString("composer")));
            compositionRecord1.setSysTransaction(resultSet.getTimestamp("sys_transaction"));
            compositionHistoryAccess = new CompositionAccess(domainAccess, compositionRecord1);
        }

        if (compositionHistoryAccess != null) {
            compositionHistoryAccess.setContent(I_EntryAccess.retrieveInstanceInCompositionVersion(domainAccess, compositionHistoryAccess, version));

            //retrieve the corresponding contribution
            I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveVersionedInstance(domainAccess, compositionHistoryAccess.getContributionVersionId(), compositionHistoryAccess.getSysTransaction());
            compositionHistoryAccess.setContributionAccess(contributionAccess);

            //retrieve versioned context
            EventContext historicalEventContext = I_ContextAccess.retrieveHistoricalEventContext(domainAccess, id, compositionHistoryAccess.getSysTransaction());
            //adjust context for entries
            if (historicalEventContext != null) {
                for (I_EntryAccess entryAccess : compositionHistoryAccess.getContent()) {
                    entryAccess.getComposition().setContext(historicalEventContext);
                }
            }

        }

        connection.close();

        return compositionHistoryAccess;
    }


    public static Integer getLastVersionNumber(I_DomainAccess domainAccess, UUID compositionId) throws Exception {

        if (!hasPreviousVersion(domainAccess, compositionId))
            return 1;

        Integer versionCount = domainAccess.getContext().fetchCount(COMPOSITION_HISTORY, COMPOSITION_HISTORY.ID.eq(compositionId));

        return versionCount + 1;
    }


    public static boolean hasPreviousVersion(I_DomainAccess domainAccess, UUID compositionId){
        return domainAccess.getContext().fetchExists(COMPOSITION_HISTORY, COMPOSITION_HISTORY.ID.eq(compositionId));
    }

    @Override
    public Timestamp getSysTransaction(){
        return compositionRecord.getSysTransaction();
    }


//    @Override
//    public I_CompositionAccess retrieve(UUID id) throws Exception {
//        return retrieveInstance(this, id);
//    }

    public static I_CompositionAccess retrieveInstance(I_DomainAccess domainAccess, UUID id) throws Exception {

        I_CompositionAccess compositionAccess = new CompositionAccess(domainAccess);
        CompositionRecord compositionRecord = domainAccess.getContext().selectFrom(COMPOSITION).where(COMPOSITION.ID.eq(id)).fetchOne();
        if (compositionRecord == null)
            return null;
        compositionAccess.setCompositionRecord(compositionRecord);
        compositionAccess.setContent(I_EntryAccess.retrieveInstanceInComposition(domainAccess, compositionAccess));
//        compositionAccess.setCommitted(true);
        //retrieve the corresponding contribution
        I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveInstance(domainAccess, compositionAccess.getContributionVersionId());
        compositionAccess.setContributionAccess(contributionAccess);

        return compositionAccess;
    }

    /**
     * use faster SQL query (one query)
     * @param domainAccess
     * @param id
     * @return
     * @throws Exception
     */
    public static I_CompositionAccess retrieveInstance2(I_DomainAccess domainAccess, UUID id) throws Exception {
        I_CompositionAccess compositionAccess = new CompositionAccess(domainAccess);

        SelectQuery<?> selectQuery = domainAccess.getContext().selectQuery();

        selectQuery.addSelect(
                ENTRY.COMPOSITION_ID.as(F_COMPOSITION_ID),
                        DSL.field("1 + COALESCE(("+
                                  DSL.selectCount()
                                        .from(COMPOSITION_HISTORY)
                                        .where(COMPOSITION_HISTORY.ID.eq(compositionRef.field(COMPOSITION.ID.getName(), UUID.class)))
                                        .groupBy(COMPOSITION_HISTORY.ID)
                                   +"), 0)").as(F_VERSION),

                ENTRY.ENTRY_.as(F_ENTRY),
                ENTRY.TEMPLATE_ID.as(F_ENTRY_TEMPLATE),
                compositionRef.field(COMPOSITION.LANGUAGE.getName()).as(F_LANGUAGE),
                territoryRef.field(TERRITORY.TWOLETTER.getName()).as(F_TERRITORY_CODE),
                composerRef.field(PARTY_IDENTIFIED.NAME.getName()).as(F_COMPOSER_NAME),
                composerRef.field(PARTY_IDENTIFIED.PARTY_REF_SCHEME.getName()).as(F_COMPOSER_REF_SCHEME),
                composerRef.field(PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.getName()).as(F_COMPOSER_REF_NAMESPACE),
                composerRef.field(PARTY_IDENTIFIED.PARTY_REF_VALUE.getName()).as(F_COMPOSER_REF_VALUE),
                composerRef.field(PARTY_IDENTIFIED.PARTY_REF_TYPE.getName()).as(F_COMPOSER_REF_TYPE),
                composerId.field(IDENTIFIER.ID_VALUE.getName()).as(F_COMPOSER_ID_VALUE),
                composerId.field(IDENTIFIER.ISSUER.getName()).as(F_COMPOSER_ID_ISSUER),
                composerId.field(IDENTIFIER.TYPE_NAME.getName()).as(F_COMPOSER_ID_TYPE_NAME),
                eventContextRef.field(EVENT_CONTEXT.START_TIME.getName()).as(F_CONTEXT_START_TIME),
                eventContextRef.field(EVENT_CONTEXT.START_TIME_TZID.getName()).as(F_CONTEXT_START_TIME_TZID),
                eventContextRef.field(EVENT_CONTEXT.END_TIME.getName()).as(F_CONTEXT_END_TIME),
                eventContextRef.field(EVENT_CONTEXT.END_TIME_TZID.getName()).as(F_CONTEXT_END_TIME_TZID),
                eventContextRef.field(EVENT_CONTEXT.LOCATION.getName()).as(F_CONTEXT_LOCATION),
                eventContextRef.field(EVENT_CONTEXT.OTHER_CONTEXT.getName()).as(F_CONTEXT_OTHER_CONTEXT),
                conceptRef.field(CONCEPT.CONCEPTID.getName()).as(F_CONCEPT_ID),
                conceptRef.field(CONCEPT.DESCRIPTION.getName()).as(F_CONCEPT_DESCRIPTION),
                facilityRef.field(PARTY_IDENTIFIED.NAME.getName()).as(F_FACILITY_NAME),
                facilityRef.field(PARTY_IDENTIFIED.PARTY_REF_SCHEME.getName()).as(F_FACILITY_REF_SCHEME),
                facilityRef.field(PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.getName()).as(F_FACILITY_REF_NAMESPACE),
                facilityRef.field(PARTY_IDENTIFIED.PARTY_REF_VALUE.getName()).as(F_FACILITY_REF_VALUE),
                facilityRef.field(PARTY_IDENTIFIED.PARTY_REF_TYPE.getName()).as(F_FACILITY_REF_TYPE),
                facilityId.field(IDENTIFIER.ID_VALUE.getName()).as(F_FACILITY_ID_VALUE),
                facilityId.field(IDENTIFIER.ISSUER.getName()).as(F_FACILITY_ID_ISSUER),
                facilityId.field(IDENTIFIER.TYPE_NAME.getName()).as(F_FACILITY_ID_TYPE_NAME),
                participationRef.field(PARTICIPATION.FUNCTION.getName()).as(F_PARTICIPATION_FUNCTION),
                participationRef.field(PARTICIPATION.MODE.getName()).as(F_PARTICIPATION_MODE),
                participationRef.field(PARTICIPATION.START_TIME.getName()).as(F_PARTICIPATION_START_TIME),
                participationRef.field(PARTICIPATION.START_TIME_TZID.getName()).as(F_PARTICIPATION_START_TIME_TZID),
                performerRef.field(PARTY_IDENTIFIED.NAME.getName()).as(F_PERFORMER_NAME),
                performerRef.field(PARTY_IDENTIFIED.PARTY_REF_SCHEME.getName()).as(F_PERFORMER_REF_SCHEME),
                performerRef.field(PARTY_IDENTIFIED.PARTY_REF_NAMESPACE.getName()).as(F_PERFORMER_REF_NAMESPACE),
                performerRef.field(PARTY_IDENTIFIED.PARTY_REF_VALUE.getName()).as(F_PERFORMER_REF_VALUE),
                performerRef.field(PARTY_IDENTIFIED.PARTY_REF_TYPE.getName()).as(F_PERFORMER_REF_TYPE)
        );
        selectQuery.addFrom(ENTRY);
        selectQuery.addJoin(compositionRef,compositionRef.field(COMPOSITION.ID.getName(), UUID.class).eq(ENTRY.COMPOSITION_ID));
        selectQuery.addJoin(composerRef,composerRef.field(PARTY_IDENTIFIED.ID.getName(), UUID.class).eq(compositionRef.field(COMPOSITION.COMPOSER.getName(), UUID.class)));
        selectQuery.addJoin(composerId, JoinType.LEFT_OUTER_JOIN, composerId.field(IDENTIFIER.PARTY.getName(), UUID.class).eq(composerRef.field(PARTY_IDENTIFIED.ID.getName(), UUID.class)));
        selectQuery.addJoin(eventContextRef,eventContextRef.field(EVENT_CONTEXT.COMPOSITION_ID.getName(), UUID.class).eq(ENTRY.COMPOSITION_ID));
        selectQuery.addJoin(facilityRef, JoinType.LEFT_OUTER_JOIN, facilityRef.field(PARTY_IDENTIFIED.ID.getName(), UUID.class).eq(eventContextRef.field(EVENT_CONTEXT.FACILITY.getName(), UUID.class)));
        selectQuery.addJoin(facilityId, JoinType.LEFT_OUTER_JOIN, facilityId.field(IDENTIFIER.PARTY.getName(), UUID.class).eq(facilityRef.field(PARTY_IDENTIFIED.ID.getName(), UUID.class)));
        selectQuery.addJoin(participationRef, JoinType.LEFT_OUTER_JOIN, participationRef.field(PARTICIPATION.EVENT_CONTEXT.getName(), UUID.class).eq(eventContextRef.field(EVENT_CONTEXT.ID.getName(), UUID.class)));
        selectQuery.addJoin(performerRef,JoinType.LEFT_OUTER_JOIN, performerRef.field(PARTY_IDENTIFIED.ID.getName(), UUID.class).eq(participationRef.field(PARTICIPATION.PERFORMER.getName(), UUID.class)));
        selectQuery.addJoin(territoryRef, territoryRef.field(TERRITORY.CODE.getName(), Integer.class).eq(compositionRef.field(COMPOSITION.TERRITORY)));
        selectQuery.addJoin(conceptRef, JoinType.LEFT_OUTER_JOIN, conceptRef.field(CONCEPT.ID.getName(), UUID.class).eq(eventContextRef.field(EVENT_CONTEXT.SETTING)));
        selectQuery.addConditions(ENTRY.COMPOSITION_ID.eq(id));

        Result<?> records = selectQuery.fetch();

        if (records.size() == 0)
            return null;

        compositionAccess.setCompositionRecord(records);
        compositionAccess.setContent(I_EntryAccess.retrieveInstanceInComposition(domainAccess, records));
//        compositionAccess.setCommitted(true);
        //retrieve the corresponding contribution
        I_ContributionAccess contributionAccess = I_ContributionAccess.retrieveInstance(domainAccess, compositionAccess.getContributionVersionId());
        compositionAccess.setContributionAccess(contributionAccess);

        return compositionAccess;
    }


    public static Map<UUID, I_CompositionAccess> retrieveCompositionsInContributionVersion(I_DomainAccess domainAccess, UUID contribution, Integer versionNumber){
        Map<UUID, I_CompositionAccess> compositions = new HashMap<>();

        try {
            domainAccess.getContext()
                    .selectFrom(COMPOSITION)
                    .where(COMPOSITION.IN_CONTRIBUTION.eq(contribution))
                    .fetch()
                    .forEach( record -> {
                        I_CompositionAccess compositionAccess = new CompositionAccess(domainAccess);
                        compositionAccess.setCompositionRecord(record);
                        try {
                            compositionAccess.setContent(I_EntryAccess.retrieveInstanceInComposition(domainAccess, compositionAccess));
                        }
                        catch (Exception e){
                            throw new IllegalArgumentException("DB inconsistency:"+e);
                        }
//                        compositionAccess.setCommitted(true);
                        compositions.put(compositionAccess.getId(), compositionAccess);

                    });
        }
        catch (Exception e){
            log.error("DB inconsistency:"+e);
            throw new IllegalArgumentException("DB inconsistency:"+e);
        }
        return compositions;
    }

    /**
     * retrieve compositions matching supplied filters
     * @param domainAccess
     * @param sqlFilter
     * @return
     */
    public static Map<UUID, I_CompositionAccess> retrieveCompositions(I_DomainAccess domainAccess, String sqlFilter){
        throw new IllegalArgumentException("NOT IMPLEMENTED");
    }

    @Override
    public I_ContributionAccess getContributionAccess() {
        return contributionAccess;
    }

    @Override
    public void setContributionAccess(I_ContributionAccess contributionAccess) {
        this.contributionAccess = contributionAccess;
    }

    @Override
    public Integer getVersion(){
        return version;
    }

    @Override
    public void updateCompositionData(Composition newComposition) throws EhrException {
        //update the mutable attributes
        setLanguageCode(seekLanguageCode(newComposition.getLanguage().getCodeString()));
        setTerritoryCode(seekTerritoryCode(newComposition.getTerritory().getCodeString()));
        setComposerId(seekComposerId(newComposition.getComposer()));
    }

    @Override
    public void setContext(EventContext eventContext) {
        composition.setContext(eventContext);
    }


}
