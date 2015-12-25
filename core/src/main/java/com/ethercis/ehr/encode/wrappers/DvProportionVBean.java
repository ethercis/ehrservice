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
import org.openehr.rm.datatypes.quantity.DvProportion;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.datatypes.quantity.ProportionKind;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

public class DvProportionVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvProportionVBean(DvProportion p) {
		this.adaptee = p;
	}
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(((DvProportion)adaptee).getPrecision());
		format.setMaximumFractionDigits(((DvProportion)adaptee).getPrecision());
		DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
		dfs.setDecimalSeparator(DvQuantity.DECIMAL_SEPARATOR);
		format.setDecimalFormatSymbols(dfs);
		format.setGroupingUsed(false);
		map.put("numerator", format.format(((DvProportion)adaptee).getNumerator()));
		map.put("denominator", format.format(((DvProportion)adaptee).getDenominator()));
		map.put("type", ((DvProportion)adaptee).getType());
		return map;
	}

    @Override
    public DvProportion parse(String value, String... defaults) {
        adaptee = ((DvProportion)adaptee).parse(value);
        return (DvProportion)adaptee;
    }

    public static DvProportion getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvProportion) return (DvProportion)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
            Double numerator = (Double) valueMap.get("numerator");
            Double denominator = (Double) valueMap.get("denominator");
            ProportionKind type = ProportionKind.valueOf((String) valueMap.get("type"));
            Integer precision = 0;
            if (valueMap.containsKey("precision")) {
                Object precisionObject = valueMap.get("precision");
                if (precisionObject instanceof Double)
                    precision = ((Double) valueMap.get("precision")).intValue();
                else if (precisionObject instanceof Integer)
                    precision = (Integer)valueMap.get("precision");
                else
                    throw new IllegalArgumentException("Could not decode precision:"+precisionObject.toString());

            }

            DvProportion object = new DvProportion(numerator, denominator, type, precision);
            return object;
        }

        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvProportion generate(){
        return new DvProportion(0D, 0D, ProportionKind.FRACTION, 0);
    }
}
