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

package com.ethercis.ehr.keyvalues.serializer;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.keyvalues.I_PathValue;
import org.openehr.rm.RMObject;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by christian on 5/17/2017.
 */
public class SerializeRmObject implements I_EcisFlatSerializer {

    public static final String TAG_OBJECT =  "$OBJECT$";
    private String[] structuralClasses = {"Element","Cluster","ItemSingle","ItemList","ItemTable","ItemTree","History","IntervalEvent","PointEvent"};

    RMObject rmObject;

    public SerializeRmObject(RMObject rmObject) {
        this.rmObject = rmObject;
    }

    @Override
    public Map<String, String> valueMap(String path, Map<String, Object> map) {
        Map<String, String> retMap = new TreeMap<>();
        boolean composite = false;
        if (map.containsKey(CompositionSerializer.TAG_NAME)) {
            Object nameAttribute = map.get(CompositionSerializer.TAG_NAME);
            if (nameAttribute instanceof String)
                retMap.put(path + I_PathValue.NAME_SUBTAG, nameAttribute.toString());
            else if (nameAttribute instanceof Map)
                retMap.put(path + I_PathValue.NAME_SUBTAG, ((Map)nameAttribute).get("value").toString());
            composite = true;
        }
        if (map.containsKey(CompositionSerializer.TAG_DEFINING_CODE)){
            retMap.put(path + I_PathValue.DEFINING_CODE_SUBTAG, ((CodePhrase)map.get(CompositionSerializer.TAG_DEFINING_CODE)).toString());
            composite = true;
        }
        if (composite){
            String classname = (String)map.get(CompositionSerializer.TAG_CLASS);
            if (!isStructural(classname)) {
                if (map.containsKey(TAG_OBJECT)){
//                            Object = (DataValue) map.get(TAG_OBJECT);
                    retMap.put(path + I_PathValue.VALUE_SUBTAG, map.get(TAG_OBJECT).toString());
                }
                else
                    retMap.put(path + I_PathValue.VALUE_SUBTAG, rmObject.toString());
            }
            else
                retMap.put(path + I_PathValue.VALUE_SUBTAG, rmObject.toString());
        }
        else {
            retMap.put(path, rmObject.toString());
        }
        return retMap;
    }

    private boolean isStructural(String classname){
        return Arrays.asList(structuralClasses).contains(classname);
    }
}
