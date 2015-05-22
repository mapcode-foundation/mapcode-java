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

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class AlphabetTest {
    private static final Logger LOG = LoggerFactory.getLogger(AlphabetTest.class);

    @Test
    public void testConvertToAlphabet() throws Exception {
        LOG.info("testConvertToAlphabet");
        convertCodeInAlphabet("\u0397\u03a0.\u03982", Territory.GRC, Alphabet.GREEK, 0);
        convertCodeInAlphabet("\u0397\u03a0.\u03982-\u03a62", Territory.GRC, Alphabet.GREEK, 2);
        convertCodeInAlphabet("GRC \u0397\u03a0.\u03982-\u03a62", Territory.GRC, Alphabet.GREEK, 2);

        final String code = "26.53UK";
        final String codeGreek = Mapcode.convertStringToAlphabet(code, Alphabet.GREEK);
        final String codeAscii = Mapcode.convertStringToPlainAscii(codeGreek);
        LOG.info("code = {}, codeGreek = {}, codeAscii = {}", code, codeGreek, codeAscii);
        assertEquals(code, codeAscii);
    }

    @Test
    public void testFromString() throws Exception {
        LOG.info("testFromString");
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("ROMAN"));
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("0"));
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("roman"));

        for (final Alphabet alphabet : Alphabet.values()) {
            assertEquals(alphabet, Alphabet.fromString(alphabet.toString()));
            assertEquals(alphabet, Alphabet.fromString(String.valueOf(alphabet.getCode())));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError1() {
        LOG.info("testFromStringError1");
        Alphabet.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError2() {
        LOG.info("testFromStringError2");
        Alphabet.fromString("1A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError3() {
        LOG.info("testFromStringError3");
        Alphabet.fromString("99");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringError4() {
        LOG.info("testFromStringError4");
        Alphabet.fromString("ROMAN1");
    }

    private static void convertCodeInAlphabet(
            @Nonnull final String code,
            @Nonnull final Territory territory, @Nonnull final Alphabet alphabet,
            final int precision) throws Exception {

        // Check type.
        final FormatType formatType = FormatType.fromPrecision(precision);
        final FormatType type = Mapcode.getMapcodeFormatType(code);
        assertEquals("code = " + code + ", type = " + type, formatType, type);

        // Check original code and converted to ASCII point at same location.
        final String codeAscii = Mapcode.convertStringToPlainAscii(code);
        final Point pointCode = MapcodeCodec.decode(code, territory);
        final Point pointAscii = MapcodeCodec.decode(codeAscii, territory);
        assertEquals("code = " + code + ", pointCode = " + pointCode + ", pointAscii = " + pointAscii,
                pointCode, pointAscii);

        // Check if it re-encodes to the same mapcode codes.
        final Mapcode mapcode = MapcodeCodec.encodeToShortest(pointCode);
        final String codeRoman;
        final String codeAlphabet;
        if (Mapcode.containsTerritory(code)) {
            codeRoman = mapcode.getCodeWithTerritory(precision);
            codeAlphabet = mapcode.getCodeWithTerritory(precision, alphabet);
        } else {
            codeRoman = mapcode.getCode(precision);
            codeAlphabet = mapcode.getCode(precision, alphabet);
        }
        LOG.info("code = {}, codeAlphabet = {}, codeAscii = {}, codeRoman = {}", code, codeAlphabet, codeAscii, codeRoman);
        assertEquals(codeAscii, codeRoman);
        assertEquals(code, codeAlphabet);
    }
}
