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

import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.openehr.am.template.TermMap;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartySelf;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.measurement.MeasurementService;
import org.openehr.rm.support.measurement.SimpleMeasurementService;
import org.openehr.rm.support.terminology.TerminologyAccess;
import org.openehr.rm.support.terminology.TerminologyService;

import java.util.HashMap;
import java.util.Map;

/**
 * Refactoring of DomainBuilder and OptBinding<br>
 * Abstract class holding common constants, fields and methods to both RM composition generators
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/22/2015.
 */
public abstract class RmBinding implements I_RmBinding {

    //field identifiers
    protected static final String VALUE = "value";
    protected static final String NULL_FLAVOUR = "null_flavour";
    protected static final String NARRATIVE = "narrative";
    protected static final String DEFINING_CODE = "defining_code";
    protected static final String ORIGIN = "origin";
    protected static final String TIME = "time";
    protected static final String TIMING = "timing";
    protected static final String START_TIME = "start_time";
    protected static final String SETTING = "setting";
    protected static final String DESCRIPTION = "description";
    protected static final String ISM_TRANSITION = "ism_transition";
    protected static final String MAGNITUDE = "magnitude";
    protected static final String TEMPLATE_ID = "template_id";
    protected static final String ACTION_ARCHETYPE_ID = "action_archetype_id";
    protected static final String FORMALISM = "formalism";
    protected static final String CHOICE = "$choice$";
    protected static final String ANY = "$any$";

    //used for INTERVAL_EVENT
    protected static final String DATA="data";
    protected static final String ARCHETYPE_NODE_ID="archetype_node_id";
    protected static final String NAME="name";
    protected static final String MATH_FUNCTION="math_function";
    protected static final String STATE="state";
    protected static final String WIDTH="width";

    //ISM_TRANSITION
    protected static final String CAREFLOW_STEP="careflow_step";
    protected static final String CURRENT_STATE="current_state";

    // default values
    protected static final String ID = "id";
    protected static final String ASSIGNER = "assigner";
    protected static final String ISSUER = "issuer";
    protected static final String TYPE = "type";

    public static final String DEFAULT_DATE = "1900-01-01";
    public static final String DEFAULT_TIME = "00:00:00";
    public static final String DEFAULT_DATE_TIME = "1900-01-01T00:00:00Z";
    public static final String DEFAULT_DURATION = "PT0S";
    public static final String DEFAULT_TEXT = "DEFAULT_TEXT_VALUE";
    public static final String DEFAULT_CODED_TEXT = "DEFAULT_CODED_TEXT_VALUE";
    public static final String DEFAULT_COUNT = "1";
    public static final String DEFAULT_URI = "http://www.DEFAULTURI.com/";
    public static final String DEFAULT_ID = "DEFAULT_ID";
    public static final String DEFAULT_ISSUER = "DEFAULT_ISSUER";
    public static final String DEFAULT_ASSIGNER = "DEFAULT_ASSIGNER";
    public static final String DEFAULT_TYPE = "DEFAULT_TYPE";
    public static final String DEFAULT_NARRATIVE = "DEFAULT_NARRATIVE";
    public static final String DEFAULT_TIMING_SCHEME = "DEFAULT_TIMING";
    public static final String DEFAULT_TIMING_FORMALISM = "DEFAULT_FORMALISM";
    public static final String DEFAULT_ACTION_ARCHETYPE_ID = "/.*/";
    public static final String DEFAULT_DESCRIPTION_NAME = "DEFAULT_DESCRIPTION_STUCT";
    public static final String DEFAULT_NODE_ID = "at0000";
    public static final String DEFAULT_CAREFLOW_STEP = "DEFAULT_CAREFLOW_STEP";

    protected Map<SystemValue,Object> systemValues = new HashMap<>();
    protected MeasurementService measurementService;
    protected TerminologyService terminologyService;
    protected TerminologyAccess openEHRTerminology;
    protected RMObjectBuilder builder;
    protected TermMap termMap;

