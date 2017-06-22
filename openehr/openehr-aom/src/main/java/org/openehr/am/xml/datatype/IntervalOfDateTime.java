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
    name = "IntervalOfDateTime",
    propOrder = {"lower", "upper"}
)
public class IntervalOfDateTime extends Interval {
    protected String lower;
    protected String upper;

    public IntervalOfDateTime() {
    }

    public String getLower() {
        return this.lower;
    }

    public void setLower(String value) {
        this.lower = value;
    }

    public String getUpper() {
        return this.upper;
    }

    public void setUpper(String value) {
        this.upper = value;
    }
}
