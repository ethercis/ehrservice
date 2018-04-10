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
package com.ethercis.ehr.building.util;

import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.*;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.history.Event;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.itemstructure.*;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.integration.GenericEntry;

import java.util.*;

/**
 * Sequential Event Processor for Composition.<p>
 *     used to invalidate composition content to perform updates...
 * @see ElementWrapper
 * @author Christian Chevalley
 *
 */
public class ContentHelper implements I_ContentHelper {


	public enum WalkerOutputMode {
		PATH,
		NAMED,
        EXPANDED,
		RAW
	}

	protected static Logger log = LogManager.getLogger(ContentHelper.class);


	protected final WalkerOutputMode tag_mode; //default
    private final boolean allElements; //default

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

	public ContentHelper(WalkerOutputMode mode) throws IllegalAccessException {
		this.allElements = false;
        this.tag_mode = mode;
//		initTags();
	}

	public ContentHelper(boolean allElements) throws IllegalAccessException {
		this.allElements = allElements;
		this.tag_mode = WalkerOutputMode.PATH;
//		initTags();
	}

	public ContentHelper(WalkerOutputMode mode, boolean allElements) throws IllegalAccessException {
        this.allElements = allElements;
        this.tag_mode = mode;
//		initTags();
    }


	public ContentHelper() throws IllegalAccessException {
		this.allElements = false;
        this.tag_mode = WalkerOutputMode.PATH;
//		initTags();
	}
	
	/**
	 * main entry method, invalidateContent a composition.
	 * @param composition
	 * @return
	 * @throws Exception
	 */
	@Override
	public void invalidateContent(Composition composition) throws Exception {
		if (composition == null || composition.getContent() == null || composition.getContent().isEmpty())
			return;
		

		for (ContentItem item : composition.getContent()) {
			traverse(item, TAG_CONTENT);
		}
	}

	@Override
    public void processItem(Locatable locatable) throws Exception {

        if (locatable instanceof Item)
            traverse((Item) locatable, TAG_ITEMS);
        else if (locatable instanceof ItemStructure)
            traverse((ItemStructure) locatable, TAG_ITEMS);
        else
            throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

    }

	@Override
	public void processItem(String tag, Locatable locatable) throws Exception {

		if (locatable instanceof Item)
			traverse((Item) locatable, TAG_ITEMS);
		else if (locatable instanceof ItemStructure)
			traverse((ItemStructure) locatable, TAG_ITEMS);
		else
			throw new IllegalArgumentException("locatable is not an Item or ItemStructure instance...");

	}

