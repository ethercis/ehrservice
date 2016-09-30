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

import com.ethercis.dao.access.interfaces.I_CompoXrefAccess;
import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.support.DataAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.COMPO_XREF;

/**
 * Deals with composition links. For example to link an ACTION with an INSTRUCTION with  or an OBSERVATION
 * Created by christian on 9/12/2016.
 */
public class CompoXRefAccess extends DataAccess implements I_CompoXrefAccess {

    static Logger log = LogManager.getLogger(CompositionAccess.class);

    public CompoXRefAccess(I_DomainAccess domainAccess) {
        super(domainAccess);
    }

    @Override
    public Map<UUID, Timestamp> getLinkList(UUID masterUid){

        Map<UUID, Timestamp> resultMap =
            getContext()
                .select(COMPO_XREF.CHILD_UUID, COMPO_XREF.SYS_TRANSACTION)
                .from(COMPO_XREF)
                .where(COMPO_XREF.MASTER_UUID.eq(masterUid))
                .fetch().intoMap(COMPO_XREF.CHILD_UUID, COMPO_XREF.SYS_TRANSACTION);

        return resultMap;
    }

    @Override
    public UUID getLastLink(UUID masterUid){
        return getContext()
                .select(COMPO_XREF.CHILD_UUID)
                .from(COMPO_XREF)
                .where(COMPO_XREF.MASTER_UUID.eq(masterUid))
                .orderBy(COMPO_XREF.SYS_TRANSACTION.desc())
                .fetchOne(COMPO_XREF.CHILD_UUID);
    }

    @Override
    public int setLink(UUID masterUid, UUID childUid){
        Timestamp timestamp = new Timestamp(DateTime.now().getMillis());
        return getContext()
                .insertInto(COMPO_XREF)
                .columns(COMPO_XREF.MASTER_UUID, COMPO_XREF.CHILD_UUID, COMPO_XREF.SYS_TRANSACTION)
                .values(masterUid, childUid, timestamp)
                .execute();
    }
}
