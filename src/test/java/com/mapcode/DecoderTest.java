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
public class DecoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(DecoderTest.class);

    @Test
    public void decodeMapcodeWithTerritory() throws Exception {
        LOG.info("decodeMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("49.4V", Territory.NLD);
        assertEquals("decode latitude", 52376514, point.getLatMicroDeg());
        assertEquals("decode longitude", 4908542, point.getLonMicroDeg());
    }

    @Test
    public void decodeFullMapcode() throws Exception {
        LOG.info("decodeFullMapcode");
        final Point point1 = MapcodeCodec.decode("NLD 49.4V");
        assertEquals("decode latitude", 52376514, point1.getLatMicroDeg());
        assertEquals("decode longitude", 4908542, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("US-ID LQJT.N94S");
        assertEquals("decode latitude", 45011346, point2.getLatMicroDeg());
        assertEquals("decode longitude", -113232731, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("US_ID LQJT.N94S");
        assertEquals("decode latitude", 45011346, point3.getLatMicroDeg());
        assertEquals("decode longitude", -113232731, point3.getLonMicroDeg());
    }

    @Test
    public void decodeInternationalMapcodeWithTerritory() throws Exception {
        LOG.info("decodeInternationalMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("VHXGB.1J9J", Territory.AAA);
        assertEquals("decode latitude", 52376504, point.getLatMicroDeg());
        assertEquals("decode longitude", 4908535, point.getLonMicroDeg());
    }

    @Test
    public void decodeFullInternationalMapcode() throws Exception {
        LOG.info("decodeFullInternationalMapcode");
        final Point point = MapcodeCodec.decode("VHXGB.1J9J");
        assertEquals("decode latitude", 52376504, point.getLatMicroDeg());
        assertEquals("decode longitude", 4908535, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionMapcodeWithTerritory() throws Exception {
        LOG.info("highPrecisionMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("49.4V-K2", Territory.NLD);
        assertEquals("decode hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEquals("decode hi-precision longitude", 4908540, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionFullMapcode() throws Exception {
        LOG.info("highPrecisionFullMapcode");
        final Point point = MapcodeCodec.decode("NLD 49.4V-K2");
        assertEquals("decode hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEquals("decode hi-precision longitude", 4908540, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis1() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis1");
        final Point point = MapcodeCodec.decode("\u0397\u03a0.\u03982-\u03a62", Territory.GRC);
        assertEquals("decodeUnicode latitude", 37971844, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 23726223,
                point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis2() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis2");
        final Point point = MapcodeCodec.decode("GRC \u0397\u03a0.\u03982-\u03a62");
        assertEquals("decodeUnicode latitude", 37971844, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 23726223,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeAthensAcropolis1() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis1");
        final Point point = MapcodeCodec.decode("\u0397\u03a0.\u03982", Territory.GRC);
        assertEquals("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 23726247,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeAthensAcropolis2() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis2");
        final Point point = MapcodeCodec.decode("GRC \u0397\u03a0.\u03982");
        assertEquals("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 23726247,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeTokyoTower1() throws Exception {
        LOG.info("unicodeMapcodeTokyoTower1");
        final Point point = MapcodeCodec.decode("\u30c1\u30ca.8\u30c1",
                Territory.JPN);
        assertEquals("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 139745394,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeTokyoTower2() throws Exception {
        LOG.info("unicodeMapcodeTokyoTower2");
        final Point point = MapcodeCodec.decode("JPN \u30c1\u30ca.8\u30c1");
        assertEquals("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 139745394,
                point.getLonMicroDeg());
    }

    @Test
    public void mapCodeWithZeroGroitzsch() throws Exception {
        LOG.info("mapCodeWithZeroGroitzsch");
        final Point point = MapcodeCodec.decode("HMVM.3Q0", Territory.DEU);
        assertEquals("decodeUnicode latitude", 51154852, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 12278574,
                point.getLonMicroDeg());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidTerritory() throws Exception {
        LOG.info("invalidTerritory");
        MapcodeCodec.decode("NLD 49.4V", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNoDot() throws Exception {
        LOG.info("invalidNoDot");
        MapcodeCodec.decode("494V", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDotLocation1() throws Exception {
        LOG.info("invalidDotLocation1");
        MapcodeCodec.decode("4.94V", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDotLocation2() throws Exception {
        LOG.info("invalidDotLocation2");
        MapcodeCodec.decode("494.V", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDotLocation3() throws Exception {
        LOG.info("invalidDotLocation3");
        MapcodeCodec.decode("494V49.4V", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidMapcode1() throws Exception {
        LOG.info("invalidMapcode1");
        MapcodeCodec.decode("494.V494V", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHighPrecisionCharacter() throws Exception {
        LOG.info("invalidHighPrecisionCharacter");
        MapcodeCodec.decode("49.4V-Z", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHighPrecisionCharacter2() throws Exception {
        LOG.info("invalidHighPrecisionCharacter2");
        MapcodeCodec.decode("49.4V-HZ", Territory.NLD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHighPrecisionCharacter3() throws Exception {
        LOG.info("invalidHighPrecisionCharacter3");
        MapcodeCodec.decode("\u0397\u03a0.\u03982-\u0411", Territory.GRC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHighPrecisionCharacter4() throws Exception {
        LOG.info("invalidHighPrecisionCharacter4");
        MapcodeCodec.decode("\u0397\u03a0.\u03982-\u0411\u0411", Territory.GRC);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument1() throws Exception {
        LOG.info("illegalArgument1");
        MapcodeCodec.decode(null, Territory.NLD);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument2() throws Exception {
        LOG.info("illegalArgument2");
        MapcodeCodec.decode("494.V494V", null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument3() throws Exception {
        LOG.info("illegalArgument3");
        MapcodeCodec.decode(null);
    }
}
