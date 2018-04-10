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

import com.ethercis.ehr.keyvalues.I_PathValue;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by christian on 5/17/2017.
 */
public class SerializeParticipation implements I_EcisFlatSerializer {

    Participation participation;

    public SerializeParticipation(Participation participation) {
        this.participation = participation;
    }

    @Override
    public Map<String, String> valueMap(String path, Map<String, Object> map) {
        Map<String, String> retMap = new TreeMap<>();
        retMap.put(path + I_PathValue.PARTICIPATION_FUNCTION_SUBTAG, participation.getFunction().getValue());
        PartyIdentified performer = (PartyIdentified) participation.getPerformer();
        if (performer!=null) {
            retMap.put(path + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, performer.getName());
            if (performer.getExternalRef() != null)
                retMap.put(path + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, performer.getExternalRef().getId().getValue());
        }
        if (participation.getMode() != null)
            retMap.put(path + I_PathValue.PARTICIPATION_MODE_SUBTAG, participation.getMode().toString());
        return retMap;
    }
}
