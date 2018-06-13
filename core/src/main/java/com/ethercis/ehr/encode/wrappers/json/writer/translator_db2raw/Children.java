package com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by christian on 3/13/2018.
 */
public class Children {

    LinkedTreeMap<String, Object> linkedTreeMap;

    public Children(LinkedTreeMap<String, Object> linkedTreeMap) {
        this.linkedTreeMap = linkedTreeMap;
    }

    public boolean isItemsOnly(){
        boolean isItems = true;

        for (String key : linkedTreeMap.keySet()) {
            if (!key.startsWith(CompositionSerializer.TAG_ITEMS) && !key.startsWith(CompositionSerializer.TAG_NAME)){
                isItems = false;
            }
        }
        return isItems;
    }

    public boolean isMultiContent(){
        int contents = 0;

        for (String key : linkedTreeMap.keySet()) {
            if (key.startsWith(CompositionSerializer.TAG_CONTENT)){
                contents++;
            }
        }
        return contents > 0;
    }

    public int contentCount(){
        int contents = 0;

        for (String key : linkedTreeMap.keySet()) {
            if (key.startsWith(CompositionSerializer.TAG_CONTENT)){
                contents++;
            }
        }
        return contents;
    }
}
