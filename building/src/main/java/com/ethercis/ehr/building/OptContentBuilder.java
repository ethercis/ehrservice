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
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.util.Map;

/**
 * Build contents from Operational Templates
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/3/2015.
 */
public class OptContentBuilder extends ContentBuilder {

    OPERATIONALTEMPLATE operationaltemplate;

    public OptContentBuilder(Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        super(values, knowledge, templateId);
    }

    public OptContentBuilder(OPERATIONALTEMPLATE operationaltemplate, Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        super(values, knowledge, templateId);
        this.operationaltemplate = operationaltemplate;
    }


    @Override
    public Composition generateNewComposition() throws Exception {

        if (knowledge.cacheContainsLocatable(templateId)){
            Composition composition =  retrieveCache(templateId);
            //TODO: assign initial composition attributes if any
            constraintMapper = retrieveConstraintMapper(templateId);
            setCompositionAttributes(composition);
            return composition;
        }

        if (operationaltemplate == null) {
            Object template = knowledge.retrieveTemplate(templateId);
            if (template instanceof OPERATIONALTEMPLATE)
                operationaltemplate = (OPERATIONALTEMPLATE)template;
            else
                throw new IllegalArgumentException("Cached template for id:"+templateId+", is not an operational template");
        }

        if (operationaltemplate == null)
            throw new IllegalArgumentException("Could not retrieve operational template:"+templateId);

        OptBinding optBinding = (values == null ? new OptBinding() : new OptBinding(values));
        Object locatable = optBinding.generate(operationaltemplate);

        constraintMapper = optBinding.getConstraintMapper();
        constraintMapper.setLenient(lenient);

        if (locatable instanceof Composition) {
            if (knowledge.isLocatableCached()) {
                storeCache(templateId, (Composition)locatable, constraintMapper);
            }
            return (Composition) locatable;
        }
        else
            throw new IllegalArgumentException("Retrieved object from template is not a valid composition (template id:"+templateId+")");

    }

    @Override
    public Locatable generate() throws Exception {

        if (knowledge.cacheContainsLocatable(templateId)){
            constraintMapper = retrieveConstraintMapper(templateId);
            Composition composition = retrieveCache(templateId);
            setCompositionAttributes(composition);
            return composition;
        }

        if (operationaltemplate == null) {
            Object template = knowledge.retrieveTemplate(templateId);
            if (template instanceof OPERATIONALTEMPLATE)
                operationaltemplate = (OPERATIONALTEMPLATE)template;
            else
                throw new IllegalArgumentException("Cached template for id:"+templateId+", is not an operational template");
        }

        if (operationaltemplate == null)
            throw new IllegalArgumentException("Could not retrieve operational template:"+templateId);

        OptBinding optBinding = (values == null ? new OptBinding() : new OptBinding(values));
        Object generated =  optBinding.generate(operationaltemplate);

        if (!(generated instanceof Locatable))
            throw new IllegalArgumentException("Generated object is not a Locatable");

        return (Locatable)generated;


    }

}
