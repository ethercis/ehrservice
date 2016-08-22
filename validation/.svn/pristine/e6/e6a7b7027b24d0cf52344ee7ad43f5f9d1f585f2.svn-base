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

package com.ethercis.validation.wrappers;

import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.schemas.v1.ARCHETYPECONSTRAINT;
import org.openehr.schemas.v1.CCODEPHRASE;
import org.openehr.schemas.v1.TERMINOLOGYID;

import java.util.Map;

/**
 * Created by christian on 7/24/2016.
 */
public class CCodePhrase extends CConstraint implements  I_CArchetypeConstraintValidate {

    protected CCodePhrase(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, ARCHETYPECONSTRAINT archetypeconstraint) throws Exception {

        if (!(aValue instanceof CodePhrase))
            throw new IllegalArgumentException("INTERNAL: argument is not a CodePhrase");

        CodePhrase codePhrase = (CodePhrase)aValue;
        CCODEPHRASE ccodephrase = (CCODEPHRASE)archetypeconstraint;

        //check terminology
        TERMINOLOGYID terminologyid = ccodephrase.getTerminologyId();

        if (!codePhrase.getTerminologyId().getValue().equals(terminologyid.getValue()))
            throw new IllegalArgumentException("CodePhrase terminology does not match, expected:"+terminologyid.getValue()+", found:"+ccodephrase.getTerminologyId().getValue());

        code_match:
        {
            if (ccodephrase.sizeOfCodeListArray() > 0) {
                //should match one in the list
                for (String code : ccodephrase.getCodeListArray()) {
                    if (codePhrase.getCodeString().equals(code))
                        break code_match;
                }
                throw new IllegalArgumentException("CodePhrase code does not match any option, found:" + codePhrase.getCodeString());
            }
        }

    }
}
