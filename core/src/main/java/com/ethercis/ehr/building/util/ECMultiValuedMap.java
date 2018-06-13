package com.ethercis.ehr.building.util;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.Map;

/**
 * Created by christian on 5/16/2018.
 */
public class ECMultiValuedMap {

    public HashSetValuedHashMap<String, Object> getInstance() {
        return new HashSetValuedHashMap<>();
    }
}
