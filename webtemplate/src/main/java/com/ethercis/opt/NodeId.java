package com.ethercis.opt;

import com.ethercis.ehr.encode.wrappers.SnakeCase;

/**
 * Created by christian on 3/8/2018.
 */
public class NodeId {

    String name;

    public NodeId(String name) {
        this.name = name;
    }

    /**
     * transform the name into an ehrscape pseudo id
     * @return
     */
    public String ehrscape(){
        String ehrscapeId = name.replaceAll("/| |-", "_").toLowerCase();

        return new SnakeCase(ehrscapeId).camelToSnake();
    }
}
