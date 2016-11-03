package com.ethercis.dao.access.support;

import com.ethercis.ehr.building.I_ContentBuilder;
import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.ehr.knowledge.KnowledgeCache;

import org.openehr.build.SystemValue;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Christian Chevalley on 4/20/2015.
 */
public class RmObjectHelper {


    private static I_KnowledgeCache knowledge ;

    static {
        Properties props = new Properties();
        props.put("knowledge.path.archetype", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.template", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        try {
            knowledge = new KnowledgeCache(null, props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RmObjectHelper() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetypes", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.templates", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        props.put("knowledge.path.opt", "/Development/Dropbox/eCIS_Development/knowledge/production/operational_templates");
        props.put("knowledge.cachelocatable", "true");
        props.put("knowledge.forcecache", "true");
        this.knowledge = new KnowledgeCache(null, props);
    }

    public static I_KnowledgeCache getKnowledge(){
        return knowledge;
    }

    public static Composition createDummyComposition(String templateId) throws Exception {
        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.section_observation_test.v2");

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(null, knowledge, templateId);
        return contentBuilder.generateNewComposition();
    }

    public static Composition createDummyCompositionWithParameters(String templateId,
                                                                   PartyIdentified composer,
                                                                   CodePhrase language,
                                                                   CodePhrase encoding,
                                                                   CodePhrase territory,
                                                                   EventContext context) throws Exception {

        knowledge.retrieveArchetype("openEHR-EHR-COMPOSITION.section_observation_test.v2");

        Map<SystemValue, Object> values = new HashMap<>();

        values.put(SystemValue.COMPOSER, composer);
        values.put(SystemValue.LANGUAGE, language);
        values.put(SystemValue.ENCODING, encoding);
        values.put(SystemValue.TERRITORY, territory);
        values.put(SystemValue.CONTEXT, context);

        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(values, knowledge, templateId);
        return contentBuilder.generateNewComposition();

    }

    /**
     * storeComposition a dummy composition from an operational template
     * @param templateId
     * @param composer
     * @param language
     * @param encoding
     * @param territory
     * @param context
     * @return
     * @throws Exception
     */
    public static Composition createDummyQualifiedCompositionWithParameters(String templateId,
                                                                   PartyIdentified composer,
                                                                   CodePhrase language,
                                                                   CodePhrase encoding,
                                                                   CodePhrase territory,
                                                                   EventContext context) throws Exception {

        //add some specific values into the composition builder

        Map<SystemValue, Object> values = new HashMap<>();

        values.put(SystemValue.COMPOSER,  composer);
        values.put(SystemValue.LANGUAGE, language);
        values.put(SystemValue.ENCODING, encoding);
        values.put(SystemValue.TERRITORY, territory);
        values.put(SystemValue.CONTEXT, context);

        //try to build an actual COMPOSITION from the instance...
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(values, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();
        return composition;
    }

    public static Composition createDummyQualifiedCompositionWithParameters(I_KnowledgeCache knowledge,
                                                                            String templateId,
                                                                            PartyIdentified composer,
                                                                            CodePhrase language,
                                                                            CodePhrase encoding,
                                                                            CodePhrase territory,
                                                                            EventContext context) throws Exception {

        //add some specific values into the composition builder

        Map<SystemValue, Object> values = new HashMap<>();

        values.put(SystemValue.COMPOSER,  composer);
        values.put(SystemValue.LANGUAGE, language);
        values.put(SystemValue.ENCODING, encoding);
        values.put(SystemValue.TERRITORY, territory);
        values.put(SystemValue.CONTEXT, context);

        //try to build an actual COMPOSITION from the instance...
        I_ContentBuilder contentBuilder = I_ContentBuilder.getInstance(values, knowledge, templateId);
        Composition composition = contentBuilder.generateNewComposition();
        return composition;
    }
}
