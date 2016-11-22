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

import com.ethercis.dao.access.jooq.ContextAccess;
import org.jooq.Result;
import org.openehr.rm.composition.EventContext;

import java.sql.Timestamp;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.EVENT_CONTEXT;

/**
 * Event Context access layer
 * ETHERCIS Project
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_ContextAccess extends I_SimpleCRUD<I_ContextAccess, UUID> {

    /**
     * get a new access layer instance to the table
     * @param domain SQL context
     * @param eventContext an EventContext instance
     * @return an <b>uncommitted</b> interface to the access layer
     * @see org.openehr.rm.composition.EventContext
     */
    public static I_ContextAccess getInstance(I_DomainAccess domain, EventContext eventContext) throws Exception {
        return new ContextAccess(domain.getContext(), eventContext);
    }

    void setRecordFields(UUID id, EventContext eventContext) throws Exception;

    /**
     * retrieve an Event Context access layer instance from the DB
     * @param domainAccess SQL context
     * @param id the event context id
     * @return an interface to the access layer
     */
    public static I_ContextAccess retrieveInstance(I_DomainAccess domainAccess, UUID id){
        return ContextAccess.retrieveInstance(domainAccess , id);
    }

    public static I_ContextAccess retrieveInstance(I_DomainAccess domainAccess, Result<?> records){
        return ContextAccess.retrieveInstance(domainAccess , records);
    }

    /**
     * quick delete...
     * @param domainAccess SQL context
     * @param id event context id
     * @return 1 on success, 0 otherwise
     */
    public static Integer delete(I_DomainAccess domainAccess, UUID id){
        return domainAccess.getContext().delete(EVENT_CONTEXT).where(EVENT_CONTEXT.ID.eq(id)).execute();
    }

    EventContext mapRmEventContext();

    EventContext mapRmEventContext(Result<?> records);

    String getOtherContextJson();

    public static EventContext retrieveHistoricalEventContext(I_DomainAccess domainAccess, UUID id, Timestamp transactionTime){
        return ContextAccess.retrieveHistoricalEventContext(domainAccess, id, transactionTime);
    }

    void setCompositionId(UUID compositionId);

    UUID getId();
}
