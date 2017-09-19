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

package com.ethercis.ehr.encode;

import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import com.ethercis.ehr.encode.wrappers.json.*;
import com.ethercis.ehr.encode.wrappers.json.serializer.*;
import com.ethercis.ehr.encode.wrappers.json.writer.*;
import com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw.ArrayListAdapter;
import com.ethercis.ehr.encode.wrappers.json.writer.translator_db2raw.LinkedTreeMapAdapter;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.EventContext;
import org.openehr.rm.composition.content.entry.Action;
import org.openehr.rm.composition.content.entry.Evaluation;
import org.openehr.rm.composition.content.entry.Instruction;
import org.openehr.rm.composition.content.entry.Observation;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
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
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.identification.ArchetypeID;
import org.openehr.rm.support.identification.GenericID;
import org.openehr.rm.support.identification.PartyRef;
import org.openehr.rm.support.identification.TerminologyID;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by christian on 9/9/2016.
 */
public class EncodeUtil {

    /**
     * utility to make sure writer adapter are set consistently
     * @return GsonBuilder
     */
    public static GsonBuilder getGsonBuilderInstance(){
        GsonBuilder builder = new GsonBuilder()
        .registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter())
        .registerTypeAdapter(DvDate.class, new DvDateAdapter())
        .registerTypeAdapter(DvTime.class, new DvTimeAdapter())
        .registerTypeAdapter(DvDuration.class, new DvDurationAdapter())
        .registerTypeAdapter(DvText.class, new DvTextAdapter())
        .registerTypeAdapter(DvCodedText.class, new DvCodedTextAdapter())
        .registerTypeAdapter(CodePhrase.class, new CodePhraseAdapter());
        return builder;
    }

    public static GsonBuilder getGsonBuilderInstance(I_DvTypeAdapter.AdapterType adapterType){
        if (adapterType == I_DvTypeAdapter.AdapterType.RAW_JSON) {
            GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapter(DvDateTime.class, new DvDateTimeSerializer(adapterType))
            .registerTypeAdapter(DvDate.class, new DvDateSerializer(adapterType))
            .registerTypeAdapter(DvTime.class, new DvTimeSerializer(adapterType))
            .registerTypeAdapter(DvDuration.class, new DvDurationSerializer(adapterType))
            .registerTypeAdapter(DvText.class, new DvTextSerializer(adapterType))
            .registerTypeAdapter(DvCodedText.class, new DvCodedTextSerializer(adapterType))
            .registerTypeAdapter(CodePhrase.class, new CodePhraseSerializer(adapterType))
            .registerTypeHierarchyAdapter(Collection.class, new CollectionSerializer(adapterType))
            .registerTypeAdapter(DvOrdinal.class, new DvOrdinalSerializer(adapterType))
            .registerTypeAdapter(DvQuantity.class, new DvQuantitySerializer(adapterType))
            .registerTypeAdapter(DvBoolean.class, new DvBooleanSerializer(adapterType))
            .registerTypeAdapter(DvCount.class, new DvCountSerializer(adapterType))
            .registerTypeAdapter(DvIdentifier.class, new DvIdentifierSerializer(adapterType))
            .registerTypeAdapter(DvInterval.class, new DvIntervalSerializer(adapterType))
            .registerTypeAdapter(DvMultimedia.class, new DvMultiMediaSerializer(adapterType))
            .registerTypeAdapter(DvParagraph.class, new DvParagraphSerializer(adapterType))
            .registerTypeAdapter(DvParsable.class, new DvParsableSerializer(adapterType))
            .registerTypeAdapter(DvProportion.class, new DvProportionSerializer(adapterType))
            .registerTypeAdapter(DvURI.class, new DvURISerializer(adapterType))
            .registerTypeAdapter(TerminologyID.class, new TerminologyIDSerializer(adapterType))
            .registerTypeAdapter(Action.class, new ActionSerializer(adapterType))
            .registerTypeAdapter(Instruction.class, new InstructionSerializer(adapterType))
            .registerTypeAdapter(Observation.class, new ObservationSerializer(adapterType))
            .registerTypeAdapter(Evaluation.class, new EvaluationSerializer(adapterType))
            .registerTypeAdapter(Archetyped.class, new ArchetypedSerializer(adapterType))
            .registerTypeAdapter(ArchetypeID.class, new ArchetypeIDSerializer(adapterType))
            .registerTypeAdapter(Cluster.class, new ClusterSerializer(adapterType))
            .registerTypeAdapter(Section.class, new SectionSerializer(adapterType))
            .registerTypeAdapter(Composition.class, new CompositionRawSerializer(adapterType))
            .registerTypeAdapter(EventContext.class, new EventContextSerializer(adapterType))
            .registerTypeAdapter(PartyIdentified.class, new PartyIdentifiedSerializer(adapterType))
            .registerTypeAdapter(PartyRef.class, new PartyRefSerializer(adapterType))
            .registerTypeAdapter(GenericID.class, new GenericIDSerializer(adapterType))
            .registerTypeAdapter(History.class, new HistorySerializer(adapterType))
            .registerTypeAdapter(PointEvent.class, new PointEventSerializer(adapterType))
            .registerTypeHierarchyAdapter(ElementWrapper.class, new ElementWrapperSerializer(adapterType));
            return builder;
        }
        else if (adapterType == I_DvTypeAdapter.AdapterType.DBJSON2RAWJSON){
            GsonBuilder builder = new GsonBuilder()
                    .registerTypeAdapter(LinkedTreeMap.class, new LinkedTreeMapAdapter())
                    .registerTypeAdapter(ArrayList.class, new ArrayListAdapter());
            return builder;
        }
        else if (adapterType == I_DvTypeAdapter.AdapterType.PG_JSONB)
            return getGsonBuilderInstance();

        return null;
    }

}
