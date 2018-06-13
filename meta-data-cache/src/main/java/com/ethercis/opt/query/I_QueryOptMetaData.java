package com.ethercis.opt.query;

import java.util.List;

/**
 * Created by christian on 5/14/2018.
 */
public interface I_QueryOptMetaData {
    List upperNotBounded();

    String type(String path);

    List nodeByFieldValue(String field, String value);

    List nodeFieldRegexp(String field, String regexp);

    Object getJsonPathVisitor();

    String getTemplateConcept();

    String getTemplateId();
}
