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
package com.ethercis.ehr.encode.wrappers;

import java.util.Map;
/**
 * simple interface to a Value wrapper for a RM type
 * @author Christian Chevalley
 *
 */
public interface I_VBeanWrapper<T> {
    public Map<String, Object> getFieldMap() throws Exception;

    public Object getAdaptee();

    public void setAdaptee(Object adaptee);

    public T parse(String value, String... defaults) throws Exception;

}
