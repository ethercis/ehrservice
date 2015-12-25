package com.ethercis.encode.wrappers.constraints;

import static org.junit.Assert.*;

import com.ethercis.ehr.encode.wrappers.DvQuantityVBean;
import com.ethercis.ehr.encode.wrappers.constraints.DvQuantityVConstraints;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantity;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.quantity.DvQuantity;

import org.openehr.rm.support.basic.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DvQuantityConstraintsTest {

	DvQuantityVBean testquantity;
    DvQuantityVConstraints constraints;
    List<CDvQuantityItem> properties = new ArrayList<CDvQuantityItem>();
    Interval<Integer> occurrences = new Interval<Integer>(1,1);
	
	@Before
	public void setUp() throws Exception {
		testquantity = new DvQuantityVBean(new DvQuantity(1.2345));
        constraints = new DvQuantityVConstraints(RMObjectBuilder.getInstance(), (DvQuantity)testquantity.getAdaptee());

        properties.add(new CDvQuantityItem(new Interval<Double>(new Double(0), new Double(100)), "kg"));
        properties.add(new CDvQuantityItem(new Interval<Double>(new Double(0), new Double(100)), "mg"));
        CDvQuantity parms = new CDvQuantity("/path", occurrences, properties);
		constraints.setConstraints(parms);
        //excludes boundaries
        constraints.setMinInclusive("mg", false);
        constraints.setMaxInclusive("mg", false);

	}
	
	@Test
	public void test() throws Exception {
		
		//check for required fields
		assertTrue(constraints.isAttributeRequired("magnitude"));
		assertFalse(constraints.isAttributeRequired("value")); //does not exist...
		
		assertEquals(double.class, constraints.getAttributeClass("magnitude"));

        Set<String> units = constraints.getUnits();
        assertEquals(2, units.size());
        assertTrue(units.contains("kg") && units.contains("mg"));

        //check limits
        assertTrue(constraints.isWithinLimits("kg", 0));
        assertTrue(constraints.isWithinLimits("kg", 100));
        assertFalse(constraints.isWithinLimits("kg", 101));
        assertFalse(constraints.isWithinLimits("ug", 0));

        //exclusive boundaries
        assertFalse(constraints.isWithinLimits("mg", 0));
        assertFalse(constraints.isWithinLimits("mg", 100));


        //check occurrences
        assertEquals(0, constraints.getPrecision("kg"));

        DvQuantity qty = new DvQuantity("kg", 1.2345, 3);

        assertTrue(constraints.validate(qty));

	}

}
