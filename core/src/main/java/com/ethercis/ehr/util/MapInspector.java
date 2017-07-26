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
package com.ethercis.ehr.util;

import com.ethercis.ehr.encode.CompositionSerializer;
import com.ethercis.ehr.encode.wrappers.DvCodedTextVBean;
import com.ethercis.ehr.encode.wrappers.json.writer.DvDateAdapter;
import com.ethercis.ehr.encode.wrappers.json.writer.DvDateTimeAdapter;
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.ParticipationVBean;
import com.ethercis.ehr.keyvalues.I_PathValue;
import com.ethercis.ehr.keyvalues.serializer.I_SerialMap;
import com.ethercis.ehr.keyvalues.serializer.SerialMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.rm.RMObject;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.common.generic.Participation;
import org.openehr.rm.common.generic.PartyIdentified;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.support.identification.PartyRef;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Christian Chevalley on 4/4/2015.
 */
public class MapInspector {

    public static final int ENCODED_ELEMENT_MAP_SIZE = 4;
    private Deque<Map<String, Object>> stack;
    static Logger log = LogManager.getLogger(MapInspector.class);
    public static final String TAG_OBJECT = "$OBJECT$";

    private String[] structuralClasses = {"Element", "Cluster", "ItemSingle", "ItemList", "ItemTable", "ItemTree", "History", "IntervalEvent", "PointEvent"};

    public MapInspector() {
        this.stack = new ArrayDeque<>();
    }

    public void inspect(Map<String, Object> map) throws Exception {
        mapInspect(map);

        //recursion termination
        if (!(stack.isEmpty())) { //build object for the preceding class...
            Map<String, Object> current = stack.getFirst();
            if (!current.containsKey("/meta")) {
                log.debug("Building object for class " + current.get(CompositionSerializer.TAG_CLASS));
                generateObject(current);
            }
        }
    }

    public void inspect(Map<String, Object> map, boolean clear) throws Exception {
        if (clear)
            stack.clear();
        inspect(map);
    }

