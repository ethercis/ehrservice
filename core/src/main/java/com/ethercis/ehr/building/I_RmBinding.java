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
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.terminology.TerminologyService;

import java.util.HashMap;
import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/17/2015.
 */
public interface I_RmBinding {


    //see https://github.com/handihealth/c4h_lcr_answer/blob/master/docs/leeds/Leeds_tech_tasks.md for usage
    static final String DEFAULT_SUBJECT = "99999-0::$SYSTEM$"; //subject ID :: Namespace
    static final String DEFAULT_COMPOSER = "99999-1::$SYSTEM$"; //composer ID :: Namespace
    static final String DEFAULT_FACILITY = "99999-2::$SYSTEM$"; //facility ID :: Namespace
    static final String DEFAULT_PROVIDER = "99999-3::$SYSTEM$"; //facility ID :: Namespace
    static final String DEFAULT_LANGUAGE = "ISO_639-1::en";
    static final String DEFAULT_TERRITORY = "ISO_3166-1::GB";
    static final String DEFAULT_ENCODING = "IANA_character-sets::UTF-8";
    static final String DEFAULT_CHARSET = "IANA_character-sets::UTF-8";
    static final Integer DEFAULT_CATEGORY = 433;

    class DefaultedMap<K, V> extends HashMap<K, V> {
        public Object get(Object key, Object defaultValue) {
            if (!containsKey(key))
                return defaultValue;
            return super.get(key);
        }
    }

    public static CodePhrase makeTerritoryCodePhrase(String territory2letters){
        return new CodePhrase("ISO_3166-1", territory2letters);
    }

    public static CodePhrase makeEncodingCodePhrase(String code){
        return new CodePhrase("IANA_character-sets", code);
    }

    public static CodePhrase makeLanguageCodePhrase(String languageCode){
        return new CodePhrase("ISO_639-1", languageCode);
    }


    /**
     * Factory method to get instance of the RM skeleton generator
     *
     * @return
     */
    public static OetBinding getInstance() throws Exception {
        return new OetBinding(null);
    }

    public static OetBinding getInstance(Map<SystemValue, Object> parameters) throws Exception {
        return new OetBinding(parameters);
    }

    public static CodePhrase makeCodePhrase(String codedString){
        String[] parts = StringUtils.split(codedString, "::");
        return new CodePhrase(parts[0], parts[1]);
    }

    public static PartyIdentified makePartyIdentified(String partyDef){
        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), "NHS-UK",  "party");
        PartyIdentified party = new PartyIdentified(partyRef, partyDef, null);
        return party;
    }

    /**
     * build a default Event Context
     * @return
     */
    public static EventContext makeDefaultEventContext() throws Exception {
        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), "NHS-UK",  "party");
        org.openehr.rm.common.generic.PartyIdentified healthcareFacility = new org.openehr.rm.common.generic.PartyIdentified(partyRef, DEFAULT_FACILITY, null);
        DateTime timenow = new DateTime(0L);
        DvCodedText concept = new DvCodedText("other care", new CodePhrase("openehr", "238"));
        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
        return new EventContext(healthcareFacility, new DvDateTime(timenow.toString()), null, null, "$SYSTEM$", concept, null, terminologyService);
    }

    public static DvCodedText makeDefaultCodedText(Integer code){
        return new DvCodedText("event", new CodePhrase("openehr", code.toString()));
    }


    static final DvCodedText NULL_FLAVOUR_VALUE = new DvCodedText("no information", new CodePhrase(TerminologyService.OPENEHR, "271"));
}