    protected RmBinding(Map<SystemValue, Object> map) throws Exception {
        if (map != null) {
            DefaultedMap<SystemValue, Object> parameters = new DefaultedMap<>();
            parameters.putAll(map);
            systemValues.put(SystemValue.MEASUREMENT_SERVICE, parameters.get(SystemValue.MEASUREMENT_SERVICE, SimpleMeasurementService.getInstance()));
            systemValues.put(SystemValue.TERMINOLOGY_SERVICE, parameters.get(SystemValue.TERMINOLOGY_SERVICE, TerminologyServiceWrapper.getInstance()));
            terminologyService = (TerminologyService) systemValues.get(SystemValue.TERMINOLOGY_SERVICE);
            measurementService = (MeasurementService) systemValues.get(SystemValue.MEASUREMENT_SERVICE);
            openEHRTerminology = terminologyService.terminology(TerminologyService.OPENEHR);
            systemValues.put(SystemValue.LANGUAGE, parameters.get(SystemValue.LANGUAGE, I_RmBinding.makeCodePhrase(DEFAULT_LANGUAGE)));
            systemValues.put(SystemValue.ENCODING, parameters.get(SystemValue.ENCODING, I_RmBinding.makeCodePhrase(DEFAULT_ENCODING)));
            systemValues.put(SystemValue.CHARSET, parameters.get(SystemValue.CHARSET, I_RmBinding.makeCodePhrase(DEFAULT_CHARSET)));
            systemValues.put(SystemValue.COMPOSER, parameters.get(SystemValue.COMPOSER, I_RmBinding.makePartyIdentified(DEFAULT_COMPOSER)));
            systemValues.put(SystemValue.TERRITORY, parameters.get(SystemValue.TERRITORY, I_RmBinding.makeCodePhrase(DEFAULT_TERRITORY)));
            systemValues.put(SystemValue.CATEGORY, parameters.get(SystemValue.CATEGORY, I_RmBinding.makeDefaultCodedText(DEFAULT_CATEGORY))); //care entry
            systemValues.put(SystemValue.SUBJECT, parameters.get(SystemValue.SUBJECT, DEFAULT_SUBJECT));
            systemValues.put(SystemValue.PROVIDER, parameters.get(SystemValue.PROVIDER, DEFAULT_PROVIDER));
            systemValues.put(SystemValue.CONTEXT, parameters.get(SystemValue.CONTEXT, I_RmBinding.makeDefaultEventContext()));
            systemValues.put(SystemValue.UID, parameters.get(SystemValue.UID, null));
        } else { //use defaults
            measurementService = SimpleMeasurementService.getInstance();
            systemValues.put(SystemValue.MEASUREMENT_SERVICE, measurementService);
            terminologyService = TerminologyServiceWrapper.getInstance();
            openEHRTerminology = terminologyService.terminology(TerminologyService.OPENEHR);
            systemValues.put(SystemValue.TERMINOLOGY_SERVICE, terminologyService);
            systemValues.put(SystemValue.LANGUAGE, I_RmBinding.makeCodePhrase(DEFAULT_LANGUAGE));
            systemValues.put(SystemValue.ENCODING, I_RmBinding.makeCodePhrase(DEFAULT_ENCODING));
            systemValues.put(SystemValue.CHARSET, I_RmBinding.makeCodePhrase(DEFAULT_CHARSET));
            systemValues.put(SystemValue.TERRITORY, I_RmBinding.makeCodePhrase(DEFAULT_TERRITORY));

        }
        builder = RMObjectBuilder.getInstance(systemValues);
        termMap = new TermMap();
    }

