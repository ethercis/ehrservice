package com.ethercis.aql.sql.queryImpl.value_field;

/**
 * Created by christian on 5/2/2018.
 */
public class NodePredicate {

    private static final String namedItemPrefix = " and name/value='";
    String nodeId;

    public NodePredicate(String nodeId) {
        this.nodeId = nodeId;
    }

    public String removeNameValuePredicate(){

        String retNodeId = nodeId;

        if (retNodeId.contains(namedItemPrefix)) {
            retNodeId = retNodeId.substring(0, retNodeId.indexOf(namedItemPrefix)) + "]";
        } else if (retNodeId.contains(",")) {
            retNodeId = retNodeId.substring(0, retNodeId.indexOf(",")) + "]";
        }

        return retNodeId;
    }

    public String predicate() {
        String predicate = null;

        if (nodeId.contains(namedItemPrefix)) {
            predicate = nodeId.substring(nodeId.indexOf(namedItemPrefix)+namedItemPrefix.length(), nodeId.indexOf("]"));
        } else if (nodeId.contains(",")) {
            predicate = nodeId.substring(nodeId.indexOf(",")+1, nodeId.indexOf("]"));
        }

        return predicate;
    }

    public boolean hasPredicate() {

        boolean retval = false;

        if (nodeId.contains(namedItemPrefix) || nodeId.contains(",")) {
            retval = true;
        }

        return retval;
    }
}
