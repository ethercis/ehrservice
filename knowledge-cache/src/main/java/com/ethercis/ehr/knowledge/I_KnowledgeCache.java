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
package com.ethercis.ehr.knowledge;

import com.ethercis.validation.ConstraintMapper;
import openEHR.v1.template.TEMPLATE;
import org.openehr.am.archetype.Archetype;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public interface I_KnowledgeCache {

    String TEMPLATE_ID = "templateId";
    String ERROR = "ERROR";
    String CREATED_ON = "createdOn";
    String LAST_ACCESS_TIME = "lastAccessTime";
    String LAST_MODIFIED_TIME = "lastModifiedTime";
    String PATH = "path";
    String TEMPLATES = "templates";
    String UID = "uid";
    String CONCEPT = "concept";

    String getArchetypePath();

    String getOptPath();

    String getTemplatePath();

    /**
     * retrieve the file for an archetype
     * @param key the name of the archetype
     * @return a file handler or null
     */
	public File retrieveArchetypeFile(String key);

    /**
     * retrieve the file for a template
     * @param key template name
     * @return a file handler or null
     */
    public File retrieveTemplateFile(String key);

    /**
     * retrieve a file associated to an operational template
     * @param key an OPT ID
     * @return a file handler or null
     */
    public File retrieveOPTFile(String key);


    String addOperationalTemplate(byte[] content) throws Exception;

    Map<String, Collection<Map<String, String>>> listOperationalTemplates() throws IOException;

    /**
     * retrieve a file associated to a knowledge type
     * @param key a resource id
     * @return a file handler or null
     */
    File retrieveFile(String key, KnowledgeCache.KnowledgeType what);

    /**
     * return a map of identifier and File for a defined search pattern.
     * Includes and excludes are regular expression used to refine searches
     * @param includes a regexp for files to include in search, null if all files must be added
     * @param excludes a regexp for files to exclude from search, null if all files must be opted out
     * @return a Map of files corresponding to the specified filters
     */
	public Map<String, File> retrieveFileMap(Pattern includes, Pattern excludes);

    /**
     * retrieve the cached archetype
     * @param key archetype name
     * @return an Archetype instance or null
     * @throws Exception
     * @see org.openehr.am.archetype.Archetype
     */
	public Archetype retrieveArchetype(String key) throws Exception;

    Object retrieveTemplate(String templateId);

    /**
     * retrieve a template associated to a key
     * @param key a template name
     * @return a TEMPLATE document instance or null
     * @throws Exception
     * @see openEHR.v1.template.TEMPLATE
     */
	public TEMPLATE retrieveOpenehrTemplate(String key) throws Exception;

    /**
     * retrieve an operational template document instance
     * @param key the name of the operational template
     * @return an OPERATIONALTEMPLATE document instance or null
     * @throws Exception
     * @see org.openehr.schemas.v1.OPERATIONALTEMPLATE
     */
	public OPERATIONALTEMPLATE retrieveOperationalTemplate(String key) throws Exception;

    /**
     * retrieve a <b>cached</b> operational template document instance using its unique Id
     * @param uuid the name of the operational template
     * @return an OPERATIONALTEMPLATE document instance or null
     * @throws Exception
     * @see org.openehr.schemas.v1.OPERATIONALTEMPLATE
     */
    public OPERATIONALTEMPLATE retrieveOperationalTemplate(UUID uuid) throws Exception;

    /**
     * retrieve a <b>cached</b> template document instance using its unique Id
     * @param uuid the name of the operational template
     * @return a TEMPLATE document instance or null
     * @throws Exception
     * @see openEHR.v1.template.TEMPLATE
     */
    TEMPLATE retrieveTemplate(UUID uuid) throws Exception;

    /**
     * retrieve the list of archetype files corresponding to the include/exclude filter
     * @param includes a regexp for files to include in search, null if all files must be added
     * @param excludes a regexp for files to exclude from search, null if all files must be opted out
     * @return
     * @throws Exception
     */
	public List<Archetype> retrieveArchetypeList(Pattern includes, Pattern excludes)
			throws Exception;

    /**
     * add explicitely a resource path for a knowledge resource type
     * @param path the path to use
     * @param type the knowledge type
     * @return true if completed successfully
     * @throws Exception
     */
	public boolean addKnowledgeSourcePath(String path, KnowledgeCache.KnowledgeType type) throws Exception;

    Object retrieveGenerated(String name);
    ConstraintMapper retrieveCachedConstraints(String name);

    void cacheGenerated(String name, Object objectOutput, ConstraintMapper constraintMapper);

    Boolean isLocatableCached();

    Boolean cacheContainsLocatable(String name);

    void invalidateCache(String templateId);

    void invalidateCache();

    public Map<String, Archetype> getArchetypeMap();

    Map<String, String> getOperationalTemplateMap();

    String archeypesList();

    String oetList();

    String optList();

    String statistics();

    boolean isForceCache();

    void setForceCache(boolean forceCache);

    String settings();

    String processingErrors();
}
