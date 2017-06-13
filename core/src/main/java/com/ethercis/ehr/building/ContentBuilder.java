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

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.EncodeUtil;
import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.constraints.ConstraintUtils;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.keyvalues.I_PathValue;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.LocatableHelper;
import com.ethercis.ehr.util.MapInspector;
import com.ethercis.ehr.util.RMDataSerializer;
import com.ethercis.validation.ConstraintMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.common.generic.PartyProxy;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.composition.content.entry.*;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.IntervalEvent;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.LocatableRef;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.rm.support.identification.UIDBasedID;
import org.openehr.schemas.v1.*;

import java.io.InputStream;
import java.util.*;

import static org.openehr.build.SystemValue.*;

/**
 * Abstract class to manage the content of a composition (an Entry) under its serialized representation.
 * At this point in time, the serialization is using the value tree  (Maps and Arrays) as a JSON structure.
 * Created by C. Chevalley on 4/3/2015.
 */
public abstract class ContentBuilder implements I_ContentBuilder{

    protected String templateId;
    protected String entry; // JSON representation
    protected Map<SystemValue, Object> values;
    protected Composition composition;
    private Deque<Map<String, Object>> stack;
    protected I_KnowledgeCache knowledge;
    static Logger log = LogManager.getLogger(ContentBuilder.class);
    private String rootArchetypeId;
    private Map<String, String> ltreeMap;
    protected ConstraintMapper constraintMapper;
    protected boolean lenient = false;

    private LocatableHelper locatableHelper = new LocatableHelper();

    //public static final String TAG_OBJECT =  "/$OBJECT$";

    public ContentBuilder(Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        this.values = values;
        this.knowledge = knowledge;
        this.templateId = templateId;
        //set lenient from the environment
        if (System.getProperty("validation.lenient") != null){
            lenient = Boolean.parseBoolean(System.getProperty("validation.lenient"));
            log.debug("ContentBuilder validation set to: "+(lenient ? "lenient" : "active"));
        }
    }

    @Override
    public void setEntryData(Composition composition) throws Exception {
        //retrieve the JSON representation for persistence
        //CHC: 29.09.16 new encoding method :)
        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
        String dbEncoded = compositionSerializer.dbEncode(composition);
        this.composition = composition;
        this.entry = dbEncoded;
        this.rootArchetypeId = compositionSerializer.getTreeRootArchetype();
        this.ltreeMap = compositionSerializer.getLtreeMap();
    }


    protected void assignElementWrapper(ElementWrapper item, Object object, String path){
        try {
//                if (!(object instanceof Map)) //case of data at node level
//                    item.getAdaptedElement().getValue().parse((String)((Map<String, Object>) object).get("value"));
            if (object instanceof DataValue) {
                item.getAdaptedElement().setValue((DataValue) object);
                item.getWrappedValue().setAdaptee(item.getAdaptedElement().getValue());
                item.setDirtyBit(true);
            }
            else {
                log.error("Object in path:" + path + " is not a Datavalue");
                throw new IllegalArgumentException("Object in path:"+path+" is not a Datavalue");
            }

        } catch (Exception e){
            throw new IllegalArgumentException("Potentially invalid path: " + path);
        }

    }

