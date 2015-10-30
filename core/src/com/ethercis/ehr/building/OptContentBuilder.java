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
import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.util.Map;

/**
 * Build contents from Operational Templates
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/3/2015.
 */
public class OptContentBuilder extends ContentBuilder {

    public OptContentBuilder(Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        super(values, knowledge, templateId);
    }

    @Override
    public Composition generateNewComposition() throws Exception {

        if (knowledge.cacheContainsLocatable(templateId)){
            return retrieveCache(templateId);
        }

        OPERATIONALTEMPLATE operationaltemplate = knowledge.retrieveOperationalTemplate(templateId);
        if (operationaltemplate == null)
            throw new IllegalArgumentException("Could not retrieve operational template:"+templateId);

        OptBinding optBinding = (values == null ? new OptBinding() : new OptBinding(values));
        Object locatable = optBinding.generate(operationaltemplate);

        if (locatable instanceof Composition) {
            if (knowledge.isLocatableCached()) {
                storeCache(templateId, (Composition)locatable);
            }
            return (Composition) locatable;
        }
        else
            throw new IllegalArgumentException("Retrieved object from template is not a valid composition (template id:"+templateId+")");

    }

}
