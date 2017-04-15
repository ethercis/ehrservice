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

package com.ethercis.ehr.building;

import com.ethercis.ehr.building.filter.FilterOutNil;
import com.ethercis.ehr.encode.DataValueAdapter;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.binding.XMLBindingException;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.SystemValue;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.quantity.ProportionKind;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.measurement.MeasurementService;
import org.openehr.rm.support.measurement.SimpleMeasurementService;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.schemas.v1.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

/**
 * Bind data from XMLBeans class to openEHR RM classes
 *
 * @author Rong.Chen
 * @author minor modifications by Erik Sundvall, Linköping University
 */
public class XMLBinding {
	Logger log = LogManager.getLogger(XMLBinding.class);
	boolean anyElement = false;
	
	public XMLBinding(Map<SystemValue, Object> values) {
		if (values == null) {
			throw new NullPointerException("values cannot be null");
		}
		init(values);
	}

	public XMLBinding() {
		this(false);
	}


	public XMLBinding(boolean anyElement) {
		this.anyElement = anyElement;
		TerminologyService terminologyService;
		try {
			terminologyService = TerminologyServiceWrapper.getInstance();
		} catch (Exception e){
			throw new IllegalArgumentException("Could not instantiate terminology service:"+e);
		}
		MeasurementService measurementService = SimpleMeasurementService.getInstance();
		CodePhrase charset = new CodePhrase("IANA_character-sets","UTF-8");
		CodePhrase lang = new CodePhrase("ISO_639-1", "en"); //defaulted to English


		Map<SystemValue, Object> values = new HashMap<SystemValue, Object>();
		values.put(SystemValue.TERMINOLOGY_SERVICE, terminologyService);
		values.put(SystemValue.MEASUREMENT_SERVICE, measurementService);
		values.put(SystemValue.CHARSET, charset);
		values.put(SystemValue.LANGUAGE, lang);
		
		init(values);
	}

	public Object bindToXML(Object obj) throws XMLBindingException {
		return bindToXML(obj, false);
	}

