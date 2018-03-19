package com.ethercis.opt.mapper;

import org.openehr.schemas.v1.*;

/**
 * Created by christian on 3/16/2018.
 */
public class NodeNameAttribute {

    CATTRIBUTE cattribute;

    public NodeNameAttribute(CATTRIBUTE cattribute) {
        this.cattribute = cattribute;
    }

    public String staticName(){
        for (COBJECT cobject: cattribute.getChildrenArray()){
            if (cobject instanceof CCOMPLEXOBJECT) {
                CCOMPLEXOBJECT ccomplexobject = (CCOMPLEXOBJECT) cobject;
                for (CATTRIBUTE cattribute1 : ccomplexobject.getAttributesArray()) {
                    if (cattribute1 instanceof CSINGLEATTRIBUTE) {
                        CSINGLEATTRIBUTE csingleattribute = (CSINGLEATTRIBUTE) cattribute1;
                        for (COBJECT cobject1 : csingleattribute.getChildrenArray()) {
                            if (cobject1 instanceof CPRIMITIVEOBJECT) {
                                CPRIMITIVEOBJECT cprimitiveobject = (CPRIMITIVEOBJECT) cobject1;
                                if (cprimitiveobject.getRmTypeName().equals(Constants.STRING)) {
                                    if (cprimitiveobject.getItem() != null && cprimitiveobject.getItem() instanceof CSTRING) {
                                        CSTRING cstring = (CSTRING) cprimitiveobject.getItem();
                                        return cstring.getListArray()[0].replaceAll("[^A-Za-z0-9 _]", "");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
