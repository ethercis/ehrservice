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
package com.ethercis.ehr.handler;

import org.openehr.rm.common.archetyped.Locatable;

import java.util.List;
import java.util.Map;

/**
 * Created by Christian Chevalley on 7/29/2014.
 */
public class ContributionManager implements I_ContributionManager {
    @Override
    public List<String> getContributionTypes(String userid, String ehrid, List<String> programs) {
        return null;
    }

    @Override
    public String startContribution(String userid, String ehrid, String description) {
        return null;
    }

    @Override
    public List<String> getCompositionList(String contributionId) {
        return null;
    }

    @Override
    public Locatable newComposition(String contributionId, String compositionName) {
        return null;
    }

    @Override
    public String submitComposition(String contributionId, Locatable composition) throws Exception {
        return null;
    }

    @Override
    public void cancelComposition(String contributionId, String compositionId) throws Exception {

    }

    @Override
    public void commitContribution(String contributionId) throws Exception {

    }

    @Override
    public void abortContribution(String contributionId) throws Exception {

    }

    @Override
    public List<Map<String, String>> getContributionList(String ehrId, List<String> programs) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, String>> getCompositions(String contributionId) throws Exception {
        return null;
    }

    @Override
    public Locatable selectComposition(String compositionId) throws Exception {
        return null;
    }

    @Override
    public String updateContribution(String contributionId, String userid) throws Exception {
        return null;
    }

 }
