/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
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

package com.ethercis.opt;

import org.openehr.rm.support.terminology.TerminologyAccess;
import org.openehr.terminology.SimpleTerminologyService;

/**
 * Created by christian on 2/19/2018.
 */
public class SimpleTerminology {

    String code;

    public SimpleTerminology(String code) {
        this.code = code;
    }

    public String label(){
        SimpleTerminologyService terminologyService = (SimpleTerminologyService) SimpleTerminologyService.getInstance();
        TerminologyAccess terminologyAccess = terminologyService.terminology("openehr");
        return terminologyAccess.rubricForCode(code, "en");
    }
}
