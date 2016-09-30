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
package com.ethercis.dao.access.jooq;

import com.ethercis.dao.access.interfaces.I_DomainAccess;
import com.ethercis.dao.access.interfaces.I_SystemAccess;
import com.ethercis.dao.access.support.DataAccess;
import com.ethercis.jooq.pg.tables.records.SystemRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

import java.net.NetworkInterface;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.UUID;

import static com.ethercis.jooq.pg.Tables.SYSTEM;

/**
 * Created by Christian Chevalley on 4/20/2015.
 */
public class SystemAccess extends DataAccess implements I_SystemAccess {

    private static final Logger log = LogManager.getLogger(SystemAccess.class);
    private SystemRecord systemRecord;

    public SystemAccess(DSLContext context){
        super(context, null);
    }

    public SystemAccess(I_DomainAccess domainAccess){
        super(domainAccess);
    }

    public SystemAccess(I_DomainAccess domainAccess , String description, String settings){
        super(domainAccess);
        systemRecord = domainAccess.getContext().newRecord(SYSTEM);
        systemRecord.setDescription(description);
        systemRecord.setSettings(settings);
    }

    /**
     * retrieveInstanceByNamedSubject the MAC address on a Windows 7 machine...
     * TODO: make it OS dependent
     * @return
     * @throws Exception
     */
    public static final String generateHashIdentifier() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces
                = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface == null
                    || networkInterface.isLoopback()
                    || networkInterface.isVirtual()) {
                continue;
            }
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac == null || mac.length == 0)
                continue;

            StringBuilder sb = new StringBuilder();
            int zeroFieldCount = 0;
            for (int i = 0; i < mac.length; i++) {
                if (mac[i] == 0)
                    zeroFieldCount++;
                sb.append(String.format("%02X%s", mac[i],
                        (i < mac.length - 1) ? "-" : ""));
            }

            if (zeroFieldCount > 4)
                continue;

            return sb.toString();
        }

        throw new RuntimeException("Failed to obtain MAC");
    }


    public static UUID createOrRetrieveLocalSystem(I_DomainAccess domainAccess) throws Exception {
        DSLContext context1 = domainAccess.getContext();
        String hostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        String MACAddress = generateHashIdentifier(); //it's not the MAC address...

        String settings = MACAddress+"|"+hostname;

        //try to retrieveInstanceByNamedSubject the corresponding entry in the system table
        Result<Record1<UUID>> uuids = context1.select(SYSTEM.ID).from(SYSTEM).where(SYSTEM.SETTINGS.equal(settings)).fetch();

        if (uuids.isEmpty()) { //storeComposition a new default entry

            Record result = context1.insertInto(SYSTEM, SYSTEM.DESCRIPTION, SYSTEM.SETTINGS).values("DEFAULT RUNNING SYSTEM", settings).returning(SYSTEM.ID).fetchOne();

            if (result == null)
                return null;

            return ((SystemRecord)result).getId();
        }

        return (UUID) uuids.get(0).getValue(0);
    }


    public static UUID retrieveInstanceId(I_DomainAccess domainAccess, String settings) throws Exception {
        UUID uuid = null;

        try {
            uuid = domainAccess.getContext().fetchOne(SYSTEM, SYSTEM.SETTINGS.eq(settings)).getId();

            if (uuid == null) {
                log.warn("Could not retrieveInstanceByNamedSubject system for settings:" + settings);
                return null;
            }
        }
        catch (Exception e){
            throw new IllegalArgumentException("Could not getNewInstance settings:"+settings+" Exception:"+e);
        }

        return uuid;
    }

    @Override
    public UUID commit(Timestamp transactionTime) throws Exception {
        systemRecord.store();
        return systemRecord.getId();
    }

    @Override
    public UUID commit() throws Exception {
        return commit(new Timestamp(DateTime.now().getMillis()));
    }

    @Override
    public Boolean update(Timestamp transactionTime) {

        if (systemRecord.changed()){
            return systemRecord.update() > 0;
        }

        return false;
    }

    @Override
    public Boolean update(Timestamp transactionTime, boolean force) throws Exception {
        return update(transactionTime);
    }

    @Override
    public Boolean update() throws Exception {
        return null;
    }

    @Override
    public Boolean update(Boolean force) throws Exception {
        return null;
    }

    @Override
    public Integer delete() {
        return systemRecord.delete();
    }

//    @Override
//    public I_SystemAccess retrieve(UUID id) throws Exception {
//        return retrieveInstance(this, id);
//    }

    public static I_SystemAccess retrieveInstance(I_DomainAccess domainAccess, UUID id) throws Exception {
        SystemAccess systemAccess = new SystemAccess(domainAccess);

        systemAccess.systemRecord = domainAccess.getContext().fetchOne(SYSTEM, SYSTEM.ID.eq(id));

        return systemAccess;
    }

    @Override
    public UUID getId(){
        return systemRecord.getId();
    }

    @Override
    public String getSettings() {return systemRecord.getSettings();}

    @Override
    public String getDescription() { return systemRecord.getDescription();}
}
