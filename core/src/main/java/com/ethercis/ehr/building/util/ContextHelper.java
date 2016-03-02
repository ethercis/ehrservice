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

import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.joda.time.DateTime;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.terminology.TerminologyService;

/**
 * Created by Christian Chevalley on 4/21/2015.
 */
public class ContextHelper {

    public static EventContext createDummyContext() throws Exception {
//        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), CompositionAttributesHelper.DEMOGRAPHIC, CompositionAttributesHelper.PARTY);
        PartyRef partyRef = new PartyRef(new GenericID("123456-123", "ETHERCIS-SCHEME"), CompositionAttributesHelper.DEMOGRAPHIC, CompositionAttributesHelper.PARTY);
        PartyIdentified healthcareFacility = new PartyIdentified(partyRef, "FACILITY", null);
        DateTime timenow = DateTime.now();
        DvCodedText concept = new DvCodedText("Other Care", "openehr", "238");
        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
        return new org.openehr.rm.composition.EventContext(healthcareFacility, new DvDateTime(timenow.toString()), null, null, "TEST LAB", concept, null, terminologyService);
    }

    public static EventContext createNullContext() throws Exception {
        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), "null", "null");
        PartyIdentified healthcareFacility = new PartyIdentified(partyRef, "null", null);
//        DateTime timenow = DateTime.now();
        DvCodedText concept = new DvCodedText("null", "openehr", "238");
        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
        return new org.openehr.rm.composition.EventContext(healthcareFacility, new DvDateTime(new DateTime(0L).toString()), null, null, null, concept, null, terminologyService);
    }
}
