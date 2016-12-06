package com.ethercis.encode.wrappers.constraints;


import com.ethercis.ehr.encode.wrappers.DvProportionVBean;
import com.ethercis.ehr.encode.wrappers.constraints.DvProportionVConstraints;
import org.junit.Before;
import org.junit.Test;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.quantity.DvProportion;
import org.openehr.rm.datatypes.quantity.ProportionKind;
import org.openehr.rm.support.basic.Interval;

import static org.junit.Assert.*;

public class DvProportionConstraintsTest {

	DvProportionVBean testProportion;
    DvProportionVConstraints constraints;
    CComplexObject properties = new CComplexObject();
    Interval<Integer> occurrences = new Interval<Integer>(1,1);
	
	@Before
	public void setUp() throws Exception {
		testProportion = new DvProportionVBean(new DvProportion(1,2, ProportionKind.FRACTION, 0));
        constraints = new DvProportionVConstraints(RMObjectBuilder.getInstance(), (DvProportion)testProportion.getAdaptee());
//        properties.add(new CDvProportionItem(new Interval<Double>(new Double(0), new Double(100)), "mg"));

        //TODO process where is the bloody CDvProportion class or create one!
        //@see https://github.com/openEHR/arch_ed-dotnet/blob/master/ArchetypeEditor/DataConstraints/ProportionConstraint.vb

//        CObject numeratorChild = new CPrimitiveObject("/path", );
//        properties.addAttribute(new CMultipleAttribute()
//                properties.add(new CDvProportionItem(new Interval<Double>(new Double(0), new Double(100)), "mg"));
//        CDvProportion parms = new CDvProportion("/path", occurrences, properties);
//		constraints.setConstraints(parms);
//        //excludes boundaries
//        constraints.setMinInclusive("mg", false);
//        constraints.setMaxInclusive("mg", false);

	}
	
	@Test
	public void test() throws Exception {
		
		//check for required fields
		assertTrue(constraints.isAttributeRequired("magnitude"));
		assertFalse(constraints.isAttributeRequired("value")); //does not exist...
		
		assertEquals(double.class, constraints.getAttributeClass("magnitude"));

//        Set<String> units = constraints.getUnits();
//        assertEquals(2, units.size());
//        assertTrue(units.contains("kg") && units.contains("mg"));

        //check limits
//        assertTrue(constraints.isWithinLimits("kg", 0));
//        assertTrue(constraints.isWithinLimits("kg", 100));
//        assertFalse(constraints.isWithinLimits("kg", 101));
//        assertFalse(constraints.isWithinLimits("ug", 0));
//
//        //exclusive boundaries
//        assertFalse(constraints.isWithinLimits("mg", 0));
//        assertFalse(constraints.isWithinLimits("mg", 100));
//
//
//        //check occurrences
//        assertEquals(0, constraints.getPrecision("kg"));
//
//        DvProportion qty = new DvProportion("kg", 1.2345, 3);

//        assertTrue(constraints.validate(qty));

	}

}
