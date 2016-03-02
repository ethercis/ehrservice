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
package com.ethercis.ehr.building.util;

import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;

import java.util.ArrayList;
import java.util.List;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 10/7/2015.
 */
public class CompositionAttributesHelper {

    public static final String DEMOGRAPHIC = "DEMOGRAPHIC";
    public static final String PARTY = "PARTY";

    public static PartyIdentified createComposer(String name, String issuer, String idcode){
//        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), DEMOGRAPHIC, PARTY);
        PartyRef partyRef = new PartyRef(new GenericID(idcode, "ETHERCIS-TEST"), DEMOGRAPHIC, PARTY);
        List<DvIdentifier> identifiers = new ArrayList<>();
        identifiers.add(new DvIdentifier(issuer, "dummy", idcode, "dummy"));
        return new PartyIdentified(partyRef, name, identifiers);
    }
}
