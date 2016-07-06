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
package com.ethercis.dao.access.interfaces;

import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.CONCEPT;

/**
 * access layer to Concepts
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 4/27/2015.
 */
public interface I_ConceptAccess {

    enum ContributionChangeType {
        creation(249),amendment(250), modification(251), synthesis(252), Unknown(253), deleted(523);
        int code;

        ContributionChangeType(int code){
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }

    /**
     * retrieve a concept
     * @param domainAccess SQL context
     * @param conceptId integer code
     * @param language language code ('en', 'fr' etc.)
     * @return the record UUID or null if not found
     */
    public static UUID fetchConcept(I_DomainAccess domainAccess, Integer conceptId, String language){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(conceptId).and(CONCEPT.LANGUAGE.equal(language))).getId();
    }

    public static String fetchConceptLitteral(I_DomainAccess domainAccess, Integer conceptId, String language){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(conceptId).and(CONCEPT.LANGUAGE.equal(language))).getDescription();
    }

    public static String fetchConceptLitteral(I_DomainAccess domainAccess, UUID uuid){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.ID.eq(uuid)).getDescription();
    }
    /**
     * retrieve a concept
     * @param domainAccess SQL context
     * @return the record UUID or null if not found
     */
    static UUID fetchContributionChangeType(I_DomainAccess domainAccess, String changeTypeStr){
        ContributionChangeType contributionChangeType = ContributionChangeType.valueOf(changeTypeStr);
        if (contributionChangeType == null)
            return null;
        int code = contributionChangeType.getCode();
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(code).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }

    static UUID fetchContributionChangeType(I_DomainAccess domainAccess, ContributionChangeType contributionChangeType){
        if (contributionChangeType == null)
            return null;
        int code = contributionChangeType.getCode();
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(code).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }

    /**
     * convenience statics to get VERSION.lifecycle_state
     * DRAFT (244)
     * @param domainAccess
     * @return
     */
    public static UUID getVlcsDraft(I_DomainAccess domainAccess){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(244).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }

    /**
     * convenience statics to get VERSION.lifecycle_state
     * ACTIVE (code: 245)
     * @param domainAccess
     * @return
     */
    public static UUID getVlcsActive(I_DomainAccess domainAccess){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(245).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }

    /**
     * convenience statics to get VERSION.lifecycle_state
     * INACTIVE (code: 246)
     * @param domainAccess
     * @return
     */
    public static UUID getVlcsInactive(I_DomainAccess domainAccess){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(246).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }

    /**
     * convenience statics to get VERSION.lifecycle_state
     * AWAITING APPROVAL (code: 247)
     * @param domainAccess
     * @return
     */
    public static UUID getVlcsAwaitingApproval(I_DomainAccess domainAccess){
        return domainAccess.getContext().fetchOne(CONCEPT, CONCEPT.CONCEPTID.eq(247).and(CONCEPT.LANGUAGE.equal("en"))).getId();
    }
}
