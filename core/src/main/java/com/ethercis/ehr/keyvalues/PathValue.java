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
package com.ethercis.ehr.keyvalues;

import com.ethercis.ehr.building.ContentBuilder;
import com.ethercis.ehr.building.ContentUtil;
import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.FieldUtil;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.*;
import com.ethercis.ehr.encode.wrappers.constraints.ConstraintUtils;
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import com.ethercis.ehr.encode.wrappers.element.AnyElementWrapper;
import com.ethercis.ehr.encode.wrappers.element.ChoiceElementWrapper;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.LocatableHelper;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.SystemValue;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.composition.content.entry.Activity;
import org.openehr.rm.composition.content.entry.Entry;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.terminology.SimpleTerminologyService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ECISFLAT handling
 * The format consists of a list of key/value pairs:
 * - path
 * - value
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 9/24/2015.
 */
public class PathValue implements I_PathValue {

    protected String templateId;
    protected PartyIdentified composer = null;
    protected CodePhrase language = null;
    protected CodePhrase territory = null;
    protected DvCodedText category = null;
    protected UIDBasedID uid = null;
    protected I_KnowledgeCache knowledge;

    protected static Logger log = LogManager.getLogger(PathValue.class);

    //context attributes
    protected PartyIdentified healthCareFacility = null;
    protected List<Participation> participationList = new ArrayList<>();
    protected DvDateTime startTime = null;
    protected DvDateTime endTime = null;
    protected String location = null;
    protected DvCodedText setting = null;
    protected ItemStructure otherContext = null;
    protected Map<String, Object> otherContextMap = new HashMap<>();

    protected List<String> doneOtherParticipationList = new ArrayList<>();


//    private String defaultTerminologyConcept = null;
    protected String defaultTerminologyLocal = DEFAULT_LOCAL_TERMINOLOGY;
    protected String defaultTerminologyTerritory = DEFAULT_TERRITORY_TERMINOLOGY;
    protected String defaultTerminologyLanguage = DEFAULT_LANGUAGE_TERMINOLOGY;
    protected String defaultPartyNamespace = DEFAULT_PARTY_NAME_SPACE;
    protected String defaultPartyScheme = DEFAULT_PARTY_SCHEME;
    protected String defaultPartyCategory = DEFAULT_PARTY_CATEGORY;

    //used to get the results of the last update (used for optimizing updates in the DB...)
    protected boolean modifiedContext = false;
    protected boolean modifiedContent = false;
    protected boolean modifiedAttributes = false;

    protected I_ContentBuilder contentBuilder;

    public PathValue(I_KnowledgeCache cache, String templateId, Properties properties) {

        if (templateId == null)
            throw new IllegalArgumentException("INTERNAL: templateId is required, check your code");

        this.knowledge = cache;
        this.templateId = templateId;

//        defaultTerminologyConcept = properties.getProperty(CONCEPT_TERMINOLOGY_PROP, "openehr");
        if (properties != null) {
            defaultTerminologyLocal = properties.getProperty(LOCAL_TERMINOLOGY_PROP, DEFAULT_LOCAL_TERMINOLOGY);
            defaultTerminologyTerritory = properties.getProperty(TERRITORY_TERMINOLOGY_PROP, DEFAULT_TERRITORY_TERMINOLOGY);
            defaultTerminologyLanguage = properties.getProperty(LANGUAGE_TERMINOLOGY_PROP, DEFAULT_LANGUAGE_TERMINOLOGY);
            defaultPartyNamespace = properties.getProperty(PARTY_NAMESPACE_PROP, DEFAULT_PARTY_NAME_SPACE);
            defaultPartyScheme = properties.getProperty(PARTY_SCHEME_PROP, DEFAULT_PARTY_SCHEME);
            defaultPartyCategory = properties.getProperty(PARTY_CATEGORY_PROP, DEFAULT_PARTY_CATEGORY);
        }
    }

    public PathValue(I_ContentBuilder contentBuilder, I_KnowledgeCache cache, String templateId, Properties properties) {
        this(cache, templateId, properties);
        this.contentBuilder = contentBuilder;
    }

