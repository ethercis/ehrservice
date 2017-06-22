//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.support.basic.Interval;

public class IntervalDurationAdapter extends XmlAdapter<IntervalOfDuration, Interval<DvDuration>> {
    public IntervalDurationAdapter() {
    }

    public Interval<DvDuration> unmarshal(IntervalOfDuration v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(new DvDuration(v.getLower()));
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(new DvDuration(v.getUpper()));
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfDuration marshal(Interval<DvDuration> v) throws Exception {
        IntervalOfDuration interval = new IntervalOfDuration();
        interval.setLower(((DvDuration)v.getLower()).getValue());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper(((DvDuration)v.getUpper()).getValue());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
