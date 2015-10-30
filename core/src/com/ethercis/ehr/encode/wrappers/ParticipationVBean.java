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
package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.CompositionSerializer;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.terminology.SimpleTerminologyService;

import java.util.Map;

/**
 * Created by DELL on 7/19/2015.
 */
public class ParticipationVBean implements I_VBeanWrapper {
    @Override
    public Map<String, Object> getFieldMap() throws Exception {
        return null;
    }

    @Override
    public Object getAdaptee() {
        return null;
    }

    @Override
    public void setAdaptee(Object adaptee) {

    }

//    "<other_participations>\n"+
//            "\t\t\t\t\t<function>\n"+
//            "\t\t\t\t\t\t<value>Oncologist</value>\n"+
//            "\t\t\t\t\t</function>\n"+
//            "\t\t\t\t\t<performer xsi:type=\"PARTY_IDENTIFIED\">\n"+
//            "\t\t\t\t\t\t<external_ref>\n"+
//            "\t\t\t\t\t\t\t<id xsi:type=\"GENERIC_ID\">\n"+
//            "\t\t\t\t\t\t\t\t<value>1345678</value>\n"+
//            "\t\t\t\t\t\t\t\t<scheme>2.16.840.1.113883.2.1.4.3</scheme>\n"+
//            "\t\t\t\t\t\t\t</id>\n"+
//            "\t\t\t\t\t\t\t<namespace>NHS-UK</namespace>\n"+
//            "\t\t\t\t\t\t\t<type>ANY</type>\n"+
//            "\t\t\t\t\t\t</external_ref>\n"+
//            "\t\t\t\t\t\t<name>Dr. Marcus Johnson</name>\n"+
//            "\t\t\t\t\t</performer>\n"+
//            "\t\t\t\t\t<mode>\n"+
//            "\t\t\t\t\t\t<value>face-to-face communication</value>\n"+
//            "\t\t\t\t\t\t<defining_code>\n"+
//            "\t\t\t\t\t\t\t<terminology_id>\n"+
//            "\t\t\t\t\t\t\t\t<value>openehr</value>\n"+
//            "\t\t\t\t\t\t\t</terminology_id>\n"+
//            "\t\t\t\t\t\t\t<code_string>216</code_string>\n"+
//            "\t\t\t\t\t\t</defining_code>\n"+
//            "\t\t\t\t\t</mode>\n"+
//            "\t\t\t\t</other_participations>"
    @Override
    /**
     * proposed encoded string format for other_participations:<br>
     *     function|id::scheme::namespace::type::name|mode::terminologyId::code
     */
    public Participation parse(String value, String... defaults) {
        String[] values = value.split("\\|");

        if (values.length != 3)
            throw new IllegalArgumentException("Passed value is not compatible for a participation (function|performer|mode)");

        String modeCodes[] = values[2].split("::");

        if (modeCodes.length != 3)
            throw new IllegalArgumentException("Passed value is not compatible for mode (mode::terminologyId::code)");


        PartyIdentified partyIdentified= new PartyIdentifiedVBean().parse(values[1], defaults);
        DvCodedText mode = new DvCodedText(modeCodes[0], modeCodes[1], modeCodes[2]);
        DvText function = new DvText(values[0]);
        Participation participation = new Participation(partyIdentified, function, mode, null, SimpleTerminologyService.getInstance());
        return participation;
    }

    public static Participation getInstance(Map<String, Object> attributes) {
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof Participation) return (Participation)value;

        if (!attributes.isEmpty()){
            Map valueMap = (Map)value;
            Map functionMap = (Map)valueMap.get("function");
            String functionValue = (String)functionMap.get("value");

            Map performerIdentified = (Map)valueMap.get("performer");
            String performerName = (String)performerIdentified.get("name");
            Map performerExternalRef = (Map)performerIdentified.get("externalRef");
            String performerIdScheme = (String)((Map)performerExternalRef.get("id")).get("scheme");
            String performerIdValue = (String)((Map)performerExternalRef.get("id")).get("value");
            String performerNameSpace = (String)(performerExternalRef.get("namespace"));
            String performerType = (String)(performerExternalRef.get("type"));

            Map modeAttibutes = (Map)valueMap.get("mode");
            String modeDefiningCodeTerminologyId = (String)(((Map)((Map)modeAttibutes.get("definingCode")).get("terminologyId")).get("value"));
            String modeDefiningCodeString = (String)((Map)modeAttibutes.get("definingCode")).get("codeString");
            String modeValue = (String)(modeAttibutes.get("value"));

            //ready to generate a new Participation
            PartyIdentified partyIdentified= new PartyIdentified(new PartyRef(new GenericID(performerIdValue, performerIdScheme), performerNameSpace, performerType), performerName, null);
            DvCodedText mode = new DvCodedText(modeValue, modeDefiningCodeTerminologyId, modeDefiningCodeString);
            DvText function = new DvText(functionValue);
            Participation participation = new Participation(partyIdentified, function, mode, null, SimpleTerminologyService.getInstance());

            return participation;

        }
        throw new IllegalArgumentException("Could not get instance");
    }
}
