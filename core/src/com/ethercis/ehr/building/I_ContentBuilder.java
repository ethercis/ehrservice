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
package com.ethercis.ehr.building;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import org.openehr.build.SystemValue;
import org.openehr.rm.composition.Composition;

import java.io.InputStream;
import java.util.Map;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 6/3/2015.
 */
public interface I_ContentBuilder {

    public static int OPT = 1; //constant for operational template mode
    public static int OET = 2; //constant for openehr template mode

    public static String OPT_EXTENSION = "opt";
    public static String OET_EXTENSION = "oet";

    /**
     * get a specialized instance of ContentBuilder depending on mode
     * @param mode OPT or OET
     * @param knowledgeCache a knowledge cache instance
     * @param templateId a template (openEhr or Operational)
     * @return a content builder
     * @throws Exception
     */
    public static I_ContentBuilder getInstance(Map<SystemValue, Object> values, int mode, I_KnowledgeCache knowledgeCache, String templateId) throws Exception {
        switch (mode){
            case OET:
                return new OetContentBuilder(values, knowledgeCache, templateId);
            case OPT:
                return new OptContentBuilder(values, knowledgeCache, templateId);
        }

        return null;
    }

    /**
     * get a specialized instance of ContentBuilder depending on template extension (.opt or .oet).
     * @param knowledgeCache
     * @param templateFileName
     * @return
     * @throws Exception
     */
    public static I_ContentBuilder getInstance(I_KnowledgeCache knowledgeCache, String templateFileName) throws Exception {
        //get the file extension
        if (templateFileName.toLowerCase().endsWith(OPT_EXTENSION))
            return new OptContentBuilder(null, knowledgeCache, templateFileName.substring(0, templateFileName.toLowerCase().indexOf("."+OPT_EXTENSION)));
        else if (templateFileName.toLowerCase().endsWith(OET_EXTENSION))
            return new OetContentBuilder(null, knowledgeCache, templateFileName.substring(0, templateFileName.toLowerCase().indexOf("."+OET_EXTENSION)));
        else
            return null;
    }

    /**
     * get a specialized instance of ContentBuilder depending on template extension (.opt or .oet).
     * @param values parameters to use to build the composition
     * @param knowledgeCache
     * @param templateFileName
     * @return
     * @throws Exception
     */
    public static I_ContentBuilder getInstance(Map<SystemValue, Object> values, I_KnowledgeCache knowledgeCache, String templateFileName) throws Exception {
        //get the file extension
        if (templateFileName.toLowerCase().endsWith(OPT_EXTENSION))
            return new OptContentBuilder(values, knowledgeCache, templateFileName.substring(0, templateFileName.toLowerCase().indexOf("."+OPT_EXTENSION)));
        else if (templateFileName.toLowerCase().endsWith(OET_EXTENSION))
            return new OetContentBuilder(values, knowledgeCache, templateFileName.substring(0, templateFileName.toLowerCase().indexOf("."+OET_EXTENSION)));
        else
            return null;
    }

    public Composition generateNewComposition() throws Exception;

    void setEntryData(Composition composition) throws Exception;

    Composition buildCompositionFromJson(String jsonData) throws Exception;

    Composition importCanonicalXML(InputStream inputStream) throws Exception;

    Composition importAsRM(Composition composition) throws Exception;

    byte[] exportCanonicalXML(String jsonData, boolean prettyPrint) throws Exception;

    byte[] exportCanonicalXML(Composition composition, boolean prettyPrint) throws Exception;

    static byte[] exportCanonicalXML(Composition composition) throws Exception {
        return ContentBuilder.canonicalExporter(composition, true);
    }

    void bindOtherContextFromJson(Composition composition, String jsonData) throws Exception;

    String getEntry();

    String getTemplateId();

    Composition getComposition();

    public void setCompositionParameters(Map<SystemValue, Object> values);
}
