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
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.DvQuantity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * defines the constraints for form rendering
 */
public final class DvQuantityVConstraints extends DataValueConstraints {

    transient Logger log = LogManager.getLogger(DvQuantityVConstraints.class);

	CDvQuantity cDvQuantity;

    /**
	 * Example: 5, 100 mg precision = 0 (integer)
	 */
	private final class Limits implements Serializable {

        private static final long serialVersionUID = -7130429128481026794L;
        private String unit; 			//is used as a key to identify the constraint
		private double min; 			//min value
		private boolean minInclusive = true; 	//true if this is less than or equal
		private double max; 			//max value
		private boolean maxInclusive = true; 	//true if this is greater than or equal
		private int precision; 			//number of decimals
		
		public Limits(String unit, double min, double max, int precision) {
			this.unit = unit;
			this.min = min;
			this.max = max;
			this.precision = precision;
		}
		
		public String getUnit() {
			return unit;
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

		public int getPrecision() {
			return precision;
		}

		public boolean isWithinLimits(double magnitude) {
			
			if (! (magnitude > min ? true : (minInclusive ? (magnitude == min  ? true : false) : false)))
				return false;

			if (! (magnitude < max ? true : (maxInclusive ? (magnitude == max ? true : false): false)))
				return false;
			
			return true;
		}

        public void setMinInclusive(boolean b){ this.minInclusive = b ;};
        public void setMaxInclusive(boolean b){ this.maxInclusive = b ;};

    }
	
	Map<String, Limits> limits = new HashMap<String, Limits>(); //array list of limits depending on selected unit

	public DvQuantityVConstraints(RMObjectBuilder builder, DataValue self) {
		super(builder, self);
 	}


	public void addLimit(String unit, double min, double max, int precision) {
		Limits l = new Limits(unit, min, max, precision);
		limits.put(unit, l);
	}

	public Set<String> getLimitUnits(){
		return limits.keySet();
	}
	
	public double getLowerLimit(String unit) {
		return limits.get(unit).getMin();
	}
	
	public double getUpperLimit(String unit) {
		return limits.get(unit).getMax();
	}
	
	public int getPrecision(String unit) {
		return limits.get(unit).getPrecision();
	}
	
	public boolean isLowerLimitInclusive(String unit) {
		return limits.get(unit).isMinInclusive();
	}
	
	public boolean isUpperLimitInclusive(String unit) {
		return limits.get(unit).isMaxInclusive();
	}

    public Set<String> getUnits(){
        return limits.keySet();
    }

    public boolean isWithinLimits(String units, double magnitude){
        if (limits.containsKey(units))
            return limits.get(units).isWithinLimits(magnitude);
        return false;
    }

    public void setMinInclusive(String unit, boolean val){
        if (limits.containsKey(unit))
           limits.get(unit).setMinInclusive(val);
    }

    public void setMaxInclusive(String unit, boolean val){
        if (limits.containsKey(unit))
            limits.get(unit).setMaxInclusive(val);
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    /**
     * convenience, can be used for test purpose...
     * @param clist
     */
    public void setConstraints(CDvQuantity clist){

        if (clist.getList() == null || clist.getList().isEmpty()){
            log.warn("Empty constraint list:"+clist);
            return;
        }

        for (CDvQuantityItem item : clist.getList()){
            addLimit(item.getUnits() != null ? item.getUnits() : "*END*",
                    item.getMagnitude() != null ? (item.getMagnitude().getLower() != null ? item.getMagnitude().getLower() : Double.MIN_VALUE) : Double.MIN_VALUE,
                    item.getMagnitude() != null ? (item.getMagnitude().getUpper() != null ? item.getMagnitude().getUpper() : Double.MAX_VALUE) : Double.MAX_VALUE,
                    item.getPrecision() != null ? (item.getPrecision().getLower() != null ? item.getPrecision().getLower() : 0) : 0);
        }

    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
		if (valueAttribute == null)
			return;
        //get the limits as found from the flatten archetype and template
        CDvQuantity constraints = (CDvQuantity) valueAttribute.getChildren().get(0);
        setConstraints(constraints);
    }

    @Override
    public boolean validate(DataValue qty){
        if (!(qty instanceof DvQuantity))
            return false;
        return isWithinLimits(((DvQuantity)qty).getUnits(), ((DvQuantity)qty).getMagnitude());
    }

}

