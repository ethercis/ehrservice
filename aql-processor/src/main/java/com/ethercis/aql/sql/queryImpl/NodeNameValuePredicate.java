package com.ethercis.aql.sql.queryImpl;

import com.ethercis.aql.sql.queryImpl.value_field.NodePredicate;

import java.util.List;

/**
 * Created by christian on 5/9/2018.
 */
public class NodeNameValuePredicate {

    NodePredicate nodePredicate;

    public NodeNameValuePredicate(NodePredicate nodePredicate) {
        this.nodePredicate = nodePredicate;
    }

    public List<String> path(List<String> jqueryPath, String nodeId){
        //do the formatting to allow name/value node predicate processing
        String predicate = nodePredicate.predicate();
        jqueryPath.add(new NodePredicate(nodeId).removeNameValuePredicate());
        //encode it to prepare for plpgsql function call: marker followed by the name/value predicate
        jqueryPath.add(I_QueryImpl.AQL_NODE_NAME_PREDICATE_MARKER);
        jqueryPath.add(predicate);

        return jqueryPath;
    }

}
