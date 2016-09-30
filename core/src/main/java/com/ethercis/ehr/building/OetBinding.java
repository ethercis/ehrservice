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
import com.ethercis.ehr.encode.wrappers.*;
import com.ethercis.ehr.encode.wrappers.constraints.DataValueConstraints;
import com.ethercis.ehr.encode.wrappers.cprimitives.*;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.terminolology.TerminologyServiceWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.*;
import org.openehr.am.archetype.constraintmodel.primitive.*;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem;
import org.openehr.am.openehrprofile.datatypes.quantity.Ordinal;
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.quantity.DvQuantity;
import org.openehr.rm.datatypes.quantity.ProportionKind;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.identification.TemplateID;
import org.openehr.rm.support.identification.TerminologyID;
import org.openehr.rm.support.terminology.TerminologyService;


import java.util.*;

public class OetBinding extends RmBinding {

    private static final Logger log = LogManager.getLogger(OetBinding.class);

    protected OetBinding() throws Exception {
        super(null);
    }


    /* private constructor */
    protected OetBinding(Map<SystemValue, Object> map) throws Exception {
       super(map);
    }


    /**
     * Create RM object tree based on given archetype with a default minimum strategy
     *
     * @param archetype
     * @return
     * @throws Exception
     */
    public Object create(Archetype archetype) throws Exception {
        return create(archetype, null, null, GenerationStrategy.MINIMUM);
    }

    public Object create(Archetype archetype,
                         GenerationStrategy strategy) throws Exception {
        return create(archetype, null, null, strategy);
    }

    /**
     * Create RM object tree based on given main the archetype and
     * associated archetypes in the archetypeMap
     *
     * TODO
     * This entry is used to create object tree for flattened templates
     *
     * @param archetype
     * @return
     * @throws Exception
     */
    public Object create(Archetype archetype,
                         Map<String, Archetype> archetypeMap) throws Exception {

        return create(archetype, null, archetypeMap, GenerationStrategy.MINIMUM);
    }

    /**
     * Create RM object tree with specified template
     *
     * @param archetype
     * @param templateId
     * @param archetypeMap
     * @param strategy
     * @return
     * @throws Exception
     */
    public Object create(Archetype archetype, String templateId,
                         Map<String, Archetype> archetypeMap,
                         GenerationStrategy strategy) throws Exception {

        return createComplexObject(archetype.getDefinition(), archetype,
                templateId, archetypeMap, strategy);
    }

