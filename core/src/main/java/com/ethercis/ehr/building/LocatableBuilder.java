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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.template.Flattener;
import org.openehr.rm.common.archetyped.Locatable;

/**
 * create a locatable from a CKM
 * Created by Christian Chevalley on 8/8/2014.
 */
public class LocatableBuilder {
    private Logger log = LogManager.getLogger(LocatableBuilder.class);
//    private I_ResourceService resourceService;
    private I_KnowledgeCache knowledgeManager;

    public LocatableBuilder(I_KnowledgeCache knowledge){
        this.knowledgeManager = knowledge;
    }

    /**
     * create a Locatable instance based on an existing template
     * @param templateId
     * @return
     * @throws Exception
     */
    public Locatable createOetInstance(String templateId, GenerationStrategy strategy) throws Exception {
        Flattener flattener = new Flattener();
//        I_KnowledgeManager knowledgeManager = resourceService.getKnowledgeManager();

        TEMPLATE template = knowledgeManager.retrieveOpenehrTemplate(templateId);

        if (template == null)
            throw new IllegalArgumentException("Could not resolve template:"+templateId);

        Archetype instance = flattener.toFlattenedArchetype(template, knowledgeManager.getArchetypeMap());

        //try to build an actual COMPOSITION from the instance...
        OetBinding generator = I_RmBinding.getInstance();
        Object loc = generator.create(instance, templateId, knowledgeManager.getArchetypeMap(), strategy);

        if (loc instanceof Locatable)
            return (Locatable)loc;

        log.error("Could not generate a valid Locatable for template id:"+templateId);
        return null;
    }

}
