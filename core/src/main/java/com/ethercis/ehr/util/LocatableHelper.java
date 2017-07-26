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
import com.ethercis.ehr.encode.VBeanUtil;
import com.ethercis.ehr.encode.wrappers.I_VBeanWrapper;
import com.ethercis.ehr.encode.wrappers.element.ElementWrapper;
import org.openehr.build.RMObjectBuilder;
import org.openehr.build.SystemValue;
import org.openehr.rm.Attribute;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.text.DvText;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/18/2015.
 */
public class LocatableHelper {

    public static final String AND_NAME_VALUE_TOKEN = "and name/value=";
    public static final String COMMA_TOKEN = ",";
    public static final String INDEX_PREFIX_TOKEN = "#";
    public static final String OPEN_BRACKET = "[";
    public static final String CLOSE_BRACKET = "]";
    public static final String FORWARD_SLASH = "/";

    private RMDataSerializer rmDataSerializer = new RMDataSerializer();

    private Map<String, Integer> arrayItemPathMap = new HashMap<>(); //contains the list of array insertion paths for a template

    public LocatableHelper() {
    }

    /**
     * convenience method to clone a Locatable
     * @param aLocatable
     * @return
     * @throws java.io.IOException
     */
    public Locatable clone(Locatable aLocatable) throws IOException {
        return RMDataSerializer.unserialize(RMDataSerializer.serialize(aLocatable));
    }

    public Locatable clone(String path, Locatable aLocatable) throws IOException {
        Object serialized = rmDataSerializer.serialize(path, aLocatable);
        Object unserialized = RMDataSerializer.unserialize(serialized);

        if (!aLocatable.getClass().equals(unserialized.getClass()))
            throw new IllegalArgumentException("INTERNAL: Class to clone does not match cached: expected:"+aLocatable.getClass()+", found:"+unserialized.getClass());

        return (Locatable)unserialized;
    }


    public static String incrementPathNodeId(String fromPathId){
        Integer id = Integer.parseInt(fromPathId.substring(2)); //skip the "at" bit
        String newNodeId = "at"+String.format("%04d", ++id);
        return newNodeId;
    }

    public  void insertHistoryEvent(History history, PointEvent event){
        insertCloneInList(history, event, "/events", null);
    }

    private static void findLastNodeIdInSibblings(String lastNodeId, List<Locatable> siblings){

        for (Object sibling: siblings){

            if (sibling instanceof Locatable){
                String nodeId = ((Locatable)sibling).getArchetypeNodeId();
                if (!nodeId.contains("openEHR")) {
                    Integer last = Integer.parseInt(lastNodeId.substring(2));
                    Integer current = Integer.parseInt(nodeId.substring(2));

                    if (current > last)
                        lastNodeId = "at" + String.format("%04d", current);
                }
                else {
                    //check if it contains a '#'
                    if (nodeId.contains(INDEX_PREFIX_TOKEN)){
                        ;
                    }
                }
            }

        }
    }

    private static String extractLastAtPath(String itemPath){
        if (itemPath.contains("[at")) {
            String path = LocatableHelper.simplifyPath(itemPath);
            return path.substring(path.lastIndexOf(OPEN_BRACKET) + 1, path.lastIndexOf(CLOSE_BRACKET));
        }
        else
            return "at0000";

    }