    /*
     * Entering point for complex object creation
     */
    public Object createComplexObject(CComplexObject ccobj,
                                      Archetype archetype, String templateId,
                                      Map<String,Archetype> archetypeMap,
                                      GenerationStrategy strategy) throws Exception {

        log.debug("create complex object " + ccobj.getRmTypeName());

        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(TEMPLATE_ID, templateId);

        String nodeId = ccobj.getNodeId();
        if(nodeId != null) {
            DvText name, description;

            // root node with archetype_id as node_id
            // TODO check if name is already define?
            if(nodeId.startsWith("openEHR")) {
                archetype = archetypeMap.get(nodeId);
                if(archetype == null) {
                    throw new Exception("unknown archetype for nodeId: " + nodeId);
                }
                name = retrieveName(archetype.getConcept(), archetype);
                description = retrieveDescription(archetype.getConcept(), archetype);
            } else {
                name = retrieveName(nodeId, archetype);
                description = retrieveDescription(nodeId, archetype);
            }
            valueMap.put("name", name);
            valueMap.put("description", description);

            // use archetypeId instead of nodeId for root
            if(nodeId.equals(archetype.getConcept())) {
                nodeId = archetype.getArchetypeId().toString();
            }
            valueMap.put("archetype_node_id", nodeId);
        }

        String rmTypeName = ccobj.getRmTypeName();

        if(ccobj.getAttributes() != null && ccobj.getAttributes().size() > 0) {
            for(CAttribute cattr : ccobj.getAttributes()) {

                // TODO create 'required' attribute
                if(cattr.isAllowed()
                        && (GenerationStrategy.MAXIMUM.equals(strategy)
                        || GenerationStrategy.MAXIMUM_EMPTY.equals(strategy)
                        || (GenerationStrategy.MINIMUM.equals(strategy)
                        && cattr.isRequired()))) {
                    Object attrValue = createAttribute(cattr, archetype,
                            archetypeMap, strategy);
                    valueMap.put(cattr.getRmAttributeName(), attrValue);


                } else if("CLUSTER".equals(rmTypeName)) { // TODO quickfix

                    Object attrValue = createAttribute(cattr, archetype,
                            archetypeMap, strategy);
                    valueMap.put(cattr.getRmAttributeName(), attrValue);
                }
            }
        }

        // deal with missing required attributes in RM
        if("DV_TEXT".equals(rmTypeName)) {

            if(! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_TEXT);
            }

        } else if("DV_CODED_TEXT".equals(rmTypeName)) {

            // TODO type-check is temporary fix
            Object definingCode = valueMap.get(DEFINING_CODE);
            CodePhrase codePhrase = null;


            if( ! (definingCode instanceof CodePhrase)) {
                if (definingCode instanceof CodePhraseVBean) {
                    codePhrase = ((CodePhrase) ((CodePhraseVBean) definingCode).getAdaptee());
                    valueMap.put(DEFINING_CODE, codePhrase);
                }
                else
                    valueMap.put(DEFINING_CODE, new CodePhrase("local", "at9999"));
            } else {
                codePhrase = (CodePhrase) valueMap.get(DEFINING_CODE);
            }


            if(valueMap.get(VALUE) == null) {
                String text = null;

                if(codePhrase != null) {
                    String code = codePhrase.getCodeString();

                    if(isLocallyDefined(codePhrase)) {

                        // archetype terms
                        text = retrieveArchetypeTermText(code, archetype, true);

                    } else if(isOpenEHRTerm(codePhrase)) {

                        // openEHR terms
                        CodePhrase language = (CodePhrase)builder.getSystemValues().get(SystemValue.LANGUAGE);
                        text = openEHRTerminology.rubricForCode(code, language.getCodeString());

                    } else {

                        // externally defined term
                        text = termMap.getText(codePhrase, ccobj.path());
                    }
                }
                if(text == null) {
                    text = DEFAULT_CODED_TEXT;
                }
                valueMap.put(VALUE, text);
            }


        }  else if("DV_URI".equals(rmTypeName) || "DV_EHR_URI".equals(rmTypeName)) {

            if(! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_URI);
            }

        }else if("DV_DATE".equals(rmTypeName)) {

            if( ! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_DATE);
            }

        } else if("DV_PARSABLE".equals(rmTypeName)) {

            if( ! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_TEXT);
            }
            if( ! valueMap.containsKey(FORMALISM)) {
                valueMap.put(FORMALISM, "text");
            }

        } else if("DV_DATE_TIME".equals(rmTypeName)) {

            if( ! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_DATE_TIME);
            }
            valueMap.put(TIME, DEFAULT_TIME);

        } else if("DV_TIME".equals(rmTypeName)) {

            if( ! valueMap.containsKey(VALUE)) {
                valueMap.put(VALUE, DEFAULT_TIME);
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
        } else if("DV_COUNT".equals(rmTypeName)) {

            if(valueMap.get(MAGNITUDE) == null) {
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

        } else if("HISTORY".equals(rmTypeName)) {

            if(! valueMap.containsKey(ORIGIN)) {
                valueMap.put(ORIGIN, new DvDateTime());
            }

        } else if("EVENT".equals(rmTypeName) || "POINT_EVENT".equals(rmTypeName)) {

            if(! valueMap.containsKey(TIME)) {
                valueMap.put(TIME, new DvDateTime());			}

        } else if("OBSERVATION".equals(rmTypeName)) {

            addEntryValues(valueMap, archetype);

        } else if("EVALUATION".equals(rmTypeName)) {

            addEntryValues(valueMap, archetype);

        } else if("ADMIN_ENTRY".equals(rmTypeName)) {

            addEntryValues(valueMap, archetype);

        } else if("ACTION".equals(rmTypeName)) {

            addEntryValues(valueMap, archetype);
            if( ! valueMap.containsKey(TIME)) {
                valueMap.put(TIME, new DvDateTime());
            }
            if(valueMap.get(DESCRIPTION) == null) {
                valueMap.put(DESCRIPTION, new ItemTree(RmBinding.DEFAULT_NODE_ID, new DvText(RmBinding.DEFAULT_DESCRIPTION_NAME), null));
            }

        } else if("INSTRUCTION".equals(rmTypeName)) {

            if( ! valueMap.containsKey(NARRATIVE)) {
                valueMap.put(NARRATIVE, new DvText(RmBinding.DEFAULT_NARRATIVE));
            }
            addEntryValues(valueMap, archetype);

        } else if("ACTIVITY".equals(rmTypeName)) {

            if( ! valueMap.containsKey(TIMING)) {
                valueMap.put(TIMING, new DvParsable(RmBinding.DEFAULT_TIMING_SCHEME, "txt"));
            }
            if ( ! valueMap.containsKey(ACTION_ARCHETYPE_ID)){
                valueMap.put(ACTION_ARCHETYPE_ID, RmBinding.DEFAULT_ACTION_ARCHETYPE_ID);
            }

        } else if("COMPOSITION".equals(rmTypeName)) {

            setCompositionValues(valueMap);

            addEntryValues(valueMap, archetype);

        } else if("ELEMENT".equals(rmTypeName)) {

            if(GenerationStrategy.MAXIMUM_EMPTY.equals(strategy)
                    && "INPUT".equals(ccobj.getAnnotation())) {

                // TODO input annotation needs to be standardized
                valueMap.put(VALUE, null);
                valueMap.put(NULL_FLAVOUR, NULL_FLAVOUR_VALUE);


            } else {

                // special fix to create a wrapping dv_coded_text of code_phrase
                // should not be necessary now when the AOM from flattener is fixed
                Object value;
//                if (valueMap.get(VALUE) instanceof  I_VBeanWrapper)
//				    value = ((I_VBeanWrapper)valueMap.get(VALUE)).getAdaptee();
//                else
                value = valueMap.get(VALUE);

                String text = null;
                if(value instanceof CodePhrase) {
                    CodePhrase code = (CodePhrase) value;
                    text = termMap.getText(code, ccobj.path() + "/value");
                    if(text == null) {
                        text = DEFAULT_CODED_TEXT;
                    }
                    value = new DvCodedText(text, code);
                    valueMap.put(VALUE, value);
                }
            }

        } else if("EVENT_CONTEXT".equals(rmTypeName)) {

            if( ! valueMap.containsKey(START_TIME)) {
                valueMap.put(START_TIME, new DvDateTime());
            }

            if( ! valueMap.containsKey(SETTING)) {
                valueMap.put(SETTING, new DvCodedText("emergency care", new CodePhrase("openehr", "227")));
            }

        } else if("SECTION".equals(rmTypeName)) {

            List list = (List) valueMap.get("items");
            if(list != null && list.isEmpty()) {
                valueMap.remove("items");
            }
        }

        // special fix for event
        if("EVENT".equals(rmTypeName)) {
            rmTypeName = "POINT_EVENT";
        }

        Object obj = builder.construct(rmTypeName, valueMap);
        //we have now a raw RM object
        Object retobj = obj;

        //wrap an Element object and add the value wrapper and constraints container
        //TODO: use an option in DomainBuilder constructor to decide to instanciate Element or ElementWrapper
        if (obj instanceof Element){
            Element element = (Element)obj;
            ElementWrapper wrapper = new ElementWrapper(element, ccobj);

            Object value = valueMap.get("value");

            if (value != null && value instanceof I_VBeanWrapper)
                wrapper.setWrappedValue((I_VBeanWrapper)valueMap.get("value"));

            DataValueConstraints constraints = VBeanUtil.getConstraintInstance(builder, wrapper.getAdaptedElement().getValue());
            if (constraints != null) {
                if (valueMap.get("description") != null) {
                    constraints.setDescription(((DvText) valueMap.get("description")).getValue());
                }

                wrapper.setConstraints(archetype, constraints);
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

        //decorate
        retobj.getClass();


        return retobj;
    }

    private void addEntryValues(Map<String, Object> valueMap,
                                Archetype archetype) throws Exception {

        CodePhrase lang = new CodePhrase("ISO_639-1", "en");
        CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
        String tid = (String) valueMap.get(TEMPLATE_ID);
        TemplateID templateId = tid == null ? null : new TemplateID(tid);
        Archetyped archetypeDetails = new Archetyped(
                archetype.getArchetypeId(), templateId, "1.0.1");

        valueMap.put("subject", subject());
        //valueMap.put("provider", provider());
        valueMap.put("encoding", charset);
        valueMap.put("language", lang);
        valueMap.put("archetype_details", archetypeDetails);
    }


    public Object createAttribute(CAttribute cattribute, Archetype archetype,
                                  Map<String, Archetype> archetypeMap, GenerationStrategy strategy)
            throws Exception {

        log.debug("create attribute " + cattribute.getRmAttributeName());

        List<CObject> children = cattribute.getChildren();
        if(cattribute instanceof CSingleAttribute) {

            log.debug("single attribute..");

            if(children != null && children.size() > 0) {
                // TODO first child is used for rm generation
                CObject cobj = children.get(0);
                return createObject(cobj, archetype, archetypeMap, strategy);
            } else {
                throw new Exception ("no child object..");
            }
        } else { // multiple c_attribute

            log.debug("multiple attribute..");

            CMultipleAttribute cma = (CMultipleAttribute) cattribute;
            Collection container;
            if(cma.getCardinality().isList()) {
                container = new ArrayList<Object>();

            } else {
                // default container type
                container = new ArrayList<Object>();
            }
            for(CObject cobj : children) {

                log.debug("looping children, required: " + cobj.isRequired());

                // TODO only create 'required' child
                if(cobj.isAllowed() &&
                        (GenerationStrategy.MAXIMUM.equals(strategy)
                                || GenerationStrategy.MAXIMUM_EMPTY.equals(strategy)
                                || (GenerationStrategy.MINIMUM.equals(strategy)
                                && cobj.isRequired()))) {

                    log.debug("required child");

                    Object obj = createObject(cobj, archetype, archetypeMap, strategy);
                    if(obj != null) {
                        container.add(obj);
                    }
                }

                // special fix for mistaken optional event:
                // events cardinality matches {1..*; unordered} matches {
                //     EVENT[at0003] occurrences matches {0..*} matches {
                else if("events".equals(cma.getRmAttributeName())
                        && "EVENT".equals(cobj.getRmTypeName())) {

                    log.debug("mandatory events attribute fix");

                    container.add(createObject(cobj, archetype, archetypeMap,
                            strategy));
                }
            }

            // TODO special rule to include first child
            if(container.isEmpty()) {

                log.debug("add first child for empty container attribute");

                // disabled
                // container.add(bindConstraintObject(children.get(0), archetype));
            }

            return container;
        }
    }

    public Object createObject(CObject cobj, Archetype archetype,
                               Map<String, Archetype> archetypeMap, GenerationStrategy strategy)
            throws Exception {

        log.debug("create object with constraint " + cobj.getClass());

        if(cobj instanceof CComplexObject) {

            // no need for templateId at this level
            return createComplexObject((CComplexObject) cobj,
                    archetype, null, archetypeMap, strategy);

        } else if(cobj instanceof CPrimitiveObject) {

            return createPrimitiveTypeObject((CPrimitiveObject) cobj, archetype);

        } else if(cobj instanceof CDomainType) {

            return createDomainTypeObject((CDomainType) cobj, archetype);

        } else {
            // TODO skip archetype_slot etc, log.warn?
            log.warn("Unresolved archetype slot, possibly the slot is not filled in the template, ignoring for now:"+cobj);
            return null;
        }
    }

    private Object createPrimitiveTypeObject(CPrimitiveObject cpo, Archetype archetype)
            throws Exception {

        CPrimitive cp = cpo.getItem();

        if(cp instanceof CBoolean) {
            return CBooleanVBean.getDefault((CBoolean) cp);

        } else if(cp instanceof CString) {

            return CStringVBean.getDefault((CString) cp);

        } else if(cp instanceof CDate) {

            return CDateVBean.getDefault((CDate) cp);

        } else if(cp instanceof CTime) {

            return CTimeVBean.getDefault((CTime) cp);

        } else if(cp instanceof CDateTime) {

            return CDateTimeVBean.getDefault((CDateTime) cp);

        } else if(cp instanceof CInteger) {

            return new Integer(Integer.MIN_VALUE);

        } else if(cp instanceof CReal) {

            return new Double(Double.MIN_VALUE);

        } else if(cp instanceof CDuration) {
            return CDurationVBean.getDefault((CDuration) cp);

        }

        // TODO implement other types
        throw new Exception("unsupported primitive type: " + cp.getType());
    }

    private Object createDomainTypeObject(CDomainType cpo, Archetype archetype)
            throws Exception {

        if(cpo instanceof CDvQuantity) {
            //try to find out the attributes of the data field (in particular limits)
            Map map = archetype.getPathNodeMap();
            DvQuantity qty =  createDvQuantity((CDvQuantity) cpo);
            return new DvQuantityVBean(qty);

        } else if(cpo instanceof CCodePhrase) {
            CodePhrase phrase = createCodePhrase((CCodePhrase) cpo);
            return new CodePhraseVBean(phrase);

        } else if(cpo instanceof CDvOrdinal) {
            DvOrdinal ordinal = createDvOrdinal((CDvOrdinal) cpo, archetype);
            return new DvOrdinalVBean(ordinal);

        } else {
            throw new Exception("unsupported c_domain_type: " + cpo.getClass());
        }
    }

    private DvOrdinal createDvOrdinal(CDvOrdinal cdo, Archetype archetype)
            throws Exception {

        if(cdo.getDefaultValue() != null) {
            Ordinal o = cdo.getDefaultValue();
            return new DvOrdinal(o.getValue(),
                    new DvCodedText(DEFAULT_CODED_TEXT, o.getSymbol()));
        }
        Set<Ordinal> list = cdo.getList();
        if(list == null || list.size() == 0) {
            throw new Exception("empty list of ordinal");
        }
        Ordinal ordinal =  list.iterator().next();
        String text = DEFAULT_CODED_TEXT;
        CodePhrase symbol = ordinal.getSymbol();
        String code = symbol.getCodeString();

        if(isLocallyDefined(symbol)) {
            text = retrieveArchetypeTermText(code, archetype, true);
        } else {
            text = termMap.getText(symbol, cdo.path());
        }

        return new DvOrdinal(ordinal.getValue(),
                new DvCodedText(text, ordinal.getSymbol()));
    }

    CodePhrase createCodePhrase(CCodePhrase ccp) throws Exception {
        if(ccp.getDefaultValue() != null) {
            return ccp.getDefaultValue();
        }
        TerminologyID tid = ccp.getTerminologyId();
        List<String> codeList = ccp.getCodeList();

        String code;
        if(codeList == null || codeList.isEmpty()) {
            code = "0123456789";
        } else {
            code = codeList.get(0);
        }
        return new CodePhrase(tid, code);
    }

    DvQuantity createDvQuantity(CDvQuantity cdq) throws Exception {
        if(cdq.getList() == null || cdq.getList().isEmpty()) {
            return new DvQuantity(0.0);
        }
        // TODO take the first item
        CDvQuantityItem item = cdq.getList().get(0);

        // TODO take the lower limit as magnitude or zero
        double magnitude;
        if(item.getMagnitude() != null) {
            magnitude = item.getMagnitude().getLower();
        } else {
            magnitude = 0;
        }

        DvQuantity qty = new DvQuantity(item.getUnits(), magnitude, measurementService);
        return qty;
    }

    /*
     * Retrieves a language-specific name of given nodeId
     *
     */
    DvText retrieveName(String nodeId, Archetype archetype) throws Exception {
        //retrieve the text and remove tailing CRLF (bug in Archetype Designer?)
        String archetypeTermText = retrieveArchetypeTermText(nodeId, archetype, true).replaceAll("(\\r|\\n)", "");
        DvText name = new DvText(archetypeTermText);
        return name;
    }

    DvText retrieveDescription(String nodeId, Archetype archetype) throws Exception {
        String description = retrieveArchetypeTermText(nodeId, archetype, false).replaceAll("(\\r|\\n)", "");
        if (description.length() == 0)
            description = "*"; //misconstruct, but avoid exception...
        DvText name = new DvText(description);
        return name;
    }


    /*
     * Retrieves just the text of given nodeId (at code)
     *
     * @param nodeId
     * @param archetype
     * @return
     * @throws Exception
     */
    String retrieveArchetypeTermText(String nodeId, Archetype archetype, boolean text) throws Exception {
        //use the language defined globally,
        //should be defined on a per user basis, hence this is a parameter coming from the session context

        String language; //use the language defined for the builder if any, otherwise use the archetype default
        String defaultlanguage = archetype.getOriginalLanguage().getCodeString();

        CodePhrase setlang = (CodePhrase)builder.getSystemValues().get(SystemValue.LANGUAGE);
        if (setlang != null)
            language = setlang.getCodeString();
        else {
            language = defaultlanguage; //use default from archetype
        }

        String rettext;

        rettext = retrieveArchetypeTermText(language, nodeId, archetype, text);

        if (rettext == null) {
            rettext = retrieveArchetypeTermText(defaultlanguage, nodeId, archetype, text);
            if (rettext == null) {
                throw new Exception("term of given code: " + nodeId +
                        ", language: " + language + " not found..");
            }
        }

        return rettext;
    }

    /**
     * retrieve a support text associated to a defined language
     * @param language
     * @param nodeId
     * @param archetype
     * @param text if true retrieve text, description otherwise
     * @return
     */
    private String retrieveArchetypeTermText(String language, String nodeId, Archetype archetype, boolean text) {

        ArchetypeTerm term = archetype.getOntology().termDefinition(language, nodeId);

        if(term == null) {
            return null;
        }

        if (text)
            return term.getText();
        else
            return term.getDescription();
    }


    void processCObject(CObject cobj) {

    }
}