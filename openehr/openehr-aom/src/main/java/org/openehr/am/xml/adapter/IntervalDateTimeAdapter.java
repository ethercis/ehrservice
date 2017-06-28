//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.openehr.am.xml.datatype.IntervalOfDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.support.basic.Interval;

public class IntervalDateTimeAdapter extends XmlAdapter<IntervalOfDate, Interval<DvDateTime>> {
    public IntervalDateTimeAdapter() {
    }

    public Interval<DvDateTime> unmarshal(IntervalOfDate v) throws Exception {
        Interval interval = new Interval();
        interval.setLower(new DvDateTime(v.getLower()));
        interval.setLowerIncluded(v.isLowerIncluded().booleanValue());
        interval.setUpper(new DvDateTime(v.getUpper()));
        interval.setUpperIncluded(v.isUpperIncluded().booleanValue());
        return interval;
    }

    public IntervalOfDate marshal(Interval<DvDateTime> v) throws Exception {
        IntervalOfDate interval = new IntervalOfDate();
        interval.setLower(((DvDateTime)v.getLower()).getValue());
        interval.setLowerIncluded(Boolean.valueOf(v.isLowerIncluded()));
        interval.setUpper(((DvDateTime)v.getUpper()).getValue());
        interval.setUpperIncluded(Boolean.valueOf(v.isUpperIncluded()));
        return interval;
    }
}