    /**
     * insert a cloned locatable in an item list
     * @param parent
     * @param clone
     * @param insertionPath
     * @param itemPath
     */
    public  void insertCloneInList(Locatable parent, Locatable clone, String insertionPath, String itemPath){
        //get the list of sibling at insertionPath
        Object objectList = parent.itemAtPath(insertionPath);

        if (clone.getArchetypeNodeId().contains("openEHR")){ //no need to go further...
            parent.addChild(insertionPath, clone);
            return;
        }

        List<Locatable> siblings;

        if (objectList == null)
            siblings = null;
        else if (objectList instanceof List){
            siblings = (List)objectList;
        } else
            siblings = null;

        String lastNodeId = itemPath == null ? "at0000" : extractLastAtPath(itemPath);

        if (siblings != null) {
            //do no increment the at00xy expression for /items or /activities since they are differentiated by names!
            if (!insertionPath.contains(CompositionSerializer.TAG_ACTIVITIES) && !insertionPath.contains(CompositionSerializer.TAG_ITEMS) && !insertionPath.contains(CompositionSerializer.TAG_EVENTS)) {
                findLastNodeIdInSibblings(lastNodeId, siblings);
                lastNodeId = LocatableHelper.incrementPathNodeId(lastNodeId);
            }
            else {
                if (!lastNodeId.contains("name")){
                    if (!clone.getName().getValue().contains(INDEX_PREFIX_TOKEN)) {
//                        lastNodeId = LocatableHelper.incrementPathNodeId(lastNodeId);
                    }
                    else{
                        lastNodeId = extractLastAtPath(itemPath);
                    }
                }
                else {
                    if (lastNodeId.contains(AND_NAME_VALUE_TOKEN))
                        lastNodeId = lastNodeId.substring(0, lastNodeId.indexOf(AND_NAME_VALUE_TOKEN));
                    else if (lastNodeId.contains(COMMA_TOKEN))
                        lastNodeId = lastNodeId.substring(0, lastNodeId.indexOf(COMMA_TOKEN));

                }

            }
        }

        clone.set("/archetypeNodeId", lastNodeId);

        parent.addChild(insertionPath, clone);
    }

    static public class NodeItem {
        private Locatable node;
//        private Locatable child;
        private String childPath;
        private String insertionPath;

        public NodeItem(Locatable node, String childPath, String insertionPath){
            this.node = node;
            this.childPath = childPath;
            this.insertionPath = insertionPath;
        }

        public Locatable getNode() {
            return node;
        }

        public String getChildPath() {
            return childPath;
        }

        public String getInsertionPath(){
            return insertionPath;
        }
    }

    /**
     * identify the attribute in the last path segment
     * @param path
     * @return
     */
    private static String identifyAttribute(String path) {
        //get the last segment of this path
        List<String> segments = Locatable.dividePathIntoSegments(path);

        String pathSegment = segments.get(segments.size() - 1);

        int index = pathSegment.indexOf(OPEN_BRACKET);
        String expression = null;
        String attributeName = null;

        // has [....] predicate expression
        if(index > 0) {

            assert(pathSegment.indexOf(CLOSE_BRACKET) > index);

            attributeName = pathSegment.substring(0, index);
            expression = pathSegment.substring(index + 1,pathSegment.indexOf(CLOSE_BRACKET));
        } else {
            attributeName = pathSegment;
        }

        return attributeName;
    }

    /**
     * identify the name in the last path segment
     * @param path
     * @return
     */
    public static String indentifyName(String path){
        List<String> segments = Locatable.dividePathIntoSegments(path);
        String pathSegment = segments.get(segments.size() - 1);

        int index = pathSegment.indexOf(OPEN_BRACKET);

        if (index < 0)
            return null; //path such as /ism_transition f.ex.

        String expression = pathSegment.substring(index + 1,pathSegment.indexOf(CLOSE_BRACKET));

        String archetypeNodeId;
        String name = null;

        if(expression.contains(" AND ") || expression.contains(" and ") || expression.contains(COMMA_TOKEN)) {

        // OG - 20100401: Fixed bug where the name contained 'AND' or 'and',
        // i.e. 'MEDICINSK BEHANDLING'.
        if(expression.contains(" AND ")) {
            index = expression.indexOf(" AND ");
        } else if (expression.contains("and")){
            index = expression.indexOf(" and ");
        } else
            index = expression.indexOf(COMMA_TOKEN);


            archetypeNodeId = expression.substring(0, index).trim();
            name = expression.substring(expression.indexOf("'") + 1,expression.lastIndexOf("'"));
        // just name, ['standing']
        } else if (expression.startsWith("'") && expression.endsWith("'")) {
            name = expression.substring(1, expression.length() - 1);
        }
        return name;
    }


