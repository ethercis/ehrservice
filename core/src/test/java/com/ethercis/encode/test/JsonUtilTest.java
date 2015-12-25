package com.ethercis.encode.test;

import com.ethercis.ehr.json.FlatJsonUtil;
import com.ethercis.ehr.json.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import org.codehaus.jackson.JsonProcessingException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class JsonUtilTest extends TestCase {
	String json = "{\n" +
            "   \"Port\":\n" +
            "   {\n" +
            "       \"@alias\": \"defaultHttp\",\n" +
            "       \"Enabled\": \"true\",\n" +
            "       \"Number\": \"10092\",\n" +
            "       \"Protocol\": \"http\",\n" +
            "       \"KeepAliveTimeout\": \"20000\",\n" +
            "       \"ThreadPool\":\n" +
            "       {\n" +
            "           \"@enabled\": \"false\",\n" +
            "           \"Max\": \"150\",\n" +
            "           \"ThreadPriority\": \"5\"\n" +
            "       },\n" +
            "       \"ExtendedProperties\":\n" +
            "       {\n" +
            "           \"Property\":\n" +
            "           [                         \n" +
            "               {\n" +
            "                   \"@name\": \"connectionTimeout\",\n" +
            "                   \"$\": \"20000\"\n" +
            "               }\n" +
            "           ]\n" +
            "       }\n" +
            "   }\n" +
            "}";

	public void _testFlatten() throws JsonProcessingException, IOException {
		Map<String, String> retmap = new JsonUtil().flatten(json);
		
		for (String path: retmap.keySet()) {
			System.out.println(path+"="+retmap.get(path));
		}
	}

    static Map<String, String> testMap = new TreeMap<>();

//    @Test
    public void _testUnflattenSimple(){

        testMap.put("ctx/composer_name", "Dr Joyce Smith");
        testMap.put("ctx/id_namespace", "NHS-UK");
        testMap.put("ctx/id_scheme", "2.16.840.1.113883.2.1.4.3");
        testMap.put("ctx/health_care_facility|name", "Northumbria Community NHS");
        testMap.put("ctx/health_care_facility|id", "99999-345");
        testMap.put("ctx/language", "en");
        testMap.put("ctx/territory", "GB");
        testMap.put("ctx/time", "2015-02-24T00:11:02.518+02:00");


        Map<String, Object> result = FlatJsonUtil.unflattenJSON(testMap);

        assertEquals("99999-345", ((Map) ((Map) result.get("ctx")).get("health_care_facility")).get("id"));

    }

//    @Test
    public void _testUnflattenWithArrays(){
        testMap.put("ctx/composer_name", "Dr Joyce Smith");
        testMap.put("ctx/id_namespace", "NHS-UK");
        testMap.put("ctx/id_scheme", "2.16.840.1.113883.2.1.4.3");
        testMap.put("ctx/health_care_facility|name", "Northumbria Community NHS");
        testMap.put("ctx/health_care_facility|id", "99999-345");
        testMap.put("ctx/language", "en");
        testMap.put("ctx/territory", "GB");
        testMap.put("ctx/time", "2015-02-24T00:11:02.518+02:00");
        testMap.put("allergies_list/allergies_and_adverse_reactions:0/adverse_reaction:0/causative_agent|value", "allergy to penicillin");
        testMap.put("allergies_list/allergies_and_adverse_reactions:0/adverse_reaction:1/causative_agent|value", "allergy to work");
        testMap.put("allergies_list/allergies_and_adverse_reactions:0/adverse_reaction:0/causative_agent|code", "91936005");
        testMap.put("allergies_list/allergies_and_adverse_reactions:0/adverse_reaction:0/causative_agent|terminology", "SNOMED-CT");
        testMap.put("allergies_list/allergies_and_adverse_reactions:0/adverse_reaction:0/reaction_details/comment", "History unclear");
        Map<String, Object> result = FlatJsonUtil.unflattenJSON(testMap);
        assertTrue(true);
    }

    @Test
    public void testReader() throws FileNotFoundException {
        FileReader fileReader = new FileReader("/Development/Dropbox/eCIS_Development/samples/ProblemList_2FLAT.json");

        Map<String, String> inputMap = FlatJsonUtil.inputStream2Map(fileReader);

        assertNotNull(inputMap);

        Map<String, Object> result = FlatJsonUtil.unflattenJSON(inputMap);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        String jsonString = gson.toJson(result);

        System.out.println(jsonString);

        assertTrue(true);

    }

}
