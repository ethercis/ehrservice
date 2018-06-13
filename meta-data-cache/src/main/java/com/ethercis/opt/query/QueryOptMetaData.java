package com.ethercis.opt.query;

import com.ethercis.opt.OptVisitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.openehr.schemas.v1.OPERATIONALTEMPLATE;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by christian on 5/7/2018.
 */
public class QueryOptMetaData implements I_QueryOptMetaData {

    Object document;

    private QueryOptMetaData(Object document) {
        this.document = document;
    }

    /**
     * prepare a document for querying
     * @return
     */
    public static QueryOptMetaData initialize(OPERATIONALTEMPLATE operationaltemplate) throws Exception {
        Map map = new OptVisitor().traverse(operationaltemplate);
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(new MapJson(map).toJson());
        return new QueryOptMetaData(document);
    }

    public static I_QueryOptMetaData getInstance(OPERATIONALTEMPLATE operationaltemplate) throws Exception {
        return initialize(operationaltemplate);
    }

    public static I_QueryOptMetaData getInstance(String visitor) throws Exception {
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(visitor);
        return new QueryOptMetaData(document);
    }

    public static I_QueryOptMetaData getInstance(Object visitor) throws Exception {
        return new QueryOptMetaData(visitor);
    }


    /**
     * returns all path for which upper limit is unbounded.
     * @return
     */
    @Override
    public List upperNotBounded(){
        return JsonPath.read(document, "$..children[?(@.max == -1)]");
    }

    /**
     * get the type of the node identified with path
     * @param path
     * @return
     */
    @Override
    public String type(String path){
        Object child =  JsonPath.read(document, "$..children[?(@.aql_path == '"+path+"')]");

        if (child != null && child instanceof JSONArray && ((JSONArray)child).size() > 0){
            Object childDef = ((JSONArray) child).get(0);
            if (childDef != null && childDef instanceof Map) {
                return (String) ((Map) childDef).get("type");
            }
        }

        return null;
    }

    /**
     * return the list of node with name == 'name'
     * @param value
     * @return
     */
    @Override
    public List nodeByFieldValue(String field, String value){
        return JsonPath.read(document, "$..children[?(@."+field+" == '"+value+"')]");
    }


    @Override
    public List nodeFieldRegexp(String field, String regexp){
        return JsonPath.read(document, "$..children[?(@."+field+" =~ "+regexp+")]");
    }

    @Override
    public Object getJsonPathVisitor(){
        return document;
    }

    @Override
    public String getTemplateConcept(){
        return (String) ((Map)document).get("concept");
    }

    @Override
    public String getTemplateId(){
        return (String) ((Map)document).get("template_id");
    }

}