    /**
     * Use for updates
     * @param properties
     */
    public PathValue(Properties properties) {
        if (properties != null) {
//        defaultTerminologyConcept = properties.getProperty(CONCEPT_TERMINOLOGY_PROP, "openehr");
            defaultTerminologyLocal = properties.getProperty(LOCAL_TERMINOLOGY_PROP, DEFAULT_LOCAL_TERMINOLOGY);
            defaultTerminologyTerritory = properties.getProperty(TERRITORY_TERMINOLOGY_PROP, DEFAULT_TERRITORY_TERMINOLOGY);
            defaultTerminologyLanguage = properties.getProperty(LANGUAGE_TERMINOLOGY_PROP, DEFAULT_LANGUAGE_TERMINOLOGY);
            defaultPartyNamespace = properties.getProperty(PARTY_NAMESPACE_PROP, DEFAULT_PARTY_NAME_SPACE);
            defaultPartyScheme = properties.getProperty(PARTY_SCHEME_PROP, DEFAULT_PARTY_SCHEME);
            defaultPartyCategory = properties.getProperty(PARTY_CATEGORY_PROP, DEFAULT_PARTY_CATEGORY);
        }
    }


    public Composition assign(Map<String, Object> keyValues) throws Exception {
        Map<String, Object> mapContent = new HashMap<>();
        Boolean hasContext = false;
        Boolean hasComposer = false;

        //traverse the queue

        for (String path : keyValues.keySet()) {
            String value;
            try {
                value = (String) keyValues.get(path);
            }catch (Exception e){
                throw new IllegalArgumentException("Value could not be interpreted, path:"+path+ ", found:"+keyValues.get(path));
            }

            if (path.startsWith(CONTENT_TAG)) {
                mapContent.put(path, value);
            }
            else if (path.startsWith(CONTEXT_TAG) && !hasContext) {
                hasContext = assignContextAttribute(keyValues);
            }
            else if (path.startsWith(LANGUAGE_TAG))
                language = parseLanguageAttribute(value);
            else if (path.startsWith(UID_TAG))
                uid = parseUIDAttribute(value);
            else if (path.startsWith(TERRITORY_TAG))
                territory = parseTerritoryAttribute(value);
            else if (path.matches(COMPOSER_REGEXP) && !hasComposer) {
                //get the actual value to work with
                value = keyValues.get(COMPOSER_TAG+IDENTIFIER_PARTY_ID_SUBTAG) + "::" + keyValues.get(COMPOSER_TAG+IDENTIFIER_PARTY_NAME_SUBTAG);
                composer = parseComposerAttribute(value);
                hasComposer = true;
            }
//            else if (path.startsWith(CATEGORY_TAG))
//                category = parseCategoryAttribute(value);
            else
                log.debug("Passthrough unhandled directive:"+path);
            //identify object class for this path
        }


        //at this stage, all values have been parsed
        EventContext eventContext = null;
        if (hasContext) {
            TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
            eventContext = new EventContext(healthCareFacility,
                    (startTime != null) ? startTime : new DvDateTime(new DateTime(0L).toString()),
                    (endTime != null) ? endTime : new DvDateTime(DateTime.now().toString()),
                    participationList.isEmpty() ? null : participationList,
                    location,
                    setting == null ? new DvCodedText("null", "openehr", "238") : setting,
                    otherContext,
                    terminologyService);
        }

        Map<SystemValue, Object> map = new HashMap<>();

        if (hasContext) map.put(SystemValue.CONTEXT, eventContext);
        if (language != null) map.put(SystemValue.LANGUAGE, language);
        if (territory != null) map.put(SystemValue.TERRITORY, territory);
        if (hasComposer) map.put(SystemValue.COMPOSER, composer);
        if (category != null) map.put(SystemValue.CATEGORY, category);
        if (uid != null) map.put(SystemValue.UID, uid);

        contentBuilder = I_ContentBuilder.getInstance(map, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();

        ConstraintUtils constraintUtils = new ConstraintUtils(contentBuilder.isLenient(), composition, contentBuilder.getConstraintMapper());
        constraintUtils.validateLocatable();

        assignItemStructure(CONTENT_TAG, composition, mapContent);

        //2nd build phase: assign other item structures with passed values
        if (composition.getContext().getOtherContext() != null)
            assignItemStructure(OTHER_CONTEXT_TAG, composition.getContext().getOtherContext(), otherContextMap);

        //validation
        new ConstraintUtils(contentBuilder.isLenient(), composition, contentBuilder.getConstraintMapper()).validateLocatable();

        return composition;
    }

    public Boolean update(Composition composition, Map<String, Object> keyValues) throws Exception {

        modifiedContent = modifiedAttributes = modifiedContext = false;

        if (composition.getContext() == null){
            EventContext context = ContextHelper.createDummyContext();
            composition.setContext(context);
        }
        modifiedContext = updateEventContext(composition.getContext(), keyValues);
        modifiedAttributes = updateCompositionAttributes(composition, keyValues);
        modifiedContent = assignItemStructure(CONTENT_TAG, composition, keyValues);

        return modifiedContext || modifiedAttributes || modifiedContent;
    }

    public Boolean updateEventContext(EventContext eventContext, Map<String, Object> keyValues) throws Exception {

        boolean modified = false;
        boolean doneParticipation = false;
        boolean doneFacility = false;
        List<String> participations = new ArrayList<>();

        for (String path: keyValues.keySet()) {

            if (!path.startsWith(CONTEXT_TAG))
                continue;

            modified = true;

            String value = (String)keyValues.get(path);
            String attribute = path.substring(CONTEXT_TAG.length());

            if (path.matches(CONTEXT_TAG + PARTICIPATION_REGEXP))
                attribute = CTX_PARTICIPATION_TAG;
            else if (path.matches(CONTEXT_TAG + FACILITY_REGEXP))
                attribute = CTX_FACILITY_TAG;
            else if (path.startsWith(CONTEXT_TAG+CTX_OTHER_CONTEXT_TAG))
                attribute = CTX_OTHER_CONTEXT_TAG;

            String basePath;
            if (path.contains("|")) {
                basePath = path.substring(0, path.indexOf("|"));
            }
            else
                basePath = path;

            switch (attribute) {
                case CTX_FACILITY_TAG:
                    if (doneFacility) continue;
                    value = keyValues.get(CONTEXT_TAG + CTX_FACILITY_TAG + IDENTIFIER_PARTY_ID_SUBTAG) + "::" + keyValues.get(CONTEXT_TAG + CTX_FACILITY_TAG + IDENTIFIER_PARTY_NAME_SUBTAG);
                    eventContext.setHealthCareFacility(new PartyIdentifiedVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory));
                    doneFacility = true;
                    break;
                case CTX_START_TIME_TAG:
                    eventContext.setStartTime(new DvDateTimeVBean(new DvDateTime(DateTime.now().toString())).parse(value));
                    break;
                case CTX_END_TIME_TAG:
                    eventContext.setEndTime(new DvDateTimeVBean(new DvDateTime(DateTime.now().toString())).parse(value));
                    break;
                case CTX_PARTICIPATION_TAG:
                    if (!doneParticipation) eventContext.setParticipations(null); //the list of participation will be rebuilt

                    if (participations.contains(basePath)) continue;
                    value = buildParticipationCode(keyValues, basePath);
                    Participation participation = new ParticipationVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory);
                    List<Participation> participationList;
                    if (eventContext.getParticipations() == null) {
                        participationList = new ArrayList<>();
                        eventContext.setParticipations(participationList);
                    } else
                        participationList = eventContext.getParticipations();
                    participationList.add(participation);
                    participations.add(basePath);
                    doneParticipation = true;
                    break;
                case CTX_SETTING_TAG:
                    //TODO: pass default to parser!
                    eventContext.setSetting(new DvCodedTextVBean(new DvCodedText("default", "local", "1")).parse(value));
                    break;
                case CTX_LOCATION_TAG:
                    eventContext.setLocation(value);
                    break;
                case CTX_OTHER_CONTEXT_TAG:
                    log.debug("Assign Other context with path:" + path);
//                    path = path.substring(path.indexOf("]")); //should be in the form: /other_context[at0001]/... strip the prefix
                    Map<String, Object> valuemap = new HashMap<>();
                    valuemap.put(path, value);
                    assignItemStructure(OTHER_CONTEXT_TAG, eventContext.getOtherContext(), valuemap);
                    break;
                default:
                    log.warn("Unhandled context attribute:" + attribute);
            }
        }
        return modified;
    }

    private boolean updateCompositionAttributes(Composition composition, Map<String, Object> keyValues) throws Exception {
        boolean modified = false;

        for (String path : keyValues.keySet()) {
            String value = (String)keyValues.get(path);

            if (LANGUAGE_TAG.equals(path)) {
                composition.setLanguage(parseLanguageAttribute(value));
                modified = true;
            }
            else if (UID_TAG.equals(path)) {
                composition.setUid(parseUIDAttribute(value));
                modified = true;
            }
            else if (TERRITORY_TAG.equals(path)) {
                composition.setTerritory(parseTerritoryAttribute(value));
                modified = true;
            }
            else if (path.matches(COMPOSER_REGEXP)) {
                //get the actual value to work with
                value = keyValues.get(COMPOSER_TAG + IDENTIFIER_PARTY_ID_SUBTAG) + "::" + keyValues.get(COMPOSER_TAG + IDENTIFIER_PARTY_NAME_SUBTAG);
                composition.setComposer( parseComposerAttribute(value));
                modified = true;
            }
//            else if (CATEGORY_TAG.equals(path)) {
//                modified = true;
//                composition.setCategory(parseCategoryAttribute(value));
//            }
        }
        return modified;
    }

    protected UIDBasedID parseUIDAttribute(String value) {
        return (UIDBasedID) new HierObjectIDVBean().parse(value);
    }

    protected DvCodedText parseCategoryAttribute(String value) {
        return new DvCodedTextVBean(new DvCodedText("default", "local", "1")).parse(value);
    }

    protected PartyIdentified parseComposerAttribute(String value) {
        return new PartyIdentifiedVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory);
    }

    protected CodePhrase parseTerritoryAttribute(String value) {
        return new CodePhraseVBean(new CodePhrase("local", "1")).parse(value, defaultTerminologyTerritory);
    }

    protected CodePhrase parseLanguageAttribute(String value) {
        return new CodePhraseVBean(new CodePhrase("local", "1")).parse(value, defaultTerminologyLanguage);
    }

    protected String buildParticipationCode(Map<String, Object> pathValues, String baseTag){
        String value = pathValues.get(baseTag+PARTICIPATION_FUNCTION_SUBTAG)+"|"+
                pathValues.get(baseTag+IDENTIFIER_PARTY_ID_SUBTAG)+"::"+
                pathValues.get(baseTag+IDENTIFIER_PARTY_NAME_SUBTAG)+"|"+
                pathValues.get(baseTag+PARTICIPATION_MODE_SUBTAG);

        return value;
    }

    /**
     * deals with "/context" entries
     * @param
     * @return
     */
    private boolean assignContextAttribute(Map<String, Object> pathValues){
        List<String> participations = new ArrayList<>();
        Boolean hasContext = false;
        Boolean hasFacility = false;
        Boolean hasParticipation = false;

        for (String path: pathValues.keySet()) {

            if (!path.startsWith(CONTEXT_TAG))
                continue;

            hasContext = true;

            String value = (String)pathValues.get(path);
            String attribute = path.substring(CONTEXT_TAG.length());

            if (path.matches(CONTEXT_TAG+PARTICIPATION_REGEXP))
                attribute = CTX_PARTICIPATION_TAG;
            else if (path.matches(CONTEXT_TAG+FACILITY_REGEXP))
                attribute = CTX_FACILITY_TAG;
            else if (path.startsWith(CONTEXT_TAG+CTX_OTHER_CONTEXT_TAG))
                attribute = CTX_OTHER_CONTEXT_TAG;

            String basePath;
            if (path.contains("|")) {
                basePath = path.substring(0, path.indexOf("|"));
            }
            else
                basePath = path;

            switch (attribute) {
                case CTX_FACILITY_TAG:
                    if (hasFacility) continue;
                    value = pathValues.get(CONTEXT_TAG+CTX_FACILITY_TAG+IDENTIFIER_PARTY_ID_SUBTAG)+"::"+pathValues.get(CONTEXT_TAG+CTX_FACILITY_TAG+IDENTIFIER_PARTY_NAME_SUBTAG);
                    healthCareFacility = new PartyIdentifiedVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory);
                    hasFacility = true;
                    break;
                case CTX_START_TIME_TAG:
                    startTime = new DvDateTimeVBean(new DvDateTime(DateTime.now().toString())).parse(value);
                    break;
                case CTX_END_TIME_TAG:
                    endTime = new DvDateTimeVBean(new DvDateTime(DateTime.now().toString())).parse(value);
                    break;
                case CTX_PARTICIPATION_TAG:
                    if (participations.contains(basePath)) continue;
                    value = buildParticipationCode(pathValues, basePath);
                    Participation participation = new ParticipationVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory);
                    participationList.add(participation);
                    participations.add(basePath);
                    break;
                case CTX_SETTING_TAG:
                    setting = new DvCodedTextVBean(new DvCodedText("default", "local", "1")).parse(value);
                    break;
                case CTX_LOCATION_TAG:
                    location = value;
                    break;
                case CTX_OTHER_CONTEXT_TAG:
                    log.debug("Assign Other context with path:" + path);
                    otherContextMap.put(path, value); //store to assign in the second phase of composition building
//                    assignItemStructure(eventContext.getOtherContext(), valuemap);
                    break;
                default:
                    log.warn("Unhandled context attribute:" + attribute);
            }
        }

        return hasContext;
    }

    private void assignEntryAttribute(Map<String, Object> pathValues, Entry entry, String path, String attribute, String value){
        boolean isComposite = false; //true if it has subtags

        if (!ArrayUtils.contains(ENTRY_TAGS, attribute)){
            log.warn("Attribute is not supported for Entry:"+attribute);
            return;
        }

        //get the prefix before subtag part only
        String basePath;
        if (path.contains("|")) {
            basePath = path.substring(0, path.indexOf("|"));
            isComposite = true;
        }
        else
            basePath = path;

        switch (attribute) {
            case ENTRY_PARTICIPATION:

                if (doneOtherParticipationList.contains(basePath))
                    return;

                if (isComposite) {
                    value = buildParticipationCode(pathValues, basePath);
                }
                log.debug("adding other_participation @"+basePath+" value:"+value);

                doneOtherParticipationList.add(basePath); //to check if this has already be processed

                Participation participation =  new ParticipationVBean().parse(value, defaultPartyScheme, defaultPartyNamespace, defaultPartyCategory);
                if (entry.getOtherParticipations() == null) {
                    List<Participation> participationList = new ArrayList<>();
                    participationList.add(participation);
                    entry.setOtherParticipations(participationList);
                }
                else {
                    List<Participation> participationList = new ArrayList<>();
                    participationList.addAll(entry.getOtherParticipations()); //participationlist in entry is immutable!
                    participationList.add(participation);
                    entry.setOtherParticipations(participationList);
                }
        }
    }

    private void assignActivityAttribute(Activity activity, String attribute, String value){
        if (!ArrayUtils.contains(ACTIVITY_TAGS, attribute)){
            log.warn("Attribute is not supported for Activity:"+attribute);
            return;
        }

        switch (attribute){
            case ACTIVITY_TIMING:
                //TODO: modify this to an actual parsing when more information is given on how to use this type (if applicable)
                //                DvParsable dvParsable = new DvParsableVBean(null).parse(value);
                DvParsable dvParsable = new DvParsable(value, "*");
                activity.setTiming(dvParsable);
                break;
            case ACTIVITY_ARCHETYPE_ID:
                activity.setActionArchetypeId(value);
                break;
            default:
                log.warn("ACTIVITY attribute is not handled:"+attribute);

        }
    }


    private DataValue assignElementValue(ElementWrapper elementWrapper, String valueToParse) throws Exception {

        I_VBeanWrapper adapted = null;
        DataValue parsed;

        if (elementWrapper instanceof ChoiceElementWrapper) {
            adapted = ((ChoiceElementWrapper) elementWrapper).getAdaptedValue(valueToParse);
            parsed = (DataValue)adapted.getAdaptee();
        }
        else if (elementWrapper instanceof AnyElementWrapper) {
            adapted = ((AnyElementWrapper) elementWrapper).getAdaptedValue(valueToParse);
            parsed = (DataValue)adapted.getAdaptee();
        }
        else {
            adapted = ElementWrapper.getAdaptedValue(elementWrapper);
            if (adapted instanceof DvCodedTextVBean)
                parsed = (DataValue) adapted.parse(valueToParse, defaultTerminologyLocal);
            else {
                //TODO: make some wild guess on the data type: ex. Date, Time, DateTime etc.
                parsed = (DataValue) adapted.parse(valueToParse);
            }
        }

        if (adapted == null || parsed == null)
            throw new IllegalArgumentException("Could not identify an adapted value for element:"+elementWrapper);

        DataValueConstraints constraints = elementWrapper.getConstraints();

        if (constraints != null && !constraints.validate(parsed)) {
            String description = constraints.getDescription();
            String nodeId = elementWrapper.getAdaptedElement().getArchetypeNodeId();
            String name = elementWrapper.getAdaptedElement().getName().getValue();
            throw new IllegalArgumentException("Parsed value for [" + nodeId + "," + name + "]" + ", '" + description + "' is not valid, found: " + parsed.toString());
        }
        return parsed;
    }

    private DataValue assignElementValue(ElementWrapper elementWrapper, Map<String, Object> attributeSet) throws Exception {

        I_VBeanWrapper adapted = null;
        DataValue parsed;

        if ((attributeSet.size() == 1 || (attributeSet.size() == 2 && attributeSet.containsKey("name"))) && attributeSet.containsKey("value")){ //single value can be parsed
            return assignElementValue(elementWrapper, (String)attributeSet.get("value"));
        }

//        if (elementWrapper instanceof ChoiceElementWrapper) {
//            adapted = ((ChoiceElementWrapper) elementWrapper).getAdaptedValue(valueToParse);
//            parsed = (DataValue)adapted.getAdaptee();
//        }
//        else if (elementWrapper instanceof AnyElementWrapper) {
//            adapted = ((AnyElementWrapper) elementWrapper).getAdaptedValue(valueToParse);
//            parsed = (DataValue)adapted.getAdaptee();
//        }
//        else {
        adapted = ElementWrapper.getAdaptedValue(elementWrapper);
        //generate a corresponding dummy adapted dummy element
        Class adaptedClass = adapted.getClass();
        Method generateDummy = adaptedClass.getDeclaredMethod("generate", null);
        Map<String, Object> valueMap = FieldUtil.getAttributes(generateDummy.invoke(null, null));

        Class adaptedElementValueClass = elementWrapper.getAdaptedElement().getValue().getClass();
        parsed = (DataValue)PathValue.decodeValue(adaptedElementValueClass.getSimpleName(), FieldUtil.flatten(valueMap), attributeSet);
//        }

        if (adapted == null || parsed == null)
            throw new IllegalArgumentException("Could not identify an adapted value for element:"+elementWrapper);

        DataValueConstraints constraints = elementWrapper.getConstraints();

        if (constraints != null && !constraints.validate(parsed)) {
            String description = constraints.getDescription();
            String nodeId = elementWrapper.getAdaptedElement().getArchetypeNodeId();
            String name = elementWrapper.getAdaptedElement().getName().getValue();
            throw new IllegalArgumentException("Parsed value for [" + nodeId + "," + name + "]" + ", '" + description + "' is not valid, found: " + parsed.toString());
        }
        return parsed;
    }


    public boolean assignItemStructure(String filterTag, Locatable locatable, Map<String, Object> pathValues) throws Exception {

        boolean modified = false;

        SortedMap<String, Object> sortedMap = new TreeMap<>();
        sortedMap.putAll(pathValues);
        String[] keySetArray = sortedMap.keySet().toArray(new String[]{});
        Map<String, Object> attributeSet = null;

//        ContentUtil contentUtil = new ContentUtil(templateId);

        for (int pathIterator = 0; pathIterator < sortedMap.keySet().size(); pathIterator++) {

            String path = keySetArray[pathIterator];

            //CHC 160819 - Change path expression containing 'and name/value=' by the shortcut ","
            //itemAtPath seems not to interpret the longer expression:
            //=> expression with name/value returns null, whereas using the shortcut returns the expected item

//            path = path.replaceAll(" and name/value=", ",");

            if (path != null && !path.startsWith(filterTag))
                continue;

            modified = true;

            Integer tagIndex = path.lastIndexOf("/");
            String lastTag = path.substring(tagIndex);
            String attribute = null;
            String attributeKey = null;
            String locatablePath = path;

            if (lastTag.contains("|")){ //a leaf node followed by an attribute
                attributeSet = new HashMap<>();
                String[] segments = lastTag.split("\\|");
                attribute = segments[1];
                attributeSet.put(attribute, sortedMap.get(path));
                locatablePath =  path.substring(0, path.length() - attribute.length() - 1);
                int j = pathIterator + 1; //look ahead and grab remaining attributes for this path
                while (j < keySetArray.length && keySetArray[j].contains(locatablePath+"|")) {
                    //grab the next attribute for this path
                    segments = keySetArray[j].split("\\|");
                    attribute = segments[1];
                    attributeSet.put(attribute, sortedMap.get(keySetArray[j]));
                    j++;
                }
                pathIterator = j-1; //skip what has been processed.
            }
            else
                attributeSet = null; //reset attribute set

            if (ArrayUtils.contains(ENTRY_TAGS, lastTag) || ArrayUtils.contains(INSTRUCTION_TAGS, lastTag) || ArrayUtils.contains(ACTIVITY_TAGS, lastTag)){
                attributeKey = path;
                locatablePath = path.substring(0, tagIndex);
                attribute = lastTag; //for readability purpose..
            }
            else { //special cases, array expressions
                if (lastTag.matches(PARTICIPATION_REGEXP)){
                    //strip the array index bit
                    attributeKey = path;
                    locatablePath = path.substring(0, tagIndex);
                    attribute = lastTag.substring(0, lastTag.indexOf(":"));
                }
            }

            if (filterTag.startsWith(OTHER_CONTEXT_TAG) || filterTag.startsWith(CompositionSerializer.TAG_ITEMS)){
                locatablePath = locatablePath.substring(filterTag.length()); //should be in the form: /context/other_context[at0001]/... strip the prefix
            }


//            Object itemAtPath = locatable.itemAtPath(locatablePath);
            Object itemAtPath = LocatableHelper.itemAtPath(locatable, locatablePath);

            if (itemAtPath == null){
                //build a definition based on the provided path...
                String lastPathSegment = locatablePath.substring(locatablePath.lastIndexOf("[")+1, locatablePath.lastIndexOf("]"));
                Map<String, Object> definition = new HashMap<>();
                if (lastPathSegment.contains("and name/value=") || lastPathSegment.contains(",")){
                    String name = lastPathSegment.substring(lastPathSegment.indexOf("'")+1, lastPathSegment.lastIndexOf("'"));
                    if (!name.contains("|")){
                        //assume simple string name
                        definition.put(CompositionSerializer.TAG_NAME, name);
                    }
                    else { //assume coded text
                        DvCodedText dvCodedText = new DvCodedText("1","local","1").parse(name);
                        definition.put(CompositionSerializer.TAG_NAME, dvCodedText.getValue());
                        Map<String, Object> definingCodeMap = new LinkedTreeMap<>();
                        Map<String, String> terminologyIdMap = new HashMap<>();
                        terminologyIdMap.put("value", dvCodedText.getTerminologyId());
                        definingCodeMap.put("terminologyId", terminologyIdMap);
                        definingCodeMap.put("codeString", dvCodedText.getCode());

                        definition.put(CompositionSerializer.TAG_DEFINING_CODE, definingCodeMap);
                    }
                }
//                definition.put(CompositionSerializer.TAG_NAME, "");
                itemAtPath = contentBuilder.insertCloneInPath(locatable, definition, locatablePath);
            }

            String valueToParse = null;
            if (attributeSet == null)
                valueToParse = (String)sortedMap.get(path);

            if (itemAtPath instanceof ElementWrapper) {
                DataValue parsed = null;
                if (valueToParse != null){
                    parsed = assignElementValue((ElementWrapper)itemAtPath, valueToParse);
                } else if (attributeSet != null){
                    parsed = assignElementValue((ElementWrapper)itemAtPath, attributeSet);
                }
                else
                    throw new IllegalArgumentException("Cannot generate any parsed value...");

                ((ElementWrapper)itemAtPath).getAdaptedElement().setValue(parsed);
                ((ElementWrapper)itemAtPath).setDirtyBit(true);
            } else if (itemAtPath instanceof Activity) {
                assignActivityAttribute((Activity) itemAtPath, attribute, (String)sortedMap.get(attributeKey));
            }
            else if (itemAtPath instanceof Entry){
                assignEntryAttribute(sortedMap, (Entry) itemAtPath, path, attribute, (String)sortedMap.get(attributeKey));
            }
            else if (itemAtPath instanceof DataValue){
                //check who is the parent of this datavalue
                String parentPath = LocatableHelper.getLocatableParentPath(locatable, locatablePath);
                Locatable parent = (Locatable) locatable.itemAtPath(parentPath);
                //get the complete attribute identifier
                String attributePath = locatablePath.substring(parentPath.length());
                if (attributeSet.size() == 1 || (attributeSet.size() == 2 && attributeSet.containsKey("name"))) {
                    DataValue dataValue = ((DataValue) itemAtPath).parse((String)attributeSet.get("value"));
                    parent.set(attributePath, dataValue);
                }
                else { //use RMBuilder for more complex value types: DV_PARSABLE etc.
                    RMObjectBuilder rmObjectBuilder = new RMObjectBuilder();
                    if (!attributeSet.containsKey("charset")){
                        CodePhrase charset = new CodePhrase("IANA_character-sets","UTF-8");
                        attributeSet.put("charset", charset);
                    }
                    if (!attributeSet.containsKey("language")){
                        CodePhrase lang = new CodePhrase("ISO_639-1", "en");
                        attributeSet.put("language", lang);
                    }
                    if (!attributeSet.containsKey("terminologyService")){
                        TerminologyService terminologyService = SimpleTerminologyService.getInstance();
                        attributeSet.put("terminologyService", terminologyService);
                    }
                    RMObject object = rmObjectBuilder.construct(itemAtPath.getClass().getSimpleName(), attributeSet);
                    parent.set(attributePath, object);
                }
            }
            else {
                log.warn("Unhandled path value:"+path);
            }
        }
        return modified;
    }


    public static Object decodeValue(String dvClassName, Map<String, String> dvMap, Map<String, Object> args) throws Exception {
        Map<String, Object> values = new HashMap<>();
        for (String atributeName: args.keySet()){
            //get the corresponding definition
            if (dvMap.containsKey(atributeName)){
                String attributeClassName = dvMap.get(atributeName);
                if (attributeClassName.contains("java.lang.")){
                    Class clazz = ClassUtils.getClass(attributeClassName);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    Object value = constructor.newInstance(args.get(atributeName));
                    values.put(atributeName, value);

                }
                else if (attributeClassName.contains(RMObjectBuilder.OPENEHR_RM_PACKAGE)) {
                    String attrClassSimpleName = attributeClassName.substring(attributeClassName.lastIndexOf(".")+1);
                    //get instrumentalized class
                    Class attributeInstrument = VBeanUtil.findInstrumentalizedClass(attrClassSimpleName);
                    if (attributeInstrument != null) {
                        Method generate = attributeInstrument.getDeclaredMethod("generate", null);
                        Object generated = generate.invoke(null, null);
                        Constructor constructor = attributeInstrument.getDeclaredConstructor(new Class[]{generated.getClass()});
                        Object instrumentalized = constructor.newInstance(generated);
                        Method parser = attributeInstrument.getDeclaredMethod("parse", new Class[]{String.class, String[].class});
                        Object value = parser.invoke(instrumentalized, args.get(atributeName), (String)null);
                        values.put(atributeName, value);
                    } else {
                        log.info("possible primitive?" + attrClassSimpleName);
                        values.put(atributeName, args.get(atributeName));
                    }
                }
                else { //primitive: int, double etc.
                    Class clazz = ClassUtils.getClass(attributeClassName);
                    Class wrapperClass = ClassUtils.primitiveToWrapper(clazz);
                    //invoke the constructor with String
                    Constructor constructor = wrapperClass.getDeclaredConstructor(new Class[]{String.class});
                    Object value = constructor.newInstance(args.get(atributeName));
                    values.put(atributeName, value);
                }

            }
        }

        //wrap up, create the object...
        Class mainInstrument = VBeanUtil.findInstrumentalizedClass(dvClassName);
        Method instanceGetter = mainInstrument.getDeclaredMethod("getInstance", new Class[]{Map.class});
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(CompositionSerializer.TAG_VALUE, values);

        Object generated = instanceGetter.invoke(null, valueMap);
        if (generated instanceof DataValue)
            return  (DataValue)generated;

        return generated;
    }

    @Override
    public boolean isModifiedContext() {
        return modifiedContext;
    }

    @Override
    public boolean isModifiedAttributes() {
        return modifiedAttributes;
    }

    @Override
    public boolean isModifiedContent() {
        return modifiedContent;
    }
}
