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
package com.ethercis.ehr.encode.wrappers.constraints;

import org.openehr.build.RMObjectBuilder;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject;
import org.openehr.am.archetype.constraintmodel.CSingleAttribute;
import org.openehr.am.archetype.constraintmodel.primitive.CDuration;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.support.basic.Interval;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvDurationVConstraints extends DataValueConstraints {

    //a map of <support::code>,text value, description
    //the descriptive text is localized depending on the language set

    DvDuration min, max; //value range inclusive
    private DvDuration assumed_value_code;

    public DvDurationVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        if (((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value") == null) return;

        CPrimitiveObject obj = (CPrimitiveObject) (((CSingleAttribute) ((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")).getChildren().get(0));
        CDuration cduration = (CDuration) obj.getItem();

        //get the value range (min,max) if set
        Interval<DvDuration> range = cduration.getInterval();
        if (range.getLower() != null)
            min = range.getLower();
        else
            min = null;

        if (range.getUpper() != null)
            max = range.getUpper();
        else
            max = null;

        //set the default assumed value
        if (cduration.hasAssumedValue()) {
            assumed_value_code = (DvDuration) cduration.assumedValue();
        }
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public DvDuration getAssumedValueCode() {
        return assumed_value_code;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvDuration))
            return false;

        return (((DvDuration) qty).compareTo(min) >= 0 && (((DvDuration) qty).compareTo(max) <= 0));

    }
}