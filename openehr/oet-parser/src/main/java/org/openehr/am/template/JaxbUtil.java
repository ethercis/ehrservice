// RVE: 2017-03-22: extracted from the openehr-am-rm-term-1.0.9.jar
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openehr.am.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;

public class JaxbUtil {
    private static Logger log = Logger.getLogger(JaxbUtil.class);
    static JAXBContext context;

    public JaxbUtil() {
    }

    public static String generateSchema() {
        final StringWriter writer = new StringWriter();
        String schema = null;

        try {
            context.generateSchema(new SchemaOutputResolver() {
                public Result createOutput(String namespaceUri, String schemaName) throws IOException {
                    StreamResult result = new StreamResult(writer);
                    result.setSystemId("PRIVANTIS-EHR");
                    return result;
                }
            });
            schema = writer.toString();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return schema;
    }

    public static Object unmarshal(InputStream is) throws JAXBException {
        Unmarshaller u = context.createUnmarshaller();
        Object ret = u.unmarshal(is);
        return ret;
    }

    public static Object unmarshal(StreamSource source, Class<?> clazz) throws JAXBException {
        Unmarshaller u = context.createUnmarshaller();
        JAXBElement ret = u.unmarshal(source, clazz);
        return ret.getValue();
    }

    public static String marshal(Object o) throws JAXBException {
        StringWriter writer = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.marshal(o, writer);
        return writer.toString();
    }

    static {
        try {
            context = JAXBContext.newInstance("openEHR.v1.template:org.openehr.schemas.v1");
        } catch (JAXBException var1) {
            throw new RuntimeException("Unable to initialize JAXBContext", var1);
        }

    }
}
