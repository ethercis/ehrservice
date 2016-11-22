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
//Copyright
package com.ethercis.dao.access.handler;

import com.ethercis.dao.access.interfaces.I_CompositionAccess;
import com.ethercis.dao.access.interfaces.I_ConceptAccess;
import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_EntryAccess;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.joda.time.DateTime;
import org.openehr.rm.composition.Composition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * ETHERCIS Project VirtualEhr
 * Created by Christian Chevalley on 9/18/2015.
 */
public class CanonicalHandler implements I_CanonicalHandler, I_CompositionMetaData {

    I_ContentBuilder contentBuilder;
    private I_KnowledgeCache knowledgeCache;
    private String templateId;
    private I_DomainAccess domainAccess;

    public CanonicalHandler(I_DomainAccess domainAccess, String templateId) throws Exception {
        this.knowledgeCache = domainAccess.getKnowledgeManager();
        this.templateId = templateId;
        contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, this.knowledgeCache, "IMPLICIT");
        this.domainAccess = domainAccess;
    }

    @Override
    public Composition build(String content) throws Exception {
//        String template = defaultedTemplateId(templateId);
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        try {
            return contentBuilder.importCanonicalXML(inputStream);
        } catch (Exception e){
            throw new IllegalArgumentException("Could not parse supplied content:"+e);
        }
    }

    @Override
    public UUID storeComposition(UUID ehrId, String xmlContent, UUID committerId, UUID systemId, String description) throws Exception {
        Composition composition = build(xmlContent);
        templateId = composition.getArchetypeDetails().getTemplateId().getValue();
        I_CompositionAccess compositionAccess = I_CompositionAccess.getNewInstance(domainAccess, composition, DateTime.now(), ehrId);
        I_EntryAccess entryAccess = I_EntryAccess.getNewInstance(domainAccess, templateId, 0, compositionAccess.getId(), composition);
        compositionAccess.addContent(entryAccess);
        UUID compositionId = compositionAccess.commit(committerId, systemId, description);
        return compositionId;
    }

    @Override
    public Boolean update(I_DomainAccess access, UUID compositionId, String content) throws Exception {
        return update(access, compositionId, content, null, null, null);
    }

    @Override
    public Boolean update(I_DomainAccess access, UUID compositionId, String content, UUID committerId, UUID systemId, String description) throws Exception {
        Composition composition = build(content);
        I_CompositionAccess compositionAccess = I_CompositionAccess.retrieveInstance(access, compositionId);
        List<I_EntryAccess> contentList = new ArrayList<>();
        contentList.add(I_EntryAccess.getNewInstance(access, templateId, 0, compositionAccess.getId(), composition));
        compositionAccess.setContent(contentList);
        return compositionAccess.update(committerId, systemId, null, I_ConceptAccess.ContributionChangeType.modification, description);
    }

    @Override
    public Map<String, Integer> getItemArrayPathMap(){
        return contentBuilder.getArrayItemPathMap();
    }
}
