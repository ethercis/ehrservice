package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.CompositionSerializer;
import junit.framework.TestCase;
import org.junit.Test;
import org.openehr.rm.datatypes.text.DvText;

import java.util.HashMap;
import java.util.Map;

public class DvCodedTextVBeanTest extends TestCase {

    @Test
    public void testBuild(){
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "coded text value");

        Map<String, Object> definingCode = new HashMap<>();
        attributes.put("definingCode", definingCode);
        Map<String, Object> terminology = new HashMap<>();
        definingCode.put("terminologyId", terminology);
        terminology.put("value", "local");
        definingCode.put("codeString", "at9999");

        Map<String, Object> valueMap = new HashMap<>();

        valueMap.put(CompositionSerializer.TAG_VALUE, attributes);

        DvText codedText = DvCodedTextVBean.getInstance(valueMap);

        assertNotNull(codedText);
    }
}