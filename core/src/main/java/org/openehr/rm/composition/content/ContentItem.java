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
package org.openehr.rm.composition.content;

import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.archetyped.Pathable;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.rm.datatypes.text.DvText;

import java.util.Set;

/**
 * ContentItem
 *
 * @author Rong Chen
 * @version 1.0
 */
public abstract class ContentItem extends Locatable {

    /**
     * Constructs a ContentItem
     *
     * @param uid
     * @param archetypeNodeId
     * @param name
     * @param archetypeDetails
     * @param feederAudit
     * @param links
     */
    protected ContentItem(UIDBasedID uid, String archetypeNodeId, DvText name,
                          Archetyped archetypeDetails, FeederAudit feederAudit,
                          Set<Link> links, Pathable parent) {
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent);
    }

    // POJO start
    protected ContentItem() {        
    }
    // POJO end
}

