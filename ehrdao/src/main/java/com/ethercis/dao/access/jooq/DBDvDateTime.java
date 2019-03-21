package com.ethercis.dao.access.jooq;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;

import java.sql.Timestamp;

public class DBDvDateTime {

    Timestamp timestamp;
    String timezone;
    final static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    public DBDvDateTime(Timestamp timestamp, String timezone) {
        this.timestamp = timestamp;
        this.timezone = timezone;
    }

    public DvDateTime decode(){
        if (timestamp == null) return null;
        DateTime codedDateTime;

        if (timezone != null)
            codedDateTime = new DateTime(timestamp, DateTimeZone.forID(timezone));
        else
            codedDateTime = new DateTime(timestamp);

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat);
        String convertedDateTime = dateTimeFormatter.print(codedDateTime);
        return new DvDateTime().parse(convertedDateTime);
    }
}