	/**
	 * Binds data from reference model instance to XML binding classes
	 */
	@SuppressWarnings("ConstantConditions")
	public Object bindToXML(Object obj, boolean asDocument) throws XMLBindingException {
		if (obj == null) {
			return null;
		}

        //take the wrapped item element for an ElementWrapper
        if (obj instanceof ElementWrapper){
            ElementWrapper wrapper = (ElementWrapper) obj;
            if (anyElement || wrapper.dirtyBitSet())
                obj = wrapper.getAdaptedElement();
            else
                return null;
        }

		String className = obj.getClass().getSimpleName();
		String xmlClassName = className;
		if ("EHRStatus".equalsIgnoreCase(className)) {
			// deal with case-sensitively challenged class name
			xmlClassName = "EhrStatus";
		}
		else if ("Match".equalsIgnoreCase(className)){
			xmlClassName = "MatchString";
		}

		Method[] methods = obj.getClass().getMethods();

		try {
			Class<?> xmlClass = null;
			Object xmlObj = null;

			if (asDocument) {
				// when serializing back to XML strings, XMLBeans needs a Document wrapper to be able to write a 
				// proper root element. If it doesn't have one, it will output an <xml-fragment/>
				Class<?> factoryClass;
				try {
					factoryClass = Class.forName(XML_BINDING_PACKAGE + 	xmlClassName + "Document$Factory");
				} catch (ClassNotFoundException e) {
					factoryClass = Class.forName(XML_BINDING_PACKAGE + 	xmlClassName.toUpperCase() + "Document$Factory");
				} catch (NoClassDefFoundError e) {
					factoryClass = Class.forName(XML_BINDING_PACKAGE + xmlClassName.toUpperCase() + "Document$Factory");
				}

				Method factoryMethod = factoryClass.getMethod(NEW_INSTANCE, XmlOptions.class);
				Object documentObj = factoryMethod.invoke(null, xmlOptions);
				Class<?> documentClass = documentObj.getClass();
				Method[] documentMethods = documentClass.getMethods();
				boolean found = false;
				for (int i = 0; i < documentMethods.length; i++) {
					Method documentMethod = documentMethods[i];
					if (documentMethod.getName().startsWith("addNew")) {
						xmlObj = documentMethod.invoke(documentObj);
						xmlClass = xmlObj.getClass();
						found = true;
						break;
					}
				}
				if (!found) {
					throw new XMLBindingException("Could not process XXXDocument.addNewXXX() method to invoke");
				}
			} else {
				xmlClass = Class.forName(XML_BINDING_PACKAGE + 	("MatchString".equals(xmlClassName) ? xmlClassName : xmlClassName.toUpperCase()));

				Class<?> factoryClass = xmlClass.getClasses()[0];
				Method factoryMethod = factoryClass.getMethod(NEW_INSTANCE, XmlOptions.class);
				xmlObj = factoryMethod.invoke(null, xmlOptions);
			}

			Map<String, Class<?>> attributes = builder.retrieveAttribute(className);
			Set<String> attributeNames = attributes.keySet();
			Object attributeValue;
			Method setterMethod;

			if (builder.isEnumType(className)){
				//manage the builder in a simplistic manner
				Class enumClass = builder.retrieveRMType(className);
				Method getterMethod = enumClass.getMethod("getValue", null);
				return getterMethod.invoke(obj, null);
//				setterMethod = findSetter(null, xmlClass, false);
//				setterMethod.invoke(xmlObj, attributeValue);
//				return xmlObj;
			}

			for (Method method : methods) {

				String name = method.getName();

				// cause dead-loop
				if ("getParent".equals(name)) {
					continue; //
				}

				if (isGetter(name, attributeNames)) {
					if (method.getParameterTypes().length > 0) {
						continue;
					}

					attributeValue = method.invoke(obj, null);
					if (attributeValue == null) {
						continue;
					}

					boolean isCollection = false;

					if (attributeValue.getClass().isArray()) {
						Object[] array = (Object[]) attributeValue;
						if (array.length == 0) {
							continue;
						}
						Object[] done = new Object[array.length];
						for (int i = 0; i < array.length; i++) {
							done[i] = bindToXML(array[i]);
						}
						attributeValue = done;

					} else if (ProportionKind.class.equals(attributeValue.getClass())) {
						ProportionKind kind = (ProportionKind) attributeValue;
						attributeValue = BigInteger.valueOf(kind.getValue());

					} else if (builder.isOpenEHRRMClass(attributeValue)) {
                        if (attributeValue instanceof ItemTree){
                            ItemTree itemTree = (ItemTree)attributeValue;
                            if (itemTree.getItems() == null || itemTree.getItems().size() == 0)
                                continue;
                        }

						attributeValue = bindToXML(attributeValue);

					} else if (Collection.class.isAssignableFrom(attributeValue.getClass())) {
						isCollection = true;
                        Collection<?> collection = (Collection<?>) attributeValue;

						String attributeName = getAttributeNameFromGetter(name);
						setterMethod = findSetter(attributeName, xmlClass, isCollection);

						Method addNew = findAddNew(attributeName, xmlClass);

                        Iterator<?> it = collection.iterator();
						for (int i = 0; it.hasNext(); i++) {
							Object value = it.next();

                            //if the returned value is null, it is probably that the dirtyBit is false (unmodified value)
                            if (value == null)
                                continue;

							Object[] array = new Object[2];
							addNew.invoke(xmlObj, null);
							array[0] = new Integer(i);
                            Object returnObject = bindToXML(value);

//                            if (returnObject == null)
////                                return null; //do not generate an XmlObject
//                                break;

//							array[1] = bindToXML(value);
                            array[1] = returnObject;
							if (setterMethod.getName().equals("setContentArray"))
								setterMethod.invoke(xmlObj, new Integer(i), returnObject);
							else
								setterMethod.invoke(xmlObj, array);
						}
					}

					if (!isCollection) {
						String attributeName = getAttributeNameFromGetter(name);

						if ("nullFlavor".equals(attributeName)) {
							attributeName = "nullFlavour";
						}

						// skip function according to specs
						if ("isMerged".equals(attributeName)) {
							continue;
						}

						setterMethod = findSetter(attributeName, xmlClass, isCollection);
						if (setterMethod == null) {
							continue;
						}

						// special handling deals with 'real' typed
						// attributes in specs but typed 'float' in xsd
						String setter = setterMethod.getName();
						if ("setAccuracy".equals(setter)|| "setDenominator".equals(setter)|| "setNumerator".equals(setter)) {
							Double d = (Double) attributeValue;
							attributeValue = d.floatValue();
						}
						setterMethod.invoke(xmlObj, attributeValue);
						if (xmlObj instanceof DVINTERVAL){
							DVINTERVAL dvinterval = (DVINTERVAL)xmlObj;
							dvinterval.setLowerUnbounded(!dvinterval.isSetLower());
							dvinterval.setUpperUnbounded(!dvinterval.isSetUpper());
						}
					}
				}
			}

            //remove the form: '<items xsi:nil="true"/>'

            if (xmlObj instanceof ITEMTREE || xmlObj instanceof CLUSTER){
//                ITEMTREE itemtree = (ITEMTREE)xmlObj;
                //check for non empty item list
                //use a cursor to remove safely a nil item (e.g. IllegalStateException if using a direct removeItems
                XmlCursor cursor = ((XmlObject)xmlObj).newCursor();
                cursor.toLastAttribute();
                cursor.toNextToken();

                boolean hasNext = false;

                do {
                    if (cursor.getObject() == null)
                        break;
                    log.debug("token nil:" + cursor.getObject().isNil());
                    if (cursor.getObject().isNil()) {

                        if (xmlObj instanceof CLUSTER)
                            log.debug("item in CLUSTER should not be nilled!"+xmlObj);

                        cursor.removeXml();

                        int type = cursor.currentTokenType().intValue();
                        if (type == XmlCursor.TokenType.INT_START) //an item follows...
                            hasNext = true;
                        else if (type == XmlCursor.TokenType.INT_ENDDOC) //reached the end of this xml-fragment
                            hasNext = false;
                    }
                    else {
						hasNext = cursor.toNextSibling();
					}
                } while (hasNext);

                cursor.dispose();

			}
			else if (xmlObj instanceof CAREENTRY){
				if (((CAREENTRY)xmlObj).getArchetypeDetails().isSetTemplateId()) {
					((CAREENTRY) xmlObj).getArchetypeDetails().unsetTemplateId();
				}
			}

			return FilterOutNil.filter(xmlObj);

		} catch (Exception e) {
			throw new XMLBindingException("exception caught when bind obj to "
					+ className + ", " + e.getMessage(), e);
		}
	}


	public Object bindToRM(Object object) throws Exception {
		log.debug("binding fragment:"+object.toString());

//		if (object instanceof LOCATABLE && ((LOCATABLE)object).isNil()) {
//			log.debug("skipping nil fragment:"+object);
//			return null;
//		}
//
		Method[] methods = object.getClass().getMethods();
		Object value;
		Map<String, Object> valueMap = new HashMap<String, Object>();

		String className = object.getClass().getSimpleName();
		if (className.endsWith("Impl")) {
			className = className.substring(0, className.length() - 4);
		}

		Map<String, Class<?>> attributes = builder.retrieveAttribute(className);
		Set<String> attributeNames = attributes.keySet();

		for (Method method : methods) {
			String name = method.getName();

			if (isGetter(name, attributeNames)) {
				if (method.getParameterTypes().length > 0) {
					continue;
				}

				String attribute = getAttributeNameFromGetter(name);

				value = method.invoke(object, null);
				if (value == null) {
					continue;
				}

				if (value.getClass().isArray()) {
					Object[] array = (Object[]) value;
					if (array.length == 0) {
						// special fix for item_structure.items
						if ("items".equals(attribute)) {
							valueMap.put(attribute, new ArrayList());
						}
						continue;
					} else {
						Object[] done = new Object[array.length];
						for (int i = 0; i < array.length; i++) {
							done[i] = bindToRM(array[i]);
						}
						value = done;
					}

				} else if (isXMLBindingClass(value)) {
					//ignore nil locatable
					if (value instanceof LOCATABLE)
						if  (!((LOCATABLE)value).isNil())
							value = bindToRM(value);
						else {
							log.debug("ignoring nil value:" + value);
							value = null;
						}
					else
						value = bindToRM(value);
				}
				if (value != null)
					valueMap.put(attribute, value);
			}
		}

		Object rmObj = builder.construct(className, valueMap);

        if (rmObj instanceof Element){ //wrap it nicely...
            Element element = (Element)rmObj;

            ElementWrapper wrapper = new ElementWrapper(element, null); //no constraints available

            Object valueObject = valueMap.get("value");

            if (valueObject != null && valueObject instanceof I_VBeanWrapper)
                wrapper.setWrappedValue((I_VBeanWrapper)valueMap.get("value"));

            DataValueConstraints constraints = VBeanUtil.getConstraintInstance(builder, wrapper.getAdaptedElement().getValue());
            if (constraints != null) {
                if (valueMap.get("description") != null) {
                    constraints.setDescription(((DvText) valueMap.get("description")).getValue());
                }

//                wrapper.setConstraints(archetype, constraints);
            }

            //since it comes from a client supplied data set
            wrapper.setDirtyBit(true);

            rmObj = wrapper;
        }
        else {
            //wrap object only if its a DataValue element of primitive...
            if (DataValueAdapter.isValueObject(rmObj)) {
                if (VBeanUtil.isInstrumentalized(rmObj)) {
                    rmObj = VBeanUtil.wrapObject(rmObj);
                }
            }
        }

		return rmObj;
	}

