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

package com.ethercis.ehr.encode.rawjson;

/**
 * Created by christian on 6/21/2017.
 */
public class NameValueEncoding {

    String jsonString;
    final String nameTag = "\"name\":[";

    public NameValueEncoding(String jsonString) {
        this.jsonString = jsonString;
    }

    public String make(){
        StringBuffer result = new StringBuffer();

        String[] token = jsonString.split("(?=(\"name\":\\[))|(?<=\\])");

        for (String clip: token){
            if (clip.startsWith(nameTag)){
                result.append(clip.replaceAll("(\\[)|(\\])", ""));
            }
            else
                result.append(clip);
        }

        return result.toString();
    }
}
