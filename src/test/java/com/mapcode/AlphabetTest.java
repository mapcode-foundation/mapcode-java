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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class AlphabetTest {
    private static final Logger LOG = LoggerFactory.getLogger(AlphabetTest.class);

    private static final String[] CODE_TEST_PAIRS = {
            "26.53UK", "\u039161.328\u03A5",
            "FR.B016", "\u0395\u03A81.0716",
            "000.EU", "\u0391\u0030\u0030.23",
            "PQ.YZ", "\u03a1\u0398.57\u0396",
            "PQ.XYZ", "\u03a1\u0398.56\u03a5\u0396",
            "PQR.YZ", "\u03a1\u0398.89\u03a5\u0396",
            "PQ.RXYZ", "\u03a1\u03983.\u03a70\u03a5\u0396",
            "PQR.XYZ", "\u03a1\u03986.\u03a71\u03a5\u0396",
            "PQRX.YZ", "\u03a1\u03989.\u03a72\u03a5\u0396",
            "PQR.SXYZ", "\u03a1\u03984.\u03a3\u03a79\u03a5\u0396",
            "PQRS.XYZ", "\u03a1\u03988.\u03a3\u03a79\u03a5\u0396",
            "PQRS.WXYZ", "\u03a1\u03987\u03a3.8\u03a9\u03a72\u03a5",
            "PQRST.WXYZ", "\u03a1\u03987\u03a3\u03a4.8\u03a9\u03a72\u03a5",
            "P4.YZ", "\u03a14.\u03a5\u0396",
            "PQ.4Z", "\u03a1\u0398.4\u0396",
            "PQ.4YZ", "\u03a1\u0398.26\u03a5\u0396",
            "PQ4.YZ", "\u03a1\u03984.\u03a5\u0396",
            "PQ.46YZ", "\u03a1\u03981.61\u03a5\u0396",
            "PQ4.6YZ", "\u03a1\u03984.6\u03a5\u0396",
            "PQ46.YZ", "\u03a1\u039846.\u03a5\u0396",
            "PQ4.S6YZ", "\u03a1\u03982.\u03a366\u03a5\u0396",
            "PQ4S.6YZ", "\u03a1\u03984\u03a3.6\u03a5\u0396",
            "PQ4S.W6YZ", "\u03a1\u03984\u03a3.\u03a96\u03a5\u0396",
            "PQ4PQ.6YZ9", "\u03a1\u03984\u03a1\u0398.6\u03a5\u03969",
            "PQ.YZ-BCD", "\u03a1\u0398.57\u0396-\u0392\u039e\u0394",
            "PQ.XYZ-BCD", "\u03a1\u0398.56\u03a5\u0396-\u0392\u039e\u0394",
            "PQR.YZ-BCD", "\u03a1\u0398.89\u03a5\u0396-\u0392\u039e\u0394",
            "PQ.RXYZ-BCD", "\u03a1\u03983.\u03a70\u03a5\u0396-\u0392\u039e\u0394",
            "PQR.XYZ-BCD", "\u03a1\u03986.\u03a71\u03a5\u0396-\u0392\u039e\u0394",
            "PQRX.YZ-BCD", "\u03a1\u03989.\u03a72\u03a5\u0396-\u0392\u039e\u0394",
            "PQR.SXYZ-BCD", "\u03a1\u03984.\u03a3\u03a79\u03a5\u0396-\u0392\u039e\u0394",
            "PQRS.XYZ-BCD", "\u03a1\u03988.\u03a3\u03a79\u03a5\u0396-\u0392\u039e\u0394",
            "PQRS.WXYZ-BCD", "\u03a1\u03987\u03a3.8\u03a9\u03a72\u03a5-\u0392\u039e\u0394",
            "PQRST.WXYZ-BCD", "\u03a1\u03987\u03a3\u03a4.8\u03a9\u03a72\u03a5-\u0392\u039e\u0394",
            "P4.YZ-BCD", "\u03a14.\u03a5\u0396-\u0392\u039e\u0394",
            "PQ.4Z-BCD", "\u03a1\u0398.4\u0396-\u0392\u039e\u0394",
            "PQ.4YZ-BCD", "\u03a1\u0398.26\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4.YZ-BCD", "\u03a1\u03984.\u03a5\u0396-\u0392\u039e\u0394",
            "PQ.46YZ-BCD", "\u03a1\u03981.61\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4.6YZ-BCD", "\u03a1\u03984.6\u03a5\u0396-\u0392\u039e\u0394",
            "PQ46.YZ-BCD", "\u03a1\u039846.\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4.S6YZ-BCD", "\u03a1\u03982.\u03a366\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4S.6YZ-BCD", "\u03a1\u03984\u03a3.6\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4S.W6YZ-BCD", "\u03a1\u03984\u03a3.\u03a96\u03a5\u0396-\u0392\u039e\u0394",
            "PQ4PQ.6YZ9-BCD", "\u03a1\u03984\u03a1\u0398.6\u03a5\u03969-\u0392\u039e\u0394",
            ""
    };

    @Test
    public void testOrderOfValues() {
        int i = 0;
        for (final Alphabet alphabet : Alphabet.values()) {
            assertEquals("Incorrect alphabet number: " + alphabet + ".number should be " + i, i, Alphabet.values()[i].getNumber());
            ++i;
        }
    }

    @Test
    public void testConvertToAlphabet() throws Exception {

        LOG.info("testConvertToAlphabet");
        convertCodeInAlphabet("\u0397\u03a0.22", Territory.GRC, Alphabet.GREEK, 0);
        convertCodeInAlphabet("\u0397\u03a0.22-\u03a62", Territory.GRC, Alphabet.GREEK, 2);
        convertCodeInAlphabet("GRC \u0397\u03a0.22-\u03a62", Territory.GRC, Alphabet.GREEK, 2);

        int i = 0;
        while (true) {
            final String code = CODE_TEST_PAIRS[i];
            if (code.isEmpty()) {
                break;
            }
            final String codeGreek = Mapcode.convertStringToAlphabet(code, Alphabet.GREEK);
            LOG.debug("testConvertToAlphabet: code={}, codeGreek={}", code, codeGreek);
            final String expect = CODE_TEST_PAIRS[i + 1];
            if (!expect.isEmpty()) {
                assertEquals(codeGreek, expect);
            }
            final String codeAscii = Mapcode.convertStringToPlainAscii(codeGreek);
            LOG.debug("testConvertToAlphabet: code={}, codeGreek={}, codeAscii={}", code, codeGreek, codeAscii);
            assertEquals(code, codeAscii);
            i += 2;
        }
    }

    @Test
    public void testFromString() {
        LOG.info("testFromString");
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("ROMAN"));
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("roman"));

        for (final Alphabet alphabet : Alphabet.values()) {
            assertEquals(alphabet, Alphabet.fromString(alphabet.toString()));
        }
    }

    @Test(expected = UnknownAlphabetException.class)
    public void testFromStringNumeric() {
        LOG.info("testFromStringNumeric");
        assertEquals(Alphabet.ROMAN, Alphabet.fromString("0"));
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
        final int type = Mapcode.getPrecisionFormat(code);
        assertEquals("code = " + code + ", type = " + type, precision, type);

        // Check original code and converted to ASCII point at same location.
        final String codeAscii = Mapcode.convertStringToPlainAscii(code);
        final Point pointCode = MapcodeCodec.decode(code, territory);
        final Point pointAscii = MapcodeCodec.decode(codeAscii, territory);
        assertEquals("code = " + code + ", pointCode = " + pointCode + ", pointAscii = " + pointAscii,
                pointCode, pointAscii);

        // Check if it re-encodes to the same mapcode codes.
        final Mapcode mapcode = MapcodeCodec.encodeToShortest(pointCode, territory);
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
