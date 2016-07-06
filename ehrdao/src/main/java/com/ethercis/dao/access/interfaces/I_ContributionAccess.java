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

import com.ethercis.dao.access.jooq.ContributionAccess;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.jooq.pg.enums.ContributionDataType;
import com.ethercis.ehr.util.EhrException;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

/**
 * Access layer to Contributions
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_ContributionAccess extends I_SimpleCRUD<I_ContributionAccess, UUID> {

    String STATE_COMPLETE = "complete";
    String STATE_INCOMPLETE = "incomplete";
    String STATE_DELETED = "deleted";

    String DATA_TYPE_COMPOSITION = "composition";
    String DATA_TYPE_FOLDER = "folder";
    String DATA_TYPE_EHR = "ehr";
    String DATA_TYPE_SYSTEM = "system";
    String DATA_TYPE_OTHER = "other";


    /**
     * get a new access layer instance
     * @param domain SQL context
     * @param ehrId the EHR uuid this contribution belong to
     * @param systemId the system uuid from which the contribution is generated
     * @param composerId the composer uuid that generate this contribution
     * @param description a literal describing the contribution
     * @param setting a concept code describing the change type
     * @param contributionType the contribution type
     * @param contributionState the contribution state
     * @return a new I_ContributionAccess
     * @see com.ethercis.dao.access.interfaces.I_PartyIdentifiedAccess
     * @see com.ethercis.dao.access.interfaces.I_EhrAccess
     * @see com.ethercis.dao.access.interfaces.I_SystemAccess
     * @see com.ethercis.dao.access.interfaces.I_ConceptAccess
     * @see com.ethercis.dao.access.util.ContributionDef
     */
    public static I_ContributionAccess getNewInstance(I_DomainAccess domain, UUID ehrId, UUID systemId, UUID composerId, String description, Integer setting, ContributionDef.ContributionType contributionType, ContributionDef.ContributionState contributionState){
        return new ContributionAccess(domain.getContext(), domain.getKnowledgeManager(), ehrId, systemId, composerId, description, setting, contributionType, contributionState);
    }

    public static I_ContributionAccess getInstance(I_DomainAccess domain, UUID ehrId){
        return new ContributionAccess(domain.getContext(), domain.getKnowledgeManager(), ehrId);
    }

    /**
     * retrieve an instance of I_ContributionAccess layer to the DB
     * @param domainAccess SQL context
     * @param contributionId the contribution id
     * @return an I_ContributionAccess instance or null
     * @throws Exception error when accessing the DB
     */
    public static I_ContributionAccess retrieveInstance(I_DomainAccess domainAccess, UUID contributionId) throws Exception {
        return ContributionAccess.retrieveInstance(domainAccess, contributionId);
    }

    /**
     * retrieve a contribution from a version
     * @param domainAccess SQL context
     * @param contributionVersionId the contribution version UUID
     * @return an I_ContributionAccess instance or null
     * @throws Exception error when accessing the DB
     */
    public static I_ContributionAccess retrieveVersionedInstance(I_DomainAccess domainAccess, UUID contributionVersionId, Integer versionNumber) throws Exception {
        return ContributionAccess.retrieveVersionedInstance(domainAccess, contributionVersionId, versionNumber);
    }

    public static I_ContributionAccess retrieveVersionedInstance(I_DomainAccess domainAccess, UUID contributionVersionId, Timestamp timestamp) throws Exception {
        return ContributionAccess.retrieveVersionedInstance(domainAccess, contributionVersionId, timestamp);
    }

    /**
     * add a new composition to this contribution<br>
     * NB: The contribution and composition requires commit() to be saved in the DB
     * @param compositionAccess a valid I_CompositionAccess
     * @throws EhrException
     */
    void addComposition(I_CompositionAccess compositionAccess) throws Exception;

    UUID updateWithSignature(String signature) throws Exception;

    /**
     * updateComposition an <b>existing</b> composition<br>
     * only a composition with the same id is effectively updated with this method<br>
     * NB: The contribution and composition requires commit() to be saved in the DB
     * @param compositionAccess
     */
    void updateComposition(I_CompositionAccess compositionAccess) throws Exception;

    boolean removeComposition(I_CompositionAccess compositionAccess) throws Exception;

    UUID commit(Timestamp transactionTime, UUID committerId, UUID systemId, String contributionType, String contributionState, String contributionChangeType, String description) throws Exception;

    UUID commit(Timestamp transactionTime, UUID committerId, UUID systemId, ContributionDataType contributionType, ContributionDef.ContributionState contributionState, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception;

    Boolean update(Timestamp transactionTime, UUID committerId, UUID systemId, String contributionType, String contributionState, String contributionChangeType, String description) throws Exception;

    Boolean update(Timestamp transactionTime, UUID committerId, UUID systemId, ContributionDataType contributionType, ContributionDef.ContributionState contributionState, I_ConceptAccess.ContributionChangeType contributionChangeType, String description) throws Exception;

    /**
     * commit the contribution with a certifying signature<br>
     * the signature is stored in the Contribution Version entry, the state of the contribution is then 'complete'
     * @param signature String representing the certification
     * @return UUID of committed contribution
     * @throws Exception
     */
    UUID commitWithSignature(String signature) throws Exception;

    /**
     * get the contribution UUID
     * @return
     */
    UUID getContributionId();

    /**
     * get the contribution <b>version</b> uuid
     * @return
     */
    UUID getContributionVersionId();

    /**
     * set the change type (from concepts)
     * @param changeType
     */
    void setChangeType(UUID changeType);

    void setChangeType(I_ConceptAccess.ContributionChangeType changeType);

    void setContributionDataType(ContributionDataType contributionDataType);

    ContributionDataType getContributionDataType();

    /**
     * set the committer UUID
     * @param committer a Party Identified
     */
    void setCommitter(UUID committer);

    /**
     * set the contribution description
     * @param description
     */
    void setDescription(String description);

    /**
     * set the time committed
     * @param timeCommitted TimeStamp
     */
    void setTimeCommitted(Timestamp timeCommitted);

    /**
     * set a system UUID
     * @param systemId UUID
     */
    void setSystemId(UUID systemId);

    /**
     * set the state of contribution
     * @param state ContributionDef
     * @see com.ethercis.dao.access.util.ContributionDef
     */
    void setState(ContributionDef.ContributionState state);

    /**
     * set the contribution as complete
     */
    void setComplete();

    /**
     * set the contribution as incomplete
     */
    void setIncomplete();

    /**
     * set the contribution as deleted
     */
    void setDeleted();

    /**
     * get the change type UUID
     * @return UUID
     */
    UUID getChangeTypeId();

    String getChangeTypeLitteral();

    /**
     * get the committer UUID
     * @return
     */
    UUID getCommitter();

    /**
     * get the description literal
     * @return
     */
    String getDescription();

    /**
     * get the time committed
     * @return
     */
    Timestamp getTimeCommitted();

    /**
     * get the system id
     * @return
     */
    UUID getSystemId();

    /**
     * get the contribution type
     * @return
     * @see com.ethercis.dao.access.util.ContributionDef.ContributionType
     */
    ContributionDef.ContributionType getContributionType();

    /**
     * get the contribution state
     * @return
     * @see com.ethercis.dao.access.util.ContributionDef.ContributionState
     */
    ContributionDef.ContributionState getContributionState();

    /**
     * get the contribution Ehr Id it belongs to
     * @return Ehr UUID
     */
    UUID getEhrId();

    I_CompositionAccess getComposition(UUID id);

    Set<UUID> getCompositionIds();

    void setDataType(ContributionDataType contributionDataType);

    String getDataType();

    UUID getId();
}
