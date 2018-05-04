package com.ethercis.aql.sql.queryImpl;

import com.ethercis.aql.sql.queryImpl.value_field.NodePredicate;
import org.openehr.rm.common.archetyped.Locatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christian on 5/3/2018.
 */
public class VariablePath {

    String path;

    public VariablePath(String path) {
        this.path = path;
    }

    public boolean hasPredicate() {

        if (path == null)
            return false;

        List<String> segments = Locatable.dividePathIntoSegments(path);
        for (int i = 0; i < segments.size(); i++) {
            String nodeId = segments.get(i);
            if (new NodePredicate(nodeId).hasPredicate())
                return true;
        }

        return false;
    }
}
