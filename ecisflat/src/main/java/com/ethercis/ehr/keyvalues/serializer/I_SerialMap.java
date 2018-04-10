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

package com.ethercis.ehr.keyvalues.serializer;

import org.openehr.rm.RMObject;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.DvInterval;

import java.util.Map;

/**
 * Created by christian on 5/18/2017.
 */
public interface I_SerialMap {
    Map<String, String> encode();

    Map<String, String> encode(DvInterval dvInterval);

    Map<String, String> encode(DvParsable dvParsable);

    Map<String, String> encode(Participation participation);

    Map<String, String> encode(PartyIdentified partyIdentified);

    Map<String, String> encode(RMObject rmObject);

    Map<String, String> encode(Object object);
}