	/**
	 * main entry method, invalidateContent an arbitrary entry (evaluation, observation, instruction, action)
	 * @param entry
	 * @param entryTag
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Entry entry, String entryTag) throws Exception {
		traverse(entry, TAG_DATA);
	}
	
	/**
	 * convenience method for processing an Evaluation
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Evaluation entry) throws Exception {
		if (entry == null || entry.getData() == null)
			return;

		invalidateContent(entry, TAG_EVALUATION);
	}
	
	/**
	 * convenience method for processing an Observation
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Observation entry) throws Exception {
		if (entry == null || entry.getData() == null)
			return;

		invalidateContent(entry, TAG_OBSERVATION);
	}
	
	/**
	 * convenience method for processing an Instruction
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Instruction entry) throws Exception {
		if (entry == null || entry.getActivities() == null)
			return;

		invalidateContent(entry, TAG_INSTRUCTION);
	}

	/**
	 * convenience method for processing an Instruction
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Action entry) throws Exception {
		if (entry == null || entry.getDescription() == null)
			return;

		invalidateContent(entry, TAG_ACTION);
	}

	/**
	 * convenience method for processing an Activity
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	private void invalidateContent(Activity entry) throws Exception {
		if (entry == null || entry.getDescription() == null)
			return;

		traverse(entry, TAG_DATA);
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
	private void traverse(ContentItem item, String tag) throws Exception {

		Map<String, Object> retmap = null;
		
		if (item == null){
			return;
		}

		log.debug("traverse element of class:"+item.getClass()+", tag:"+tag+", nodeid:"+item.getArchetypeNodeId());

		if (item instanceof Observation) {
			Observation observation = (Observation) item;

			if (observation.getProtocol() != null)
				traverse(observation.getProtocol(), TAG_PROTOCOL);
			
			if (observation.getData() != null)
				traverse(observation.getData(), TAG_DATA);
			
			if (observation.getState() != null)
				traverse(observation.getState(), TAG_STATE);

		} else if (item instanceof Evaluation) {
			Evaluation evaluation = (Evaluation) item;

			if (evaluation.getProtocol() != null)
				traverse(evaluation.getProtocol(), TAG_PROTOCOL);
			
			if (evaluation.getData() != null)
				traverse(evaluation.getData(), TAG_DATA);

		} else if (item instanceof Instruction) {
			Instruction instruction = (Instruction) item;

			if (instruction.getProtocol() != null)
				traverse(instruction.getProtocol(), TAG_PROTOCOL);

			if (instruction.getActivities() != null) {
				for (Activity activity : instruction.getActivities()) {
					traverse(activity, TAG_DESCRIPTION);
				}
			}

		} else if (item instanceof Action) {
			Action action = (Action) item;

			if (action.getProtocol() != null)
				traverse(action.getProtocol(), TAG_PROTOCOL);
			
			if (action.getDescription() != null)
				traverse(action.getDescription(), TAG_DESCRIPTION);

		} else if (item instanceof Section) {

			for (ContentItem contentItem : ((Section) item).getItems()) {
				traverse(contentItem, TAG_ITEMS);
			}

		} else if (item instanceof AdminEntry) {
			AdminEntry adminEntry = (AdminEntry) item;
			if (adminEntry.getData() != null)
				traverse(adminEntry.getData(), TAG_DATA);

		} else if (item instanceof GenericEntry) {
			GenericEntry genericEntry = (GenericEntry)item;

			traverse(genericEntry.getData(), TAG_DATA);

		} else {
			log.warn("This item is not handled!"+item.nodeName());
		}
	}

	private void traverse(Activity activity, String tag) throws Exception{
		if (activity == null)
			return;

		log.debug("traverse activity:"+activity);

		traverse(activity.getDescription(), null); //don't add a /data in path for description (don't ask me why...)

	}
	
	
	/**
	 * History level in composition
	 * @param item
	 * @param tag
	 * @throws Exception
	 */
	private void traverse(History<?> item, String tag) throws Exception {
		if (item == null){
			return;
		}

		log.debug("traverse history:"+item);

		//CHC: 160531 add explicit name
		History history = (History)item;

		if (item.getSummary() != null)
			traverse(item.getSummary(), TAG_SUMMARY);
		
		if (item.getEvents() != null) {

			for (Event<?> event : item.getEvents()) {

				if (event.getData() != null)
					traverse(event.getData(), TAG_DATA);
				if (event.getState() != null)
					traverse(event.getState(), TAG_STATE);

			}
		}
	}
	
	/**
	 * ItemStructure: single, tree or table
	 * @param item
	 * @param uppertag
	 * @throws Exception
	 */
	private void traverse(ItemStructure item, String uppertag) throws Exception {

		log.debug("traverse itemstructure:"+item);
		
		if (item == null){
			return;
		}


		if (item instanceof ItemSingle) {
			ItemSingle itemSingle = (ItemSingle)item;
			if (itemSingle.getItem()!=null){
				traverse(itemSingle.getItem(), TAG_ITEMS);
			}
		} else if (item instanceof ItemList) {
			ItemList list = (ItemList) item;
			if (list.getItems() != null) {

				for (Element element : list.getItems()) {
					traverse(element, TAG_ITEMS);
				}
			}
		} else if (item instanceof ItemTree) {
			ItemTree tree = (ItemTree) item;

			if (tree.getItems() != null) {

				for (Item subItem : tree.getItems()) {
					traverse(subItem, TAG_ITEMS);
				}
			}

		} else if (item instanceof ItemTable) {
			ItemTable table = (ItemTable) item;
			if (table.getRows() != null) {

				for (Item subItem : table.getRows()) {
					traverse(subItem, TAG_ITEMS);
				}
			}
		}
	}

    protected void invalidateElement(Element element) throws Exception {
		log.debug("should invalidate this element:"+element);
    }

	/**
	 * Element level, normally cannot go deeper...
	 * @param item
	 * @param tag
	 * @throws Exception
	 */
	private void traverse(Item item, String tag) throws Exception {
		log.debug("traverse item:"+item);

		if (item == null){
			return;
		}

		if (item instanceof Element) {
			invalidateElement((Element) item);
		} else if (item instanceof ElementWrapper){
            if (allElements || ((ElementWrapper)item).dirtyBitSet()) {
				((ElementWrapper)item).setDirtyBit(false);
			}
            else
                log.debug("Ignoring unchanged element:"+item.toString());
        }

        else if (item instanceof Cluster) {

			Cluster c = (Cluster) item;

			if (c.getItems() != null) {

				for (Item clusterItem : c.getItems()) {
					traverse(clusterItem, TAG_ITEMS);
				}
			}
		}
	}
}
