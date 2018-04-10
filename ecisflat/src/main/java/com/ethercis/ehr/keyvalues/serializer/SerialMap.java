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
 * Created by christian on 5/17/2017.
 */
public class SerialMap implements I_SerialMap {

    Map<String, Object> map;
    String path;
    Object target;

    public SerialMap(Object target, Map<String, Object> map, String path) {
        this.map = map;
        this.path = path;
        this.target = target;
    }

    public SerialMap(Map<String, Object> map, String path) {
        this.map = map;
        this.path = path;
    }

    @Override
    public Map<String, String> encode(){
        Map<String, String> retmap;

        if (target == null)
            return null;

        //no easy Late Binding...
        if (target instanceof DvInterval)
            retmap = encode((DvInterval)target);
        else if (target instanceof DvParsable)
            retmap =  encode((DvParsable)target);
        else if (target instanceof Participation)
            retmap =  encode((Participation)target);
        else if (target instanceof PartyIdentified)
            retmap =  encode((PartyIdentified)target);
        else if (target instanceof RMObject)
            retmap =  encode((RMObject)target);
        else
            retmap =  encode(target);

        return retmap;
    }

    @Override
    public Map<String, String> encode(DvInterval dvInterval){
        return new SerializeDvInterval(dvInterval).valueMap(path, map);
    }

    @Override
    public Map<String, String> encode(DvParsable dvParsable){
        return new SerializeDvParsable(dvParsable).valueMap(path, map);
    }

    @Override
    public Map<String, String> encode(Participation participation){
        return new SerializeParticipation(participation).valueMap(path, map);
    }

    @Override
    public Map<String, String> encode(PartyIdentified partyIdentified){
        return new SerializePartyIdentified(partyIdentified).valueMap(path, map);
    }

    @Override
    public Map<String, String> encode(RMObject rmObject){
        return new SerializeRmObject(rmObject).valueMap(path, map);
    }

    @Override
    public Map<String, String> encode(Object object){
        return new SerializePrimitive(object).valueMap(path, map);
    }

}