    private static DvText getNewName(Map<String, Object> definition){
        Object nameDefinition = definition.get(CompositionSerializer.TAG_NAME);
        if (nameDefinition instanceof String) { //backward compatibility
            String name = (String) definition.get(CompositionSerializer.TAG_NAME);
            CodePhrase codePhrase = null;
            if (definition.containsKey(CompositionSerializer.TAG_DEFINING_CODE)) { //DvCodedText for name
                Map definingCodeMap = (LinkedTreeMap) definition.get(CompositionSerializer.TAG_DEFINING_CODE);
                String terminologyId = (String) ((Map) definingCodeMap.get("terminologyId")).get("value");
                String codeString = (String) definingCodeMap.get("codeString");
                codePhrase = new CodePhrase(terminologyId, codeString);
            }
            DvText newName;
            if (codePhrase != null)
                newName = new DvCodedText(name, codePhrase);
            else //DvText
                newName = new DvText(name);

            return newName;
        }
        else if (nameDefinition instanceof Map){
            String name = (String)((Map) nameDefinition).get("value");
            CodePhrase codePhrase = null;
            if (((Map) nameDefinition).containsKey(CompositionSerializer.TAG_DEFINING_CODE)) { //DvCodedText for name
                Map definingCodeMap = (LinkedTreeMap) ((Map) nameDefinition).get(CompositionSerializer.TAG_DEFINING_CODE);
                String terminologyId = (String) ((Map) definingCodeMap.get("terminologyId")).get("value");
                String codeString = (String) definingCodeMap.get("codeString");
                codePhrase = new CodePhrase(terminologyId, codeString);
            }
            DvText newName;
            if (codePhrase != null)
                newName = new DvCodedText(name, codePhrase);
            else //DvText
                newName = new DvText(name);

            return newName;

        }
        else if (nameDefinition instanceof List){
            nameDefinition = ((List) nameDefinition).get(0);
            String name = (String)((Map) nameDefinition).get("value");
            CodePhrase codePhrase = null;
            if (((Map) nameDefinition).containsKey(CompositionSerializer.TAG_DEFINING_CODE)) { //DvCodedText for name
                Map definingCodeMap = (LinkedTreeMap) ((Map) nameDefinition).get(CompositionSerializer.TAG_DEFINING_CODE);
                String terminologyId = (String) ((Map) definingCodeMap.get("terminologyId")).get("value");
                String codeString = (String) definingCodeMap.get("codeString");
                codePhrase = new CodePhrase(terminologyId, codeString);
            }
            DvText newName;
            if (codePhrase != null)
                newName = new DvCodedText(name, codePhrase);
            else //DvText
                newName = new DvText(name);

            return newName;

        }
        else
            throw new IllegalArgumentException("Could not handle name definition:"+nameDefinition);
    }

    @Override
    public Object insertCloneInPath(Locatable locatable, Map<String, Object> definition, String path) throws Exception {
        log.debug("Item could not be located, cloning required:" + path);

        //check for potential sibling
        String siblingPath = locatableHelper.siblingPath(path);
//        Locatable sibling = (Locatable) locatable.itemAtPath(siblingPath);
        Locatable sibling = (Locatable) locatableHelper.itemAtPath(locatable, siblingPath);

        if (sibling != null){
            LocatableHelper.NodeItem parent = LocatableHelper.backtrackItemAtPath(locatable, path);
//            Locatable cloned = LocatableHelper.clone(sibling);
            Locatable cloned = locatableHelper.clone(siblingPath, sibling);
            String parentPath = Locatable.parentPath(path);
            if (definition.containsKey(CompositionSerializer.TAG_NAME)) {
                DvText newName = getNewName(definition);
                cloned.setName(newName);
                //change the name of the adapted element as well
                if (cloned instanceof ElementWrapper) {
                    Element element = ((ElementWrapper)cloned).getAdaptedElement();
                    element.setName(newName);
                }
            }
            else
                locatableHelper.adjustChildrenNames(cloned, parentPath, path);

            locatableHelper.insertCloneInList(parent.getNode(), cloned, parent.getInsertionPath(), path);
            locatableHelper.addItemPath(LocatableHelper.simplifyPath(parentPath));
            log.debug("Inserted sibling at path:" + path);
        }
        else {
            LocatableHelper.NodeItem parent = LocatableHelper.backtrackItemAtPath(locatable, path);
            if (parent != null) {
                Locatable cloned = locatableHelper.cloneChildAtPath(parent.getNode(), parent.getChildPath());
                locatableHelper.adjustChildrenNames(cloned, parent.getChildPath(), path);
                locatableHelper.insertCloneInList(parent.getNode(), cloned, parent.getInsertionPath(), parent.getChildPath());
                locatableHelper.addItemPath(LocatableHelper.simplifyPath(parent.getChildPath()));
            }
//            sibling = (Locatable) locatable.itemAtPath(siblingPath);
        }
        //reference the newly created child
//        Object itemAtPath = locatable.itemAtPath(path);
        Object itemAtPath = locatableHelper.itemAtPath(locatable, path);

        //TODO: set a termination to avoid endless loop...
        //TODO: fail on /content[openEHR-EHR-ACTION.laboratory_test.v1 and name/value='Laboratory test tracker']/ism_transition
        if (itemAtPath == null && sibling == null){

            if (sibling == null && locatable.getParent() == null){
                    //end of recursion (potentially not found)
                    log.warn("Recursion aborted, no more parent for locatable...:"+path);
            }
            else
                itemAtPath = insertCloneInPath(locatable, definition, path); //deeper...
        }

        if (itemAtPath == null) //something really wrong here...
            throw new IllegalArgumentException("Unhandled path in template:"+path+" with definition:"+definition+", possible cause is out of synch template and persisted data (composition id: "+locatable.getName()+")");

        return itemAtPath;
    }

