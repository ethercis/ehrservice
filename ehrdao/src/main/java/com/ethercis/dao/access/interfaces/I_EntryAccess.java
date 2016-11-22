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

import com.ethercis.dao.access.jooq.EntryAccess;
import org.jooq.Result;
import org.openehr.rm.composition.Composition;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.ENTRY;

/**
 * Entry (Composition Content) access layer
 * Created by Christian Chevalley on 4/21/2015.
 */
public interface I_EntryAccess extends I_SimpleCRUD<I_EntryAccess, UUID> {

    /**
     * get a new Entry
     * @param domain SQL context
     * @param templateId the template Id for the composition
     * @param sequence a sequence number (Integer) if applicable
     * @param compositionId the composition entry owning this content
     * @param composition the actual composition to store
     * @return an access layer instance
     * @throws Exception
     * @see org.openehr.rm.composition.Composition
     */
    public static I_EntryAccess getNewInstance(I_DomainAccess domain, String templateId, Integer sequence, UUID compositionId, Composition composition) throws Exception {
        return new EntryAccess(domain.getContext(), domain.getKnowledgeManager(), templateId, sequence, compositionId, composition);
    }

    /**
     * retrieve an Entry instance
     * @param domainAccess SQL context
     * @param entryId the UUID for the entry to retrieve
     * @return an access layer instance
     * @throws Exception
     */
    public static I_EntryAccess retrieveInstance(I_DomainAccess domainAccess, UUID entryId) throws Exception {
        return EntryAccess.retrieveInstance(domainAccess, entryId);
    }

    /**
     * retrieve the list of entries for a composition
     * @param domainAccess SQL context
     * @param compositionAccess a composition access interface instance
     * @return a list of I_EntryAccess
     * @throws Exception
     */
    public static List<I_EntryAccess> retrieveInstanceInComposition(I_DomainAccess domainAccess, I_CompositionAccess compositionAccess) throws Exception {
        return EntryAccess.retrieveInstanceInComposition(domainAccess, compositionAccess);
    }

    public static List<I_EntryAccess> retrieveInstanceInComposition(I_DomainAccess domainAccess, Result<?> records) throws Exception {
        return EntryAccess.retrieveInstanceInComposition(domainAccess, records);
    }

    public static List<I_EntryAccess> retrieveInstanceInCompositionVersion(I_DomainAccess domainAccess, I_CompositionAccess compositionHistoryAccess, int version) throws Exception {
        return EntryAccess.retrieveInstanceInCompositionVersion(domainAccess, compositionHistoryAccess, version);
    }

    public static I_EntryAccess EntryAccess(I_DomainAccess domain){
        return new EntryAccess(domain.getContext(), domain.getKnowledgeManager());
    }

    /**
     * get the actual composition held in this entry
     * @return Composition
     * @see org.openehr.rm.composition.Composition
     */
    Composition getComposition();

    /**
     * get the entry Id
     * @return
     */
    UUID getId();

    /**
     * get the entry values as a JSON string
     * @return
     */
    String getEntryJson();

    /**
     * get the entry category record id<br>
     * Category entry is a concept identified by a code and language in
     * the concept table. The concept is always identified for the English
     * language ('en')
     * @return UUID of category concept
     */
    UUID getCategory();

    /**
     * get the composition Id owning this entry
     * @return
     */
    UUID getCompositionId();

    /**
     * get the template Id (a string) used to build the composition entry
     * @return
     */
    String getTemplateId();

    /**
     * get the sequence number if applicable
     * @return
     */
    Integer getSequence();

    /**
     * Get the root archetype to build the composition
     * @return
     */

    String getArchetypeId();

    /**
     * get the Item Type as a literal<br>
     * Item type is one of
     * <ul>
     *     <li>section</li>
     *     <li>care_entry</li>
     *     <li>admin</li>
     * </ul>
     * @return
     */
    String getItemType();

    /**
     * set the composition data with an actual Composition
     * @param templateId the template id used to build the composition
     * @param composition Composition
     * @throws Exception
     * @see org.openehr.rm.composition.Composition
     */
    void setCompositionData(String templateId, Composition composition) throws Exception;

    /**
     * set the owner composition by its Id
     * @param compositionId UUID
     */
    void setCompositionId(UUID compositionId);

    /**
     * set the template id to build the composition
     * @param templateId a string
     */
    void setTemplateId(String templateId);

    /**
     * set the sequence number of this entry
     * @param sequence
     */
    void setSequence(Integer sequence);

    /**
     * delete all entries belonging to a composition
     * @param domainAccess SQL access
     * @param compositionId a composition id
     * @return count of deleted
     */
    public static Integer deleteFromComposition(I_DomainAccess domainAccess, UUID compositionId){
        return domainAccess.getContext().delete(ENTRY).where(ENTRY.COMPOSITION_ID.eq(compositionId)).execute();
    }

    /**
     * delete an entry
     * @param domainAccess SQL access
     * @param id UUID of entry to delete
     * @return count of deleted
     */
    public static Integer delete(I_DomainAccess domainAccess, UUID id){
        return domainAccess.getContext().delete(ENTRY).where(ENTRY.ID.eq(id)).execute();
    }

    /**
     * perform an arbitrary SQL query on entries and return the result set as a JSON string
     * @param domainAccess SQL access
     * @param query a valid SQL queryJSON string
     * @return a JSON formatted result set
     * @throws Exception
     */
    public static Map<String, Object> queryJSON(I_DomainAccess domainAccess, String query) throws Exception {
        return EntryAccess.queryJSON(domainAccess, query);
    }

    /**
     * perform an arbitrary AQL query on entries and return the result set as a JSON string
     * @param domainAccess SQL access
     * @param query a valid SQL queryJSON string
     * @return a JSON formatted result set
     * @throws Exception
     */
    public static Map<String, Object> queryAqlJson(I_DomainAccess domainAccess, String query) throws Exception {
        return EntryAccess.queryAqlJson(domainAccess, query, false);
    }

    public static Map<String, Object> explainAqlJson(I_DomainAccess domainAccess, String query) throws Exception {
        return EntryAccess.queryAqlJson(domainAccess, query, true);
    }
}
