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

package com.ethercis.ehr.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by christian on 1/21/2016.
 */
public class MapInspectorTest {

    @Test
    public void testListInnnerClasses() throws Exception {

        String classname = "DvInterval<DvCount>";

        List<String> list = MapInspector.listInnnerClasses(classname);

        assertEquals("DvInterval", list.get(0));
        assertEquals("DvCount", list.get(1));

        classname = "Type1<Type2<Type3>>";

        list = MapInspector.listInnnerClasses(classname);

        assertEquals("Type1", list.get(0));
        assertEquals("Type2", list.get(1));
        assertEquals("Type3", list.get(2));

    }
}