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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject;
import org.openehr.am.archetype.constraintmodel.CSingleAttribute;
import org.openehr.am.archetype.constraintmodel.primitive.CDateTime;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.support.basic.Interval;
import org.openehr.schemas.v1.ARCHETYPECONSTRAINT;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvDateTimeVConstraints extends DataValueConstraints {
    static Logger logger = LogManager.getLogger(DvDateTimeVConstraints.class);

    //a map of <support::code>,text value, description
    //the descriptive text is localized depending on the language set

    private DvDateTime min, max; //value range inclusive
    private String format;

    private DvDateTime assumed_value;

    public DvDateTimeVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        if ((((org.openehr.am.archetype.constraintmodel.CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")) == null)
            return;

        CPrimitiveObject obj = (CPrimitiveObject)(((CSingleAttribute) ((org.openehr.am.archetype.constraintmodel.CComplexObject) valueAttribute.getChildren().get(0)).getAttribute("value")).getChildren().get(0));
        CDateTime cdatetime = (CDateTime)obj.getItem();

        //get the value range (min,max) if set
        Interval<DvDateTime> range = cdatetime.getInterval();
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
            assumed_value = (DvDateTime) cdatetime.assumedValue();
        }

        if (cdatetime.getPattern() != null)
            format = cdatetime.getPattern();
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public DvDateTime getAssumedValueCode(){
        return assumed_value;
    }
    public DvDateTime getMin() {
        return min;
    }

    public DvDateTime getMax() {
        return max;
    }

    public String getFormat() {
        return format;
    }

    public DvDateTime getAssumed_value() {
        return assumed_value;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvDateTime))
            return false;

        //check in range
        DateTime time = ((DvDateTime) qty).getDateTime();

        return (time.isAfter(min.getDateTime()) && time.isBefore(max.getDateTime()));
    }

    public static void validate(String path, String datetime, ARCHETYPECONSTRAINT constraint) throws Exception {
//        if (constraint instanceof CCOMPLEXOBJECT) {
//            CCOMPLEXOBJECT ccomplexobject = (CCOMPLEXOBJECT) constraint.changeType(CCOMPLEXOBJECT.type);
//            CComplexObject.validate(path, datetime, ccomplexobject);
//        }
//        else
//            logger.warn("Validation on non CCOMPLEXOBJECT is not supported, constraint is of class:"+constraint.getClass());

    }
}
