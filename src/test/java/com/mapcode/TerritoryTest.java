/*
 * Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapcode;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class TerritoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(TerritoryTest.class);

    @Test(expected = UnknownTerritoryException.class)
    public void emptyTerritoryCodeTest() throws Exception {
        LOG.info("emptyCodeTest");
        Territory.fromString("");
    }

    @Test
    public void disambiguateMNTest1() throws Exception {
        LOG.info("disambiguateMNTest2");
        final Territory territory1 = Territory.fromString("IND-MN");
        final Territory territory2 = Territory.fromString("MN", ParentTerritory.IND);
        final Territory territory3 = Territory.fromString("MN", ParentTerritory.USA);
        assertEquals(territory1, territory2);
        assertNotEquals(territory2, territory3);
    }

    @Test(expected = UnknownTerritoryException.class)
    public void disambiguateMNTest2() throws Exception {
        LOG.info("disambiguateMNTest2");
        Territory.fromString("MN", ParentTerritory.RUS);
    }
}
