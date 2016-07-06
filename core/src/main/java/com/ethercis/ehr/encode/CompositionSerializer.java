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
import org.apache.log4j.Logger;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
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
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.integration.GenericEntry;
import org.openehr.schemas.v1.LOCATABLE;

import java.util.*;

/**
 * Sequential Event Processor for Composition.<p>
 * Takes an RM composition and serialize it as a Maps of Maps and Arrays with WrappedElements.
 * since some duplicate entries have been noticed, the node id contains type:name:archetype_id
 * @see ElementWrapper
 * @author Christian Chevalley
 *
 */
public class CompositionSerializer {



	public enum WalkerOutputMode {
		PATH,
		NAMED,
        EXPANDED
	}
	
	private static Logger log = Logger.getLogger(CompositionSerializer.class);

	private Map<String, Object> ctree;

	private String treeRootClass;
	private String treeRootArchetype;

	private final WalkerOutputMode tag_mode; //default
    private final boolean allElements; //default

//	private Gson gson = new Gson();

	public static final String TAG_META = "/meta";
	public static final String TAG_CONTENT = "/content";
	static final String TAG_PROTOCOL = "/protocol";
	static final String TAG_DATA = "/data";
	static final String TAG_STATE = "/state";
	static final String TAG_DESCRIPTION = "/description";
	public static final String TAG_TIME = "/time";
	public static final String TAG_WIDTH = "/width";
	public static final String TAG_MATH_FUNCTION = "/math_function";
	static final String TAG_INSTRUCTION="/instruction";
	public static final String TAG_NARRATIVE = "/narrative";
	public static final String TAG_ITEMS="/items";
    static final String TAG_OTHER_CONTEXT = "/context/other_context";
	public static final String TAG_ACTIVITIES="/activities";
	public static final String TAG_ACTIVITY="/activity";
	public static final String TAG_VALUE="/value";
	public static final String TAG_EVENTS="/events";
	public static final String TAG_ORIGIN="/origin";
	static final String TAG_SUMMARY="/summary";
	public static final String TAG_TIMING="/timing";
	static final String TAG_COMPOSITION="/composition";
	static final String TAG_ENTRY="/entry";
	static final String TAG_EVALUATION="/evaluation";
	static final String TAG_OBSERVATION="/observation";
	static final String TAG_ACTION="/action";
	public static final String TAG_ISM_TRANSITION="/ism_transition";
	public static final String TAG_CURRENT_STATE = "/current_state";
	public static final String TAG_CAREFLOW_STEP = "/careflow_step";
	public static final String TAG_TRANSITION = "/transition";
	public static final String TAG_WORKFLOW_ID = "/workflow_id";
	public static final String TAG_GUIDELINE_ID = "/guideline_id";
    public static final String TAG_OTHER_PARTICIPATIONS="/other_participations";
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

	
	static String TAG_ACTION_ARCHETYPE_ID="/action_archetype_id";

	public CompositionSerializer(WalkerOutputMode mode) {
		this.allElements = false;
        this.tag_mode = mode;
	}

    public CompositionSerializer(WalkerOutputMode mode, boolean allElements) {
        this.allElements = allElements;
        this.tag_mode = mode;
    }


    public CompositionSerializer() {
		this.allElements = false;
        this.tag_mode = WalkerOutputMode.PATH;
	}
	
