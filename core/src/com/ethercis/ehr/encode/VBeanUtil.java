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
package com.ethercis.ehr.encode;

import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import org.apache.log4j.Logger;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.basic.DataValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class VBeanUtil {
    private static String instrSuffix = "VBean";
    private static String constraintsSuffix = "VConstraints";

    private static String packageName = "com.ethercis.ehr.encode.wrappers";

    public static String TAG_VALUE_AS_STRING = "/VALUE_AS_STRING";

    static private Logger log = Logger.getLogger(VBeanUtil.class);
	
	/**
	 * identifies if a instrumentalization of the class exists.<p>
	 * the convention is an instrumentalized class is named after the class as<p>
	 * &lt;classname&gt;<b>VBean</b><p>
	 * the instrument class must be defined in package<p>
	 * <pre>com.ethercis.com.ethercis.ehr.service.encode.types</pre><br>
	 * further, the instrument class must implement<br>
     *      <ul>
	 *          <li> a constructor to wrap the parent class instance</li>
	 *          <li> a public method 'getFieldMap()' returning a Map</li>
	 *      </ul>
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean isInstrumentalized(Object object) {
		String clazzname = object.getClass().getSimpleName();
		try {
			Class<?> instrClazz = Class.forName(packageName+"."+clazzname+instrSuffix);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static Class findInstrumentalizedClass(Object object) {;
		String clazzname = object.getClass().getSimpleName();
		try {
			Class<?> instrClazz = Class.forName(packageName+"."+clazzname+instrSuffix);
			return instrClazz;
		} catch (ClassNotFoundException e) {
            log.warn("Could not process an instrument for class "+clazzname);
			return null;
		}
	}


	/**
	 * wrap an object using the instrument class constructor
	 * @param anobject
	 * @return
	 */
	public static Object wrapObject(Object anobject) {
		String clazzname = anobject.getClass().getSimpleName();
		try {
			Class<?> instrClazz = Class.forName(packageName+"."+clazzname+instrSuffix);
			Constructor<?> constructor;
			try {
				constructor = instrClazz.getDeclaredConstructor(new Class[] {anobject.getClass()});
			} catch (SecurityException e) {
				return null;
			} catch (NoSuchMethodException e) {
				return null;
			}
			
			try {
				return constructor.newInstance(anobject);
			} catch (InstantiationException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}
			
		} catch (ClassNotFoundException e) {
			return null;
		}
		
	}
	
	/**
	 * wrap an object using the instrument class constructor
	 * @param anobject
	 * @return
	 */
	public static Object wrapObject(Class<?> instrClazz, Object anobject) {
		Constructor<?> constructor;
		try {
			constructor = instrClazz.getDeclaredConstructor(new Class[] {anobject.getClass()});
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
		
		try {
			return constructor.newInstance(anobject);
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
		
	}
	/**
	 * returns the map of field=value contained in the object
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static  Map<String, Object> getFieldMap(Object object){
		Class<?> instrClazz = object.getClass();
		Method getter;
		try {
			getter = instrClazz.getDeclaredMethod("getFieldMap", new Class[] {});
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}

		try {
			return (Map<String, Object>) getter.invoke(object, new Object[] {});
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}

	}
	
	/**
	 * utility to consistently set the value map for a datavalue
	 * @param valuemap
	 * @param value
	 * @throws Exception
	 */
	public static void setValueMap(Map<String, Object> valuemap, DataValue value) throws Exception {
		
		Class<?> instrClass;
		
		if (value.getClass().getName().contains("VBean")) { //instrument class can be used directly
			instrClass = value.getClass();
		}
		else
			instrClass = findInstrumentalizedClass(value);
		
		if (instrClass != null) {
			Object wrapped = VBeanUtil.wrapObject(instrClass, value);
			Map<String, Object> fieldmap = VBeanUtil.getFieldMap(wrapped);
			for (String fieldname: fieldmap.keySet()) {
				try {
					valuemap.put("/"+fieldname, fieldmap.get(fieldname));
				} catch (IllegalArgumentException e) {
					Logger.getLogger(VBeanUtil.class).error("duplicate field:"+fieldname+" Exception"+e);
					throw new Exception("duplicate field:"+fieldname+", please fix the input structure");
				}
			}
		} 
		else
			valuemap.put(TAG_VALUE_AS_STRING, value);
	}

    public static boolean isConstraintImplemented(Object object) {
        String clazzname = object.getClass().getSimpleName();
        try {
            Class<?> instrClazz = Class.forName(packageName+".constraints."+clazzname+constraintsSuffix);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class findConstraintClass(Object object) {;
        String clazzname = object.getClass().getSimpleName();
        try {
            Class<?> instrClazz = Class.forName(packageName+".constraints."+clazzname+constraintsSuffix);
            return instrClazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static DataValueConstraints getConstraintInstance(RMObjectBuilder builder, Object obj) throws Exception {
        if (!(obj instanceof DataValue))
            throw new Exception("invalid object");

        if (isConstraintImplemented(obj)){
            Class implementingClass = findConstraintClass(obj);
            try {
                Constructor<?> constructor = implementingClass.getConstructor(new Class[]{RMObjectBuilder.class, DataValue.class});
                return (DataValueConstraints)constructor.newInstance(builder, (DataValue)obj);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
