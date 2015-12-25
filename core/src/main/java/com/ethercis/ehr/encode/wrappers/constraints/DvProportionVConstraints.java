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
import org.openehr.am.archetype.constraintmodel.primitive.CReal;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.DvProportion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvProportionVConstraints extends DataValueConstraints {

    private final class Limits implements Serializable {

        private static final long serialVersionUID = 2067896966194071392L;
        private double min;            //min value
        private boolean minInclusive = true;    //true if this is less than or equal
        private double max;            //max value
        private boolean maxInclusive = true;    //true if this is greater than or equal
        private double assumedValue;

        public double getAssumedValue() {
            return assumedValue;
        }

        public void setAssumedValue(double assumedValue) {
            this.assumedValue = assumedValue;
        }


        public Limits() {
        }

        public void setMin(double min) {
            this.min = min;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public boolean isMinInclusive() {
            return minInclusive;
        }

        public double getMax() {
            return max;
        }

        public boolean isMaxInclusive() {
            return maxInclusive;
        }

        public boolean isWithinLimits(double magnitude) {

            if (!(magnitude > min ? true : (minInclusive ? (magnitude == min ? true : false) : false)))
                return false;

            if (!(magnitude < max ? true : (maxInclusive ? (magnitude == max ? true : false) : false)))
                return false;

            return true;
        }

        public void setMinInclusive(boolean b) {
            this.minInclusive = b;
        }

        public void setMaxInclusive(boolean b) {
            this.maxInclusive = b;
        }

    }

    Map<String, Limits> limits; //array list of limits depending on selected numerator/denominator

    public DvProportionVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    private Limits setLocalLimits(CReal settings) {

        if (settings.getInterval() == null)
            return null;

        Limits limits = new Limits();
        limits.setMin(settings.getInterval().getLower());
        limits.setMax(settings.getInterval().getUpper());
        limits.setMinInclusive(settings.getInterval().isLowerIncluded());
        limits.setMaxInclusive(settings.getInterval().isUpperIncluded());
        if (settings.hasAssumedValue())
            limits.setAssumedValue((Double) settings.assumedValue());

        return limits;
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        limits = new HashMap<String, Limits>();
        CComplexObject ccobj = (CComplexObject) valueAttribute.getChildren().get(0);
        if (ccobj == null)
            return;
        if (ccobj.getAttribute("numerator") != null) {
            CPrimitiveObject obj = (CPrimitiveObject) ((ccobj.getAttribute("numerator")).getChildren().get(0));
            CReal citem = (CReal) obj.getItem();
            limits.put("numerator", setLocalLimits(citem));
        }

        if (ccobj.getAttribute("denominator") != null) {
            CPrimitiveObject obj = (CPrimitiveObject) ((ccobj.getAttribute("denominator")).getChildren().get(0));
            CReal citem = (CReal) obj.getItem();
            limits.put("denominator", setLocalLimits(citem));
        }
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvProportion))
            return false;

        return limits.get("numerator").isWithinLimits(((DvProportion) qty).getNumerator())
                &&
                limits.get("denominator").isWithinLimits(((DvProportion) qty).getDenominator());

    }
}