    private ItemStack itemStack = new ItemStack();

	
	/**
	 * to remain consistent regarding datastructure, we use a map which prevents duplicated keys... and throw
	 * an exception if one is detected...
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> newPathMap(){
		return MapUtils.predicatedMap(new TreeMap<String, Object>(), PredicateUtils.uniquePredicate(), null);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> newMultiMap(){
		return MapUtils.multiValueMap(new HashMap<String, Object>());
	}
	/**
	 * put a key=value pair in a map and detects duplicates.
	 * @param map
	 * @param key
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private Object putObject(Map<String, Object> map, String key, Object obj) throws Exception {
		//CHC: 160602
		if (obj == null) return null;
		if (obj instanceof Map && ((Map) obj).size() == 0)
				return  null;
//		if (obj instanceof TreeMap)
//			if (((TreeMap) obj).size() == 0)
//				return  null;
//		if (map.size() > 0 && obj instanceof MultiValueMap) //should fit protocol, state etc.
//			if (((MultiValueMap) obj).size() == 0)
//				return  null;
		//======================
		Object retobj = null;
		try {
			//TODO: strip 'and name/value.... from the key to insert in the tree map
			retobj = map.put(key, obj);
		} catch (IllegalArgumentException e) {
			log.error("Ignoring duplicate key in path detected:"+key+" path:"+itemStack.pathStackDump()+" Exception:"+e);
//			throw new Exception("duplicate key:"+key+", please fix the input structure");
		}
		
		return retobj;
	}
	
	private String getNodeTag(String prefix, Locatable node, Class mapClass) {
		
		switch (tag_mode) {
		case PATH: 
			if (node == null)
				return prefix;
			else {
				String path = prefix + "[" + node.getArchetypeNodeId() + "]";
				if (!mapClass.equals(MultiValueMap.class)){
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

            default: return "*INVALID MODE*";
		}
	}

    private void encodePathItem(Map<String, Object> map, String tag) throws Exception {
        switch (tag_mode) {
            case PATH:
                putObject(map, TAG_PATH, tag == null ? itemStack.pathStackDump() : itemStack.pathStackDump()+tag);
                break;
            case NAMED:
                putObject(map, TAG_PATH, tag == null ? itemStack.namedStackDump() : itemStack.namedStackDump()+tag.substring(1));
                break;
            case EXPANDED:
                putObject(map, TAG_PATH, tag == null ? itemStack.expandedStackDump() : itemStack.expandedStackDump()+tag.substring(1));
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
	private void encodeNodeAttribute(Map<String, Object> map, String tag, Object value, String name) throws Exception {
		Map<String, Object> valuemap = newPathMap();
		//CHC: 160317 make name optional ex: timing
		if (name != null)
			putObject(valuemap, TAG_NAME, name);


		//CHC: 160317 make value optional ex. simple name for activity
		if (value != null) {
			putObject(valuemap, TAG_CLASS, value.getClass().getSimpleName());
			putObject(valuemap, TAG_VALUE, value);
		}
		encodePathItem(valuemap, tag);

		putObject(map, tag, valuemap);

	}

	private void encodeNodeMetaData(Map<String, Object> map, Object... values) throws Exception {
		return;
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

	}


	private Map<String, Object> mapRmObjectAttributes(RMObject object, String name) throws Exception {
		Map<String, Object> valuemap = newPathMap();
		putObject(valuemap, TAG_NAME, name);
		putObject(valuemap, TAG_CLASS, object.getClass().getSimpleName());

		//assign the actual object to the value (instead of its field equivalent...)
		putObject(valuemap, TAG_VALUE, object);
        encodePathItem(valuemap, null);

		return valuemap;

	}
	/**
	 * main entry method, process a composition.
	 * @param composition
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Composition composition) throws Exception {
		ctree = newPathMap();
		if (composition == null || composition.getContent() == null || composition.getContent().isEmpty())
			return null;
		
//		pushPathStack(TAG_COMPOSITION+"["+composition.getArchetypeNodeId()+"]");
		Map<String, Object>ltree = newMultiMap();

		for (ContentItem item : composition.getContent()) {
			putObject(ltree, getNodeTag(TAG_CONTENT, item, ltree.getClass()), traverse(item, TAG_CONTENT));
		}
		log.debug(ltree.toString());

        itemStack.popStacks();

		putObject(ctree, getNodeTag(TAG_COMPOSITION, composition, ctree.getClass()), ltree);
		//store locally the tree root
		String path = (String) ctree.keySet().toArray()[0];
		treeRootArchetype = ItemStack.normalizeLabel(path);
		treeRootClass = ItemStack.getLabelType(path);

		return ctree;
	}

    public Map<String, Object> processItem(Locatable locatable) throws Exception {
        ctree = newPathMap();

        if (locatable instanceof Item)
            putObject(ctree, getNodeTag(TAG_OTHER_CONTEXT, locatable, ctree.getClass()), traverse((Item)locatable, TAG_ITEMS));
        else if (locatable instanceof ItemStructure)
            putObject(ctree, getNodeTag(TAG_OTHER_CONTEXT, locatable, ctree.getClass()), traverse((ItemStructure)locatable, TAG_ITEMS));
        else
            throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

        return ctree;
    }

	public Map<String, Object> processItem(String tag, Locatable locatable) throws Exception {
		ctree = newPathMap();

		if (locatable instanceof Item)
			putObject(ctree, getNodeTag(tag, locatable, ctree.getClass()), traverse((Item)locatable, TAG_ITEMS));
		else if (locatable instanceof ItemStructure)
			putObject(ctree, getNodeTag(tag, locatable, ctree.getClass()), traverse((ItemStructure)locatable, TAG_ITEMS));
		else
			throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

		return ctree;
	}

	/**
	 * main entry method, process an arbitrary entry (evaluation, observation, instruction, action)
	 * @param entry
	 * @param entryTag
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Entry entry, String entryTag) throws Exception {
		ctree = newPathMap();

        itemStack.pushStacks(entryTag + "[" + entry.getArchetypeNodeId() + "]", entry.getName().getValue());
//		pushPathStack(entryTag + "[" + entry.getArchetypeNodeId() + "]");
//        pushNamedStack(entry.getName().getValue());

		putObject(ctree, getNodeTag(entryTag, entry, ctree.getClass()), traverse(entry, TAG_DATA));
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
	public Map<String, Object> process(Evaluation entry) throws Exception {
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
	public Map<String, Object> process(Observation entry) throws Exception {
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
	public Map<String, Object> process(Instruction entry) throws Exception {
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
	public Map<String, Object> process(Action entry) throws Exception {
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
	public Map<String, Object> process(Activity entry) throws Exception {
		if (entry == null || entry.getDescription() == null)
			return null;

		ctree = newPathMap();

        itemStack.pushStacks(entry + "[" + entry.getArchetypeNodeId() + "]", entry.getName().getValue());

		putObject(ctree, getNodeTag(TAG_ACTIVITIES, entry, ctree.getClass()), traverse(entry, TAG_DATA));
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
				putObject(ltree, getNodeTag(TAG_PROTOCOL,observation.getProtocol(), ltree.getClass()), traverse(observation.getProtocol(),TAG_PROTOCOL));
			
			if (observation.getData() != null)
				putObject(ltree, getNodeTag(TAG_DATA, observation.getData(), ltree.getClass()), traverse(observation.getData(), TAG_DATA));
			
			if (observation.getState() != null)
				putObject(ltree, getNodeTag(TAG_STATE, observation.getState(), ltree.getClass()), traverse(observation.getState(), TAG_STATE));

			if (observation.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, observation.getWorkflowId(), observation.getName().getValue());

			if (observation.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, observation.getGuidelineId(), observation.getName().getValue());

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof Evaluation) {
			Evaluation evaluation = (Evaluation) item;
			Map<String, Object>ltree = newPathMap();

			if (evaluation.getProtocol() != null)
				putObject(ltree, getNodeTag(TAG_PROTOCOL,evaluation.getProtocol(), ltree.getClass()), traverse(evaluation.getProtocol(),TAG_PROTOCOL));
			
			if (evaluation.getData() != null)
				putObject(ltree, getNodeTag(TAG_DATA, evaluation.getData(), ltree.getClass()), traverse(evaluation.getData(), TAG_DATA));

			if (evaluation.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, evaluation.getWorkflowId(), evaluation.getName().getValue());

			if (evaluation.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, evaluation.getGuidelineId(), evaluation.getName().getValue());

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
				putObject(ltree, getNodeTag(TAG_PROTOCOL, ((Instruction) item).getProtocol(), ltree.getClass()), traverse(instruction.getProtocol(), TAG_PROTOCOL));
			if (instruction.getNarrative() != null && !instruction.getNarrative().equals(new DvText(RmBinding.DEFAULT_NARRATIVE)))
				encodeNodeAttribute(ltree, TAG_NARRATIVE, instruction.getNarrative(), instruction.getName().getValue());
			if (instruction.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, instruction.getWorkflowId(), instruction.getName().getValue());
			if (instruction.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, instruction.getGuidelineId(), instruction.getName().getValue());
			if (instruction.getUid() != null)
				encodeNodeAttribute(ltree, TAG_UID, instruction.getUid(), instruction.getUid().getValue());

			if (instruction.getActivities() != null) {

//				for (Activity act : instruction.getActivities()) {
//                    itemStack.pushStacks(TAG_ACTIVITIES + "[" + act.getArchetypeNodeId() + "]", act.getName().getValue());
//					putObject(ltree, getNodeTag(TAG_ACTIVITIES, act, ltree.getClass()), traverse(act, TAG_DESCRIPTION));
//				}
				Map<String, Object> activities = newMultiMap();
				for (Activity act : instruction.getActivities()) {
					itemStack.pushStacks(TAG_ACTIVITIES + "[" + act.getArchetypeNodeId() + "]", act.getName().getValue());
					putObject(activities, getNodeTag(TAG_ACTIVITIES, act, activities.getClass()), traverse(act, TAG_DESCRIPTION));
					itemStack.popStacks();
				}

				putObject(ltree, TAG_ACTIVITIES, activities);

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

			if (action.getProtocol() != null)
				putObject(ltree, getNodeTag(TAG_PROTOCOL, action, ltree.getClass()), traverse(action.getProtocol(), TAG_PROTOCOL));
			
			if (action.getDescription() != null)
				putObject(ltree, getNodeTag(TAG_DESCRIPTION, action.getDescription(), ltree.getClass()), traverse(action.getDescription(), TAG_DESCRIPTION));
			
			if (action.getInstructionDetails() != null)
				putObject(ltree, getNodeTag(TAG_INSTRUCTION, item, ltree.getClass()), traverse(action.getInstructionDetails().getWfDetails(), TAG_INSTRUCTION));

			if (action.getWorkflowId() != null)
				encodeNodeAttribute(ltree, TAG_WORKFLOW_ID, action.getWorkflowId(), action.getName().getValue());

			if (action.getGuidelineId() != null)
				encodeNodeAttribute(ltree, TAG_GUIDELINE_ID, action.getGuidelineId(), action.getName().getValue());

			if (action.getTime() != null){
				if (!action.getTime().equals(new DvDateTime(RmBinding.DEFAULT_DATE_TIME)))
					encodeNodeAttribute(ltree, TAG_TIME, action.getTime(), action.getName().getValue());
			}

			if (action.getInstructionDetails() != null){
				InstructionDetails instructionDetails = action.getInstructionDetails();
				encodeNodeAttribute(ltree, TAG_INSTRUCTION_DETAILS+TAG_ACTIVITY_ID, instructionDetails.getActivityId(), action.getName().getValue());
				encodeNodeAttribute(ltree, TAG_INSTRUCTION_DETAILS+TAG_INSTRUCTION_ID, instructionDetails.getInstructionId(), action.getName().getValue());
			}


			if (action.getIsmTransition() != null){
				ISMTransition ismTransition = action.getIsmTransition();
				if (ismTransition != null && ismTransition.getCareflowStep() != null && !ismTransition.getCareflowStep().getValue().equals("DUMMY")) {
					if (ismTransition.getCurrentState() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_CURRENT_STATE, ismTransition.getCurrentState(), action.getName().getValue());
					if (ismTransition.getTransition() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_TRANSITION, ismTransition.getTransition(), action.getName().getValue());
					if (ismTransition.getCareflowStep() != null)
						encodeNodeAttribute(ltree, TAG_ISM_TRANSITION + TAG_CAREFLOW_STEP, ismTransition.getCareflowStep(), action.getName().getValue());
				}
			}

			//CHC: 160531 add explicit name
			if (action.getName() != null) encodeNodeMetaData(ltree, action);

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof Section) {

			Map<String, Object>ltree = newMultiMap();

			for (ContentItem i : ((Section) item).getItems()) {
				putObject(ltree, getNodeTag(TAG_ITEMS, i, ltree.getClass()), traverse(i, TAG_ITEMS));
			}
			//CHC: 160531 add explicit name
			Section section = (Section)item;
			if (section.getName() != null) encodeNodeMetaData(ltree, section);

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof AdminEntry) {
			AdminEntry ae = (AdminEntry) item;
			Map<String, Object>ltree = newPathMap();

			//CHC: 160531 add explicit name
			if (ae.getName() != null) encodeNodeMetaData(ltree, ae);

			if (ae.getData() != null)
				putObject(ltree, getNodeTag(TAG_DATA, ae.getData(), ltree.getClass()), traverse(ae.getData(), TAG_DATA));

			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

		} else if (item instanceof GenericEntry) {
			Map<String, Object>ltree = newPathMap();

			GenericEntry ge = (GenericEntry)item;
			//CHC: 160531 add explicit name
			if (ge.getName() != null) encodeNodeMetaData(ltree, ge);

			putObject(ltree, getNodeTag(TAG_DATA, ge.getData(), ltree.getClass()), traverse(ge.getData(), TAG_DATA));

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

//        if (item instanceof Instruction)
//		if (item.getUid() != null) {
//			map.put(TAG_UID, mapRmObjectAttributes(item.getUid(), "uid"));
//		}
	}

	private Map<String, Object> traverse(Activity act, String tag) throws Exception{
		if (act == null)
			return null;

		log.debug("traverse activity:"+act);

		Map<String, Object>ltree = newPathMap();

        if (act.getTiming() != null) {
			//CHC: 160317 do not pass a name for time
			if (!act.getTiming().equals(new DvParsable(RmBinding.DEFAULT_TIMING_SCHEME, RmBinding.DEFAULT_TIMING_FORMALISM)))
            	encodeNodeAttribute(ltree, TAG_TIMING, act.getTiming(), null);
        }

		//CHC: 160317 add explicit name for activity
		encodeNodeAttribute(ltree, TAG_NAME, null, act.getName().getValue());

        itemStack.pushStacks(tag + "[" + act.getDescription().getArchetypeNodeId() + "]", act.getDescription().getName().getValue());
//        pushPathStack(tag + "[" + act.getDescription().getArchetypeNodeId() + "]");
//        pushNamedStack(act.getName().getValue());

		log.debug(itemStack.pathStackDump()+TAG_DESCRIPTION+"["+act.getArchetypeNodeId()+"]="+act.getDescription().toString());
		putObject(ltree, getNodeTag(TAG_DESCRIPTION, act.getDescription(), ltree.getClass()), traverse(act.getDescription(), null)); //don't add a /data in path for description (don't ask me why...)


		if (act.getActionArchetypeId() != null) putObject(ltree, TAG_ACTION_ARCHETYPE_ID, act.getActionArchetypeId().trim());

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
			if (!item.getOrigin().equals(new DvDateTime(OptBinding.DEFAULT_DATE_TIME)))
				encodeNodeAttribute(ltree, TAG_ORIGIN, item.getOrigin(), item.getName().getValue());
		}

		if (item.getSummary() != null)
			putObject(ltree, getNodeTag(TAG_SUMMARY, item, ltree.getClass()), traverse(item.getSummary(), TAG_SUMMARY));
		
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
						encodeNodeAttribute(subtree, TAG_WIDTH, intervalEvent.getWidth(), event.getName().getValue());
					if (intervalEvent.getMathFunction() != null)
						encodeNodeAttribute(subtree, TAG_MATH_FUNCTION, intervalEvent.getMathFunction(), event.getName().getValue());
//					if (intervalEvent.getSampleCount() != null)
//						encodeNodeAttribute(subtree, TAG_MATH_FUNCTION, intervalEvent.getMathFunction(), event.getName().getValue());
				}


				if (event.getTime() != null) {
					if (!event.getTime().equals(new DvDateTime(OptBinding.DEFAULT_DATE_TIME)))
						encodeNodeAttribute(subtree, TAG_TIME, event.getTime(), event.getName().getValue());
				}
				if (event.getData() != null)
					putObject(subtree, getNodeTag(TAG_DATA, event.getData(), subtree.getClass()), traverse(event.getData(), TAG_DATA));
				if (event.getState() != null)
					putObject(subtree, getNodeTag(TAG_STATE, event.getState(), subtree.getClass()), traverse(event.getState(), TAG_STATE));

                itemStack.popStacks();
				putObject(eventtree, getNodeTag(TAG_EVENTS, event, eventtree.getClass()), subtree);
			}

			putObject(ltree, getNodeTag(TAG_EVENTS, null, ltree.getClass()), eventtree);
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
	private void compactEntry(Map<String, Object>target, String key, Map<String, Object>entry) throws Exception{
        //if entry is null, ignore, the dirty bit is not set...
        if (entry != null) {
            if (entry.keySet().size() == 1 && entry.get(TAG_VALUE) != null) {
                Object o = entry.get(TAG_VALUE);
                // TAG_VALUE is not required in the properties map representation
                putObject(target, key, o);
            } else
                putObject(target, key, entry); //unchanged and uncompacted
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

			ItemSingle is = (ItemSingle)item;
			//CHC: 160531 add explicit name
			if (is.getName() != null) encodeNodeMetaData(ltree, is);

			if (is.getItem()!=null){
				compactEntry(ltree, getNodeTag(TAG_ITEMS, is, ltree.getClass()), traverse(is.getItem(), TAG_ITEMS));
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;
		} else if (item instanceof ItemList) {
			Map<String, Object>ltree = newMultiMap();

			ItemList list = (ItemList) item;

			//CHC: 160531 add explicit name
			if (list.getName() != null) encodeNodeMetaData(ltree, list);

			if (list.getItems() != null) {

				for (Element element : list.getItems()) {
					if (ltree.containsKey(getNodeTag(TAG_ITEMS,item, ltree.getClass())))
						log.warn("ItemList: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(ltree, getNodeTag(TAG_ITEMS, element, ltree.getClass()), traverse(element, TAG_ITEMS));
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
			if (tree.getName() != null) encodeNodeMetaData(ltree, tree);

			if (tree.getItems() != null) {

				for (Item subItem : tree.getItems()) {
					if (ltree.containsKey(getNodeTag(TAG_ITEMS,item, ltree.getClass())))
						log.warn("ItemTree: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(ltree, getNodeTag(TAG_ITEMS, subItem, ltree.getClass()), traverse(subItem, TAG_ITEMS));
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
			if (table.getName() != null) encodeNodeMetaData(ltree, table);

			if (table.getRows() != null) {

				for (Item subItem : table.getRows()) {
					if (ltree.containsKey(getNodeTag(TAG_ITEMS, item, ltree.getClass())))
						log.warn("ItemTable: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
					compactEntry(ltree, getNodeTag(TAG_ITEMS, subItem, ltree.getClass()), traverse(subItem, TAG_ITEMS));
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
	private String getCompositeClassName(DataValue dataValue){
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

    private Map<String, Object> setElementAttributesMap(Element element) throws Exception {
        Map<String, Object>ltree = newPathMap();

        if (element != null && element.getValue() != null && !element.getValue().toString().isEmpty()){
            log.debug(itemStack.pathStackDump()+"="+ element.getValue());
            Map<String, Object> valuemap = newPathMap();
            //VBeanUtil.setValueMap(valuemap, element.getValue());
            putObject(valuemap, TAG_NAME, element.getName().getValue());

			if (element.getName() instanceof DvCodedText) {
				DvCodedText dvCodedText = (DvCodedText)element.getName();
				if (dvCodedText.getDefiningCode() != null)
					putObject(valuemap, TAG_DEFINING_CODE, dvCodedText.getDefiningCode());
			}

			putObject(valuemap, TAG_CLASS, getCompositeClassName(element.getValue()));
//            putObject(valuemap, TAG_CLASS, element.getValue().getClass().getSimpleName());
            //assign the actual object to the value (instead of its field equivalent...)
            putObject(valuemap, TAG_VALUE, element.getValue());
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
	private Map<String, Object> traverse(Item item, String tag) throws Exception {
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
			itemStack.pushStacks(tag + "[" + item.getArchetypeNodeId() + "]", item.getName().getValue());

			Cluster c = (Cluster) item;

			//CHC: 160531 add explicit name
			if (c.getName() != null) encodeNodeMetaData(ltree, item);

			if (c.getItems() != null) {

				for (Item i : c.getItems()) {
					putObject(ltree, getNodeTag(TAG_ITEMS, i, ltree.getClass()), traverse(i, TAG_ITEMS));
                }
			}
			if (ltree.size() > 0)
				retmap = ltree;
			else
				retmap = null;

			itemStack.popStacks();
		}

		return retmap;
	}

	public Map<String, String> getLtreeMap(){
		return itemStack.getLtreeMap();
	}

	public String getTreeRootArchetype() {
		return treeRootArchetype;
	}

	public String getTreeRootClass() {
		return treeRootClass;
	}

	public static String jsonMapToString(Map map){
		GsonBuilder builder = new GsonBuilder();

		//register TypeAdapters for all known datatypes we can serialize/parse...
//		adapter.setBuilderAdapters(builder);
//		builder.registerTypeAdapter(DataValue.class, adapter);
//
		builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
//		builder.registerTypeAdapter(DvQuantity.class, adapter);
//		builder.registerTypeAdapter(DvText.class, adapter);

		Gson gson = builder.setPrettyPrinting().create();
		String mapjson = gson.toJson(map);

		return mapjson;
	}
}