    protected void assignValuesFromStack(Composition composition, ArrayDeque<Map<String, Object>> stack) throws Exception {

        ConstraintUtils constraintUtils = new ConstraintUtils(lenient, composition, constraintMapper);
        //traverse the queue
        for (Map<String, Object> definition: stack){
            if (definition.containsKey("/meta")) continue; //ignore the meta entry (not used to build only for querying

            //get path from the /content part only
            String path = (String)definition.get(CompositionSerializer.TAG_PATH);

            if (path == null) //meta data: name etc.
                continue;

            path = path.substring(path.indexOf(CompositionSerializer.TAG_CONTENT));

            Object object = definition.get(MapInspector.TAG_OBJECT);

            if (object == null) {
                if (definition.containsKey(CompositionSerializer.TAG_VALUE))
                    object = definition.get(CompositionSerializer.TAG_VALUE);
                else
                    continue; //no assignment
            }

            //assignment
            String lastTag = path.substring(path.lastIndexOf("/"));
            if (lastTag.matches(I_PathValue.PARTICIPATION_REGEXP) ||
                    lastTag.equals(CompositionSerializer.TAG_TIMING) ||
                    lastTag.equals(CompositionSerializer.TAG_TIME) ||
                    lastTag.equals(CompositionSerializer.TAG_WIDTH) ||
                    lastTag.equals(CompositionSerializer.TAG_MATH_FUNCTION) ||
                    lastTag.equals(CompositionSerializer.TAG_NARRATIVE) ||
                    lastTag.equals(CompositionSerializer.TAG_ACTION_ARCHETYPE_ID) ||
                    lastTag.equals(CompositionSerializer.TAG_PROVIDER) ||
                    lastTag.equals(I_PathValue.ORIGIN_TAG)){
                path = path.substring(0, path.lastIndexOf("/"));
            }
            else if (path.contains(CompositionSerializer.TAG_ISM_TRANSITION)){
                path = path.substring(0, path.indexOf(CompositionSerializer.TAG_ISM_TRANSITION)+CompositionSerializer.TAG_ISM_TRANSITION.length());
            }
            else if (path.contains(CompositionSerializer.TAG_INSTRUCTION_DETAILS)){
                path = path.substring(0, path.indexOf(CompositionSerializer.TAG_INSTRUCTION_DETAILS)+CompositionSerializer.TAG_INSTRUCTION_DETAILS.length());
            }
            else if (path.contains(CompositionSerializer.TAG_UID)){ //instruction!
                path = path.substring(0, path.indexOf(CompositionSerializer.TAG_UID));
            }


            Object itemAtPath = composition.itemAtPath(path);

            //HACK! if an itemAtPath is already there with dirtyBit == true, just clone the element for this path
//            if (itemAtPath == null || (itemAtPath instanceof ElementWrapper && ((ElementWrapper)itemAtPath).dirtyBitSet())) {
            if (itemAtPath == null) {
                if (!lenient && constraintMapper != null && !constraintMapper.isValidNode(LocatableHelper.simplifyPath(path)))
                    throw new IllegalArgumentException("VALIDATION: Invalid child element at:"+path);
                itemAtPath = insertCloneInPath(composition, definition, path);
            }

            if (itemAtPath instanceof ElementWrapper) {
                assignElementWrapper((ElementWrapper) itemAtPath, object, path);
                setItemAttributes(((ElementWrapper) itemAtPath).getAdaptedElement(), definition);
//                constraintUtils.validateItem(path, itemAtPath);
            }
            else if (itemAtPath instanceof Activity){
                Activity activity = (Activity)itemAtPath;
                if (object instanceof DvParsable) {
                    activity.setTiming((DvParsable) object);
                }
                else if (lastTag.equals(CompositionSerializer.TAG_ACTION_ARCHETYPE_ID)) {
                    activity.setActionArchetypeId((String) object);
                }

            } else if (itemAtPath instanceof Instruction) {
                if (lastTag.equals(CompositionSerializer.TAG_NARRATIVE)) {
                    ((Instruction) itemAtPath).setNarrative((DvText) object);
                }
                else if (lastTag.equals(CompositionSerializer.TAG_UID)) {
                    ((Instruction) itemAtPath).setUid((HierObjectID) object);
                }
            }
            else if (itemAtPath instanceof Action) {
                if (object instanceof DvDateTime) {
                    ((Action) itemAtPath).setTime((DvDateTime) object);
                }
            }
            else if (itemAtPath instanceof History) {
                if (object instanceof DvDateTime) {
                    ((History) itemAtPath).setOrigin((DvDateTime) object);
                    log.debug("DvDateTime set to" + itemAtPath);
                }
                constraintUtils.validateItem(LocatableHelper.simplifyPath(path), itemAtPath);
            }
            else if (itemAtPath instanceof PointEvent){
                if (object instanceof DvDateTime){ //set origin
                    PointEvent originePE = (PointEvent)itemAtPath;
                    originePE.setTime((DvDateTime) object);
                    log.debug("point event time:" + ((PointEvent) itemAtPath).getTime());
                }
            }
            else if (itemAtPath instanceof ISMTransition){ //a node attribute (f.e. ism_transition)
                ISMTransition ismTransition = (ISMTransition)itemAtPath;

                if (lastTag.equals(CompositionSerializer.TAG_CAREFLOW_STEP))
                    ismTransition.setCareflowStep((DvCodedText)object);
                else if (lastTag.equals(CompositionSerializer.TAG_TRANSITION))
                    ismTransition.setTransition((DvCodedText) object);
                else if (lastTag.equals(CompositionSerializer.TAG_CURRENT_STATE))
                    ismTransition.setCurrentState((DvCodedText) object);
                else
                    throw new IllegalArgumentException("Invalid tag in ISMTransition:"+lastTag);

            }
            else if (itemAtPath instanceof IntervalEvent){
                IntervalEvent intervalEvent = (IntervalEvent)itemAtPath;
                if (lastTag.equals(CompositionSerializer.TAG_TIME))
                    intervalEvent.setTime((DvDateTime) object);
                else if (lastTag.equals(CompositionSerializer.TAG_WIDTH))
                    intervalEvent.setWidth((DvDuration) object);
                else if (lastTag.equals(CompositionSerializer.TAG_MATH_FUNCTION))
                    intervalEvent.setMathFunction((DvCodedText) object);
                else
                    log.warn("Unhandled Tag in IntervalEvent"+lastTag);
            }
            else if (itemAtPath instanceof InstructionDetails){ //a node attribute (f.e. ism_transition)
                InstructionDetails instructionDetails = (InstructionDetails)itemAtPath;
                if (lastTag.equals(CompositionSerializer.TAG_ACTIVITY_ID))
                    instructionDetails.setActivityID((String) object);
                else if (lastTag.equals(CompositionSerializer.TAG_INSTRUCTION_ID))
                    instructionDetails.setInstructionId((LocatableRef) object);
                else
                    throw new IllegalArgumentException("Invalid tag in InstructionDetails:"+lastTag);
            }
            else if (!(itemAtPath instanceof Entry)){
                log.warn("Unhandled value in stack:" + path+", item:"+itemAtPath);
            }

            //more decoration for Entry...
            if (itemAtPath instanceof Entry) {
                if (object instanceof Participation) {
                    Entry entry = ((Entry) itemAtPath);
                    entry.addOtherParticipation((Participation) object);
                } else if (object instanceof HierObjectID) {
                    ((Entry) itemAtPath).setUid((HierObjectID) object);
                } else if (lastTag.equals(CompositionSerializer.TAG_WORKFLOW_ID)) {
                    ((Entry) itemAtPath).setWorkflowId((ObjectRef) object);
                } else if (lastTag.equals(CompositionSerializer.TAG_PROVIDER)){
                    ((Entry)itemAtPath).setProvider((PartyIdentified)object);
                }
            }

            if (itemAtPath instanceof CareEntry) {
                if (lastTag.equals(CompositionSerializer.TAG_GUIDELINE_ID)) {
                    ((CareEntry) itemAtPath).setGuidelineId((ObjectRef) object);
                }
            }

        }
//        constraintUtils.validateCardinality(composition);
//        constraintUtils.validateElementConstraints(composition);
    }


