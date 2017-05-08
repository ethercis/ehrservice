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

package com.ethercis.dao.access.util;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.jooq.pg.tables.records.TemplateRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * This class populate the 'template' table with meta-data found in each templates held in a directory
 * Created by christian on 4/27/2017.
 */
public class OptTemplateRef extends DataAccess implements I_OptTemplateRef {

    static Logger log = LogManager.getLogger(OptTemplateRef.class);

    public OptTemplateRef(I_DomainAccess domainAccess) {
        super(domainAccess);
    }

    public OptTemplateRef(DSLContext context, I_KnowledgeCache knowledgeCache) {
        super(context, knowledgeCache);
    }

    @Override
    public void upsert() throws Exception {
        Map<String,Collection<Map<String, String>>> operationalTemplateMap = getKnowledgeManager().listOperationalTemplates();
        int count = 0;
        for (Map<String, String> defMap: operationalTemplateMap.get(getKnowledgeManager().TEMPLATES)){
            if (defMap.containsKey(getKnowledgeManager().ERROR))
                continue;
            else {
                ++count;
                upsertEntry(defMap);
            }
        }
        log.info("Created:"+count+" template entries");
    }

    @Override
    public void deleteAll(){
        context.delete(TEMPLATE_HEADING_XREF).execute();
        context.delete(TEMPLATE).execute(); //referential integrity!
    }

    private void upsertEntry(Map<String, String> definitions) throws Exception {
        UUID templateUid = UUID.fromString(definitions.get(getKnowledgeManager().UID));
        //check if the entry exists first
        if (context.fetchExists(TEMPLATE, TEMPLATE.UID.eq(templateUid))){
            //create a new template Uid
            templateUid = UUID.randomUUID();
        }

        String templateId = definitions.get(getKnowledgeManager().TEMPLATE_ID);
        if (context.fetchExists(TEMPLATE, TEMPLATE.TEMPLATE_ID.eq(templateId))){
            //create a new template Uid
            log.warn("TemplateId already exists, skipping:"+templateId);
            return;
        }

        TemplateRecord templateRecord = context.newRecord(TEMPLATE);
        templateRecord.setUid(templateUid);
        templateRecord.setTemplateId(templateId);
        templateRecord.setConcept(definitions.get(getKnowledgeManager().CONCEPT));
        templateRecord.store();

    }
}
