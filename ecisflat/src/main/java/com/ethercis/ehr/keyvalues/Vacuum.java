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

package com.ethercis.ehr.keyvalues;

import com.ethercis.ehr.encode.CompositionSerializer;
import org.apache.commons.collections4.iterators.PeekingIterator;

import java.util.Map;

/**
 * Created by christian on 4/26/2017.
 */
public class Vacuum {

    public Vacuum(){}

    /**
     * eliminate all meta-data entries not related to an actual data value entry
     * @param sortedMap
     * @return
     */
    public Map<String,String> metaClean(Map<String, String> sortedMap){
        PeekingIterator<String> peekingIterator = new PeekingIterator(sortedMap.keySet().iterator());

        String previousPath = null;
        while (peekingIterator.hasNext()) {
            String path = peekingIterator.next();
            if (isMetaData(path)) { //check if next entry is an actual child of this node
                String suffix = resolveMetaDataSuffix(path);
                if (suffix != null) {
                    String pathChildTest = path.substring(0, path.lastIndexOf(suffix));
                    //do not keep this entry since it must be preceded by an actual child path holding a value field (which we cannot assert btw)
                    if (isMetaData(previousPath) || !previousPath.startsWith(pathChildTest))
                        peekingIterator.remove();
                }
            }
            previousPath = path;
        }
        return sortedMap;
    }

    private boolean isMetaData(String path){
        return path.endsWith(CompositionSerializer.TAG_TIME) || path.endsWith(CompositionSerializer.TAG_ORIGIN) || path.endsWith(CompositionSerializer.TAG_TIMING);
    }

    private String resolveMetaDataSuffix(String path){
        String suffix = null;
        if (path.endsWith(CompositionSerializer.TAG_TIME))
            suffix = CompositionSerializer.TAG_TIME;
        else if (path.endsWith(CompositionSerializer.TAG_ORIGIN))
            suffix = CompositionSerializer.TAG_ORIGIN;
        else if (path.endsWith(CompositionSerializer.TAG_TIMING))
            suffix = CompositionSerializer.TAG_TIMING;

        return suffix;
    }
}
