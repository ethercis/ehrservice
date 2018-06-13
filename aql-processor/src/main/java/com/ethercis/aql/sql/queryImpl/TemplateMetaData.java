package com.ethercis.aql.sql.queryImpl;

import com.ethercis.opt.query.I_IntrospectCache;

/**
 * Created by christian on 5/9/2018.
 */
public abstract class TemplateMetaData {

    protected I_IntrospectCache introspectCache;

    public TemplateMetaData(I_IntrospectCache introspectCache) {
        this.introspectCache = introspectCache;
    }
}
