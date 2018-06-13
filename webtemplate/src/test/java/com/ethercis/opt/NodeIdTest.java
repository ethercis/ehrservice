package com.ethercis.opt;

import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.Test;

/**
 * Created by christian on 3/8/2018.
 */
public class NodeIdTest extends TestCase {

    @Test
    public void testFormatting(){

        assertEquals("uri_resource_identifier", new NodeId("URI - resource identifier").ehrscape());
        assertEquals("relationship_role", new NodeId("Relationship/ role").ehrscape());
        assertEquals("state_definition", new NodeId("State - definition").ehrscape());
        assertEquals("date_time", new NodeId("Date/Time").ehrscape());
        assertEquals("heading1", new NodeId("Heading1").ehrscape());
        assertEquals("heading_1", new NodeId("Heading 1").ehrscape());
        assertEquals("slot_to_contain_other_cluster_archetypes", new NodeId("Slot to contain other Cluster archetypes").ehrscape());
        assertEquals("allergies_and_adverse_reactions", new NodeId("Allergies and adverse reactions").ehrscape());

    }

}