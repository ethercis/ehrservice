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
package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.DataValueAdapter;
import org.apache.commons.lang.StringUtils;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.DvQuantified;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.support.basic.Interval;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

public class DvQuantityVBean extends DataValueAdapter implements I_VBeanWrapper {
	
//	protected DvQuantityVBean(double magnitude, int precision,
//			MeasurementService measurementService) {
//		super(magnitude, precision, measurementService);
//	}
	
	public DvQuantityVBean(DvQuantity q) {
		this.adaptee = q;
	}
	
	//get the type signature and spot the scalars for encoding
	
	public Map<String, Object> getFieldMap() throws Exception {
		
		Map<String, Object> mapval = new HashMap<String, Object>();
		
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(((DvQuantity)adaptee).getPrecision());
        format.setMaximumFractionDigits(((DvQuantity)adaptee).getPrecision());
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(((DvQuantity)adaptee).DECIMAL_SEPARATOR);
        format.setDecimalFormatSymbols(dfs);
        format.setGroupingUsed(false);
        
        mapval.put("magnitude", format.format(((DvQuantity)adaptee).getMagnitude()));
        
        if (!StringUtils.isEmpty(((DvQuantity)adaptee).getUnits()))
        	mapval.put("units", ((DvQuantity)adaptee).getUnits());

        return mapval;
	}

    @Override
    public DvQuantity parse(String value, String... defaults) {
        adaptee = ((DvQuantity)adaptee).parse(value);
        return (DvQuantity)adaptee;
    }

    public static DvQuantity getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvQuantity) return (DvQuantity)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            Double magnitude = (Double)valueMap.get("magnitude");
            Integer precision = 0;
            Double accuracy = 0D;
            Boolean accuracyPercent = false;
            DvInterval<DvQuantity> normalRange = null;
            if (valueMap.containsKey("precision")) {
                Object precisionObject = valueMap.get("precision");
                if (precisionObject instanceof Double)
                    precision = ((Double) valueMap.get("precision")).intValue();
                else if (precisionObject instanceof Integer)
                    precision = (Integer)valueMap.get("precision");
                else
                    throw new IllegalArgumentException("Could not decode precision:"+precisionObject.toString());

            }
            if (valueMap.containsKey("accuracy"))
                accuracy = (Double)valueMap.get("accuracy");
            if (valueMap.containsKey("accuracyPercent"))
                accuracyPercent = (Boolean)valueMap.get("accuracyPercent");
            if (valueMap.containsKey("normalRange")){
                Map<String, Object> rangeMap = (Map<String, Object>)((Map<String, Object>) valueMap.get("normalRange")).get("interval");
                Map<String, Object> lowerValueMap = new HashMap<>();
                lowerValueMap.put(CompositionSerializer.TAG_VALUE, rangeMap.get("lower"));
                DvQuantity lower = DvQuantityVBean.getInstance(lowerValueMap);
                Map<String, Object> upperValueMap = new HashMap<>();
                upperValueMap.put(CompositionSerializer.TAG_VALUE, rangeMap.get("upper"));
                DvQuantity upper = DvQuantityVBean.getInstance(upperValueMap);
                Boolean lowerIncluded = (Boolean)rangeMap.get("lowerIncluded");
                Boolean upperIncluded = (Boolean)rangeMap.get("upperIncluded");
                normalRange = new DvInterval(lower, upper);
                Interval interval = normalRange.getInterval();
                interval.setLowerIncluded(lowerIncluded);
                interval.setUpperIncluded(upperIncluded);
            }
            String units = (String)valueMap.get("units");
            return new DvQuantity(null, normalRange, null, accuracy, accuracyPercent, null, units, magnitude, precision, null);
        }
        else if (value instanceof String){
            DvQuantity object = (DvQuantity) DvQuantity.parseValue((String)value);
            return object;
        }


        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvQuantity generate(){
        DvQuantity dvQuantity = new DvQuantity("kg", 0D, 0);
        return dvQuantity;
    }

    public static DvQuantity increment(DvQuantity quantity){
        DvQuantified<DvQuantity> newQuantity = new DvQuantity(1D);
        return (DvQuantity)quantity.add(newQuantity);
    }
}
