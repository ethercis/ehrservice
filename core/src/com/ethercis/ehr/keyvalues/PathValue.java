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

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.ehr.encode.DataValueAdapter;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.*;
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.LocatableHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.openehr.build.SystemValue;
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
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.terminology.SimpleTerminologyService;

import java.util.*;

/**
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

    protected static Logger log = Logger.getLogger(PathValue.class);

    //context attributes
    protected PartyIdentified healthCareFacility = null;
    protected List<Participation> participationList = new ArrayList<>();
    protected DvDateTime startTime = null;
    protected DvDateTime endTime = null;
    protected String location = null;
    protected DvCodedText setting = null;
    protected ItemStructure otherContext = null;
    protected Map<String, String> otherContextMap = new HashMap<>();

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


    public Composition assign(Map<String, String> keyValues) throws Exception {
        Map<String, String> mapContent = new HashMap<>();
        Boolean hasContext = false;
        Boolean hasComposer = false;

        //traverse the queue

        for (String path : keyValues.keySet()) {
            String value = keyValues.get(path);

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
            else if (path.startsWith(CATEGORY_TAG))
                category = parseCategoryAttribute(value);
            else
                log.debug("Passthrough unhandled directive:"+path);
            //identify object class for this path
        }


        //at this stage, all values have been parsed
        EventContext eventContext = null;
        if (hasContext) {
            TerminologyService terminologyService = SimpleTerminologyService.getInstance();
            eventContext = new EventContext(healthCareFacility,
                    (startTime != null) ? startTime : new DvDateTime(DateTime.now().toString()),
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

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(map, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();

        assignItemStructure(CONTENT_TAG, composition, mapContent);

        //2nd build phase: assign other item structures with passed values
        if (composition.getContext().getOtherContext() != null)
            assignItemStructure(OTHER_CONTEXT_TAG, composition.getContext().getOtherContext(), otherContextMap);

        return composition;
    }

    public Boolean update(Composition composition, Map<String, String> keyValues) throws Exception {

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

    public Boolean updateEventContext(EventContext eventContext, Map<String, String> keyValues) throws Exception {

        boolean modified = false;
        boolean doneParticipation = false;
        boolean doneFacility = false;
        List<String> participations = new ArrayList<>();

        for (String path: keyValues.keySet()) {

            if (!path.startsWith(CONTEXT_TAG))
                continue;

            modified = true;

            String value = keyValues.get(path);
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
                    eventContext.setSetting(new DvCodedTextVBean(new DvCodedText("default", "local", "1")).parse(value));
                    break;
                case CTX_LOCATION_TAG:
                    eventContext.setLocation(value);
                    break;
                case CTX_OTHER_CONTEXT_TAG:
                    log.debug("Assign Other context with path:" + path);
//                    path = path.substring(path.indexOf("]")); //should be in the form: /other_context[at0001]/... strip the prefix
                    Map<String, String> valuemap = new HashMap<>();
                    valuemap.put(path, value);
                    assignItemStructure(OTHER_CONTEXT_TAG, eventContext.getOtherContext(), valuemap);
                    break;
                default:
                    log.warn("Unhandled context attribute:" + attribute);
            }
        }
        return modified;
    }

    private boolean updateCompositionAttributes(Composition composition, Map<String, String> keyValues) throws Exception {
        boolean modified = false;

        for (String path : keyValues.keySet()) {
            String value = keyValues.get(path);

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
            } else if (CATEGORY_TAG.equals(path)) {
                modified = true;
                composition.setCategory(parseCategoryAttribute(value));
            }
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

    protected String buildParticipationCode(Map<String, String> pathValues, String baseTag){
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
    private boolean assignContextAttribute(Map<String, String> pathValues){
        List<String> participations = new ArrayList<>();
        Boolean hasContext = false;
        Boolean hasFacility = false;
        Boolean hasParticipation = false;

        for (String path: pathValues.keySet()) {

            if (!path.startsWith(CONTEXT_TAG))
                continue;

            hasContext = true;

            String value = pathValues.get(path);
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

    private void assignEntryAttribute(Map<String, String> pathValues, Entry entry, String path, String attribute, String value){
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

        }
    }

    public boolean assignItemStructure(String filterTag, Locatable locatable, Map<String, String> pathValues) throws Exception {

        boolean modified = false;

        for (final String path: pathValues.keySet()) {

            if (path != null && !path.startsWith(filterTag))
                continue;

            modified = true;

            Integer tagIndex = path.lastIndexOf("/");
            String lastTag = path.substring(tagIndex);
            String attribute = null;
            String attributeKey = null;
            String locatablePath = path;

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

            if (filterTag.startsWith(OTHER_CONTEXT_TAG)){
                locatablePath = path.substring(path.indexOf("]")+1); //should be in the form: /context/other_context[at0001]/... strip the prefix
            }


            Object itemAtPath = locatable.itemAtPath(locatablePath);

            if (itemAtPath == null) {
                log.debug("Item could not be located, cloning required:" + locatablePath);
                LocatableHelper.NodeItem parent = LocatableHelper.backtrackItemAtPath(locatable, locatablePath);
                if (parent != null) {
                    Locatable cloned = LocatableHelper.cloneChildAtPath(parent.getNode(), parent.getChildPath());
                    LocatableHelper.insertChildInList(parent.getNode(), cloned, parent.getInsertionPath());
                }
                itemAtPath = locatable.itemAtPath(locatablePath);

                if (itemAtPath == null)
                    throw new InternalError("Oops, could not clone item at:" + locatablePath);

            }

            if (itemAtPath instanceof ElementWrapper) {
                ElementWrapper elementWrapper = (ElementWrapper) itemAtPath;
                I_VBeanWrapper adapted = elementWrapper.getWrappedValue();
                if (adapted == null) { //check for adapted Element instead
                    Object dataValue = elementWrapper.getAdaptedElement().getValue();
                    if (DataValueAdapter.isValueObject(dataValue)) {
                        if (VBeanUtil.isInstrumentalized(dataValue)) {
                            adapted = (I_VBeanWrapper) VBeanUtil.wrapObject(dataValue);
                            elementWrapper.setWrappedValue(adapted);
                        }
                    }
                }

                DataValue parsed;
                if (adapted instanceof DvCodedTextVBean)
                    parsed = (DataValue) adapted.parse(pathValues.get(path), defaultTerminologyLocal);
                else
                    parsed = (DataValue) adapted.parse(pathValues.get(path));

                elementWrapper.getAdaptedElement().setValue(parsed);

                DataValueConstraints constraints = elementWrapper.getConstraints();

                if (constraints != null && !constraints.validate(parsed)) {
                    String description = constraints.getDescription();
                    String nodeId = elementWrapper.getAdaptedElement().getArchetypeNodeId();
                    String name = elementWrapper.getAdaptedElement().getName().getValue();
                    throw new IllegalArgumentException("Parsed value for [" + nodeId + "," + name + "]" + ", '" + description + "' is not valid, found: " + parsed.toString());
                }
                elementWrapper.setDirtyBit(true);
            } else if (itemAtPath instanceof Activity) {
                assignActivityAttribute((Activity) itemAtPath, attribute, pathValues.get(attributeKey));
            }
            else if (itemAtPath instanceof Entry){
                assignEntryAttribute(pathValues, (Entry) itemAtPath, path, attribute, pathValues.get(attributeKey));
            }
        }
        return modified;
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
