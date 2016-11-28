package org.openehr.am.template;

import com.ethercis.ehr.building.DomainBuilder;
import com.ethercis.ehr.building.GenerationStrategy;
import com.ethercis.ehr.resources.I_KnowledgeManager;
import com.ethercis.ehr.resources.KnowledgeManager;
import junit.framework.TestCase;
import openEHR.v1.template.TEMPLATE;
import org.junit.Before;
import org.openehr.am.archetype.Archetype;
import org.openehr.rm.composition.Composition;

import java.util.Properties;
import java.util.regex.Pattern;

public class FlattenerNestedClusterSlotTest extends TestCase {

    I_KnowledgeManager knowledge;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("knowledge.path.archetypes", "/Development/Dropbox/eCIS_Development/knowledge/production/archetypes");
        props.put("knowledge.path.templates", "/Development/Dropbox/eCIS_Development/knowledge/production/templates");
        knowledge = new KnowledgeManager(props);

        Pattern include = Pattern.compile(".*");

        knowledge.getFiles(include, null);
    }


    public void testToFlattenedArchetype() throws Exception {

        //use the fat nurse form to check multiple entries management
        knowledge.getArchetype("openEHR-EHR-ADMIN_ENTRY.privantis_patient_administrative_data.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.country.v1");
        knowledge.getArchetype("openEHR-EHR-ADMIN_ENTRY.privantis_drsp_administrative_data.v1");
        knowledge.getArchetype("openEHR-EHR-EVALUATION.verbal_examination.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.diabetes.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.privantis_ethnic_groups.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.drs_history_elements.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.drs_allergy.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.drs_biology.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.laboratory_test_hba1c.v1");
        knowledge.getArchetype("openEHR-EHR-SECTION.arterial_blood_pressure.v1");
        knowledge.getArchetype("openEHR-EHR-OBSERVATION.blood_pressure.v1");
        knowledge.getArchetype("openEHR-EHR-SECTION.visual_acuity.v1");
        knowledge.getArchetype("openEHR-EHR-OBSERVATION.visual_acuity.v1");
        knowledge.getArchetype("openEHR-EHR-SECTION.intraocular_pressure.v1");
        knowledge.getArchetype("openEHR-EHR-OBSERVATION.intraocular_pressure.v1");
        knowledge.getArchetype("openEHR-EHR-EVALUATION.adverse_reaction.v1");
        knowledge.getArchetype("openEHR-EHR-EVALUATION.use_of_tropicamide.v1");
        knowledge.getArchetype("openEHR-EHR-OBSERVATION.fundus_photograph.v1");
        knowledge.getArchetype("openEHR-EHR-COMPOSITION.nurse_form.v2");

        knowledge.getArchetype("openEHR-EHR-EVALUATION.verbal_examination_test.v1");
        knowledge.getArchetype("openEHR-EHR-CLUSTER.drs_biology_test.v1");
        knowledge.getArchetype("openEHR-EHR-COMPOSITION.ecis_test.v1");


        String templateId = "ECIS Test 1";

        TEMPLATE form = knowledge.getTemplate(templateId);

        FlattenerNew flattener = new FlattenerNew();

        Archetype instance = flattener.toFlattenedArchetype(form, knowledge.getArchetypeMap());

        assertNotNull(instance);

        //try to build an actual COMPOSITION from the instance...
        DomainBuilder generator = DomainBuilder.getInstance();
        Composition composition = (Composition)generator.create(instance, templateId, knowledge.getArchetypeMap(), GenerationStrategy.MAXIMUM);

        assertNotNull(composition);



    }
}