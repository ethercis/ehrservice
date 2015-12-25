package com.ethercis.encode.wrappers;

import static org.junit.Assert.*;

import java.util.Map;

import com.ethercis.ehr.encode.wrappers.DvProportionVBean;
import org.junit.Test;
import org.openehr.rm.datatypes.quantity.DvProportion;
import org.openehr.rm.datatypes.quantity.ProportionKind;


public class DvProportionVBeanTest {

	@Test
	public void testGetFieldMap() throws Exception {
		ProportionKind pk;
		DvProportion proportion = new DvProportion(17, 9, ProportionKind.FRACTION , 0);
		DvProportionVBean vbean = new DvProportionVBean(proportion);
		Map<String, Object> map = vbean.getFieldMap();
		assertEquals(17, Integer.parseInt((String)map.get("numerator")));
		assertEquals(9, Integer.parseInt((String)map.get("denominator")));
		assertEquals(3, ((ProportionKind)map.get("type")).getValue());
	}

}
