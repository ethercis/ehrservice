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

package com.ethercis.ehr.encode;

import com.ethercis.ehr.building.I_ContentBuilder;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;

import java.util.Map;

/**
 * Created by christian on 10/5/2016.
 */
public interface I_CompositionSerializer {

    static I_CompositionSerializer getInstance() throws IllegalAccessException {
        //default mode PG_JSONB encoding
        return new CompositionSerializer();
    }

    static I_CompositionSerializer getInstance(CompositionSerializer.WalkerOutputMode mode, boolean allElements) throws IllegalAccessException {
        if (mode == CompositionSerializer.WalkerOutputMode.RAW)
            return new CompositionSerializerRawJson(allElements);
        else
            return new CompositionSerializer(mode, allElements);
    }

    static I_CompositionSerializer getInstance(CompositionSerializer.WalkerOutputMode mode) throws IllegalAccessException {
        if (mode == CompositionSerializer.WalkerOutputMode.RAW)
            return new CompositionSerializerRawJson(false);
        else
            return new CompositionSerializer(mode);
    }

//    static void resetTags() throws IllegalAccessException {
//        new CompositionSerializer(); //use the constructor to reset all tags to JSONB encoding
//    }

    Map<String, Object> process(Composition composition) throws Exception;

    Map<String, Object> processItem(Locatable locatable) throws Exception;

    Map<String, Object> processItem(String tag, Locatable locatable) throws Exception;

    Map<String, String> getLtreeMap();

    String getTreeRootArchetype();

    String getTreeRootClass();

    String dbEncode(Locatable locatable) throws Exception;

    String dbEncode(Composition composition) throws Exception;

    String dbEncode(String tag, Locatable locatable) throws Exception;

    String dbEncodeContent(String tag, Locatable locatable) throws Exception;

    Map<String, Object> dbEncodeAsMap(String tag, Locatable locatable) throws Exception;

    Object dbEncodeContentAsMap(String tag, Locatable locatable) throws Exception;
}
