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

import com.ethercis.ehr.building.OptBinding;
import com.ethercis.ehr.building.RmBinding;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.collections.map.PredicatedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.*;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.history.Event;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.IntervalEvent;
import org.openehr.rm.datastructure.itemstructure.*;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.integration.GenericEntry;

import java.util.*;

/**
 * Sequential Event Processor for Composition.<p>
 * Takes an RM composition and serialize it as a Maps of Maps and Arrays with WrappedElements.
 * since some duplicate entries have been noticed, the node id contains type:name:archetype_id
 * @see ElementWrapper
 * @author Christian Chevalley
 *
 */
public class CompositionSerializer implements I_CompositionSerializer {


	public enum WalkerOutputMode {
		PATH,
		NAMED,
        EXPANDED,
		RAW
	}
	
	protected static Logger log = LogManager.getLogger(CompositionSerializer.class);

	private Map<String, Object> ctree;

	private String treeRootClass;
	private String treeRootArchetype;

	protected final WalkerOutputMode tag_mode; //default
    protected final boolean allElements; //default

//	private Gson gson = new Gson();

	public static final String TAG_META = "/meta";
	public static final String TAG_CONTENT = "/content";
	public static final String TAG_PROTOCOL = "/protocol";
	public static final String TAG_DATA = "/data";
	public static final String TAG_STATE = "/state";
	public static final String TAG_DESCRIPTION = "/description";
	public static final String TAG_TIME = "/time";
	public static final String TAG_WIDTH = "/width";
	public static final String TAG_MATH_FUNCTION = "/math_function";
	public static final String TAG_INSTRUCTION="/instruction";
	public static final String TAG_NARRATIVE = "/narrative";
	public static final String TAG_ITEMS="/items";
    public static final String TAG_OTHER_CONTEXT = "/context/other_context";
	public static final String TAG_ACTIVITIES="/activities";
	public static final String TAG_ACTIVITY="/activity";
	public static final String TAG_VALUE="/value";
	public static final String TAG_EVENTS="/events";
	public static final String TAG_ORIGIN="/origin";
	public static final String TAG_SUMMARY="/summary";
	public static final String TAG_TIMING="/timing";
	public static final String TAG_COMPOSITION="/composition";
	public static final String TAG_ENTRY="/entry";
	public static final String TAG_EVALUATION="/evaluation";
	public static final String TAG_OBSERVATION="/observation";
	public static final String TAG_ACTION="/action";
	public static final String TAG_ISM_TRANSITION="/ism_transition";
	public static final String TAG_CURRENT_STATE = "/current_state";
	public static final String TAG_CAREFLOW_STEP = "/careflow_step";
	public static final String TAG_TRANSITION = "/transition";
	public static final String TAG_WORKFLOW_ID = "/workflow_id";
	public static final String TAG_GUIDELINE_ID = "/guideline_id";
    public static final String TAG_OTHER_PARTICIPATIONS="/other_participations";
	public static final String TAG_PROVIDER="/provider"; //care entry provider
    public static final String TAG_UID="/uid";
	public static final String TAG_OTHER_DETAILS = "/other_details";
	public static final String TAG_INSTRUCTION_DETAILS="/instruction_details";
	public static final String TAG_ACTIVITY_ID = "/action_id";
	public static final String TAG_INSTRUCTION_ID = "/instruction_id";
    public static final String TAG_PATH = "/$PATH$";
    public static final String TAG_CLASS = "/$CLASS$";
    public static final String TAG_NAME =  "/name";
	public static final String TAG_DEFINING_CODE =  "/defining_code";
	public static final String INNER_CLASS_LIST = "$INNER_CLASS_LIST$";
	public static final String TAG_ACTION_ARCHETYPE_ID="/action_archetype_id";
	public static final String TAG_ARCHETYPE_NODE_ID="/archetype_node_id";

	protected CompositionSerializer(WalkerOutputMode mode) throws IllegalAccessException {
		this.allElements = false;
        this.tag_mode = mode;
//		initTags();
	}

	protected CompositionSerializer(boolean allElements) throws IllegalAccessException {
		this.allElements = allElements;
		this.tag_mode = WalkerOutputMode.PATH;
//		initTags();
	}

    protected CompositionSerializer(WalkerOutputMode mode, boolean allElements) throws IllegalAccessException {
        this.allElements = allElements;
        this.tag_mode = mode;
//		initTags();
    }


    protected CompositionSerializer() throws IllegalAccessException {
		this.allElements = false;
        this.tag_mode = WalkerOutputMode.PATH;
//		initTags();
	}
	
