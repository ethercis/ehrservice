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
package com.ethercis.ehr.handler;

import org.openehr.rm.common.archetyped.Locatable;

import java.util.List;
import java.util.Map;

/**
 * Created by Christian Chevalley on 8/19/2014.
 */
public interface I_ContributionManager {
    /**
     * get the list of available contribution types for a given user, com.ethercis.ehr (e.g. patient) and within a program
     * <p>
     *     the contribution type can be seen as a collection of compositions relevant for a given act. For example,
     *     a screening visit may involve a number of examinations supported by different compositions (or forms)
     * </p>
     * @param userid the user to get possible contributions for
     * @param ehrid indirectly the patient, provides a list of already (optional)
     * @param programs a list of programs
     * @return a list of types
     */
    List<String> getContributionTypes(String userid, String ehrid, List<String> programs);

    /**
     * start a new contribution by a contributor (user) for a given patient (ehrid), with the given type (optional)
     * @param userid the contributor or committer
     * @param ehrid the target recipient
     * @param description descriptive literal
     * @return a token id for the contribution (e.g. transaction)
     * @throws Exception
     */
    String startContribution(String userid, String ehrid, String description) throws Exception;

    /**
     * returns the list of composition names valid for a contribution
     * @param contributionId the ref id of the contribution
     * @return a list of composition ids (template ids)
     * @throws Exception
     */
    List<String> getCompositionList(String contributionId) throws Exception;

    /**
     * create a new composition
     * <p>
     *     the composition created is set with the maximum number of possible fields,
     *     all fields are set to their default value
     * </p>
     * <p>
     *     NB: the Locatable created contains wrapped element with constraints!
     * </p>
     * @see com.ethercis.ehr.encode.wrappers.ElementWrapper
     * @param contributionId the ref id of the contribution
     * @param compositionName the name for composition (template id)
     * @return an empty Locatable for edition
     */
    Locatable newComposition(String contributionId, String compositionName);

    /**
     * submit a composition for persistence
     * @param contributionId the current contribution
     * @param composition a Locatable holding the composition
     * @return a composition id
     * @throws Exception if fail
     */
    String submitComposition(String contributionId, Locatable composition) throws Exception;

    /**
     * delete a composition from the contribution
     * @param contributionId the current contribution
     * @param compositionId a composition Id
     * @throws Exception if fail
     */
    void cancelComposition(String contributionId, String compositionId) throws Exception;

    /**
     * commit a contribution for persistence in the backend storage
     * @param contributionId the current contribution
     * @throws Exception if fail
     */
    void commitContribution(String contributionId) throws Exception;

    /**
     * abort a contribution
     * @param contributionId the current contribution
     * @throws Exception if fail
     */
    void abortContribution(String contributionId) throws Exception;

    /**
     * get the list of contributions for a given com.ethercis.ehr and optionally for a given program
     * @param ehrId the com.ethercis.ehr to search contributions for
     * @param programs one or more programs to filter out the search
     * @return a list of key-value pairs of string containing:
     *
     *     <ul>
     *         <li>contribution Id: unique identifier for a contribution</li>
     *         <li>description: a descriptive literal</li>
     *         <li>committer: the user committing the contribution</li>
     *         <li>system: the origin of the contribution</li>
     *         <li>date: the date and time of contribution</li>
     *         <li>version: the version of the contribution</li>
     *         <li>status: the status of the contribution (original, updated)</li>
     *     </ul>
     *
     * @throws Exception
     */
    List<Map<String, String>> getContributionList(String ehrId, List<String> programs) throws Exception;

    /**
     * returns the list of existing compositions for an existing contribution
     * @param contributionId the id of a contribution
     * @return a list of key-value pairs consisting of:
     *
     *     <ul>
     *         <li>composition Id: unique identifier for a composition</li>
     *         <li>description: a descriptive literal</li>
     *         <li>composer: the user committing the composition</li>
     *         <li>context: the context of the composition (facility, location, program)</li>
     *         <li>date: the date and time of composition</li>
     *         <li>language: the language originally used for the composition</li>
     *         <li>version: the version of the composition</li>
     *         <li>status: the status of the composition (original, updated)</li>
     *     </ul>
     *
     * @throws Exception
     */
    List<Map<String, String>> getCompositions(String contributionId) throws Exception;

    /**
     * get an existing composition for review or edition
     * @param compositionId the id of the composition
     * @return a Locatable containing the composition
     * @throws Exception
     */
    Locatable selectComposition(String compositionId) throws Exception;

    /**
     * update a contribution by a contributor. The method performs the initial process to version the contribution
     * for updating (as nothing is deleted as per openEHR requirement)
     * @param contributionId the id of a contribution
     * @param userid the contributor or committer
     * @return a token id for the contribution (e.g. transaction)
     * @throws Exception
     */
    String updateContribution(String contributionId, String userid) throws Exception;
}
