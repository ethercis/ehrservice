package com.ethercis.ehr.encode.rawjson;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 3/12/2018.
 */
public class CompositionMap {

    Map<String, Object> fromDBMap;

    public CompositionMap(Map<String, Object> fromDBMap) {
        this.fromDBMap = fromDBMap;
    }

    public LinkedTreeMap<String, Object> restructure() {

        LinkedTreeMap<String, Object> structuredMap = new LinkedTreeMap<>();

        for (Object entry : fromDBMap.entrySet()) {
            if (entry instanceof Map.Entry) {
                String key = (String) ((Map.Entry) entry).getKey();
                if (key.equals("content")) {
                    //build an array
                    Map<String, Object> contentMap = (Map<String, Object>) ((Map.Entry) entry).getValue();
                    Map<String, Object> nameMapOut = new LinkedTreeMap<>();
                    String contentKey = null;
                    for (Object item : contentMap.entrySet()) {
                        if (item instanceof Map.Entry) {
                            //get the composition entry in this content
                            String itemKey = (String) ((Map.Entry) item).getKey();
                            if (itemKey.contains(CompositionSerializer.TAG_COMPOSITION)) {
                                Map<String, Object> itemMap = (Map<String, Object>) ((Map.Entry) item).getValue();
                                //save the key of the child to later put its name
                                for (String nodePath : itemMap.keySet()) {
                                    contentKey = nodePath;
                                }
                                structuredMap.putAll(itemMap);
//                                structuredMap.put(itemKey, contentList);
                            } else if (itemKey.equals(CompositionSerializer.TAG_NAME)) {
                                nameMapOut.put(CompositionSerializer.TAG_NAME, ((Map.Entry) item).getValue());
                            } else
                                throw new IllegalArgumentException("not sure what to do with this item...");
                        }
                    }
                    if ((structuredMap.get(contentKey) instanceof List)
                            && (((List) structuredMap.get(contentKey)).size()) > 0
                            && (((List) structuredMap.get(contentKey)).get(0)) instanceof Map)
                        ((Map) ((List) structuredMap.get(contentKey)).get(0)).putAll(nameMapOut);

                } else {
                    structuredMap.put(key, ((Map.Entry) entry).getValue());
                }
            }
        }

        return structuredMap;
    }

    private LinkedTreeMap<String, Object> factorizeContent(LinkedTreeMap<String, Object> structuredMap){
        List<Object> contentList = new ArrayList<>();
        LinkedTreeMap<String, Object> resultMap = new LinkedTreeMap<>();

        for (Object item : structuredMap.entrySet()) {
            String itemKey = (String) ((Map.Entry) item).getKey();
            Object itemValue = ((Map.Entry) item).getValue();
            LinkedTreeMap<String, Object> newMap = new LinkedTreeMap<>();
            newMap.put(itemKey, itemValue);
            if (itemKey.contains(CompositionSerializer.TAG_CONTENT)){
                contentList.add(newMap);
            }
            else {
                resultMap.putAll(newMap);
            }
        }

        if (!contentList.isEmpty()){
            resultMap.put("content", contentList);
        }

        return resultMap;
    }
}
