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
package com.ethercis.ehr.building;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import openEHR.v1.template.TEMPLATE;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.FlattenerNew;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;

import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/3/2015.
 */
public class OetContentBuilder extends ContentBuilder {

    TEMPLATE template = null;

    public OetContentBuilder(Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        super(values, knowledge, templateId);
    }

    public OetContentBuilder(TEMPLATE template, Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        super(values, knowledge, templateId);
        this.template = template;
        log.warn("USING openEHR TEMPLATE for templateId:"+templateId);
    }


    @Override
    public Composition generateNewComposition() throws Exception {

        if (knowledge.cacheContainsLocatable(templateId)){
            constraintMapper = retrieveConstraintMapper(templateId);
            Composition composition = retrieveCache(templateId);
            setCompositionAttributes(composition);
            return composition;
        }

        //create a new composition
        if (template == null)
            template = knowledge.retrieveOpenehrTemplate(this.templateId);

        if (template == null)
            throw new IllegalArgumentException("Could not retrieve operational template:"+templateId);

        FlattenerNew flattener = new FlattenerNew();
        Archetype instance = flattener.toFlattenedArchetype(template, knowledge.getArchetypeMap());
        OetBinding generator = (values == null ? I_RmBinding.getInstance() : I_RmBinding.getInstance(values));
        Composition newComposition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM_EMPTY);

        if (newComposition == null)
            throw new IllegalArgumentException("Could not generate composition:"+templateId);

        if (knowledge.isLocatableCached()) {
            storeCache(templateId, newComposition, constraintMapper);
        }

        return newComposition;
    }

    @Override
    public Locatable generate() throws Exception {
        throw new IllegalArgumentException("Not implemented");
    }

}
