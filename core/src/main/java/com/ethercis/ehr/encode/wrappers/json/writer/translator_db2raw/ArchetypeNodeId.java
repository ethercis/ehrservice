package com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw;

import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;
import com.google.gson.stream.JsonWriter;


import java.io.IOException;

/**
 * Created by christian on 4/26/2018.
 */
public class ArchetypeNodeId {

    JsonWriter writer;
    String archetypeNodeId;

    public ArchetypeNodeId(JsonWriter writer, String nodeIdentifier) {
        this.writer = writer;
        this.archetypeNodeId = nodeIdentifier;
    }

    public void write() throws IOException {
        if (archetypeNodeId != null && !archetypeNodeId.isEmpty()) {
            writer.name(I_DvTypeAdapter.ARCHETYPE_NODE_ID).value(archetypeNodeId);

            if (new DomainStructure(archetypeNodeId).isArchetypeSlot())
                writer.name(I_DvTypeAdapter.AT_CLASS).value(new DomainStructure(archetypeNodeId).archetypeSlotType());
        }
    }
}
