package com.ethercis.opt.query;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

/**
 * Created by christian on 5/9/2018.
 */
public class MetaData {

    I_KnowledgeCache knowledgeCache;
    DSLContext context;

    public MetaData(DSLContext context, I_KnowledgeCache knowledgeCache) {
        this.context = context;
        this.knowledgeCache = knowledgeCache;
    }

    public String typeForPath(String templateId, String path) throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE) knowledgeCache.retrieveTemplate(templateId);
        QueryOptMetaData queryOptMetaData = QueryOptMetaData.initialize(operationaltemplate);

        return queryOptMetaData.type(path);
    }
}
