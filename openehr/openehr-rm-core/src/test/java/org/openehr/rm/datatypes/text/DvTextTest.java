/*
 * component:   "openEHR Reference Implementation"
 * description: "Class DvTextTest"
 * keywords:    "unit test"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL: http://svn.openehr.org/ref_impl_java/BRANCHES/RM-1.0-update/libraries/src/test/org/openehr/rm/datatypes/text/DvTextTest.java $"
 * revision:    "$LastChangedRevision: 2 $"
 * last_change: "$LastChangedDate: 2005-10-12 23:20:08 +0200 (Wed, 12 Oct 2005) $"
 */
/**
 * TextTest
 *
 * @author Rong Chen
 * @version 1.0 
 */
package org.openehr.rm.datatypes.text;

import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.rm.support.terminology.TestTerminologyService;

import junit.framework.TestCase;

public class DvTextTest extends TestCase {

    public DvTextTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testValidValue() throws Exception {
        assertTrue("good value", DvText.validValue("good value"));
        assertFalse("null value", DvText.validValue(null));

        // this seems to be allowed in ethercis
        //assertFalse("value with \\r\\n ", DvText.validValue("bad value\r\n"));
        //assertFalse("value with \\n", DvText.validValue("bad value\n"));
        //assertFalse("value with \\r", DvText.validValue("bad value\r"));

        assertFalse("empty value", DvText.validValue(""));
    }

    public void testConstructor() throws Exception {
        DvText text;

        // verify that both language and charset are optional now
        text = new DvText("value", null, null, null, null, null, null);

        // try the new minimal constructor
        text = new DvText("value");
    }
    
    public void testCreateWithNullEncoding() throws Exception {
    	TerminologyService ts = TestTerminologyService.getInstance();
    	CodePhrase lang = new CodePhrase("ISO_639-1", "en");
    	CodePhrase charset = null;
    	DvText dt = new DvText("test", lang, charset, ts);
    	assertNotNull("failed to create dvText", dt);
    }
    
    public void testCreateWithNullLanguage() throws Exception {
    	TerminologyService ts = TestTerminologyService.getInstance();
    	CodePhrase lang = null;
    	CodePhrase charset = new CodePhrase("IANA_character-sets", "UTF-8");
    	DvText dt = new DvText("test", lang, charset, ts);
    	assertNotNull("failed to create dvText", dt);
    }

}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is DvTextTest.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2003-2008
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */