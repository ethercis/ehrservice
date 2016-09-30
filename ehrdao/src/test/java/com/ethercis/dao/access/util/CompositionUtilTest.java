package com.ethercis.dao.access.util;

import com.ethercis.dao.access.support.AccessTestCase;
import com.ethercis.ehr.encode.CompositionSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openehr.rm.support.identification.ObjectVersionID;

import java.util.Map;

public class CompositionUtilTest extends AccessTestCase {
    String templateId = "COLNEC Medication";

    @Before
    public void setUp() throws Exception {
        setupDomainAccess();
    }

    @Test
    public void testDumpTemplate(){
        try {
            Map<String, String> map = CompositionUtil.dumpTemplateMap(knowledge, templateId, CompositionSerializer.WalkerOutputMode.EXPANDED);
            assertNotNull(map);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();

            String jsonString = gson.toJson(map);
            System.out.println(jsonString);

            map = CompositionUtil.dumpTemplateMap(knowledge, templateId, CompositionSerializer.WalkerOutputMode.PATH);
            assertNotNull(map);
            jsonString = gson.toJson(map);
            System.out.println(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testObjectVersionID(){
        String id = "0f930863-afcf-4471-9e8a-18a88870aa7e";
        String system = "ethercis.test.sg";
        String version = "1";

        ObjectVersionID objectVersionID = new ObjectVersionID(id, system, version);

        assertNotNull(objectVersionID);
    }
}