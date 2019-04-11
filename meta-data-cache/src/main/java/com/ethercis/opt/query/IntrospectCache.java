package com.ethercis.opt.query;

import com.ethercis.ehr.knowledge.I_KnowledgeCache;
import com.ethercis.jooq.pg.tables.records.TemplateRecord;
import com.ethercis.opt.OptVisitor;
import openEHR.v1.template.TEMPLATE;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.nustaq.serialization.FSTConfiguration;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ethercis.jooq.pg.Tables.TEMPLATE;

/**
 * Cache for introspection and parsed operational templates.
 * Used to speed up resolution of template's meta data to perform AQL->SQL translation. For
 * example, to identify the actual type of a value point or cardinality of a node (array)
 * Created by christian on 5/10/2018.
 */
public class IntrospectCache implements I_IntrospectCache {

    Logger log = LogManager.getLogger(IntrospectCache.class);
    DSLContext context;
    I_KnowledgeCache knowledge;
    Map<UUID, I_QueryOptMetaData> jsonPathVisitorMap = new ConcurrentHashMap<>();
    boolean hasUnresolvedTemplateUid = false;

    public IntrospectCache(DSLContext context, I_KnowledgeCache knowledgeCache) {
        this.knowledge = knowledgeCache;
        this.context = context;
    }

    @Override
    public I_QueryOptMetaData visitor(UUID uuid) throws Exception {

        I_QueryOptMetaData retval;

        if (jsonPathVisitorMap.containsKey(uuid))
            retval = jsonPathVisitorMap.get(uuid);
        else {
            //get the template introspect from the DB

            byte[] bytes = context.select(TEMPLATE.VISITOR).from(TEMPLATE).where(TEMPLATE.UID.eq(uuid)).fetchOne(0, byte[].class);
            if (bytes != null && bytes.length > 0) {
                //compile the visitor
//                I_QueryOptMetaData visitor = new QueryOptMetaData().getInstance(bytes);
                log.info("Updating visitor cache for template in DB with UUID:" + uuid);

                FSTConfiguration serializer = FSTConfiguration.createDefaultConfiguration();
                I_QueryOptMetaData visitor = QueryOptMetaData.getInstance(serializer.asObject(bytes));
                //cache it
                jsonPathVisitorMap.put(uuid, visitor);

                retval = visitor;
            } else {
                OPERATIONALTEMPLATE operationaltemplate = knowledge.retrieveOperationalTemplate(uuid);
                if (operationaltemplate != null) {
                    log.info("Updating visitor cache for template:" + knowledge.retrieveOperationalTemplate(uuid).getTemplateId());
                    //synchronize the repository
                    synchronize(uuid);
                    //then return the visitor recursively
                    return visitor(uuid);
                } else {
                    retval = null;
                }
            }

        }
        return retval;
    }

    @Override
    public I_QueryOptMetaData visitor(String templateId) throws Exception {

        //get the matching template if any
        OPERATIONALTEMPLATE operationaltemplate = (OPERATIONALTEMPLATE) knowledge.retrieveTemplate(templateId);

        if (operationaltemplate != null)
            return visitor(UUID.fromString(operationaltemplate.getUid().getValue()));
        else {
            if (synchronize(templateId) != null) {
                OPERATIONALTEMPLATE cachedOpt = knowledge.retrieveOperationalTemplate(templateId);
                if (cachedOpt != null) {
                    UUID uuid = UUID.fromString(cachedOpt.getUid().getValue());
                    return visitor(uuid);
                } else
                    throw new IllegalArgumentException("Could not retrieve knowledge cache for template id:" + templateId);
            } else {
                throw new IllegalArgumentException("Could not synchronize cache for template id:" + templateId);
            }
        }
    }


    /**
     * synchronize the cache and DB with templates in knowledge cache
     */
    @Override
    public IntrospectCache synchronize() throws Exception {

        log.info("Synchronizing meta data cache DB with templates in Knowledge cache (operational templates path = " + knowledge.getOptPath() + ")");
        Map<String, Collection<Map<String, String>>> operationalTemplateMap = knowledge.listOperationalTemplates();
        int count = 0;
        for (Map<String, String> defMap : operationalTemplateMap.get(knowledge.TEMPLATES)) {
            if (defMap.containsKey(knowledge.ERROR))
                continue;
            else {
                ++count;
                upsertTemplateMetaData(defMap);
            }
        }
        log.info("Synchronizing done");
        return this;
    }

    @Override
    public IntrospectCache synchronize(UUID uuid) throws Exception {

        log.info("Synchronizing meta data cache DB for template in Knowledge cache (operational templates path = " + knowledge.getOptPath() + ")");
        Map<String, Collection<Map<String, String>>> operationalTemplateMap = knowledge.listOperationalTemplates();
        int count = 0;
        for (Map<String, String> defMap : operationalTemplateMap.get(knowledge.TEMPLATES)) {
            if (defMap.containsKey(knowledge.ERROR))
                continue;
            else {
                if (UUID.fromString(defMap.get(knowledge.UID)).equals(uuid)) {
                    upsertTemplateMetaData(defMap);
                    count++;
                    break;
                }
            }
        }
        log.info("Synchronizing done for " + count + " entry");
        return this;
    }

