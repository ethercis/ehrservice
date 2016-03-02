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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import java.io.IOException;

/**
 * Created by christian on 3/1/2016.
 */
public class JodaPeriodAdapter extends TypeAdapter<Period> {


    @Override
    public void write(JsonWriter jsonWriter, Period period) throws IOException {
        if (period == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.value(period.toString());

    }

    @Override
    public Period read(JsonReader arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
