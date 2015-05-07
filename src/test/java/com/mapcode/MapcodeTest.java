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

import com.mapcode.Mapcode.MapcodeFormatType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MapcodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(MapcodeTest.class);

    @Test
    public void checkValidMapcodeFormats() {
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
    }

    @Test
    public void checkInvalidMapcodeFormats() {
        LOG.info("checkInvalidMapcodeFormats");

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

        assertEquals(MapcodeFormatType.MAPCODE_TYPE_INVALID, Mapcode.getMapcodeFormatType("ABC"));
        assertEquals(MapcodeFormatType.MAPCODE_TYPE_PRECISION_0, Mapcode.getMapcodeFormatType("AA.BB"));
        assertEquals(MapcodeFormatType.MAPCODE_TYPE_PRECISION_1, Mapcode.getMapcodeFormatType("AA.BB-1"));
        assertEquals(MapcodeFormatType.MAPCODE_TYPE_PRECISION_2, Mapcode.getMapcodeFormatType("AA.BB-12"));
    }

    @Test
    public void checkConvertToAscii() {
        LOG.info("checkConvertToAscii");
        assertEquals("KM.8K", Mapcode.convertToAscii("\u30c1\u30ca.8\u30c1"));
        assertEquals("HJ.Q2-Z", Mapcode.convertToAscii("\u0397\u03a0.\u03982-\u0411"));
    }
}
