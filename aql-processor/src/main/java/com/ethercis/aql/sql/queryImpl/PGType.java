package com.ethercis.aql.sql.queryImpl;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by christian on 5/9/2018.
 */
public class PGType {

    List<String> segmentedPath;

    public PGType(List<String> segmentedPath) {
        this.segmentedPath = segmentedPath;
    }

    public String forRmType(String type) {
        String attribute = segmentedPath.get(segmentedPath.size() - 1);
        String pgtype = null;

        switch (type) {
            case "DV_QUANTITY":
                if (StringUtils.endsWith(attribute, "magnitude"))
                    pgtype = "real";
                break;
            case "DV_PROPORTION":
                if (StringUtils.endsWith(attribute, "numerator"))
                    pgtype = "real";
                else if (StringUtils.endsWith(attribute, "denominator"))
                    pgtype = "real";
                break;
            case "DV_COUNT":
                if (StringUtils.endsWith(attribute, "magnitude"))
                    pgtype = "int8";
                break;
            case "DV_ORDINAL":
                if (StringUtils.endsWith(attribute, "value"))
                    pgtype = "int8";
                break;
        }

        return pgtype;
    }
}
