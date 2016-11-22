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

package com.ethercis.dao.access.interfaces;

import com.ethercis.dao.access.jooq.PartyIdentifiedAccess;
import org.jooq.Result;

/**
 * Created by christian on 11/9/2016.
 */
public interface I_PartyIdentifiedFacilityAccess extends I_PartyIdentifiedAccess {

    static org.openehr.rm.common.generic.PartyIdentified retrievePartyIdentified(Result<?> records){
        //get the composer attributes
        String name = (String)records.getValue(0, I_CompositionAccess.F_FACILITY_NAME);
        String ref_scheme = (String)records.getValue(0, I_CompositionAccess.F_FACILITY_REF_SCHEME);
        String ref_namespace = (String)records.getValue(0, I_CompositionAccess.F_FACILITY_REF_NAMESPACE);
        String ref_value = (String)records.getValue(0, I_CompositionAccess.F_FACILITY_REF_VALUE);
        String ref_type = (String)records.getValue(0, I_CompositionAccess.F_FACILITY_REF_TYPE);

        return PartyIdentifiedAccess.retrievePartyIdentified(name, ref_scheme, ref_namespace, ref_value, ref_type);
    }
}
