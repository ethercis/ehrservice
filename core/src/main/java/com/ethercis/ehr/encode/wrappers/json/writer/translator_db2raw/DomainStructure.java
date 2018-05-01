package com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw;

import com.ethercis.ehr.encode.CompositionSerializer;

/**
 * Created by christian on 4/26/2018.
 */
public class DomainStructure {

    public static final String OPEN_EHR_EHR = "openEHR-EHR-";
    String nodeIdentifier;

    public DomainStructure(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    /**
     * is it a composition structure element?
     * @return
     */
    public boolean isArchetypeSlot(){
        return nodeIdentifier.contains(OPEN_EHR_EHR);
    }

    public String archetypeSlotType(){

        String type;

        if (nodeIdentifier.equals(CompositionSerializer.TAG_EVENTS))
            type = "POINT_EVENT";
        else if (nodeIdentifier.equals(CompositionSerializer.TAG_ACTIVITIES))
            type = "ACTIVITY";
        else
            type = nodeIdentifier.substring(OPEN_EHR_EHR.length(), nodeIdentifier.indexOf("."));

        return type;
    }

}
