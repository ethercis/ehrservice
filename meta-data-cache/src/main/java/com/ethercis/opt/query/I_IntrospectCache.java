package com.ethercis.opt.query;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by christian on 5/14/2018.
 */
public interface I_IntrospectCache {
    I_QueryOptMetaData visitor(UUID uuid) throws Exception;

    I_QueryOptMetaData visitor(String templateId) throws Exception;

    IntrospectCache synchronize() throws Exception;

    IntrospectCache synchronize(UUID templateId) throws Exception;

    IntrospectCache synchronize(String templateId) throws Exception;

    IntrospectCache load();

    int invalidateDBCache() throws Exception;

    IntrospectCache invalidate();

    IntrospectCache invalidate(String templateId);

    IntrospectCache erase(String templateId);

    int size();

    List<Map<String, String>> visitors() throws Exception;
}
