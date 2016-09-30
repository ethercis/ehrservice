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

package com.ethercis.aql.sql.queryImpl;

import com.ethercis.aql.definition.VariableDefinition;
import com.ethercis.jooq.pg.Tables;
import com.ethercis.aql.sql.PathResolver;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * map an AQL datavalue expression into a SQL field
 * TODO: used external representation to make it more flexible...
 * Created by christian on 5/6/2016.
 */
public class CompositionAttributeQuery extends ObjectQuery implements I_QueryImpl {

    private String serverNodeId;
    private String columnAlias;
    private boolean containsEhrStatus = false;
    private boolean containsEhrId = false;
    private String ehrIdAlias;

    public CompositionAttributeQuery(DSLContext context, PathResolver pathResolver, List<VariableDefinition> definitions, String serverNodeId){
        super(context, pathResolver, definitions);
        this.serverNodeId = serverNodeId;
    }

    @Override
    public Field<?> makeField(UUID compositionId, String identifier, VariableDefinition variableDefinition, boolean withAlias) {
        //resolve composition attributes and/or context
        columnAlias = variableDefinition.getPath();
        if (columnAlias == null)
            return null;
        switch (columnAlias){
            case "uid/value":
                if (withAlias)
                    return uid(compositionId, withAlias, variableDefinition.getAlias());
                else
                    return rawUid(compositionId, withAlias, variableDefinition.getAlias());
//                return rawUid(compositionId, withAlias, variableDefinition.getAlias());
            case "name/value":
                return name(compositionId, withAlias, variableDefinition.getAlias());
            case "archetype_node_id":
                return archetypeNodeId(compositionId, withAlias, variableDefinition.getAlias());
            case "template_id":
                return templateId(compositionId, withAlias, variableDefinition.getAlias());
            case "language/value":
                return language(compositionId,withAlias, variableDefinition.getAlias());
            case "territory/value":
                return territory(compositionId, withAlias, variableDefinition.getAlias());
            case "composer/name":
                return composerNameValue(compositionId, withAlias, variableDefinition.getAlias());
            case "composer/id/namespace":
                return composerIdNamespace(compositionId, withAlias, variableDefinition.getAlias());
            case "composer/id/scheme":
                return composerIdScheme(compositionId, withAlias, variableDefinition.getAlias());
            case "composer/id/ref":
                return composerIdRef(compositionId, withAlias, variableDefinition.getAlias());
            case "composer/type":
                return composerType(compositionId, withAlias, variableDefinition.getAlias());
            case "context/start_time/value":
                return contextStartTime(compositionId, withAlias, variableDefinition.getAlias());
            case "context/end_time/value":
                return contextEndTime(compositionId, withAlias, variableDefinition.getAlias());
            case "context/location":
                return contextLocation(compositionId, withAlias, variableDefinition.getAlias());
            case "context/facility/name/value":
                return contextFacilityName(compositionId, withAlias, variableDefinition.getAlias());
            case "context/facility/id/namespace":
                return contextFacilityNamespace(compositionId, withAlias, variableDefinition.getAlias());
            case "context/facility/id/ref":
                return contextFacilityRef(compositionId, withAlias, variableDefinition.getAlias());
            case "context/facility/id/scheme":
                return contextFacilityScheme(compositionId, withAlias, variableDefinition.getAlias());
            case "context/facility/id/type":
                return contextFacilityType(compositionId, withAlias, variableDefinition.getAlias());
            case "ehr_status/subject/external_ref/namespace":
                return ehrStatusSubjectNamespace(compositionId, withAlias, variableDefinition.getAlias());
            case "ehr_status/subject/external_ref/id/value":
                return ehrStatusSubjectIdValue(compositionId, withAlias, variableDefinition.getAlias());
            case "ehr_id/value":
                return ehrIdValue(compositionId, withAlias, variableDefinition.getAlias());


        }
        if (columnAlias.startsWith("ehr_status/other_details"))
        {
            return ehrStatusOtherDetails(variableDefinition, withAlias);
        }
        return null;
    }

    @Override
    public Field<?> whereField(UUID compositionId, String identifier, VariableDefinition variableDefinition) {
        return makeField(compositionId, identifier, variableDefinition, false);
    }

    private Field<?> uid(UUID compositionId, boolean alias, String aliasStr){

        Field<?> select = DSL.field(COMP_EXPAND.COMPOSITION_ID
                +"||"
                + DSL.val("::")
                +"||"
                + DSL.val(serverNodeId)
                +"||"
                + DSL.val("::")
                +"||"
                + DSL.field(context.select(DSL.count().add(1)).from(COMPOSITION_HISTORY).where(Tables.COMPOSITION_HISTORY.ID.eq(compositionId))), SQLDataType.VARCHAR)
                .as(alias && aliasStr!= null && !aliasStr.isEmpty() ? aliasStr : "uid")
                ;

        return select;
    }