    /**
     * used to assign an ItemTree (for example other_context)
     * @param itemStructure
     * @param stack
     * @throws Exception
     */
    protected void assignValuesFromStack(ItemStructure itemStructure, ArrayDeque<Map<String, Object>> stack) throws Exception {
        //traverse the queue
//        ConstraintUtils constraintUtils = new ConstraintUtils(lenient, itemStructure, constraintMapper);

        for (Map<String, Object> definition: stack){
            String path = (String)definition.get(CompositionSerializer.TAG_PATH);
            Object object = definition.get(MapInspector.TAG_OBJECT);

            if (object == null) continue; //no assignment

            path = path.substring(path.indexOf("]")+1); //strip the prefix since it is the root

            Object itemAtPath = itemStructure.itemAtPath(path);

            if (itemAtPath == null) {
                itemAtPath = insertCloneInPath(itemStructure, definition, path);
            }

            if (itemAtPath instanceof ElementWrapper) {
                assignElementWrapper((ElementWrapper) itemAtPath, object, path);
                setItemAttributes(((ElementWrapper) itemAtPath).getAdaptedElement(), definition);
//                constraintUtils.validateItem(path, itemAtPath);
            }
            else if (itemAtPath instanceof Activity){
                Activity activity = (Activity)itemAtPath;
                if (object instanceof DvParsable) {
                    activity.setTiming((DvParsable) object);
                }
            }
            else if (itemAtPath instanceof Entry){
                if (object instanceof Participation){
                    Entry entry = ((Entry)itemAtPath);
                    entry.addOtherParticipation((Participation) object);
                }
                if (itemAtPath instanceof Instruction && object instanceof HierObjectID){
                    ((Entry)itemAtPath).setUid((HierObjectID)object);
                }
            }
        }
        //check if all required element have been set

//        constraintUtils.validateElementConstraints();
//        constraintUtils.validateCardinality();
    }

