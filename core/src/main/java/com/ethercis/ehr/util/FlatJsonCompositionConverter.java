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

package com.ethercis.ehr.util;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.marand.thinkehr.jsonlib.CompositionConversionException;
import com.marand.thinkehr.jsonlib.CompositionConverter;
import com.marand.thinkehr.jsonlib.impl.CompositionConverterImpl;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.jaxb.am.Template;
import org.openehr.rm.composition.Composition;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * utility class to encapsulate calls to EhrScape flat json library
 * Created by christian on 2/25/2016.
 */
public class FlatJsonCompositionConverter implements I_FlatJsonCompositionConverter {

    private CompositionConverter converter;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private I_KnowledgeCache knowledge;
    I_ContentBuilder contentBuilder;

    protected FlatJsonCompositionConverter(I_KnowledgeCache knowledgeCache, CompositionConverter compositionConverter) throws Exception {
        this.converter = compositionConverter;
        this.unmarshaller = getUnmarshallerInstance();
        this.marshaller = getMarshallerIntance();
        this.knowledge = knowledgeCache;
    }

    public static Unmarshaller getUnmarshallerInstance() throws JAXBException {
        JAXBContext unmarshallerContext = JAXBContext.newInstance("org.openehr.jaxb.rm:org.openehr.jaxb.am");
        Unmarshaller unmarshaller = unmarshallerContext.createUnmarshaller();
        unmarshaller.setSchema(null); // disable schema validation
        return unmarshaller;
    }

    public static Marshaller getMarshallerIntance() throws Exception {
        JAXBContext marshallerContext = JAXBContext.newInstance(org.openehr.jaxb.rm.Composition.class);
        Marshaller marshaller = marshallerContext.createMarshaller();
        marshaller.setSchema(null);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        return marshaller;
    }

    private Template templateFromXml(Unmarshaller unmarshaller, String templateXml) throws JAXBException {
        //get the template
        JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(templateXml));
        Template jaxbTemplate = (Template)jaxbElement.getValue();
        return jaxbTemplate;
    }

    public static I_FlatJsonCompositionConverter getInstance(I_KnowledgeCache knowledgeCache) throws Exception {
        CompositionConverter converter = new CompositionConverterImpl();
        FlatJsonCompositionConverter flatJsonCompositionConverter = new FlatJsonCompositionConverter(knowledgeCache, converter);
        return flatJsonCompositionConverter;
    }

    @Override
    public Composition toComposition(String templateId, Map<String, Object> flatJsonMap) throws Exception {
        //get the template from the cache
        OPERATIONALTEMPLATE operationaltemplate = ((OPERATIONALTEMPLATE)knowledge.retrieveTemplate(templateId));
        return toComposition(operationaltemplate, flatJsonMap);
    }

    @Override
    public Composition toComposition(OPERATIONALTEMPLATE operationaltemplate, Map<String, Object> flatJsonMap) throws Exception {

        if (operationaltemplate == null)
            throw new IllegalArgumentException("No template found...");

        StringWriter stringWriter = new StringWriter();

        //get the unmarshalled template
        XmlOptions xmlOptions = new XmlOptions().setSaveSyntheticDocumentElement(new QName("http://schemas.openehr.org/v1","template"));
        String templateXml = operationaltemplate.xmlText(xmlOptions);
        JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(templateXml));
        Template jaxbTemplate = (Template)jaxbElement.getValue();

        //rebuild from map
        String templateId = operationaltemplate.getTemplateId().getValue();
        contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);

        org.openehr.jaxb.rm.Composition newComposition;
        try {
            newComposition = converter.toComposition(jaxbTemplate, flatJsonMap);
        } catch (CompositionConversionException e){
            throw new IllegalArgumentException("Could not convert JSON map:"+e.getCause());
        }

        //convert it into an RM composition
        marshaller.marshal(new JAXBElement<>( new QName("http://schemas.openehr.org/v1", "composition"),
                org.openehr.jaxb.rm.Composition.class, null, newComposition), stringWriter);
        byte[] bytes = stringWriter.getBuffer().toString().getBytes();

        Composition composition = contentBuilder.importCanonicalXML(new ByteArrayInputStream(bytes));
        return composition;
    }

    @Override
    public Map<String, Object> fromComposition(String templateId, Composition composition) throws Exception {
        //get the template from the cache
        Object cachedTemplate = knowledge.retrieveTemplate(templateId);

        if (cachedTemplate instanceof OPERATIONALTEMPLATE) {

            OPERATIONALTEMPLATE operationaltemplate = ((OPERATIONALTEMPLATE) knowledge.retrieveTemplate(templateId));

            return fromComposition(operationaltemplate, composition, false);
        }
        else
            throw new IllegalArgumentException("Template id does not match a valid operational template:"+templateId);
    }

    @Override
    public Map<String, Object> fromComposition(String templateId, Composition composition, boolean allElements) throws Exception {
        //get the template from the cache
        OPERATIONALTEMPLATE operationaltemplate = ((OPERATIONALTEMPLATE)knowledge.retrieveTemplate(templateId));

        return fromComposition(operationaltemplate, composition, allElements);
    }

    @Override
    public Map<String, Object> fromComposition(OPERATIONALTEMPLATE operationaltemplate, Composition composition)  throws Exception  {
        return fromComposition(operationaltemplate, composition, false);
    }

    @Override
    public Map<String, Object> fromComposition(OPERATIONALTEMPLATE operationaltemplate, Composition composition, boolean allElements)  throws Exception  {
        //get the unmarshalled template
        XmlOptions xmlOptions = new XmlOptions().setSaveSyntheticDocumentElement(new QName("http://schemas.openehr.org/v1","template"));
        String templateXml = operationaltemplate.xmlText(xmlOptions);
        JAXBElement jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(templateXml));
        Template jaxbTemplate = (Template)jaxbElement.getValue();

        //get the unmarshalled composition
        String templateId = operationaltemplate.getTemplateId().getValue();
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, I_ContentBuilder.OPT, knowledge, templateId);
        //we create a composition with all possible elements
        byte[] exportXml = contentBuilder.exportCanonicalXML(composition, true, allElements);
        if (exportXml == null)
            throw new IllegalArgumentException("Could not export composition under canonical XML");

        String xmlized = new String(exportXml);
        jaxbElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(xmlized));
        org.openehr.jaxb.rm.Composition jaxbComposition = (org.openehr.jaxb.rm.Composition)jaxbElement.getValue();

        //perform conversion
        Map<String, Object> map = converter.fromComposition(jaxbTemplate, jaxbComposition);
        return map;
    }

    @Override
    public Map<String, Integer> getItemArrayPathMap(){
        return contentBuilder.getArrayItemPathMap();
    }
}
