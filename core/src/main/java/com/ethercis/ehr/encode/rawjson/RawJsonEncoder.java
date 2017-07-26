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

package com.ethercis.ehr.encode.rawjson;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 2/17/2017.
 */
public class RawJsonEncoder {

    private final I_KnowledgeCache knowledge;
    private String lenient;

    public RawJsonEncoder(I_KnowledgeCache knowledgeCache){
        this.knowledge = knowledgeCache;
    }

    //utility to ensure a true AQL path.
    //eliminate /events and /activities from the list since it used only to contain arrays of their respective categories
    private String pathNoDuplicates(String pathSegments){
        List<String> segments = new ArrayList<>();
        segments.addAll(Arrays.asList(pathSegments.split("/")));

//        return pathSegments;
        if (segments.contains(CompositionSerializer.TAG_EVENTS.substring(1)))
            segments.remove(CompositionSerializer.TAG_EVENTS.substring(1));
        if (segments.contains(CompositionSerializer.TAG_ACTIVITIES.substring(1)))
            segments.remove(CompositionSerializer.TAG_ACTIVITIES.substring(1));

        return StringUtils.join(segments.toArray(new String[]{}), "/");
    }

    private Locatable preProcess(String templateId, String jsonDbEncoded, String path) throws Exception {
        lenient = System.getProperty("validation.lenient");
        System.setProperty("validation.lenient", "true");
        I_ContentBuilder content = I_ContentBuilder.getInstance(knowledge, templateId);

        //build a composition with the retrieved values

        Composition newComposition = content.buildCompositionFromJson(jsonDbEncoded);
        Locatable item = (Locatable)newComposition.itemAtPath(pathNoDuplicates(path));

        return item;
    }

    private void postProcess(){
        if (lenient == null)
            System.setProperty("validation.lenient", "false");
        else
            System.setProperty("validation.lenient", lenient);
    }

    public String encode(String templateId, String jsonDbEncoded, String path) throws Exception {
        Locatable item = preProcess(templateId, jsonDbEncoded, path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        String stringMap = rawCompositionSerializer.dbEncode("fragment", item);

        postProcess();

        return stringMap;
    }

    public String encode(String tag, String templateId, String jsonDbEncoded, String path) throws Exception {
        Locatable item = preProcess(templateId, jsonDbEncoded, path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        String stringMap = rawCompositionSerializer.dbEncode(tag, item);

        postProcess();


        return stringMap;
    }

    public String encodeContent(String tag, String templateId, String jsonDbEncoded, String path) throws Exception {
        Locatable item = preProcess(templateId, jsonDbEncoded, path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        String stringMap = rawCompositionSerializer.dbEncodeContent(tag, item);

        postProcess();


        return stringMap;
    }

    public String encodeContentAsMap(String tag, String templateId, String jsonDbEncoded, String path) throws Exception {
        Locatable item = preProcess(templateId, jsonDbEncoded, path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        String stringMap = rawCompositionSerializer.dbEncodeContent(tag, item);

        postProcess();

        //hack the name/value array to be a valid name/value
        stringMap = new NameValueEncoding(stringMap).make();

        return stringMap;
    }

    public Map<String, Object> encodeAsMap(String tag, String templateId, String jsonDbEncoded, String path) throws Exception {
        Locatable item = preProcess(templateId, jsonDbEncoded, path);

        //encode it as a RAW json
        I_CompositionSerializer rawCompositionSerializer = I_CompositionSerializer.getInstance(CompositionSerializer.WalkerOutputMode.RAW);
        Map<String, Object> map = rawCompositionSerializer.dbEncodeAsMap(tag, item);

        postProcess();


        return map;
    }
}
