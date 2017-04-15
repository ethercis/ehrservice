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
package org.openehr.build;

import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.changecontrol.Contribution;
import org.openehr.rm.common.changecontrol.OriginalVersion;
import org.openehr.rm.common.generic.*;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.*;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.DataStructure;
import org.openehr.rm.datastructure.history.Event;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.IntervalEvent;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.*;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.basic.DvState;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.*;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.demographic.*;
import org.openehr.rm.ehr.EHR;
import org.openehr.rm.ehr.EHRAccess;
import org.openehr.rm.ehr.EHRStatus;
import org.openehr.rm.support.identification.*;
import org.openehr.rm.support.identification.UUID;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reference model class instances builder
 *
 * @author Rong Chen
 * @version 1.0
 */
public class RMObjectBuilder {

	public static RMObjectBuilder getInstance(Map<SystemValue, Object> systemValues) {
		return new RMObjectBuilder(systemValues);
	}

	public static RMObjectBuilder getInstance() {
		return defaultInstance;
	}

	public RMObjectBuilder(Map<SystemValue, Object> systemValues) {
		if (systemValues == null) {
			throw new NullPointerException("systemValues cannot be null");
		}
		this.systemValues = systemValues;

		loadTypeMap();
	}

	public Map<SystemValue, Object> getSystemValues() {
		return systemValues;
	}

	public void setSystemValues(Map<SystemValue, Object> systemValues) {
		this.systemValues = systemValues;
	}

	public RMObjectBuilder() {
		this(new HashMap<SystemValue, Object>());
	}

	private void loadTypeMap() {
		Class<?>[] classes = {
				// implied types
				Integer.class,         String.class,          Boolean.class,         Double.class,

				// common classes
				PartySelf.class,       Archetyped.class,      Attestation.class,     AuditDetails.class,
				Participation.class,   PartyIdentified.class, PartyRelated.class,	 PartySelf.class,
				OriginalVersion.class, Contribution.class,

				// support classes
				TerminologyID.class,   ArchetypeID.class,     HierObjectID.class,    AccessGroupRef.class,
				GenericID.class,       InternetID.class,      ISO_OID.class,         LocatableRef.class,
				ObjectVersionID.class, ObjectRef.class,       PartyRef.class,        TemplateID.class,
				TerminologyID.class,   UUID.class,            VersionTreeID.class,

				// datatypes classes
				DvBoolean.class,       DvState.class,         DvIdentifier.class,    DvText.class,
				DvCodedText.class,     DvParagraph.class,     CodePhrase.class,      DvCount.class,
				DvOrdinal.class,       DvQuantity.class,      DvInterval.class,      DvProportion.class,
				ProportionKind.class,  DvDate.class,          DvDateTime.class,      DvTime.class,
				DvDuration.class,      DvParsable.class,      DvURI.class,           DvEHRURI.class,
				DvMultimedia.class,    TermMapping.class,     Match.class,

				// datastructure classes
				Element.class,         Cluster.class,         ItemSingle.class,      ItemList.class,
				ItemTable.class,       ItemTree.class,        History.class,         IntervalEvent.class,
				PointEvent.class,
			
				// domain classes
				Action.class,          Activity.class,        Evaluation.class,      ISMTransition.class,
				Instruction.class,     InstructionDetails.class, Observation.class,  AdminEntry.class,
				Section.class,         Composition.class,     EventContext.class,    EHRStatus.class,
				EHR.class, EHRAccess.class,
				
				// demographic classes
				Address.class,         PartyIdentity.class,   Agent.class,           Group.class,
				Organisation.class,    Person.class,          Contact.class,         PartyRelationship.class, 
				Role.class,            Capability.class,
                
                // abstract classes
                Locatable.class,       ItemStructure.class,   DataStructure.class,   Event.class,
                Item.class,            ContentItem.class,     Entry.class,           CareEntry.class,
                Party.class,           Actor.class,
		};

		Map<String, Class<?>> newTypeMap = new HashMap<String, Class<?>>();
		for (Class<?> klass : classes) {
			String name = klass.getSimpleName();
			newTypeMap.put(name.toUpperCase(), klass);
		}
		typeMap = Collections.unmodifiableMap(newTypeMap);
	}

