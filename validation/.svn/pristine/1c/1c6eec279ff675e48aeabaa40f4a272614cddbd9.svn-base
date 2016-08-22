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

package com.ethercis.validation.hardwired;

import com.ethercis.validation.ConstraintMapper;
import com.ethercis.validation.wrappers.ValidationException;
import org.openehr.rm.datastructure.history.History;

/**
 * Created by christian on 8/11/2016.
 */
public class CHistory  extends StructureConstraint implements I_CHWConstraintValidate {


    public CHistory(ConstraintMapper constraintMapper) {
        super(constraintMapper);
    }

    @Override
    public void validate(String path, Object aValue) throws Exception {
        if (!(aValue instanceof History))
           ValidationException.raise(path, "Unexpected value type in History:"+aValue,"HIS01");

        History history = (History)aValue;
        Integer eventsOccurrences = history.getEvents().size();

        validate(path, eventsOccurrences);
    }
}
