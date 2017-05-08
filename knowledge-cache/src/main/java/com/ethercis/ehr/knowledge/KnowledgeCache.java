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
import openEHR.v1.template.TemplateDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimePrinter;
import org.openehr.am.archetype.Archetype;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import se.acode.openehr.parser.ADLParser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


/**
 * Look up and caching for archetypes, openEHR showTemplates and Operational Templates. Search in path defined as
 * <ul>
 * <li> 1. System environment ETHERCIS_ARCHETYPE_DIR, ETHERCIS_TEMPLATE_DIR, ETHERCIS_OPT_DIR</li>
 * <li> 2. Application path %USER_HOME%/.ethercis/archetype, %USER_HOME%/.ethercis/template, %USER_HOME%/.ethercis/opt</li>
 * <li> 3. User can also include a source directory by invoking addXYZPath method</li>
 * </ul>
 *
 * <p>
 *     The resources extensions are defined by the following default:
 *     <ul>
 *     <li>ADL: archetype</li>
 *     <li>OET: openehr template</li>
 *     <li>OPT: operational template</li>
 *     </ul>
 * </p>
 * 
 * @author C. Chevalley
 * 
 */
public class KnowledgeCache implements I_KnowledgeCache {

    public static final String KNOWLEDGE_FORCECACHE = "knowledge.forcecache";
    public static final String KNOWLEDGE_CACHELOCATABLE = "knowledge.cachelocatable";
    public static final String KNOWLEDGE_PATH_ARCHETYPE = "knowledge.path.archetype";
    public static final String KNOWLEDGE_PATH_TEMPLATE = "knowledge.path.template";
    public static final String KNOWLEDGE_PATH_OPT = "knowledge.path.opt";
    public static final String KNOWLEDGE_PATH_BACKUP = "knowledge.path.backup";
    private Map<String, File> archetypeFileMap = new HashMap<String, File>();
	private Map<String, Archetype> atArchetypeCache = new HashMap<String, Archetype>();
	private Map<String, File> templatesFileMap = new HashMap<String, File>();

    //Cache
	private Map<String, TEMPLATE> atTemplatesCache = new HashMap<String, TEMPLATE>();
	private Map<String, File> optFileMap = new HashMap<String, File>();
	private Map<String, OPERATIONALTEMPLATE> atOptCache = new HashMap<>();

    //Cache a serialized object with the corresponding constraint map
    private class CacheSerialized {
        Object serialized;
        ConstraintMapper constraintMapper;

        public CacheSerialized(Object serialized, ConstraintMapper constraintMapper) {
            this.serialized = serialized;
            this.constraintMapper = constraintMapper;
        }

        public Object getSerialized() {
            return serialized;
        }

        public ConstraintMapper getConstraintMapper() {
            return constraintMapper;
        }
    }

    private Map<String, CacheSerialized> cacheSerialized = new HashMap<>();

    //index
    //template index with UUID (not used so far...)
    private Map<UUID, String> idxCache = new HashMap<>();

    //global template index by template_id, points to map entries!
    private Map<String, Object> globalIndex = new HashMap<>();


    //processing error (for JMX)
    private Map<String, String> errorMap = new HashMap<>();

    //stats for JMX
    boolean used_environment = false;

    //true if parsing and caching must be done at startup time
    boolean forceCache = false;

    //true: cache all generated locatable for optimization
    boolean cacheLocatable = false;

    //used by JMX to show the settings
    private final String NULL_PATH = "";

    private String archetypePath;
    private String templatePath;
    private String optPath;
    private String backupPath = NULL_PATH;


    /**
     * utility class to identify a file extension from a qualifier
     */
    public enum KnowledgeType {
		ARCHETYPE ("ARCHETYPE", "adl"),
		TEMPLATE("TEMPLATE", "oet"),
		OPT("OPT", "opt");
		
		private String qualifier;
		private String extension;
		
		KnowledgeType(String qualifier, String extension){
			this.qualifier = qualifier;
			this.extension = extension;
		}
		
