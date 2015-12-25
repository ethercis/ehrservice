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

import com.ethercis.ehr.encode.wrappers.*;
import junit.framework.Assert;
import org.apache.commons.collections4.MapUtils;
import org.junit.Test;
import org.openehr.build.RMObjectBuilder;
import org.openehr.rm.datatypes.quantity.DvInterval;
import org.openehr.rm.datatypes.quantity.datetime.DvDate;
import org.openehr.rm.datatypes.quantity.datetime.DvDateTime;

import java.util.Map;

/**
 * Created by christian on 11/19/2015.
 */
public class FieldUtilTest {

    @Test
    public void testFlatten() throws Exception {
        RMObjectBuilder rmObjectBuilder = new RMObjectBuilder();

        DvInterval<DvDateTime> dvDateTimeDvInterval = DvIntervalVBean.createQualifiedInterval(DvDate.class);

        Map<String, Object> requiredParameters = FieldUtil.getAttributes(PartyIdentifiedVBean.generate());

        Assert.assertNotNull(requiredParameters);

        Map flatten = FieldUtil.flatten(requiredParameters);

        Assert.assertNotNull(flatten);
        MapUtils.debugPrint(System.out, "FLATTEN", flatten);
    }
}