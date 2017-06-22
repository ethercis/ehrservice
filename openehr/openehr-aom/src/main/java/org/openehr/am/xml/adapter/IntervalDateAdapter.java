//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.support.basic.Interval;

public class IntervalDateAdapter extends XmlAdapter<IntervalOfDate, Interval<DvDate>> {
    public IntervalDateAdapter() {
    }

    public Interval<DvDate> unmarshal(IntervalOfDate v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(new DvDate(v.getLower()));
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(new DvDate(v.getUpper()));
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfDate marshal(Interval<DvDate> v) throws Exception {
        IntervalOfDate interval = new IntervalOfDate();
        interval.setLower(((DvDate)v.getLower()).getValue());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper(((DvDate)v.getUpper()).getValue());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
