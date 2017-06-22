//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfTime;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.support.basic.Interval;

public class IntervalTimeAdapter extends XmlAdapter<IntervalOfTime, Interval<DvTime>> {
    public IntervalTimeAdapter() {
    }

    public Interval<DvTime> unmarshal(IntervalOfTime v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(new DvTime(v.getLower()));
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(new DvTime(v.getUpper()));
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfTime marshal(Interval<DvTime> v) throws Exception {
        IntervalOfTime interval = new IntervalOfTime();
        interval.setLower(((DvTime)v.getLower()).getValue());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper(((DvTime)v.getUpper()).getValue());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
