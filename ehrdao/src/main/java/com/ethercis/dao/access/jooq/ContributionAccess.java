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
import com.ethercis.jooq.pg.enums.ContributionChangeType;
import com.ethercis.jooq.pg.enums.ContributionDataType;
import com.ethercis.jooq.pg.enums.ContributionState;
import com.ethercis.jooq.pg.tables.records.ContributionHistoryRecord;
import com.ethercis.jooq.pg.tables.records.ContributionRecord;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;

import java.sql.Timestamp;
import java.util.*;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by Christian Chevalley on 4/17/2015.
 */
public class ContributionAccess extends DataAccess implements I_ContributionAccess {

    Logger logger = LogManager.getLogger(CompositionAccess.class);

    private ContributionRecord contributionRecord;

    private Map<UUID, I_CompositionAccess> compositions = new HashMap<>();
    private final String signature = "$system$"; //used to sign a contribution during commit


    public ContributionAccess(DSLContext context, I_KnowledgeCache knowledgeManager, UUID ehrId, UUID systemId, UUID committerId, String description, Integer changeTypeCode, ContributionDef.ContributionType contributionType, ContributionDef.ContributionState contributionState){

        super(context, knowledgeManager);

        this.contributionRecord = context.newRecord(CONTRIBUTION);

        contributionRecord.setId(UUID.randomUUID());
        String changeTypeString = I_ConceptAccess.fetchConceptLitteral(this, changeTypeCode, "en");
        contributionRecord.setChangeType(ContributionChangeType.valueOf(changeTypeString));
        contributionRecord.setCommitter(committerId);
        contributionRecord.setSystemId(systemId);
        contributionRecord.setDescription(description);
        contributionRecord.setContributionType(ContributionDataType.valueOf(contributionType.getLiteral()));
        contributionRecord.setState(com.ethercis.jooq.pg.enums.ContributionState.valueOf(contributionState.getLiteral()));
//        contributionRecord.setSignature(signature);
        contributionRecord.setEhrId(ehrId);
    }

    public ContributionAccess(DSLContext context, I_KnowledgeCache knowledgeManager, UUID ehrId){

        super(context, knowledgeManager);

        this.contributionRecord = context.newRecord(CONTRIBUTION);

        contributionRecord.setEhrId(ehrId);
    }


    public ContributionAccess(I_DomainAccess domainAccess) {
        super(domainAccess);
    }

    @Override
    public void addComposition(I_CompositionAccess compositionAccess) throws Exception {
        //set local composition field from this contribution
        compositionAccess.setEhrid(contributionRecord.getEhrId());
        if (compositionAccess.getComposerId() == null)
            compositionAccess.setComposerId(contributionRecord.getCommitter());
        compositionAccess.setContributionId(contributionRecord.getId()); //this is the ContributionVersionId!!!

        compositions.put(compositionAccess.getId(), compositionAccess);
    }

