package com.ethercis.aql.sql.binding;

import com.ethercis.ehr.encode.I_CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.json.I_DvTypeAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christian on 3/12/2018.
 */
public class CompositionVersionedUuid {

    String uuid;

    public CompositionVersionedUuid(String uuid) {
        this.uuid = uuid;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> uidMap = new HashMap<>();
        Map<String, String> uidDef = new HashMap<>();

        uidMap.put("uid", uidDef);

        uidDef.put(I_DvTypeAdapter.AT_CLASS, "OBJECT_VERSION_ID");
        uidDef.put("value", uuid);

        return uidMap;

    }
}
