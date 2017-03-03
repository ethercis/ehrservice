package com.ethercis.ehr.encode.wrappers;

import junit.framework.TestCase;
import org.junit.Test;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;

import java.util.HashMap;
import java.util.Map;

public class DvCodedTextVBeanTest extends TestCase {

    @Test
    public void testBuild(){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "local");
        attributes.put("codeString", "at9999");
        attributes.put("value", "coded text value");

        DvText codedText = DvCodedTextVBean.getInstance(attributes);

        assertNotNull(codedText);
    }
}