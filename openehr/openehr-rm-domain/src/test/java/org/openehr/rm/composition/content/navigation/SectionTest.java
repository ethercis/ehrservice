/*
 * Copyright (C) 2004 Rong Chen, Acode HB, Sweden
 * All rights reserved.
 *
 * The contents of this software are subject to the FSF GNU Public License 2.0;
 * you may not use this software except in compliance with the License. You may
 * obtain a copy of the License at http://www.fsf.org/licenses/gpl.html
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package org.openehr.rm.composition.content.navigation;

import org.openehr.rm.composition.CompositionTestBase;
import org.openehr.rm.composition.content.ContentItem;
import org.openehr.rm.composition.content.entry.Observation;
import org.openehr.rm.datatypes.text.DvText;

import java.util.List;
import java.util.ArrayList;

/**
 * SectionTest
 *
 * @author Rong Chen
 * @version 1.0
 */
public class SectionTest extends CompositionTestBase {

    public SectionTest(String test) {
        super(test);
    }

    public void setUp() throws Exception {
        List<ContentItem> items = new ArrayList<ContentItem>();
        observationTwo = observation("observation 2");
        items.add(observationTwo);
        
        sectionThree = section("section 3");
        items.add(sectionThree);
        
        sectionTwo = new Section("at0000", new DvText("section 2"), items);
        items = new ArrayList<ContentItem>();
        items.add(sectionTwo);
        
        observationOne = observation("observation 1"); 
        items.add(observationOne);
        section = new Section("at0000", new DvText("section"), items);
    }

    public void tearDown() throws Exception {
        section = null;
    }
    
    public void testItemAtPathWhole() {
    	path = "/";
    	value = section.itemAtPath(path);
    	assertEquals(section, value);
    }
    
    public void testItemAtPathSectionTwo() {
    	path = "/items['section 2']";
    	value = section.itemAtPath(path);
    	assertEquals(sectionTwo, value);
    }
    
    public void testItemAtPathSectionThree() {
    	path = "/items['section 2']/items['section 3']";
    	value = section.itemAtPath(path);
    	assertEquals(sectionThree, value);
    }
    
    public void testItemAtPathObservationOne() {
    	path = "/items['observation 1']";
    	value = section.itemAtPath(path);
    	assertEquals(observationOne, value);
    }
    
    public void testItemAtPathObservationTwo() {
    	path = "/items['section 2']/items['observation 2']";
    	value = section.itemAtPath(path);
    	assertEquals(observationTwo, value);
    }

    /* fields */
    private Section section;
    private Section sectionTwo;
    private Section sectionThree;
    private Observation observationOne;
    private Observation observationTwo;
}
