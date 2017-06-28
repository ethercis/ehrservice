//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.openehr.am.xml.datatype.Interval;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "IntervalOfInteger",
    propOrder = {"lower", "upper"}
)
public class IntervalOfInteger extends Interval {
    protected Integer lower;
    protected Integer upper;

    public IntervalOfInteger() {
    }

    public Integer getLower() {
        return this.lower;
    }

    public void setLower(Integer value) {
        this.lower = value;
    }

    public Integer getUpper() {
        return this.upper;
    }

    public void setUpper(Integer value) {
        this.upper = value;
    }
}
