package com.ethercis.aql.sql.binding;

import com.ethercis.aql.compiler.QueryParser;
import org.jooq.SelectQuery;

/**
 * Created by christian on 4/27/2018.
 */
public class LimitBinding {
    QueryParser queryParser;
    SelectQuery selectQuery;

    public LimitBinding(QueryParser queryParser, SelectQuery selectQuery) {
        this.queryParser = queryParser;
        this.selectQuery = selectQuery;
    }

    public SelectQuery bind(){
        if (queryParser.getLimitAttribute() != null || queryParser.getOffsetAttribute() != null)
            selectQuery.addLimit(queryParser.getOffsetAttribute() == null ? 0 : queryParser.getOffsetAttribute(),
                    queryParser.getLimitAttribute() == null ? 0 : queryParser.getLimitAttribute());

        return selectQuery;
    }
}