    protected ItemStack itemStack = new ItemStack();

//	private void initTags() throws IllegalAccessException {
//		TagSetter.setTagDefinition(this, TagSetter.DefinitionSet.PG_JSONB);
//	}
	/**
	 * to remain consistent regarding datastructure, we use a map which prevents duplicated keys... and throw
	 * an exception if one is detected...
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> newPathMap(){
		return MapUtils.predicatedMap(new TreeMap<String, Object>(), PredicateUtils.uniquePredicate(), null);
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> newMultiMap(){
		return MapUtils.multiValueMap(new HashMap<String, Object>());
	}

	protected Map<String, Object> mapName(DvText aName){
		Map<String, Object> nameMap = new HashMap<>();
		if (aName instanceof DvCodedText) {
			nameMap.put("defining_code", ((DvCodedText)(aName)).getDefiningCode());
		}
		nameMap.put("value", aName.getValue());
		return nameMap;
	}

	private Map<String, Object> mapName(DvCodedText aName){
		Map<String, Object> nameMap = new HashMap<>();
		nameMap.put("defining_code", aName.getDefiningCode());
		nameMap.put("value", aName.getValue());
		return nameMap;
	}

	private Map<String, Object> mapName(String aName){
		Map<String, Object> nameMap = new HashMap<>();
		nameMap.put("value", new DvText(aName));
		return nameMap;
	}

	private Map<String, List> nameAsValueList(Map map, Map nameValues){
		List<Map> nameListMap = new ArrayList<>();
		nameListMap.add(nameValues);
		map.put(TAG_NAME, nameListMap);
		return map;
	}
	/**
	 * put a key=value pair in a map and detects duplicates.
	 * @param map
	 * @param key
	 * @param addStructure
	 * @return
	 * @throws Exception
	 */
	protected Object putObject(Object node, Map<String, Object> map, String key, Object addStructure) throws Exception {
		//CHC: 160602
		if (addStructure == null) return null;
		if (addStructure instanceof Map && ((Map) addStructure).size() == 0)
				return  null;

//		Object retStructure = null;

		if (key.equals(TAG_NAME)){
//			List<Map> nameListMap = new ArrayList<>();
//			nameListMap.add((Map)addStructure);
//			map.put(TAG_NAME, nameListMap);
//			return map;
			return nameAsValueList(map, (Map)addStructure);
		}

		try {
			map.put(key, addStructure);
			//add explicit name
			if (node instanceof Locatable && map instanceof PredicatedMap && !map.containsKey(TAG_NAME)){
//				Map<String, Object> nameMap = new HashMap<>();
//				if (((Locatable) node).getName() instanceof DvCodedText) {
//					nameMap.put(TAG_DEFINING_CODE, ((DvCodedText)((Locatable) node).getName()).getDefiningCode());
//				}
//				nameMap.put(TAG_VALUE, ((Locatable) node).getName().getValue());
//				map.put(TAG_NAME, mapName(((Locatable) node).getName()));
				nameAsValueList(map, mapName(((Locatable) node).getName()));
			}

		} catch (IllegalArgumentException e) {
			log.error("Ignoring duplicate key in path detected:" + key + " path:" + itemStack.pathStackDump() + " Exception:" + e);
//			throw new Exception("duplicate key:"+key+", please fix the input structure");
		}

		return map;
	}
	
	protected String getNodeTag(String prefix, Locatable node, Object container) {
		
		switch (tag_mode) {
		case PATH: 
			if (node == null)
				return prefix;
			else {
				String path = prefix + "[" + node.getArchetypeNodeId() + "]";
				if (!container.getClass().equals(MultiValueMap.class) && (!(path.startsWith(CompositionSerializer.TAG_DESCRIPTION)))){
					if (path.contains("[openEHR-") || path.contains(CompositionSerializer.TAG_ACTIVITIES) || path.contains(CompositionSerializer.TAG_ITEMS) || path.contains(CompositionSerializer.TAG_EVENTS)) {
					//expand name in key
						String name = node.getName().getValue();

						if (name != null) {
							path = path.substring(0, path.lastIndexOf("]")) + " and name/value='" + name + "']";
//						path = path + " and name/value='" + name + "']";
						}
					}
//            	else
//                	log.warn("Ignoring entry/item name:"+name);
				}

				return path;
			}
		
		case NAMED:
        case EXPANDED:
		case RAW:
			if (prefix.equals(TAG_ORIGIN) || prefix.equals(TAG_TIME) || prefix.equals(TAG_TIMING) || (prefix.equals(TAG_EVENTS) && node == null))
						return "["+prefix.substring(1)+"]";
			else
				if (node == null)
					return "!!!INVALID NAMED for "+ prefix+" !!!"; //comes from encodeNodeAttribute...
				else {
                    /* ISSUE, the name can be a translation hence any query in the JSON structure will be impossible!
					String name = node.nodeName();
					*/
                    if (node instanceof ElementWrapper){
                        ElementWrapper elementWrapper = (ElementWrapper)node;
                        return elementWrapper.getAdaptedElement().getName().getValue();
                    }
                    else
                        return node.getArchetypeNodeId();
				}

            default:
				return "*INVALID MODE*";
		}
	}

	protected String extractNodeTag(String prefix, Locatable node, Object container) {

		switch (tag_mode) {
			case PATH:
				if (node == null)
					return prefix;
				else {
					String path = prefix + "[" + node.getArchetypeNodeId() + "]";
					if (!container.getClass().equals(MultiValueMap.class) && (!(path.startsWith(CompositionSerializer.TAG_DESCRIPTION)))){
						if (path.contains("[openEHR-") || path.contains(CompositionSerializer.TAG_ACTIVITIES) || path.contains(CompositionSerializer.TAG_ITEMS) || path.contains(CompositionSerializer.TAG_EVENTS)) {
							//expand name in key
							String name = node.getName().getValue();

							if (name != null) {
								path = path.substring(0, path.lastIndexOf("]")) + " and name/value='" + name + "']";
//						path = path + " and name/value='" + name + "']";
							}
						}
//            	else
//                	log.warn("Ignoring entry/item name:"+name);
					}

					return path;
				}

			case NAMED:
			case EXPANDED:
			case RAW:
				if (prefix.equals(TAG_ORIGIN) || prefix.equals(TAG_TIME) || prefix.equals(TAG_TIMING) || (prefix.equals(TAG_EVENTS) && node == null))
					return "["+prefix.substring(1)+"]";
				else
				if (node == null)
					return "!!!INVALID NAMED for "+ prefix+" !!!"; //comes from encodeNodeAttribute...
				else {
                    /* ISSUE, the name can be a translation hence any query in the JSON structure will be impossible!
					String name = node.nodeName();
					*/
					if (node instanceof ElementWrapper){
						ElementWrapper elementWrapper = (ElementWrapper)node;
						return elementWrapper.getAdaptedElement().getName().getValue();
					}
					else
						return node.getArchetypeNodeId();
				}

			default:
				return "*INVALID MODE*";
		}
	}

