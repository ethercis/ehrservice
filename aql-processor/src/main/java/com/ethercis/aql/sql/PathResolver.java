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

package com.ethercis.aql.sql;

import com.ethercis.aql.containment.Containment;
import com.ethercis.aql.containment.IdentifierMapper;
import com.ethercis.aql.sql.binding.ContainBinder;
import com.ethercis.aql.sql.queryImpl.CompositionAttributeQuery;
import com.ethercis.aql.sql.queryImpl.JsonbEntryQuery;
import org.apache.log4j.Logger;
import org.jooq.*;

import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.*;

/**
 * Resolve the path corresponding to a symbol in a given context
 * <p>
 *     Path are resolved at runtime by performing a query on the CONTAINMENT table.
 *     For example to resolve the path of contained archetype 'openEHR_EHR_OBSERVATION_laboratory_test_v0'
 *     in composition 'openEHR_EHR_COMPOSITION_report_result_v1', the following query is executed
 *      <pre><code>
 *      select "ehr"."containment"."path"
 *          from "ehr"."containment"
 *          where (
 *              "ehr"."containment"."comp_id" = 'b97e9fde-d994-4874-b671-8b1cd81b811c'
 *              and (label ~ 'openEHR_EHR_COMPOSITION_report_result_v1.*.openEHR_EHR_OBSERVATION_laboratory_test_v0')
 *          )
 *      </code></pre>
 *      The found path is for example: <code>/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']</code>
 *      it is used then to build the actual path to a datavalue
 * </p>
 * Created by christian on 5/3/2016.
 */
public class PathResolver {
    Logger logger = Logger.getLogger(PathResolver.class);
    DSLContext context;
    //query string format: SELECT path from containment where comp_id = 'UUID' AND and label ~ 'lquery expression'
//    private static String select01 = "SELECT path from ehr.containment where comp_id = ";
//    private static String label01 = "and label ~ ";

    private IdentifierMapper mapper;


    public PathResolver(DSLContext context, IdentifierMapper mapper){
        this.context = context;
        this.mapper = mapper;
    }

    private String lqueryExpression(String identifier){
        Object containment = mapper.getContainer(identifier);

        if (!(containment instanceof Containment))
            throw new IllegalArgumentException("No path found for identifier:"+identifier);

        return ContainBinder.buildLquery((Containment) containment);
    }

    public String pathOf(String identifier, UUID comp_id){
        String lquery = lqueryExpression(identifier);
//        String query = select01 + "'"+comp_id+"' "+ label01 + "'"+lquery +"'";
        //query the DB to get the path
        String labelWhere = "label ~ '"+lquery+"'";
        Result<?> records = context.select(CONTAINMENT.PATH).from(CONTAINMENT).where(CONTAINMENT.COMP_ID.eq(comp_id)).and(labelWhere).fetch();
//        Result<?> records = context.fetch(query);

        if (records.isEmpty()){
            logger.debug("No path found for identifier (query return no records):" + identifier);
            return null;
        }
        if (records.size() > 1){
            logger.debug("Multiple paths found for identifier, returning first one:" + identifier);
        }

        String path = (String)records.get(0).getValue("path");
        return path;

    }

    public String pathOf(String identifier){
        return mapper.getPath(identifier);

    }

    /**
     * resolve all the paths in the current containment mapper for a composition
     * @param comp_id
     */
    public void resolvePaths(UUID comp_id){
        SelectField<?>[] selectFields = {
                CONTAINMENT.PATH
        } ;
        Table<?> from = CONTAINMENT;

        for (String identifier: mapper.identifiers()) {
            try {
                String lquery = lqueryExpression(identifier);

//                String query = select01 + "'" + comp_id + "' " + label01 + "'" + lquery + "'";

                //query the DB to get the path
                String labelWhere = "label ~ '"+lquery+"'";
                Result<?> records = context.select(CONTAINMENT.PATH).from(from).where(CONTAINMENT.COMP_ID.eq(comp_id)).and(labelWhere).fetch();

//                Result<Record> records = context.fetch(query);

                if (records.isEmpty()) {
                    logger.debug("No path found for identifier (query return no records):" + identifier);
                }
                if (records.size() > 1) {
                    logger.debug("Multiple paths found for identifier, returning first one:" + identifier);
                }

                String path = (String) records.get(0).getValue("path");
                mapper.setPath(identifier, path);
                if (((Containment)mapper.getContainer(identifier)).getClassName().equals("COMPOSITION")){
                    mapper.setQueryStrategy(identifier, CompositionAttributeQuery.class);
                }
                else
                    mapper.setQueryStrategy(identifier, JsonbEntryQuery.class);
            } catch (IllegalArgumentException e){
                logger.debug("No path for:"+e);
            }
        }
    }

    public void resolvePaths(){
        SelectField<?>[] selectFields = {
                CONTAINMENT.PATH
        } ;
        Table<?> from = CONTAINMENT;

        for (String identifier: mapper.identifiers()) {
            try {
                mapper.setPath(identifier, null);
                if (mapper.getContainer(identifier) != null
                        && mapper.getContainer(identifier) instanceof Containment
                        && ((Containment)mapper.getContainer(identifier)).getClassName().equals("COMPOSITION")){
                    mapper.setQueryStrategy(identifier, CompositionAttributeQuery.class);
                }
                else
                    mapper.setQueryStrategy(identifier, JsonbEntryQuery.class);
            } catch (IllegalArgumentException e){
                logger.debug("No path for:"+e);
            }
        }
    }
}
