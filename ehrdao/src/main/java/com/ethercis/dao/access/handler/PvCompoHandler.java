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
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.ContentHelper;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.keyvalues.PathValue;
import org.joda.time.DateTime;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class PvCompoHandler extends PathValue implements I_CompositionMetaData {

    private I_CompositionAccess compositionAccess;
    private final I_DomainAccess domainAccess;

    public PvCompoHandler(I_DomainAccess domainAccess, I_CompositionAccess compositionAccess, String templateId, Properties properties) {
        super(domainAccess.getKnowledgeManager(), templateId, properties);
        this.compositionAccess = compositionAccess;
        this.domainAccess = domainAccess;
    }

    public PvCompoHandler(I_DomainAccess domainAccess, String templateId, Properties properties) {
        super(domainAccess.getKnowledgeManager(), templateId, properties);
        this.compositionAccess = null;
        this.domainAccess = domainAccess;
    }

    public UUID storeComposition(UUID ehrId, Map<String, Object> keyValues) throws Exception {
        return storeComposition(ehrId, keyValues, null, null, null);
    }

    public UUID storeComposition(UUID ehrId, Map<String, Object> keyValues, UUID committerId, UUID systemId, String description) throws Exception {

        Composition composition = assign(keyValues);

        compositionAccess = I_CompositionAccess.getNewInstance(domainAccess, composition, DateTime.now(), ehrId);

        if (composition.getContent() != null){
            I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(domainAccess, templateId, 0, compositionAccess.getId(), composition);
            compositionAccess.addContent(entryAccess);
        }

        return compositionAccess.commit(committerId, systemId, description);
    }

    public Boolean updateComposition(Map<String, Object> keyValues, UUID committerId, UUID systemId, String description) throws Exception {

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

//        EventContext eventContext = contextAccess.mapRmEventContext();

       if (updateCompositionAttributes(keyValues))
            changed = true;

        for (I_EntryAccess entryAccess: compositionAccess.getContent()) {
//            templateId = entryAccess.getTemplateId();
//            Composition newComposition = build(global, content);
            Composition composition = entryAccess.getComposition();
            if (contentBuilder == null){
                contentBuilder = I_ContentBuilder.getInstance(knowledge, entryAccess.getTemplateId());
            }
            new ContentHelper().invalidateContent(composition);
            if (assignItemStructure(CONTENT_TAG, composition, keyValues)) {
                changed = true;
                entryAccess.setCompositionData(entryAccess.getTemplateId(), composition);
            }

            I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, domainAccess.getKnowledgeManager(), entryAccess.getTemplateId());
            contentBuilder.bindOtherContextFromJson(composition, contextAccess.getOtherContextJson());
            EventContext eventContext = composition.getContext();

            if  (updateEventContext(eventContext, keyValues)) {
                contextAccess.setRecordFields(contextId, eventContext);
                changedContext = true;
            }
        }

        if (changedContext)
            contextAccess.update(updateTransactionTime);

        if (changed)
            return compositionAccess.update(updateTransactionTime, committerId, systemId, null, I_ConceptAccess.ContributionChangeType.modification, description);

        return true; //nothing to do...
    }

    private boolean updateCompositionAttributes(Map<String, Object> keyValues) throws Exception {
        boolean modified = false;

        for (String path : keyValues.keySet()) {
            String value = (String)keyValues.get(path);

            if (LANGUAGE_TAG.equals(path)) {
                compositionAccess.setLanguageCode(parseLanguageAttribute(value).getCodeString());
                log.debug("Updated language");
                modified = true;
            }
//            else if (UID_TAG.equals(path)) {
//                compositionAccess.setUid(parseUIDAttribute(value));
//                modified = true;
//            }
            else if (TERRITORY_TAG.equals(path)) {
                String territoryCodeString = parseTerritoryAttribute(value).getCodeString();
                Integer territoryCode = I_CompositionAccess.fetchTerritoryCode(domainAccess, territoryCodeString);
                compositionAccess.setTerritoryCode(territoryCode);
                log.debug("Updated territory");
                modified = true;
            }
            else if (path.matches(COMPOSER_REGEXP)) {
                //get the actual value to work with
                value = keyValues.get(COMPOSER_TAG + IDENTIFIER_PARTY_ID_SUBTAG) + "::" + keyValues.get(COMPOSER_TAG + IDENTIFIER_PARTY_NAME_SUBTAG);
                PartyIdentified composer = parseComposerAttribute(value);
                UUID composerId = I_PartyIdentifiedAccess.getOrCreateParty(domainAccess, composer);
                compositionAccess.setComposerId(composerId);
                log.debug("Updated composer");

                modified = true;
            }
        }
        return modified;
    }


    @Override
    public Map<String, Integer> getItemArrayPathMap() {
        return contentBuilder.getArrayItemPathMap();
    }
}
