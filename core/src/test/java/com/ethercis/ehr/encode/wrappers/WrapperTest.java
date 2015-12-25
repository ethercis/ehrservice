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

package com.ethercis.ehr.encode.wrappers;

import org.junit.Test;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.basic.DvState;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvParagraph;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.identification.HierObjectID;

/**
 * Created by christian on 11/17/2015.
 */
public class WrapperTest {


    @Test
    public void testGenerate(){
        CodePhrase codePhrase = CodePhraseVBean.generate();
        DvBoolean dvBoolean = DvBooleanVBean.generate();
        DvCodedText dvCodedText = DvCodedTextVBean.generate();
        DvCount dvCount = DvCountVBean.generate();
        DvDateTime dvDateTime = DvDateTimeVBean.generate();
        DvDate dvDate = DvDateVBean.generate();
        DvDuration dvDuration = DvDurationVBean.generate();
        DvEHRURI dvEHRURI = DvEHRURIVBean.generate();
        DvIdentifier dvIdentifier = DvIdentifierVBean.generate();
        DvInterval dvInterval = DvIntervalVBean.generate();
        DvMultimedia dvMultimedia = DvMultimediaVBean.generate();
        DvOrdinal dvOrdinal = DvOrdinalVBean.generate();
        DvParagraph dvParagraph = DvParagraphVBean.generate();
        DvParsable dvParsable = DvParsableVBean.generate();
        DvProportion dvProportion = DvProportionVBean.generate();
        DvQuantity dvQuantity = DvQuantityVBean.generate();
        DvState dvState = DvStateVBean.generate();
        DvText dvText = DvTextVBean.generate();
        DvTime dvTime = DvTimeVBean.generate();
        DvURI dvURI = DvURIVBean.generate();
        HierObjectID hierObjectID = HierObjectIDVBean.generate();
        Participation participation = ParticipationVBean.generate();
        PartyIdentified partyIdentified = PartyIdentifiedVBean.generate();
        String string = StringVBean.generate();
    }
}
