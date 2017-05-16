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
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.PartyRef;

import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 9/28/2015.
 */
public class PartyIdentifiedVBean implements I_VBeanWrapper  {
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

    @Override
    /**
     * proposed encoded string format for a "simplified" PartyIdenfied<br>
     * id::scheme::namespace::type::name<br>
     * Defaults:<br>
     * <li>Scheme</li>
     * <li>Namespace</li>
     * <li>Category</li>
     *
     **/
    public PartyIdentified parse(String value, String... defaults) {
        String identifier, namespace, scheme, type, name;
        String partyCodes[] = value.split("::");

        if (partyCodes.length != 5) {
            if (partyCodes.length == 2){ //assume we have identifier::name, insert scheme::namespace||type using default values
                identifier = partyCodes[0];
                name = partyCodes[1];
                scheme = defaults[0];
                namespace = defaults[1];
                type = defaults[2];
            }
            else
                throw new IllegalArgumentException("Passed value is not compatible for party (id::scheme::namespace::type::name)");
        }
        else {
            identifier = partyCodes[0];
            scheme = partyCodes[1];
            namespace = partyCodes[2];
            type = partyCodes[3];
            name = partyCodes[4];
        }

        PartyIdentified partyIdentified= new PartyIdentified(new PartyRef(new GenericID(identifier, scheme), namespace, type), name, null);
        return partyIdentified;
    }

    public static PartyIdentified getInstance(Map<String, Object> attributes) {
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof PartyIdentified) return (PartyIdentified)value;

        if (!attributes.isEmpty()){
            Map valueMap = (Map)value;
            Map externalRefMap = (Map)valueMap.get("externalRef");

            if (externalRefMap != null) {
                String performerIdScheme = (String) ((Map) externalRefMap.get("id")).get("scheme");
                String performerIdValue = (String) ((Map) externalRefMap.get("id")).get("value");
                String performerNameSpace = (String) (externalRefMap.get("namespace"));
                String performerType = (String) (externalRefMap.get("type"));
                return new PartyIdentified(new PartyRef(new GenericID(performerIdValue, performerIdScheme), performerNameSpace, performerType), (String)valueMap.get("name"), null);
            }

            return null;

        }
        throw new IllegalArgumentException("Could not get instance");
    }

    public static PartyIdentified generate(){
        PartyRef partyRef = new PartyRef(new HierObjectID("ref"), "NHS-UK",  "party");
        PartyIdentified party = new PartyIdentified(partyRef, "dummy", null);
        return party;
    }
}
