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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class AlphabetTest {
    private static final Logger LOG = LoggerFactory.getLogger(AlphabetTest.class);

    @Test
    public void convertGreek1() throws Exception {
        LOG.info("convertGreek1");
        final String a1 = "\u0397\u03a0.\u03982";
        LOG.info("type = {}", Mapcode.getMapcodeFormatType(a1));
        final String x1 = Mapcode.convertToAscii(a1);
        final Point p1 = MapcodeCodec.decode(x1, Territory.GRC);
        final String y1 = MapcodeCodec.encodeToShortest(p1).getMapcode();
        final String b1 = Mapcode.convertToAlphabet(y1, Alphabet.GREEK);
        LOG.info("a1 = {}, b1 = {}, x1 = {}, y1 = {}, p1 = {}", a1, b1, x1, y1, p1);
        assertEquals(a1, b1);
        assertEquals(x1, y1);

        final String a2 = "\u0397\u03a0.\u03982-\u03a62";
        LOG.info("type = {}", Mapcode.getMapcodeFormatType(a2));
        final String x2 = Mapcode.convertToAscii(a2);
        final Point p2 = MapcodeCodec.decode(x2, Territory.GRC);
        final String y2 = MapcodeCodec.encodeToShortest(p2).getMapcodePrecision(2);
        final String b2 = Mapcode.convertToAlphabet(y2, Alphabet.GREEK);
        LOG.info("a2 = {}, b2 = {}, x2 = {}, y2 = {}, p2 = {}", a2, b2, x2, y2, p2);
        assertEquals(a2, b2);
        assertEquals(x2, y2);

        final String a3 = "GRC \u0397\u03a0.\u03982-\u03a62";
        LOG.info("type = {}", Mapcode.getMapcodeFormatType(a3));
        final String x3 = Mapcode.convertToAscii(a3);
        final Point p3 = MapcodeCodec.decode(x3);
        final String y3 = MapcodeCodec.encodeToShortest(p3).getMapcodePrecision(2);
        final String b3 = Mapcode.convertToAlphabet(y3, Alphabet.GREEK);
        LOG.info("a3 = {}, b3 = {}, x3 = {}, y3 = {}, p3 = {}", a3, b3, x3, y3, p3);
        assertEquals(a3, Territory.GRC.toString() + ' ' + b3);
        assertEquals(x3, Territory.GRC.toString() + ' ' + y3);
    }

    @Test
    public void convertGreek2() throws Exception {
        LOG.info("convertGreek2");
        final String a1 = "36128.92UW";
        final Point p1 = MapcodeCodec.decode(a1);
        final String y1 = MapcodeCodec.encodeToShortest(p1).getMapcodePrecision(0);
        final String b1 = Mapcode.convertToAlphabet(y1, Alphabet.GREEK);
        final String d1 = Mapcode.convertToAlphabet(y1, Alphabet.ROMAN);
        LOG.info("a1 = {}, b1 = {}, c1 = {}, d1 = {}, y1 = {}, p1 = {}", a1, b1, d1, y1, p1);
        assertEquals(a1, y1);
        assertEquals(a1, d1);

        final String a2 = "36228.92UW-TK";
        final Point p2 = MapcodeCodec.decode(a2);
        final String y2 = MapcodeCodec.encodeToShortest(p2).getMapcodePrecision(2);
        final String b2 = Mapcode.convertToAlphabet(y2, Alphabet.GREEK);
        final String c2 = Mapcode.convertToAlphabet(b2, Alphabet.ROMAN);
        LOG.info("a2 = {}, b2 = {}, c2 = {}, y2 = {}, p2 = {}", a2, b2, c2, y2, p2);
        assertEquals(a2, c2);
    }
}
