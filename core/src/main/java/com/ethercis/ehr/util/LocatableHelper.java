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
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datatypes.text.DvText;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * ETHERCIS Project ehrservice
 * Created by Christian Chevalley on 8/18/2015.
 */
public class LocatableHelper {


    /**
     * convenience method to clone a Locatable
     * @param aLocatable
     * @return
     * @throws java.io.IOException
     */
    public static Locatable clone(Locatable aLocatable) throws IOException {
        return RMDataSerializer.unserialize(RMDataSerializer.serialize(aLocatable));
    }

    public static String incrementPathNodeId(String fromPathId){
        Integer id = Integer.parseInt(fromPathId.substring(2)); //skip the "at" bit
        String newNodeId = "at"+String.format("%04d", ++id);
        return newNodeId;
    }

    public static void insertHistoryEvent(History history, PointEvent event){
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
                    if (nodeId.contains("#")){
                        ;
                    }
                }
            }

        }
    }

    private static String extractLastAtPath(String itemPath){
        if (itemPath.contains("[at"))
            return itemPath.substring(itemPath.lastIndexOf("[")+1, itemPath.lastIndexOf("]"));
        else
            return "at0000";

    }

    public  static void insertCloneInList(Locatable parent, Locatable clone, String insertionPath, String itemPath){
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
                    if (!clone.getName().getValue().contains("#"))
                        lastNodeId = LocatableHelper.incrementPathNodeId(lastNodeId);
                    else{
                        lastNodeId = extractLastAtPath(itemPath);
                    }
                }
                else
                    lastNodeId = lastNodeId.substring(0, lastNodeId.indexOf(" and name/value"));

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

    private static String identifyAttribute(String path) {
        //get the last segment of this path
        List<String> segments = Locatable.dividePathIntoSegments(path);

        String pathSegment = segments.get(segments.size() - 1);

        int index = pathSegment.indexOf("[");
        String expression = null;
        String attributeName = null;

        // has [....] predicate expression
        if(index > 0) {

            assert(pathSegment.indexOf("]") > index);

            attributeName = pathSegment.substring(0, index);
            expression = pathSegment.substring(index + 1,pathSegment.indexOf("]"));
        } else {
            attributeName = pathSegment;
        }

        return attributeName;
    }

    private static String indentifyName(String path){
        List<String> segments = Locatable.dividePathIntoSegments(path);
        String pathSegment = segments.get(segments.size() - 1);

        int index = pathSegment.indexOf("[");
        String expression = pathSegment.substring(index + 1,pathSegment.indexOf("]"));

        String archetypeNodeId;
        String name = null;

        if(expression.contains(" AND ")
            || expression.contains(" and ")) {

        // OG - 20100401: Fixed bug where the name contained 'AND' or 'and',
        // i.e. 'MEDICINSK BEHANDLING'.
        if(expression.contains(" AND ")) {
            index = expression.indexOf(" AND ");
        } else {
            index = expression.indexOf(" and ");
        }
            archetypeNodeId = expression.substring(0, index).trim();
            name = expression.substring(expression.indexOf("'") + 1,expression.lastIndexOf("'"));
        // just name, ['standing']
        } else if (expression.startsWith("'") && expression.endsWith("'")) {
            name = expression.substring(1, expression.length() - 1);
        }
        return name;
    }

    public static String siblingPath(String unresolvedPath){
        //if the last path is qualified with a name/value, check if a similar item exists

        List<String> segments = Locatable.dividePathIntoSegments(unresolvedPath);
        String last = segments.get(segments.size() - 1);
        if (last.contains(" and name/value=")){
            last = last.substring(0, last.indexOf(" and name/value="))+"]";
        }

        StringBuffer tentativePath = new StringBuffer();
        for (int i = 0; i < segments.size() - 1; i++ ){
            tentativePath.append(segments.get(i)+"/");
        }
        tentativePath.append(last);

        return tentativePath.toString();
    }

    public static Locatable siblingAtPath(Locatable locatable, String unresolvedPath) {
        //if the last path is qualified with a name/value, check if a similar item exists

        List<String> segments = Locatable.dividePathIntoSegments(unresolvedPath);
        String last = segments.get(segments.size() - 1);
        if (last.contains(" and name/value=")){
            last = last.substring(0, last.indexOf(" and name/value="))+"]";
        }

        StringBuffer tentativePath = new StringBuffer();
        for (int i = 0; i < segments.size() - 1; i++ ){
            tentativePath.append(segments.get(i)+"/");
        }
        tentativePath.append(last);

        Object sibling = locatable.itemAtPath(tentativePath.toString());

        if (sibling != null )
            return (Locatable)sibling;

        return null;

    }

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

        return new NodeItem((Locatable)parentAtPath, lastPath, "/"+identifyAttribute(lastPath));
   }

   public static Locatable cloneChildAtPath(Locatable node, String childPath)  throws Exception {
       if (childPath != null) { //identify a clonable child for this parent
           String attribute = identifyAttribute(childPath);

           if (attribute != null){
               //get the corresponding method
               Class clazz = node.getClass();
               String getterName = "get"+node.toFirstUpperCaseCamelCase(attribute);

               Method getter = clazz.getMethod(getterName, null);
               //get the attribute
               Object item = getter.invoke(node, null);

               if (item instanceof List){
                   List<Locatable> itemList = (List)item;
                   if (itemList.size() > 0) {
                       Object toClone = itemList.get(0);
                       Locatable cloned = clone((Locatable) toClone);
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
                   throw new IllegalArgumentException("Ttem cannot be replicated since it is not an ItemList:"+item);
           }

       }

       return null;

   }

}
