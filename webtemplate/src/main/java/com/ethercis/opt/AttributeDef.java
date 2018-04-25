package com.ethercis.opt;

import com.ethercis.ehr.encode.wrappers.SnakeCase;
import com.ethercis.opt.mapper.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by christian on 4/25/2018.
 */
public class AttributeDef {

    String identifier;

    public AttributeDef(String identifier) {
        this.identifier = identifier;
    }

    public Map<String, String> naming(){

        Map<String, String> synonyms = new HashMap<>();

        synonyms.put(Constants.ATTRIBUTE, attributeIdentifier());
        synonyms.put(Constants.NAME, attributeName());
        synonyms.put(Constants.ID, attributeEhrScapeID());

        return synonyms;
    }

    public String attributeIdentifier(){
        return new SnakeCase(identifier).camelToSnake();
    }

    public String attributeName(){
        return StringUtils.capitalize(identifier);
    }

    public String attributeEhrScapeID(){
        return new NodeId(attributeIdentifier()).ehrscape();
    }

}
