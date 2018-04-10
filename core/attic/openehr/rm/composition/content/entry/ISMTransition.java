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

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.RMObject;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.terminology.*;

/**
 * Model of a transition in the Instruction State machine, caused by a careflow step.
 * The attributes document the careflow step as well as the ISM transition.
 *
 * @author Yin Su Lim
 * @version 1.0
 */
public final class ISMTransition extends RMObject {

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
	public ISMTransition(
            @Attribute(name = "currentState", required = true) DvCodedText currentState,
            @Attribute(name = "transition") DvCodedText transition,
            @Attribute(name = "careflowStep") DvCodedText careflowStep,
            @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService)  {
		if (currentState == null) {
			throw new IllegalArgumentException("null currentState");
		}
		if (terminologyService == null) {
			throw new IllegalArgumentException("null terminologyService");
		}
		if (!terminologyService.terminology(TerminologyService.OPENEHR)
                        .codesForGroupName(OpenEHRTerminologyGroupIdentifiers
                        		.INSTRUCTION_STATES.getValue(), "en")
                        .contains(currentState.getDefiningCode())) {
			throw new IllegalArgumentException("unknown currentState:" + currentState);
		}
		if (transition != null && !terminologyService.terminology(TerminologyService.OPENEHR)
                        .codesForGroupName(OpenEHRTerminologyGroupIdentifiers
                        		.INSTRUCTION_TRANSITIONS.getValue(), "en")
                        .contains(transition.getDefiningCode())) {
			throw new IllegalArgumentException("unknown transition:" + transition);
		}
		this.currentState = currentState;
		this.transition = transition;
		this.careflowStep = careflowStep;		
	}

	/**
	 * The step in the careflow process which occurred as part of generating
	 * this action, e.g. "dispense", "start_administration". This attribute 
	 * represents the clinical label for the activity, as opposed to 
	 * currentState which represents the state machine computable form.
	 * 
	 * @return careflowStep
	 */
	public DvCodedText getCareflowStep() {
		return careflowStep;
	}

	/**
	 * The ISM current state. Coded by openEHR terminology group "ISM states"
	 * 
	 * @return currentState
	 */
	public DvCodedText getCurrentState() {
		return currentState;
	}

	/**
	 * The ISM transition which occurred to arrive in the currentState. Coded by 
	 * openEHR terminology group "ISM transitions"
	 * 
	 * @return transition
	 */
	public DvCodedText getTransition() {
		return transition;
	}

	//POJO start
	ISMTransition() {
	}

	public void setCareflowStep(DvCodedText careflowStep) {
		this.careflowStep = careflowStep;
	}

	public void setCurrentState(DvCodedText currentState) {
		this.currentState = currentState;
	}

	public void setTransition(DvCodedText transition) {
		this.transition = transition;
	}
	//POJO end

	/* fields */
	private DvCodedText currentState;
	private DvCodedText transition;
	private DvCodedText careflowStep;

}

