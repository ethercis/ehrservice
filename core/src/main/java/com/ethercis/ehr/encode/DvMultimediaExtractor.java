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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.Action;
import org.openehr.rm.composition.content.entry.Activity;
import org.openehr.rm.composition.content.entry.AdminEntry;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.composition.content.entry.Instruction;
import org.openehr.rm.composition.content.entry.Observation;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.history.Event;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.itemstructure.ItemList;
import org.openehr.rm.datastructure.itemstructure.ItemSingle;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datastructure.itemstructure.ItemTable;
import org.openehr.rm.datastructure.itemstructure.ItemTree;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.integration.GenericEntry;

public class DvMultimediaExtractor {
	private static Logger log = LogManager.getLogger(DvMultimediaExtractor.class);
	/**
	 * 
	 * @param composition
	 * @param path
	 * @return
	 */
	public static List<DvMultimedia> extract(Composition composition,
			String path) {
		List<DvMultimedia> medias = new ArrayList<DvMultimedia>();
		Locatable loc = (Locatable) composition.itemAtPath(path);
		if (loc == null)
			return medias;
		if (loc instanceof Item) {
			handle(medias, (Item) loc);
		} else if (loc instanceof ItemStructure) {
			handle(medias, (ItemStructure) loc);
		} else if (loc instanceof History) {
			handle(medias, (History<?>) loc);
		} else if (loc instanceof ContentItem) {
			handle(medias, (ContentItem) loc);
		} else {
			log.warn("Locatable type "+loc.getClass() + " does not support");
		}
		return medias;

	}
	/**
	 * 
	 * @param composition
	 * @return
	 */
	public static List<DvMultimedia> extract(Composition composition) {
		List<DvMultimedia> medias = new ArrayList<DvMultimedia>();

		if (composition == null || composition.getContent() == null
				|| composition.getContent().isEmpty())
			return medias;
		for (ContentItem item : composition.getContent()) {
			handle(medias, item);
		}
		return medias;

	}

	public static void handle(List<DvMultimedia> medias, ContentItem item) {

		if (item == null)
			return;

		if (item instanceof Observation) {
			Observation o = (Observation) item;
			handle(medias, o.getProtocol());
			handle(medias, o.getData());
			handle(medias, o.getState());
		} else if (item instanceof Evaluation) {
			Evaluation e = (Evaluation) item;
			handle(medias, e.getProtocol());
			handle(medias, e.getData());
		} else if (item instanceof Instruction) {
			Instruction i = (Instruction) item;
			handle(medias, i.getProtocol());
			if (i.getActivities() != null) {
				for (Activity act : i.getActivities()) {
					handle(medias, act.getDescription());
				}
			}
		} else if (item instanceof Action) {
			Action a = (Action) item;
			handle(medias, a.getProtocol());
			handle(medias, a.getDescription());
			if (a.getInstructionDetails() != null) {
				handle(medias, a.getInstructionDetails().getWfDetails());
			}
		} else if (item instanceof Section) {
			for (ContentItem i : ((Section) item).getItems()) {
				handle(medias, i);
			}
		} else if (item instanceof AdminEntry) {
			handle(medias, ((AdminEntry) item).getData());
		} else if (item instanceof GenericEntry) {
			handle(medias, ((GenericEntry) item).getData());
		}
	}

	public static void handle(List<DvMultimedia> medias, History<?> item) {
		if (item == null)
			return;

		handle(medias, item.getSummary());
		if (item.getEvents() != null) {
			for (Event<?> event : item.getEvents()) {
				handle(medias, event.getData());
				handle(medias, event.getState());
			}
		}
	}

	public static void handle(List<DvMultimedia> medias, ItemStructure item) {
		if (item == null)
			return;

		if (item instanceof ItemSingle) {
			handle(medias, ((ItemSingle) item).getItem());
		} else if (item instanceof ItemList) {
			ItemList list = (ItemList) item;
			if (list.getItems() != null) {
				for (Element e : list.getItems()) {
					handle(medias, e);
				}
			}
		} else if (item instanceof ItemTree) {
			ItemTree tree = (ItemTree) item;
			if (tree.getItems() != null) {
				for (Item i : tree.getItems()) {
					handle(medias, i);
				}
			}
		} else if (item instanceof ItemTable) {
			ItemTable table = (ItemTable) item;
			if (table.getRows() != null) {
				for (Item i : table.getRows()) {
					handle(medias, i);
				}
			}
		}
	}

	public static void handle(List<DvMultimedia> medias, Item item) {
		if (item == null)
			return;

		if (item instanceof Element) {
			Element e = (Element) item;
			if (e.getValue() != null && e.getValue() instanceof DvMultimedia) {
				// System.out.println("item=" + e.getValue());
				handle(medias, (DvMultimedia) e.getValue());
			}
		} else if (item instanceof Cluster) {
			Cluster c = (Cluster) item;
			if (c.getItems() != null) {
				for (Item i : c.getItems()) {
					handle(medias, i);
				}
			}
		}

	}

	public static void handle(List<DvMultimedia> medias, DvMultimedia m) {
		medias.add(m);
	}
}
