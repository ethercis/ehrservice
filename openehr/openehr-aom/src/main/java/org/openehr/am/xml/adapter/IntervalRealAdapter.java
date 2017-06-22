//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfReal;
import org.openehr.rm.support.basic.Interval;

public class IntervalRealAdapter extends XmlAdapter<IntervalOfReal, Interval<Double>> {
    public IntervalRealAdapter() {
    }

    public Interval<Double> unmarshal(IntervalOfReal v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(v.getLower());
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(v.getUpper());
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfReal marshal(Interval<Double> v) throws Exception {
        IntervalOfReal interval = new IntervalOfReal();
        interval.setLower((Double)v.getLower());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper((Double)v.getUpper());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
