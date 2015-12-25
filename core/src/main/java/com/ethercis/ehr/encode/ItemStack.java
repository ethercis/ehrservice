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

import org.apache.log4j.Logger;

import java.util.Stack;

/**
* ETHERCIS Project ehrservice
* Created by Christian Chevalley on 8/3/2015.
*/
public class ItemStack {
    private Logger log = Logger.getLogger(ItemStack.class);
    //contains the ADL path to an element
    private Stack<String> pathStack = new Stack<String>();
    //contains the named path to an element (used to bind Flat JSON)
    private Stack<String> namedStack = new Stack<String>();

    private int floorPathStack = 0;
    private int floorNamedStack = 0;


    public void pushStacks(String path, String name){
        //specify name/value for path in the format /something[openEHR-EHR-blablah...] for disambiguation
        log.debug("-- PUSH PATH:" + path +"::"+name);
        if (path.contains("[openEHR-") || path.contains(CompositionSerializer.TAG_ACTIVITIES) || path.contains(CompositionSerializer.TAG_ITEMS)){
            //add name in path
//            if (!name.contains("'"))
            if (name != null)
                path = path.substring(0, path.indexOf("]"))+" and name/value='"+name+"']";
//            else
//                log.warn("Ignoring entry/item name:"+name);
        }
        pushStack(pathStack, path);
        pushStack(namedStack, name);
//        pushNamedStack(name);
    }

    public void popStacks(){
        log.debug("-- POP PATH:"+ (pathStack.isEmpty() ? "*empty*":pathStack.lastElement()));
        popStack(pathStack);
        popStack(namedStack);
    }

    private void pushStack(Stack stack, String s){
//        log.debug("-- PUSH PATH:" + s);
        stack.push(s);
        floorPathStack++;
//		log.debug("FLOOR:"+floorPathStack+"->"+s);
    }

    private void popStack(Stack stack){
        if (!stack.empty()){
            stack.pop();
            floorPathStack--;
//			log.debug("FLOOR:"+floorPathStack);
        }
    }

//    private void pushNamedStack(String s){
//        log.debug("-- PUSH NAMED:" + s);
//        namedStack.push(s);
//        floorNamedStack++;
////		log.debug("FLOOR:"+floorNamedStack+"->"+s);
//    }
//
//    private void popNamedStack(){
//        log.debug("-- POP  NAMED:"+ (namedStack.isEmpty() ? "*empty*":namedStack.lastElement()));
//        if (!namedStack.empty()){
//            namedStack.pop();
//            floorNamedStack--;
////			log.debug("FLOOR:"+floorPathStack);
//        }
//    }

    public String stackDump(Stack stack){
        StringBuffer b = new StringBuffer();
        for (Object s: stack.toArray()) b.append((String)s);
        return b.toString();
    }

    public String namedStackDump(){
        StringBuffer b = new StringBuffer();
        for (Object s: namedStack.toArray()) b.append(s+"/");
        return b.toString();
    }

    public String expandedStackDump(){
        StringBuffer b = new StringBuffer();
        int i = 0;
        for (Object s: namedStack.toArray()) {
            b.append(s+"{{"+pathStack.get(i++)+"}}/");
        }
        return b.toString();

    }

    public String pathStackDump(){
        return stackDump(pathStack);
    }
}