    /**
     * import an XML representation  of a standard RM composition
     *
     *     The composition is intended then to be stored in the DB, as it does not contains any constraints<br>
     *     The import is induced by creating a Contribution containing a number of Compositions.
     *     Hence the use case would be:<br>
     *         <ul>
     *             <li>import the XML string and generate the corresponding RM composition</li>
     *             <li>save the composition into the DB</li>
     *             <li>retrieve the composition as required. At this stage the composition is created with constraints</li>
     *         </ul>
     *
     * The Content instance contains this composition alongwith the value map (json) and other attributes
     * @param inputStream an input stream to read the XML representation
     * @return an <b>RM</b> composition
     * @throws Exception
     *
     * @see org.openehr.rm.composition.Composition
     */
    @Override
    public Composition importCanonicalXML(InputStream inputStream) throws Exception {

//        if (templateId == null){
//            throw new IllegalArgumentException("Template ID is required for importing XML Composition");
//        }

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setCharacterEncoding("UTF-8");
        Map<String, String> nameSpacesMap = new HashMap<>();
        nameSpacesMap.put("", "http://schemas.openehr.org/v1");
        xmlOptions.setLoadSubstituteNamespaces(nameSpacesMap);


        CompositionDocument cd = CompositionDocument.Factory.parse(inputStream, xmlOptions);
        COMPOSITION comp = cd.getComposition();

        //get the templateId and generate the constraint map if not lenient
        templateId = comp.getArchetypeDetails().getTemplateId().getValue();
        //force the creation of a dummy composition to assemble the constraints
        generateNewComposition();

//        XMLBinding binding = new XMLBinding();
        XMLBinding binding = new XMLBinding();

        Object rmObj = binding.bindToRM(comp);
        //consistency test
        if (!(rmObj instanceof Composition))
            throw new IllegalArgumentException("Parsed object does not yield an RM composition");

        //validate the result
        new ConstraintUtils(lenient, (Composition)rmObj, constraintMapper).validateLocatable();

        Composition importedComposition = (Composition)rmObj;
        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
        String mapjson = compositionSerializer.dbEncode(importedComposition);

        //create an actual RM composition
        this.entry = mapjson;
        this.composition = importedComposition;

        return (Composition)rmObj;
    }

    @Override
    public Composition importAsRM(Composition composition) throws Exception {

        //the templateId is found in the composition
        this.templateId = composition.getArchetypeDetails().getTemplateId().getValue();

        I_CompositionSerializer compositionSerializer = I_CompositionSerializer.getInstance();
        String mapjson = compositionSerializer.dbEncode(composition);

        //create an actual RM composition
        this.entry = mapjson;
        this.composition = composition;

        return composition;
    }


