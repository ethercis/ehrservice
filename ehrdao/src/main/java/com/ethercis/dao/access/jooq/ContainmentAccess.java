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

import com.ethercis.dao.access.interfaces.I_ContainmentAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.encode.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Created by christian on 6/1/2016.
 */
public class ContainmentAccess extends DataAccess implements I_ContainmentAccess {

    static Logger logger = LogManager.getLogger(ContainmentAccess.class);

    UUID entryId;
    UUID compositionId;

    Map<String, String> ltree;
    Boolean debug;

    public  ContainmentAccess(DSLContext context, UUID entryId, String archetypeId, Map<String, String> ltreeMap, boolean debug){
        super(context, null);
        ltree = new HashMap<>();
        this.entryId = entryId;
        this.debug = debug;

        //initial label and path
        String rootArchetype = ItemStack.normalizeLabel(archetypeId);
        ltree.put(rootArchetype, "/composition["+archetypeId+"]");

        for (Map.Entry entry: ltreeMap.entrySet()){
            String label = rootArchetype+"."+entry.getKey().toString();
            String path = entry.getValue().toString();
            ltree.put(label, path);
            if (debug)
                logger.debug("LABEL:"+label+"|PATH:"+path);
        }
    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {
        return commit();
    }

    @Override
    public UUID commit() throws Exception {
        commitContainments();
        return null;
    }

    @Override
    public Boolean update(Timestamp transactionTime) throws Exception {
        return update();
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        return update();
    }

    @Override
    public Boolean update() throws Exception {
        commitContainments();
        return true;
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        return update();
    }

    //TODO: changes this to work on template instead and avoid the loop for writing!
    private void commitContainments() throws SQLException {
        if (ltree == null){
            throw new IllegalArgumentException("Containment label tree is not initialized, aborting");
        }
        //if entries exists already for this entry delete them
        if (context.fetchExists(CONTAINMENT, CONTAINMENT.COMP_ID.eq(compositionId))){
            context.delete(CONTAINMENT).where(CONTAINMENT.COMP_ID.eq(compositionId)).execute();
        }

        //insert the new containment for this composition
        for (Map.Entry entry: ltree.entrySet()){
            context.insertInto(CONTAINMENT, CONTAINMENT.COMP_ID, CONTAINMENT.LABEL, CONTAINMENT.PATH)
                    .values(DSL.val(compositionId), DSL.field(DSL.val(entry.getKey().toString())+"::ltree"), DSL.val(entry.getValue().toString()))
                    .execute();
        }
    }

    @Override
    public Integer delete() throws Exception {
        return null;
    }

    @Override
    public void setCompositionId(UUID compositionId) {
        this.compositionId = compositionId;
    }
}
