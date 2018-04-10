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
 * Model of a reverse internet domain, as used to uniquely identify an internet domain.
 * 
 * @author Yin Su Lim
 * @version 1.0
 *
 */
public class InternetID extends UID {

    /**
     * @param value
     */
	@FullConstructor
    public InternetID(@Attribute(name = "value", required = true)String value) {
        super(value);
//        if (!value.matches(PATTERN)) {
//            throw new IllegalArgumentException("wrong format");
//        }
                /* or checking using java.net.URL ?
                try {
                        URL url = new URL("http", value, 0, "");
                } catch (MalformedURLException e) {
                        throw new IllegalArgumentException("wrong format");
                }
                 * but this won't check the format of host or domain.
                 */
    }
    
    private static String PATTERN = "[a-zA-Z]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\\.[a-zA-Z]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*";
}