    /**
     * export an XML representation of a composition.<br>
     *     the composition can be either an extended composition with constraints or a standard RM composition.
     * @param jsonData
     * @param prettyPrint
     * @return
     * @throws Exception
     */
    @Override
    public byte[] exportCanonicalXML(String jsonData, boolean prettyPrint) throws Exception {
        byte[] data = null;
        //generate an XML representation of the current composition
        Composition toSerializeComposition = buildCompositionFromJson(jsonData);

        if (toSerializeComposition == null){
            throw new IllegalArgumentException("Could not build composition from available data...");
        }

        data = exportCanonicalXML(toSerializeComposition, prettyPrint);

        return data;
    }

    /**
     * serialize a composition using XmlBeans (e.g. Canonical XML)
     * <p>
     *     NB: serialization using XStream does not yield an XML representation compatible with XmlBeans schema...
     * </p>
     * @param composition an RM composition
     * @param prettyPrint true if indented Xml is wanted
     * @return byte array representing the resulting Xml String
     * @throws Exception
     */
    @Override
    public byte[] exportCanonicalXML(Composition composition, boolean prettyPrint) throws Exception {
        return canonicalExporter(composition, prettyPrint, false);
    }

    @Override
    public byte[] exportCanonicalXML(Composition composition, boolean prettyPrint, boolean anyElement) throws Exception {
        return canonicalExporter(composition, prettyPrint, anyElement);
    }

    @Override
    public byte[] exportCanonicalXML(Locatable locatable, boolean prettyPrint) throws Exception {
        return canonicalExporter(locatable, prettyPrint, false);
    }

    @Override
    public byte[] exportCanonicalXML(Locatable locatable, boolean prettyPrint, boolean anyElement) throws Exception {
        return canonicalExporter(locatable, prettyPrint, anyElement);
    }


    public static final String SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String SCHEMA_OPENEHR_ORG_V1 = "http://schemas.openehr.org/v1";


