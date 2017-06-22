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
package org.openehr.rm.datatypes.text;


import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enumeration of match between terms
 * 
 * @author Rong Chen
 * @version 1.0
 */

public enum Match {
    NARROWER ("<"),
    EQUIVALENT ("="),
    BROADER (">"),
    UNKNOWN ("?");

    /* constructor */
    @FullConstructor Match(@Attribute(name = "value", required = true) String value) {
        this.value = value;
    }

    /**
     * Return value of this term match
     *
     * @return value
     */
    public String getValue() {
        return value;
    }


    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    private static final Map<String,Match> MATCH_MAP;

    static {
        Map<String,Match> map = new ConcurrentHashMap<String,Match>();
        for (Match instance : Match.values()) {
            map.put(instance.getValue(),instance);
        }
        MATCH_MAP = Collections.unmodifiableMap(map);
    }

    public static Match get (String value) {
        return MATCH_MAP.get(value);
    }

    private String value;
}

/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is Match.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2003-2004
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */