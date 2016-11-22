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

import com.ethercis.ehr.encode.DataValueAdapter;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.DvIntervalVBean;
import com.ethercis.ehr.encode.wrappers.element.AnyElementWrapper;
import com.ethercis.ehr.encode.wrappers.element.ChoiceElementWrapper;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import com.ethercis.ehr.util.LocatableHelper;
import com.ethercis.validation.ConstraintMapper;
import com.ethercis.validation.OptConstraintMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.content.entry.ISMTransition;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.identification.ArchetypeID;
import org.openehr.rm.support.identification.TemplateID;
import org.openehr.rm.support.identification.TerminologyID;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.schemas.v1.*;
import org.openehr.schemas.v1.impl.CCODEREFERENCEImpl;

import java.util.*;

public class OptBinding extends RmBinding {

	Logger log = LogManager.getLogger(OptBinding.class);

	private OptConstraintMapper constrainMapper = new OptConstraintMapper();

	private Map<String,Map<String, String>> termTable=new  HashMap<String,Map<String, String>>();

	private Map<String,List<DVORDINAL>> ordinalTable=new  HashMap<String,List<DVORDINAL>>();

	public OptBinding() throws Exception {
        super(null);
	}

    protected OptBinding(Map<SystemValue, Object> map) throws Exception {
        super(map);
    }

    /**
     * stuff in some required attributes if not yet present
     * @param valueMap
     * @param archetypeRoot
     * @throws Exception
     */
	private void addEntryValues(Map<String, Object> valueMap, CARCHETYPEROOT archetypeRoot) throws Exception {
		String tid = (String) valueMap.get(TEMPLATE_ID);
		TemplateID templateId = tid == null ? null : new TemplateID(tid);

		if (!valueMap.containsKey(SystemValue.SUBJECT.id())) {
			valueMap.put(SystemValue.SUBJECT.id(), subject());
		}
		// valueMap.put("provider", provider());
		if (!valueMap.containsKey(SystemValue.ENCODING.id())) {
			CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
			valueMap.put(SystemValue.ENCODING.id(), charset);
		}
		if (!valueMap.containsKey(SystemValue.LANGUAGE.id())) {
			CodePhrase lang = new CodePhrase("ISO_639-1", "en");
			valueMap.put(SystemValue.LANGUAGE.id(), lang);
		}
		if (!valueMap.containsKey("archetype_details")) {
			ArchetypeID arId = new ArchetypeID(archetypeRoot.getArchetypeId().getValue());
			Archetyped archetypeDetails = new Archetyped(arId, templateId, "1.0.1");
			valueMap.put("archetype_details", archetypeDetails);
		}
	}

	private void addItemStructureValues(Map<String, Object> valueMap, CARCHETYPEROOT archetypeRoot) throws Exception {

		if (!valueMap.containsKey("archetype_details")) {
			ArchetypeID arId = new ArchetypeID(archetypeRoot.getArchetypeId().getValue());
			Archetyped archetypeDetails = new Archetyped(arId, null, "1.0.1");
			valueMap.put("archetype_details", archetypeDetails);
		}
	}

	private Object createPrimitiveTypeObject(CPRIMITIVEOBJECT cpo)
			throws Exception {

		CPRIMITIVE cp = cpo.getItem();

		if (cp instanceof CBOOLEAN) {
            if (((CBOOLEAN) cp).isSetAssumedValue())
                return ((CBOOLEAN) cp).getAssumedValue();

			if (((CBOOLEAN) cp).getTrueValid()) {
				return "true";
			} else {
				return "false";
			}

		} else if (cp instanceof CSTRING) {
            if (((CSTRING) cp).isSetAssumedValue())
                return ((CSTRING) cp).getAssumedValue();

            if (((CSTRING) cp).isSetPattern())
                return ((CSTRING) cp).getPattern();

			return createString((CSTRING) cp);

		} else if (cp instanceof CDATE) {
            if (((CDATE) cp).isSetAssumedValue())
                return ((CDATE) cp).getAssumedValue();
			return DEFAULT_DATE;

		} else if (cp instanceof CTIME) {
            if (((CTIME) cp).isSetAssumedValue())
                return ((CTIME) cp).getAssumedValue();
			return DEFAULT_TIME;

		} else if (cp instanceof CDATETIME) {
            if (((CDATETIME) cp).isSetAssumedValue())
                return ((CDATETIME) cp).getAssumedValue();
			return DEFAULT_DATE_TIME;

		} else if (cp instanceof CINTEGER) {
            if (((CINTEGER) cp).isSetAssumedValue())
                return ((CINTEGER) cp).getAssumedValue();
			return new Integer(0);

		} else if (cp instanceof CREAL) {
            if (((CREAL) cp).isSetAssumedValue())
                return new Double(((CREAL) cp).getAssumedValue());

			return new Double(0);

		} else if (cp instanceof CDURATION) {
			CDURATION cd = (CDURATION) cp;
            DvDuration duration = null;

            if(cd.isSetAssumedValue()) {
                duration = new DvDuration(cd.getAssumedValue());
			 } else if(cd.getRange() != null) {
                 if(cd.getRange().getLower() != null) {
                     duration = new DvDuration(cd.getRange().getLower());
                 } else if(cd.getRange().getUpper() != null) {
                     duration = new DvDuration(cd.getRange().getUpper());
                 }
            } if(duration == null) {
                    return DEFAULT_DURATION;
            } else {
                return duration.toString();
            }

		}
		return null;

	}

	private String createString(CSTRING cs) {
		String[] csarr = cs.getListArray(); 
		if ( csarr != null && csarr.length > 0) {

			return csarr[0];

		} else {
			return "string value";
		}
	}

	private Object createDomainTypeObject(CDOMAINTYPE cpo,Map<String, String> termDef, String path) throws Exception {

		if (cpo instanceof CDVQUANTITY) {
            if (((CDVQUANTITY) cpo).isSetAssumedValue())
                return ((CDVQUANTITY) cpo).getAssumedValue();

			return createDvQuantity((CDVQUANTITY) cpo);

		} else if (cpo instanceof CCODEPHRASE) {
            if (((CCODEPHRASE) cpo).isSetAssumedValue())
                return ((CCODEPHRASE) cpo).getAssumedValue();

			return createCodePhrase((CCODEPHRASE) cpo);

		} else if (cpo instanceof CDVORDINAL) {
            if (((CDVORDINAL) cpo).isSetAssumedValue())
                return ((CDVORDINAL) cpo).getAssumedValue();

			return createDvOrdinal((CDVORDINAL) cpo, termDef, path);

		} else {
			throw new Exception("unsupported c_domain_type: " + cpo.getClass());
		}
	}

	private DvOrdinal createDvOrdinal(CDVORDINAL cdo,Map<String, String> termDef, String path) throws Exception {

		List<DVORDINAL> list = Arrays.asList(cdo.getListArray());
		if (list == null || list.size() == 0) {
			throw new Exception("empty list of ordinal");
		}
		ordinalTable.put(path, list);
		DVORDINAL ordinal = list.iterator().next();
		String text = DEFAULT_CODED_TEXT;
		DVCODEDTEXT symbol = ordinal.getSymbol();
		String code = symbol.getDefiningCode().getCodeString();

		if ("local".equalsIgnoreCase(code) || code.startsWith("at")) {
			text = termDef.get(code);
		} else {
			//System.out.println("termMap=" +termMap.getTermMap());
			try{
				text = termMap.getText("{"+terminologyService+"}::{"+symbol.getDefiningCode().getCodeString() +"}", path);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return new DvOrdinal(-1, new DvCodedText(text, new CodePhrase(TerminologyService.OPENEHR, code)));
	}

	CodePhrase createCodePhrase(CCODEPHRASE ccp) throws Exception {

		TERMINOLOGYID tid = ccp.getTerminologyId();
		String[] codeList = ccp.getCodeListArray();

        String referenceSetUri = null;

        if (ccp instanceof CCODEREFERENCE)
            referenceSetUri = ((CCODEREFERENCEImpl)ccp).getReferenceSetUri();

		String code;
		if (codeList == null || codeList.length == 0) {
			code = "0123456789";
		} else {
			code = codeList[0];
		}

        String terminology;
        if (tid != null)
            terminology = tid.getValue();
        else if (referenceSetUri != null)
            terminology = referenceSetUri.substring(referenceSetUri.indexOf("terminology:"), referenceSetUri.indexOf("?"));
        else
            throw new IllegalArgumentException("Could not resolve terminology in:"+ccp.toString());

		return new CodePhrase(new TerminologyID(terminology), code);
	}

	DvQuantity createDvQuantity(CDVQUANTITY cdq) throws Exception {
		CQUANTITYITEM[] qtyitems = cdq.getListArray();
		if (qtyitems == null || qtyitems.length == 0) {
			return new DvQuantity(0.0);
		}
		// TODO take the first item
		CQUANTITYITEM item = qtyitems[0];

        if (qtyitems.length > 1){
            log.debug("CQUANTITYITEM multiple items:"+qtyitems.length);
        }


		// TODO take the lower limit as magnitude or zero
		double magnitude;
		if (item.getMagnitude() != null) {
			magnitude = item.getMagnitude().getLower();
		} else {
			magnitude = 0;
		}
		return new DvQuantity(item.getUnits(), magnitude, measurementService);
	}

	public Locatable bindToRm(OPERATIONALTEMPLATE opt,
			Map<String, String> rawParams) throws Exception {

		Locatable loc = (Locatable) generate(opt);
		Map<String, String> params = new HashMap<String, String>();
		Properties props = new Properties();
		for (String key : rawParams.keySet()) {
			String value = rawParams.get(key);
			if (value != null && !value.isEmpty()) {
				params.put(key, value);
				props.put(key, value);
			}
		}

		for (Object key : params.keySet()) {
			String path = key.toString();
			if (path.startsWith("/")) {
				Object item = loc.itemAtPath(path);
				// log.debug("found item=" + item);
				//System.out.println("found item=" + item +",path="+path);
				if (item != null) {
					if (item instanceof Element) {
						Element e = (Element) item;						
						if (e.getValue() != null) {
							if (e.getValue() instanceof DvCodedText) {
								((DvCodedText) e.getValue()).setValue(getTermDef(path, params.get(path)));
								((DvCodedText) e.getValue()).getDefiningCode().setCodeString(params.get(path));

							} else if (e.getValue() instanceof DvText) {
								((DvText) e.getValue()).setValue(params.get(path));

							} else if (e.getValue() instanceof DvBoolean) {
								((DvBoolean) e.getValue()).parse((params.get(path)));

							} else if (e.getValue() instanceof DvDateTime) {
								((DvDateTime) e.getValue()).parse(params.get(path));

							} else if (e.getValue() instanceof DvDate) {
								((DvDate) e.getValue()).parse((String) props.get(path));

							} else if (e.getValue() instanceof DvCount) {
								((DvCount) e.getValue()).setMagnitude(Integer.parseInt((String) props.get(path)));

							} else if (e.getValue() instanceof DvOrdinal) {
								DVORDINAL ord = getOrdinalTermDef(path, Integer.parseInt((String) props.get(path)));
								String def=getTermDef(path,ord.getSymbol().getDefiningCode().getCodeString());
								
								((DvOrdinal) e.getValue()).setValue(Integer.parseInt((String) props.get(path)));
								((DvOrdinal) e.getValue()).getSymbol().getDefiningCode()
										.setCodeString(ord.getSymbol().getDefiningCode().getCodeString());
								((DvOrdinal) e.getValue()).getSymbol().setValue(def);

							} else if (e.getValue() instanceof DvProportion) {
								((DvProportion) e.getValue()).setNumerator(Double.parseDouble((String) props.get(path)));

							} else if (e.getValue() instanceof DvQuantity) {
								((DvQuantity) e.getValue()).setMagnitude(Double.parseDouble((String) props.get(path)));

							} else {
								// TODO implement more type
								log.warn("not supported yet "
										+ e.getValue().getClass());
							}
						} else {
							log.warn("e.getValue() is null" + path);
						}
					}

				} else {
					log.warn("cannot get item at path " + path);
				}
			}
		}
		
		return loc;
	}

	/**
	 * Generate empty Rm from template
	 * 
	 * @param opt
	 * @return
	 * @throws Exception
	 */
	public Object generate(OPERATIONALTEMPLATE opt) throws Exception {
		CARCHETYPEROOT def = opt.getDefinition();

		Object c = handleArchetypeRoot(opt, def, null, "");
		constrainMapper.setTerminology(termTable);
		return c;
	}

	private DVORDINAL getOrdinalTermDef(String path,int index){
		for(String keyTerm:ordinalTable.keySet()){		
			if(keyTerm!=null && !keyTerm.isEmpty() && path.startsWith(keyTerm)){
				List<DVORDINAL> ordinalList = ordinalTable.get(keyTerm);
				for(DVORDINAL ord:ordinalList){
					if(ord!=null && index==ord.getValue()){
						//return getTermDef(path,ord.getSymbol().getDefiningCode().getCodeString());
						return ord;
					}
				}
			}
		}
		return null;
	}
	/**
	 * 
	 * @param path
	 * @param code
	 * @return
	 */
	private String getTermDef(String path,String code){
		for(String keyTerm:termTable.keySet()){
			//log.debug("--------" + path + "," + keyTerm +">" + termTable.get(keyTerm));
			
			if(keyTerm!=null && !keyTerm.isEmpty() && path.startsWith(keyTerm)){
				Map<String, String> termDef =termTable.get(keyTerm);
				return termDef.get(code);
			}
		}
		return null;
	}
	/**
	 * 
	 * @param opt
	 * @param def
	 * @param name
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private Object handleArchetypeRoot(OPERATIONALTEMPLATE opt,
                                       CARCHETYPEROOT def, String name, String path) throws Exception {

		Map<String, String> termDef = new HashMap<String, String>();
		// Keep term definition to map
		for (ARCHETYPETERM term : def.getTermDefinitionsArray()) {
			String code = term.getCode();
			for (StringDictionaryItem item : term.getItemsArray()) {
				if ("text".equals(item.getId())) {
					// TODO currently keep only text , let's check that should
					// we keep description?
					termDef.put(code, item.getStringValue());
				}
			}
		}
		log.debug("CARCHETYPEROOT path=" +path);
		termTable.put(path, termDef);
		// Load complex component
		return handleComplexObject(opt, def, termDef, name, path);
	}

	/**
	 * Load complex component
	 * 
	 * @param opt
	 * @param ccobj
	 * @param termDef
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private Object handleComplexObject(OPERATIONALTEMPLATE opt,
                                       CCOMPLEXOBJECT ccobj, Map<String, String> termDef, String name,
                                       String path) throws Exception {

		Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put(TEMPLATE_ID, opt.getTemplateId().getValue());

		String nodeId = ccobj.getNodeId();
		String rmTypeName = ccobj.getRmTypeName();
		log.debug("rmTypeName=" + rmTypeName + ":nodeId=" + nodeId + ":ccobj="
				+ ccobj);

		constrainMapper.addToValidPath(path);
		constrainMapper.addToExistence(path, ccobj.getOccurrences());

		if (nodeId != null && nodeId.trim().length() > 0) {
			DvText txtName = null;
			// root node with archetype_id as node_id
			// TODO check if name is already defined?
			if (ccobj instanceof CARCHETYPEROOT) {
//				txtName = new DvText(((CARCHETYPEROOT) ccobj).getArchetypeId()
//						.getValue());
				log.debug("set archetype_node_id="
						+ (((CARCHETYPEROOT) ccobj)).getArchetypeId()
								.getValue());
				valueMap.put("archetype_node_id", (((CARCHETYPEROOT) ccobj))
						.getArchetypeId().getValue());
                String termName = termDef.get(nodeId);
                if (termName != null) {
                    txtName = new DvText(termName);
                    valueMap.put("name", txtName);
                } else {
                    log.warn("name not found for nodeId " + nodeId);
                }
				valueMap.put("name", txtName);
			} else {
				log.debug("set archetype_node_id=" + nodeId);
				valueMap.put("archetype_node_id", nodeId);
				String termName = termDef.get(nodeId);
				if (termName != null) {
					txtName = new DvText(termName);
					valueMap.put("name", txtName);
				} else {
					log.warn("name not found for nodeId " + nodeId);
				}
			}

		}
		// Loop Create attributes
		CATTRIBUTE[] cattributes = ccobj.getAttributesArray();
		if (cattributes != null && cattributes.length > 0) {
			for (CATTRIBUTE attr : cattributes) {
				String pathloop = path + "/" + attr.getRmAttributeName();
				COBJECT[] children = attr.getChildrenArray();
				String attrName = attr.getRmAttributeName();
				if (attr instanceof CSINGLEATTRIBUTE) {
					if (children != null && children.length > 0) {
						try {
							COBJECT cobj = children[0];
                            if (children.length > 1)
                            {
                                log.debug("Multiple children in CATTRIBUTE:"+children.length);
                            }
							Object attrValue = handleCObject(opt, cobj, termDef,
                                    valueMap, attrName, pathloop);
							log.debug("attrName=" + attrName + ": attrValue="+ attrValue);
							if (attrValue != null) {
								valueMap.put(attr.getRmAttributeName(),attrValue);
							}
						} catch (Exception e) {
							log.error("Cannot create attribute name "+ attrName + " on path " + pathloop, e);

						}
					}
				} else if (attr instanceof CMULTIPLEATTRIBUTE) {
					CMULTIPLEATTRIBUTE cma = (CMULTIPLEATTRIBUTE) attr;
					List<Object> container = new ArrayList<Object>();

					for (COBJECT cobj : children) {
						try {

							Object attrValue = handleCObject(opt, cobj, termDef,
                                    valueMap, attrName, pathloop);
							log.debug("attrName=" + attrName + ": attrValue="+ attrValue);
							if (attrValue != null) {
								container.add(attrValue);
							}

						} catch (Exception e) {
							log.error("Cannot create attribute name "+ attrName + " on path " + pathloop, e);

						}
					}
					log.debug("valueMap.put " + attr.getRmAttributeName()+ " :" + container);
					valueMap.put(attr.getRmAttributeName(), container);
					constrainMapper.addToCardinalityList(path+"/"+attr.getRmAttributeName(), cma);
				}
			}
		}
		if ("DV_TEXT".equals(rmTypeName)) {

			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_TEXT);
			}
        } else if ("DV_CODED_TEXT".equals(rmTypeName)) {
            Object definingCode = valueMap.get(DEFINING_CODE);
            CodePhrase codePhrase = null;

            if (!(definingCode instanceof CodePhrase)) {
                valueMap.put(DEFINING_CODE, new CodePhrase("local", "at9999"));
            } else {
                codePhrase = (CodePhrase) valueMap.get(DEFINING_CODE);
            }
            if (valueMap.get(VALUE) == null) {
                String text = null;
                if (codePhrase != null) {
                    String code = codePhrase.getCodeString();
                    if (isLocallyDefined(codePhrase)) {
                        // archetype terms
                        text = termDef.get(code);
                    } else if (isOpenEHRTerm(codePhrase)) {
                        // openEHR terms
                        // TODO hard-coded language "en"
                        text = openEHRTerminology.rubricForCode(code, "en");
                    } else {
                        // externally defined term
                        text = termMap.getText(codePhrase, path);
                    }
                }
                if (text == null) {
                    text = DEFAULT_CODED_TEXT;
                }
                valueMap.put(VALUE, text);
            }
		} else if ("DV_URI".equals(rmTypeName) || "DV_EHR_URI".equals(rmTypeName)) {
			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_URI);
			}

		} else if ("DV_DATE_TIME".equals(rmTypeName)) {

			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_DATE_TIME);
			}

		} else if ("DV_DATE".equals(rmTypeName)) {

			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_DATE);
			}

		} else if ("DV_TIME".equals(rmTypeName)) {

			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_TIME);
			}
        } else if("DV_PARSABLE".equals(rmTypeName)) {

			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, DEFAULT_TEXT);
			}
			if (!valueMap.containsKey(FORMALISM)) {
				valueMap.put(FORMALISM, "text");
			}
		} else if (rmTypeName.startsWith("DV_INTERVAL")){
			if (!valueMap.containsKey(VALUE)) {
				//get the DvOrdered type defining this interval
				String dvOrderedTypeName = rmTypeName.substring(rmTypeName.indexOf("<")+1, rmTypeName.indexOf(">"));
				Class orderedClass = builder.retrieveRMType(dvOrderedTypeName);
				log.debug("Found dvOrdered in interval:" + dvOrderedTypeName);
				DvInterval interval = DvIntervalVBean.createQualifiedInterval(orderedClass);
//				valueMap.put(VALUE, interval);
				return interval;
			}
		} else if("DV_MULTIMEDIA".equals(rmTypeName)) {
            CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
            CodePhrase language = new CodePhrase("ISO_639-1", "en");
            String alternateText = "alternative text";
            CodePhrase mediaType = new CodePhrase("IANA_media-types", "text/plain");
            CodePhrase compressionAlgorithm = new CodePhrase("openehr_compression_algorithms", "other");
            //byte[] integrityCheck = new byte[0];
            CodePhrase integrityCheckAlgorithm = new CodePhrase("openehr_integrity_check_algorithms", "SHA-1");
            DvMultimedia thumbnail = null;
            DvURI uri = new DvURI("www.iana.org");
            //byte[] data = new byte[0];
            TerminologyService terminologyService = TerminologyServiceWrapper.getInstance();
            DvMultimedia dm = new DvMultimedia(charset, language, alternateText,
                    mediaType, compressionAlgorithm, null,
                    integrityCheckAlgorithm, thumbnail, uri, null, terminologyService);
            return dm;
		} else if ("DV_BOOLEAN".equals(rmTypeName)) {
			if (!valueMap.containsKey(VALUE)) {
				valueMap.put(VALUE, "false");
			}
		} else if ("DV_COUNT".equals(rmTypeName)) {

			if (valueMap.get(MAGNITUDE) == null) {
				valueMap.put(MAGNITUDE, DEFAULT_COUNT);
			}
        } else if("DV_DURATION".equals(rmTypeName)) {
            if (valueMap.get(VALUE) == null) {
                valueMap.put(VALUE, DEFAULT_DURATION);
            }
        } else if ("DV_IDENTIFIER".equals(rmTypeName)) {
            if (valueMap.get(ID) == null) {
                valueMap.put(ID, DEFAULT_ID);
            }
            if (valueMap.get(ASSIGNER) == null) {
                valueMap.put(ASSIGNER, DEFAULT_ASSIGNER);
            }
            if (valueMap.get(ISSUER) == null) {
                valueMap.put(ISSUER, DEFAULT_ISSUER);
            }
            if (valueMap.get(TYPE) == null) {
                valueMap.put(TYPE, DEFAULT_TYPE);
            }
        } else if("DV_PROPORTION".equals(rmTypeName)) {

            // default dv_proportion
            // DV_PROPORTION) <
            //     numerator = <0.5>
            // 	   denominator = <1.0>
            //     type = <0>
            //     precision = <2>
            // >

            //if( ! valueMap.containsKey("type")) { //generally: ignore garbage coming from flattener...
            valueMap.put("numerator", 1);
            valueMap.put("denominator", 1);
            valueMap.put("precision", 0);
            valueMap.put("type", ProportionKind.RATIO);
            //}
		} else if ("HISTORY".equals(rmTypeName)) {

			if (!valueMap.containsKey(ORIGIN)) {
				valueMap.put(ORIGIN, new DvDateTime(DEFAULT_DATE_TIME));
			}
			//test only for now
			constrainMapper.bind(path, ccobj);

		} else if ("EVENT".equals(rmTypeName) || "POINT_EVENT".equals(rmTypeName)) {

			if (!valueMap.containsKey(TIME)) {
				valueMap.put(TIME, new DvDateTime(DEFAULT_DATE_TIME));
			}

		} else if ("OBSERVATION".equals(rmTypeName)) {

			addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);

		} else if ("EVALUATION".equals(rmTypeName)) {

			addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);

		} else if ("ADMIN_ENTRY".equals(rmTypeName)) {

			addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);

        } else if("ACTION".equals(rmTypeName)) {

            addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);
            if( ! valueMap.containsKey(TIME)) {
                valueMap.put(TIME, new DvDateTime(DEFAULT_DATE_TIME));
            }
            if(valueMap.get(DESCRIPTION) == null) {
                valueMap.put(DESCRIPTION, new ItemTree(DEFAULT_NODE_ID, new DvText(DEFAULT_DESCRIPTION_NAME), null));
            }
			if(valueMap.get(ISM_TRANSITION) == null) {
				DvCodedText currentState = new DvCodedText("Initial", "openehr", "524");
				DvCodedText transition = new DvCodedText("Initiate", "openehr", "535");
				DvCodedText step = new DvCodedText("Plan", "local", "at0001");
				valueMap.put(ISM_TRANSITION, new ISMTransition(currentState, transition, step, TerminologyServiceWrapper.getInstance()));
			}


		} else if ("ISM_TRANSITION".equals(rmTypeName)){
			//do not assume a default for careflow_step
			if (!valueMap.containsKey(CAREFLOW_STEP))
				valueMap.put(CAREFLOW_STEP, new DvCodedText(DEFAULT_CAREFLOW_STEP, "local", "0000"));
		}
		else if("INSTRUCTION".equals(rmTypeName)) {

            if( ! valueMap.containsKey(NARRATIVE)) {
                valueMap.put(NARRATIVE, new DvText(DEFAULT_NARRATIVE));
            }
            addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);

        } else if("ACTIVITY".equals(rmTypeName)) {

            if( ! valueMap.containsKey(TIMING)) {
                valueMap.put(TIMING, new DvParsable(DEFAULT_TIMING_SCHEME, DEFAULT_TIMING_FORMALISM));
            }

            if ( ! valueMap.containsKey(ACTION_ARCHETYPE_ID)){
                valueMap.put(ACTION_ARCHETYPE_ID, DEFAULT_ACTION_ARCHETYPE_ID);
            }

		} else if ("COMPOSITION".equals(rmTypeName)) {
            setCompositionValues(valueMap);

			addEntryValues(valueMap, (CARCHETYPEROOT) ccobj);

		} else if ("ELEMENT".equals(rmTypeName)) {
			// valueMap.put(VALUE, null);
			// valueMap.put(NULL_FLAVOUR, NULL_FLAVOUR_VALUE);

            if (ccobj.getAttributesArray().length > 0) {
                Object obj = ccobj.getAttributesArray()[0];
                if (ccobj.getAttributesArray().length > 1){
                    log.debug("Multiple CCOBJ ELEMENT:"+ccobj.getAttributesArray().length);
                }
                if (obj instanceof CSINGLEATTRIBUTE) {
                    CSINGLEATTRIBUTE attr = (CSINGLEATTRIBUTE) obj;

                    if (VALUE.equals(attr.getRmAttributeName())) {
                        if (attr.getChildrenArray().length > 0) {
                            List<String> children = new ArrayList<>();
                            for (COBJECT cobj: attr.getChildrenArray()){
                                children.add(cobj.getRmTypeName());
                            }
                            log.debug("ELEMENT children length:"+attr.getChildrenArray().length);
                            if (attr.getChildrenArray().length > 1){
                                log.debug("CHOICE:"+attr.getChildrenArray().length);
                                valueMap.put(CHOICE, children); //will be used to instantiate a ChoiceElementWrapper
                            }
                            COBJECT cobj = attr.getChildrenArray()[0];
                            Object attrValue = handleCObject(opt, cobj, termDef, valueMap, name, path);
                            valueMap.put(attr.getRmAttributeName(), attrValue); //in case of choice it is defaulted to the first child
                        }
                        else
                            log.debug("ELEMENT without child, assuming ANY type");
                    } else {
						//TODO: add additional attributes (contains DvCodedText supplementary data ex. terminology id + code)
                        log.debug("additional attribute found for element attr.getRmAttributeName()=" + attr.getRmAttributeName());
                    }
					constrainMapper.addToWatchList(path, attr);
                }
                else {
                    log.debug("Other type for obj:"+obj);
                }
            }
            else {
                log.debug("Empty attribute list for ELEMENT, assuming ANY type:"+ccobj.toString());
				valueMap.put(ANY, true);
				//create a dummy value for this pseudo-element
				DvText dvText = new DvText("*any*");
				valueMap.put(VALUE, dvText);
            }

		} else if ("EVENT_CONTEXT".equals(rmTypeName)) {

			if (!valueMap.containsKey(START_TIME)) {
				valueMap.put(START_TIME, new DvDateTime(DEFAULT_DATE_TIME));
			}

			if (!valueMap.containsKey(SETTING)) {
				valueMap.put(SETTING, new DvCodedText("other care", new CodePhrase("openehr", "238")));
			}

		} else if ("SECTION".equals(rmTypeName)) {

			List list = (List) valueMap.get("items");
			if (list != null && list.isEmpty()) {
				valueMap.remove("items");
			}
			addItemStructureValues(valueMap, (CARCHETYPEROOT) ccobj);

		} else if ("CLUSTER".equals(rmTypeName)) {
			if (ccobj instanceof CARCHETYPEROOT)
				addItemStructureValues(valueMap, (CARCHETYPEROOT) ccobj);
		}
		else if ("INTERVAL_EVENT".equals(rmTypeName)){
			if (!valueMap.containsKey(ARCHETYPE_NODE_ID))
				valueMap.put(ARCHETYPE_NODE_ID, "at0000");
			if (!valueMap.containsKey(NAME))
				valueMap.put(NAME, "interval_event_name");
			if (!valueMap.containsKey(TEMPLATE_ID))
				valueMap.put(TEMPLATE_ID, "template_id");
			if (!valueMap.containsKey(TIME))
				valueMap.put(TIME, new DvDateTime(DEFAULT_DATE_TIME));
			if (!valueMap.containsKey(WIDTH))
				valueMap.put(WIDTH, new DvDuration(DEFAULT_DURATION));
		}
		else {
            if (!(rmTypeName.equals("CLUSTER") ||
                    rmTypeName.equals("ITEM_TREE") ||
                    rmTypeName.equals("DV_INTERVAL<DV_QUANTITY>") || //used in Action, defaulted
                    rmTypeName.equals("ISM_TRANSITION"))) //used in Action, defaulted
			    log.warn("Unhandled type: "+rmTypeName );
		}
		if ("EVENT".equals(rmTypeName)) {
			rmTypeName = "POINT_EVENT";
		}

		Object obj = null;
		try {
			obj = builder.construct(rmTypeName, valueMap);
		} catch (Exception e) {

			log.warn("Could not create instance of type:" + rmTypeName + " ,for nodeid="+ nodeId +", path:"+path+" ,valueMap:" + valueMap+" ,details:"+e.getMessage());
		}

        Object retobj = obj;

        //wrap an Element object and add the value wrapper and constraints container
        if (obj instanceof Element){
            Element element = (Element)obj;

			ElementWrapper wrapper;

			if (valueMap.containsKey(CHOICE))
				wrapper = new ChoiceElementWrapper(element, EZBindCComplexObject(path, obj, ccobj), (List)valueMap.get(CHOICE));
			else if (valueMap.containsKey(ANY))
				wrapper = new AnyElementWrapper(element, EZBindCComplexObject(path, obj, ccobj));
			else
            	wrapper = new ElementWrapper(element, EZBindCComplexObject(path, obj, ccobj));

            Object value = valueMap.get(VALUE);

            if (value != null) {
				if (value instanceof I_VBeanWrapper)
					wrapper.setWrappedValue((I_VBeanWrapper) valueMap.get(VALUE));
				else {
					I_VBeanWrapper wrapped = (I_VBeanWrapper) VBeanUtil.wrapObject(value);
					wrapper.setWrappedValue(wrapped);
				}

			}


            DataValueConstraints constraints = VBeanUtil.getConstraintInstance(builder, wrapper.getAdaptedElement().getValue());
            if (constraints != null) {
                if (valueMap.get("description") != null) {
                    constraints.setDescription(((DvText) valueMap.get("description")).getValue());
                }

//                wrapper.setConstraints(archetype, constraints);
            }

            retobj = wrapper;

        }
        else {
            //wrap object only if its a DataValue element of primitive...
            if (DataValueAdapter.isValueObject(obj)) {
                if (VBeanUtil.isInstrumentalized(obj)) {
                    retobj = VBeanUtil.wrapObject(obj);
                    //fill in the decorator with valuemap

                }
            }
        }

        return retobj;
	}

    private CComplexObject EZBindCComplexObject(String path, Object object, CCOMPLEXOBJECT ccomplexobject) throws Exception {
		//CHC: 160809: use a new validation strategy
		constrainMapper.bind(LocatableHelper.simplifyPath(path), ccomplexobject);

//        CComplexObject cComplexObject = new CComplexObject();
//        cComplexObject.setAnyAllowed(true); //could not get this from XML representation
//        org.openehr.rm.support.basic.Interval<Integer> integerInterval = new Interval<>();
//
//		ConstraintBinding constraintBinding = new ConstraintBinding(object);
//		constraintBinding.bind(path, ccomplexobject);
//
//        if (ccomplexobject.getOccurrences().isSetLower())
//            integerInterval.setLower(ccomplexobject.getOccurrences().getLower());
//        else
//            integerInterval.setLower(0);
//
//        if (ccomplexobject.getOccurrences().isSetUpper())
//            integerInterval.setUpper(ccomplexobject.getOccurrences().getUpper());
//        else
//            integerInterval.setUpper(Integer.MAX_VALUE);
//
//        integerInterval.setLowerIncluded(ccomplexobject.getOccurrences().getLowerIncluded());
//        integerInterval.setUpperIncluded(ccomplexobject.getOccurrences().getUpperIncluded());
//
//        cComplexObject.setOccurrences(integerInterval);
//        cComplexObject.setNodeId(ccomplexobject.getNodeId());
//        cComplexObject.setPath(path);

//        CAttribute value_attribute = ccomplexobject.

        return null;

    }

	private Object handleCObject(OPERATIONALTEMPLATE opt, COBJECT cobj, Map<String, String> termDef, Map<String, Object> valueMap, String attrName, String path) throws Exception {
		// if ( cobj.getOccurrences().isAvailable() ) {
		log.debug("cobj=" + cobj.getClass() + ":" + cobj.getRmTypeName());

		if (cobj instanceof CARCHETYPEROOT) {
			if (!((CARCHETYPEROOT) cobj).getArchetypeId().getValue().isEmpty()) {
				path = path + "[" + ((CARCHETYPEROOT) cobj).getArchetypeId().getValue() + "]";
			}
			log.debug("CARCHETYPEROOT path=" + path);
			return handleArchetypeRoot(opt, (CARCHETYPEROOT) cobj, attrName, path);
		} else if (cobj instanceof CDOMAINTYPE) {
			return createDomainTypeObject((CDOMAINTYPE) cobj, termDef, path);
		} else if (cobj instanceof CCOMPLEXOBJECT) {
			// Skip when path is /category and /context
			if ("/category".equalsIgnoreCase(path)) {
				return null;
			}
            else if ("/context".equalsIgnoreCase(path)){
                return handleComplexObject(opt, (CCOMPLEXOBJECT) cobj, termDef, attrName, path);
            }
			if (!((CCOMPLEXOBJECT) cobj).getNodeId().isEmpty()) {
				path = path + "[" + ((CCOMPLEXOBJECT) cobj).getNodeId() + "]";
			}
			log.debug("CONTEXT path=" + path);
			return handleComplexObject(opt, (CCOMPLEXOBJECT) cobj, termDef, attrName, path);
		} else if (cobj instanceof ARCHETYPESLOT) {
			if (!((ARCHETYPESLOT) cobj).getNodeId().isEmpty()) {
				path = path + "[" + ((ARCHETYPESLOT) cobj).getNodeId() + "]";
			}
			ARCHETYPESLOT slot = (ARCHETYPESLOT) cobj;
			// slot.

			// slot.getIncludes().get(0).
			log.debug("ARCHETYPESLOT path=" + path);
			return null;
			// return handleComplexObject(opt, (CCOMPLEXOBJECT) cobj, termDef,
			// attrName, path);
		} else if (cobj instanceof CPRIMITIVEOBJECT) {
			return createPrimitiveTypeObject((CPRIMITIVEOBJECT) cobj);
		} else {
			if (cobj.getNodeId() == null) {
				log.debug("NodeId is null : " + cobj);
				return null;
			}
			log.debug("Some value cannot process because is not CARCHETYPEROOT or CCOMPLEXOBJECT : "
					+ cobj);

			return null;

		}

	}

	private String getCompName(String nodeId, String name) {
		if (name != null && !name.isEmpty()) {
			if (nodeId != null && !nodeId.isEmpty()) {
				return name + "[" + nodeId + "]";
			} else {
				return name;
			}
		} else {
			return nodeId;
		}
	}

	public ConstraintMapper getConstraintMapper() {
		return constrainMapper;
	}
}
