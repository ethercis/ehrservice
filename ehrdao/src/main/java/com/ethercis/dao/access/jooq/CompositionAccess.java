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
import org.jooq.DSLContext;
import org.jooq.Record;
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
        PartyProxy composer = composition.getComposer();

        if (!(composer instanceof PartyIdentified))
            throw new IllegalArgumentException("Composer found in composition is not an IdenfiedParty and is not supported:"+composer.toString());


        UUID composerId = I_PartyIdentifiedAccess.getOrCreateParty(this, (PartyIdentified) composer);

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
        }

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


}
