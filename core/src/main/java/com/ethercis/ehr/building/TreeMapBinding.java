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

import com.ethercis.ehr.encode.ItemStack;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.I_EhrScapeWrapper;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.json.TreeMapNode;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.PredicateUtils;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Christian Chevalley
 *
 */
public class TreeMapBinding {
	public enum WalkerOutputMode {
		PATH,
		NAMED
	}

	private static Logger log = LogManager.getLogger(TreeMapBinding.class);

	private Map<String, Object> ctree;
	private WalkerOutputMode tag_mode = WalkerOutputMode.PATH; //default

    private TreeMapNode treeMap;

//    private String[] pathTarget;
//    private int cursor = 0; //this is the cursor in the pathTarget string array


	public TreeMapBinding(Map<String, String> nodeRecords, WalkerOutputMode mode) {
        this.treeMap = new TreeMapNode(FlatJsonUtil.unflattenJSON(nodeRecords));
        this.tag_mode = mode;
	}

	public TreeMapBinding(Map<String, String> nodeRecords) {
        this.treeMap = new TreeMapNode(FlatJsonUtil.unflattenJSON(nodeRecords));
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

    //set to lower case and replace spaces by '_'
    private String normalize(String aString){
        return aString.toLowerCase().replaceAll("[ /]", "_");
    }
    
    private String normalize(Locatable locatable){
        return normalize(locatable.getName().toString());
    }

    public Map<String, Object> process(Composition composition) throws Exception {
        return process(composition, treeMap);
    }


	/**
	 * main entry method, process a composition.
	 * @param composition
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Composition composition, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

        TreeMapNode tree = searchNode;

		if (composition == null || composition.getContent() == null || composition.getContent().isEmpty())
			return null;

        String rootName = normalize(composition);
        
        if (!tree.getCurrentTree().contains(rootName)) //this is not corresponding to this composition...
            return null;

        itemStack.pushStacks(""+"["+composition.getArchetypeNodeId()+"]", rootName);

        //get the part relevant for the composition content
        tree = tree.nextChild(rootName);

		for (ContentItem item : composition.getContent()) {
            traverse(item, tree.nextChild(normalize(item)));
		}

        itemStack.popStacks();
		return null;
	}

	/**
	 * main entry method, invalidateContent an arbitrary entry (evaluation, observation, instruction, action)
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Entry entry, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		ctree = newPathMap();

//        if (!entry.getName().getValue().equals(pathTarget[cursor])) {
//            --cursor;
//            return null;
//        }

        itemStack.pushStacks("[" + entry.getArchetypeNodeId() + "]", normalize(entry.getName().getValue()));

        traverse(entry, searchNode.nextChild(normalize(entry)));
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
	public Map<String, Object> process(Evaluation entry, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		if (entry == null || entry.getData() == null)
			return null;

		return process(entry, searchNode.nextChild(normalize(entry)));
	}
	
	/**
	 * convenience method for processing an Observation
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Observation entry, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		if (entry == null || entry.getData() == null)
			return null;

		return process(entry, searchNode.nextChild(normalize(entry)));
	}
	
	/**
	 * convenience method for processing an Instruction
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Instruction entry, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		if (entry == null || entry.getActivities() == null)
			return null;

		return process(entry, searchNode.nextChild(normalize(entry)));
	}	
	
	/**
	 * convenience method for processing an Activity
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> process(Activity entry, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		if (entry == null || entry.getDescription() == null)
			return null;

		ctree = newPathMap();

        itemStack.pushStacks("[" + entry.getArchetypeNodeId() + "]", normalize(entry.getName().getValue()));
//		pushPathStack(entry + "[" + entry.getArchetypeNodeId() + "]");
//        pushNamedStack(entry.getName().getValue());

//		putObject(ctree, getNodeTag(TAG_ACTIVITIES, entry), traverse(entry, TAG_DATA));
        traverse(entry, searchNode.nextChild(normalize(entry)));
		log.debug(ctree.toString());

        itemStack.popStacks();
		return ctree;
	}		
	
	public void setMode(WalkerOutputMode mode) {
		this.tag_mode = mode;
	}


	/**
	 * domain level: Observation, evaluation, instruction, action. section, admin etc.
	 * @param item
	 * @throws Exception
	 */
	private Map<String, Object> traverse(ContentItem item, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		Map<String, Object> retmap = null;
		
		if (item == null){
			return null;
		}

        itemStack.pushStacks("[" + item.getArchetypeNodeId() + "]", normalize(item.getName().getValue()));
//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());
		
		if (item instanceof Observation) {
			Observation observation = (Observation) item;
			Map<String, Object>ltree = newPathMap();

			if (observation.getProtocol() != null)
//				putObject(ltree, getNodeTag(TAG_PROTOCOL, observation.getProtocol()), traverse(observation.getProtocol(), TAG_PROTOCOL));
                traverse(observation.getProtocol(), searchNode.nextChild(observation.getProtocol()));

			if (observation.getData() != null)
//                putObject(ltree, getNodeTag(TAG_DATA, observation.getData()), traverse(observation.getData(), TAG_DATA));
				traverse(observation.getData(), searchNode.nextChild(normalize(observation.getData())));

			if (observation.getState() != null)
//                putObject(ltree, getNodeTag(TAG_STATE, observation.getState()), traverse(observation.getState(), TAG_STATE));
				traverse(observation.getState(), searchNode.nextChild(normalize(observation.getState())));
			retmap = ltree;
			
		} else if (item instanceof Evaluation) {
			Evaluation evaluation = (Evaluation) item;
			Map<String, Object>ltree = newPathMap();
			
			if (evaluation.getProtocol() != null) {
//                putObject(ltree, getNodeTag(TAG_PROTOCOL,evaluation.getProtocol()), traverse(evaluation.getProtocol(),TAG_PROTOCOL));

                Integer i = 0;
                for (Item itemProtocol : ((ItemTree) evaluation.getProtocol()).getItems()) {
//                    traverse(evaluation.getProtocol(), searchNode.nextChild(normalize(evaluation.getProtocol())));
                    searchNode = searchNode.nextChild(i++);
                    traverse(itemProtocol, searchNode.nextChild(normalize(itemProtocol)));
                }
            }
			if (evaluation.getData() != null) {
//                Integer i = 0;
                for (Item itemData: ((ItemTree) evaluation.getData()).getItems()) {
//                putObject(ltree, getNodeTag(TAG_DATA, evaluation.getData()), traverse(evaluation.getData(), TAG_DATA));
//                    searchNode = searchNode.nextChild(i++);
                    traverse(itemData, searchNode.nextChild(normalize(itemData)));
                }

            }
			retmap = ltree;
			
		} else if (item instanceof Instruction) {
			Map<String, Object>ltree = newMultiMap();

			Instruction instruction = (Instruction) item;
			if (instruction.getProtocol() != null)
//                putObject(ltree, getNodeTag(TAG_PROTOCOL, ((Instruction) item).getProtocol()), traverse(instruction.getProtocol(), TAG_PROTOCOL));
				traverse(instruction.getProtocol(), searchNode.nextChild(normalize(instruction.getProtocol())));

			if (instruction.getActivities() != null) {

                int cnt = 0;
				for (Activity act : instruction.getActivities()) {
                    itemStack.pushStacks("[" + act.getArchetypeNodeId() + "]", act.getName().getValue()+":"+cnt++);
//                    pushPathStack(TAG_ACTIVITIES + "[" + act.getArchetypeNodeId() + "]");
//                    pushNamedStack(act.getName().getValue());
//                    putObject(ltree, getNodeTag(TAG_ACTIVITIES, act), traverse(act, TAG_DESCRIPTION));
//                    putObject(ltree, getNodeTag(TAG_ACTIVITIES, act), traverse(act, TAG_DESCRIPTION));
					traverse(act, searchNode.nextChild(normalize(act)));
				}
			}
			retmap = ltree;

		} else if (item instanceof Action) {
			Map<String, Object>ltree = newPathMap();

			Action action = (Action) item;
			if (action.getProtocol() != null)
//                putObject(ltree, getNodeTag(TAG_PROTOCOL, action), traverse(action.getProtocol(), TAG_PROTOCOL));
				traverse(action.getProtocol(), searchNode.nextChild(normalize(action.getProtocol())));
			
			if (action.getDescription() != null)
//                putObject(ltree, getNodeTag(TAG_DESCRIPTION, action.getDescription()), traverse(action.getDescription(), TAG_DESCRIPTION));
				traverse(action.getDescription(),searchNode.nextChild(normalize(action.getDescription())) );
			
			if (action.getInstructionDetails() != null)
//                putObject(ltree, getNodeTag(TAG_INSTRUCTION, item), traverse(action.getInstructionDetails().getWfDetails(), TAG_INSTRUCTION));
				traverse(action.getInstructionDetails().getWfDetails(), searchNode.nextChild(normalize(action.getInstructionDetails().getWfDetails())));
			
			if (action.getTime() != null){
//				encodeNodeAttribute(ltree, TAG_TIME, action.getTime(), action.getName().getValue());
                ;
			}
			
			retmap = ltree;

		} else if (item instanceof Section) {

			Map<String, Object>ltree = newMultiMap();

            Integer i = 0;
			for (ContentItem contentItem : ((Section) item).getItems()) {
//                putObject(ltree, getNodeTag(TAG_ITEMS, i), traverse(i, TAG_ITEMS));
                //get the corresponding occurence in tree
                searchNode = searchNode.nextChild(i++);
                //perform the traversal
				traverse(contentItem, searchNode.nextChild(normalize(contentItem)));
			}
			retmap = ltree;

		} else if (item instanceof AdminEntry) {
			AdminEntry adminEntry = (AdminEntry) item;
			Map<String, Object>ltree = newPathMap();

			if (adminEntry.getData() != null)
//                putObject(ltree, getNodeTag(TAG_DATA, ae.getData()), traverse(ae.getData(), TAG_DATA));
				traverse(adminEntry.getData(), searchNode.nextChild(normalize(adminEntry.getData())));
			retmap = ltree;

		} else if (item instanceof GenericEntry) {
			Map<String, Object>ltree = newPathMap();

			GenericEntry genericEntry = (GenericEntry)item;
//            putObject(ltree, getNodeTag(TAG_DATA, ge.getData()), traverse(ge.getData(), TAG_DATA));
			traverse(genericEntry.getData(), searchNode.nextChild(normalize(genericEntry.getData())));
			retmap = ltree;

		} else {
			log.warn("This item is not handled!"+item.nodeName());
		}

        //add complementary attributes

		if (item instanceof Entry){
//			putEntryAttributes(retmap, (Entry) item);
            ;
		}

        itemStack.popStacks();
		return retmap;

	}

	private Map<String, Object> traverse(Activity activity, TreeMapNode searchNode) throws Exception{
        if (searchNode == null)
            return null;

		if (activity == null)
			return null;

		Map<String, Object>ltree = newPathMap();

        if (activity.getTiming() != null) {
//            encodeNodeAttribute(ltree, TAG_TIMING, act.getTiming(), act.getName().getValue());
            ;
        }

        itemStack.pushStacks("[" + activity.getDescription().getArchetypeNodeId() + "]", normalize(activity.getDescription().getName().getValue()));
//        pushPathStack(tag + "[" + act.getDescription().getArchetypeNodeId() + "]");
//        pushNamedStack(act.getName().getValue());

//		log.debug(elementStack.pathStackDump()+TAG_DESCRIPTION+"["+act.getArchetypeNodeId()+"]="+act.getDescription().toString());
//		putObject(ltree, getNodeTag(TAG_DESCRIPTION, act.getDescription()), traverse(act.getDescription(), null)); //don't add a /data in path for description (don't ask me why...)
        traverse(activity.getDescription(), searchNode.nextChild(activity.getDescription())); //don't add a /data in path for description (don't ask me why...)


		if (activity.getActionArchetypeId() != null)
			if (tag_mode == WalkerOutputMode.PATH)
//				putObject(ltree, TAG_ACTION_ARCHETYPE_ID, act.getActionArchetypeId().trim());
                ;
			else
				; //skip

        itemStack.popStacks();
		return ltree;
	}
	
	
	/**
	 * History level in composition
	 * @param item
	 * @throws Exception
	 */
	private Map<String, Object> traverse(History<?> item, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

        if (item == null){
			return null;
		}

        itemStack.pushStacks("[" + item.getArchetypeNodeId() + "]", normalize(item.getName().getValue()));
//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());

		Map<String, Object>ltree = newPathMap();
		
//		log.debug(elementStack.pathStackDump()+TAG_ORIGIN+"["+item.getArchetypeNodeId()+"]="+item.getOrigin().toString());
		
		if (item.getOrigin() != null)
//			encodeNodeAttribute(ltree, TAG_ORIGIN, item.getOrigin(), item.getName().getValue());
            {;}

		if (item.getSummary() != null)
//            putObject(ltree, getNodeTag(TAG_SUMMARY, item), traverse(item.getSummary(), TAG_SUMMARY));
			traverse(item.getSummary(), searchNode.nextChild(normalize(item.getSummary())));
		
		if (item.getEvents() != null) {

			Map<String, Object>eventtree = newMultiMap();
//			putObject(ltree, getNodeTag(TAG_EVENTS, null), eventtree);

            int cnt = 0;
			for (Event<?> event : item.getEvents()) {
                itemStack.pushStacks("[" + event.getArchetypeNodeId() + "]", normalize(event.getName().getValue()+":"+cnt++));
//				pushPathStack(TAG_EVENTS + "[" + event.getArchetypeNodeId() + "]");
//                pushNamedStack(event.getName().getValue());

				Map<String, Object> subtree = newPathMap();
//				log.debug(elementStack.pathStackDump()+TAG_TIME+"["+event.getArchetypeNodeId()+"]="+event.getTime());

				if (event.getTime() != null)
					//encodeNodeAttribute(subtree, TAG_TIME, event.getTime(), event.getName().getValue());
                    ;

				if (event.getData() != null)
//                    putObject(subtree, getNodeTag(TAG_DATA, event.getData()), traverse(event.getData(), TAG_DATA));
					traverse(event.getData(), searchNode.nextChild(normalize(event.getData())));

				if (event.getState() != null)
//                    putObject(subtree, getNodeTag(TAG_STATE, event.getState()), traverse(event.getState(), TAG_STATE));
					traverse(event.getState(), searchNode.nextChild(event.getState()));

                itemStack.popStacks();
//				putObject(eventtree, getNodeTag(TAG_EVENTS, event), subtree);
			}

			
		}
        itemStack.popStacks();
		return ltree;

	}
	
	/**
	 * ItemStructure: single, tree or table
	 * @param item
	 * @throws Exception
	 */
	private Map<String, Object> traverse(ItemStructure item, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		Map<String, Object> retmap = null;
		
		if (item == null){
			return null;
		}


        itemStack.pushStacks("[" + item.getArchetypeNodeId() + "]", normalize(item.getName().getValue()));


		if (item instanceof ItemSingle) {
			Map<String, Object>ltree = newPathMap();

			ItemSingle is = (ItemSingle)item;
			if (is.getItem()!=null){
				//compactEntry(ltree, getNodeTag(TAG_ITEMS,is), traverse(is.getItem(), TAG_ITEMS));
                traverse(is.getItem(), searchNode.nextChild(normalize(is.getItem())));
                ;
			}
			retmap = ltree;
		} else if (item instanceof ItemList) {
			Map<String, Object>ltree = newMultiMap();

			ItemList list = (ItemList) item;
			if (list.getItems() != null) {

				for (Element element : list.getItems()) {
//					if (ltree.containsKey(getNodeTag(TAG_ITEMS,item)))
//						log.warn("ItemList: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
//					compactEntry(ltree, getNodeTag(TAG_ITEMS, e), traverse(e, TAG_ITEMS));
					traverse(element, searchNode.nextChild(normalize(element)));
                    ;
				}
			}
			retmap = ltree;

		} else if (item instanceof ItemTree) {
			Map<String, Object>ltree = newPathMap();

			ItemTree tree = (ItemTree) item;
			if (tree.getItems() != null) {

                Integer i = 0;
				for (Item item1 : tree.getItems()) {
//					if (ltree.containsKey(getNodeTag(TAG_ITEMS,item)))
//						log.warn("ItemTree: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
//					compactEntry(ltree, getNodeTag(TAG_ITEMS, i), traverse(i, TAG_ITEMS));
                    searchNode = searchNode.nextChild(i++);
					traverse(item1, searchNode.nextChild(normalize(item1)));
                    ;
				}
			}
			retmap = ltree;

		} else if (item instanceof ItemTable) {
			Map<String, Object>ltree = newPathMap();

			ItemTable table = (ItemTable) item;
			if (table.getRows() != null) {

				for (Item item1 : table.getRows()) {
//					if (ltree.containsKey(getNodeTag(TAG_ITEMS, item)))
//						log.warn("ItemTable: Overwriting entry for key:"+TAG_ITEMS+"["+item.getArchetypeNodeId()+"]");
//                    compactEntry(ltree, getNodeTag(TAG_ITEMS, i), traverse(i, TAG_ITEMS));
					traverse(item1, searchNode.nextChild(normalize(item1)));
                    ;
				}
			}
			retmap = ltree;

		}
        itemStack.popStacks();
		return retmap;

	}

    private Map<String, String> elementValueMap(TreeMapNode searchNode){
        if (searchNode.getCurrentNode() instanceof String){
            Map retmap = new HashMap<>();
            retmap.put("value", searchNode.getCurrentNode());
            return retmap;
        }
        else {
            Map retmap = new HashMap<>();
            if (searchNode.getCurrentNode() instanceof List){
                for (Object item: (List)searchNode.getCurrentNode()){
                    ;
                }
            }
            else
                retmap.putAll((Map)searchNode.getCurrentNode());
            return retmap;
        }
    }

	/**
	 * Element level, normally cannot go deeper...
	 * @param item
	 * @throws Exception
	 */
	private Map<String, Object> traverse(Item item, TreeMapNode searchNode) throws Exception {
        if (searchNode == null)
            return null;

		Map<String, Object> retmap = null;

		if (item == null){
			return null;
		}

        itemStack.pushStacks( "[" + item.getArchetypeNodeId() + "]", normalize(item));
//		pushPathStack(tag + "[" + item.getArchetypeNodeId() + "]");
//        pushNamedStack(item.getName().getValue());

        //for compatibility purpose, normally only ElementWrapper should be passed
		if (item instanceof Element) {
			//retmap = setElementAttributesMap((Element) item);
            log.debug(itemStack.namedStackDump()+normalize(item));
            ;
			
		} else if (item instanceof ElementWrapper){
            //retmap = setElementAttributesMap(((ElementWrapper)item).getAdaptedElement());
            log.debug(itemStack.namedStackDump()+((ElementWrapper) item).getAdaptedElement().getName().getValue());
            I_VBeanWrapper wrapped = ((ElementWrapper)item).getWrappedValue();
            if (wrapped instanceof I_EhrScapeWrapper) {
                Map<String, String> attributes = new HashMap<>();
                if (searchNode.getCurrentNode() instanceof String){ //straight value...
                    attributes.put("value", (String)searchNode.getCurrentNode());
                }
                else {
                    attributes =  elementValueMap(searchNode);
                }

                if (attributes.size() > 0){
                    Object instance = ((I_EhrScapeWrapper)wrapped).createInstance(attributes);
                    wrapped.setAdaptee(instance);
                }
            }

        }

        else if (item instanceof Cluster) {
			Map<String, Object>ltree = newMultiMap();

			Cluster cluster = (Cluster) item;
			if (cluster.getItems() != null) {

				for (Item item1 : cluster.getItems()) {
//                    putObject(ltree, getNodeTag(TAG_ITEMS, i), traverse(i, TAG_ITEMS));
					traverse(item1, searchNode.nextChild(normalize(item1)));
                }
			}
			retmap = ltree;

		}
        itemStack.popStacks();
		return retmap;
	}

}
