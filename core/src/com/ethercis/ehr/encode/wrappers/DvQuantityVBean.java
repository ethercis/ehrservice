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
import org.openehr.rm.datatypes.quantity.DvQuantity;

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
            Integer precision = ((Double)valueMap.get("precision")).intValue();
            String units = (String)valueMap.get("units");
            return new DvQuantity(units, magnitude, precision);
        }
        else if (value instanceof String){
            DvQuantity object = (DvQuantity) DvQuantity.parseValue((String)value);
            return object;
        }


        throw new IllegalArgumentException("Could not get instance");
    }
}
