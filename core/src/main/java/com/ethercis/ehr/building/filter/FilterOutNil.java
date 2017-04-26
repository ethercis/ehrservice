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

package com.ethercis.ehr.building.filter;

import org.apache.commons.collections.ListUtils;
import org.openehr.rm.composition.content.entry.ISMTransition;
import org.openehr.schemas.v1.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by christian on 11/27/2015.
 */
public class FilterOutNil {

    public static Object filter(Object xmlObj){
        //check for residual content...
        if (xmlObj instanceof CLUSTER){
            return filter((CLUSTER)xmlObj);
        }
        else if (xmlObj instanceof ITEMLIST){
            return filter((ITEMLIST) xmlObj);
        }
        else if (xmlObj instanceof ITEMSINGLE){
            return filter((ITEMSINGLE) xmlObj);
        }
        else if (xmlObj instanceof ITEMTABLE){
            return filter((ITEMTABLE) xmlObj);
        }
        else if (xmlObj instanceof ITEMTREE){
            return filter((ITEMTREE) xmlObj);
        }
        else if (xmlObj instanceof EVALUATION){
            //check if it contains any data
            return filter((EVALUATION) xmlObj);
        }
        else if (xmlObj instanceof OBSERVATION){
            //check if it contains any data
            return filter((OBSERVATION) xmlObj);
        }
        else if (xmlObj instanceof POINTEVENT){
            return filter((POINTEVENT) xmlObj);
        }
        else if (xmlObj instanceof HISTORY){
            return filter((HISTORY)xmlObj);
        }
        else if (xmlObj instanceof  INSTRUCTION){
            return filter((INSTRUCTION) xmlObj);
        }
        else if (xmlObj instanceof  ACTION){
            return filter((ACTION) xmlObj);
        }
        else if (xmlObj instanceof  ACTIVITY){
            return filter((ACTIVITY) xmlObj);
        }
        else if (xmlObj instanceof SECTION){
            return  filter((SECTION) xmlObj);
        }
        else if (xmlObj instanceof COMPOSITION){
            return filter((COMPOSITION) xmlObj);

        }

        return xmlObj;

    }


    public static CLUSTER filter(CLUSTER cluster) {
        
        if (cluster.isNil())
            return null;
        
        if (cluster.sizeOfItemsArray() == 0){
            return null;
        }

        if (cluster.isSetArchetypeDetails() && cluster.getArchetypeDetails().isNil()){
            cluster.unsetArchetypeDetails();
        }
        if (cluster.isSetFeederAudit() && cluster.getFeederAudit().isNil()){
            cluster.unsetFeederAudit();
        }
        if (cluster.isSetUid() && cluster.getUid().isNil()){
            cluster.unsetUid();
        }       
        return cluster;
    }

    public static ITEMTREE filter(ITEMTREE itemtree){
        
        if (itemtree.isNil())
            return null;
        
        if (itemtree.sizeOfItemsArray() == 0)
            return null;

        if (itemtree.isSetArchetypeDetails() && itemtree.getArchetypeDetails().isNil()){
            itemtree.unsetArchetypeDetails();
        }
        if (itemtree.isSetFeederAudit() && itemtree.getFeederAudit().isNil()){
            itemtree.unsetFeederAudit();
        }
        if (itemtree.isSetUid() && itemtree.getUid().isNil()){
            itemtree.unsetUid();
        }
        
        return itemtree;
    }

    public static ITEMTABLE filter(ITEMTABLE itemtable){

        if (itemtable.isNil())
            return null;

        if (itemtable.sizeOfRowsArray() == 0)
            return null;

        if (itemtable.isSetArchetypeDetails() && itemtable.getArchetypeDetails().isNil()){
            itemtable.unsetArchetypeDetails();
        }
        if (itemtable.isSetFeederAudit() && itemtable.getFeederAudit().isNil()){
            itemtable.unsetFeederAudit();
        }
        if (itemtable.isSetUid() && itemtable.getUid().isNil()){
            itemtable.unsetUid();
        }

        return itemtable;
    }

    public static ITEMLIST filter(ITEMLIST itemlist){
        
        if (itemlist.isNil())
            return null;
        
        if (itemlist.sizeOfItemsArray() == 0)
            return null;

        if (itemlist.isSetArchetypeDetails() && itemlist.getArchetypeDetails().isNil()){
            itemlist.unsetArchetypeDetails();
        }
        if (itemlist.isSetFeederAudit() && itemlist.getFeederAudit().isNil()){
            itemlist.unsetFeederAudit();
        }
        if (itemlist.isSetUid() && itemlist.getUid().isNil()){
            itemlist.unsetUid();
        }

        return itemlist;
    }

    public static ITEMSINGLE filter(ITEMSINGLE itemsingle){

        if (itemsingle.isNil())
            return null;

        if (itemsingle.getItem() == null)
            return null;

        if (itemsingle.isSetArchetypeDetails() && itemsingle.getArchetypeDetails().isNil()){
            itemsingle.unsetArchetypeDetails();
        }
        if (itemsingle.isSetFeederAudit() && itemsingle.getFeederAudit().isNil()){
            itemsingle.unsetFeederAudit();
        }
        if (itemsingle.isSetUid() && itemsingle.getUid().isNil()){
            itemsingle.unsetUid();
        }

        return itemsingle;
    }