    public static byte[] canonicalExporter(Composition composition, boolean prettyPrint, boolean anyElement){
        byte[] data = null;
        //generate an XML representation of the current composition
        if (composition == null){
            throw new IllegalArgumentException("Could not build composition from available data...");
        }

        try {
//            XStream xStream = new XStream();
//            String xml = xStream.toXML(composition); // this does not marshal consistently with XmlBeans...

            XMLBinding xmlBinding = new XMLBinding(anyElement);
            Object object = xmlBinding.bindToXML(composition, true);

            if (!(object instanceof COMPOSITION))
                throw new IllegalArgumentException("Invalid binding of object, resulting in class:"+object.getClass());

            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setCharacterEncoding("UTF-8");
            xmlOptions.setUseDefaultNamespace();
        	HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
//		    uriToPrefixMap.put(SCHEMA_XSI, "xsi");
		    uriToPrefixMap.put("", SCHEMA_OPENEHR_ORG_V1);
		    xmlOptions.setLoadSubstituteNamespaces(uriToPrefixMap);
            xmlOptions.setSaveAggressiveNamespaces();
            xmlOptions.setSaveNamespacesFirst();
            xmlOptions.setSaveOuter();

            if (prettyPrint) {
                xmlOptions.setSavePrettyPrint();
                xmlOptions.setSavePrettyPrintIndent(4);
            }

            xmlOptions.setDocumentType(CompositionDocument.type);

            xmlOptions.setSaveUseOpenFrag();

            String xml = ((COMPOSITION)object).xmlText(xmlOptions);

//            String xml = ((COMPOSITIONImpl)object).xmlText();
//            try {
                data = xml.getBytes();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    private static SchemaType implyClass(LOCATABLE anItem){
        String archetypeNodeId = anItem.getArchetypeNodeId();
        String rmClassName = archetypeNodeId.substring("openEHR-EHR-".length(), archetypeNodeId.indexOf("."));

        switch (rmClassName){
            case "ITEM_TREE":
                return ITEMTREE.type;
            case "ITEM_SINGLE":
                return ITEMSINGLE.type;
            case "ITEM_TABLE":
                return ITEMTABLE.type;
            case "ITEM_LIST":
                return ITEMLIST.type;
            case "CLUSTER":
                return CLUSTER.type;
            case "ELEMENT":
                return ELEMENT.type;
            default:
                throw new IllegalArgumentException("Could not imply class:"+rmClassName);
        }
    }

    public static Locatable parseOtherDetailsXML(InputStream otherDetailsXmlStream) throws Exception {
        ItemsDocument items =  ItemsDocument.Factory.parse(otherDetailsXmlStream);
        XMLBinding binding = new XMLBinding();
        LOCATABLE locatable = items.getItems();
        SchemaType itemType = implyClass(locatable);
        Object rmObj = binding.bindToRM(locatable.changeType(itemType));
        if (rmObj instanceof Locatable)
            return (Locatable)rmObj;
        else
            throw new IllegalArgumentException("Generated object is not a Locatable:"+rmObj);
    }


    public static byte[] canonicalExporter(Locatable locatable, boolean prettyPrint, boolean anyElement){
        byte[] data = null;
        //generate an XML representation of the current composition
        if (locatable == null){
            throw new IllegalArgumentException("No locatable given (locatable == null)");
        }

        try {
//            XStream xStream = new XStream();
//            String xml = xStream.toXML(composition); // this does not marshal consistently with XmlBeans...

            XMLBinding xmlBinding = new XMLBinding(anyElement);
            Object object = xmlBinding.bindToXML(locatable, false);

            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setUseDefaultNamespace();
            HashMap<String, String> uriToPrefixMap = new HashMap<String, String>();
//		    uriToPrefixMap.put(SCHEMA_XSI, "xsi");
            uriToPrefixMap.put("", SCHEMA_OPENEHR_ORG_V1);
            xmlOptions.setLoadSubstituteNamespaces(uriToPrefixMap);
            xmlOptions.setSaveAggressiveNamespaces();
            xmlOptions.setSaveNamespacesFirst();
            xmlOptions.setSaveOuter();

            if (prettyPrint) {
                xmlOptions.setSavePrettyPrint();
                xmlOptions.setSavePrettyPrintIndent(4);
            }

            xmlOptions.setDocumentType(CompositionDocument.type);

            xmlOptions.setSaveUseOpenFrag();

            if (object != null) {
                String xml = ((LOCATABLE) object).xmlText(xmlOptions);

//                try {
                    data = xml.getBytes("UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
            }
            else //empty object...
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    protected MapInspector getMapInspector(String jsonData) throws Exception {
        GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
        Gson gson = builder.setPrettyPrinting().create();

        Map<String, Object> retmap = gson.fromJson(jsonData, TreeMap.class);

        //traverse the tree
        MapInspector inspector = new MapInspector();
        inspector.inspect(retmap);
        return inspector;
    }

    @Override
    public Composition buildCompositionFromJson(String jsonData) throws Exception {

        //TODO: use a cache mechanism for generated composition
        //time measured is about 250 ms to generate the composition and 15ms to set the values...
        long start = System.nanoTime();
        Composition newComposition = this.generateNewComposition(); //will use the corresponding class instance generator
        long end = System.nanoTime();

        log.debug("generate composition [ms]:"+(end - start)/1000000);

        if (jsonData == null || jsonData.equals("null"))
            return newComposition;  //this is a composition without content (perfectly valid...)

        MapInspector inspector = getMapInspector(jsonData);

        start = System.nanoTime();
        assignValuesFromStack(newComposition, (ArrayDeque) inspector.getStack());
        end = System.nanoTime();

        log.debug("set values [ms]:"+(end - start)/1000000);

        this.composition = newComposition;

        return newComposition;
    }

    @Override
    public Locatable buildLocatableFromJson(String jsonData) throws Exception {
        MapInspector inspector = getMapInspector(jsonData);

        //TODO: use a cache mechanism for generated composition
        //time measured is about 250 ms to generate the composition and 15ms to set the values...
        long start = System.nanoTime();
        Locatable newLocatable = this.generate(); //will use the corresponding class instance generator
        long end = System.nanoTime();

        log.debug("generate locatable [ms]:"+(end - start)/1000000);

        start = System.nanoTime();
        if (newLocatable instanceof ItemStructure)
            assignValuesFromStack((ItemStructure)newLocatable, (ArrayDeque) inspector.getStack());
        else
            throw new IllegalArgumentException("Could not handle locatable:"+newLocatable);
        end = System.nanoTime();

        log.debug("set values [ms]:"+(end - start)/1000000);

        return newLocatable;
    }

    @Override
    public void bindOtherContextFromJson(Composition composition, String jsonData) throws Exception {
        if (jsonData == null || composition == null)
            return;
        MapInspector inspector = getMapInspector(jsonData);
        if (composition.getContext().getOtherContext() == null){
            List<Item> items = new ArrayList<>();
            ItemStructure otherContextStructure = new ItemTree("/items[at0001]", new DvText("Tree"), items);
            composition.getContext().setOtherContext(otherContextStructure);
        }
        assignValuesFromStack(composition.getContext().getOtherContext(), (ArrayDeque) inspector.getStack());
    }

    @Override
    public void bindItemStructureFromJson(ItemStructure itemStructure, String jsonData) throws Exception {
        MapInspector inspector = getMapInspector(jsonData);
        assignValuesFromStack(itemStructure, (ArrayDeque) inspector.getStack());
    }

    protected void storeCache(String templateID, Composition composition, ConstraintMapper constraintMapper) throws Exception {
        byte[] bytes = RMDataSerializer.serializeRaw(composition);
        knowledge.cacheGenerated(templateID, bytes, constraintMapper);
    }

    protected Composition retrieveCache(String id) throws Exception {
        if (knowledge.cacheContainsLocatable(id)) {
            Object retrievedObject = RMDataSerializer.unserializeRaw((byte[])knowledge.retrieveGenerated(id));
            if (retrievedObject instanceof Composition)
                return (Composition)retrievedObject ;
            else
                throw new IllegalArgumentException("Cache inconsistency, cache object is not a composition:"+id);
        }
        else
            return null;
    }

    protected ConstraintMapper retrieveConstraintMapper(String id) throws Exception {
        if (knowledge.cacheContainsLocatable(id)) {
            return knowledge.retrieveCachedConstraints(id);
        }
        else
            return null;
    }

    @Override
    public String getEntry(){
        return entry;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public Composition getComposition() {
        return composition;
    }

    @Override
    public String getRootArchetypeId() {
        return rootArchetypeId;
    }

    @Override
    public Map<String, String> getLtreeMap() {
        return ltreeMap;
    }

    public void setCompositionParameters(Map<SystemValue, Object> values){
        this.values = values;
    }

    public void setCompositionAttributes(Composition composition){

        if (values == null)
            return;

        for (Map.Entry<SystemValue, Object> systemValue: values.entrySet()){
            switch (systemValue.getKey()){
                case CATEGORY:
                    composition.setCategory((DvCodedText)systemValue.getValue());
                    break;
                case LANGUAGE:
                    composition.setLanguage((CodePhrase)systemValue.getValue());
                    break;
                case TERRITORY:
                    composition.setTerritory((CodePhrase)systemValue.getValue());
                    break;
                case COMPOSER:
                    composition.setComposer((PartyProxy)systemValue.getValue());
                    break;
                case UID:
                    composition.setUid((UIDBasedID)systemValue.getValue());
                    break;
                case CONTEXT:
                    ItemStructure other_context = null;
                    if (composition.getContext().getOtherContext() != null)
                        other_context = composition.getContext().getOtherContext();
                    composition.setContext((EventContext)systemValue.getValue());
                    composition.getContext().setOtherContext(other_context);
                    break;
                default:
                    throw new IllegalArgumentException("Could not handle composition attribute:"+systemValue.getKey());
            }
        }
    }

    private void setItemAttributes(Item item, Map definition){

        if (definition.containsKey(CompositionSerializer.TAG_NAME)) {
            Object name = definition.get(CompositionSerializer.TAG_NAME);
            if (name instanceof String) {
                if (definition.containsKey(CompositionSerializer.TAG_DEFINING_CODE)){
                    Map definingCode = (Map)definition.get(CompositionSerializer.TAG_DEFINING_CODE);
                    DvCodedText codedName = new DvCodedText((String)name, new CodePhrase((String)((Map)definingCode.get("terminologyId")).get("value"), (String)(definingCode.get("codeString"))));
                    item.setName(codedName);
                }
                else
                    item.setName(new DvText((String) name));
            }
            else if (name instanceof DvText || name instanceof DvCodedText)
                item.setName((DvText)name);
//            else
//                throw new IllegalArgumentException("Could not handle name value of type:"+name);
        }
    }

    @Override
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    @Override
    public Boolean isLenient(){
        return lenient;
    }

    @Override
    public ConstraintMapper getConstraintMapper(){
        return constraintMapper;
    }

    @Override
    public Map<String, Integer> getArrayItemPathMap(){
        return locatableHelper.getArrayItemPathMap();
    }
}
