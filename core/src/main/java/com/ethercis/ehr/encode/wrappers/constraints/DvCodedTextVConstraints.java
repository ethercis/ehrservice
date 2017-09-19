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
package com.ethercis.ehr.encode.wrappers.constraints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.ConstraintRef;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.SystemValue;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.schemas.v1.CATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvCodedTextVConstraints extends DataValueConstraints {

    private final int CODE_TEXT = 1;
    private final int CODE_DESCRIPTION = 2;
    private transient Logger log = LogManager.getLogger(DvCodedTextVConstraints.class);

    //a map of <support::code>,text value, description
    //the descriptive text is localized depending on the language set
    private Map<String, Map<Integer, String>> defining_codes = new HashMap<String, Map<Integer, String>>();
    private String assumed_definining_code;

    public DvCodedTextVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    public void putDefiningCode(String code, String text, String description){
        HashMap<Integer, String> texts = new HashMap<Integer, String>();
        texts.put(CODE_TEXT, text);
        texts.put(CODE_DESCRIPTION, description);
        defining_codes.put(code, texts);
    }

    public String getText(String code){
        Map<Integer, String> texts = defining_codes.get(code);

        if (texts == null)
            return null;

        return texts.get(CODE_TEXT);
    }

    public String getDescription(String code){
        Map<Integer, String> texts = defining_codes.get(code);

        if (texts == null)
            return null;

        return texts.get(CODE_DESCRIPTION);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {

        if (valueAttribute == null)
            return;

        Object objectConstraint = ((CComplexObject)(valueAttribute.getChildren().get(0))).getAttribute("defining_code").getChildren().get(0);

        CCodePhrase cphrase = null;

        if (objectConstraint instanceof CCodePhrase)
            cphrase = (CCodePhrase)((CComplexObject)(valueAttribute.getChildren().get(0))).getAttribute("defining_code").getChildren().get(0);
        else if (objectConstraint instanceof ConstraintRef){
            ConstraintRef ref = (ConstraintRef)objectConstraint;
            ref.getReference();
            return;
        }
        else {
            throw new IllegalArgumentException("Unknown constraint instance found:"+objectConstraint.getClass());
        }

        String language; //use the language defined for the builder if any, otherwise use the archetype default
        String defaultlanguage = archetype.getOriginalLanguage().getCodeString();

        CodePhrase setlang = (CodePhrase)builder.getSystemValues().get(SystemValue.LANGUAGE);
        if (setlang != null)
            language = setlang.getCodeString();
        else {
            language = defaultlanguage; //use default from archetype
        }

        //get the code list for this coded text;
        if (cphrase.getCodeList() == null){
            log.warn("No code list defined for codephrase:"+cphrase);
            return;
        }

        for (String code: cphrase.getCodeList()){
//            System.out.println("code:"+code);
            ArchetypeTerm term = archetype.getOntology().termDefinition(language, code);

            if (term == null){ //default to English
                term = archetype.getOntology().termDefinition(defaultlanguage, code);
            }

            if (term == null)
                throw new IllegalArgumentException("Internal error, term code:"+code+" has no related term in terminology");

            putDefiningCode(code, term.getText(), term.getDescription());
        }

        //set the default assumed value
        CodePhrase cp = cphrase.getAssumedValue();
        if (cp != null){
            assumed_definining_code = cp.getCodeString();
        }
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public String getAssumedValueCode(){
        return assumed_definining_code;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvCodedText))
            return false;
        String codestring = ((DvCodedText) qty).getDefiningCode().getCodeString();

        if (defining_codes.containsKey(codestring))
            return true;

        return false;
    }

    public static boolean validate(DataValue value, CATTRIBUTE[] cattributes){
        return true;
    }
}
