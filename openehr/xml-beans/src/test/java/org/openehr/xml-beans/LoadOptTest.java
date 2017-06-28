package org.openehr.am.template;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.xmlbeans.XmlException;
import org.openehr.schemas.v1.CATTRIBUTE;
import org.openehr.schemas.v1.CSINGLEATTRIBUTE;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;
import org.openehr.schemas.v1.TemplateDocument;
import org.openehr.schemas.v1.TemplateDocument.Factory;

public class LoadOptTest extends TestCase {

    public void testLoadOpt() throws IOException, XmlException {
        final InputStream is = getClass().getClassLoader().getResourceAsStream("bmi.opt");
        final OPERATIONALTEMPLATE opt = Factory.parse(is).getTemplate();

        for(CATTRIBUTE att: opt.getDefinition().getAttributesArray())
        {
            final String name = att.getRmAttributeName();
            if ("category".equals(name))
            {
                assertTrue(att instanceof CSINGLEATTRIBUTE);
            }
        }
    }
}
