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
import com.ethercis.dao.access.interfaces.I_ConceptAccess;
import com.ethercis.dao.access.interfaces.I_ContributionAccess;
import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.dao.jooq.enums.ContributionDataType;
import com.ethercis.dao.jooq.enums.ContributionState;
import com.ethercis.dao.jooq.tables.records.ContributionRecord;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;

import java.sql.Timestamp;
import java.util.*;

import static com.ethercis.dao.jooq.Tables.COMPOSITION;
import static com.ethercis.dao.jooq.Tables.CONTRIBUTION;

/**
 * Created by Christian Chevalley on 4/17/2015.
 */
public class ContributionAccess extends DataAccess implements I_ContributionAccess {

    Logger logger = Logger.getLogger(CompositionAccess.class);

    private ContributionRecord contributionRecord;

    private Map<UUID, I_CompositionAccess> compositions = new HashMap<>();
    private final String signature = "$system$"; //used to sign a contribution during commit


    public ContributionAccess(DSLContext context, I_KnowledgeCache knowledgeManager, UUID ehrId, UUID systemId, UUID composerId, String description, Integer changeTypeCode, ContributionDef.ContributionType contributionType, ContributionDef.ContributionState contributionState){

        super(context, knowledgeManager);

        this.contributionRecord = context.newRecord(CONTRIBUTION);

        contributionRecord.setId(UUID.randomUUID());
        contributionRecord.setChangeType(I_ConceptAccess.fetchConcept(this, changeTypeCode, "en"));
        contributionRecord.setCommitter(composerId);
        contributionRecord.setSystemId(systemId);
        contributionRecord.setDescription(description);
        contributionRecord.setContributionType(ContributionDataType.valueOf(contributionType.getLiteral()));
        contributionRecord.setState(com.ethercis.dao.jooq.enums.ContributionState.valueOf(contributionState.getLiteral()));
        contributionRecord.setSignature(signature);
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
            logger.warn("Contribution state has not be set");
        }


        if (compositions.isEmpty())
            logger.warn("Contribution does not contain any composition...");

        contributionRecord.setTimeCommitted(new Timestamp(DateTime.now().getMillis()));
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
    public UUID commitWithSignature(String signature) throws Exception {
        contributionRecord.setSignature(signature);
        contributionRecord.setState(ContributionState.valueOf("complete"));
        contributionRecord.store();

        return contributionRecord.getId();
    }

    @Override
    public UUID updateWithSignature(String signature) throws Exception {
        contributionRecord.setSignature(signature);
        contributionRecord.setState(ContributionState.valueOf("complete"));
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

        if (contributionRecord.getState() == ContributionState.incomplete){
            logger.warn("Contribution state has not be set");
        }

        if (force || contributionRecord.changed()) {

            if (!contributionRecord.changed()) {
                //hack: force tell jOOQ to perform updateComposition whatever...
                contributionRecord.changed(true);
                //jOOQ limited support of TSTZRANGE, exclude sys_period from updateComposition!
                contributionRecord.changed(CONTRIBUTION.SYS_PERIOD, false); //managed by an external trigger anyway...
            }
            contributionRecord.setSysTransaction(transactionTime);
            contributionRecord.setTimeCommitted(new Timestamp(DateTime.now().getMillis()));
            contributionRecord.update();
            updated |= true;
        }

        //commit or updateComposition the compositions
        //TODO: ---- not complete !!!
        //get the list of composition uuids *referencing* the current contribution
        List<UUID> allUuids = context.select(COMPOSITION.ID).from(COMPOSITION).where(COMPOSITION.IN_CONTRIBUTION.eq(contributionRecord.getId())).fetch(COMPOSITION.ID);
        updateChangedCompositions(CollectionUtils.intersection(allUuids, compositions.keySet()), transactionTime, force);
        commitAddedCompositions(CollectionUtils.subtract(compositions.keySet(), allUuids), transactionTime);
        deleteRemovedCompositions(CollectionUtils.subtract(allUuids, compositions.keySet()));

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
        contributionRecord.setChangeType(changeType);
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
        return contributionRecord.getChangeType();
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
}
