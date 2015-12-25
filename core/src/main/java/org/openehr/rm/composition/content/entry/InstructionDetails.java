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
package org.openehr.rm.composition.content.entry;

import org.apache.commons.lang.StringUtils;
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.RMObject;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.support.identification.LocatableRef;

/**
 * Used to record details of the Instruction causing an Action.
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public class InstructionDetails extends RMObject {

	/**
	 * @param uid
	 * @param archetypeNodeId
	 * @param name
	 * @param archetypeDetails
	 * @param feederAudit
	 * @param links
	 * @param parent
	 */
	@FullConstructor
	public InstructionDetails(
            @Attribute(name = "instructionId", required = true) LocatableRef instructionId,
            @Attribute(name = "activityId", required = true) String activityId,
            @Attribute(name = "wfDetails") ItemStructure wfDetails) {
		if (instructionId == null) {
			throw new IllegalArgumentException("null instructionId");
		}
		if (StringUtils.isEmpty(activityId)) {
			throw new IllegalArgumentException("null or empty activityId");
		}
		this.instructionId =instructionId;
		this.activityId = activityId;
		this.wfDetails = wfDetails;
	}

	/**
	 * Identifier of Activity within Instruction, in the form of its archetype 
	 * path.
	 * 
	 * @return activityId
	 */
	public String getActivityId() {
		return activityId;
	}

	/**
	 * Id of causing Instruction
	 * 
	 * @return instructionId
	 */
	public LocatableRef getInstructionId() {
		return instructionId;
	}

	/**
	 * Various workflow engine state details, potentially including such things as:
	 * -condition that fired to cause this Action to be done
	 * -list of notifications which actually occured
	 * -other workflow engine state
	 * 
	 * @return wfDetails
	 */
	public ItemStructure getWfDetails() {
		return wfDetails;
	}

	//POJO start
	InstructionDetails() {
	}

	public void setActivityID(String activityId) {
		this.activityId = activityId;
	}

	public void setInstructionId(LocatableRef instructionId) {
		this.instructionId = instructionId;
	}

	public void setWfDetails(ItemStructure wfDetails) {
		this.wfDetails = wfDetails;
	}
	//POJO end
	
	/* fields */
	private LocatableRef instructionId;
	private String activityId;
	private ItemStructure wfDetails;

}

