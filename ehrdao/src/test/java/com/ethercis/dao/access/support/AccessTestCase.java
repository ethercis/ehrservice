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
        props.put("knowledge.path.archetype", "core/src/test/resources/knowledge/archetypes");
        props.put("knowledge.path.template", "core/src/test/resources/knowledge/templates");
        props.put("knowledge.path.opt", "core/src/test/resources/knowledge/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");

        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        Map<String, Object> properties = new HashMap<>();
        properties.put(I_DomainAccess.KEY_DIALECT, "POSTGRES");
        properties.put(I_DomainAccess.KEY_URL, "jdbc:postgresql://" + System.getProperty("test.db.host") + ":" + System.getProperty("test.db.port") + "/" + System.getProperty("test.db.name"));
        properties.put(I_DomainAccess.KEY_LOGIN, System.getProperty("test.db.user"));
        properties.put(I_DomainAccess.KEY_PASSWORD, System.getProperty("test.db.password"));

        properties.put(I_DomainAccess.KEY_KNOWLEDGE, knowledge);

        try {
            testDomainAccess = new DummyDataAccess(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = testDomainAccess.getContext();
    }

}
