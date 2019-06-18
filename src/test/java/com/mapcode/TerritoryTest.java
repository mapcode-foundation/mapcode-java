/*
 * Copyright (C) 2014-2019, Stichting Mapcode Foundation (http://www.mapcode.com)
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

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@SuppressWarnings("MagicNumber")
public class TerritoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(TerritoryTest.class);

    @Test(expected = UnknownTerritoryException.class)
    public void emptyTerritoryNumberTest() {
        LOG.info("emptyTerritoryNumberTest");
        Territory.fromString("");
    }

    @Test
    public void checkConvert() {
        LOG.info("checkConvert");
        final String s1 = "รฉะ ฑง.ดฯง";
        final String s2 = Mapcode.convertStringToPlainAscii(s1);
        assertEquals("THA KF.LZF", s2);
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
        assertEquals(1, Territory.VAT.getAlphabets().length);
        assertEquals(1, Territory.PRI.getAlphabets().length);
        assertEquals(1, Territory.AAA.getAlphabets().length);
        assertEquals(3, Territory.CN_XZ.getAlphabets().length);
        assertEquals(3, Territory.IN_LD.getAlphabets().length);
        assertEquals(Alphabet.ROMAN, Territory.VAT.getAlphabets()[0]);
        assertEquals(Alphabet.ROMAN, Territory.PRI.getAlphabets()[0]);
        assertEquals(Alphabet.ROMAN, Territory.CPT.getAlphabets()[0]);
        assertEquals(Alphabet.TIBETAN, Territory.CN_XZ.getAlphabets()[0]);
        assertEquals(Alphabet.CHINESE, Territory.CN_XZ.getAlphabets()[1]);
        assertEquals(Alphabet.ROMAN, Territory.CN_XZ.getAlphabets()[2]);
        assertEquals(Alphabet.MALAYALAM, Territory.IN_LD.getAlphabets()[0]);
        assertEquals(Alphabet.ROMAN, Territory.IN_LD.getAlphabets()[1]);
        assertEquals(Alphabet.DEVANAGARI, Territory.IN_LD.getAlphabets()[2]);
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
    public void testTerritoryFromString() {
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
    public void testTerritoryFromStringIncorrectDash1() {
        LOG.info("testTerritoryFromStringIncorrectDash1");

        // Issue: https://github.com/mapcode-foundation/mapcode-java/issues/23
        assertEquals(Territory.AAA, Territory.fromString("CHE-GR"));    // Exception must be thrown.
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testTerritoryFromStringIncorrectDash2() {
        LOG.info("testTerritoryFromStringIncorrectDash2");
        assertEquals(Territory.AAA, Territory.fromString("USA-NLD"));    // Exception must be thrown.
    }

    @Test(expected = UnknownTerritoryException.class)
    public void testTerritoryFromStringNumeric() {
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

    @Test
    public void testFromNumberOK() {
        LOG.info("testFromNumberOK");
        assertEquals(Territory.VAT, Territory.fromNumber(0));
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

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO2Error1() {
        LOG.info("testFromStringCountryISO2Error1");
        Territory.fromCountryISO2("US-IN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO2Error2() {
        LOG.info("testFromStringCountryISO2Error2");
        Territory.fromCountryISO2("USA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO2Error3() {
        LOG.info("testFromStringCountryISO2Error3");
        Territory.fromCountryISO2("");
    }

    @Test
    public void testFromStringCountryISO2OK() {
        LOG.info("testFromStringCountryISO2OK");
        assertEquals("NLD", Territory.fromCountryISO2("NL").toString());
        assertEquals("BRA", Territory.fromCountryISO2("br").toString());
        assertEquals("USA", Territory.fromCountryISO2("Us").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO3Error1() {
        LOG.info("testFromStringCountryISO3Error1");
        Territory.fromCountryISO3("US-IN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO3Error2() {
        LOG.info("testFromStringCountryISO3Error2");
        Territory.fromCountryISO3("US");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO3Error3() {
        LOG.info("testFromStringCountryISO3Error3");
        Territory.fromCountryISO3("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringCountryISO3Error4() {
        LOG.info("testFromStringCountryISO3Error4");
        Territory.fromCountryISO3("AAA");
    }

    @Test
    public void testFromStringCountryISO3OK() {
        LOG.info("testFromStringCountryISO3OK");
        assertEquals("NLD", Territory.fromCountryISO3("NLD").toString());
        assertEquals("BRA", Territory.fromCountryISO3("bra").toString());
        assertEquals("USA", Territory.fromCountryISO3("Usa").toString());
    }

    @Test
    public void testFromStringCountryISOOK() {
        LOG.info("testFromStringCountryISOOK");
        assertEquals("NLD", Territory.fromCountryISO2("NL").toString());
        assertEquals("BRA", Territory.fromCountryISO2("br").toString());
        assertEquals("USA", Territory.fromCountryISO2("Us").toString());
        assertEquals("NLD", Territory.fromCountryISO("NLD").toString());
        assertEquals("BRA", Territory.fromCountryISO("bra").toString());
        assertEquals("USA", Territory.fromCountryISO("Usa").toString());
    }

    @Test
    public void testGetCountryISO3FromISO2() {
        LOG.info("testGetCountryISO3FromISO2");
        assertEquals("NLD", Territory.getCountryISO3FromISO2("NL"));
        assertEquals("BRA", Territory.getCountryISO3FromISO2("br"));
        assertEquals("USA", Territory.getCountryISO3FromISO2("Us"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCountryISO3FromISO2Error() {
        LOG.info("testGetCountryISO3FromISO2Error");
        Territory.getCountryISO3FromISO2("NLD");
    }

    @Test
    public void testGetCountryISO2FromISO3() {
        LOG.info("testGetCountryISO2FromISO3");
        assertEquals("NL", Territory.getCountryISO2FromISO3("NLD"));
        assertEquals("BR", Territory.getCountryISO2FromISO3("bra"));
        assertEquals("US", Territory.getCountryISO2FromISO3("Usa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCountryISO2FromISO3Error() {
        LOG.info("testGetCountryISO2FromISO3Error");
        Territory.getCountryISO2FromISO3("NL");
    }

    @Test
    public void testGetCountryISO2Codes() {
        LOG.info("testGetCountryISO2Codes");
        final Set<String> countryISO2Codes = Territory.allCountryISO2Codes();
        LOG.info("set={}", countryISO2Codes);
        assertEquals(251, countryISO2Codes.size());
        assertEquals(2, countryISO2Codes.iterator().next().length());
    }

    @Test
    public void testGetCountryISO3Codes() {
        LOG.info("testGetCountryISO3Codes");
        final Set<String> countryISO3Codes = Territory.allCountryISO3Codes();
        LOG.info("set={}", countryISO3Codes);
        assertEquals(251, countryISO3Codes.size());
        assertEquals(3, countryISO3Codes.iterator().next().length());
    }
}
