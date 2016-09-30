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
import com.ethercis.ehr.encode.VBeanUtil;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.datatypes.quantity.DvCount;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.DvOrdered;
import org.openehr.rm.datatypes.quantity.DvQuantity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//Interval@629ca1fb[lower=0,lowerIncluded=true,upper=0,upperIncluded=true]
public class DvIntervalVBean extends DataValueAdapter implements I_VBeanWrapper {

    public static final String DATE_TIME_REGEXP = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$";
    public static final String DATE_REGEXP = "^[0-9]{4}-[0-9]{2}-[0-9]{2}";
    public static final String TIME_REGEXP = "^[0-9]{2}:[0-9]{2}:[0-9]{2}$";


    public DvIntervalVBean(DvInterval<DvOrdered<?>> i) {
		this.adaptee = i;
	}

    static Logger log = LogManager.getLogger(DvIntervalVBean.class);
	
	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("lower", ((DvInterval<DvOrdered<?>>)adaptee).getLower());
		map.put("lowerIncluded", ((DvInterval<DvOrdered<?>>)adaptee).isLowerIncluded());
		map.put("upper", ((DvInterval<DvOrdered<?>>)adaptee).getUpper());
		map.put("upperIncluded", ((DvInterval<DvOrdered<?>>)adaptee).isUpperIncluded());
		return map;
	}

    private static boolean isDateTimeString(String expression){
        return expression.matches(DATE_TIME_REGEXP);
    }

    private static boolean isDateString(String expression){
        return expression.matches(DATE_REGEXP);
    }

    private static boolean isTimeString(String expression){
        return expression.matches(TIME_REGEXP);
    }

    private static DvOrdered evaluateIntervalValue(String value){
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(CompositionSerializer.TAG_VALUE, value);

        if (isDateTimeString(value))
            return DvDateTimeVBean.getInstance(valueMap);
        else if (isDateString(value))
            return DvDateVBean.getInstance(valueMap);
        else if (isTimeString(value))
            return DvTimeVBean.getInstance(valueMap);
        else
            return DvQuantityVBean.getInstance(valueMap);
    }

    @Override
    public DvInterval<DvOrdered<?>> parse(String value, String... defaults) {
//        adaptee = ((DvInterval)adaptee).parse(value);
        //value format is <lower>::<upper>
        if (value == null)
            throw new IllegalArgumentException("Invalid DvInterval range passed, was null");

        String range[] = value.split("::");
        if (range.length < 2)
            throw new IllegalArgumentException("Invalid DvInterval range passed, should be in format: <lower>::<upper>, was:"+value);
        //at this stage we know what classes constitute the interval range
//        Class rangeClass = ((DvInterval)this.getAdaptee()).getLower().getClass();
        //we can simply parse with the corresponding object instance
        DvOrdered lower = (DvOrdered) ((DvInterval)this.getAdaptee()).getLower().parse(range[0]);
        DvOrdered upper = (DvOrdered) ((DvInterval)this.getAdaptee()).getUpper().parse(range[1]);

//        adaptee = new DvInterval(evaluateIntervalValue(range[0]), evaluateIntervalValue(range[1]));
        adaptee = new DvInterval(lower, upper);

        return (DvInterval<DvOrdered<?>>) adaptee;
    }
    ///value={interval=
    // {lower={value=2010-01-01T10:00:00, epoch_offset=1.2623148E12},
    // upper={value=2010-01-01T10:00:00, epoch_offset=1.2623148E12},
    // lowerIncluded=true,
    // upperIncluded=true}}
    public static DvInterval getInstance(Map<String, Object> attributes){
        Object value = attributes.get(CompositionSerializer.TAG_VALUE);

        if (value == null)
            throw new IllegalArgumentException("No value in attributes");

        if (value instanceof DvInterval) return (DvInterval)value;

        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map) value;
//            DvInterval<DvQuantity> dvInterval;
            //do some "guess" of the type of the Interval (since it is a specialization of DvOrdered...)
            //get the classname of the inner class
            String innerClassName = attributes.containsKey(CompositionSerializer.INNER_CLASS_LIST) ?
                    ((List<String>)attributes.get(CompositionSerializer.INNER_CLASS_LIST)).get(0) :
                    "DvQuantity"; //default it to DvQuantity...

            //get the instrumentalized class corresponding to the classname
            Class clazz = VBeanUtil.findInstrumentalizedClass(innerClassName);

            if (clazz == null)
                throw new IllegalArgumentException("Could not find an instrument for class:"+innerClassName);

            LinkedTreeMap attributeMap = new LinkedTreeMap();

            LinkedTreeMap lowerValueMap = ((LinkedTreeMap) ((LinkedTreeMap) valueMap.get("interval")).get("lower"));

            attributeMap.put(CompositionSerializer.TAG_VALUE, lowerValueMap);
            DvOrdered lower = (DvOrdered)VBeanUtil.getInstance(clazz, attributeMap);

            LinkedTreeMap upperValueMap = ((LinkedTreeMap) ((LinkedTreeMap) valueMap.get("interval")).get("upper"));

            attributeMap.put(CompositionSerializer.TAG_VALUE, upperValueMap);
            DvOrdered upper = (DvOrdered)VBeanUtil.getInstance(clazz, attributeMap);

            return new DvInterval(lower, upper);
            /*
            if (lowerValueMap.containsKey("magnitude")){ //assumes DvInterval<DvQuantity>
                Map lowerMap = new HashMap<String, Object>();
                lowerMap.put(CompositionSerializer.TAG_VALUE, lowerValueMap);
                DvQuantity lowerQuantity = DvQuantityVBean.getInstance(lowerMap);

                //dito for the upper values
                LinkedTreeMap upperValueMap = ((LinkedTreeMap) ((LinkedTreeMap) valueMap.get("interval")).get("upper"));
                Map upperMap = new HashMap<String, Object>();
                upperMap.put(CompositionSerializer.TAG_VALUE, upperValueMap);
                DvQuantity upperQuantity = DvQuantityVBean.getInstance(upperMap);

                return new DvInterval(lowerQuantity, upperQuantity);

            } else if (lowerValueMap.containsKey("value")){
                LinkedTreeMap upperValueMap = ((LinkedTreeMap) ((LinkedTreeMap) valueMap.get("interval")).get("upper"));
                return new DvInterval(evaluateIntervalValue(lowerValueMap.get("value").toString()), evaluateIntervalValue(upperValueMap.get("value").toString()));

            }
            else {
                log.warn("Could not identify Interval type:"+valueMap);
            }
            */
        }
        throw new IllegalArgumentException("Could not get instance");
    }

    public static DvInterval generate(){
        return new DvInterval(new DvCount(0), new DvCount(1));
    }

    public static DvInterval createQualifiedInterval(Class orderedClass){
        try {
            Class instrumentalizedClass = VBeanUtil.findInstrumentalizedClass(orderedClass.getSimpleName());
            Method generator = instrumentalizedClass.getMethod("generate", null);
            Object orderedObject = generator.invoke(null, null);
            //find increment method to ensure lower < upper
            Method incrementor = instrumentalizedClass.getMethod("increment", new Class[]{orderedClass});

            if (orderedObject instanceof DvOrdered){
                return new DvInterval((DvOrdered)orderedObject, (DvOrdered)incrementor.invoke(null, orderedObject));
            }
            else
                throw new IllegalArgumentException("Invalid ordered type in interval:"+orderedClass.getName());

        } catch (Exception e){
            throw new IllegalArgumentException("Invalid ordered type in interval:"+orderedClass.getName());
        }
    }
}
