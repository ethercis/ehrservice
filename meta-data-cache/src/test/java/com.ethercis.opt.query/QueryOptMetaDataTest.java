package com.ethercis.opt.query;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by christian on 5/7/2018.
 */
public class QueryOptMetaDataTest extends TestCase {

    I_KnowledgeCache knowledge;

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
    }

    @Test
    public void testQueryUpperUnbounded() throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE) knowledge.retrieveTemplate("IDCR Problem List.v1");
        List result = QueryOptMetaData.initialize(operationaltemplate).upperNotBounded();

        assertNotNull(result);

        assertEquals(3, result.size());
    }

    @Test
    public void testQueryUpperUnbounded2() throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE) knowledge.retrieveTemplate("IDCR - Laboratory Test Report.v0");
        List result = QueryOptMetaData.initialize(operationaltemplate).upperNotBounded();

        assertNotNull(result);

        assertEquals(15, result.size());
    }

    @Test
    public void testQueryType() throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE) knowledge.retrieveTemplate("IDCR Problem List.v1");
        QueryOptMetaData queryOptMetaData = QueryOptMetaData.initialize(operationaltemplate);

        String result = queryOptMetaData.type("/content[openEHR-EHR-SECTION.problems_issues_rcp.v1]/items[openEHR-EHR-EVALUATION.problem_diagnosis.v1]/data[at0001]/items[at0012]");

        assertEquals("DV_TEXT", result);
    }

    @Test
    public void testQueryByFieldValue() throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE)knowledge.retrieveTemplate("IDCR Problem List.v1");
        QueryOptMetaData queryOptMetaData = QueryOptMetaData.initialize(operationaltemplate);

        List result = queryOptMetaData.nodeByFieldValue("name", "Problem/Diagnosis name");

        assertEquals(1, result.size());
    }

    @Test
    public void testQueryByFieldRegexp() throws Exception {
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE)knowledge.retrieveTemplate("IDCR Problem List.v1");
        QueryOptMetaData queryOptMetaData = QueryOptMetaData.initialize(operationaltemplate);

        List result = queryOptMetaData.nodeFieldRegexp("name", "/PROBLEM.*/i");

        assertEquals(3, result.size());
    }

}