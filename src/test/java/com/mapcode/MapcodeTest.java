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

import com.mapcode.Mapcode.PrecisionFormat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class MapcodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(MapcodeTest.class);

    @Test
    public void checkValidPrecisionFormats() {
        LOG.info("checkValidPrecisionFormats");

        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("A1.B1"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("a1.B1"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("00.01"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AAA.01"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AAA.BBB"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AAAA.BBB"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AAAA.BBBB"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AAAAA.BBBB"));
        assertEquals(PrecisionFormat.PRECISION_1, Mapcode.getPrecisionFormat("AA.AA-0"));
        assertEquals(PrecisionFormat.PRECISION_2, Mapcode.getPrecisionFormat("AA.AA-01"));
        assertEquals(PrecisionFormat.PRECISION_1, Mapcode.getPrecisionFormat("AA.AA-A"));
        assertEquals(PrecisionFormat.PRECISION_2, Mapcode.getPrecisionFormat("AA.AA-AA"));
        assertEquals(PrecisionFormat.PRECISION_1, Mapcode.getPrecisionFormat("AA.AA-Y"));
        assertEquals(PrecisionFormat.PRECISION_2, Mapcode.getPrecisionFormat("AA.AA-1Y"));
        assertEquals(PrecisionFormat.PRECISION_3, Mapcode.getPrecisionFormat("AA.AA-321"));
        assertEquals(PrecisionFormat.PRECISION_4, Mapcode.getPrecisionFormat("AA.AA-21PQ"));
        assertEquals(PrecisionFormat.PRECISION_5, Mapcode.getPrecisionFormat("AA.AA-321PQ"));
        assertEquals(PrecisionFormat.PRECISION_6, Mapcode.getPrecisionFormat("AA.AA-321PQR"));
        assertEquals(PrecisionFormat.PRECISION_7, Mapcode.getPrecisionFormat("AA.AA-4321PRS"));
        assertEquals(PrecisionFormat.PRECISION_8, Mapcode.getPrecisionFormat("AA.AA-4321PQRS"));

        // Mapcode may contain correctly formatted (possible incorrect) territory code.
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("NLD XX.XX"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("USA-NLD XX.XX"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("IN XX.XX"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("US-IN XX.XX"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("US_IN XX.XX"));
        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("RU-IN XX.XX"));
    }

    @Test
    public void checkInvalidPrecisionFormats() {
        LOG.info("checkInvalidPrecisionFormats");

        // Territory code must be correct syntax.
        assertFalse(Mapcode.isValidMapcodeFormat("NL- XX.XX"));
        assertFalse(Mapcode.isValidMapcodeFormat("US IN XX.XX"));

        // Incorrect (nunber of) characters.
        assertFalse(Mapcode.isValidMapcodeFormat("A"));
        assertFalse(Mapcode.isValidMapcodeFormat("AB"));
        assertFalse(Mapcode.isValidMapcodeFormat("AB."));
        assertFalse(Mapcode.isValidMapcodeFormat(".A"));
        assertFalse(Mapcode.isValidMapcodeFormat(".AB"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.B"));
        assertFalse(Mapcode.isValidMapcodeFormat("a.B"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.1"));
        assertFalse(Mapcode.isValidMapcodeFormat("0.01"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01."));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01.0"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01.00"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01-"));
        assertFalse(Mapcode.isValidMapcodeFormat("00.01-"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAAAAA.BBBB"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAAAA.BBBBB"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-1Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-12Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-123Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-1234Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-12345Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-123456Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-1234567Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-123456789")); // more than 8 eAtension characters!
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA-12"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A-12"));
    }

    @Test
    public void checkPrecisionFormat1() {
        LOG.info("checkPrecisionFormat1");

        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("AA.BB"));
        assertEquals(PrecisionFormat.PRECISION_1, Mapcode.getPrecisionFormat("AA.BB-1"));
        assertEquals(PrecisionFormat.PRECISION_2, Mapcode.getPrecisionFormat("AA.BB-12"));
        assertEquals(PrecisionFormat.PRECISION_3, Mapcode.getPrecisionFormat("AA.BB-123"));
        assertEquals(PrecisionFormat.PRECISION_4, Mapcode.getPrecisionFormat("AA.BB-1234"));
        assertEquals(PrecisionFormat.PRECISION_5, Mapcode.getPrecisionFormat("AA.BB-12345"));
        assertEquals(PrecisionFormat.PRECISION_6, Mapcode.getPrecisionFormat("AA.BB-123456"));
        assertEquals(PrecisionFormat.PRECISION_7, Mapcode.getPrecisionFormat("AA.BB-1234567"));
        assertEquals(PrecisionFormat.PRECISION_8, Mapcode.getPrecisionFormat("AA.BB-12345678"));

        assertEquals(PrecisionFormat.PRECISION_0, Mapcode.getPrecisionFormat("\u0e9a\u0e97\u0e84 \u0eab\u0ea7\u0e84.\u0ea73\u0eaa"));
        assertEquals(PrecisionFormat.PRECISION_1, Mapcode.getPrecisionFormat("\u0f40\u0f64\u0f4c \u0f535\u0f41\u0f42.5\u0f629-\u0f40"));
        assertEquals(PrecisionFormat.PRECISION_2, Mapcode.getPrecisionFormat("\u039d\u039b\u0394 \u03a7\u03a6\u0394.\u03a63\u03a9-\u039e7"));
    }

    @Test(expected = UnknownPrecisionFormatException.class)
    public void checkPrecisionFormat2() {
        LOG.info("checkPrecisionFormat2");

        Mapcode.getPrecisionFormat("ABC");
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
