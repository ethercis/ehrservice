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

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 9/30/2015.
 */
public interface I_PathValue {

    public static final String CONTEXT_TAG = "/context";
    public static final String LANGUAGE_TAG = "/language";
    public static final String TERRITORY_TAG = "/territory";
    public static final String COMPOSER_TAG = "/composer";
    public static final String ORIGIN_TAG = "/origin";

    public static final String IDENTIFIER_PARTY_NAME_SUBTAG = "|name";
    public static final String IDENTIFIER_PARTY_ID_SUBTAG = "|identifier";
    public static final String IDENTIFIER_PARTY_NAMESPACE_SUBTAG = "|namespace";
    public static final String IDENTIFIER_PARTY_SCHEME_SUBTAG = "|scheme";

    public static final String VALUE_SUBTAG = "|value";
    public static final String DEFINING_CODE_SUBTAG = "|defining_code";
    public static final String NAME_SUBTAG = "|name";
    String FORMALISM_SUBTAG = "|formalism";

    public static final String CATEGORY_TAG = "/category";
    public static final String CONTENT_TAG = "/content";
    public static final String UID_TAG = "uid";
    public static final String TIME_TAG = "time";
    public static final String CTX_FACILITY_TAG = "/health_care_facility";

    public static final String CTX_PARTICIPATION_TAG = "/participation";
    public static final String PARTICIPATION_FUNCTION_SUBTAG = "|function";
    public static final String PARTICIPATION_MODE_SUBTAG = "|mode";

    public static final String PARTICIPATION_REGEXP = "/participation(:?[0-9]*)(\\|name|\\|function|\\|identifier|\\|mode)?";
    public static final String FACILITY_REGEXP = "/health_care_facility(\\|name|\\|identifier)";
    public static final String COMPOSER_REGEXP = "/composer(\\|name|\\|identifier)";

    public static final String ATTRIBUTES_REGEXP = "/language|/territory|/composer.*|/category";

    public static final String CTX_START_TIME_TAG = "/start_time";
    public static final String CTX_END_TIME_TAG = "/end_time";
    public static final String CTX_LOCATION_TAG = "/location";
    public static final String CTX_SETTING_TAG = "/setting";
    public static final String CTX_OTHER_CONTEXT_TAG = "/other_context";

    public static final String OTHER_CONTEXT_TAG = CONTEXT_TAG+CTX_OTHER_CONTEXT_TAG;


    //structure specifics
    //entry
    public static final String ENTRY_SUBJECT = "/subject";
    public static final String ENTRY_PROVIDER = "/provider";
    public static final String ENTRY_ENCODING = "/encoding";
    public static final String ENTRY_WORKFLOW_ID = "/workflow_id";
    public static final String ENTRY_PARTICIPATION = "/participation";

    public String[] ENTRY_TAGS = {ENTRY_SUBJECT, ENTRY_PROVIDER, ENTRY_ENCODING, ENTRY_WORKFLOW_ID, ENTRY_PARTICIPATION};

    //Instruction
    public static final String INSTRUCTION_NARRATIVE = "/narrative";
    public static final String INSTRUCTION_EXPIRY_TIME = "/expiry_time";
    public static final String INSTRUCTION_GUIDELINE_ID = "/guideline";
    public static final String INSTRUCTION_WFDEFINITION = "/wf_definition";

    public static final String[] INSTRUCTION_TAGS = {INSTRUCTION_NARRATIVE, INSTRUCTION_EXPIRY_TIME, INSTRUCTION_GUIDELINE_ID, INSTRUCTION_WFDEFINITION};

    //Activity
    public static final String ACTIVITY_TIMING = "/timing";
    public static final String ACTIVITY_ARCHETYPE_ID = "/action_archetype_id";

    public static final String[] ACTIVITY_TAGS = {ACTIVITY_TIMING, ACTIVITY_ARCHETYPE_ID};

    public static final String CONCEPT_TERMINOLOGY_PROP = "default.terminology.concept";
    public static final String TERRITORY_TERMINOLOGY_PROP = "default.terminology.territory";
    public static final String LANGUAGE_TERMINOLOGY_PROP = "default.terminology.language";
    public static final String LOCAL_TERMINOLOGY_PROP = "default.terminology.local";
    public static final String PARTY_NAMESPACE_PROP = "default.party.namespace";
    public static final String PARTY_SCHEME_PROP = "default.party.scheme";
    public static final String PARTY_CATEGORY_PROP = "default.party.category";

    public static final String DEFAULT_LOCAL_TERMINOLOGY = "local";
    public static final String DEFAULT_TERRITORY_TERMINOLOGY = "ISO_3166-1";
    public static final String DEFAULT_LANGUAGE_TERMINOLOGY = "ISO_639-1";
    public static final String DEFAULT_PARTY_NAME_SPACE = "NHS-UK";
    public static final String DEFAULT_PARTY_SCHEME = "2.16.840.1.113883.2.1.4.3";
    public static final String DEFAULT_PARTY_CATEGORY = "ANY";


    boolean isModifiedContext();
    boolean isModifiedAttributes();
    boolean isModifiedContent();

}
