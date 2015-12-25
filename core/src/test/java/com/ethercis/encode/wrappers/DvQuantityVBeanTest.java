package com.ethercis.encode.wrappers;

import com.ethercis.ehr.encode.wrappers.DvQuantityVBean;
import org.apache.commons.collections.MapUtils;
import org.junit.Test;
import org.openehr.rm.datatypes.quantity.DvQuantity;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;

public class DvQuantityVBeanTest {

	@Test
	public void testGetFields() throws Exception {
		DvQuantityVBean qty = new DvQuantityVBean((DvQuantity)DvQuantity.parseValue("DV_QUANTITY,6.1,kg"));
		
		Map<String, Object> map = qty.getFieldMap();
		MapUtils.debugPrint(System.out, "Field Map", map);

        DvQuantity dvQuantity = (DvQuantity)qty.getAdaptee();
	}

    @Test
    public void testBuild() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("magnitude", (Double)12.0);
        attributes.put("precision", (Integer)0);
        attributes.put("units", "mU");

        DvQuantity quantity = DvQuantityVBean.getInstance(attributes);

        assertNotNull(quantity);
    }

}