    @Override
    public IntrospectCache synchronize(String templateId) throws Exception {

        log.info("Synchronizing meta data cache DB for template in Knowledge cache (operational templates path = " + knowledge.getOptPath() + ")");
        Map<String, Collection<Map<String, String>>> operationalTemplateMap = knowledge.listOperationalTemplates();
        int count = 0;
        for (Map<String, String> defMap : operationalTemplateMap.get(knowledge.TEMPLATES)) {
            if (defMap.containsKey(knowledge.ERROR))
                continue;
            else {
                if (defMap.get(knowledge.TEMPLATE_ID).equals(templateId)) {
                    upsertTemplateMetaData(defMap);
                    count++;
                    break;
                }
            }
        }
        log.info("Synchronizing done for " + count + " entry");
        return this;
    }

    private int upsertTemplateMetaData(Map<String, String> definitions) throws Exception {

        UUID templateUid = UUID.fromString(definitions.get(knowledge.UID));

        if (jsonPathVisitorMap.containsKey(templateUid))
            return 1; //already in cache

        FSTConfiguration serializer = FSTConfiguration.createDefaultConfiguration();
        //check if the entry exists first
        OPERATIONALTEMPLATE operationaltemplate = knowledge.retrieveOperationalTemplate(templateUid);
        Map map = new OptVisitor().traverse(operationaltemplate);
        I_QueryOptMetaData visitor = QueryOptMetaData.getInstance(new MapJson(map).toJson());

        int result = 0;

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] opt;
        try {
            opt = Files.readAllBytes(Paths.get(definitions.get(knowledge.PATH)));
        }
        catch (Exception e){
            log.error("File could not be read, could not compute MD5 for opt:"+knowledge.TEMPLATE_ID+"file:"+knowledge.PATH);
            opt = new String().getBytes();
        }

        if (context.fetchExists(TEMPLATE, TEMPLATE.UID.eq(templateUid))) {
            //update introspect and parsed opt if required
            log.info("Updating template DB entry:" + definitions.get(knowledge.TEMPLATE_ID));

            //check if introspect and parsed_opt are empty first
            Result<TemplateRecord> templateRecords = context.selectFrom(TEMPLATE).where(TEMPLATE.UID.eq(templateUid)).fetch();

            if (templateRecords.size() > 1)
                throw new IllegalArgumentException("Not consistent db, found multiple templates (" + templateRecords.size() + ") with same uid:" + templateUid);

            TemplateRecord templateRecord = templateRecords.get(0);

            if (templateRecord.getIntrospect() == null || templateRecord.getParsedOpt() == null || templateRecord.getVisitor() == null) {

                result = context.update(TEMPLATE)
                        .set(TEMPLATE.PARSED_OPT, DSL.val(serializer.asByteArray(operationaltemplate)))
                        .set(TEMPLATE.INTROSPECT, (Object) DSL.field(DSL.val(new MapJson(map).toJson()) + "::jsonb"))
                        .set(TEMPLATE.VISITOR, DSL.val(serializer.asByteArray(visitor.getJsonPathVisitor())))
                        .where(TEMPLATE.UID.eq(templateUid))
                        .execute();
                if (result == 0) {
                    log.warn("Could not update template meta data for template id:" + templateUid);
                } else
                    jsonPathVisitorMap.put(templateUid, visitor);
            } else { //just update the cache
                jsonPathVisitorMap.put(templateUid, visitor);
            }

        } else { //new record
            log.info("Inserting new template entry:" + definitions.get(knowledge.TEMPLATE_ID));

            result = context.insertInto(TEMPLATE,
                    TEMPLATE.UID,
                    TEMPLATE.TEMPLATE_ID,
                    TEMPLATE.CONCEPT,
                    TEMPLATE.INTROSPECT,
                    TEMPLATE.PARSED_OPT,
                    TEMPLATE.VISITOR,
                    TEMPLATE.MD5)
                    .values(DSL.val(templateUid),
                            DSL.val(definitions.get(knowledge.TEMPLATE_ID)),
                            DSL.val(definitions.get(knowledge.CONCEPT)),
                            DSL.field(DSL.val(new MapJson(map).toJson()) + "::jsonb"),
                            DSL.val(serializer.asByteArray(operationaltemplate)),
                            DSL.val(serializer.asByteArray(visitor.getJsonPathVisitor())),
                            DSL.val(messageDigest.digest(opt)))
                    .execute();

            if (result == 0) {
                log.warn("Could not insert template meta data for template id:" + templateUid);
            } else
                jsonPathVisitorMap.put(templateUid, visitor);
        }

