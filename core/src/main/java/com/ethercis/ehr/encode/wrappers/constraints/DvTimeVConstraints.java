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
import org.joda.time.DateTime;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject;
import org.openehr.am.archetype.constraintmodel.CSingleAttribute;
import org.openehr.am.archetype.constraintmodel.primitive.CTime;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.support.basic.Interval;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvTimeVConstraints extends DataValueConstraints {

    private DvTime min, max; //value range inclusive
    private String format;

    private DvTime assumed_value;

    public DvTimeVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        if ((((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")) == null)
            return;

        CPrimitiveObject obj = (CPrimitiveObject) (((CSingleAttribute) ((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")).getChildren().get(0));
        CTime cdatetime = (CTime) obj.getItem();

        //get the value range (min,max) if set
        Interval<DvTime> range = cdatetime.getInterval();
        if (range != null && range.getLower() != null)
            min = range.getLower();
        else
            min = null;

        if (range != null && range.getUpper() != null)
            max = range.getUpper();
        else
            max = null;

        //set the default assumed value
        if (cdatetime.hasAssumedValue()) {
            assumed_value = (DvTime) cdatetime.assumedValue();
        }

        if (cdatetime.getPattern() != null)
            format = cdatetime.getPattern();
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public DvTime getAssumedValueCode() {
        return assumed_value;
    }

    public DvTime getMin() {
        return min;
    }

    public DvTime getMax() {
        return max;
    }

    public String getFormat() {
        return format;
    }

    public DvTime getAssumed_value() {
        return assumed_value;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvTime))
            return false;

        //check in range
        DateTime time = ((DvTime) qty).getDateTime();

        return (time.isAfter(min.getDateTime()) && time.isBefore(max.getDateTime()));
    }
}