		public String getQualifier() { return qualifier; }
		public String getExtension() { return extension; }
	}
	
	public static String DEFAULT_ENCODING = "UTF-8";

	private static Logger log = LogManager.getLogger(KnowledgeCache.class);

//    private RunTimeSingleton global;

    /**
     * default constructor, use environment variables and/or user directory
     */
	public KnowledgeCache() {

        used_environment = true;
       //perform mapping for defined environment variables
		for (KnowledgeType k: KnowledgeType.values()) {
            String env = "ETHERCIS_" + k.getQualifier() + "_DIR";
            if (System.getenv(env) != null) {
                String systemEnvDir = System.getenv(env);
                try {
                    if (new File(systemEnvDir).exists()) {
                        log.debug("mapping knowledge resource dir:" + systemEnvDir);
                        addKnowledgeSourcePath(systemEnvDir, k);
                    }
                } catch (IOException e) {
                    log.warn("Path not found:" + systemEnvDir);
                } catch (Exception e) {
                    log.warn("Could not map resource path:", e);
                }
            }
        }

        //perform mapping if resources found in user directory
		String localDirPrefix = System.getProperty("user.home")
				+ File.separator + ".ethercis" + File.separator;
		for (KnowledgeType k: KnowledgeType.values()) {
			String dir = localDirPrefix+k.getQualifier().toLowerCase();
			try {
				if (new File(dir).exists()) {
					log.debug("mapping knowledge dir:"+dir);
					addKnowledgeSourcePath(dir, k);
				}
			} catch (Exception e) {
				log.warn("Could not map resource directory:"+dir+ "exception:"+e);
			}
		}

        log.info(settings());
	}

