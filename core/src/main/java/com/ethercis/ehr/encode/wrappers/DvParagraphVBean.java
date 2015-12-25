/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ethercis.ehr.encode.wrappers;

import com.ethercis.ehr.encode.DataValueAdapter;
import org.openehr.rm.datatypes.text.DvParagraph;
import org.openehr.rm.datatypes.text.DvText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DvParagraphVBean extends DataValueAdapter implements I_VBeanWrapper {

	public DvParagraphVBean(DvParagraph p) {
		this.adaptee = p;
	}

	@Override
	public Map<String, Object> getFieldMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuffer sb = new StringBuffer();
		for (DvText item: ((DvParagraph)adaptee).getItems())
			sb.append(item.getValue()+",");
		if (sb.length() > 0)
			sb.deleteCharAt(sb.lastIndexOf(","));
		
		map.put("items", sb.toString());
		return map;
	}

    @Override
    public DvParagraph parse(String value, String... defaults) {
        adaptee = ((DvParagraph)adaptee).parse(value);
        return (DvParagraph)adaptee;
    }

	public static DvParagraph generate(){
		List<DvText> texts = new ArrayList<>();
		texts.add(DvTextVBean.generate());
		return new DvParagraph(texts);
	}
}
