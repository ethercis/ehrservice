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

package com.ethercis.aql.sql.binding;

/**
 * String container with a qualifier
 * <p>
 *     the qualifier is used to identify a query part strategy (SQL or JSON)
 * </p>
 * Created by christian on 5/20/2016.
 */
public interface I_TaggedStringBuffer {

    StringBuffer append(String string);

    void replaceLast(String previous, String newString);

    int lastIndexOf(String string);

    int indexOf(String string);

    void replace(String previous, String newString);

    String toString();

    int length();

    WhereBinder.TagField getTagField();

    void setTagField(WhereBinder.TagField tagField);

    boolean startWith(String tag);
}
