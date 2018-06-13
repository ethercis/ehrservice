package com.ethercis.aql.sql.queryImpl;

import java.util.List;

/**
 * Created by christian on 5/9/2018.
 */
public class SegmentedPath {

    List<String> segmentedPath;

    public SegmentedPath(List<String> segmentedPath) {
        this.segmentedPath = segmentedPath;
    }

    public String reduce(){

        StringBuffer stringBuffer = new StringBuffer();

        for (String segment: segmentedPath){

            if (segment.startsWith("/composition"))
                continue;
            if (segment.startsWith("/value"))
                continue;
            if (!segment.contains("[") && !segment.contains("]"))
                continue;
            if (!segment.startsWith("/"))
                continue;
            stringBuffer.append(segment);
        }

        return stringBuffer.toString();

    }
}