    public static EVALUATION filter(EVALUATION evaluation){
        //check if it contains any data
        if (evaluation.getData().isNil()){
            return null;
        }

        if (evaluation.isSetProtocol() && evaluation.getProtocol().isNil()){
            evaluation.unsetProtocol();
        }
        if (evaluation.isSetWorkFlowId() && evaluation.getWorkFlowId().isNil()){
            evaluation.unsetWorkFlowId();
        }
        if (evaluation.isSetArchetypeDetails() && evaluation.getArchetypeDetails().isNil()){
            evaluation.unsetArchetypeDetails();
        }
        if (evaluation.isSetProvider() && evaluation.getProvider().isNil()){
            evaluation.unsetProvider();
        }
        if (evaluation.isSetFeederAudit() && evaluation.getFeederAudit().isNil()){
            evaluation.unsetFeederAudit();
        }
        if (evaluation.isSetGuidelineId() && evaluation.getGuidelineId().isNil()){
            evaluation.unsetGuidelineId();
        }
        if (evaluation.isSetUid() && evaluation.getUid().isNil()){
            evaluation.unsetUid();
        }

        return evaluation;
    }

    public static OBSERVATION filter(OBSERVATION observation){
        //check if it contains any data

        if (observation.getData().isNil()){
            return null;
        }
        if (observation.isSetState() && observation.getState().isNil()){
            observation.unsetState();
        }
        if (observation.isSetProtocol() && observation.getProtocol().isNil()){
            observation.unsetProtocol();
        }
        if (observation.isSetWorkFlowId() && observation.getWorkFlowId().isNil()){
            observation.unsetWorkFlowId();
        }
        if (observation.isSetArchetypeDetails() && observation.getArchetypeDetails().isNil()){
            observation.unsetArchetypeDetails();
        }
        if (observation.isSetProvider() && observation.getProvider().isNil()){
            observation.unsetProvider();
        }
        if (observation.isSetFeederAudit() && observation.getFeederAudit().isNil()){
            observation.unsetFeederAudit();
        }
        if (observation.isSetGuidelineId() && observation.getGuidelineId().isNil()){
            observation.unsetGuidelineId();
        }
        if (observation.isSetUid() && observation.getUid().isNil()){
            observation.unsetUid();
        }
        return observation;
    }

    public static POINTEVENT filter(POINTEVENT pointevent){

        if (pointevent.getData().isNil())
            return null;
        
        if (pointevent.isSetState() && pointevent.getState().isNil())
            pointevent.unsetState();
        
        if (pointevent.isSetUid() && pointevent.getUid().isNil())
            pointevent.unsetUid();
        
        if (pointevent.isSetFeederAudit() && pointevent.getFeederAudit().isNil())
            pointevent.unsetFeederAudit();
        
        if (pointevent.isSetArchetypeDetails() && pointevent.getArchetypeDetails().isNil())
            pointevent.unsetArchetypeDetails();

        return pointevent;
    }

    public static HISTORY filter(HISTORY history){
        if (history.sizeOfEventsArray() == 0)
            return null;
        
        if (history.sizeOfEventsArray() == 1 && history.getEventsArray(0).isNil())
            return null;

        List<EVENT> eventList = new ArrayList<EVENT>();

        for (EVENT event: history.getEventsArray()) {
            if (!event.isNil())
                eventList.add(event);
        }
        EVENT[] events = eventList.toArray(new EVENT[0]);
        history.setEventsArray(events);
        
        if (history.isSetSummary() && history.getSummary().isNil())
            history.unsetSummary();
        
        if (history.isSetArchetypeDetails() && history.getArchetypeDetails().isNil())
            history.unsetArchetypeDetails();
        
        if (history.isSetUid() && history.getUid().isNil())
            history.unsetUid();
        
        if (history.isSetFeederAudit() && history.getFeederAudit().isNil())
            history.unsetFeederAudit();
        
        if (history.isSetDuration() && history.getDuration().isNil())
            history.unsetDuration();
        
        if (history.isSetPeriod() && history.getPeriod().isNil())
            history.unsetPeriod();

        return history;
    }

