package com.ethercis.opt.query;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import junit.framework.TestCase;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by christian on 5/7/2018.
 */
public class QueryOptWithCacheTest extends TestCase {

    I_KnowledgeCache knowledge;
    protected DSLContext context;
    Connection connection;
    IntrospectCache introspectCache;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "src/test/resources/knowledge");
        props.put("knowledge.path.template", "src/test/resources/knowledge");
        props.put("knowledge.path.opt", "src/test/resources/knowledge");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        knowledge = new KnowledgeCache(null, props);

        Pattern include = Pattern.compile(".*");

        knowledge.retrieveFileMap(include, null);

        String userName = System.getProperty("test.db.user");
        String password = System.getProperty("test.db.password");
        String url = "jdbc:postgresql://" + System.getProperty("test.db.host") + ":" + System.getProperty("test.db.port") + "/" + System.getProperty("test.db.name");
        connection = DriverManager.getConnection(url, userName, password);
        try  {
            context = DSL.using(connection, SQLDialect.POSTGRES);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //build a meta cache
        introspectCache = new IntrospectCache(context, knowledge).synchronize();
    }

    @Test
    public void testQueryUpperUnbounded() throws Exception {
        List result = introspectCache.visitor("IDCR Problem List.v1").upperNotBounded();

        assertNotNull(result);

        assertEquals(3, result.size());
    }

    @Test
    public void testQueryType() throws Exception {
        String result = introspectCache
                            .visitor("IDCR Problem List.v1")
                            .type("/content[openEHR-EHR-SECTION.problems_issues_rcp.v1]/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/data[at0001]/items[at0012]");

        assertEquals("DV_TEXT", result);
    }

    @Test
    public void testQueryByFieldValue() throws Exception {
        List result = introspectCache
                            .visitor("IDCR Problem List.v1")
                            .nodeByFieldValue("name", "Problem/Diagnosis name");

        assertEquals(1, result.size());
    }

    @Test
    public void testQueryByFieldRegexp() throws Exception {
        List result = introspectCache
                        .visitor("IDCR Problem List.v1")
                        .nodeFieldRegexp("name", "/PROBLEM.*/i");

        assertEquals(3, result.size());
    }

}