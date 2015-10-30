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
import org.openehr.am.archetype.constraintmodel.primitive.CDate;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.support.basic.Interval;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvDateVConstraints extends DataValueConstraints {

    private DvDate min, max; //value range inclusive
    private String format;

    private DvDate assumed_value;

   public DvDateVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        if ((((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")) == null)
            return;

        CPrimitiveObject obj = (CPrimitiveObject)(((CSingleAttribute) ((CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")).getChildren().get(0));
        CDate cdatetime = (CDate)obj.getItem();

        //get the value range (min,max) if set
        Interval<DvDate> range = cdatetime.getInterval();
        if (range != null && range.getLower() != null)
            min = range.getLower();
        else
            min = null;

        if (range != null && range.getUpper() != null)
            max = range.getUpper();
        else
            max = null;

        //set the default assumed value
        if (cdatetime.hasAssumedValue()){
            assumed_value = (DvDate) cdatetime.assumedValue();
        }

        if (cdatetime.getPattern() != null)
            format = cdatetime.getPattern();
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public DvDate getAssumedValueCode(){
        return assumed_value;
    }
    public DvDate getMin() {
        return min;
    }

    public DvDate getMax() {
        return max;
    }

    public String getFormat() {
        return format;
    }

    public DvDate getAssumed_value() {
        return assumed_value;
    }

    @Override
        public boolean validate(DataValue qty) {
        if (!(qty instanceof DvDate))
            return false;

        //check in range
        DateTime time = ((DvDate) qty).getDateTime();

        Boolean eval = true;

        if (min != null)
            eval &= time.isAfter(min.getDateTime());

        if (max != null)
            eval &= time.isBefore(max.getDateTime());

        return (eval);
    }
}