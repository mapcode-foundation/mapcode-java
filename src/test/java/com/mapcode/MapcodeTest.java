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

import com.mapcode.Mapcode.FormatType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class MapcodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(MapcodeTest.class);

    @Test
    public void checkValidCodeFormats() {
        LOG.info("checkValidMapcodeFormats");

        assertTrue(Mapcode.isValidMapcodeFormat("A1.B1"));
        assertTrue(Mapcode.isValidMapcodeFormat("a1.B1"));
        assertTrue(Mapcode.isValidMapcodeFormat("00.01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAA.01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAA.BBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAA.BBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAA.BBBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAAA.BBBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AAAAA.BBBBB"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-0"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-01"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-A"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-AA"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-Y"));
        assertTrue(Mapcode.isValidMapcodeFormat("AA.AA-1Y"));

        // Mapcode may contain correctly formatted (possible incorrect) territory code.
        assertTrue(Mapcode.isValidMapcodeFormat("NLD XX.XX"));
        assertTrue(Mapcode.isValidMapcodeFormat("USA-NLD XX.XX"));
        assertTrue(Mapcode.isValidMapcodeFormat("IN XX.XX"));
        assertTrue(Mapcode.isValidMapcodeFormat("US-IN XX.XX"));
        assertTrue(Mapcode.isValidMapcodeFormat("US_IN XX.XX"));
        assertTrue(Mapcode.isValidMapcodeFormat("RU-IN XX.XX"));

        // Territory code must be correct syntax.
        assertFalse(Mapcode.isValidMapcodeFormat("NL- XX.XX"));
        assertFalse(Mapcode.isValidMapcodeFormat("US IN XX.XX"));
    }

    @Test
    public void checkInvalidCodeFormats() {
        LOG.info("checkInvalidMapcodeFormats");

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
        assertFalse(Mapcode.isValidMapcodeFormat("AAAAAA.BBBBB"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAAAA.BBBBBB"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-012"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("AA.AA-1Z"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A-1"));
        assertFalse(Mapcode.isValidMapcodeFormat("A.AAA-12"));
        assertFalse(Mapcode.isValidMapcodeFormat("AAA.A-12"));
    }

    @Test
    public void checkMapcodeFormatType() {
        LOG.info("checkMapcodeFormatType");

        assertEquals(FormatType.INVALID, Mapcode.getMapcodeFormatType("ABC"));
        assertEquals(FormatType.PRECISION_0, Mapcode.getMapcodeFormatType("AA.BB"));
        assertEquals(FormatType.PRECISION_1, Mapcode.getMapcodeFormatType("AA.BB-1"));
        assertEquals(FormatType.PRECISION_2, Mapcode.getMapcodeFormatType("AA.BB-12"));
    }

    @Test
    public void checkConvertToAscii() {
        LOG.info("checkConvertToAscii");

        // Check ASCII characters.
        assertEquals("KM.8K", Mapcode.convertStringToPlainAscii("KM.8K"));
        assertEquals("HJ.Q2-Z", Mapcode.convertStringToPlainAscii("HJ.Q2-Z"));
        assertEquals("36228.92UW", Mapcode.convertStringToPlainAscii("36228.92UW"));
        assertEquals("36228.92UW-TK", Mapcode.convertStringToPlainAscii("36228.92UW-TK"));

        // Check unicode characters.
        assertEquals("GRC", Mapcode.convertStringToPlainAscii("\u0393\u03a8\u039e"));
        assertEquals("KM.8K", Mapcode.convertStringToPlainAscii("\u30c1\u30ca.8\u30c1"));
        assertEquals("HJ.Q2-Z", Mapcode.convertStringToPlainAscii("\u0397\u03a0.\u03982-\u0411"));
    }
}
