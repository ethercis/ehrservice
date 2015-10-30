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

import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datastructure.history.History;
import org.openehr.rm.datastructure.history.PointEvent;

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
        insertChildInList(history, event, "/events");
    }

    public  static void insertChildInList(Locatable parent, Locatable child, String insertionPath){
        //get the list of sibling at insertionPath
        Object objectList = parent.itemAtPath(insertionPath);

        List<Locatable> siblings;

        if (objectList == null)
            siblings = null;
        else if (objectList instanceof List){
            siblings = (List)objectList;
        } else
            siblings = null;

        String lastNodeId = "at0000";

        if (siblings == null)
            lastNodeId = "at0000";
        else {
            for (Object sibling: siblings){

                if (sibling instanceof Locatable){
                    String nodeId = ((Locatable)sibling).getArchetypeNodeId();
                    Integer last = Integer.parseInt(lastNodeId.substring(2));
                    Integer current = Integer.parseInt(nodeId.substring(2));

                    if (current > last)
                        lastNodeId = "at"+ String.format("%04d", current);
                }

            }
            lastNodeId = LocatableHelper.incrementPathNodeId(lastNodeId);
        }

        child.set("/archetypeNodeId", lastNodeId);

        parent.addChild(insertionPath, child);
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
                   Object toClone = ((List)item).get(0);
                   Locatable cloned = clone((Locatable) toClone);
                   return cloned;
               }
               else
                   throw new IllegalArgumentException("Ttem cannot be replicated since it is not an ItemList:"+item);
           }

       }

       return null;

   }

}