    /**
     * constructor using explicit directory declaration
     * the constructor scans the environment variables and user directory as well.
     * @param path the path to use
     * @param what the type of resource
     */
	public KnowledgeCache(String path, KnowledgeType what) {

		try {
			switch (what) {
			case ARCHETYPE:
				addKnowledgeSourcePath(path, archetypeFileMap, what.getExtension());
				break;
			case TEMPLATE:
				addKnowledgeSourcePath(path, templatesFileMap, what.getExtension());
				break;
			case OPT:
				addKnowledgeSourcePath(path, optFileMap, what.getExtension());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

        log.info(settings());
	}

    /**
     * grab variables from service properties
     * @param props
     * @return
     */
    private boolean setVariablesFromServiceProperties(Properties props){

        if (props == null || props.size() == 0)
            return false;

        for (Entry<Object, Object> entry: props.entrySet()) {
            if ( entry != null && ((String)entry.getKey()).compareTo(KNOWLEDGE_FORCECACHE)==0) {
                forceCache = Boolean.parseBoolean((String)entry.getValue());
                log.info("Force Cache set to:"+forceCache);
            }
            else if ( entry != null && ((String)entry.getKey()).compareTo(KNOWLEDGE_CACHELOCATABLE)==0) {
                //TODO: always false for the time being since it requires deep changes in composition builder
                cacheLocatable = Boolean.parseBoolean((String)entry.getValue());
                log.info("Locatable caching set to:"+cacheLocatable);
            }
            else if ( entry != null && ((String)entry.getKey()).compareTo(KNOWLEDGE_PATH_ARCHETYPE)==0) {

                try {
                    archetypePath = (String) entry.getValue();
                    log.debug("mapping archetype path:"+archetypePath);
                    addKnowledgeSourcePath(archetypePath, KnowledgeType.ARCHETYPE);
                } catch (Exception e) {
                    log.error("Could not map archetype path:"+entry.getValue());
                    throw new IllegalArgumentException("Invalid archetype path:"+entry.getValue());
                }
            }
            else if ( entry != null && ((String)entry.getKey()).compareTo(KNOWLEDGE_PATH_TEMPLATE)==0) {
                try {
                    templatePath = (String)entry.getValue();
                    log.debug("mapping template path:"+templatePath);
                    addKnowledgeSourcePath(templatePath, KnowledgeType.TEMPLATE);
                } catch (Exception e) {
                    log.error("Could not map template path:" + entry.getValue());
                    throw new IllegalArgumentException("Invalid template path:"+entry.getValue());
                }
            }
            else if ( entry != null && ((String)entry.getKey()).compareTo("knowledge.path.opt")==0) {
                try {
                    optPath = (String)(entry.getValue());
                    log.debug("mapping operational template path:"+ optPath);
                    addKnowledgeSourcePath(optPath, KnowledgeType.OPT);
                } catch (Exception e) {
                    log.error("Could not map OPT path:" + entry.getValue());
                    throw new IllegalArgumentException("Invalid OPT path:"+entry.getValue());
                }
            }
            else if ( entry != null && ((String)entry.getKey()).compareTo(KNOWLEDGE_PATH_BACKUP)==0) {
                try {
                    backupPath = (String)(entry.getValue());
                    log.debug("Backup path:"+ backupPath);
                } catch (Exception e) {
                    log.error("Could not map backup path:" + entry.getValue());
                    throw new IllegalArgumentException("Invalid backup path:"+entry.getValue());
                }
            }
        }
        return true;
    }

    /**
     * grab variables from environment settings or services.properties
     * @param properties
     */
    private void setVariablesFromSingleton(Properties properties){

        if (properties == null)
            return;

        forceCache = Boolean.parseBoolean((String)properties.getOrDefault(KNOWLEDGE_FORCECACHE, "false"));
        cacheLocatable = Boolean.parseBoolean((String)properties.getOrDefault(KNOWLEDGE_CACHELOCATABLE, "false"));
        archetypePath = (String)properties.getOrDefault(KNOWLEDGE_PATH_ARCHETYPE, "");
        templatePath = (String)properties.getOrDefault(KNOWLEDGE_PATH_TEMPLATE, "");
        optPath = (String)properties.getOrDefault(KNOWLEDGE_PATH_OPT, "");
        backupPath = (String)properties.getOrDefault(KNOWLEDGE_PATH_BACKUP, "");
    }


	/**
	 * initialize with properties passed from a ServiceInfo context
     * the following are valid service properties:
     * <ul>
     * <li>knowledge.path.archetype</li>
     * <li>knowledge.path.template</li>
     * <li>knowledge.path.opt</li>
     * </ul>
     * @param globals proporties passed into the RuntimeSingleton or other strategy
	 * @param props the service properties
     *
	 */
	public KnowledgeCache(Properties globals, Properties props) throws Exception {

        if (!(setVariablesFromServiceProperties(props)))
            setVariablesFromSingleton(globals);

        try {
            addKnowledgeSourcePath(archetypePath, KnowledgeType.ARCHETYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load archetype path...");
        }

        try {
            addKnowledgeSourcePath(templatePath, KnowledgeType.TEMPLATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load template path...");
        }

        try {
            addKnowledgeSourcePath(optPath, KnowledgeType.OPT);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load operational template path...");
        }

        if (forceCache)
            forceLoadCache();
	}

    /**
     * force caching found resources
     */
    private void forceLoadCache() throws Exception {

        for (String fileName: archetypeFileMap.keySet())
            retrieveArchetype(fileName);

        for (String fileName: templatesFileMap.keySet())
            retrieveOpenehrTemplate(fileName);

        for (String fileName: optFileMap.keySet())
            retrieveOperationalTemplate(fileName);
    }

    @Override
	public File retrieveArchetypeFile(String key) {
		File ret= archetypeFileMap.get(key);
		return ret;
	}

    @Override
    public File retrieveTemplateFile(String key) {
        return templatesFileMap.get(key);
    }

    @Override
    public File retrieveOPTFile(String key) {
        return optFileMap.get(key);
    }

    @Override
    public String addOperationalTemplate(byte[] content) throws Exception {

        InputStream inputStream = new ByteArrayInputStream(content);

        org.openehr.schemas.v1.TemplateDocument document = org.openehr.schemas.v1.TemplateDocument.Factory.parse(inputStream);
        OPERATIONALTEMPLATE template = document.getTemplate();

        if (template == null)
            throw new IllegalArgumentException("Could not parse input template");

        //get the filename from the template template Id
        String filename = template.getTemplateId().getValue();

        //copy the content to filename in OPT path
        Path path = Paths.get(optPath + "/" + filename+".opt");

        //check if this template already exists
        if (Files.exists(path)){
            //create a backup
            if (!backupPath.equals(NULL_PATH)) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");
                Path backupRepository = Paths.get(backupPath + "/" + filename + "_" + formatter.print(DateTime.now()) + ".opt.bak");
                Files.copy(path, backupRepository);
            }
        }

        try {
            Files.write(path, content, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write file:"+filename+" in directory:"+optPath+", reason:"+e);
        }

        //load it in the cache
        optFileMap.put(filename, path.toFile());
        atOptCache.put(filename, template);
        idxCache.put(UUID.fromString(template.getUid().getValue()), filename);

        String templateId = template.getTemplateId().getValue();

        globalIndex.put(templateId, template);

        //invalidate the cache for this template
        invalidateCache(templateId);

        //retrieve the template Id for this new entry
        return template.getTemplateId().getValue();
    }

    @Override
    public Map<String, Collection<Map<String, String>>> listOperationalTemplates() throws IOException {
        Map<String, Collection<Map<String, String>>> mainMap = new HashMap<>();

        Collection<Map<String, String>> collection = new ArrayList<>();

        ZoneId zoneId = ZoneId.systemDefault();

        for (String filename: optFileMap.keySet()){
            Map<String, String> entryMap = new HashMap<>();

            OPERATIONALTEMPLATE operationaltemplate = atOptCache.get(filename);

            if (operationaltemplate == null){
                entryMap.put(ERROR, "Reported error for file:"+filename+", error:"+errorMap.get(filename));
                continue;
            }

            if (operationaltemplate.getTemplateId() != null) {
                entryMap.put(TEMPLATE_ID, operationaltemplate.getTemplateId().getValue());
            }
            else {
                entryMap.put(ERROR, "Could not get template id for template in file:"+filename);
            }
            if (operationaltemplate.getUid() != null){
                entryMap.put(UID, operationaltemplate.getUid().getValue());
            }
            if (operationaltemplate.getConcept() != null){
                entryMap.put(CONCEPT, operationaltemplate.getConcept());
            }

            Path path = Paths.get(getOptPath()+"/"+filename+".opt");
            try {
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                ZonedDateTime creationTime = ZonedDateTime.parse(attributes.creationTime().toString()).withZoneSameInstant(zoneId);
                entryMap.put(CREATED_ON, creationTime.toString());

                ZonedDateTime lastAccessTime = ZonedDateTime.parse(attributes.lastAccessTime().toString()).withZoneSameInstant(zoneId);
                entryMap.put(LAST_ACCESS_TIME, lastAccessTime.toString());

                ZonedDateTime lastModifiedTime = ZonedDateTime.parse(attributes.lastModifiedTime().toString()).withZoneSameInstant(zoneId);
                entryMap.put(LAST_MODIFIED_TIME, lastModifiedTime.toString());

                entryMap.put(PATH, path.toString());
            }
            catch (Exception e){
                entryMap.put(ERROR, "disconnected file? tried:"+getOptPath()+"/"+filename+".opt");
            }

            collection.add(entryMap);
        }

        mainMap.put(TEMPLATES, collection);

        return mainMap;
    }

    @Override
    public File retrieveFile(String key, KnowledgeType what) {

		switch(what) {
			case ARCHETYPE: return retrieveArchetypeFile(key);
			case TEMPLATE: return retrieveTemplateFile(key);
			case OPT: return retrieveOPTFile(key);
		}
		return null;
	}

    @Override
	public Map<String, File> retrieveFileMap(Pattern includes, Pattern excludes) {
		Map<String, File> mf = new LinkedHashMap<String, File>();
		if (includes != null) {
			for (String s : archetypeFileMap.keySet()) {
				if (includes.matcher(s).find()) {
					mf.put(s, archetypeFileMap.get(s));
				}
			}
		} else {
			mf.putAll(archetypeFileMap);
		}

		if (excludes != null) {
			List<String> removeList = new ArrayList<String>();
			for (String s : mf.keySet()) {
				if (excludes.matcher(s).find()) {
					removeList.add(s);
				}
			}
			for (String s : removeList) {
				mf.remove(s);
			}
		}
		return mf;
	}

    @Override
	public Archetype retrieveArchetype(String key) throws Exception {
		log.debug("retrieveArchetype(" + key + ")");
		Archetype at = atArchetypeCache.get(key);
		if (at == null) {
			InputStream in = null;
			try {
				in = getStream(key, KnowledgeType.ARCHETYPE);
                at = new ADLParser(in, DEFAULT_ENCODING).parse();
				atArchetypeCache.put(key, at);
			} catch (Exception e) {
                errorMap.put(key, e.getMessage());
                log.error("Could not parse archetype:"+key+" error:"+e);
//                throw new ServiceManagerException(global, SysErrorCode.INTERNAL_ILLEGALARGUMENT, "Could not parse archetype:"+key+" error:"+e);
			} finally {
				if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e){
                        throw new IllegalArgumentException("Could not close stream:"+e);
                    }
                }
			}
		}
		return at;
	}

    @Override
    public Object retrieveTemplate(String templateId){
        return globalIndex.get(templateId);
    }

    @Override
	public TEMPLATE retrieveOpenehrTemplate(String key) throws Exception {
		log.debug("retrieveOpenehrTemplate(" + key + ")");
		TEMPLATE template = atTemplatesCache.get(key);
		if (template == null) {
			InputStream in = null;
			try {
				in = getStream(key, KnowledgeType.TEMPLATE);

                TemplateDocument tdoc = TemplateDocument.Factory.parse(in);
                template = tdoc.getTemplate();

				atTemplatesCache.put(key, template);
                idxCache.put(UUID.fromString(template.getId()), key);
                globalIndex.put(template.getName(), template);
			} catch (Exception e) {
                errorMap.put(key, e.getMessage());
                log.error("Could not parse template:"+key+" error:"+e);
//                throw new ServiceManagerException(global, SysErrorCode.INTERNAL_ILLEGALARGUMENT, "Could not parse template:"+key+" error:"+e);
			} finally {
				if (in != null)
                    try {
                        in.close();
                    } catch (IOException e){
                        throw new IllegalArgumentException("Could not close stream:"+e);
                    }
			}
		}
		
		return template;
	}

    @Override
	public OPERATIONALTEMPLATE retrieveOperationalTemplate(String key) throws Exception {
		log.debug("retrieveOperationalTemplate(" + key + ")");
		OPERATIONALTEMPLATE template = atOptCache.get(key);
		if (template == null) {
			InputStream in = null;
			try {
				in = getStream(key, KnowledgeType.OPT);

                org.openehr.schemas.v1.TemplateDocument document = org.openehr.schemas.v1.TemplateDocument.Factory.parse(in);
                template = document.getTemplate();
                //use the template id instead of the file name as key
				atOptCache.put(key, template);
                idxCache.put(UUID.fromString(template.getUid().getValue()), key);
                globalIndex.put(template.getTemplateId().getValue(), template);
			} catch (Exception e) {
                errorMap.put(key, e.getMessage());
                log.error("Could not parse operational template:"+key+" error:"+e);
//                throw new ServiceManagerException(global, SysErrorCode.INTERNAL_ILLEGALARGUMENT, "Could not parse operational template:"+key+" error:"+e);
			} finally {
				if (in != null)
                    try {
                        in.close();
                    } catch (IOException e){
                        throw new IllegalArgumentException("Could not close stream:"+e);
                    }
			}
		}
		return template;
	}

    @Override
    public OPERATIONALTEMPLATE retrieveOperationalTemplate(UUID uuid) throws Exception {
        String key = idxCache.get(uuid);

        if (key == null)
            return null;

        return atOptCache.get(key);
    }

    @Override
    public TEMPLATE retrieveTemplate(UUID uuid) throws Exception {
        String key = idxCache.get(uuid);

        if (key == null)
            return null;

        return atTemplatesCache.get(key);
    }

    @Override
	public List<Archetype> retrieveArchetypeList(Pattern includes, Pattern excludes)
			throws Exception {
		log.debug("retrieveArchetype(" + includes + ", " + excludes + ")");
		List<Archetype> atList = new ArrayList<Archetype>();
		Map<String, File> files = retrieveFileMap(includes, excludes);
		for (Entry<String, File> entry : files.entrySet()) {
			atList.add(retrieveArchetype(entry.getKey()));
		}
		return atList;
	}

	@SuppressWarnings("resource")
	private InputStream getStream(String key, KnowledgeType what) throws IOException {
		File file = retrieveFile(key, what);
		return file != null ? new FileInputStream(file) : null;
	}

    @Override
	public boolean addKnowledgeSourcePath(String path, KnowledgeType what) throws Exception {
		switch(what) {
			case ARCHETYPE:
                return addKnowledgeSourcePath(path, archetypeFileMap, what.getExtension());
			case TEMPLATE:
                return addKnowledgeSourcePath(path, templatesFileMap, what.getExtension());
			case OPT:
                return addKnowledgeSourcePath(path, optFileMap, what.getExtension());
		}
		
		return false;
	}

	private boolean addKnowledgeSourcePath(String path, Map<String, File> resource, String extension) throws Exception {
		if (path == null) return false;

		path = path.trim();
		if (path.isEmpty())
			throw new IllegalArgumentException("Source path is empty!");

		File root = new File(path);

		root = new File(root.getAbsolutePath());

		if (!root.isDirectory())
			throw new IllegalArgumentException("Supplied source path:"+ path + "("+ root.getAbsolutePath() + ") is not a directory!");

		List<File> tr = new ArrayList<File>();
		tr.add(root);
		while (tr.size() > 0) {
			File r = tr.remove(tr.size() - 1);
			for (File f : r.listFiles()) {
				if (f.isHidden())
					continue;

				if (f.isFile()) {
					String key = f.getName().replaceAll("([^\\\\\\/]+)\\."+extension, "$1");
					//System.out.println("key=" + key + ":" +f);
					resource.put(key, f);
				} else if (f.isDirectory()) {
					tr.add(f);
				}
			}
		}
		return true;
	}

    @Override
    public Object retrieveGenerated(String name){
        if (cacheLocatable) {
            CacheSerialized cached = cacheSerialized.get(name);
            if (cached != null)
                return cached.getSerialized();
            else
                return null;
        }
        else
            return null;
    }

    @Override
    public ConstraintMapper retrieveCachedConstraints(String name){
        if (cacheLocatable) {
            CacheSerialized cached = cacheSerialized.get(name);
            if (cached != null)
                return cached.getConstraintMapper();
            else
                return null;
        }
        else
            return null;
    }

    @Override
    public void cacheGenerated(String name, Object objectOutput, ConstraintMapper constraintMapper){
        if (cacheLocatable) {
            CacheSerialized serialized = new CacheSerialized(objectOutput, constraintMapper);
            cacheSerialized.put(name, serialized);
        }
    }

    @Override
    public Boolean isLocatableCached() {
        return cacheLocatable;
    }

    @Override
    public Boolean cacheContainsLocatable(String name){
        if (cacheLocatable)
            return cacheSerialized.containsKey(name);
        else
            return false;
    }

    @Override
    public void invalidateCache(String templateId){
        if (cacheContainsLocatable(templateId)){
            log.info("invalidating cache for template id:"+templateId);
            cacheSerialized.remove(templateId);
        }
    }

    @Override
    public void invalidateCache(){
        log.info("invalidating the whole cache");
        cacheSerialized.clear();
    }

	@Override
	public Map<String, Archetype> getArchetypeMap() {
		return atArchetypeCache;
	}

    @Override
    public Map<String, String> getOperationalTemplateMap() {
        Map<String, String> returnMap = new HashMap<>();

        for (OPERATIONALTEMPLATE operationaltemplate: atOptCache.values()){
            returnMap.put(operationaltemplate.getTemplateId().getValue(), operationaltemplate.getConcept());
        }

        return returnMap;
    }

    @Override
    public String archeypesList(){
        StringBuffer sb = new StringBuffer();
        sb.append("FOUND ARCHETYPES:\n");
        sb.append("=================\n");
        for (String atName: archetypeFileMap.keySet())
            sb.append(atName+":"+(archetypeFileMap.get(atName)).getAbsolutePath()+"\n");

        return sb.toString();
    }

    @Override
    public String oetList(){
        StringBuffer sb = new StringBuffer();
        sb.append("FOUND TEMPLATES:\n");
        sb.append("================\n");

        for (String atName: templatesFileMap.keySet())
            sb.append(atName+":"+(templatesFileMap.get(atName)).getAbsolutePath()+"\n");

        return sb.toString();
    }

    @Override
    public String optList(){
        StringBuffer sb = new StringBuffer();
        sb.append("FOUND OPERATIONAL TEMPLATES:\n");
        sb.append("============================\n");

        for (String atName: optFileMap.keySet())
            sb.append(atName+":"+(optFileMap.get(atName)).getAbsolutePath()+"\n");

        return sb.toString();
    }

    @Override
    public String statistics(){
        StringBuffer sb = new StringBuffer();
        sb.append("\n\nFound Archetype Definitions    :"+archetypeFileMap.size()+"\n");
        sb.append("Found openEHR Templates        :"+templatesFileMap.size()+"\n");
        sb.append("Found Operational Templates    :"+optFileMap.size()+"\n");
        sb.append("In-cache Archetypes            :"+atArchetypeCache.size()+"\n");
        sb.append("In-cache Templates             :"+atTemplatesCache.size()+"\n");
        sb.append("In-cache generated locatable   :"+ cacheSerialized.size()+"\n");
        sb.append("Global template index          :"+globalIndex.size()+"\n");
        sb.append("Processing errors found        :"+errorMap.size()+"\n");

        sb.append("=== INTERNAL ====\n");
        sb.append(settings());

        return sb.toString();
    }

    @Override
    public boolean isForceCache() {
        return forceCache;
    }

    @Override
    public void setForceCache(boolean forceCache) {
        this.forceCache = forceCache;
    }

    @Override
    public String settings(){
        StringBuffer sb = new StringBuffer();
        sb.append("Force Cache              :"+ forceCache);
        sb.append("\nCache Generated Locatable:"+ cacheLocatable);
        sb.append("\nArchetype Path           :"+archetypePath);
        sb.append("\nTemplate Path            :"+templatePath);
        sb.append("\nBackup Path              :"+ (backupPath.equals(NULL_PATH) ? "*no template backup will be done*" : backupPath));
        sb.append("\nOperational Template Path:"+optPath);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String processingErrors(){
        StringBuffer sb = new StringBuffer();

        for (Entry<String, String> entry: errorMap.entrySet()){
            sb.append(entry.getKey()+" ==> "+entry.getValue()+"\n");
        }

        return sb.toString();
    }

    @Override
    public String getArchetypePath() {
        return archetypePath;
    }

    @Override
    public String getOptPath() {
        return optPath;
    }

    @Override
    public String getTemplatePath() {
        return templatePath;
    }


}
