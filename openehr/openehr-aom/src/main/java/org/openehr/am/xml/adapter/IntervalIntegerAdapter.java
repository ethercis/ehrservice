//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfInteger;
import org.openehr.rm.support.basic.Interval;

public class IntervalIntegerAdapter extends XmlAdapter<IntervalOfInteger, Interval<Integer>> {
    public IntervalIntegerAdapter() {
    }

    public Interval<Integer> unmarshal(IntervalOfInteger v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(v.getLower());
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(v.getUpper());
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfInteger marshal(Interval<Integer> v) throws Exception {
        IntervalOfInteger interval = new IntervalOfInteger();
        interval.setLower((Integer)v.getLower());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper((Integer)v.getUpper());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