    /**
     * set the names of children from a path to a end path.
     * Often, the name of nodes is given in a name expression in the locatable path itself as<br>
     *     <code>[at00xy and name/value='a name']</code>
     *     <br>
     * This method traverse the locatable and sets the name of each child based on the path name
     * expression
     * @param node the node containing the children
     * @param fromPath the path of the current node (it can be a subset of a Locatable tree)
     * @param toPath the full path where the children names are defined
     */
    public static void adjustChildrenNames(Locatable node, String fromPath, String toPath){
        if (toPath.length() > fromPath.length()){
            String diffPath = toPath.substring(fromPath.length());
            //get the corresponding name if any
            String childPath = "";
            for (String pathSegment: Locatable.dividePathIntoSegments(diffPath)) {
                childPath += FORWARD_SLASH +pathSegment;
                String name = LocatableHelper.indentifyName(pathSegment);
                if (name != null) {
                    String strippedPath = "";
                    //strip the name part from the path
                    if (childPath.contains(AND_NAME_VALUE_TOKEN))
                        strippedPath = childPath.substring(0, childPath.lastIndexOf(AND_NAME_VALUE_TOKEN)) + CLOSE_BRACKET;
                    else if (childPath.contains(COMMA_TOKEN))
                        strippedPath = childPath.substring(0, childPath.lastIndexOf(COMMA_TOKEN)) + CLOSE_BRACKET;

                    //update the name accordingly
                    Locatable childSegment = (Locatable)node.itemAtPath(strippedPath);
                    if (childSegment != null) {
                        childSegment.setName(new DvText(name));
                    }
                    else
                        node.setName(new DvText(name));

                }
            }
            //new path for this adjusted item
            String newPath = String.join("/", Locatable.dividePathIntoSegments(diffPath));
        }
    }

    /**
     * identify the path of sibling in a item list or array corresponding to an unresolved path.
     * @param unresolvedPath
     * @return
     */
    public static String siblingPath(String unresolvedPath){
        //if the last path is qualified with a name/value, check if a similar item exists

        List<String> segments = Locatable.dividePathIntoSegments(unresolvedPath);
        String last = segments.get(segments.size() - 1);
        if (last.contains(AND_NAME_VALUE_TOKEN) || last.contains(COMMA_TOKEN)){
            last = last.contains(AND_NAME_VALUE_TOKEN) ? last.substring(0, last.indexOf(AND_NAME_VALUE_TOKEN))+ CLOSE_BRACKET : last.substring(0, last.indexOf(COMMA_TOKEN))+ CLOSE_BRACKET;
        }

        StringBuffer tentativePath = new StringBuffer();
        for (int i = 0; i < segments.size() - 1; i++ ){
            tentativePath.append(segments.get(i)+ FORWARD_SLASH);
        }
        tentativePath.append(last);

        return tentativePath.toString();
    }

    /**
     * return a sibling locatable for an unresolved path
     * @param locatable
     * @param unresolvedPath
     * @return
     */
    public static Locatable siblingAtPath(Locatable locatable, String unresolvedPath) {
        //if the last path is qualified with a name/value, check if a similar item exists

        List<String> segments = Locatable.dividePathIntoSegments(unresolvedPath);
        String last = segments.get(segments.size() - 1);
        if (last.contains(AND_NAME_VALUE_TOKEN)){
            last = last.substring(0, last.indexOf(AND_NAME_VALUE_TOKEN))+ CLOSE_BRACKET;
        }

        StringBuffer tentativePath = new StringBuffer();
        for (int i = 0; i < segments.size() - 1; i++ ){
            tentativePath.append(segments.get(i)+ FORWARD_SLASH);
        }
        tentativePath.append(last);

        Object sibling = locatable.itemAtPath(tentativePath.toString());

        if (sibling != null )
            return (Locatable)sibling;

        return null;

    }

    /**
     * return the first parent matching an unresolved path by identify the first item which
     * path matches fully a partial path expression
     * @param locatable
     * @param unresolvedPath
     * @return
     */
    public static NodeItem backtrackItemAtPath(Locatable locatable, String unresolvedPath) {


        //find first parent existing for this unresolved path
        String parentPath = Locatable.parentPath(unresolvedPath);
        Object parentAtPath = locatable.itemAtPath(parentPath);
        Object childAtPath = null;
        String lastPath = unresolvedPath;

        while (parentAtPath == null && parentPath != null && parentPath.length() > 0){
            lastPath = parentPath;
            parentPath = Locatable.parentPath(parentPath);
            if (parentPath == null || parentPath.length() <= 0)
                break;
            childAtPath = parentAtPath;
            parentAtPath = locatable.itemAtPath(parentPath);
        }

        return new NodeItem((Locatable)parentAtPath, lastPath, FORWARD_SLASH +identifyAttribute(lastPath));
   }

