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

package com.ethercis.ehr.encode.wrappers.terminolology;

import org.openehr.rm.support.terminology.CodeSetAccess;
import org.openehr.rm.support.terminology.OpenEHRCodeSetIdentifiers;
import org.openehr.rm.support.terminology.TerminologyAccess;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.terminology.SimpleTerminologyService;

import java.util.List;
import java.util.Map;

/**
 * utility wrapper to encapsulate an actual terminolgy service based on application context
 * Created by christian on 1/20/2016.
 */
public class TerminologyServiceWrapper implements TerminologyService {

    private static TerminologyService terminologyService = null;
    private static TerminologyService instance = new TerminologyServiceWrapper();

    private TerminologyServiceWrapper(){
    }

    public static TerminologyService getInstance(){
        if (terminologyService != null)
            return instance;

        if (System.getProperty("terminology.provider") != null){
            //resolve class and instance
            return null;
        }
        else
        {
           terminologyService = SimpleTerminologyService.getInstance();
        }

        return instance;
    }


    @Override
    public TerminologyAccess terminology(String s) {
        return terminologyService.terminology(s);
    }

    @Override
    public CodeSetAccess codeSet(String s) {
        return terminologyService.codeSet(s);
    }

    @Override
    public CodeSetAccess codeSetForId(OpenEHRCodeSetIdentifiers openEHRCodeSetIdentifiers) {
        return terminologyService.codeSetForId(openEHRCodeSetIdentifiers);
    }

    @Override
    public boolean hasTerminology(String s) {
        return terminologyService.hasTerminology(s);
    }

    @Override
    public boolean hasCodeSet(String s) {
        return terminologyService.hasCodeSet(s);
    }

    @Override
    public List<String> terminologyIdentifiers() {
        return terminologyService.terminologyIdentifiers();
    }

    @Override
    public List<String> codeSetIdentifiers() {
        return terminologyService.codeSetIdentifiers();
    }

    @Override
    public Map<String, String> openehrCodeSets() {
        return terminologyService.openehrCodeSets();
    }
}
