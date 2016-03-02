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
import com.ethercis.ehr.util.MapInspector;
import org.apache.commons.collections4.iterators.PeekingIterator;
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



    public static Map<String, String> renderFlat(Composition composition) throws Exception {
        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(composition);

        return generateEcisFlat(composition, retmap);
    }

    public static Map<String, String> renderFlat(Locatable locatable) throws Exception {
        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.processItem(locatable);

        return generateEcisFlat(retmap);
    }

    public static Map<String, String> renderFlat(Locatable locatable, boolean allElements, CompositionSerializer.WalkerOutputMode mode) throws Exception {
        CompositionSerializer inspector = new CompositionSerializer(mode, allElements);
        Map<String, Object>retmap = inspector.processItem(locatable);

        return generateEcisFlat(retmap);
    }

    public static Map<String, String> renderFlat(Composition composition, boolean allElements, CompositionSerializer.WalkerOutputMode mode) throws Exception {
        CompositionSerializer inspector = new CompositionSerializer(mode, allElements);
        Map<String, Object>retmap = inspector.process(composition);

        return generateEcisFlat(composition, retmap);
    }

    private static boolean isMetaData(String path){
        return path.endsWith(CompositionSerializer.TAG_TIME) || path.endsWith(CompositionSerializer.TAG_ORIGIN) || path.endsWith(CompositionSerializer.TAG_TIMING);
    }

    private static String resolveMetaDataSuffix(String path){
        String suffix = null;
        if (path.endsWith(CompositionSerializer.TAG_TIME))
            suffix = CompositionSerializer.TAG_TIME;
        else if (path.endsWith(CompositionSerializer.TAG_ORIGIN))
            suffix = CompositionSerializer.TAG_ORIGIN;
        else if (path.endsWith(CompositionSerializer.TAG_TIMING))
            suffix = CompositionSerializer.TAG_TIMING;

        return suffix;
    }

    /**
     * eliminate all meta-data entries not related to an actual data value entry
     * @param sortedMap
     * @return
     */
    private static Map<String,String> vacuum(Map<String, String> sortedMap){
        PeekingIterator<String> peekingIterator = new PeekingIterator(sortedMap.keySet().iterator());

        String previousPath = null;
        while (peekingIterator.hasNext()) {
            String path = peekingIterator.next();
            if (isMetaData(path)) { //check if next entry is an actual child of this node
                String suffix = resolveMetaDataSuffix(path);
                if (suffix != null) {
                    String pathChildTest = path.substring(0, path.lastIndexOf(suffix));
                    //do not keep this entry since it must be preceded by an actual child path holding a value field (which we cannot assert btw)
                    if (isMetaData(previousPath) || !previousPath.startsWith(pathChildTest))
                        peekingIterator.remove();
                }
            }
            previousPath = path;
        }
        return sortedMap;
    }

    private static Map<String, String> generateEcisFlat(Composition composition, Map<String, Object> compositionMap) throws Exception {
        MapInspector mapInspector = new MapInspector();
        mapInspector.inspect(compositionMap);
        Map<String, String> flatten = mapInspector.getStackFlatten();

        //add context data
        EventContext eventContext = composition.getContext();
        String baseTag = I_PathValue.CONTEXT_TAG;
        if (eventContext.getHealthCareFacility() != null) {
            flatten.put(baseTag + I_PathValue.CTX_FACILITY_TAG + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, eventContext.getHealthCareFacility().getName());
            flatten.put(baseTag + I_PathValue.CTX_FACILITY_TAG + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, eventContext.getHealthCareFacility().getExternalRef().getId().getValue());
        }
        if (eventContext.getStartTime() != null)
            flatten.put(baseTag+I_PathValue.CTX_START_TIME_TAG, eventContext.getStartTime().getValue());

        if (eventContext.getEndTime() != null)
            flatten.put(baseTag+I_PathValue.CTX_END_TIME_TAG, eventContext.getEndTime().getValue());

        int index = 0;

        if (eventContext.getParticipations() != null) {
            for (Participation participation : eventContext.getParticipations()) {
                flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.PARTICIPATION_FUNCTION_SUBTAG, participation.getFunction().getValue());
                PartyIdentified partyIdentified = (PartyIdentified)participation.getPerformer();
                flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, partyIdentified.getName());
                flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, participation.getPerformer().getExternalRef().getId().getValue());
                flatten.put(baseTag + I_PathValue.ENTRY_PARTICIPATION + ":" + index + I_PathValue.PARTICIPATION_MODE_SUBTAG, participation.getMode().toString());
                index++;
            }
        }

        if (eventContext.getOtherContext() != null){
            CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
            Map<String, Object>retmap = inspector.processItem(eventContext.getOtherContext());
            mapInspector.inspect(retmap, true); //clear the stack before inspecting
            Map<String, String> otherContextMapFlat = mapInspector.getStackFlatten();
            for (String path: otherContextMapFlat.keySet()){
                //we strip the 'items[at0001]' part from the path...
                flatten.put(I_PathValue.OTHER_CONTEXT_TAG+"[at0001]"+path.substring(path.indexOf("]")+1), otherContextMapFlat.get(path));
            }
        }

        if (eventContext.getLocation()!= null)
            flatten.put(baseTag + I_PathValue.CTX_LOCATION_TAG, eventContext.getLocation());
        if (eventContext.getSetting() != null)
            flatten.put(baseTag + I_PathValue.CTX_SETTING_TAG, eventContext.getSetting().toString());

        if (composition.getComposer() != null) {
            PartyIdentified composer = (PartyIdentified)composition.getComposer();
            flatten.put(I_PathValue.COMPOSER_TAG + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, composer.getName());
            if (composer.getExternalRef() != null)
                flatten.put(I_PathValue.COMPOSER_TAG + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, composer.getExternalRef().getId().getValue());
        }

        if (composition.getCategory() != null)
            flatten.put(I_PathValue.CATEGORY_TAG, composition.getCategory().getValue());
        if (composition.getTerritory() != null)
            flatten.put(I_PathValue.TERRITORY_TAG, composition.getTerritory().getCodeString());
        if (composition.getLanguage()!= null)
            flatten.put(I_PathValue.LANGUAGE_TAG, composition.getLanguage().getCodeString());

        return vacuum(flatten);
//        return flatten;

    }

    private static Map<String, String> generateEcisFlat(Map<String, Object> locatableMap) throws Exception {
        MapInspector mapInspector = new MapInspector();
        mapInspector.inspect(locatableMap);
        Map<String, String> flatten = mapInspector.getStackFlatten();

        return vacuum(flatten);
//        return flatten;
    }
}