    public void inspect(Reader jsonReader) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DvDateTime.class, new DvDateTimeAdapter());
        builder.registerTypeAdapter(DvDate.class, new DvDateAdapter());
        Gson gson = builder.setPrettyPrinting().create();

        Map<String, Object> retmap = gson.fromJson(jsonReader, TreeMap.class);

        inspect(retmap);
    }

    public void inspect(Reader jsonReader, boolean clear) throws Exception {
        if (clear)
            stack.clear();
        inspect(jsonReader);
    }

    public Deque<Map<String, Object>> getStack() {
        return stack;
    }

    public String simplifyPathExpression(String path) {
        if (path.contains("{{")) //annotated
            return path;
        List<String> segments = Locatable.dividePathIntoSegments(path);
        List<String> result = new ArrayList<>();
//        StringBuffer stringBuffer = new StringBuffer();
        //first segment is added
        result.add(segments.get(0));
        segments.remove(0);

        for (String segment : segments) {
            if (segment.contains(" and name/value") || segment.contains(",")) {
                String simplified = segment.substring(0, segment.indexOf(segment.contains(" and name/value") ? " and name/value" : ",")) + "]";
                result.add(simplified);
            } else
                result.add(segment);
        }

        return "/" + String.join("/", result);
    }

    public String simplifyPathExpressionKeepArrayIndex(String path) {
        if (path == null)
            return null;
        if (path.contains("{{")) //annotated
            return path;
        List<String> segments = Locatable.dividePathIntoSegments(path);
        List<String> result = new ArrayList<>();
//        StringBuffer stringBuffer = new StringBuffer();
        //first segment is added
//        result.add(segments.get(0));
//        segments.remove(0);

        for (String segment : segments) {
            if (segment.contains(LocatableHelper.AND_NAME_VALUE_TOKEN) || segment.contains(LocatableHelper.COMMA_TOKEN)) {
                if (segment.contains(LocatableHelper.INDEX_PREFIX_TOKEN)) {
//                    Integer arrayValue = LocatableHelper.retrieveIndexValue(segment);
//                    String trimmed = LocatableHelper.trimNameValue(segment);
//                    trimmed = trimmed.substring(0, trimmed.length() -1 )+",'#"+arrayValue+"']";

                    result.add(segment.replace(LocatableHelper.AND_NAME_VALUE_TOKEN, LocatableHelper.COMMA_TOKEN));
                } else {
                    String trimmed = LocatableHelper.trimNameValue(segment);
                    result.add(trimmed);
                }
            } else
                result.add(segment);
        }

        return "/" + String.join("/", result);
    }

    public Map<String, String> getStackFlatten() {
        Map<String, String> retMap = new TreeMap<>();

        for (Object valueDefinition : stack) {
            Map<String, Object> map = (Map) valueDefinition;
            String path = simplifyPathExpressionKeepArrayIndex((String) map.get(CompositionSerializer.TAG_PATH));
            //transform the path expression, remove ' and name/value='

            Object target = map.get(CompositionSerializer.TAG_VALUE);

            I_SerialMap serialMap = new SerialMap(target, map, path);

            Map encoded = serialMap.encode();
            if (encoded != null)
                retMap.putAll(encoded);

//            if (object instanceof Participation) {
//                Participation participation = (Participation) object;
//
//                retMap.put(path + I_PathValue.PARTICIPATION_FUNCTION_SUBTAG, participation.getFunction().getValue());
//                PartyIdentified performer = (PartyIdentified) participation.getPerformer();
//                if (performer!=null) {
//                    retMap.put(path + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, performer.getName());
//                    if (performer.getExternalRef() != null)
//                        retMap.put(path + I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, performer.getExternalRef().getId().getValue());
//                }
//                if (participation.getMode() != null)
//                     retMap.put(path + I_PathValue.PARTICIPATION_MODE_SUBTAG, participation.getMode().toString());
//            }
//            else if (object instanceof PartyIdentified){ //used for care entry provider
//                PartyIdentified partyIdentified = (PartyIdentified)object;
//                retMap.put(path + I_PathValue.IDENTIFIER_PARTY_NAME_SUBTAG, partyIdentified.getName());
//                retMap.put(path+I_PathValue.IDENTIFIER_PARTY_ID_SUBTAG, partyIdentified.getExternalRef().getId().getValue());
//                retMap.put(path + I_PathValue.IDENTIFIER_PARTY_NAMESPACE_SUBTAG, ((PartyRef) partyIdentified.getExternalRef()).getNamespace());
////                retMap.put(path+I_PathValue.IDENTIFIER_PARTY_SCHEME_SUBTAG, ((PartyRef)partyIdentified.getExternalRef()).getgetId().getValue());
//            }
//            else if (object instanceof DvParsable) {
//                retMap.put(path+I_PathValue.VALUE_SUBTAG, ((DvParsable) object).getValue());
//                retMap.put(path+I_PathValue.FORMALISM_SUBTAG, ((DvParsable)object).getFormalism());
//            }
//            else if (object instanceof DvInterval) {
//                DvInterval interval = (DvInterval) object;
//                String lowerValue = (interval.getLower() != null ? interval.getLower().toString() : "[null]");
//                String upperValue = (interval.getUpper() != null ? interval.getUpper().toString() : "[null]");
//                retMap.put(path, lowerValue +"::"+upperValue);
//            }
//            else if (object != null) {
//                boolean composite = false;
//                if (map.containsKey(CompositionSerializer.TAG_NAME)) {
//                    Object nameAttribute = map.get(CompositionSerializer.TAG_NAME);
//                    if (nameAttribute instanceof String)
//                        retMap.put(path + I_PathValue.NAME_SUBTAG, nameAttribute.toString());
//                    else if (nameAttribute instanceof Map)
//                        retMap.put(path + I_PathValue.NAME_SUBTAG, ((Map)nameAttribute).get("value").toString());
//                    composite = true;
//                }
//                if (map.containsKey(CompositionSerializer.TAG_DEFINING_CODE)){
//                    retMap.put(path + I_PathValue.DEFINING_CODE_SUBTAG, ((CodePhrase)map.get(CompositionSerializer.TAG_DEFINING_CODE)).toString());
//                    composite = true;
//                }
//                if (composite){
//                    String classname = (String)map.get(CompositionSerializer.TAG_CLASS);
//                    if (!isStructural(classname)) {
//                        if (map.containsKey(TAG_OBJECT)){
////                            Object = (DataValue) map.get(TAG_OBJECT);
//                            retMap.put(path + I_PathValue.VALUE_SUBTAG, map.get(TAG_OBJECT).toString());
//                        }
//                        else
//                            retMap.put(path + I_PathValue.VALUE_SUBTAG, object.toString());
//                    }
//                    else
//                        retMap.put(path + I_PathValue.VALUE_SUBTAG, object.toString());
//                }
//                else {
//                    retMap.put(path, object.toString());
//                }
//            }
//            else
//                log.error("no mapping for object map:" + map);
        }
        return retMap;
    }

    /**
     * utility to list classnames when inner classes are specified
     *
     * @param className
     * @return
     */

    public static List<String> listInnnerClasses(String className) {
        List<String> classList = new ArrayList<>();
        extractInnerClass(classList, className);
        return classList;
    }

    private static void extractInnerClass(List<String> classList, String className) {

        if (!className.contains("<")) {
            classList.add(className);
            return;
        }

        String outerClass = className.substring(0, className.indexOf("<"));
        classList.add(outerClass);

        String innerClassName = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">"));
        extractInnerClass(classList, innerClassName);
    }

    private void generateObject(Map<String, Object> attributes) throws Exception {

        if (attributes.containsKey(TAG_OBJECT) && attributes.get(TAG_OBJECT) != null) //the object may have been supplied depending on the context
            //make sure the created object is a DataValue, otherwise create a new one...
            if (attributes.get(TAG_OBJECT) instanceof DataValue || attributes.get(TAG_OBJECT) instanceof ObjectID || attributes.get(TAG_OBJECT) instanceof PartyIdentified)
                return;

        String className = (String) attributes.get(CompositionSerializer.TAG_CLASS);

        if (className == null) {
            log.error("NULL className found for " + attributes);
            return;
//            throw new IllegalArgumentException("NULL class name found for attributes:"+attributes);
        }

        if (className.contains("<")) {
            List<String> classList = listInnnerClasses(className);
            className = classList.get(0) + "VBean";
            //by convention we pass the list of inner classes to the getInstance() method of the ValueBean
            attributes.put(CompositionSerializer.INNER_CLASS_LIST, classList.subList(1, classList.size()));
        } else
            className += "VBean";

        Class clazz = Class.forName("com.ethercis.ehr.encode.wrappers." + className);
        Method method = clazz.getDeclaredMethod("getInstance", Map.class);

        Object object;

        try {
            object = method.invoke(null, attributes); //static call of method
        } catch (Exception e) {
            if (clazz.equals(DvCodedTextVBean.class)) { //try a DvText
                clazz = Class.forName("com.ethercis.ehr.encode.wrappers.DvTextVBean");
                method = clazz.getDeclaredMethod("getInstance", Map.class);

                try {
                    object = method.invoke(null, attributes);

                } catch (Exception ex) {
                    throw new IllegalArgumentException("Could not invoke constructor for class DvText attributes:" + attributes + " exception:" + e);
                }
            } else
                throw new IllegalArgumentException("Could not invoke constructor for class:" + clazz + " attributes:" + attributes + " exception:" + e);
        }

        try {
            attributes.put(TAG_OBJECT, object);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not add object:" + object);
        }

    }

    private void addObjectAttributesOnStack(String key, Object value) throws IllegalArgumentException {
        if (stack.isEmpty()) {
            Map<String, Object> objectAttributes = new HashMap<>();
            stack.push(objectAttributes);
//            throw new IllegalArgumentException("Inconsistent entry PATH in JSON structure " + value);
        }
        Map<String, Object> current = stack.getFirst();
        try {
            current.put(key, value);
        } catch (Exception e) {
            log.debug("duplicate key detected:" + key);
        }
    }

    private void mapInspect(Map<String, Object> map) throws Exception {
        for (Map.Entry<String, Object> node : map.entrySet()) {
            String key = node.getKey();
            Object value = node.getValue();

            if (map.size() == ENCODED_ELEMENT_MAP_SIZE && map.containsKey(CompositionSerializer.TAG_CLASS)) { //encoded element
                generateObject(map);
                stack.push(map);
                return;
            }

            if (key.equals(VBeanUtil.TAG_VALUE_AS_STRING)) {
                log.debug("VALUE AS STRING ------->");
                //it is expected to have a DataValue object here...
                if (value instanceof Map) { //build object for the preceding class...
                    Map<String, Object> current = stack.getFirst();
//                    log.debug("Building object for class" + current.get(CompWalker.TAG_CLASS));
                    current.put(TAG_OBJECT, ((Map) value).get("value"));
                }
//                continue;
            }
//            else if (key.equals(CompositionSerializer.TAG_CLASS)) { //new map object, trigger to add a new entry on stack
//                //if there is a value as string don't create an instance
////                if (map.containsKey(VBeanUtil.TAG_VALUE_AS_STRING))
////                    continue;
//
//                if (!(stack.isEmpty())){ //build object for the preceding class...
//                    Map<String, Object> current = stack.getFirst();
//                    log.debug("Building object for class " + current.get(CompositionSerializer.TAG_CLASS));
//                    generateObject(current);
//                }
//
////                Map<String, Object> objectAttributes = new HashMap<>();
////                objectAttributes.put(key, value);
////                stack.push(objectAttributes);
//            }
////            else if (key.equals(CompositionSerializer.TAG_PATH)) {
////                addObjectAttributesOnStack(key, value);
////            }


            if (!new NodeAttribute(key).isMetaData() && value instanceof String) {
                //try to get a path for this key/value pair
                String path = null;

                for (Object sibling : map.values()) {
                    if (sibling instanceof Map) { //grab a path
//                        if (((Map) sibling).containsKey(CompositionSerializer.TAG_PATH)){
//                            path = (String)((Map) sibling).get(CompositionSerializer.TAG_PATH);
//                            //and strip the suffix
//                            path = path.substring(0, path.lastIndexOf("/"));
//                        }
                        path = new PathItem((Map) sibling).findPathValue();
                    }
                }

                if (path != null) {
                    Map<String, Object> values = new HashMap<>();
                    values.put(CompositionSerializer.TAG_PATH, path + key);
                    values.put(CompositionSerializer.TAG_CLASS, String.class.getSimpleName());
                    values.put(CompositionSerializer.TAG_VALUE, value);
                    values.put(CompositionSerializer.TAG_NAME, key.substring(1));
                    stack.push(values);
                } else {
                    addObjectAttributesOnStack(key, value);
                    log.debug(key + "=" + value);
                }
            }
//            else if (key.equals(CompositionSerializer.TAG_VALUE)){ //simplest case, pass the whole /value map
//                Map<String, Object> valueMap = new HashMap<>();
//                valueMap.put(CompositionSerializer.TAG_VALUE, value);
//                generateObject(valueMap);
//                stack.push(valueMap);
//            }
            else if (value instanceof Map) {
                if (key.equals(CompositionSerializer.TAG_VALUE)) { //a map of values...
                    Map<String, Object> valueMap = ((Map) value);
                    Map<String, Object> attributes = new HashMap<>();
                    //add the original
//                    ((Map)value).put(CompositionSerializer.TAG_VALUE, value);
                    if (!valueMap.containsKey(CompositionSerializer.TAG_VALUE) /*&& valueMap.containsKey("value")*/) {
                        //and interpreted.
                        Map<String, Object> valueAttributes = new HashMap<>();
//                        valueAttributes.put("value", valueMap.get("value"));
                        valueAttributes.putAll(valueMap);
                        valueMap.put(CompositionSerializer.TAG_VALUE, valueAttributes);
//                        valueMap.remove("value");
                    }

                    if (!valueMap.containsKey(CompositionSerializer.TAG_CLASS)) {
                        //ensure the object can be rebuilt...
                        valueMap.put(CompositionSerializer.TAG_CLASS, map.get(CompositionSerializer.TAG_CLASS));
                    }
                    if (!((Map) value).containsKey(CompositionSerializer.TAG_PATH)) {
                        //and interpreted.
                        valueMap.put(CompositionSerializer.TAG_PATH, map.get(CompositionSerializer.TAG_PATH));
                    }
                    if (!valueMap.containsKey(CompositionSerializer.TAG_NAME)) {
                        //and interpreted.
                        valueMap.put(CompositionSerializer.TAG_NAME, map.get(CompositionSerializer.TAG_NAME));
                    }

                    generateObject(valueMap);
                    stack.push(valueMap);
//                    return;
//                    Map<String, Object> current = stack.getFirst();
////                    log.debug("Building object for class" + current.get(CompWalker.TAG_CLASS));
//
//                    current.put(TAG_OBJECT, ((Map) value).get(CompWalker.TAG_VALUE));
//                    //remove the /value entry if any
//                    if (current.containsKey(CompWalker.TAG_VALUE))
//                        current.remove(CompWalker.TAG_VALUE);
                } else {
                    if (((Map) value).containsKey(CompositionSerializer.TAG_CLASS) && !isStructural((String) ((Map) value).get(CompositionSerializer.TAG_CLASS))) {
                        generateObject((Map<String, Object>) value);
                        stack.push((Map<String, Object>) value);
//                        return;
                    } else {
                        Map<String, Object> submap = (Map<String, Object>) value;
                        mapInspect(submap);
                    }
                }
            } else if (value instanceof List) {
                if (CompositionSerializer.TAG_OTHER_PARTICIPATIONS.equals(key)) {
                    int index = 0;
                    for (Map participation : (List<Map>) value) {
                        Map<String, Object> participationMap = new HashMap<>();
                        participationMap.putAll(participation);
//                        participationMap.remove(CompositionSerializer.TAG_VALUE);
                        participationMap.put(CompositionSerializer.TAG_VALUE, participation.get(CompositionSerializer.TAG_VALUE));
//                        participationMap.remove(CompositionSerializer.TAG_PATH);
                        participationMap.put(CompositionSerializer.TAG_PATH, participation.get(CompositionSerializer.TAG_PATH) + "/participation:" + index++);

                        Object taggedValue = participationMap.get(CompositionSerializer.TAG_VALUE);
                        if (taggedValue instanceof Participation)
                            participationMap.put(TAG_OBJECT, taggedValue);
                        else if (taggedValue instanceof Map) {
                            Map<String, Object> valueMap = new HashMap<>();
                            valueMap.put(CompositionSerializer.TAG_VALUE, taggedValue);
                            participationMap.put(TAG_OBJECT, ParticipationVBean.getInstance(valueMap));
                        } else
                            throw new IllegalArgumentException("Participation could not be parsed properly...");

                        stack.push(participationMap);
                    }
                } else if (CompositionSerializer.TAG_NAME.equals(key)) {
                    //ignore node names (contained in item path)
//                    Map nameValueMap = (Map)((List<Object>)value).get(0);
//                    stack.push(nameValueMap);
                } else
                    listInspect((List<Object>) value);
            } else if (value instanceof Double) {
                addObjectAttributesOnStack(key, value);
                log.debug(key + "=" + value);
            } else if (value instanceof Boolean) {
                addObjectAttributesOnStack(key, value);
                log.debug(key + "=" + value);
            } else if (value instanceof DataValue) {
                if (stack.isEmpty()) {
                    stack.push(map);
                } else {
                    Map<String, Object> current = stack.getFirst();
                    log.debug("Building object for class" + current.get(CompositionSerializer.TAG_CLASS));
                    try {
                        current.put(TAG_OBJECT, value);
                    } catch (IllegalArgumentException e) { //predicate reject it, object is already in the map...

                    }
                }
            } else {
                if (!new NodeAttribute(key).isMetaData() && !(value instanceof Participation))
                    throw new IllegalArgumentException("Invalid entry detected in map:" + String.valueOf(value));
            }

        }
    }

//    private boolean isElementMetaData(String key){
//        return key.equals(CompositionSerializer.TAG_NAME ) || key.equals(CompositionSerializer.TAG_PATH) || key.equals(CompositionSerializer.TAG_CLASS);
//    }

    private void listInspect(List<Object> list) throws Exception {
        for (Object node : list) {
            if (node instanceof Map) {
                mapInspect((Map<String, Object>) node);
            } else if (node instanceof List) {
                listInspect((List<Object>) node);
            } else
                ; //TODO: quick fix, ignore as this may come from complex types such as Joda Time
            // throw new IllegalArgumentException("Invalid entry detected in list:"+String.valueOf(node));

        }
    }

    public void resetStack() {
        stack.clear();
    }

    private boolean isStructural(String classname) {
        return Arrays.asList(structuralClasses).contains(classname);
    }

}
