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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.schemas.v1.ARCHETYPECONSTRAINT;
import org.openehr.schemas.v1.CCODEPHRASE;
import org.openehr.schemas.v1.CONSTRAINTREF;
import org.openehr.schemas.v1.CSINGLEATTRIBUTE;

import java.util.Map;

/**
 * Created by christian on 8/10/2016.
 */
public class CDvCodedText extends CConstraint implements I_CArchetypeConstraintValidate {

    Logger logger = LogManager.getLogger(CDvCodedText.class);

    protected CDvCodedText(Map<String, Map<String, String>> localTerminologyLookup) {
        super(localTerminologyLookup);
    }

    @Override
    public void validate(String path, Object aValue, ARCHETYPECONSTRAINT archetypeconstraint) throws Exception {

        DvCodedText checkValue = (DvCodedText)aValue;

        if (!(archetypeconstraint instanceof CSINGLEATTRIBUTE))
            ValidationException.raise(path, "Constraint for DvCodedText is not applicable:"+archetypeconstraint, "SYS01");
        CSINGLEATTRIBUTE csingleattribute = (CSINGLEATTRIBUTE)archetypeconstraint;

        Object object = csingleattribute.getChildrenArray(0);

        if (!(object instanceof CCODEPHRASE)) {
            if (object instanceof CONSTRAINTREF) //safely ignore it!
            {
                logger.warn("Constraint reference is not supported, path:"+path);
                return;
            }
            ValidationException.raise(path, "Constraint child is not a code phrase constraint:" + object, "SYS01");
        }
        CCODEPHRASE ccodephrase = (CCODEPHRASE)object;

        if (checkValue.getDefiningCode().getTerminologyId() != null)
            if (checkValue.getDefiningCode().getCodeString() == null)
                ValidationException.raise(path, "A code is required when a terminology is specified:"+checkValue, "TEXT01");

        if (ccodephrase.getCodeListArray().length == 0)
            return;

//CHC, 21.12.16: ticket #10. Loosen validation on coded text
//        for (String termKey: ccodephrase.getCodeListArray()) {
//            String matcher = localTerminologyLookup.get(lookupPath(path)).get(termKey);
//            if (matcher.equals(checkValue.getValue()))
//                return;
//        }
//        ValidationException.raise(path, "Value does not match any defined codes,found:"+aValue, "TEXT01");
    }

    private String lookupPath(String path){
        int last = path.lastIndexOf("[openEHR-");
        last = path.indexOf("]", last);

        return path.substring(0, last+1);
    }
}