    public static Locatable getLocatableParent(Locatable locatable, String path) {
        List<String> segments = Locatable.dividePathIntoSegments(path);

        for (int i = segments.size() - 1; i >= 0; i --){
            String parentPath = FORWARD_SLASH +String.join(FORWARD_SLASH, segments.subList(0, i));
            if (locatable.itemAtPath(parentPath) instanceof Locatable)
                return (Locatable)locatable.itemAtPath(parentPath);
        }
        return null;
    }

    public static String getLocatableParentPath(Locatable locatable, String path) {
        List<String> segments = Locatable.dividePathIntoSegments(path);

        for (int i = segments.size() - 1; i >= 0; i --){
            String parentPath = FORWARD_SLASH +String.join(FORWARD_SLASH, segments.subList(0, i));
            if (locatable.itemAtPath(parentPath) instanceof Locatable)
                return parentPath;
        }
        return null;
    }

    private static Object matchingItemInList(List<Locatable> itemList, String path){
        if (path.contains(COMMA_TOKEN) || path.contains(AND_NAME_VALUE_TOKEN)){
            List<String> segments = Locatable.dividePathIntoSegments(path);
            String lastNodeid = segments.get(segments.size() - 1);

            if (lastNodeid.contains(AND_NAME_VALUE_TOKEN) || lastNodeid.contains(COMMA_TOKEN)){
                if (lastNodeid.contains(INDEX_PREFIX_TOKEN)) {
                    String nameValueToken = trimIndexValue(extractNameValueToken(lastNodeid)).trim();
                    for (Locatable locatable: itemList){
                        if (locatable.getName().getValue().equals(nameValueToken))
                            return locatable;
                    }
                }
            }
        }
        return itemList.get(0); //default
    }

    /**
     * clone the item at a given path
     * @param node
     * @param childPath
     * @return
     * @throws Exception
     */
   public Locatable cloneChildAtPath(Locatable node, String childPath)  throws Exception {
       if (childPath != null) { //identify a clonable child for this parent
           String attribute = identifyAttribute(childPath);

           if (attribute != null){
               //get the corresponding method
               Class clazz = node.getClass();
               String getterName = "get"+node.toFirstUpperCaseCamelCase(attribute);

               Method getter;
               try {
                   getter = clazz.getMethod(getterName, null);
               } catch (NoSuchMethodException nsme){
                   throw new IllegalArgumentException("Could not set attribute:'"+attribute+"', path:"+childPath+", possible out of sync template");
               }
               //get the attribute
               Object item = getter.invoke(node, null);

               if (item instanceof List){
                   List<Locatable> itemList = (List)item;
                   if (itemList.size() > 0) {
//                       Object toClone = itemList.get(0);
                       Object toClone = matchingItemInList(itemList, childPath);
//                       Locatable cloned = clone((Locatable) toClone);
                       Locatable cloned = clone(childPath, (Locatable) toClone);
                       String name = indentifyName(childPath);
                       if (name != null)
                           cloned.setName(new DvText(name));
                       return cloned;
                   }
                   else { //just create a simple ElementWrapper as the insertion point
                       Element element = new Element("at0000", "place_holder", new DvText("place_holder"));
                       I_VBeanWrapper wrapped = (I_VBeanWrapper) VBeanUtil.wrapObject(new DvText("place_holder"));
                       ElementWrapper elementWrapper = new ElementWrapper(element, null);
                       elementWrapper.setWrappedValue(wrapped);
                       return elementWrapper;
                   }
               }
               else
                   throw new IllegalArgumentException("Item cannot be replicated since it is not an ItemList:"+item);
           }

       }

       return null;

   }

