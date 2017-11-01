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


package com.ethercis.dao.access.handler;

import com.ethercis.dao.access.interfaces.*;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.FlatJsonCompositionConverter;
import com.ethercis.ehr.util.I_FlatJsonCompositionConverter;
import org.joda.time.DateTime;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Utility class to factorize the implementation of composition update in particular
 * ETHERCIS Project VirtualEhr
 * Created by Christian Chevalley on 9/18/2015.
 */
public class FlatJsonHandler implements I_FlatJsonHandler, I_CompositionMetaData {

    private final I_CompositionAccess compositionAccess;
    private final I_DomainAccess domainAccess;
    private String templateId;
    private final I_KnowledgeCache knowledgeCache;
    private Map<String, Integer> itemArrayPathMap;

    public FlatJsonHandler(I_DomainAccess domainAccess,  I_CompositionAccess compositionAccess, String templateId, Properties properties){
        this.knowledgeCache = domainAccess.getKnowledgeManager();
        this.templateId = templateId;
        this.domainAccess = domainAccess;
        this.compositionAccess = compositionAccess;
    }

    public FlatJsonHandler(I_DomainAccess domainAccess, String templateId){
        this.knowledgeCache = domainAccess.getKnowledgeManager();
        this.templateId = templateId;
        this.domainAccess = domainAccess;
        this.compositionAccess = null;
    }

    @Override
    public Composition build(String content) throws Exception {
        I_FlatJsonCompositionConverter flatJsonCompositionConverter = FlatJsonCompositionConverter.getInstance(knowledgeCache);
        Map flatMap = FlatJsonUtil.inputStream2Map(new StringReader(new String(content.getBytes())));
        Composition newComposition = flatJsonCompositionConverter.toComposition(templateId, flatMap);
        itemArrayPathMap = flatJsonCompositionConverter.getItemArrayPathMap();
        return newComposition;
    }

    @Override
    public UUID store(UUID ehrId, String flatJsonContent, UUID committerId, UUID systemId, String description) throws Exception {
        Composition newComposition = build(flatJsonContent);
        I_CompositionAccess access = I_CompositionAccess.getNewInstance(domainAccess, newComposition, DateTime.now(), ehrId);
        I_EntryAccess entry= I_EntryAccess.getNewInstance(domainAccess, templateId, 0, access.getId(), newComposition);
        access.addContent(entry);
        UUID compositionId = access.commit(committerId, systemId, description);
        return compositionId;
    }

    @Override
    public Boolean update(I_DomainAccess access, UUID compositionId, String content) throws Exception {
        return update(access, compositionId, content, null, null, null);
    }

    @Override
    public Boolean update(I_DomainAccess access, UUID compositionId, String content, UUID committerId, UUID systemId, String description) throws Exception {
        boolean changed = false;
        boolean changedContext = false;

        UUID contextId = compositionAccess.getContextId();
        Timestamp updateTransactionTime = new Timestamp(DateTime.now().getMillis());

        I_ContextAccess contextAccess;

        if (compositionAccess.getContextId() == null){
            EventContext context = ContextHelper.createNullContext();
            contextAccess = I_ContextAccess.getInstance(domainAccess, context);
            contextAccess.commit(updateTransactionTime);
        }
        else
            contextAccess = I_ContextAccess.retrieveInstance(domainAccess, contextId);

        for (I_EntryAccess entryAccess: compositionAccess.getContent()) {
            //set the template Id
            templateId = entryAccess.getTemplateId();
            Composition newComposition = build(content);
            entryAccess.setCompositionData(newComposition.getArchetypeDetails().getTemplateId().getValue(), newComposition);
            compositionAccess.updateCompositionData(newComposition);
            changed = true;
            EventContext eventContext = newComposition.getContext();
            contextAccess.setRecordFields(contextId, eventContext);
            changedContext = true;
        }

        if (changedContext)
            contextAccess.update(updateTransactionTime);

        if (changed) {
            return compositionAccess.update(updateTransactionTime, committerId, systemId, null, I_ConceptAccess.ContributionChangeType.modification, description, true);
        }

        return true; //nothing to do...
    }

    @Override
    public Map<String, Integer> getItemArrayPathMap() {
        return itemArrayPathMap;
    }
}
