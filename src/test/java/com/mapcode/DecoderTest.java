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

    private void assertEqualsWithinMillionth(final String name,final int v1,final int v2) throws Exception {
        if ( (v1-v2)>1 || (v2-v1)>1 ) {
          assertEquals(name,v1,v2);
        }
    }

    @Test
    public void decodeMapcodeWithTerritory() throws Exception {
        LOG.info("decodeMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("49.4V", Territory.NLD);
        assertEqualsWithinMillionth("decode latitude", 52376514, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point.getLonMicroDeg());
    }

    @Test
    public void decodeUpperLowercaseMapcode() throws Exception {
        LOG.info("decodeUpperLowercaseMapcode");
        final Point point1 = MapcodeCodec.decode("XXXXX.1234");
        assertEqualsWithinMillionth("decode latitude", 59596312, point1.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 155931892, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("Xxxxx.1234");
        assertEqualsWithinMillionth("decode latitude", 59596312, point2.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 155931892, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("xxxxx.1234");
        assertEqualsWithinMillionth("decode latitude", 59596312, point3.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 155931892, point3.getLonMicroDeg());
    }

    @Test
    public void decodeFullMapcode() throws Exception {
        LOG.info("decodeFullMapcode");
        final Point point1 = MapcodeCodec.decode("NLD 49.4V");
        assertEqualsWithinMillionth("decode latitude", 52376514, point1.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("US-ID LQJT.N94S");
        assertEqualsWithinMillionth("decode latitude", 45011346, point2.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", -113232731, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("US_ID LQJT.N94S");
        assertEqualsWithinMillionth("decode latitude", 45011346, point3.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", -113232731, point3.getLonMicroDeg());
    }

    @Test
    public void decodeInternationalMapcodeWithTerritory() throws Exception {
        LOG.info("decodeInternationalMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("VHXGB.1J9J", Territory.AAA);
        assertEqualsWithinMillionth("decode latitude", 52376504, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908535, point.getLonMicroDeg());
    }

    @Test
    public void decodeFullInternationalMapcode() throws Exception {
        LOG.info("decodeFullInternationalMapcode");
        final Point point = MapcodeCodec.decode("VHXGB.1J9J");
        assertEqualsWithinMillionth("decode latitude", 52376504, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908535, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionMapcodeWithTerritory() throws Exception {
        LOG.info("highPrecisionMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("49.4V-K2", Territory.NLD);
        assertEqualsWithinMillionth("decode hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decode hi-precision longitude", 4908540, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionFullMapcode() throws Exception {
        LOG.info("highPrecisionFullMapcode");
        Point point = MapcodeCodec.decode("NLD 49.4V-K2");
        assertEqualsWithinMillionth("decode hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decode hi-precision longitude", 4908540, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-0");
        assertEquals("decode 8-precision latitude", 51999955, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 4999901, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-123");
        assertEquals("decode 8-precision latitude", 51999948, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 4999925, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-MVRGBD0S");
        assertEquals("decode 8-precision latitude", 52000000, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 5000000, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis1() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis1");
        final Point point = MapcodeCodec.decode("\u0397\u03a0.\u03982-\u03a62", Territory.GRC);
        assertEqualsWithinMillionth("decodeUnicode latitude", 37971844, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 23726223,
                point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis2() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis2");
        final Point point = MapcodeCodec.decode("GRC \u0397\u03a0.\u03982-\u03a62");
        assertEqualsWithinMillionth("decodeUnicode latitude", 37971844, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 23726223,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeAthensAcropolis1() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis1");
        final Point point = MapcodeCodec.decode("\u0397\u03a0.\u03982", Territory.GRC);
        assertEqualsWithinMillionth("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 23726247,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeAthensAcropolis2() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis2");
        final Point point = MapcodeCodec.decode("GRC \u0397\u03a0.\u03982");
        assertEqualsWithinMillionth("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 23726247,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeAthensAcropolis3() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis3");
        final Point point = MapcodeCodec.decode("\u0393\u03a8\u039e \u0397\u03a0.\u03982");
        assertEqualsWithinMillionth("decodeUnicode latitude", 37971812, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 23726247,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeTokyoTower1() throws Exception {
        LOG.info("unicodeMapcodeTokyoTower1");
        final Point point = MapcodeCodec.decode("\u30c1\u30ca.8\u30c1",
                Territory.JPN);
        assertEqualsWithinMillionth("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 139745394,
                point.getLonMicroDeg());
    }

    @Test
    public void unicodeMapcodeTokyoTower2() throws Exception {
        LOG.info("unicodeMapcodeTokyoTower2");
        final Point point = MapcodeCodec.decode("JPN \u30c1\u30ca.8\u30c1");
        assertEqualsWithinMillionth("decodeUnicode latitude", 35658660, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 139745394,
                point.getLonMicroDeg());
    }

    @Test
    public void mapCodeWithZeroGroitzsch() throws Exception {
        LOG.info("mapCodeWithZeroGroitzsch");
        final Point point = MapcodeCodec.decode("HMVM.3Q0", Territory.DEU);
        assertEqualsWithinMillionth("decodeUnicode latitude", 51154852, point.getLatMicroDeg());
        assertEqualsWithinMillionth("decodeUnicode longitude", 12278574,
                point.getLonMicroDeg());
    }

    @Test
    public void validTerritory() throws Exception {
        LOG.info("validTerritory");
        final Point point0 = MapcodeCodec.decode("NLD 49.4V", null);
        assertEqualsWithinMillionth("decode latitude", 52376514, point0.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point0.getLonMicroDeg());

        final Point point1 = MapcodeCodec.decode("NLD 49.4V", Territory.NLD);
        assertEqualsWithinMillionth("decode latitude", 52376514, point1.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("NLD 49.4V", Territory.USA);
        assertEqualsWithinMillionth("decode latitude", 52376514, point2.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("NLD 49.4V");
        assertEqualsWithinMillionth("decode latitude", 52376514, point3.getLatMicroDeg());
        assertEqualsWithinMillionth("decode longitude", 4908542, point3.getLonMicroDeg());
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
        MapcodeCodec.decode("494.V49V", Territory.NLD);
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
    @Test(expected = UnknownMapcodeException.class)
    public void illegalArgument2() throws Exception {
        LOG.info("illegalArgument2");
        MapcodeCodec.decode("494.V49V", null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgument3() throws Exception {
        LOG.info("illegalArgument3");
        MapcodeCodec.decode(null);
    }
}
