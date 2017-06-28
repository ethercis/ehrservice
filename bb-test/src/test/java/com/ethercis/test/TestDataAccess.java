package com.ethercis.test;

import static org.junit.Assert.assertNotNull;

import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import java.util.Properties;
import org.jooq.SQLDialect;

public class TestDataAccess extends DataAccess {

    public TestDataAccess(
            final SQLDialect postgres, final String localhost, final String ethercis,
            final String ethercis1, final I_KnowledgeCache knowledge) throws Exception {
        super(postgres, localhost, ethercis, ethercis1, knowledge);
    }

    public static DataAccess newInstance() throws Exception {
        final String basePath = System.getProperty("test.knowledge.basepath");
        assertNotNull("Please set the system property 'test.knowledge.basepath'", basePath);

        Properties props = new Properties();
        props.put("knowledge.path.archetype", basePath + "/archetypes");
        props.put("knowledge.path.template", basePath + "/templates");
        props.put("knowledge.path.opt", basePath + "/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        final KnowledgeCache knowledge = new KnowledgeCache(null, props);

        return new TestDataAccess(
                SQLDialect.POSTGRES,
                "jdbc:postgresql://" + System.getProperty("test.db.host") + "/" + System.getProperty("test.db.name"),
                System.getProperty("test.db.user"),
                System.getProperty("test.db.password"),
                knowledge
        );
    }
}