	protected Method findSetter(String attributeName, Class<?> xmlClass, boolean isCollection) throws NoSuchMethodException {

		if (attributeName == null){ //simple set (Enum type)
			return xmlClass.getMethod("setStringValue", String.class);
		}

		Method[] methods = xmlClass.getMethods();

		String name = "set" + attributeName.substring(0, 1).toUpperCase() +
				attributeName.substring(1);

		if (isCollection) {
			name += "Array";
		}

		for (Method method : methods) {
			if (method.getName().equals(name)) {
				Type[] paras = method.getParameterTypes();
				if (isCollection) {
					if (paras.length == 2) {
						return method;
					}
				} else if (paras.length == 1) {
					return method;
				}
			}
		}
		return null;
	}

	protected Method findAddNew(String attributeName, Class<?> xmlClass) {
		Method[] methods = xmlClass.getMethods();
		String name = "addNew" + attributeName.substring(0, 1).toUpperCase() +
				attributeName.substring(1);

		for (Method method : methods) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}

	protected boolean isGetter(String method, Set<String> attributes) {
		if (!method.startsWith("get")) {
			return false;
		}
		String name = getAttributeNameFromGetter(method);
		return attributes.contains(name);
	}

	protected String getAttributeNameFromGetter(String name) {
		name = name.substring(3, name.length());
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
		if (name.endsWith("Array")) {
			name = name.substring(0, name.length() - 5);
		}
		return name;
	}

	protected boolean isXMLBindingClass(Object obj) {
		return obj.getClass().getName().contains(XML_BINDING_PACKAGE);
	}

	protected void init(Map<SystemValue, Object> values) {
		xmlOptions = new XmlOptions();

//=============================================================
//		HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
//		uriToPrefixMap.put(SCHEMA_XSI, "xsi");
//		uriToPrefixMap.put(SCHEMA_OPENEHR_ORG_V1, "v1");
//		xmlOptions.setSaveSuggestedPrefixes(uriToPrefixMap);
//        xmlOptions.setSaveAggressiveNamespaces();
//=============================================================

//        xmlOptions.setUseDefaultNamespace();
//        HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
//        uriToPrefixMap.put(SCHEMA_XSI, "xsi");
//        uriToPrefixMap.put("", SCHEMA_OPENEHR_ORG_V1);
//        xmlOptions.setSaveImplicitNamespaces(uriToPrefixMap);

//		xmlOptions.setSavePrettyPrint();
//		xmlOptions.setCharacterEncoding("UTF-8");
        xmlOptions.setUseDefaultNamespace();
        HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
//		    uriToPrefixMap.put(SCHEMA_XSI, "xsi");
        uriToPrefixMap.put("", SCHEMA_OPENEHR_ORG_V1);
        xmlOptions.setLoadSubstituteNamespaces(uriToPrefixMap);

		builder = new RMObjectBuilder(values);
	}

	/* namespace for generated binding class */
	private static String XML_BINDING_PACKAGE = "org.openehr.schemas.v1.";

	private static final String NEW_INSTANCE = "newInstance";

	public static final String SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String SCHEMA_OPENEHR_ORG_V1 = "http://schemas.openehr.org/v1";

	private RMObjectBuilder builder;
	private XmlOptions xmlOptions;
}
