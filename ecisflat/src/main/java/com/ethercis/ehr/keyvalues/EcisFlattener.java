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

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.util.MapInspector;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;

import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/5/2015.
 */
public class EcisFlattener implements I_EcisFlattener {


    private final Boolean allElements;
    CompositionSerializer.WalkerOutputMode mode;

    public EcisFlattener(Boolean allElements){
        this.allElements = allElements;
        this.mode = CompositionSerializer.WalkerOutputMode.PATH;
    }

    public EcisFlattener(Boolean allElements, CompositionSerializer.WalkerOutputMode mode){
        this.allElements = allElements;
        this.mode = mode;
    }


    public EcisFlattener(){
        this.allElements = false;
        this.mode = CompositionSerializer.WalkerOutputMode.PATH;
    }

    @Override
    public Map<String, String> render(Composition composition) throws Exception {
        I_CompositionSerializer inspector;

        if (allElements)
            inspector = I_CompositionSerializer.getInstance(mode, allElements);
        else
            inspector = I_CompositionSerializer.getInstance(mode);
        Map<String, Object>retmap = inspector.process(composition);

        return generateEcisFlat(composition, retmap);
    }

    @Override
    public Map<String, String> render(Locatable locatable) throws Exception {
        I_CompositionSerializer inspector;

        if (allElements)
            inspector = I_CompositionSerializer.getInstance(mode, allElements);
        else
            inspector = I_CompositionSerializer.getInstance(mode);

        Map<String, Object>retmap = inspector.processItem(locatable);

        return generateEcisFlat(retmap);
    }


    private Map<String, String> generateEcisFlat(Composition composition, Map<String, Object> compositionMap) throws Exception {
        MapInspector mapInspector = new MapInspector();
        mapInspector.inspect(compositionMap);
        Map<String, String> flatten = mapInspector.getStackFlatten();

        //add context data
        EventContext eventContext = composition.getContext();
        if (eventContext != null) {
            String baseTag = I_PathValue.CONTEXT_TAG;
            if (eventContext.getHealthCareFacility() != null) {
                flatten.put(baseTag + I_PathValue.CTX_FACILITY_TAG + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, eventContext.getHealthCareFacility().getName());
                flatten.put(baseTag + I_PathValue.CTX_FACILITY_TAG + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, eventContext.getHealthCareFacility().getExternalRef().getId().getValue());
            }
            if (eventContext.getStartTime() != null)
                flatten.put(baseTag + I_PathValue.CTX_START_TIME_TAG, eventContext.getStartTime().getValue());

            if (eventContext.getEndTime() != null)
                flatten.put(baseTag + I_PathValue.CTX_END_TIME_TAG, eventContext.getEndTime().getValue());

            int index = 0;

            if (eventContext.getParticipations() != null) {
                for (Participation participation : eventContext.getParticipations()) {
                    flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.PARTICIPATION_FUNCTION_SUBTAG, participation.getFunction().getValue());
                    PartyIdentified partyIdentified = (PartyIdentified) participation.getPerformer();
                    flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, partyIdentified.getName());
                    flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, participation.getPerformer().getExternalRef().getId().getValue());
                    flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.PARTICIPATION_MODE_SUBTAG, participation.getMode().toString());
                    index++;
                }
            }

            if (eventContext.getOtherContext() != null) {
                I_CompositionSerializer inspector = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.PATH, allElements);
                Map<String, Object> retmap = inspector.processItem(eventContext.getOtherContext());
                mapInspector.inspect(retmap, true); //clear the stack before inspecting
                Map<String, String> otherContextMapFlat = mapInspector.getStackFlatten();
                for (String path : otherContextMapFlat.keySet()) {
                    //we strip the 'items[at0001]' part from the path...
                    flatten.put(I_PathValue.OTHER_CONTEXT_TAG + "[at0001]" + path.substring(path.indexOf("]") + 1), otherContextMapFlat.get(path));
                }
            }

            if (eventContext.getLocation() != null)
                flatten.put(baseTag + I_PathValue.CTX_LOCATION_TAG, eventContext.getLocation());
            if (eventContext.getSetting() != null)
                flatten.put(baseTag + I_PathValue.CTX_SETTING_TAG, eventContext.getSetting().toString());
        }
        if (composition.getComposer() != null) {
            PartyIdentified composer = (PartyIdentified)composition.getComposer();
            flatten.put(I_PathValue.COMPOSER_TAG + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, composer.getName());
            if (composer.getExternalRef() != null)
                flatten.put(I_PathValue.COMPOSER_TAG + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, composer.getExternalRef().getId().getValue());
        }

//        if (composition.getCategory() != null)
//            flatten.put(I_PathValue.CATEGORY_TAG, composition.getCategory().getValue());
        if (composition.getTerritory() != null)
            flatten.put(I_PathValue.TERRITORY_TAG, composition.getTerritory().getCodeString());
        if (composition.getLanguage()!= null)
            flatten.put(I_PathValue.LANGUAGE_TAG, composition.getLanguage().getCodeString());

        return new Vacuum().metaClean(flatten);
//        return flatten;

    }

    @Override
    public Map<String, String> generateEcisFlat(Map<String, Object> locatableMap) throws Exception {
        MapInspector mapInspector = new MapInspector();
        mapInspector.inspect(locatableMap);
        Map<String, String> flatten = mapInspector.getStackFlatten();

        return new Vacuum().metaClean(flatten);
//        return flatten;
    }
}
