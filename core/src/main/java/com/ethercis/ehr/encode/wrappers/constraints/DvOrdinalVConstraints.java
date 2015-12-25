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

import org.openehr.build.SystemValue;
import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.ontology.ArchetypeTerm;
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal;
import org.openehr.am.openehrprofile.datatypes.quantity.Ordinal;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.quantity.DvOrdinal;
import org.openehr.rm.datatypes.text.CodePhrase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christian Chevalley on 7/7/2014.
 */
public class DvOrdinalVConstraints extends DataValueConstraints {

    private final int CODE_TEXT = 1;
    private final int CODE_DESCRIPTION = 2;

    //a map of <support::code>,text value, description
    //the descriptive text is localized depending on the language set
    private Map<Integer, Map<Integer, String>> value_set = new HashMap<Integer, Map<Integer, String>>();
    private Integer assumed_value_code = -1;

    public DvOrdinalVConstraints(RMObjectBuilder builder, DataValue parent) {
        super(builder, parent);
    }

    public void putValueSet(Integer code, String text, String description) {
        HashMap<Integer, String> texts = new HashMap<Integer, String>();
        texts.put(CODE_TEXT, text);
        texts.put(CODE_DESCRIPTION, description);
        value_set.put(code, texts);
    }

    public String getText(String code) {
        Map<Integer, String> texts = value_set.get(code);

        if (texts == null)
            return null;

        return texts.get(CODE_TEXT);
    }

    public String getDescription(String code) {
        Map<Integer, String> texts = value_set.get(code);

        if (texts == null)
            return null;

        return texts.get(CODE_DESCRIPTION);
    }

    @Override
    public void setConstraints(Archetype archetype, CAttribute valueAttribute) {
        String language; //use the language defined for the builder if any, otherwise use the archetype default
        String defaultlanguage = archetype.getOriginalLanguage().getCodeString();

        CodePhrase setlang = (CodePhrase) builder.getSystemValues().get(SystemValue.LANGUAGE);
        if (setlang != null)
            language = setlang.getCodeString();
        else {
            language = defaultlanguage; //use default from archetype
        }

        //get the code list for this coded text;
        for (Ordinal code : ((CDvOrdinal) (valueAttribute.getChildren().get(0))).getList()) {
//            System.out.println("code:"+code);
            ArchetypeTerm term = archetype.getOntology().termDefinition(language, code.getSymbol().getCodeString());
            putValueSet(code.getValue(), term.getText(), term.getDescription());
        }

        //set the default assumed value
        CDvOrdinal ord = (CDvOrdinal) valueAttribute.getChildren().get(0);
        if (ord.hasAssumedValue()) {
            assumed_value_code = ord.getAssumedValue().getValue();
        }
    }

    @Override
    public DataValueConstraints getConstraints() {
        return this;
    }

    public Integer getAssumedValueCode() {
        return assumed_value_code;
    }

    @Override
    public boolean validate(DataValue qty) {
        if (!(qty instanceof DvOrdinal))
            return false;

        return (value_set.containsKey(((DvOrdinal) qty).getValue()));
    }
}