	/**
	 * Return a map with name as the key and index of position as the value for required parameters of the full constructor
	 * in the RMObject
	 *
	 * @return empty map if the {@link FullConstructor} is not found
	 */
	public SortedMap<String, Class<?>> getAttributeTypes(Class<?> rmClass) {
		SortedMap<String, Class<?>> map = new TreeMap<String, Class<?>>();
		Constructor constructor = fullConstructor(rmClass);
		if (constructor == null) {
			return map;
		}
		Annotation[][] annotations = constructor.getParameterAnnotations();
		Class<?>[] types = constructor.getParameterTypes();

		if (annotations.length != types.length) {
			throw new IllegalArgumentException("less annotations");
		}
		for (int i = 0; i < types.length; i++) {
			if (annotations[i].length == 0) {
				throw new IllegalArgumentException("missing annotations of attribute " + i);
			}
			Attribute attribute = (Attribute) annotations[i][0];
			map.put(attribute.name(), types[i]);
		}
		return map;
	}

	/**
	 * Return a map with name as the key and index of position as the value for all parameters of the full constructor
	 * in the RMObject
	 *
	 * @return empty map if the {@link FullConstructor} is not found
	 */
	public SortedMap<String, Integer> getAttributeIndices(Class<?> rmClass) {
		SortedMap<String, Integer> map = new TreeMap<String, Integer>();
		Constructor constructor = fullConstructor(rmClass);
		if (constructor == null) {
			return map;
		}
		Annotation[][] annotations = constructor.getParameterAnnotations();

		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].length == 0) {
				throw new IllegalArgumentException("missing annotation at position " + i);
			}
			Attribute attribute = (Attribute) annotations[i][0];
			map.put(attribute.name(), i);
		}
		return map;
	}

	/**
	 * Return a map with name as the key and index of position as the value for all parameters of the full constructor
	 * in the RMObject
	 *
	 * @return empty map if the {@link FullConstructor} is not found
	 */
	public SortedMap<String, Attribute> getAttributes(Class<?> rmClass) {
		SortedMap<String, Attribute> map = new TreeMap<String, Attribute>();
		Constructor constructor = fullConstructor(rmClass);
		if (constructor == null) {
			return map;
		}

		Annotation[][] annotations = constructor.getParameterAnnotations();

		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].length == 0) {
				throw new IllegalArgumentException("missing annotation at position " + i);
			}
			Attribute attribute = (Attribute) annotations[i][0];
			map.put(attribute.name(), attribute);
		}
		return map;
	}

	/**
	 * Find the {@link FullConstructor} for a particular class.
	 *
	 * @return null if not found
	 */
	public Constructor<?> fullConstructor(Class<?> rmClass) {
        if (Modifier.isAbstract(rmClass.getModifiers()))
        {
            return null;
        }
		Constructor<?>[] array = rmClass.getConstructors();
		for (Constructor<?> constructor : array) {
			if (constructor.isAnnotationPresent(FullConstructor.class)) {
				return constructor;
			}
		}
		return null;
	}

	/**
	 * Construct an instance of RM class of given name and values. <p> If the input is a string, and the required
	 * attribute is some other types (integer, double etc), it will be converted into right type. if there is any error
	 * during conversion, AttributeFormatException will be thrown.
	 *
	 * @return created instance
	 */
	public RMObject construct(String rmClassName, Map<String, Object> valueMap) throws Exception {

        Class<?> rmClass = retrieveRMType(rmClassName);

        valueMap = renameAttributes(valueMap);
        Constructor constructor = fullConstructor(rmClass);
        if (constructor == null) {
            throw new RMObjectBuildingException(String.format("Missing @FullConstructor for class %s",rmClassName));
        }
        Map<String, Class<?>> typeMap = getAttributeTypes(rmClass);
        Map<String, Integer> indexMap = getAttributeIndices(rmClass);
        Map<String, Attribute> attributeMap = getAttributes(rmClass);
        Object[] valueArray = new Object[indexMap.size()];
        I_VBeanWrapper value_wrapper = null;


        for (String name : typeMap.keySet()) {
            Class<?> type = typeMap.get(name);
            Integer index = indexMap.get(name);
            Attribute attribute = attributeMap.get(name);
            if (index == null || type == null || attribute == null) {
                throw new RMObjectBuildingException(String.format("unknown attribute %s", name));
            }

            Object value = valueMap.get(name);

            if (value instanceof I_VBeanWrapper) {
                value_wrapper = (I_VBeanWrapper) value;
                value = ((I_VBeanWrapper) value).getAdaptee();
                value = getParameter(value, type, attribute);
                value_wrapper.setAdaptee(value);
            }
            else {
                value = getParameter(value, type, attribute);
            }
            valueArray[index] = value;
        }

        try {
			Object ret = constructor.newInstance(valueArray);
			return (RMObject) ret;
		} catch (Exception e) {

			if (e instanceof AttributeFormatException || e instanceof AttributeMissingException) {
				throw new IllegalArgumentException(String.format("failed to create new instance of %s: %s, valueMap: %s", rmClassName, e.getMessage(), toString(valueMap), e), e);
			} else if (e instanceof AttributeMissingException) {
				throw new IllegalArgumentException(String.format("failed to create new instance of %s: %s, valueMap: %s",rmClassName, e.getMessage(), toString(valueMap), e), e);
			}

			if (stringParsingTypes.contains(rmClassName)) {
				throw new IllegalArgumentException(String.format("failed to create new instance of %s: wrong format for type", rmClassName), e);
			}

            if (e instanceof InvocationTargetException)
            {
                Throwable target = ((InvocationTargetException) e).getTargetException();
                if (target instanceof Exception)
                {
                    e = (Exception)target;
                }
            }

			throw new IllegalArgumentException(String.format("failed to create new instance of %s: %s: %s, valueMap: %s",rmClassName, e.getClass().getSimpleName(), e.getMessage(), toString(valueMap), e), e);
		}
	}

	public Class<?> retrieveRMType(String rmClassName) throws RMObjectBuildingException {
		rmClassName = toClassName(rmClassName);
        int idx = rmClassName.indexOf("<");
        if (idx != -1)
        {
            rmClassName = rmClassName.substring(0, idx);
        }
		Class<?> rmClass = typeMap.get(rmClassName.toUpperCase());
		if (rmClass == null) {
			throw new RMObjectBuildingException(String.format("RM type unknown: %s", rmClassName));
		}
		return rmClass;
	}

	public Map<String, Class<?>> retrieveAttribute(String rmClassName)throws RMObjectBuildingException {
		Class<?> rmClass = retrieveRMType(rmClassName);
		if (Modifier.isAbstract(rmClass.getModifiers())) {
			throw new RMObjectBuildingException(String.format("RM type abstract: %s", rmClassName));
		}
		Map<String, Class<?>> map = getAttributeTypes(rmClass);
		return map;
	}

	public boolean isEnumType(String rmClassName)throws RMObjectBuildingException{
		Class<?> rmClass = retrieveRMType(rmClassName);
		return rmClass.isEnum();
	}

	public String toFieldName(String input) {
		if (input == null) {
			return null;
		}
		String result = toClassName(input);

		int length = result.length();
		if (length == 0) {
			return result;
		} else {
			return result.substring(0, 1).toLowerCase() + result.substring(1);
		}
	}

	public String toAttributeName(String input) {
		if (input == null) {
			return null;
		}
		String result = toRmEntityName(input);
		result = result.toLowerCase();
		return result;
	}

	public String toClassName(String input) {
		if (input == null) {
			return null;
		}
		String stripped = StringUtils.strip(input);
// this breaks DvInterval<DvDate>
//		if (stripped.startsWith("<")) {
//			stripped = stripped.substring(1);
//		}
//		if (stripped.endsWith(">")) {
//			stripped = stripped.substring(0, stripped.length() - 1);
//		}

		String[] array = StringUtils.splitByCharacterTypeCamelCase(stripped);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			String s = array[i];
            if (s.startsWith("<") || s.startsWith(">")) { // DV_INTERVAL<DV_DATE>
                buf.append(s);
                continue;
            }
			if (!startsWithALetter(s)) {
				continue;
			}
			buf.append(StringUtils.capitalize(s.toLowerCase()));
		}
		return buf.toString();
	}

	public String toRmEntityName(String input) {
		if (input == null) {
			return null;
		}
		String stripped = StringUtils.strip(input);
// this breaks DV_INTERVAL<DV_DATE>
//		if (stripped.startsWith("<")) {
//			stripped = stripped.substring(1);
//		}
//		if (stripped.endsWith(">")) {
//			stripped = stripped.substring(0, stripped.length() - 1);
//		}

		String[] array = StringUtils.splitByCharacterTypeCamelCase(stripped);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			String s = array[i];
            if (s.startsWith("<") || s.startsWith(">")) { // DvInterval<DvDate>
                if (buf.length() > 0) {
                    int last = buf.length()-1;
                    char c = buf.charAt(last);
                    if ('_' == c) {
                        buf.deleteCharAt(last);
                    }
                }
                buf.append(s);
                continue;
            }
			if (!startsWithALetter(s)) {
				continue;
			}
			buf.append(s.toUpperCase());
			buf.append('_');
		}
		if (buf.length() > 0 && buf.charAt(buf.length() - 1) == '_') {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	private boolean startsWithALetter(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		char first = s.charAt(0);
		int characterType = Character.getType(first);
		if (characterType == Character.LOWERCASE_LETTER || characterType == Character.UPPERCASE_LETTER) {
			return true;
		}
		return false;
	}

	/**
	 * Finds the matching RM class that can be used to create RM object for given value map
	 *
	 * @throws RMObjectBuildingException if no matching RM class is found
	 */
	public String findMatchingRMClass(Map<String, Object> valueMap) throws RMObjectBuildingException {
		OUTER:
		for (Class<?> rmClass : typeMap.values()) {
			String rmClassName = rmClass.getSimpleName();

			if (skippedTypesInMatching.contains(rmClassName)) {
				continue; // skip simple value types
			}

			if (Modifier.isAbstract(rmClass.getModifiers())) {
				continue; // skip abstract classes
			}

			if (rmClass.isEnum()) {
				continue; // skip enums
			}

			valueMap = renameAttributes(valueMap);

			Constructor constructor = fullConstructor(rmClass);
			if (constructor == null) {
				throw new RMObjectBuildingException(String.format("annotated constructor missing for %s",
						rmClassName));
			}

			Annotation[][] annotations = getParameterAnnotations(rmClass, constructor);

			Class<?>[] types = constructor.getParameterTypes();
			Set<String> attributes = new HashSet<String>();
			Set<String> systemAttributes = new HashSet<String>();

			// check the value for each attribute is of the right type
			for (int i = 0; i < types.length; i++) {
				if (annotations[i].length == 0) {
					throw new RMObjectBuildingException(String.format("attribute annotation missing for %s",
							rmClassName));
				}
				Attribute attribute = (Attribute) annotations[i][0];
				String name = attribute.name();
				attributes.add(name);

				if (attribute.system()) {
					systemAttributes.add(name);
					continue;
				}

				Object value = valueMap.get(name);

				if (attribute.required() && value == null) {
					continue OUTER;
				} else if (value != null) {
					Class<?> type = types[i];

					if (type.isPrimitive()) {
						type = ClassUtils.primitiveToWrapper(type);
					}

					if (!type.isInstance(value)) {
						continue OUTER;
					}
				}
			}

			// check each non-system attribute is needed for this constructor
			for (String attr : valueMap.keySet()) {
				if (systemAttributes.contains(attr)) {
					continue;
				}
				if (!attributes.contains(attr)) {
					continue OUTER;
				}
			}

			// completed all checks, this one matches
			return rmClassName;
		}

		throw new RMObjectBuildingException(String.format("no RM type matches valueMap %s", toString(valueMap)));
	}

	private Object getParameter(Object value, Class<?> parameterType, Attribute attribute)
			throws RMObjectBuildingException {

		value = getSystemParameter(attribute, value);

		if (value == null) {
			checkRequired(attribute);
			return defaultValue(parameterType);
		} else if (parameterType.isEnum() && !value.getClass().isEnum()) {
			if (parameterType.equals(ProportionKind.class)) {
				value = ProportionKind.fromValue(Integer.parseInt(value.toString()));
			} else {
				@SuppressWarnings("unchecked")
				Class<? extends Enum> enumType = (Class<? extends Enum>) parameterType;
				value = Enum.valueOf(enumType, value.toString());
			}
        } else if (CharSequence.class.isAssignableFrom(parameterType)) {
      	    value = String.valueOf(value);
		} else if (value.getClass().isArray()) {
			if (Set.class.isAssignableFrom(parameterType)) {
                value = toSet(value);
			} else if (Collection.class.isAssignableFrom(parameterType)) {
                value = toList(value);
			}
		} else if (!parameterType.isPrimitive()) {
			tryCast(parameterType, value instanceof I_VBeanWrapper ? ((I_VBeanWrapper) value).getAdaptee() : value);
		} else {
            value = convertPrimitive(parameterType, value);
        }

		return value;
	}

	private void tryCast(Class<?> parameterType, Object value)
			throws RMObjectBuildingException {
		try {
			parameterType.cast(value);
		} catch (ClassCastException e) {
			throw new RMObjectBuildingException(String.format("wrong type %s, expected %s",
					value.getClass().getSimpleName(), parameterType));
		}
	}

	private Object toSet(Object value) {
		Object[] array = (Object[]) value;
		Set<Object> set = new HashSet<Object>();
		for (Object o : array) {
			set.add(o);
		}
		return set;
	}

	private Object toList(Object value) {
		Object[] array = (Object[]) value;
		List<Object> list = new ArrayList<Object>();
		for (Object o : array) {
			list.add(o);
		}
		return list;
	}

	private Object convertPrimitive(Class<?> type, Object value)
			throws AttributeFormatException {
        try {
			if (type.equals(int.class) || type.equals(Integer.class)) {
                if (!(value instanceof Integer)) {
                    value = Integer.parseInt(String.valueOf(value));
                }
			} else if (type.equals(short.class) || type.equals(Short.class)) {
                if (!(value instanceof Short)) {
                    value = Short.parseShort(String.valueOf(value));
                }
			} else if (type.equals(long.class) || type.equals(Long.class)) {
                if (!(value instanceof Long)) {
                    value = Long.parseLong(String.valueOf(value));
                }
			} else if (type.equals(double.class) || type.equals(Double.class)) {
                if (!(value instanceof Double)) {
                    value = Double.parseDouble(String.valueOf(value));
                }
			} else if (type.equals(float.class) || type.equals(Float.class)) {
                if (!(value instanceof Float)) {
                    value = Float.parseFloat(String.valueOf(value));
                }
			} else if (type.equals(byte[].class) || type.equals(Byte[].class)) {
                if (!(value instanceof Byte)) {
                    value = String.valueOf(value).getBytes();
                }
			} else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                if (!(value instanceof Boolean)) {
                    value = Boolean.parseBoolean(String.valueOf(value));
                }
			} else if (type.equals(char.class) || type.equals(Character.class)) {
                if (!(value instanceof Character)) {
                    String str = String.valueOf(value);
    				if (str.length() > 0) {
    					value = str.charAt(0);
    				} else {
    					throw new AttributeFormatException(String.format("wrong format, expect %s", type));
    				}
                }
			}
		} catch (NumberFormatException e) {
			throw new AttributeFormatException(String.format("wrong format expect %s: %s",
					type, e.getMessage(), e));
		}
		return value;
	}

	private void checkRequired(Attribute attribute) throws AttributeMissingException {
		if (attribute.required()) {
			throw new AttributeMissingException(String.format("missing value for required attribute '%s'",
					attribute.name()));
		}
	}

	private Object getSystemParameter(Attribute attribute, Object value)
			throws RMObjectBuildingException {
		String parameter = attribute.name();
		if (attribute.system()) {
			SystemValue sysValue = SystemValue.fromId(parameter);
			if (sysValue == null) {
				throw new RMObjectBuildingException(String.format("unknown system value %s", parameter));
			}
			Object systemValue = systemValues.get(sysValue);
			if (systemValue == null) {
				if (value == null) {
					throw new AttributeMissingException(String.format(
							"missing value for system attribute %s", parameter));
				}
				// No system value for system attribute %s, using mapped value
			} else {
				value = systemValue;
			}
		}
		return value;
	}

	private String toString(Map<String, Object> valueMap) {
		if (valueMap.isEmpty()) {
			return "{}";
		}
		StringBuffer buf = new StringBuffer();
		buf.append("{\n");
		for (String key : valueMap.keySet()) {
			buf.append("    ");
			buf.append(key);
			buf.append("=");
			Object value = valueMap.get(key);
			if (value != null) {
				buf.append(value.getClass().getName());
				buf.append(":");
				buf.append(value.toString());
			} else {
				buf.append("null");
			}
			buf.append(",\n");
		}
		buf.deleteCharAt(buf.length() - 1);
		buf.deleteCharAt(buf.length() - 1);
		buf.append("}");
		return buf.toString();
	}

	private Annotation[][] getParameterAnnotations(Class<?> rmClass, Constructor constructor) {
		Annotation[][] annotations = constructor.getParameterAnnotations();
		if (annotations == null || annotations.length == 0) {
			throw new IllegalStateException("attribute annotations missing for "
					+ rmClass);
		}
		return annotations;
	}

	private Map<String, Object> renameAttributes(Map<String, Object> valueMap) {
		// replace underscore separated names with camel case
		Map<String, Object> filteredMap = new HashMap<String, Object>();
		for (String name : valueMap.keySet()) {
			filteredMap.put(toFieldName(name), valueMap.get(name));
		}
		return filteredMap;
	}

	public Object defaultValue(Class<?> type) {
		if (type == boolean.class) {
			return Boolean.FALSE;
		} else if (type == double.class) {
			return new Double(0);
		} else if (type == float.class) {
			return new Float(0);
		} else if (type == int.class) {
			return new Integer(0);
		} else if (type == short.class) {
			return new Short((short) 0);
		} else if (type == long.class) {
			return new Long(0);
		} else if (type == char.class) {
			return new Character((char) 0);
		} else if (type == byte.class) {
			return new Byte((byte) 0);
		}
		return null;
	}

	public boolean isOpenEHRRMClass(Object obj) {
		return obj.getClass().getName().contains(OPENEHR_RM_PACKAGE);
	}

	public Method getter(String attributeName, Class<?> klass) {
		Method[] methods = klass.getMethods();
		String name = "get" + attributeName.substring(0, 1).toUpperCase() +
				attributeName.substring(1);

		for (Method method : methods) {
			if (method.getName().equals(name)) {
				Type[] paras = method.getParameterTypes();
				if (paras.length == 0) {
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves Map of attribute classes indexed by names of given class
	 */
	public Map<String, Class<?>> retrieveRMAttributes(String rmClassName) throws RMObjectBuildingException {
		Class<?> rmClass = retrieveRMType(rmClassName);
		Map<String, Class<?>> map = getAttributeTypes(rmClass);
		Map<String, Class<?>> ret = new HashMap<String, Class<?>>();
		for (String name : map.keySet()) {
			ret.put(toRmEntityName(name), map.get(name));
		}
		return ret;
	}

	public static final String OPENEHR_RM_PACKAGE = "org.openehr.rm.";

	protected static final Set<String> skippedTypesInMatching;

	protected static final Set<String> stringParsingTypes;

	protected static final RMObjectBuilder defaultInstance = new RMObjectBuilder();

	static {
		Set<String> newStringParsingTypes = new HashSet<String>();
		String[] types = {
				"DvDate",              "DvDateTime",          "DvTime",               "DvDuration",
		};
		newStringParsingTypes.addAll(Arrays.asList(types));
		stringParsingTypes = Collections.unmodifiableSet(newStringParsingTypes);

		Set<String> newSkippedTypesInMatching = new HashSet<String>();
		String[] skippedTypes = {
				// don't match the primitives
				"Integer",             "String",              "Boolean",             "Double",
				// don't match these because these clash with DvText
				"DvDateTime",          "DvDate",              "DvTime",              "DvDuration",
				"TerminologyID",       "ArchetypeID",         "TemplateID",          "ISO_OID",
				"HierObjectID",        "DvBoolean",           "InternetID",          "UUID",
				"ObjectVersionID",     "DvURI",               "DvEHRURI",
				// don't match these because we prefer ItemList
				"Cluster",             "Section",             "ItemTree"
		};
		newSkippedTypesInMatching.addAll(Arrays.asList(skippedTypes));
		skippedTypesInMatching = Collections.unmodifiableSet(newSkippedTypesInMatching);
	}

	protected Map<SystemValue, Object> systemValues;
	protected Map<String, Class<?>> typeMap;

}

