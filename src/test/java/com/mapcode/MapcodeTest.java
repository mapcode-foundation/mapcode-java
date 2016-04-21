/*
 * Copyright (C) 2014-2016 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import static org.junit.Assert.*;

public class MapcodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(MapcodeTest.class);

    @Test
    public void checkValidMapcodeFormat() {
        LOG.info("checkValidMapcodeFormat");
        assertTrue(Mapcode.isValidMapcodeFormat("A1.B1"));
        assertTrue(Mapcode.isValidMapcodeFormat("a1.B1"));
        assertTrue(Mapcode.isValidMapcodeFormat("00.01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAA.01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAA.BBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAA.BBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAA.BBBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAAA.BBBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-0"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-A"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-AA"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-Y"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-1Y"));
    }

    @Test
    public void checkValidPrecisionFormats() {
        LOG.info("checkValidPrecisionFormats");

        assertEquals(0, Mapcode.getPrecisionFormat("P1.B1"));
        assertEquals(0, Mapcode.getPrecisionFormat("p1.B1"));
        assertEquals(0, Mapcode.getPrecisionFormat("00.01"));
        assertEquals(0, Mapcode.getPrecisionFormat("PPP.01"));
        assertEquals(0, Mapcode.getPrecisionFormat("PPP.BBB"));
        assertEquals(0, Mapcode.getPrecisionFormat("PPPP.BBB"));
        assertEquals(0, Mapcode.getPrecisionFormat("PPPP.BBBB"));
        assertEquals(0, Mapcode.getPrecisionFormat("PPPPP.BBBB"));
        assertEquals(1, Mapcode.getPrecisionFormat("PP.PP-0"));
        assertEquals(2, Mapcode.getPrecisionFormat("PP.PP-01"));
        assertEquals(1, Mapcode.getPrecisionFormat("PP.PP-P"));
        assertEquals(2, Mapcode.getPrecisionFormat("PP.PP-PP"));
        assertEquals(1, Mapcode.getPrecisionFormat("PP.PP-Y"));
        assertEquals(2, Mapcode.getPrecisionFormat("PP.PP-1Y"));
        assertEquals(3, Mapcode.getPrecisionFormat("PP.PP-321"));
        assertEquals(4, Mapcode.getPrecisionFormat("PP.PP-21PQ"));
        assertEquals(5, Mapcode.getPrecisionFormat("PP.PP-321PQ"));
        assertEquals(6, Mapcode.getPrecisionFormat("PP.PP-321PQR"));
        assertEquals(7, Mapcode.getPrecisionFormat("PP.PP-4321PRS"));
        assertEquals(8, Mapcode.getPrecisionFormat("PP.PP-4321PQRS"));

        // Mapcode may contain correctly formatted (possible incorrect) territory code.
        assertEquals(0, Mapcode.getPrecisionFormat("NLD XX.XX"));
        assertEquals(0, Mapcode.getPrecisionFormat("USA-NLD XX.XX"));
        assertEquals(0, Mapcode.getPrecisionFormat("IN XX.XX"));
        assertEquals(0, Mapcode.getPrecisionFormat("US-IN XX.XX"));
        assertEquals(0, Mapcode.getPrecisionFormat("US_IN XX.XX"));
        assertEquals(0, Mapcode.getPrecisionFormat("RU-IN XX.XX"));
    }

    @Test
    public void checkInvalidPrecisionFormats() {
        LOG.info("checkInvalidPrecisionFormats");

        // Territory code must be correct syntax.
        assertFalse(Mapcode.isValidMapcodeFormat("NL- XX.XX"));
        assertFalse(Mapcode.isValidMapcodeFormat("US IN XX.XX"));

        // Incorrect (nunber of) characters.
        assertFalse(Mapcode.isValidMapcodeFormat("P"));
        assertFalse(Mapcode.isValidMapcodeFormat("PQ"));
        assertFalse(Mapcode.isValidMapcodeFormat("PQ."));
        assertFalse(Mapcode.isValidMapcodeFormat(".P"));
        assertFalse(Mapcode.isValidMapcodeFormat(".PQ"));
        assertFalse(Mapcode.isValidMapcodeFormat("P.Q"));
        assertFalse(Mapcode.isValidMapcodeFormat("p.Q"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.01"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01."));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01.0"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01.00"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01-"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01-"));
        assertFalse(Mapcode.isValidMapcodeFormat("PPPPPP.QQQQ"));
        assertFalse(Mapcode.isValidMapcodeFormat("PPPPP.QQQQQ"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-1Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-12Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-123Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-1234Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-12345Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-123456Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-1234567Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("PP.PP-123456789")); // more than 8 ePtension characters!
        assertFalse(Mapcode.isValidMapcodeFormat("P.PPP"));
        assertFalse(Mapcode.isValidMapcodeFormat("PPP.P"));
        assertFalse(Mapcode.isValidMapcodeFormat("P.PPP-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("PPP.P-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("P.PPP-12"));
        assertFalse(Mapcode.isValidMapcodeFormat("PPP.P-12"));
    }

    @Test
    public void checkPrecisionFormat1() {
        LOG.info("checkPrecisionFormat1");

        assertEquals(0, Mapcode.getPrecisionFormat("PP.QQ"));
        assertEquals(1, Mapcode.getPrecisionFormat("PP.QQ-1"));
        assertEquals(2, Mapcode.getPrecisionFormat("PP.QQ-12"));
        assertEquals(3, Mapcode.getPrecisionFormat("PP.QQ-123"));
        assertEquals(4, Mapcode.getPrecisionFormat("PP.QQ-1234"));
        assertEquals(5, Mapcode.getPrecisionFormat("PP.QQ-12345"));
        assertEquals(6, Mapcode.getPrecisionFormat("PP.QQ-123456"));
        assertEquals(7, Mapcode.getPrecisionFormat("PP.QQ-1234567"));
        assertEquals(8, Mapcode.getPrecisionFormat("PP.QQ-12345678"));

        assertEquals(0, Mapcode.getPrecisionFormat("\u0e9a\u0e97\u0e84 \u0eab\u0ea7\u0e84.\u0ea73\u0eaa"));
        assertEquals(1, Mapcode.getPrecisionFormat("\u0f40\u0f64\u0f4c \u0f535\u0f41\u0f42.5\u0f629-\u0f40"));
        assertEquals(2, Mapcode.getPrecisionFormat("\u039d\u039b\u0394 \u03a7\u03a6\u0394.\u03a63\u03a9-\u039e7"));
    }

    @Test(expected = UnknownPrecisionFormatException.class)
    public void checkUnknownPrecisionFormatException1() {
        LOG.info("checkUnknownPrecisionFormatException1");
        Mapcode.getPrecisionFormat("ABC");
    }

    @Test(expected = UnknownPrecisionFormatException.class)
    public void checkUnknownPrecisionFormatException2() {
        LOG.info("checkUnknownPrecisionFormatException2");
        Mapcode.getPrecisionFormat("494.V494V");
    }

    @Test
    public void checkConvertToAscii() {
        LOG.info("checkConvertToAscii");

        // Check ASCII characters.
        assertEquals("KM.8K", Mapcode.convertStringToPlainAscii("KM.8K"));
        assertEquals("HJ.Q2-Z", Mapcode.convertStringToPlainAscii("HJ.Q2-Z"));
        assertEquals("36228.92UW", Mapcode.convertStringToPlainAscii("36228.92UW"));
        assertEquals("36228.92UW-TK", Mapcode.convertStringToPlainAscii("36228.92UW-TK"));
        assertEquals("12345.12EU-12345678", Mapcode.convertStringToPlainAscii("12345.12EU-12345678"));

        // Check unicode characters.
        assertEquals("GRC", Mapcode.convertStringToPlainAscii("\u0393\u03a8\u039e"));
        assertEquals("KM.8K", Mapcode.convertStringToPlainAscii("\u30c1\u30ca.8\u30c1"));
        assertEquals("HJ.Q2-Z", Mapcode.convertStringToPlainAscii("\u0397\u03a0.\u03982-\u0411"));
    }
}
