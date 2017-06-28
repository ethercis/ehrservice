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
    name = "IntervalOfReal",
    propOrder = {"lower", "upper"}
)
public class IntervalOfReal extends Interval {
    protected Double lower;
    protected Double upper;

    public IntervalOfReal() {
    }

    public Double getLower() {
        return this.lower;
    }

    public void setLower(Double value) {
        this.lower = value;
    }

    public Double getUpper() {
        return this.upper;
    }

    public void setUpper(Double value) {
        this.upper = value;
    }
}
