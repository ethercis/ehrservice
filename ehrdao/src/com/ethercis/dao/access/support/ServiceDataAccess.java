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
package com.ethercis.dao.access.support;

import java.util.Map;

/**
 * A simple wrapper to encapsulate resource accesses from services
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/30/2015.
 */
public class ServiceDataAccess extends DataAccess {

    public ServiceDataAccess(Map<String, Object> properties) throws Exception {
        super(properties);
    }
}
