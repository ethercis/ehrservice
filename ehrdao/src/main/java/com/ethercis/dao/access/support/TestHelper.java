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
package com.ethercis.dao.access.support;

import com.ethercis.dao.access.interfaces.*;
import com.ethercis.ehr.building.util.CompositionAttributesHelper;
import com.ethercis.ehr.building.util.ContextHelper;
import com.ethercis.dao.access.util.ContributionDef;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.terminology.TerminologyService;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for test purpose
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/15/2015.
 */
public class TestHelper {

    public static UUID createDummySystem(I_DomainAccess testDomainAccess) throws Exception {
        I_SystemAccess systemAccess = I_SystemAccess.getInstance(testDomainAccess, "DUMMY SYSTEM FOR TEST", "System_" + RandomStringUtils.random(5, true, false));
        systemAccess.commit();
        return systemAccess.getId();
    }

    public static UUID createDummyEhr(I_DomainAccess testDomainAccess) throws Exception {

        return createDummyEhr(testDomainAccess, createDummyPatient(testDomainAccess), createDummySystem(testDomainAccess));

    }

    public static UUID createDummyPatient(I_DomainAccess domainAccess) throws Exception {
        I_PartyIdentifiedAccess partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(domainAccess, "Patient_" + RandomStringUtils.random(10, true, false));

        return partyIdentifiedAccess.commit();

    }

    public static UUID createDummyEhr(I_DomainAccess testDomainAccess, UUID patientId, UUID systemId) throws Exception {
        //storeComposition a new patient
        I_EhrAccess ehrAccess = I_EhrAccess.getInstance(testDomainAccess,
                patientId,
                systemId,
                null,
                null);
        UUID ehrId = ehrAccess.commit();

        return ehrId;
    }

    public static UUID createDummyCommitter(I_DomainAccess domainAccess) throws Exception {
        I_PartyIdentifiedAccess partyIdentifiedAccess = I_PartyIdentifiedAccess.getInstance(domainAccess, "Doctor_" + RandomStringUtils.random(10, true, false));

        return partyIdentifiedAccess.commit();
    }

    public static UUID createDummyEventContext(I_DomainAccess domainAccess) throws Exception {
        EventContext eventContext = ContextHelper.createDummyContext();
        I_ContextAccess contextAccess = I_ContextAccess.getInstance(domainAccess, eventContext);
        return contextAccess.commit();
    }

    public static UUID createDummyContribution(I_DomainAccess domainAccess) throws Exception {

        return createDummyContribution(domainAccess,
                createDummyEhr(domainAccess),
                createDummySystem(domainAccess),
                createDummyCommitter(domainAccess));

    }

    public static UUID createDummyContribution(I_DomainAccess domainAccess, UUID ehrId, UUID systemId, UUID committerId) throws Exception {
        String description = "Contribution_" + RandomStringUtils.random(10);
        String templateId = "section  observation test";
        Integer setting = 233; //primary care

        I_ContributionAccess contributionAccess = I_ContributionAccess.getNewInstance(domainAccess,
                ehrId,
                systemId,
                committerId,
                description,
                setting,
                ContributionDef.ContributionType.COMPOSITION,
                ContributionDef.ContributionState.INCOMPLETE);

        return contributionAccess.commit();

    }

    public static EventContext createDummyEventContext(){
        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), CompositionAttributesHelper.DEMOGRAPHIC, CompositionAttributesHelper.PARTY);
        List<DvIdentifier> identifiers = new ArrayList<>();
        identifiers.add(new DvIdentifier("NHS-UK", "NHS-UK", "999999-1234", "2.16.840.1.113883.2.1.4.3"));
        PartyIdentified healthcareFacility = new PartyIdentified(partyRef, "FACILITY", identifiers);
        DateTime timenow = DateTime.now();
        DvCodedText concept = new DvCodedText("primary medical care", "openehr", "228");
        TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
        PartyIdentified performer = new PartyIdentified(partyRef, "HERR DOKTOR", null);
        Participation participation = new Participation(performer,
                new DvText("doctor"),
                new DvCodedText("telephone", "openehr", "204"),
                new DvInterval<DvDateTime>(new DvDateTime(DateTime.now().toString()), null),
                TerminologyServiceWrapper.getInstance());
        List<Participation> participations = new ArrayList<>();
        participations.add(participation);
        return new org.openehr.rm.composition.EventContext(healthcareFacility, new DvDateTime(timenow.toString()), null, participations, "TEST LAB", concept, null, terminologyService);
    }


}