        return result;

    }

    /**
     * load the cache from the DB
     */
    @Override
    public IntrospectCache load() {

        FSTConfiguration serializer = FSTConfiguration.createDefaultConfiguration();

        context.selectFrom(TEMPLATE)
                .where(TEMPLATE.INTROSPECT.isNotNull())
                .fetch()
                .forEach(record -> {
                            UUID uid = record.getUid();
                            if (!jsonPathVisitorMap.containsKey(uid)) {
                                //load it in the cache
                                try {
                                    //get the visitor from the DB
                                    I_QueryOptMetaData visitor = null;
                                    if (record.getVisitor() != null) {
                                        Object encodedVisitor = serializer.asObject(record.getVisitor());
                                        if (encodedVisitor != null)
                                            visitor = QueryOptMetaData.getInstance(encodedVisitor);
//                                    I_QueryOptMetaData visitor = new QueryOptMetaData().getInstance(record.getIntrospect().toString());
                                        OPERATIONALTEMPLATE operationaltemplate = knowledge.retrieveOperationalTemplate(uid);
                                        if (operationaltemplate != null)
                                            log.info("Loading visitor for template:" + operationaltemplate.getTemplateId().getValue());
                                        else {
                                            log.warn("Loading visitor for template:" + record.getTemplateId() + "(!not in knowledge cache!), uid=(" + uid + ")");
                                            hasUnresolvedTemplateUid = true;
                                        }
                                    }

                                    if (visitor != null)
                                        jsonPathVisitorMap.put(uid, visitor);

                                } catch (Exception e) {
                                    throw new IllegalArgumentException("Invalid visitor for template uuid="+uid+" ("+record.getTemplateId()+")");
                                }
                            }
                        }
                );

        if (hasUnresolvedTemplateUid)
            log.warn("Knowledge cache is not in sync with template DB");

        return this;
    }

    /**
     * delete meta data cache info in DB (but keep id, template_id and concept as is)
     *
     * @throws Exception
     */
    @Override
    public int invalidateDBCache() throws Exception {

        int result = context.update(TEMPLATE)
                .set(TEMPLATE.PARSED_OPT, new byte[]{})
                .set(TEMPLATE.INTROSPECT, (Object) DSL.field(DSL.val((Byte) null) + "::jsonb"))
                .set(TEMPLATE.VISITOR, new byte[]{})
                .execute();

        if (result == 0) {
            log.warn("Could not invalidate DB cache");

        }
        return result;
    }


    /**
     * empty the cache
     */
    @Override
    public IntrospectCache invalidate() {
        jsonPathVisitorMap.clear();
        return this;
    }

    @Override
    public IntrospectCache invalidate(String templateId) {

        TemplateRecord templateRecord = context.selectFrom(TEMPLATE).where(TEMPLATE.TEMPLATE_ID.eq(templateId)).fetchAny();

        if (templateRecord != null) {
            jsonPathVisitorMap.remove(templateRecord.getUid());
            //clears the visitor entry etc.
            int result = context.update(TEMPLATE)
                    .set(TEMPLATE.PARSED_OPT, new byte[]{})
                    .set(TEMPLATE.INTROSPECT, (Object) DSL.field(DSL.val((Byte) null) + "::jsonb"))
                    .set(TEMPLATE.VISITOR, new byte[]{})
                    .where(TEMPLATE.UID.eq(templateRecord.getUid()))
                    .execute();

            if (result == 0) {
                log.warn("Could not invalidate DB cache for template:" + templateId);
            }
        }
        return this;
    }

    @Override
    public IntrospectCache erase(String templateId) {

        TemplateRecord templateRecord = context.selectFrom(TEMPLATE).where(TEMPLATE.TEMPLATE_ID.eq(templateId)).fetchAny();

        if (templateRecord != null) {
            jsonPathVisitorMap.remove(templateRecord.getUid());
            //delete the entry
            int result = context.deleteFrom(TEMPLATE)
                    .where(TEMPLATE.UID.eq(templateRecord.getUid()))
                    .execute();

            if (result == 0) {
                log.warn("Could not delete DB cache for template:" + templateId);
            }
        }
        return this;
    }

    @Override
    public int size() {
        return jsonPathVisitorMap.size();
    }

    @Override
    public List<Map<String, String>> visitors() throws Exception {
        List<Map<String, String>> retlist = new ArrayList<>();
        for (UUID uid : jsonPathVisitorMap.keySet()) {
            openEHR.v1.template.TEMPLATE template = knowledge.retrieveTemplate(uid);
            I_QueryOptMetaData queryOptMetaData = jsonPathVisitorMap.get(uid);

            Map<String, String> map = new HashMap<>();

            map.put("uuid", uid.toString());
            map.put("templateId", queryOptMetaData.getTemplateId());
            map.put("inKnowledgeCache", new Boolean(template != null).toString());

            retlist.add(map);
        }

        return retlist;
    }

    @Override
    public I_IntrospectCache setKnowledge(I_KnowledgeCache knowledge){
        this.knowledge = knowledge;
        return this;
    }
}
