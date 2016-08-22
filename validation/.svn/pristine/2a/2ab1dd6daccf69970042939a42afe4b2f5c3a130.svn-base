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
import com.ethercis.validation.OptConstraintMapper;
import com.ethercis.validation.wrappers.IntervalComparator;
import com.ethercis.validation.wrappers.ValidationException;
import org.openehr.schemas.v1.CARDINALITY;
import org.openehr.schemas.v1.CATTRIBUTE;
import org.openehr.schemas.v1.CCOMPLEXOBJECT;
import org.openehr.schemas.v1.CMULTIPLEATTRIBUTE;

/**
 * Created by christian on 8/11/2016.
 */
public abstract class StructureConstraint {
    protected final ConstraintMapper constraintMapper;

    public StructureConstraint(ConstraintMapper constraintMapper) {
        this.constraintMapper = constraintMapper;
    }

    public void validate(String path, Integer eventsOccurrences) throws Exception {

        //check mandatory fields
        CCOMPLEXOBJECT ccomplexobject = ((OptConstraintMapper.OptConstraintItem)constraintMapper.getConstraintItem(path)).getConstraint();

        for (CATTRIBUTE cattribute: ccomplexobject.getAttributesArray()){
            if (cattribute instanceof CMULTIPLEATTRIBUTE){
                CARDINALITY cardinality = ((CMULTIPLEATTRIBUTE) cattribute).getCardinality();
                //check cardinality..

                if ((eventsOccurrences > 1) &&  cardinality.getIsUnique())
                    ValidationException.raise(path, "Only one event is allowed in history", "HIS02");

                //check within boundaries
                IntervalComparator.isWithinBoundaries(eventsOccurrences, cardinality.getInterval());
            }
        }

    }
}
