/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
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

package com.ethercis.opt;

import java.util.List;

/**
 * Created by christian on 2/15/2018.
 */
public class JsonbItemArray {

    List<String> itemPathArray;

    public JsonbItemArray(List<String> itemPathArray) {
        this.itemPathArray = itemPathArray;
    }

    public List<String> resolveArrayIndex() {

        for (int i=0; i < itemPathArray.size(); i++ ){
            String nodeId = itemPathArray.get(i);
            if (nodeId.contains("#")){
                Integer index = retrieveIndex(nodeId);
                //change the default index of the previous one
                if (i - 1 >= 0){
                    itemPathArray.set(i - 1, index.toString());
                }
                //remove to the name/value short cut part...
                String nodeIdTrimmed = nodeId.split(",")[0]+"]";
                itemPathArray.set(i, nodeIdTrimmed);
            }
        }

        return itemPathArray;
    }

    public static int retrieveIndex(String nodeId) {
        if (nodeId.contains("#")){
            Integer indexValue = Integer.valueOf((nodeId.split("#")[1]).split("']")[0]);
            return indexValue;
        }
        return 0;
    }
}
