//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.xml.datatype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.openehr.am.xml.datatype.IntervalOfDate;
import org.openehr.am.xml.datatype.IntervalOfDateTime;
import org.openehr.am.xml.datatype.IntervalOfDuration;
import org.openehr.am.xml.datatype.IntervalOfInteger;
import org.openehr.am.xml.datatype.IntervalOfReal;
import org.openehr.am.xml.datatype.IntervalOfTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "Interval",
    propOrder = {"lowerIncluded", "upperIncluded", "lowerUnbounded", "upperUnbounded"}
)
@XmlSeeAlso({IntervalOfDateTime.class, IntervalOfInteger.class, IntervalOfTime.class, IntervalOfDuration.class, IntervalOfReal.class, IntervalOfDate.class})
public abstract class Interval {
    @XmlElement(
        name = "lower_included"
    )
    protected Boolean lowerIncluded;
    @XmlElement(
        name = "upper_included"
    )
    protected Boolean upperIncluded;
    @XmlElement(
        name = "lower_unbounded"
    )
    protected boolean lowerUnbounded;
    @XmlElement(
        name = "upper_unbounded"
    )
    protected boolean upperUnbounded;

    public Interval() {
    }

    public Boolean isLowerIncluded() {
        return this.lowerIncluded;
    }

    public void setLowerIncluded(Boolean value) {
        this.lowerIncluded = value;
    }

    public Boolean isUpperIncluded() {
        return this.upperIncluded;
    }

    public void setUpperIncluded(Boolean value) {
        this.upperIncluded = value;
    }

    public boolean isLowerUnbounded() {
        return this.lowerUnbounded;
    }

    public void setLowerUnbounded(boolean value) {
        this.lowerUnbounded = value;
    }

    public boolean isUpperUnbounded() {
        return this.upperUnbounded;
    }

    public void setUpperUnbounded(boolean value) {
        this.upperUnbounded = value;
    }
}
