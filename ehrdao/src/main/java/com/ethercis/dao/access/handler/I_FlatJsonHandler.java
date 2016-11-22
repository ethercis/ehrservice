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

package com.ethercis.dao.access.handler;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import org.openehr.rm.composition.Composition;

import java.util.UUID;

/**
 * Created by christian on 11/8/2016.
 */
public interface I_FlatJsonHandler {
    Composition build(String content) throws Exception;

    UUID store(UUID ehrId, String flatJsonContent, UUID committerId, UUID systemId, String description) throws Exception;

    Boolean update(I_DomainAccess access, UUID compositionId, String content) throws Exception;

    Boolean update(I_DomainAccess access, UUID compositionId, String content, UUID committerId, UUID systemId, String description) throws Exception;
}
