package com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * Created by christian on 3/12/2018.
 */
public class ArrayChildren {

    ArrayList arrayList;

    public ArrayChildren(ArrayList value) {
        this.arrayList = value;
    }

    public boolean hasElement(){
        for (Object entry: arrayList){
            if (entry instanceof LinkedTreeMap){
                LinkedTreeMap itemMap = (LinkedTreeMap)entry;
                if (itemMap.containsKey(CompositionSerializer.TAG_PATH))
                    return true;
            }
        }
        return false;
    }
}