    @Override
    public boolean removeComposition(I_CompositionAccess compositionAccess) throws Exception {
        I_CompositionAccess removed = compositions.remove(compositionAccess.getId());
        return removed != null;
    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {

        if (contributionRecord.getState() == ContributionState.incomplete){
            logger.warn("Contribution state has not been set");
        }


//        if (compositions.isEmpty())
//            logger.warn("Contribution does not contain any composition...");

        DateTime committedDateTime = DateTime.now();
        contributionRecord.setTimeCommitted(new Timestamp(committedDateTime.getMillis()));
        contributionRecord.setTimeCommittedTzid(committedDateTime.getZone().getID());
        contributionRecord.setSysTransaction(transactionTime);
        contributionRecord.store();
        UUID contributionId = contributionRecord.getId();

        contributionRecord.store();

        //commit the compositions
        for (I_CompositionAccess compositionAccess: compositions.values()){
            compositionAccess.commit(transactionTime);
        }

        return contributionId;
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public UUID commit(Timestamp transactionTime, UUID committerId, UUID systemId, String contributionType, String contributionState, String contributionChangeType, String description) throws Exception {

        ContributionDataType type = null;
        ContributionDef.ContributionState state = null;
        I_ConceptAccess.ContributionChangeType changeType = null;

        if (contributionType == null)
            type = ContributionDataType.valueOf(contributionType);

        if (contributionState != null)
            state = ContributionDef.ContributionState.valueOf(contributionState);

        if (contributionChangeType != null)
            changeType = I_ConceptAccess.ContributionChangeType.valueOf(contributionChangeType);

        return commit(transactionTime, committerId, systemId, type, state, changeType, description);
    }

    @Override
    public UUID commit(Timestamp transactionTime, UUID committerId, UUID systemId, ContributionDataType contributionType, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception {
        //set contribution  attributes
        if (committerId == null) {
            //get current user from JVM
            String defaultUser = System.getProperty("user.name");
            //check for that user in the DB
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            String scheme = System.getProperty("host.name");
            if (scheme == null)
                scheme = "local";
            committerId = I_PartyIdentifiedAccess.getOrCreatePartyByExternalRef(this, defaultUser, UUID.randomUUID().toString(), scheme, localMachine.getCanonicalHostName(), "PARTY");
        }
        setCommitter(committerId);

        if (contributionType == null)
            setContributionDataType(ContributionDataType.valueOf(I_ContributionAccess.DATA_TYPE_OTHER));
        else
            setContributionDataType(contributionType);

        if (systemId != null) {
            setSystemId(systemId);
        }
        else {
            setSystemId(I_SystemAccess.createOrRetrieveLocalSystem(this));
        }

        if (state != null)
            setState(state);
        else
            setState(ContributionDef.ContributionState.valueOf(I_ContributionAccess.STATE_COMPLETE));

        if (contributionChangeType != null)
            setChangeType(I_ConceptAccess.fetchContributionChangeType(this, contributionChangeType.name()));
        else
            setChangeType(I_ConceptAccess.fetchContributionChangeType(this, I_ConceptAccess.ContributionChangeType.creation));

        if (description != null)
            setDescription(description);

        return commit(transactionTime);
    }

    @Override
    public Boolean update(Timestamp transactionTime, UUID committerId, UUID systemId, String contributionType, String contributionState, String contributionChangeType, String description) throws Exception {
        //set contribution  attributes
        ContributionDataType type = null;
        ContributionDef.ContributionState state = null;
        I_ConceptAccess.ContributionChangeType changeType = null;

        if (contributionType == null)
            type = ContributionDataType.valueOf(contributionType);

        if (contributionState != null)
            state = ContributionDef.ContributionState.valueOf(contributionState);

        if (contributionChangeType != null)
            changeType = I_ConceptAccess.ContributionChangeType.valueOf(contributionChangeType);

        return update(transactionTime, committerId, systemId, type, state, changeType, description);
    }


    @Override
    public Boolean update(Timestamp transactionTime, UUID committerId, UUID systemId, ContributionDataType contributionType, ContributionDef.ContributionState state, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception {
        //set contribution  attributes
        if (committerId != null)
            setCommitter(committerId);
        if (contributionType != null)
            setContributionDataType(contributionType);
        if (systemId != null)
            setSystemId(systemId);
        if (state != null)
            setState(state);
        if (contributionChangeType != null)
            setChangeType(I_ConceptAccess.fetchContributionChangeType(this, contributionChangeType));
        if (description != null)
            setDescription(description);

        return update(transactionTime);
    }

    @Override
    public UUID commitWithSignature(String signature) throws Exception {
        contributionRecord.setSignature(signature);
        contributionRecord.setState(ContributionState.valueOf("complete"));
        DateTime committedDateTime = DateTime.now();
        contributionRecord.setTimeCommitted(new Timestamp(committedDateTime.getMillis()));
        contributionRecord.setTimeCommittedTzid(committedDateTime.getZone().getID());
        contributionRecord.store();

        return contributionRecord.getId();
    }

    @Override
    public UUID updateWithSignature(String signature) throws Exception {
        contributionRecord.setSignature(signature);
        contributionRecord.setState(ContributionState.valueOf("complete"));
        DateTime committedDateTime = DateTime.now();
        contributionRecord.setTimeCommitted(new Timestamp(committedDateTime.getMillis()));
        contributionRecord.setTimeCommittedTzid(committedDateTime.getZone().getID());
        contributionRecord.update();

        return contributionRecord.getId();
    }


    /**
     * updateComposition an existing composition
     * @param compositionAccess
     */
    @Override
    public void updateComposition(I_CompositionAccess compositionAccess) throws Exception {

        compositions.remove(compositionAccess.getId());
        compositions.put(compositionAccess.getId(), compositionAccess);
        logger.info("Updated composition with id:" + compositionAccess.getId());
        contributionRecord.changed(true);
        update(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws Exception {
        return update(transactionTime, false);
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        boolean updated = false;

//        if (contributionRecord.getState() == ContributionState.incomplete){
//            logger.warn("Contribution state has not been set");
//        }

        if (force || contributionRecord.changed()) {

            if (!contributionRecord.changed()) {
                //hack: force tell jOOQ to perform updateComposition whatever...
                contributionRecord.changed(true);
                //jOOQ limited support of TSTZRANGE, exclude sys_period from updateComposition!
                contributionRecord.changed(CONTRIBUTION.SYS_PERIOD, false); //managed by an external trigger anyway...
            }
            contributionRecord.setSysTransaction(transactionTime);
            DateTime committedDateTime = DateTime.now();
            contributionRecord.setTimeCommitted(new Timestamp(committedDateTime.getMillis()));
            contributionRecord.setTimeCommittedTzid(committedDateTime.getZone().getID());
//            contributionRecord.setTimeCommitted(new Timestamp(DateTime.now().getMillis()));
            contributionRecord.update();
            updated |= true;
        }

        //commit or updateComposition the compositions
        //TODO: ---- not complete !!!
        //get the list of composition uuids *referencing* the current contribution
//        List<UUID> allUuids = context.select(COMPOSITION.ID).from(COMPOSITION).where(COMPOSITION.IN_CONTRIBUTION.eq(contributionRecord.getId())).fetch(COMPOSITION.ID);
//        updateChangedCompositions(CollectionUtils.intersection(allUuids, compositions.keySet()), transactionTime, force);
//        commitAddedCompositions(CollectionUtils.subtract(compositions.keySet(), allUuids), transactionTime);
//        deleteRemovedCompositions(CollectionUtils.subtract(allUuids, compositions.keySet()));

        return updated;
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
        //delete contribution record
        count += contributionRecord.delete();

        return count;
    }

    private void deleteRemovedCompositions(Collection<UUID> removed){
        if (removed.isEmpty())
            return;

        for (UUID uuid: removed){
            context.delete(COMPOSITION).where(COMPOSITION.ID.eq(uuid));
            logger.debug("Deleted composition:"+uuid);
        }
    }

    private void commitAddedCompositions(Collection<UUID> added, Timestamp transactionTime) throws Exception {
        if (added.isEmpty())
            return;

        for (UUID uuid: added){
            compositions.get(uuid).commit(transactionTime);
            logger.debug("Committed composition:"+uuid);
        }
    }

    private void updateChangedCompositions(Collection<UUID> updated, Timestamp transactionTime, boolean force) throws Exception {
        if (updated.isEmpty())
            return;

        for (UUID uuid: updated){
            compositions.get(uuid).update(transactionTime, force);
            logger.debug("Updated composition:"+uuid);
        }
    }

    public static I_ContributionAccess retrieveInstance(I_DomainAccess domainAccess, UUID contributionId) throws Exception {

        ContributionAccess contributionAccess = new ContributionAccess(domainAccess);

        contributionAccess.contributionRecord = domainAccess.getContext().fetchOne(CONTRIBUTION, CONTRIBUTION.ID.eq(contributionId));

        if (contributionAccess.contributionRecord == null)
            return null;

        contributionAccess.compositions = CompositionAccess
                .retrieveCompositionsInContributionVersion(domainAccess, contributionAccess.contributionRecord.getId(), 0);

        return contributionAccess;

    }

    public static I_ContributionAccess retrieveVersionedInstance(I_DomainAccess domainAccess, UUID contributionVersionId, Integer versionNumber) throws Exception {
//
//        ContributionAccess contributionAccess = new ContributionAccess(domainAccess);
//
//        //get contribution record
//        contributionAccess.contributionVersionRecord = domainAccess.getContext().fetchOne(CONTRIBUTION_VERSION, CONTRIBUTION_VERSION.ID.eq(contributionVersionId));
//
//        //get the contribution referenced in contribution record
//        contributionAccess.contributionRecord = domainAccess.getContext().fetchOne(CONTRIBUTION, CONTRIBUTION.ID.eq(contributionAccess.contributionVersionRecord.getContributionId()));
//
//        //get the compositions referencing the contribution  version
//        contributionAccess.compositions = CompositionAccess.retrieveCompositionsInContributionVersion(domainAccess, contributionAccess.contributionVersionRecord.getId());
//
//        return contributionAccess;
            return null;
    }

    public static I_ContributionAccess retrieveVersionedInstance(I_DomainAccess domainAccess, UUID contributionVersionId, Timestamp transactionTime) throws Exception {

        ContributionAccess contributionAccess = new ContributionAccess(domainAccess);

        ContributionHistoryRecord contributionHistoryRecord = domainAccess.getContext()
                .fetchOne(CONTRIBUTION_HISTORY,
                        CONTRIBUTION_HISTORY.ID.eq(contributionVersionId)
                                .and(CONTRIBUTION_HISTORY.SYS_TRANSACTION.eq(transactionTime)));

        if (contributionHistoryRecord != null){
            contributionAccess.contributionRecord = domainAccess.getContext().newRecord(CONTRIBUTION);
            contributionAccess.contributionRecord.from(contributionHistoryRecord);
            return contributionAccess;
        }
        else
            return null;
    }

    public I_ContributionAccess retrieve(UUID id) throws Exception {
        return retrieveInstance(this, id);
    }


    @Override
    public UUID getContributionId(){
        return contributionRecord.getId();
    }

    @Override
    public UUID getContributionVersionId() { return null;}

    @Override
    public void setChangeType(UUID changeType){
        String changeTypeString = I_ConceptAccess.fetchConceptLitteral(this, changeType);
        contributionRecord.setChangeType(ContributionChangeType.valueOf(changeTypeString));
    }

    @Override
    public void setChangeType(I_ConceptAccess.ContributionChangeType changeType){
        contributionRecord.setChangeType(ContributionChangeType.valueOf(changeType.name()));
    }

    @Override
    public void setContributionDataType(ContributionDataType contributionDataType){
        contributionRecord.setContributionType(contributionDataType);
    }

    @Override
    public ContributionDataType getContributionDataType(){
        return contributionRecord.getContributionType();
    }

    @Override
    public void setCommitter(UUID committer){
        contributionRecord.setCommitter(committer);
    }

    @Override
    public void setDescription(String description){
        contributionRecord.setDescription(description);
    }

    @Override
    public void setTimeCommitted(Timestamp timeCommitted){
        contributionRecord.setTimeCommitted(timeCommitted);
    }

    @Override
    public void setSystemId(UUID systemId){
        contributionRecord.setSystemId(systemId);
    }

    @Override
    public void setState(ContributionDef.ContributionState state){
        contributionRecord.setState(ContributionState.valueOf(state.getLiteral()));
    }

    @Override
    public void setComplete(){
        contributionRecord.setState(ContributionState.valueOf(ContributionState.complete.getLiteral()));
    }

    @Override
    public void setIncomplete(){
        contributionRecord.setState(ContributionState.valueOf(ContributionState.incomplete.getLiteral()));
    }

    @Override
    public void setDeleted(){
        contributionRecord.setState(ContributionState.valueOf(ContributionState.deleted.getLiteral()));
    }

    @Override
    public UUID getChangeTypeId(){
        ContributionChangeType contributionChangeType = contributionRecord.getChangeType();
        I_ConceptAccess.ContributionChangeType contributionChangeType1 = I_ConceptAccess.ContributionChangeType.valueOf(contributionChangeType.getLiteral());
        return I_ConceptAccess.fetchContributionChangeType(this, contributionChangeType1);
    }

    @Override
    public String getChangeTypeLitteral(){
        ContributionChangeType contributionChangeType = contributionRecord.getChangeType();
        return contributionChangeType.getLiteral();
    }

    @Override
    public UUID getCommitter(){
        return contributionRecord.getCommitter();
    }

    @Override
    public String getDescription(){
        return contributionRecord.getDescription();
    }

    @Override
    public Timestamp getTimeCommitted(){
        return contributionRecord.getTimeCommitted();
    }

    @Override
    public UUID getSystemId(){
        return contributionRecord.getSystemId();
    }

    @Override
    public ContributionDef.ContributionType getContributionType(){
        return ContributionDef.ContributionType.valueOf(contributionRecord.getContributionType().getLiteral());
    }

    @Override
    public ContributionDef.ContributionState getContributionState(){
        return ContributionDef.ContributionState.valueOf(contributionRecord.getState().getLiteral());
    }

    @Override
    public UUID getEhrId(){
        return contributionRecord.getEhrId();
    }

    @Override
    public Set<UUID> getCompositionIds(){
        return compositions.keySet();
    }

    @Override
    public I_CompositionAccess getComposition(UUID id){
        return compositions.get(id);
    }

    @Override
    public void setDataType(ContributionDataType contributionDataType){
        contributionRecord.setContributionType(contributionDataType);
    }

    @Override
    public String getDataType(){
        return contributionRecord.getContributionType().getLiteral();
    }

    @Override
    public UUID getId(){
        return contributionRecord.getId();
    }
}
