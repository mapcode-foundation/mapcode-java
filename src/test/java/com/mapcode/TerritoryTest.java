/*
 * Copyright (C) 2014-2015 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import com.mapcode.Territory.AlphaCodeFormat;
import org.junit.Ignore;
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
    public void checkFullName() throws Exception {
        LOG.info("checkFullName");
        assertEquals(Territory.AAA, Territory.fromString("International"));
        assertEquals(Territory.AAA, Territory.fromString("Worldwide"));
        assertEquals(Territory.AAA, Territory.fromString("Earth"));
        assertEquals(Territory.NLD, Territory.fromString("Netherlands"));
        assertEquals(Territory.CN_XZ, Territory.fromString("Xizang"));
        assertEquals(Territory.CN_XZ, Territory.fromString("Tibet"));
    }

    @Test
    public void checkDash() throws Exception {
        LOG.info("checkDash");
        assertEquals(Territory.IN_MN, Territory.fromString("IND-MN"));
        assertEquals(Territory.IN_MN, Territory.fromString("IND_MN"));
        assertEquals(Territory.US_MN, Territory.fromString("USA-MN"));
        assertEquals(Territory.US_MN, Territory.fromString("USA_MN"));

        assertEquals(Territory.COD, Territory.fromString("Congo-Kinshasa"));
        assertEquals(Territory.US_IN, Territory.fromString("United States of America-IN"));
        assertEquals(Territory.US_IN, Territory.fromString("United States of America IN"));
    }

    @Test
    public void disambiguateMNTest1() throws Exception {
        LOG.info("disambiguateMNTest2");
        final Territory territory0 = Territory.fromString("IND-MN");
        final Territory territory1 = Territory.fromString("IN-MN");
        final Territory territory2 = Territory.fromString("MN", Territory.IND);
        final Territory territory3 = Territory.fromString("MN", Territory.USA);
        assertEquals(territory0, territory1);
        assertEquals(territory1, territory2);
        assertNotEquals(territory2, territory3);
    }

    @Test(expected = UnknownTerritoryException.class)
    public void disambiguateMNTest2() throws Exception {
        LOG.info("disambiguateMNTest2");
        Territory.fromString("MN", Territory.RUS);
    }

    @Test
    public void testTerritoryFromString() throws Exception {
        LOG.info("testTerritoryFromString");

        // Accept ISO-style codes.
        assertEquals(Territory.NLD, Territory.fromString("NLD"));
        assertEquals(Territory.ARG, Territory.fromString("ARG"));
        assertEquals(Territory.ASM, Territory.fromString("US-AS"));
        assertEquals(Territory.ASM, Territory.fromString("USA-AS"));
        assertEquals(Territory.RUS, Territory.fromString("RU"));
        assertEquals(Territory.CHN, Territory.fromString("CN"));
        assertEquals(Territory.AUS, Territory.fromString("AU"));
        assertEquals(Territory.US_IN, Territory.fromString("IN"));
        assertEquals(Territory.US_IN, Territory.fromString("US-IN"));
        assertEquals(Territory.US_IN, Territory.fromString("USA-IN"));
        assertEquals(Territory.RU_IN, Territory.fromString("RUS-IN"));
        assertEquals(Territory.IN_BR, Territory.fromString("BR"));
        assertEquals(Territory.IN_AS, Territory.fromString("AS"));

        // Accept long and short.
        assertEquals(Territory.USA, Territory.fromString("USA"));
        assertEquals(Territory.USA, Territory.fromString("US"));

        for (final Territory territory : Territory.values()) {
            assertEquals(territory, Territory.fromString(territory.toString()));
        }
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testTerritoryFromStringIncorrectDash1() throws Exception {
        LOG.info("testTerritoryFromStringIncorrectDash1");

        // Issue: https://github.com/mapcode-foundation/mapcode-java/issues/23
        assertEquals(Territory.AAA, Territory.fromString("CHE-GR"));    // Exception must be thrown.
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testTerritoryFromStringIncorrectDash2() throws Exception {
        LOG.info("testTerritoryFromStringIncorrectDash2");
        assertEquals(Territory.AAA, Territory.fromString("USA-NLD"));    // Exception must be thrown.
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testTerritoryFromStringNumeric() throws Exception {
        LOG.info("testTerritoryFromStringNumeric");

        // No longer support: numeric codes.
        assertEquals(Territory.AAA, Territory.fromString("0"));         // Exception must be thrown.
    }

    @Test
    public void checkAlphabet() throws Exception {
        LOG.info("checkAlphabet");
        assertEquals(Territory.NLD, Territory.fromString("NLD"));
        assertEquals(Territory.NLD, Territory.fromString("\u039d\u039b\u0394"));
        assertEquals(Territory.NLD, Territory.fromString("\u0417\u041b\u0414"));

        assertEquals("NLD", Territory.NLD.toAlphaCode(AlphaCodeFormat.INTERNATIONAL));
        assertEquals("NLD", Territory.NLD.toAlphaCode(AlphaCodeFormat.INTERNATIONAL, Alphabet.ROMAN));
        assertEquals("\u0417\u041b\u0414", Territory.NLD.toAlphaCode(AlphaCodeFormat.INTERNATIONAL, Alphabet.CYRILLIC));
        assertEquals("\u039d\u039b\u0394", Territory.NLD.toAlphaCode(AlphaCodeFormat.INTERNATIONAL, Alphabet.GREEK));
        assertEquals("\u0393\u03a8\u039e", Territory.GRC.toAlphaCode(AlphaCodeFormat.INTERNATIONAL, Alphabet.GREEK));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError1() {
        LOG.info("testFromStringError1");
        Territory.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError2() {
        LOG.info("testFromStringError2");
        Territory.fromString("1A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError3() {
        LOG.info("testFromStringError3");
        Territory.fromString("999");
    }
}