    /**
     * sets the valueMap parameters for a Composition from SystemValues if any
     * @param map
     * @return
     */
    protected void setCompositionValues(Map<String, Object> map){
        if (systemValues.isEmpty())
            return ;

        if (!map.containsKey(SystemValue.CATEGORY.id()) && systemValues.containsKey(SystemValue.CATEGORY))
            map.put(SystemValue.CATEGORY.id(), systemValues.get(SystemValue.CATEGORY));
        else if (!map.containsKey(SystemValue.CATEGORY.id()))
            map.put(SystemValue.CATEGORY.id(), new DvCodedText("event", new CodePhrase("openehr", "433")));

        if (!map.containsKey(SystemValue.TERRITORY.id()) && systemValues.containsKey(SystemValue.TERRITORY))
            map.put(SystemValue.TERRITORY.id(), systemValues.get(SystemValue.TERRITORY));
        else if (!map.containsKey(SystemValue.TERRITORY.id()))
            map.put(SystemValue.TERRITORY.id(), I_RmBinding.makeCodePhrase(DEFAULT_TERRITORY));

        if (!map.containsKey(SystemValue.COMPOSER.id()) && systemValues.containsKey(SystemValue.COMPOSER))
            map.put(SystemValue.COMPOSER.id(), systemValues.get(SystemValue.COMPOSER));
        else if (!map.containsKey(SystemValue.COMPOSER.id()))
            map.put(SystemValue.COMPOSER.id(), PartyIdentified.named("aComposer"));

        if (!map.containsKey(SystemValue.LANGUAGE.id()) && systemValues.containsKey(SystemValue.LANGUAGE))
            map.put(SystemValue.LANGUAGE.id(), systemValues.get(SystemValue.LANGUAGE));
        else if (!map.containsKey(SystemValue.LANGUAGE.id()))
            map.put(SystemValue.LANGUAGE.id(), I_RmBinding.makeCodePhrase(DEFAULT_LANGUAGE));

        //next are non required parameters
        if (!map.containsKey(SystemValue.CHARSET.id()) && systemValues.containsKey(SystemValue.CHARSET))
            map.put(SystemValue.CHARSET.id(), systemValues.get(SystemValue.CHARSET));

        if (!map.containsKey(SystemValue.TERMINOLOGY_SERVICE.id()) && systemValues.containsKey(SystemValue.TERMINOLOGY_SERVICE))
            map.put(SystemValue.TERMINOLOGY_SERVICE.id(), systemValues.get(SystemValue.TERMINOLOGY_SERVICE));

        if (!map.containsKey(SystemValue.MEASUREMENT_SERVICE.id()) && systemValues.containsKey(SystemValue.MEASUREMENT_SERVICE))
            map.put(SystemValue.MEASUREMENT_SERVICE.id(), systemValues.get(SystemValue.MEASUREMENT_SERVICE));

        if (!map.containsKey(SystemValue.SUBJECT.id()) && systemValues.containsKey(SystemValue.SUBJECT))
            map.put(SystemValue.SUBJECT.id(), systemValues.get(SystemValue.SUBJECT));

        if (!map.containsKey(SystemValue.PROVIDER.id()) && systemValues.containsKey(SystemValue.PROVIDER))
            map.put(SystemValue.PROVIDER.id(), systemValues.get(SystemValue.PROVIDER));

//        if (!map.containsKey(SystemValue.CONTEXT.id()) && systemValues.containsKey(SystemValue.CONTEXT)) {
        if (systemValues.containsKey(SystemValue.CONTEXT)) { //otherwise use the default context
            EventContext suppliedEventContext = (EventContext)systemValues.get(SystemValue.CONTEXT);
            if (map.containsKey(SystemValue.CONTEXT.id())) {
                EventContext originalEventContext = (EventContext) map.get(SystemValue.CONTEXT.id());
                suppliedEventContext.setOtherContext(originalEventContext.getOtherContext());
            }
            map.put(SystemValue.CONTEXT.id(), suppliedEventContext);
        }

        if (!map.containsKey(SystemValue.UID.id()) && systemValues.containsKey(SystemValue.UID))
            map.put(SystemValue.UID.id(), systemValues.get(SystemValue.UID));
    }

    /*
 * Checks if given code_phrase is locally defined within an archetype
 */
    protected boolean isLocallyDefined(CodePhrase code) {
        return "local".equalsIgnoreCase(code.getTerminologyId().toString())
                && code.getCodeString().startsWith("at");
    }

    protected boolean isOpenEHRTerm(CodePhrase code) {
        return "openehr".equalsIgnoreCase(code.getTerminologyId().getValue());
    }

    // test subject
    protected PartySelf subject() throws Exception {
//        PartyRef party = new PartyRef(new HierObjectID("1.2.4.5.6.12.1"), "NHS-UK", "PARTY");
//        return new PartySelf(party);
        return new PartySelf();
    }

    // test provider
    protected PartyIdentified provider() throws Exception {
        PartyRef performer = new PartyRef(new HierObjectID("1.3.3.1"), "NHS-UK", "ORGANISATION");
        return new PartyIdentified(performer, "provider's name", null);
    }


}
