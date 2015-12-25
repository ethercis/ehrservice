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
package com.ethercis.ehr.json;

import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/13/2015.
 */
public interface I_TreeMapNode {
    TreeMapNode nextChild(Object key) throws IllegalArgumentException;

    TreeMapNode getChild(Object key) throws IllegalArgumentException;

    boolean hasChild(Object key) throws IllegalArgumentException;

    Integer childSize(Object key);

    Map<String, Object> asAttributes();

    Integer size();

    TreeMapNode nextSibling();

    int size(Object object) throws IllegalArgumentException;

    boolean hasChild() throws IllegalArgumentException;

    boolean isTreeNode();

    boolean isArray();

    boolean hasParent();

    Object getCurrentNode();

    TreeMapNode getCurrentTree();

    Object getCurrentKey();
}
