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

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang.ArrayUtils;
import org.openehr.rm.datatypes.basic.DataValue;
import org.openehr.rm.datatypes.basic.DvBoolean;
import org.openehr.rm.datatypes.basic.DvIdentifier;
import org.openehr.rm.datatypes.basic.DvState;
import org.openehr.rm.datatypes.encapsulated.DvMultimedia;
import org.openehr.rm.datatypes.encapsulated.DvParsable;
import org.openehr.rm.datatypes.quantity.*;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;
import org.openehr.rm.datatypes.quantity.datetime.DvDuration;
import org.openehr.rm.datatypes.quantity.datetime.DvTime;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.datatypes.text.DvParagraph;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.uri.DvEHRURI;
import org.openehr.rm.datatypes.uri.DvURI;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public abstract class DataValueAdapter extends TypeAdapter<DataValue> implements Serializable {

    private static final long serialVersionUID = -7938411006834684803L;
    protected Object adaptee; //the actual encapsulated object

	//classes for which serialize/parse is implemented (specialization of DataValue)
	//NB: DvParsable, DvURI, DvEHRURI, DvMultimedia are not complete in the RM library
	private static Class<?>[] value_classes = {
			DvBoolean.class,       DvState.class,         DvIdentifier.class,    DvText.class,
			DvCodedText.class,     DvParagraph.class,     CodePhrase.class,      DvCount.class,
			DvOrdinal.class,       DvQuantity.class,      DvInterval.class,      DvProportion.class,
			DvDate.class,          DvDateTime.class,      DvTime.class,
			DvDuration.class,      DvParsable.class,      DvURI.class,           DvEHRURI.class,
			DvMultimedia.class			
	};
	
	public DataValueAdapter() {
		
	}
	
	public void setBuilderAdapters(GsonBuilder builder) {
		
		for (Class<?> c: value_classes) {
			builder.registerTypeAdapter(c, this);
		}
		
	}
	
	@Override
	public DataValue read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}

		String serialized = reader.nextString();

		//invoke respective class parser
		DataValue parsedval = DataValue.parseValue(serialized);
		return parsedval;
	}

	@Override
	public void write(JsonWriter writer, DataValue dvalue) throws IOException {
		if (dvalue == null) {
			writer.nullValue();
			return;
		}
		
		writer.value(dvalue.serialise());
		
	}

    public static boolean isValueObject(Object obj){
        if (obj == null)
            return false;

        Class clazz = obj.getClass();

//        for (Class c: value_classes){
//            if (clazz.equals(c))
//                return  true;
//        }
//
//        return false;

        return ArrayUtils.contains(value_classes, clazz);
    }

	public static boolean isValueClass(Class clazz){
		return ArrayUtils.contains(value_classes, clazz);
	}

    static public Object getValue(Map<String, Object> attributes) throws IllegalArgumentException {
        if (attributes.isEmpty() || ((!(attributes.containsKey("value"))) && (!(attributes.containsKey(CompositionSerializer.TAG_VALUE)))))
            throw new IllegalArgumentException("Required attribute 'value' is missing ");

        Object value ;

        value = attributes.get("value");

        if (value == null)
            value = attributes.get(CompositionSerializer.TAG_VALUE);

        return value;
    }

    public Object getAdaptee() {
        return adaptee;
    }

    public void setAdaptee(Object adaptee){ this.adaptee = adaptee;};
}
