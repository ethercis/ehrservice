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
import com.ethercis.ehr.encode.DvDateAdapter;
import com.ethercis.ehr.encode.DvDateTimeAdapter;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.keyvalues.I_PathValue;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.util.LocatableHelper;
import com.ethercis.ehr.util.MapInspector;
import com.ethercis.ehr.util.RMDataSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.build.SystemValue;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.entry.*;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.HierObjectID;
import org.openehr.rm.support.identification.LocatableRef;
import org.openehr.rm.support.identification.ObjectRef;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;
import org.openehr.schemas.v1.impl.COMPOSITIONImpl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
    static Logger log = Logger.getLogger(ContentBuilder.class);

    //public static final String TAG_OBJECT =  "/$OBJECT$";

    public ContentBuilder(Map<SystemValue, Object> values, I_KnowledgeCache knowledge, String templateId) throws Exception {
        this.values = values;
        this.knowledge = knowledge;
        this.templateId = templateId;
    }

    @Override
    public void setEntryData(Composition composition) throws Exception {
        //retrieve the JSON representation for persistence
        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object> retMap = inspector.process(composition);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//        builder.registerTypeAdapter(ArrayList.class, new ArrayListAdapter());

         //choose this option to ease reading and debugging... but not for storing into DB
        Gson gson = builder.setPrettyPrinting().create();
        /*
        Gson gson = builder.create();
        */
        this.composition = composition;
        this.entry = gson.toJson(retMap);
    }

