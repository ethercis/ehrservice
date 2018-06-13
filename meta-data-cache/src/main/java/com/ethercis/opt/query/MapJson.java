package com.ethercis.opt.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by christian on 5/10/2018.
 */
public class MapJson {

    Map<String, Object> jsonMap;

    public MapJson(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }


    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(jsonMap);
    }
}