    protected void encodePathItem(Map<String, Object> map, String tag) throws Exception {
        switch (tag_mode) {
            case PATH:
                putObject(null, map, TAG_PATH, tag == null ? itemStack.pathStackDump() : itemStack.pathStackDump()+tag);
                break;
            case NAMED:
                putObject(null, map, TAG_PATH, tag == null ? itemStack.namedStackDump() : itemStack.namedStackDump()+tag.substring(1));
                break;
            case EXPANDED:
                putObject(null, map, TAG_PATH, tag == null ? itemStack.expandedStackDump() : itemStack.expandedStackDump()+tag.substring(1));
                break;
			case RAW:
//				putObject(map, TAG_PATH, tag == null ? itemStack.expandedStackDump() : itemStack.expandedStackDump()+tag.substring(1));
				break;
            default:
                throw new IllegalArgumentException("Invalid tagging mode!");
        }

    }

    /**
     * encode a single value for example activity timing
     * @param map
     * @param tag
     * @param value
     * @throws Exception
     */
	private void encodeNodeAttribute(Map<String, Object> map, String tag, Object value, DvText name) throws Exception {
		Map<String, Object> valuemap = newPathMap();
		//CHC: 160317 make name optional ex: timing
		if (name != null) {
			putObject(value, valuemap, TAG_NAME, mapName(name));
		}


		//CHC: 160317 make value optional ex. simple name for activity
		if (value != null) {
			putObject(value, valuemap, TAG_CLASS, value.getClass().getSimpleName());
			putObject(value, valuemap, TAG_VALUE, value);
			encodePathItem(valuemap, tag);
			putObject(value, map, tag, valuemap);
		}

	}

	private void encodeNodeMetaData(Map<String, Object> map, Locatable locatable) throws Exception {
		//do nothing (side effects)
//		StringBuffer stringBuffer = new StringBuffer();
//
//		for (int i = 0; i < values.length; i++){
//			if (values[i] instanceof  Locatable) {
//				Locatable locatable = (Locatable)values[i];
//				stringBuffer.append(locatable.getArchetypeNodeId());
//				stringBuffer.append("::");
//				stringBuffer.append(locatable.getName().getValue());
//			}
//			else
//				stringBuffer.append(values[i]);
//			if (i < values.length -1 )
//				stringBuffer.append("|");
//		}
//
//		putObject(map, TAG_META, stringBuffer.toString());

//		Map<String, Object> metaMap = new HashMap<>();
//
//		metaMap.put(TAG_META, new HashMap<String, Object>().put(TAG_NAME, locatable.getName()));
//		//				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, observation.getWorkflowId(), observation.getName().getValue());
//		putObject(map, TAG_META, )

//		putObject(null, map, TAG_NAME, mapName(locatable.getName()));

	}

	private Map<String, Object> objectAttributes(RMObject object, String name) throws Exception {
		Map<String, Object> valuemap = newPathMap();
		putObject(object, valuemap, TAG_NAME, mapName(name));
		putObject(object, valuemap, TAG_CLASS, object.getClass().getSimpleName());

		//assign the actual object to the value (instead of its field equivalent...)
		putObject(object, valuemap, TAG_VALUE, object);

		return valuemap;
	}

	private Map<String, Object> mapRmObjectAttributes(RMObject object, String name) throws Exception {
		Map<String, Object> valuemap = objectAttributes(object, name);
        encodePathItem(valuemap, null);
		return valuemap;
	}

	private Map<String, Object> mapRmObjectAttributes(RMObject object, String name, String tag) throws Exception {
		Map<String, Object> valuemap = objectAttributes(object, name);
		encodePathItem(valuemap, tag);
		return valuemap;
	}

	/**
	 * main entry method, process a composition.
	 * @param composition
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> process(Composition composition) throws Exception {
		ctree = newPathMap();
		if (composition == null  /* CHC 170426: no content is legit... || composition.getContent() == null || composition.getContent().isEmpty() */)
			return null;
		
