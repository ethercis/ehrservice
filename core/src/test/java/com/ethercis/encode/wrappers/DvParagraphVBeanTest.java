package com.ethercis.encode.wrappers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ethercis.ehr.encode.wrappers.DvParagraphVBean;
import org.junit.Test;
import org.openehr.rm.datatypes.text.DvParagraph;
import org.openehr.rm.datatypes.text.DvText;


public class DvParagraphVBeanTest {

	@Test
	public void testGetFieldMap() throws Exception {
		List<DvText> items = new ArrayList<DvText>();
		for (String s: new String[]{"p1", "p2", "p3"}){
			DvText text = new DvText(s);
			items.add(text);
		}
		
		DvParagraph paragraph = new DvParagraph(items);
		DvParagraphVBean vbean = new DvParagraphVBean(paragraph);
		Map<String, Object> map = vbean.getFieldMap();
		
		String sp = (String)map.get("items");
		
		assertEquals("p1,p2,p3", sp);
		
		   
	}

}
