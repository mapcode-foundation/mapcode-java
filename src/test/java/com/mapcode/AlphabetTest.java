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

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testConvertToAlphabet() throws Exception {
        LOG.info("testConvertToAlphabet");
        convertCodeInAlphabet("\u0397\u03a0.\u03982", Territory.GRC, Alphabet.GREEK, 0);
        convertCodeInAlphabet("\u0397\u03a0.\u03982-\u03a62", Territory.GRC, Alphabet.GREEK, 2);
        convertCodeInAlphabet("GRC \u0397\u03a0.\u03982-\u03a62", Territory.GRC, Alphabet.GREEK, 2);

        convertCodeInAlphabet("XX.XX", Territory.NLD, Alphabet.GREEK, 0);
        convertCodeInAlphabet("XX.XX-12", Territory.NLD, Alphabet.GREEK, 2);

        convertCodeInAlphabet("NLD XX.XX", Territory.USA, Alphabet.GREEK, 0);
        convertCodeInAlphabet("NLD XX.XX-12", Territory.USA, Alphabet.GREEK, 2);

        convertCodeInAlphabet("36128.92UW", Territory.GRC, Alphabet.GREEK, 0);
        convertCodeInAlphabet("36228.92UW-TK", Territory.GRC, Alphabet.GREEK, 2);
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
        final String codeAscii = Mapcode.convertMapcodeToPlainAscii(code);
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