    /**
     * return a path without name/value in nodeId predicate
     * @param path
     * @return
     */
    public static String simplifyPath(String path){
        //if the last path is qualified with a name/value, check if a similar item exists
        StringBuffer tentativePath = new StringBuffer();
        tentativePath.append(FORWARD_SLASH);

        List<String> segments = Locatable.dividePathIntoSegments(path);
        for (int i = 0; i < segments.size(); i++){
            String segment = segments.get(i);
            if (segment.contains(AND_NAME_VALUE_TOKEN) || segment.contains(COMMA_TOKEN)){
                String trimmedNodeId = segment.contains(AND_NAME_VALUE_TOKEN) ? segment.split(AND_NAME_VALUE_TOKEN)[0] : segment.split(COMMA_TOKEN)[0];
                tentativePath.append(trimmedNodeId.trim());
                tentativePath.append(CLOSE_BRACKET);
            }
            else
                tentativePath.append(segment);
            if (i < segments.size() - 1)
                tentativePath.append(FORWARD_SLASH);
        }

        return tentativePath.toString();
    }

    /**
     * check if path contains expression like 'and name/value='iteration #1''
     * @param path
     * @return
     */
    public static boolean hasDefinedOccurence(String path){
        //if the last path is qualified with a name/value, check if a similar item exists
        List<String> segments = Locatable.dividePathIntoSegments(path);
        for (int i = 0; i < segments.size(); i++){
            String segment = segments.get(i);
            if (segment.contains(AND_NAME_VALUE_TOKEN)||segment.contains(COMMA_TOKEN)){

                String namePart = segment.contains(AND_NAME_VALUE_TOKEN) ? segment.split(AND_NAME_VALUE_TOKEN)[1] : segment.split(COMMA_TOKEN)[1];
                if (namePart.contains(INDEX_PREFIX_TOKEN))
                    return true;
            }
        }

        return false;
    }

    public static Object itemAtPath(Locatable locatable, String path){
        return locatable.itemAtPath(path.replaceAll(AND_NAME_VALUE_TOKEN, COMMA_TOKEN));
    }

    /**
     * retrieve the value of an array index in a nodeId predicate
     * @param nodeId
     * @return
     */
    public static Integer retrieveIndexValue(String nodeId) {
        if (nodeId.contains(INDEX_PREFIX_TOKEN)){
            Integer indexValue = Integer.valueOf((nodeId.split(INDEX_PREFIX_TOKEN)[1]).split("']")[0]);
            return indexValue;
        }
        return null;
    }

    public static String trimIndexValue(String nodeid) {
        if (nodeid.contains(INDEX_PREFIX_TOKEN)){
            return nodeid.substring(0, nodeid.indexOf(INDEX_PREFIX_TOKEN));
        }
        return nodeid;
    }

    public static String trimNameValue(String nodeid){
        if (nodeid.contains(AND_NAME_VALUE_TOKEN) || nodeid.contains(COMMA_TOKEN))
            return (nodeid.substring(0, nodeid.indexOf(nodeid.contains(AND_NAME_VALUE_TOKEN) ? AND_NAME_VALUE_TOKEN : COMMA_TOKEN)).trim()+ CLOSE_BRACKET).trim();
        return nodeid.trim();
    }

    public static String extractNameValueToken(String nodeid){
        if (nodeid.contains("'"))
            return nodeid.substring(nodeid.indexOf("'") + 1, nodeid.lastIndexOf("'"));
        return nodeid;
    }

    public Map<String, Integer> getArrayItemPathMap() {
        return arrayItemPathMap;
    }

    public void addItemPath(String itemPath){
        if (!arrayItemPathMap.containsKey(itemPath))
            arrayItemPathMap.put(itemPath, 1);
        else
            arrayItemPathMap.put(itemPath, arrayItemPathMap.get(itemPath) + 1);
    }

    public static Class parameterClass(Object locatable, String parameterName) throws NoSuchFieldException {
        Class parameterClass = null;
        RMObjectBuilder objectBuilder = RMObjectBuilder.getInstance();
        Constructor constructor = objectBuilder.fullConstructor(locatable.getClass());
        Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++){
            Annotation[] annotationDef = annotations[i];
            Attribute attribute = (Attribute) annotationDef[0];
            if (attribute.name().equals(parameterName)){
                parameterClass = constructor.getParameterTypes()[i];
                break;
            }
        }
//        Class clazz = LocatableHelper.itemAtPath(entry, attribute).getClass();
        return parameterClass;
    }
}