    private Field<?> rawUid(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSITION_ID).as(aliasStr == null ? columnAlias : aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSITION_ID);
    }

    private Field<?> name(UUID compositionId, boolean alias, String aliasStr){
        //postgresql equivalent expression
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSITION_NAME).as(aliasStr == null ? columnAlias : aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSITION_NAME);
    }

    private Field<?> archetypeNodeId(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.ARCHETYPE_ID).as(aliasStr == null ? columnAlias : aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.ARCHETYPE_ID);
    }

    private Field<?> templateId(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.TEMPLATE_ID).as(aliasStr == null ? columnAlias : aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.TEMPLATE_ID);
    }

    private Field<?> language(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.LANGUAGE).as(aliasStr == null ? columnAlias : aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.LANGUAGE);
    }

    private Field<?> territory(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.TERRITORY).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.TERRITORY);
    }

    private Field<?> composerNameValue(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSER_NAME).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSER_NAME);
    }

    private Field<?> composerIdNamespace(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSER_NAMESPACE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSER_NAMESPACE);
    }

    private Field<?> composerIdScheme(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSER_SCHEME).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSER_SCHEME);
    }

    private Field<?> composerIdRef(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSER_REF).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSER_REF);
    }

    private Field<?> composerType(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.COMPOSER_TYPE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.COMPOSER_TYPE);
    }

    private Field<?> contextStartTime(UUID compositionId,boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field("to_char(" + COMP_EXPAND.START_TIME + ",'YYYY-MM-DD\"T\"HH24:MI:SS')" + "||" + COMP_EXPAND.START_TIME_TZID)
                    .as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field("to_char(" + COMP_EXPAND.START_TIME + ",'YYYY-MM-DD\"T\"HH24:MI:SS')" + "||" + COMP_EXPAND.START_TIME_TZID);
    }

    private Field<?> contextEndTime(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field("to_char(" + COMP_EXPAND.END_TIME + ",'YYYY-MM-DD\"T\"HH24:MI:SS')" + "||" + COMP_EXPAND.END_TIME_TZID)
                    .as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field("to_char(" + COMP_EXPAND.END_TIME + ",'YYYY-MM-DD\"T\"HH24:MI:SS')" + "||" + COMP_EXPAND.END_TIME_TZID);
    }

    private Field<?> contextLocation(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.CTX_LOCATION).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.CTX_LOCATION);
    }

    private Field<?> contextFacilityName(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.FACILITY_NAME).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.FACILITY_NAME);
    }

    private Field<?> contextFacilityRef(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.FACILITY_REF).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.FACILITY_REF);
    }

    private Field<?> contextFacilityScheme(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.FACILITY_SCHEME).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.FACILITY_SCHEME);
    }

    private Field<?> contextFacilityNamespace(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.FACILITY_NAMESPACE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.FACILITY_NAMESPACE);
    }

    private Field<?> contextFacilityType(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.FACILITY_TYPE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.FACILITY_TYPE);
    }

    private Field<?> ehrStatusSubjectIdValue(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.SUBJECT_EXTERNALREF_ID_VALUE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.SUBJECT_EXTERNALREF_ID_VALUE);
    }

    private Field<?> ehrStatusSubjectNamespace(UUID compositionId, boolean alias, String aliasStr){
        if (alias) {
            Field<?> select = DSL.field(COMP_EXPAND.SUBJECT_EXTERNALREF_ID_NAMESPACE).as(aliasStr == null ? columnAlias:aliasStr);
            return select;
        }
        else
            return DSL.field(COMP_EXPAND.SUBJECT_EXTERNALREF_ID_NAMESPACE);
    }

    private Field<?> ehrIdValue(UUID compositionId, boolean alias, String aliasStr){
        containsEhrId = true;
        ehrIdAlias = (aliasStr == null ? columnAlias : aliasStr);
        if (!containsEhrStatus()) {
            if (alias) {
                Field<?> select = DSL.field("DISTINCT {0}", COMP_EXPAND.EHR_ID).as(aliasStr == null ? columnAlias : aliasStr);
                return select;
            } else
                return DSL.field(COMP_EXPAND.EHR_ID);
        }
        else {
            if (alias) {
                Field<?> select = DSL.field("DISTINCT {0}", STATUS.EHR_ID).as(aliasStr == null ? columnAlias : aliasStr);
                return select;
            } else
                return DSL.field(STATUS.EHR_ID);
        }
    }

    private Field<?> ehrStatusOtherDetails(VariableDefinition variableDefinition, boolean withAlias){
        containsEhrStatus = true;
        String variablePath = variableDefinition.getPath().substring("ehr_status/other_details".length() + 1);
        Field<?> field = JsonbEntryQuery.makeField(JsonbEntryQuery.OTHER_ITEM.OTHER_DETAILS, null, variableDefinition.getAlias(), variablePath, withAlias);
        return field;
    }

    public boolean containsEhrStatus() {
        return containsEhrStatus;
    }
    public boolean containsEhrId() {
        return containsEhrId;
    }

    public String getEhrIdAlias() {
        return ehrIdAlias;
    }
}
