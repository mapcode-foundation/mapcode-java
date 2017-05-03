/*
 * Copyright (C) 2014-2017, Stichting Mapcode Foundation (http://www.mapcode.com)
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class TerritoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(TerritoryTest.class);

    @Test(expected = UnknownTerritoryException.class)
    public void emptyTerritoryCodeTest() {
        LOG.info("emptyCodeTest");
        Territory.fromString("");
    }

    @Test
    public void checkFullName() {
        LOG.info("checkFullName");
        assertEquals(Territory.AAA, Territory.fromString("International"));
        assertEquals(Territory.AAA, Territory.fromString("Worldwide"));
        assertEquals(Territory.AAA, Territory.fromString("Earth"));
        assertEquals(Territory.NLD, Territory.fromString("Netherlands"));
        assertEquals(Territory.CN_XZ, Territory.fromString("Xizang"));
        assertEquals(Territory.CN_XZ, Territory.fromString("Tibet"));
    }

    @Test
    public void checkAlphabets() {
        LOG.info("checkAlphabets");
        assertEquals(Territory.VAT.getAlphabets().length, 1);
        assertEquals(Territory.PRI.getAlphabets().length, 1);
        assertEquals(Territory.AAA.getAlphabets().length, 1);
        assertEquals(Territory.CN_XZ.getAlphabets().length, 3);
        assertEquals(Territory.IN_LD.getAlphabets().length, 3);
        assertEquals(Territory.VAT.getAlphabets()[0], Alphabet.ROMAN);
        assertEquals(Territory.PRI.getAlphabets()[0], Alphabet.ROMAN);
        assertEquals(Territory.CPT.getAlphabets()[0], Alphabet.ROMAN);
        assertEquals(Territory.CN_XZ.getAlphabets()[0], Alphabet.TIBETAN);
        assertEquals(Territory.CN_XZ.getAlphabets()[1], Alphabet.CHINESE);
        assertEquals(Territory.CN_XZ.getAlphabets()[2], Alphabet.ROMAN);
        assertEquals(Territory.IN_LD.getAlphabets()[0], Alphabet.MALAYALAM);
        assertEquals(Territory.IN_LD.getAlphabets()[1], Alphabet.ROMAN);
        assertEquals(Territory.IN_LD.getAlphabets()[2], Alphabet.DEVANAGARI);
    }

    @Test
    public void checkDash() {
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
    public void disambiguateMNTest1() {
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
    public void disambiguateMNTest2() {
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
    public void checkAlphabet() {
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

    @Test(expected = UnknownTerritoryException.class)
    public void testFromStringError1() {
        LOG.info("testFromStringError1");
        Territory.fromString("");
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testFromStringError2() {
        LOG.info("testFromStringError2");
        Territory.fromString("1A");
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testFromStringError3() {
        LOG.info("testFromStringError3");
        Territory.fromString("999");
    }

    public void testFromNumberOK() {
        LOG.info("testFromNumberOK");
        Territory.fromNumber(0);
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testFromNumberError1() {
        LOG.info("testFromNumberError1");
        Territory.fromNumber(-1);
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testFromNumberError2() {
        LOG.info("testFromNumberError2");
        Territory.fromNumber(99999);
    }
}