//		pushPathStack(TAG_COMPOSITION+"["+composition.getArchetypeNodeId()+"]");
		Map<String, Object>ltree = newMultiMap();

		if (composition.getContent() != null && !composition.getContent().isEmpty()) {
			for (ContentItem item : composition.getContent()) {
				putObject(item, ltree, getNodeTag(TAG_CONTENT, item, ltree), traverse(item, TAG_CONTENT));
			}
		}
		log.debug(ltree.toString());

        itemStack.popStacks();

		putObject(composition, ctree, getNodeTag(TAG_COMPOSITION, composition, ctree), ltree);
		//store locally the tree root
		if (ctree.size() > 0) {
			String path = (String) ctree.keySet().toArray()[0];
			treeRootArchetype = ItemStack.normalizeLabel(path);
			treeRootClass = ItemStack.getLabelType(path);
		}

		return ctree;
	}

	@Override
    public Map<String, Object> processItem(Locatable locatable) throws Exception {
        ctree = newPathMap();

        if (locatable instanceof Item)
            putObject(locatable, ctree, getNodeTag(TAG_OTHER_CONTEXT, locatable, ctree), traverse((Item)locatable, TAG_ITEMS));
        else if (locatable instanceof ItemStructure)
            putObject(locatable, ctree, getNodeTag(TAG_OTHER_CONTEXT, locatable, ctree), traverse((ItemStructure)locatable, TAG_ITEMS));
        else
            throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

        return ctree;
    }

	@Override
	public Map<String, Object> processItem(String tag, Locatable locatable) throws Exception {
		ctree = newPathMap();

		if (locatable instanceof Item)
//			putObject(ctree, getNodeTag(tag, locatable, ctree.getClass()), traverse((Item)locatable, TAG_ITEMS));
			putObject(locatable, ctree, tag, traverse((Item)locatable, TAG_ITEMS));
		else if (locatable instanceof ItemStructure)
//			putObject(ctree, getNodeTag(tag, locatable, ctree.getClass()), traverse((ItemStructure)locatable, TAG_ITEMS));
			putObject(locatable, ctree, tag, traverse((ItemStructure)locatable, TAG_ITEMS));
		else
			throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

		return ctree;
	}

	/**
	 * main entry method, invalidateContent an arbitrary entry (evaluation, observation, instruction, action)
	 * @param entry
	 * @param entryTag
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Entry entry, String entryTag) throws Exception {
		ctree = newPathMap();

        itemStack.pushStacks(entryTag + "[" + entry.getArchetypeNodeId() + "]", entry.getName().getValue());
//		pushPathStack(entryTag + "[" + entry.getArchetypeNodeId() + "]");
//        pushNamedStack(entry.getName().getValue());

		putObject(entry, ctree, getNodeTag(entryTag, entry, ctree), traverse(entry, TAG_DATA));
		log.debug(ctree.toString());

        itemStack.popStacks();
		return ctree;

	}
	
	/**
	 * convenience method for processing an Evaluation
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Evaluation entry) throws Exception {
		if (entry == null || entry.getData() == null)
			return null;

		return process(entry, TAG_EVALUATION);
	}
	
	/**
	 * convenience method for processing an Observation
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Observation entry) throws Exception {
		if (entry == null || entry.getData() == null)
			return null;

		return process(entry, TAG_OBSERVATION);
	}
	
	/**
	 * convenience method for processing an Instruction
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Instruction entry) throws Exception {
		if (entry == null || entry.getActivities() == null)
			return null;

		return process(entry, TAG_INSTRUCTION);
	}

	/**
	 * convenience method for processing an Instruction
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Action entry) throws Exception {
		if (entry == null || entry.getDescription() == null)
			return null;

		return process(entry, TAG_ACTION);
	}

	/**
	 * convenience method for processing an Activity
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> process(Activity entry) throws Exception {
		if (entry == null || entry.getDescription() == null)
			return null;

		ctree = newPathMap();

        itemStack.pushStacks(entry + "[" + entry.getArchetypeNodeId() + "]", entry.getName().getValue());

		putObject(entry, ctree, getNodeTag(TAG_ACTIVITIES, entry, ctree), traverse(entry, TAG_DATA));
		log.debug(ctree.toString());

        itemStack.popStacks();
		return ctree;
	}		
	
//	public void setMode(WalkerOutputMode mode) {
//		this.tag_mode = mode;
//	}


	/**
	 * domain level: Observation, evaluation, instruction, action. section, admin etc.
	 * @param item
	 * @param tag
	 * @throws Exception
	 */
	private Map<String, Object> traverse(ContentItem item, String tag) throws Exception {

		Map<String, Object> retmap = null;
		
		if (item == null){
			return null;
		}

		log.debug("traverse element of class:"+item.getClass()+", tag:"+tag+", nodeid:"+item.getArchetypeNodeId());
        itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", item.getName().getValue());
//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());
		
		if (item instanceof Observation) {
			Observation observation = (Observation) item;
			Map<String, Object>ltree = newPathMap();

			//CHC: 160531 add explicit name
			if (observation.getName() != null) encodeNodeMetaData(ltree, observation);

			if (observation.getProtocol() != null)
				putObject(observation, ltree, getNodeTag(TAG_PROTOCOL,observation.getProtocol(), ltree), traverse(observation.getProtocol(),TAG_PROTOCOL));
			
			if (observation.getData() != null)
				putObject(observation, ltree, getNodeTag(TAG_DATA, observation.getData(), ltree), traverse(observation.getData(), TAG_DATA));
			
			if (observation.getState() != null)
				putObject(observation, ltree, getNodeTag(TAG_STATE, observation.getState(), ltree), traverse(observation.getState(), TAG_STATE));

			if (observation.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, observation.getWorkflowId(), observation.getName());

			if (observation.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, observation.getGuidelineId(), observation.getName());

			if (observation.getUid() != null)
				encodeNodeAttribute(ltree, TAG_UID, observation.getUid(), observation.getName());

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof Evaluation) {
			Evaluation evaluation = (Evaluation) item;
			Map<String, Object>ltree = newPathMap();

			if (evaluation.getProtocol() != null)
				putObject(evaluation, ltree, getNodeTag(TAG_PROTOCOL,evaluation.getProtocol(), ltree), traverse(evaluation.getProtocol(),TAG_PROTOCOL));
			
			if (evaluation.getData() != null)
				putObject(evaluation, ltree, getNodeTag(TAG_DATA, evaluation.getData(), ltree), traverse(evaluation.getData(), TAG_DATA));

			if (evaluation.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, evaluation.getWorkflowId(), evaluation.getName());

			if (evaluation.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, evaluation.getGuidelineId(), evaluation.getName());

			if (evaluation.getUid() != null)
				encodeNodeAttribute(ltree, TAG_UID, evaluation.getUid(), evaluation.getName());

			//CHC: 160531 add explicit name
			if (evaluation.getName() != null) encodeNodeMetaData(ltree, evaluation);

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;
			
		} else if (item instanceof Instruction) {
//			Map<String, Object>ltree = newMultiMap();
            Map<String, Object>ltree = newPathMap();

			Instruction instruction = (Instruction) item;

			if (instruction.getProtocol() != null)
				putObject(instruction, ltree, getNodeTag(TAG_PROTOCOL, ((Instruction) item).getProtocol(), ltree), traverse(instruction.getProtocol(), TAG_PROTOCOL));
			if (instruction.getNarrative() != null && (allElements || !instruction.getNarrative().equals(new DvText(RmBinding.DEFAULT_NARRATIVE))))
				encodeNodeAttribute(ltree, TAG_NARRATIVE, instruction.getNarrative(), instruction.getName());
			if (instruction.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, instruction.getWorkflowId(), instruction.getName());
			if (instruction.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, instruction.getGuidelineId(), instruction.getName());
			if (instruction.getUid() != null)
				encodeNodeAttribute(ltree, TAG_UID, instruction.getUid(), new DvText(instruction.getUid().getValue()));

			if (instruction.getActivities() != null) {

//				for (Activity act : instruction.getActivities()) {
//                    itemStack.pushStacks(TAG_ACTIVITIES + "[" + act.getArchetypeNodeId() + "]", act.getName().getValue());
//					putObject(ltree, getNodeTag(TAG_ACTIVITIES, act, ltree.getClass()), traverse(act, TAG_DESCRIPTION));
//				}
				Map<String, Object> activities = newMultiMap();
				for (Activity activity : instruction.getActivities()) {
					itemStack.pushStacks(TAG_ACTIVITIES + "[" + activity.getArchetypeNodeId() + "]", activity.getName().getValue());
//					putObject(activities, getNodeTag(TAG_ACTIVITIES, act, activities.getClass()), traverse(act, TAG_DESCRIPTION));
					putObject(activity, activities, getNodeTag(TAG_ACTIVITIES, activity, activities), traverse(activity, TAG_DESCRIPTION));
					itemStack.popStacks();
				}

				putObject(instruction, ltree, TAG_ACTIVITIES, activities);

			}
			//CHC: 160531 add explicit name
			if (instruction.getName() != null) encodeNodeMetaData(ltree, instruction);

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof Action) {
			Map<String, Object>ltree = newPathMap();

			Action action = (Action) item;
			boolean hasActiveContent = false;

			if (action.getProtocol() != null) {
				Object protocol = traverse(action.getProtocol(), TAG_PROTOCOL);
				if (protocol != null) {
					putObject(action, ltree, getNodeTag(TAG_PROTOCOL, action.getProtocol(), ltree), traverse(action.getProtocol(), TAG_PROTOCOL));
					hasActiveContent = true;
				}
			}
			
			if (action.getDescription() != null) {
				Object description = traverse(action.getDescription(), TAG_DESCRIPTION);
				if (description != null) {
					putObject(action, ltree, getNodeTag(TAG_DESCRIPTION, action.getDescription(), ltree), traverse(action.getDescription(), TAG_DESCRIPTION));
					hasActiveContent = true;
				}
			}
			
			if (action.getInstructionDetails() != null) {
				putObject(action, ltree, getNodeTag(TAG_INSTRUCTION, item, ltree), traverse(action.getInstructionDetails().getWfDetails(), TAG_INSTRUCTION));
//				hasActiveContent = true;
			}

			if (action.getWorkflowId() != null) {
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, action.getWorkflowId(), action.getName());
//				hasActiveContent = true;
			}

			if (action.getGuidelineId() != null) {
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, action.getGuidelineId(), action.getName());
//				hasActiveContent = true;
			}

			if (action.getTime() != null){
				if (allElements || !action.getTime().equals(new DvDateTime(RmBinding.DEFAULT_DATE_TIME))) {
					encodeNodeAttribute(ltree, TAG_TIME, action.getTime(), action.getName());
//					hasActiveContent = true;
				}
			}

			if (action.getInstructionDetails() != null){
				InstructionDetails instructionDetails = action.getInstructionDetails();
				encodeNodeAttribute(ltree, TAG_INSTRUCTION_DETAILS+TAG_ACTIVITY_ID, instructionDetails.getActivityId(), action.getName());
				encodeNodeAttribute(ltree, TAG_INSTRUCTION_DETAILS+TAG_INSTRUCTION_ID, instructionDetails.getInstructionId(), action.getName());
//				hasActiveContent = true;
			}


			if (action.getIsmTransition() != null){
				ISMTransition ismTransition = action.getIsmTransition();
				if (ismTransition != null && ismTransition.getCareflowStep() != null && (allElements || !ismTransition.getCareflowStep().getValue().equals("DUMMY"))) {
					if (ismTransition.getCurrentState() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_CURRENT_STATE, ismTransition.getCurrentState(), action.getName());
					if (ismTransition.getTransition() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_TRANSITION, ismTransition.getTransition(), action.getName());
					if (ismTransition.getCareflowStep() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_CAREFLOW_STEP, ismTransition.getCareflowStep(), action.getName());
				}
			}

			//CHC: 160531 add explicit name
			if (action.getName() != null) encodeNodeMetaData(ltree, action);

			if (hasActiveContent) //ism_transition is always set (comes from the template initially)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof Section) {

			Map<String, Object>ltree = newMultiMap();

			for (ContentItem contentItem : ((Section) item).getItems()) {
				putObject(contentItem, ltree, getNodeTag(TAG_ITEMS, contentItem, ltree), traverse(contentItem, TAG_ITEMS));
			}
			//CHC: 160531 add explicit name
			Section section = (Section)item;
			if (section.getName() != null) encodeNodeMetaData(ltree, section);

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof AdminEntry) {
			AdminEntry adminEntry = (AdminEntry) item;
			Map<String, Object>ltree = newPathMap();

			//CHC: 160531 add explicit name
			if (adminEntry.getName() != null) encodeNodeMetaData(ltree, adminEntry);

			if (adminEntry.getData() != null)
				putObject(adminEntry, ltree, getNodeTag(TAG_DATA, adminEntry.getData(), ltree), traverse(adminEntry.getData(), TAG_DATA));

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof GenericEntry) {
			Map<String, Object>ltree = newPathMap();

			GenericEntry genericEntry = (GenericEntry)item;
			//CHC: 160531 add explicit name
			if (genericEntry.getName() != null) encodeNodeMetaData(ltree, genericEntry);

			putObject(genericEntry, ltree, getNodeTag(TAG_DATA, genericEntry.getData(), ltree), traverse(genericEntry.getData(), TAG_DATA));

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else {
			log.warn("This item is not handled!"+item.nodeName());
		}

        //add complementary attributes

		if (item instanceof Entry){
			putEntryAttributes(retmap, (Entry) item);
		}

        itemStack.popStacks();
		return retmap;

	}

	private void putEntryAttributes(Map<String, Object> map, Entry item) throws Exception {
		List<Participation> participations = item.getOtherParticipations();

		if (participations != null && !participations.isEmpty()){
			List<Object> sublist = new ArrayList<>();
			for (Participation participation: participations) {
				sublist.add(mapRmObjectAttributes(participation, "other participation"));
//				subtree.put("/participation", participation.getFunction().getValue());
//				subtree.put("mode", participation.getMode().getValue());
//				map.put(TAG_OTHER_PARTICIPATIONS, participation);
			}
			map.put(TAG_OTHER_PARTICIPATIONS, sublist);
		}

		if (item.getProvider() != null){
			PartyIdentified provider = (PartyIdentified)item.getProvider();
			map.put(TAG_PROVIDER, mapRmObjectAttributes(provider, provider.getName(), TAG_PROVIDER));
		}

//        if (item instanceof Instruction)
//		if (item.getUid() != null) {
//			map.put(TAG_UID, mapRmObjectAttributes(item.getUid(), "uid"));
//		}
	}

	private Map<String, Object> traverse(Activity activity, String tag) throws Exception{
		if (activity == null)
			return null;

		log.debug("traverse activity:"+activity);

		Map<String, Object>ltree = newPathMap();

        if (activity.getTiming() != null) {
			//CHC: 160317 do not pass a name for time
			if (allElements || !activity.getTiming().equals(new DvParsable(RmBinding.DEFAULT_TIMING_SCHEME, RmBinding.DEFAULT_TIMING_FORMALISM)))
            	encodeNodeAttribute(ltree, TAG_TIMING, activity.getTiming(), null);
        }

		//CHC: 160317 add explicit name for activity
		encodeNodeAttribute(ltree, TAG_NAME, null, activity.getName());

        itemStack.pushStacks(tag + "[" + activity.getDescription().getArchetypeNodeId() + "]", activity.getDescription().getName().getValue());
//        pushPathStack(tag + "[" + act.getDescription().getArchetypeNodeId() + "]");
//        pushNamedStack(act.getName().getValue());

		log.debug(itemStack.pathStackDump()+TAG_DESCRIPTION+"["+activity.getArchetypeNodeId()+"]="+activity.getDescription().toString());
		putObject(activity, ltree, getNodeTag(TAG_DESCRIPTION, activity.getDescription(), ltree), traverse(activity.getDescription(), null)); //don't add a /data in path for description (don't ask me why...)


		if (activity.getActionArchetypeId() != null) putObject(activity, ltree, TAG_ACTION_ARCHETYPE_ID, activity.getActionArchetypeId().trim());

        itemStack.popStacks();
		return ltree;
	}
	
	
	/**
	 * History level in composition
	 * @param item
	 * @param tag
	 * @throws Exception
	 */
	private Map<String, Object> traverse(History<?> item, String tag) throws Exception {
		if (item == null){
			return null;
		}

		log.debug("traverse history:"+item);

        itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", item.getName().getValue());
//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());

		Map<String, Object>ltree = newPathMap();

		//CHC: 160531 add explicit name
		History history = (History)item;
		if (history.getName() != null) encodeNodeMetaData(ltree, history);
		
		log.debug(itemStack.pathStackDump()+TAG_ORIGIN+"["+item.getArchetypeNodeId()+"]="+item.getOrigin().toString());
		
		if (item.getOrigin() != null) {
			if (allElements || !item.getOrigin().equals(new DvDateTime(OptBinding.DEFAULT_DATE_TIME)))
				encodeNodeAttribute(ltree, TAG_ORIGIN, item.getOrigin(), item.getName());
		}

		if (item.getSummary() != null)
			putObject(history, ltree, getNodeTag(TAG_SUMMARY, item, ltree), traverse(item.getSummary(), TAG_SUMMARY));
		
		if (item.getEvents() != null) {

			Map<String, Object>eventtree = newMultiMap();
			
			for (Event<?> event : item.getEvents()) {
                itemStack.pushStacks(TAG_EVENTS + "[" + event.getArchetypeNodeId() + "]", event.getName().getValue());
//				pushPathStack(TAG_EVENTS + "[" + event.getArchetypeNodeId() + "]");
//                pushNamedStack(event.getName().getValue());

				Map<String, Object> subtree = newPathMap();
				log.debug(itemStack.pathStackDump()+TAG_TIME+"["+event.getArchetypeNodeId()+"]="+event.getTime());

				if (event instanceof IntervalEvent){
					IntervalEvent intervalEvent = (IntervalEvent)event;
					if (intervalEvent.getWidth() != null)
						encodeNodeAttribute(subtree, TAG_WIDTH, intervalEvent.getWidth(), event.getName());
					if (intervalEvent.getMathFunction() != null)
						encodeNodeAttribute(subtree, TAG_MATH_FUNCTION, intervalEvent.getMathFunction(), event.getName());
//					if (intervalEvent.getSampleCount() != null)
//						encodeNodeAttribute(subtree, TAG_MATH_FUNCTION, intervalEvent.getMathFunction(), event.getName().getValue());
				}


				if (event.getTime() != null) {
					if (!allElements || event.getTime().equals(new DvDateTime(OptBinding.DEFAULT_DATE_TIME)))
						encodeNodeAttribute(subtree, TAG_TIME, event.getTime(), event.getName());
				}
				if (event.getData() != null)
					putObject(event, subtree, getNodeTag(TAG_DATA, event.getData(), subtree), traverse(event.getData(), TAG_DATA));
				if (event.getState() != null)
					putObject(event, subtree, getNodeTag(TAG_STATE, event.getState(), subtree), traverse(event.getState(), TAG_STATE));

                itemStack.popStacks();
				putObject(event, eventtree, getNodeTag(TAG_EVENTS, event, eventtree), subtree);
			}

			putObject(history, ltree, getNodeTag(TAG_EVENTS, null, ltree), eventtree);
		}

        itemStack.popStacks();
		return ltree;

	}
	
	/**
	 * identify if the entry is a value singleton. If so, compact the entry to be "KEY/Value=entry"
	 * if not, use the usual convention of hash of hash...
	 * @param target
	 * @throws Exception
	 */
	protected void compactEntry(Object node, Map<String, Object>target, String key, Map<String, Object>entry) throws Exception{
        //if entry is null, ignore, the dirty bit is not set...
        if (entry != null) {
            if (entry.keySet().size() == 1 && entry.get(TAG_VALUE) != null) {
                Object o = entry.get(TAG_VALUE);
                // TAG_VALUE is not required in the properties map representation
                putObject(null, target, key, o);
            } else
                putObject(null, target, key, entry); //unchanged and uncompacted
        }
	}

	/**
	 * ItemStructure: single, tree or table
	 * @param item
	 * @param uppertag
	 * @throws Exception
	 */
	private Map<String, Object> traverse(ItemStructure item, String uppertag) throws Exception {

		Map<String, Object> retmap = null;

		log.debug("traverse itemstructure:"+item);
		
		if (item == null){
			return null;
		}


        if (uppertag != null) {
            itemStack.pushStacks(uppertag + "[" + item.getArchetypeNodeId() + "]", item.getName().getValue());
//            pushPathStack(uppertag + "[" + item.getArchetypeNodeId() + "]");
//            pushNamedStack(item.getName().getValue());
        }
		
		if (item instanceof ItemSingle) {
			Map<String, Object>ltree = newPathMap();

			ItemSingle itemSingle = (ItemSingle)item;
			//CHC: 160531 add explicit name
//			if (itemSingle.getName() != null) encodeNodeMetaData(ltree, itemSingle);
//			if (itemSingle.getName() != null) putObject(null, ltree, TAG_NAME, mapName(itemSingle.getName()));

			if (itemSingle.getItem()!=null){
				compactEntry(itemSingle, ltree, getNodeTag(TAG_ITEMS, itemSingle, ltree), traverse(itemSingle.getItem(), TAG_ITEMS));
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;
		} else if (item instanceof ItemList) {
			Map<String, Object>ltree = newMultiMap();

			ItemList list = (ItemList) item;

			//CHC: 160531 add explicit name
//			if (list.getName() != null) encodeNodeMetaData(ltree, list);
//			if (list.getName() != null) putObject(null, ltree, TAG_NAME, mapName(list.getName()));

			if (list.getItems() != null) {

				for (Item listItem : list.getItems()) {
					if (ltree.containsKey(extractNodeTag(TAG_ITEMS, item, ltree)))
						log.warn("ItemList: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(listItem, ltree, getNodeTag(TAG_ITEMS, (Locatable)listItem, ltree), traverse(listItem, TAG_ITEMS));
				}
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof ItemTree) {
//CHC:160317			Map<String, Object>ltree = newPathMap();
			Map<String, Object>ltree = newMultiMap();

			ItemTree tree = (ItemTree) item;

			//CHC: 160531 add explicit name
//			if (tree.getName() != null) encodeNodeMetaData(ltree, tree);
//			if (tree.getName() != null) putObject(null, ltree, TAG_NAME, mapName(tree.getName()));

			if (tree.getItems() != null) {

				for (Item subItem : tree.getItems()) {
					if (ltree.containsKey(extractNodeTag(TAG_ITEMS, item, ltree)))
						log.warn("ItemTree: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(subItem, ltree, getNodeTag(TAG_ITEMS, subItem, ltree), traverse(subItem, TAG_ITEMS));
				}
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof ItemTable) {
//CHC:160317			Map<String, Object>ltree = newPathMap();
			Map<String, Object>ltree = newMultiMap();

			ItemTable table = (ItemTable) item;

			//CHC: 160531 add explicit name
//			if (table.getName() != null) encodeNodeMetaData(ltree, table);
			if (table.getName() != null) putObject(null, ltree, TAG_NAME, mapName(table.getName()));

			if (table.getRows() != null) {

				for (Item subItem : table.getRows()) {
					if (ltree.containsKey(getNodeTag(TAG_ITEMS, item, ltree)))
						log.warn("ItemTable: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(subItem, ltree, getNodeTag(TAG_ITEMS, subItem, ltree), traverse(subItem, TAG_ITEMS));
				}
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		}

		if (uppertag != null) itemStack.popStacks();

		return retmap;

	}

	/**
	 * extrapolate composite class name such as DvInterval<DvCount>
	 * @param dataValue
	 * @return
	 */
	protected String getCompositeClassName(DataValue dataValue){
		String classname = dataValue.getClass().getSimpleName();

		switch (classname){
			case "DvInterval":
				//get the classname of lower/upper
				DvInterval interval = (DvInterval)dataValue;
				String lowerClassName = interval.getLower().getClass().getSimpleName();
				String upperClassName = interval.getUpper().getClass().getSimpleName();

				if (!lowerClassName.equals(upperClassName))
					throw new IllegalArgumentException("Lower and Upper classnames do not match:"+lowerClassName+" vs."+upperClassName);

				return classname+"<"+lowerClassName+">";
			default:
				return classname;
		}
	}

    protected Map<String, Object> setElementAttributesMap(Element element) throws Exception {
        Map<String, Object>ltree = newPathMap();

        if (element != null && element.getValue() != null && !element.getValue().toString().isEmpty()){
            log.debug(itemStack.pathStackDump()+"="+ element.getValue());
            Map<String, Object> valuemap = newPathMap();
            //VBeanUtil.setValueMap(valuemap, element.getValue());
            putObject(element, valuemap, TAG_NAME, mapName(element.getName()));

//			if (element.getName() instanceof DvCodedText) {
//				DvCodedText dvCodedText = (DvCodedText)element.getName();
//				if (dvCodedText.getDefiningCode() != null)
//					putObject(element, valuemap, TAG_DEFINING_CODE, dvCodedText.getDefiningCode());
//			}

			putObject(element, valuemap, TAG_CLASS, getCompositeClassName(element.getValue()));
//            putObject(valuemap, TAG_CLASS, element.getValue().getClass().getSimpleName());
            //assign the actual object to the value (instead of its field equivalent...)
            putObject(element, valuemap, TAG_VALUE, element.getValue());
//
            encodePathItem(valuemap, null);
//            if (tag_mode == WalkerOutputMode.PATH) {
//                putObject(valuemap, TAG_PATH, elementStack.pathStackDump());
//            }

            ltree.put(TAG_VALUE, valuemap);
        }
        else
            throw new IllegalArgumentException("Invalid element detected in map");

        return ltree;
    }

	/**
	 * Element level, normally cannot go deeper...
	 * @param item
	 * @param tag
	 * @throws Exception
	 */
	protected Map<String, Object> traverse(Item item, String tag) throws Exception {
		Map<String, Object> retmap = null;

		log.debug("traverse item:"+item);

		if (item == null){
			return null;
		}


//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());

        //for compatibility purpose, normally only ElementWrapper should be passed
		if (item instanceof Element) {
			itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", null);
			retmap = setElementAttributesMap((Element) item);
			itemStack.popStacks();
		} else if (item instanceof ElementWrapper){
            if (allElements || ((ElementWrapper)item).dirtyBitSet()) {
				//TODO: add coded name item.getName().getValue()
				itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", tag.equals(TAG_ITEMS) ? item.getName().getValue() : null);
				retmap = setElementAttributesMap(((ElementWrapper) item).getAdaptedElement());
				itemStack.popStacks();
			}
            else
                log.debug("Ignoring unchanged element:"+item.toString());
        }

        else if (item instanceof Cluster) {
			Map<String, Object>ltree = newMultiMap();
//			Map<String, Object>ltree = new TreeMap<>();
			itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", item.getName().getValue());

			Cluster cluster = (Cluster) item;
			boolean hasContent = false;

			//CHC: 160531 add explicit name
//			if (c.getName() != null) encodeNodeMetaData(ltree, item);

			if (cluster.getItems() != null) {

				//CHC:160914: fixed issue with cluster encoding as items (generated /value {/name... /value... /$PATH$... $CLASS$})
				//this caused inconsistencies when running AQL queries
				for (Item clusterItem : cluster.getItems()) {
//					compactEntry(clusterItem, ltree, getNodeTag(TAG_ITEMS, clusterItem, ltree), traverse(clusterItem, TAG_ITEMS));
//					putObject(ltree, getNodeTag(TAG_ITEMS, clusterItem, ltree), traverse(clusterItem, TAG_ITEMS));
					Object clusterItems = traverse(clusterItem, TAG_ITEMS);
					if (clusterItems != null) {
						if (clusterItems instanceof Map && ((Map)clusterItems).containsKey(TAG_VALUE)) {
							ltree.put(getNodeTag(TAG_ITEMS, clusterItem, ltree), ((Map)clusterItems).get(TAG_VALUE));
						}
						else {
							ltree.put(getNodeTag(TAG_ITEMS, clusterItem, ltree), clusterItems);
						}
					}
                }
				if (ltree.size() > 0) hasContent = true;

				if (cluster.getName() != null) ltree.put(TAG_NAME, mapName(item.getName()));

			}
			if (hasContent)
				retmap = ltree;
			else
				retmap = null;

			itemStack.popStacks();
		}

		return retmap;
	}

	@Override
	public Map<String, String> getLtreeMap(){
		return itemStack.getLtreeMap();
	}

	@Override
	public String getTreeRootArchetype() {
		return treeRootArchetype;
	}

	@Override
	public String getTreeRootClass() {
		return treeRootClass;
	}

	/**
	 * Convenience method, encode a locatable to be persisted in a JSON data entry
	 * @param locatable
	 * @return
	 * @throws Exception
	 */
	@Override
	public String dbEncode(Locatable locatable) throws Exception {
		Map<String, Object> stringObjectMap = processItem(locatable);
		GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
		Gson gson = builder.setPrettyPrinting().create();
		return gson.toJson(stringObjectMap);
	}

	@Override
	public String dbEncode(Composition composition) throws Exception {
		Map<String, Object> stringObjectMap = process(composition);
		GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
		Gson gson = builder.setPrettyPrinting().create();
		return gson.toJson(stringObjectMap);
	}

	@Override
	public String dbEncode(String tag, Locatable locatable) throws Exception {
		Map<String, Object> stringObjectMap = processItem(tag, locatable);
		GsonBuilder builder = EncodeUtil.getGsonBuilderInstance();
		Gson gson = builder.setPrettyPrinting().create();
		return gson.toJson(stringObjectMap);
	}

	@Override
	public String dbEncodeContent(String tag, Locatable locatable) throws Exception{
		return null;
	}

	@Override
	public Map<String, Object> dbEncodeAsMap(String tag, Locatable locatable) throws Exception {
		return processItem(tag, locatable);
	}

	@Override
	public Object dbEncodeContentAsMap(String tag, Locatable locatable) throws Exception {
		return null;
	}
}
