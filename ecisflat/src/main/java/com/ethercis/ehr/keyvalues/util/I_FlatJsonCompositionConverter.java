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

package com.ethercis.ehr.keyvalues.util;


import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.util.Map;

/**
 * Encapsulate flat json to/from composition conversion using ThinkEhr library
 * Created by christian on 2/25/2016.
 */
public interface I_FlatJsonCompositionConverter {
    Composition toComposition(String templateId, Map<String, Object> flatJsonMap) throws Exception;

    Composition toComposition(OPERATIONALTEMPLATE operationaltemplate, Map<String, Object> flatJsonMap) throws Exception;

    Map<String, Object> fromComposition(String templateId, Composition composition) throws Exception;

    Map<String, Object> fromComposition(String templateId, Composition composition, boolean allElements) throws Exception;

    Map<String, Object> fromComposition(OPERATIONALTEMPLATE operationaltemplate, Composition composition)  throws Exception;

    Map<String, Object> fromComposition(OPERATIONALTEMPLATE operationaltemplate, Composition composition, boolean allElements)  throws Exception;

    Map<String, Integer> getItemArrayPathMap();
}