//    public String setEntryData(Object entry) throws Exception {
//        //retrieve the JSON representation for persistence
//        CompWalker inspector = new CompWalker(CompWalker.WalkerOutputMode.NAMED);
//        Map<String, Object> retmap = inspector.process(entry, CompWalker.TAG_CONTENT);
//
//        GsonBuilder builder = new GsonBuilder();
//        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//
//        //choose this option to ease reading and debugging... but not for storing into DB
//        Gson gson = builder.setPrettyPrinting().create();
//        /*
//        Gson gson = builder.create();
//        */
//
//        return gson.toJson(retmap);
//    }


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

    protected void assignValuesFromStack(Composition composition, ArrayDeque<Map<String, Object>> stack) throws Exception {

        //traverse the queue
        for (Map<String, Object> definition: stack){
            String path = (String)definition.get(CompositionSerializer.TAG_PATH);

            //get path from the /content part only
            path = path.substring(path.indexOf(CompositionSerializer.TAG_CONTENT));

            Object object = definition.get(MapInspector.TAG_OBJECT);

            if (object == null) continue; //no assignment

            //assignment
            String lastTag = path.substring(path.lastIndexOf("/"));
            if (lastTag.matches(I_PathValue.PARTICIPATION_REGEXP) ||
                    lastTag.equals(CompositionSerializer.TAG_TIMING) ||
                    lastTag.equals(CompositionSerializer.TAG_TIME) ||
                    lastTag.equals(CompositionSerializer.TAG_NARRATIVE) ||
                    lastTag.equals(I_PathValue.ORIGIN_TAG)){
                path = path.substring(0, path.lastIndexOf("/"));
            }
            else if (path.contains(CompositionSerializer.TAG_ISM_TRANSITION)){
                path = path.substring(0, path.indexOf(CompositionSerializer.TAG_ISM_TRANSITION)+CompositionSerializer.TAG_ISM_TRANSITION.length());
            }
            else if (path.contains(CompositionSerializer.TAG_INSTRUCTION_DETAILS)){
                path = path.substring(0, path.indexOf(CompositionSerializer.TAG_INSTRUCTION_DETAILS)+CompositionSerializer.TAG_INSTRUCTION_DETAILS.length());
            }

            Object itemAtPath = composition.itemAtPath(path);

            //HACK! if an itemAtPath is already there with dirtyBit == true, just clone the element for this path
//            if (itemAtPath == null || (itemAtPath instanceof ElementWrapper && ((ElementWrapper)itemAtPath).dirtyBitSet())) {
            if (itemAtPath == null) {
                log.debug("Item could not be located, cloning required:" + path);
                LocatableHelper.NodeItem parent = LocatableHelper.backtrackItemAtPath(composition, path);
                if (parent != null){
                    Locatable cloned = LocatableHelper.cloneChildAtPath(parent.getNode(), parent.getChildPath());
                    LocatableHelper.insertChildInList(parent.getNode(), cloned, parent.getInsertionPath());
                }
                //reference the newly created child
                itemAtPath = composition.itemAtPath(path);
                if (itemAtPath == null) //something really wrong here...
                    throw new IllegalArgumentException("INTERNAL: failed to successfully clone child structure at:"+path);
            }

            if (itemAtPath instanceof ElementWrapper) {
                assignElementWrapper((ElementWrapper) itemAtPath, object, path);

                setItemAttributes(((ElementWrapper) itemAtPath).getAdaptedElement(), definition);
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
                } else if (object instanceof HierObjectID) {
                    ((Entry) itemAtPath).setUid((HierObjectID) object);
                } else if (lastTag.equals(CompositionSerializer.TAG_WORKFLOW_ID)) {
                    ((Entry) itemAtPath).setWorkflowId((ObjectRef) object);
                }
            } else if (itemAtPath instanceof Instruction) {
                if (lastTag.equals(CompositionSerializer.TAG_NARRATIVE)) {
                    ((Instruction) itemAtPath).setNarrative((DvText) object);
                }
            } else if (itemAtPath instanceof CareEntry) {
                if (lastTag.equals(CompositionSerializer.TAG_GUIDELINE_ID)) {
                    ((CareEntry) itemAtPath).setGuidelineId((ObjectRef) object);
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
            }
            else if (itemAtPath instanceof PointEvent){
                if (object instanceof DvDateTime){ //set origin
                    PointEvent originePE = (PointEvent)itemAtPath;
                    originePE.setTime((DvDateTime)object);
                    log.debug("point event time:" + ((PointEvent) itemAtPath).getTime());
                }
            }
            else if (itemAtPath instanceof ISMTransition){ //a node attribute (f.e. ism_transition)
                ISMTransition ismTransition = (ISMTransition)itemAtPath;
                switch (lastTag){
                    case CompositionSerializer.TAG_CAREFLOW_STEP:
                        ismTransition.setCareflowStep((DvCodedText)object);
                        break;
                    case CompositionSerializer.TAG_TRANSITION:
                        ismTransition.setTransition((DvCodedText) object);
                        break;
                    case CompositionSerializer.TAG_CURRENT_STATE:
                        ismTransition.setCurrentState((DvCodedText) object);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid tag in ISMTransition:"+lastTag);
                }
            }
            else if (itemAtPath instanceof InstructionDetails){ //a node attribute (f.e. ism_transition)
                InstructionDetails instructionDetails = (InstructionDetails)itemAtPath;
                switch (lastTag){
                    case CompositionSerializer.TAG_ACTIVITY_ID:
                        instructionDetails.setActivityID((String) object);
                        break;
                    case CompositionSerializer.TAG_INSTRUCTION_ID:
                        instructionDetails.setInstructionId((LocatableRef) object);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid tag in InstructionDetails:"+lastTag);
                }
            }
            else
                log.debug("Unhandled value in stack:"+path);

        }
    }

    /**
     * used to assign an ItemTree (for example other_context)
     * @param itemStructure
     * @param stack
     * @throws Exception
     */
    protected void assignValuesFromStack(ItemStructure itemStructure, ArrayDeque<Map<String, Object>> stack) throws Exception {
        //traverse the queue
        for (Map<String, Object> definition: stack){
            String path = (String)definition.get(CompositionSerializer.TAG_PATH);
            Object object = definition.get(MapInspector.TAG_OBJECT);

            if (object == null) continue; //no assignment

            path = path.substring("/items[at0001]".length()); //strip the prefix since it is the root

            Object itemAtPath = itemStructure.itemAtPath(path);

            if (itemAtPath == null) {
                log.debug("Item could not be located, cloning required:" + path);
                LocatableHelper.NodeItem parent = LocatableHelper.backtrackItemAtPath(itemStructure, path);
                if (parent != null){
                    Locatable cloned = LocatableHelper.cloneChildAtPath(parent.getNode(), parent.getChildPath());
                    LocatableHelper.insertChildInList(parent.getNode(), cloned, parent.getInsertionPath());
                }
                //reference the newly created child
                itemAtPath = itemStructure.itemAtPath(path);
                if (itemAtPath == null) //something really wrong here...
                    throw new IllegalArgumentException("INTERNAL: failed to successfully clone child structure at:"+path);
            }

            if (itemAtPath instanceof ElementWrapper)
                assignElementWrapper((ElementWrapper)itemAtPath, object, path);
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
        Map<String, String> nameSpacesMap = new HashMap<>();
        nameSpacesMap.put("", "http://schemas.openehr.org/v1");
        xmlOptions.setLoadSubstituteNamespaces(nameSpacesMap);


        CompositionDocument cd = CompositionDocument.Factory.parse(inputStream, xmlOptions);
        COMPOSITION comp = cd.getComposition();
        XMLBinding binding = new XMLBinding();

        Object rmObj = binding.bindToRM(comp);
        //consistency test
        if (!(rmObj instanceof Composition))
            throw new IllegalArgumentException("Parsed object does not yield an RM composition");

        Composition importedComposition = (Composition)rmObj;

        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(importedComposition);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
        Gson gson = builder.setPrettyPrinting().create();
        String mapjson = gson.toJson(retmap);

        //create an actual RM composition
        this.entry = mapjson;
        this.composition = importedComposition;

        return (Composition)rmObj;
    }

    @Override
    public Composition importAsRM(Composition composition) throws Exception {

        //the templateId is found in the composition
        this.templateId = composition.getArchetypeDetails().getTemplateId().getValue();

        CompositionSerializer inspector = new CompositionSerializer(CompositionSerializer.WalkerOutputMode.PATH);
        Map<String, Object>retmap = inspector.process(composition);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
        Gson gson = builder.setPrettyPrinting().create();
        String mapjson = gson.toJson(retmap);

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
        return canonicalExporter(composition, prettyPrint);
    }

    public static final String SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String SCHEMA_OPENEHR_ORG_V1 = "http://schemas.openehr.org/v1";

    public static byte[] canonicalExporter(Composition composition, boolean prettyPrint){
        byte[] data = null;
        //generate an XML representation of the current composition
        if (composition == null){
            throw new IllegalArgumentException("Could not build composition from available data...");
        }

        try {
//            XStream xStream = new XStream();
//            String xml = xStream.toXML(composition); // this does not marshal consistently with XmlBeans...

            XMLBinding xmlBinding = new XMLBinding();
            Object object = xmlBinding.bindToXML(composition, true);

            if (!(object instanceof COMPOSITIONImpl))
                throw new IllegalArgumentException("Invalid binding of object, resulting in class:"+object.getClass());

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

            String xml = ((COMPOSITIONImpl)object).xmlText(xmlOptions);

//            String xml = ((COMPOSITIONImpl)object).xmlText();
            try {
                data = xml.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    protected MapInspector getMapInspector(String jsonData) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
        builder.registerTypeAdapter(DvDate.class, new DvDateAdapter());
        Gson gson = builder.setPrettyPrinting().create();

        Map<String, Object> retmap = gson.fromJson(jsonData, TreeMap.class);

        //traverse the tree
        MapInspector inspector = new MapInspector();
        inspector.inspect(retmap);
        return inspector;
    }

    public Composition buildCompositionFromJson(String jsonData) throws Exception {
        MapInspector inspector = getMapInspector(jsonData);

        //TODO: use a cache mechanism for generated composition
        //time measured is about 250 ms to generate the composition and 15ms to set the values...
        long start = System.nanoTime();
        Composition newComposition = this.generateNewComposition(); //will use the corresponding class instance generator
        long end = System.nanoTime();

        log.debug("generate composition [ms]:"+(end - start)/1000000);

        start = System.nanoTime();
        assignValuesFromStack(newComposition, (ArrayDeque) inspector.getStack());
        end = System.nanoTime();

        log.debug("set values [ms]:"+(end - start)/1000000);

        return newComposition;
    }

    @Override
    public void bindOtherContextFromJson(Composition composition, String jsonData) throws Exception {
        if (jsonData == null || composition == null)
            return;
        MapInspector inspector = getMapInspector(jsonData);
        assignValuesFromStack(composition.getContext().getOtherContext(), (ArrayDeque) inspector.getStack());
    }

    protected void storeCache(String id, Composition composition) throws Exception {
        byte[] bytes = RMDataSerializer.serializeRaw(composition);
        knowledge.cacheGenerated(id, bytes);
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

    public void setCompositionParameters(Map<SystemValue, Object> values){
        this.values = values;
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
            else
                throw new IllegalArgumentException("Could not handle name value of type:"+name);
        }
    }

}
