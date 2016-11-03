package com.ethercis.dao.access.support;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;

import junit.framework.TestCase;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by Christian Chevalley on 4/25/2015.
 */
public abstract class AccessTestCase extends TestCase {

    protected I_DomainAccess testDomainAccess;
    protected DSLContext context;
    protected I_KnowledgeCache knowledge;

    protected void setupDomainAccess() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");

        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        Map<String, Object> properties = new HashMap<>();
        properties.put(I_DomainAccess.KEY_DIALECT, "POSTGRES");
        properties.put(I_DomainAccess.KEY_URL, "jdbc:postgresql://localhost:5434/ethercis");
        properties.put(I_DomainAccess.KEY_LOGIN, "postgres");
        properties.put(I_DomainAccess.KEY_PASSWORD, "postgres");

        properties.put(I_DomainAccess.KEY_KNOWLEDGE, knowledge);

        try {
            testDomainAccess = new DummyDataAccess(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = testDomainAccess.getContext();
    }

}
