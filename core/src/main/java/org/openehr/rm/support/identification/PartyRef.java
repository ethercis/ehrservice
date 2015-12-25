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
package org.openehr.rm.support.identification;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;

/**
 * Identifier for parties in a demographic service. There are
 * typically a number of subtypes of the "PARTY" class, including
 * "PERSON", "ORGANISATION", etc.
 *
 * @author Rong Chen
 * @version 1.0
 */
public class PartyRef extends ObjectRef {

    // POJO start
	PartyRef() {
    }
    // POJO end

    /**
     * Construt a PartyRef
     *
     * @param id
     * @throws IllegalArgumentException if id or type null
     */
	@FullConstructor
    public PartyRef(
    		@Attribute(name = "id", required = true)ObjectID id,
            @Attribute(name = "namespace", required = true)String namespace,
    		@Attribute(name = "type", required = true)String type) {
        super(id, namespace, type);
    }    
}

