package com.ethercis.opt.query;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import junit.framework.TestCase;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by christian on 5/10/2018.
 */
public class IntrospectCacheTest extends TestCase {
    I_KnowledgeCache knowledge;
    protected DSLContext context;
    Connection connection;

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
    }

    @Test
    public void testSynchronize() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        introspectCache.synchronize();

        assertTrue(introspectCache.size() > 0);

        I_QueryOptMetaData visitor = introspectCache.visitor(UUID.fromString("06141eb7-0088-4bb3-badb-78a53fcfbb13"));

        assertNotNull(visitor);

    }

    @Test
    public void testInvalidateDBCache() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        int ret = introspectCache.invalidateDBCache();

        assertTrue(ret > 0);

        I_QueryOptMetaData visitor = introspectCache.visitor(UUID.fromString("001861fc-2a0a-45cc-8d4d-20679be022bd"));

        assertNull(visitor);
    }

    @Test
    public void testLoad() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        introspectCache.invalidate();

//        assertTrue(introspectCache.load().size() > 0);

        introspectCache.load().synchronize();

        assertTrue(introspectCache.size() > 0);
    }

    @Test
    public void testRetrieveVisitorByUUID() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        introspectCache.load();

        assertNotNull(introspectCache.visitor(UUID.fromString("06141eb7-0088-4bb3-badb-78a53fcfbb13")));
    }

    @Test
    public void testRetrieveVisitorByTemplateId() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        assertNotNull(introspectCache.visitor("IDCR - Immunisation summary.v0"));
    }

    //the scenario is: query for a missing template in DB
    //assume the template is in cache (loaded from REST), then the DB should be resynchronize
    //and visitor returned.
    @Test
    public void testMissingVisitorByTemplateId() throws Exception {
        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge);

        //invalidate and erase the cache for this template (test)
        introspectCache.erase("prescription");

        //simulate knowledge load new template
        //read in a template into a string
        Path prescriptionOptPath = Paths.get("src/test/resources/not-in-cache/prescription.opt");
        byte[] content = Files.readAllBytes(prescriptionOptPath);
        knowledge.addOperationalTemplate(content);

        I_QueryOptMetaData visitor = introspectCache.visitor("prescription");

        //house cleaning
        Files.delete(Paths.get(knowledge.getOptPath()+"/"+"prescription.opt"));

        assertEquals("prescription", visitor.getTemplateId());
        assertNotNull(visitor.getJsonPathVisitor());
    }

    @Test
    public void testVisitors() throws Exception {

        I_IntrospectCache introspectCache = new IntrospectCache(context, knowledge).load().synchronize();

        List<Map<String, String>> visitors = introspectCache.visitors();

        assertTrue(visitors.size() > 0);

    }

}