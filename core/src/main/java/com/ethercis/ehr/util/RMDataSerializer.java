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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.openehr.rm.common.archetyped.Locatable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Marshal/unmarshal an extended RM composition
 * Used to fast serialize the composition and pass it in messages
 * Created by Christian Chevalley on 8/5/2014.
 */
public class RMDataSerializer implements Serializable {

    public static FSTConfiguration configuration =  FSTConfiguration.createDefaultConfiguration();

    private static final long serialVersionUID = -2639020605055348125L;
    Locatable locatable;
    static Logger log = LogManager.getLogger("RMDataSerializer");

    static SerializedCache serializedCache = new SerializedCache();


//    static Map<String, FSTObjectOutput> serializedCache = new HashMap<>();

    public RMDataSerializer(Locatable handler){
        this.locatable = handler;
    }

    public RMDataSerializer(){
    }

    public RMDataSerializer setHandler(Locatable handler){
        this.locatable = handler;
        return this;
    }

    public String write2tempfile() throws IOException {
        File f = File.createTempFile("rmdata_", null, new File("C:\\TMP"));
        String fpath = f.getAbsolutePath();
        log.info("creating temp file:"+fpath);
        FileOutputStream fout = new FileOutputStream(f);
//        ObjectOutputStream oout = new ObjectOutputStream(fout);
        FSTObjectOutput oout = new FSTObjectOutput(fout);
        oout.writeObject(locatable);
        oout.close();
        log.info("Locatable written to file:"+fpath);
        return fpath;
    }

    public String write2file(String filename) throws IOException {
        File f = new File(filename);

        if (f.exists()){
            f.delete();
        }

        String fpath = f.getAbsolutePath();
        log.info("creating file:"+fpath);
        FileOutputStream fout = new FileOutputStream(f);
//        ObjectOutputStream oout = new ObjectOutputStream(fout);
        FSTObjectOutput oout = new FSTObjectOutput(fout);
        oout.writeObject(locatable);
        oout.close();
        log.info("Locatable written to file:"+fpath);
        return fpath;
    }

    public static FSTObjectOutput serialize(Locatable loc) throws IOException {

        return  configuration.getObjectOutput(configuration.asByteArray(loc));
    }

    public byte[] serialize(String path, Locatable loc) throws IOException {
        FSTConfiguration configuration =  FSTConfiguration.createDefaultConfiguration();
        String actualPath = LocatableHelper.simplifyPath(path);
        if (serializedCache.containsKey(actualPath))
            return serializedCache.getObject(actualPath);

        byte[] serialized = configuration.asByteArray(loc);

        serializedCache.put(actualPath, serialized, loc.getClass());

        return  serialized;
    }

    public static byte[] serializeRaw(Object object) throws IOException {
        return configuration.asByteArray(object);
    }

    public static Object unserializeRaw(byte[] bytes){
        return configuration.asObject(bytes);
    }

    public static Locatable unserialize(Object outputStream) throws IllegalArgumentException, IOException {
        Object object = configuration.asObject((byte[])outputStream);

        if (object instanceof Locatable)
            return (Locatable)object;

        throw new IllegalArgumentException("supplied byte array is not a serialized Locatable");
    }

    public static Locatable unserialize(byte[] outputStream) throws IllegalArgumentException, IOException {

        if (!(outputStream instanceof byte[]))
            throw new IllegalArgumentException("Serialized object is not in a compatible format");

        return unserialize(outputStream);

    }


    public static RMDataSerializer getInstance(String fpath) throws Exception {
        FileInputStream fin = new FileInputStream(fpath);
//        ObjectInputStream ois = new ObjectInputStream(fin);
        FSTObjectInput ois = new FSTObjectInput(fin);
        Object obj = ois.readObject();
        ois.close();

        if (obj instanceof Locatable){
            return new RMDataSerializer((Locatable)obj);
        }

        log.error("Object deserialized from file is not a Locatable!");
        return null;
    }

    public Locatable getLocatable() {
        return locatable;
    }
}