    public static INSTRUCTION filter(INSTRUCTION instruction){

        for (int i = instruction.sizeOfActivitiesArray() - 1; i >= 0; i--){
            ACTIVITY activity = instruction.getActivitiesArray(i);
            if (activity.isNil() || activity.getDescription().isNil()) {
                instruction.removeActivities(i);
            }
        }
        //more cosmetic
        if (instruction.isSetExpiryTime() && instruction.getExpiryTime().isNil())
            instruction.unsetExpiryTime();
        
        if (instruction.isSetWfDefinition() && instruction.getWfDefinition().isNil())
            instruction.unsetWfDefinition();

        if (instruction.isSetProtocol() && instruction.getProtocol().isNil()){
            instruction.unsetProtocol();
        }
        if (instruction.isSetWorkFlowId() && instruction.getWorkFlowId().isNil()){
            instruction.unsetWorkFlowId();
        }
        if (instruction.isSetArchetypeDetails() && instruction.getArchetypeDetails().isNil()) {
            instruction.unsetArchetypeDetails();
        }
        if (instruction.isSetWorkFlowId() && instruction.getWorkFlowId().isNil()){
            instruction.unsetWorkFlowId();
        }
        if (instruction.isSetArchetypeDetails() && instruction.getArchetypeDetails().isNil()){
            instruction.unsetArchetypeDetails();
        }
        if (instruction.isSetProvider() && instruction.getProvider().isNil()){
            instruction.unsetProvider();
        }
        if (instruction.isSetGuidelineId() && instruction.getGuidelineId().isNil()){
            instruction.unsetGuidelineId();
        }
        return instruction;
    }

    public static ACTION filter(ACTION action){
        
        //more cosmetic
        if (action.isSetInstructionDetails() && action.getInstructionDetails().isNil())
            action.unsetInstructionDetails();

        if (action.isSetProtocol() && action.getProtocol().isNil()){
            action.unsetProtocol();
        }
        if (action.isSetWorkFlowId() && action.getWorkFlowId().isNil()){
            action.unsetWorkFlowId();
        }
        if (action.isSetArchetypeDetails() && action.getArchetypeDetails().isNil()) {
            action.unsetArchetypeDetails();
        }
        if (action.isSetWorkFlowId() && action.getWorkFlowId().isNil()){
            action.unsetWorkFlowId();
        }
        if (action.isSetArchetypeDetails() && action.getArchetypeDetails().isNil()){
            action.unsetArchetypeDetails();
        }
        if (action.isSetProvider() && action.getProvider().isNil()){
            action.unsetProvider();
        }
        if (action.isSetGuidelineId() && action.getGuidelineId().isNil()){
            action.unsetGuidelineId();
        }
        if (action.getIsmTransition() != null){
            ISMTRANSITION ismTransition = action.getIsmTransition();
            if (ismTransition.isSetCareflowStep()){
                //check for DUMMY entry
                DVCODEDTEXT careflow = ismTransition.getCareflowStep();
                if (careflow.getValue().equals("DUMMY"))
                    ismTransition.unsetCareflowStep();
            }
        }
        if (action.getDescription() != null && ((ITEMSTRUCTURE)action.getDescription()).isNil()){
            action.setDescription(null);
            action.setNil();
        }


        return action;
    }

    public static ACTIVITY filter(ACTIVITY activity){

        //more cosmetic
        if (activity.isSetUid() && activity.getUid().isNil())
            activity.unsetUid();

        if (activity.isSetArchetypeDetails() && activity.getArchetypeDetails().isNil()) {
            activity.unsetArchetypeDetails();
        }

        if (activity.isSetArchetypeDetails() && activity.getArchetypeDetails().isNil()){
            activity.unsetArchetypeDetails();
        }

        return activity;
    }

    public static EVENT filter(EVENT event){

        //more cosmetic
        if (event.isNil())
            return null;

        return event;
    }
    
    public static SECTION filter(SECTION section){
        //check for nil items and remove them

        for (int i = section.sizeOfItemsArray() - 1; i >= 0; i--){
            CONTENTITEM contentitem = section.getItemsArray(i);
            if (contentitem.isNil()) {
                section.removeItems(i);
            }
        }

        if (section.sizeOfItemsArray() == 0)
            return null;

        if (section.isSetArchetypeDetails() && section.getArchetypeDetails().isNil())
            section.unsetArchetypeDetails();

        if (section.isSetUid() && section.getUid().isNil())
            section.unsetUid();

        if (section.isSetFeederAudit() && section.getFeederAudit().isNil())
            section.unsetFeederAudit();

        return section;
    }

    public static COMPOSITION filter(COMPOSITION composition){
        for (int i = composition.sizeOfContentArray() - 1; i >= 0; i--)
        {
            CONTENTITEM contentitem = composition.getContentArray(i);
            if (contentitem.isNil()){
                composition.removeContent(i);
            }
        }

//CHC:170426 a composition with no content is legit...
//        if (composition.sizeOfContentArray() == 0)
//            return null;
        
        if (composition.isSetArchetypeDetails() && composition.getArchetypeDetails().isNil())
            composition.unsetArchetypeDetails();

        if (composition.isSetUid() && composition.getUid().isNil())
            composition.unsetUid();

        if (composition.isSetFeederAudit() && composition.getFeederAudit().isNil())
            composition.unsetFeederAudit();

        if (composition.isSetContext() && composition.getContext().isNil())
            composition.unsetContext();

        if (composition.isSetContext()) {
            EVENTCONTEXT eventcontext = composition.getContext();
            if (eventcontext.isSetOtherContext() && eventcontext.getOtherContext().isNil())
                eventcontext.unsetOtherContext();
        }

        return composition;
    }


}
