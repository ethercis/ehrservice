package org.openehr.am.archetype.constraintmodel;

// RVE 2017-03-22: this class was part of openehr-am-rm-term-1.0.9
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.archetype.ontology.TermBindingItem;
import org.openehr.rm.support.identification.ArchetypeID;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "C_ARCHETYPE_ROOT",
        propOrder = {"archetypeId", "termDefinitions", "termBindings"}
)
public class CArchetypeRoot extends CComplexObject {
    @XmlElement(
            name = "archetype_id",
            required = true
    )
    private ArchetypeID archetypeId;
    @XmlElement(
            name = "term_definitions"
    )
    private List<ArchetypeTerm> termDefinitions = new ArrayList();
    @XmlElement(
            name = "term_bindings"
    )
    private List<TermBindingItem> termBindings = new ArrayList();

    public CArchetypeRoot() {
        super();
    }

    public ArchetypeID getArchetypeId() {
        return this.archetypeId;
    }

    public void setArchetypeId(ArchetypeID archetypeId) {
        this.archetypeId = archetypeId;
    }

    public List<ArchetypeTerm> getTermDefinitions() {
        if(this.termDefinitions == null) {
            this.termDefinitions = new ArrayList();
        }

        return this.termDefinitions;
    }

    public void setTermDefinitions(List<ArchetypeTerm> termDefinitions) {
        this.termDefinitions = termDefinitions;
    }

    public List<TermBindingItem> getTermBindings() {
        if(this.termBindings == null) {
            this.termBindings = new ArrayList();
        }

        return this.termBindings;
    }

    public void setTermBindings(List<TermBindingItem> termBindings) {
        this.termBindings = termBindings;
    }
}

