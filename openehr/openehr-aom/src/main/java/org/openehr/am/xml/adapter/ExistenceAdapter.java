//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.archetype.constraintmodel.CAttribute.Existence;
import org.openehr.am.xml.datatype.IntervalOfInteger;

public class ExistenceAdapter extends XmlAdapter<IntervalOfInteger, Existence> {
    public ExistenceAdapter() {
    }

    public Existence unmarshal(IntervalOfInteger v) throws Exception {
        int lower = v.getLower().intValue();
        int upper = v.getUpper().intValue();
        if(!v.isLowerIncluded().booleanValue()) {
            ++lower;
        }

        if(!v.isUpperIncluded().booleanValue()) {
            --upper;
        }

        return lower > 0 && upper > 0?Existence.REQUIRED:(lower == 0 && upper > 0?Existence.OPTIONAL:Existence.NOT_ALLOWED);
    }

    public IntervalOfInteger marshal(Existence v) throws Exception {
        IntervalOfInteger result = new IntervalOfInteger();
        if(v.equals(Existence.REQUIRED)) {
            result.setLower(Integer.valueOf(1));
            result.setUpper(Integer.valueOf(1));
        } else if(v.equals(Existence.OPTIONAL)) {
            result.setLower(Integer.valueOf(0));
            result.setUpper(Integer.valueOf(1));
        } else {
            result.setLower(Integer.valueOf(0));
            result.setUpper(Integer.valueOf(0));
        }

        result.setLowerIncluded(Boolean.valueOf(true));
        result.setUpperIncluded(Boolean.valueOf(true));
        return result;
    }
}
