/*
 * Copyright (c) Ripple Foundation CIC Ltd, UK, 2017
 * Author: Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ethercis.opt;

import com.ethercis.aql.sql.binding.I_JoinBinder;
import com.ethercis.aql.sql.queryImpl.JsonbEntryQuery;
import com.ethercis.ehr.encode.CompositionSerializer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.ethercis.jooq.pg.Tables.ENTRY;
import static com.ethercis.jooq.pg.Tables.EVENT_CONTEXT;
import static com.ethercis.jooq.pg.Tables.STATUS;

/**
 * Created by christian on 2/15/2018.
 */
public class JsonbQuery {

    private enum PATH_PART {IDENTIFIER_PATH_PART, VARIABLE_PATH_PART}

    private enum ITEM_TYPE {COMPOSITION, OTHER_DETAILS, OTHER_CONTEXT, OTHER}

    private static final String SELECT_EHR_OTHER_DETAILS_MACRO = I_JoinBinder.statusRecordTable.field(STATUS.OTHER_DETAILS)+"->('"+ CompositionSerializer.TAG_OTHER_DETAILS+"')";
    private final static String JSONBSelector_EHR_OTHER_DETAILS_OPEN = SELECT_EHR_OTHER_DETAILS_MACRO +" #>> '{";
    private static final String SELECT_EHR_OTHER_CONTEXT_MACRO = EVENT_CONTEXT.OTHER_CONTEXT+"->('"+CompositionSerializer.TAG_OTHER_CONTEXT+"[at0001]"+"')";
    private final static String JSONBSelector_EHR_OTHER_CONTEXT_OPEN = SELECT_EHR_OTHER_CONTEXT_MACRO +" #>> '{";
    private final static String JSONBSelector_CLOSE = "}'";
    private final static String JSONBSelector_COMPOSITION_OPEN = ENTRY.ENTRY_ +" #>> '{";

    String itemPath;
    ITEM_TYPE type;

    public JsonbQuery(String itemPath) {
        this.itemPath = itemPath;

        if (itemPath.contains("other_context"))
            type = ITEM_TYPE.OTHER_CONTEXT;
        else if (itemPath.contains("other_details"))
            type = ITEM_TYPE.OTHER_DETAILS;
        else
            type = ITEM_TYPE.COMPOSITION;
    }

    private String wrap(String itemPath, String open, String close){
        if (itemPath.contains("/item_count")){
            //trim the last array index in the prefix
            //look ahead for an index expression: ','<nnn>','
            String[] segments = itemPath.split("(?=(,[0-9]*,))");
            //trim the last index expression
            String pathPart = StringUtils.join(ArrayUtils.subarray(segments, 0, segments.length - 1));
            return "jsonb_array_length(content #> '{"+pathPart+"}')";
        }
        else
            return open +itemPath+ close;

    }

    public String generate(){

        String sqlPath;

        List<String> segments = JsonbEntryQuery.jqueryPath(JsonbEntryQuery.PATH_PART.VARIABLE_PATH_PART, itemPath, "0");
        segments = new JsonbItemArray(segments).resolveArrayIndex();
        String itemPath = StringUtils.join(segments.toArray(new String[]{}), ",");

        sqlPath = wrap(itemPath, type.equals(ITEM_TYPE.COMPOSITION) ?
                        JSONBSelector_COMPOSITION_OPEN :
                        (type.equals(ITEM_TYPE.OTHER_DETAILS) ?
                            JSONBSelector_EHR_OTHER_DETAILS_OPEN :
                            JSONBSelector_EHR_OTHER_CONTEXT_OPEN
                        ),
                JSONBSelector_CLOSE);

        if (segments.get(segments.size() - 1).contains("magnitude")){ //force explicit type cast for DvQuantity
            sqlPath = "("+sqlPath+")::float";
        }

        return sqlPath;

    }


}
