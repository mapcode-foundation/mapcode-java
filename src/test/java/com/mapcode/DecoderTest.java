/*
 * Copyright (C) 2016-2021, Stichting Mapcode Foundation (http://www.mapcode.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapcode;

import com.mapcode.Decoder.Unicode2Ascii;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared", "MagicNumber"})
public class DecoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(DecoderTest.class);

    @Test
    public void tableDecodeChars() {
        LOG.info("tableDecodeChars");
        assertEquals("Length: " + Decoder.DECODE_CHARS.length, 256, Decoder.DECODE_CHARS.length);
    }

    @Test
    public void tableAscii2Language() {
        LOG.info("tableAscii2Language");
        assertEquals(Decoder.ASCII2LANGUAGE.length, Decoder.UNICODE2ASCII.length - 10 /* Digits */ - 3 /* Lowercase */);
        int i = 1;
        for (final char[] chars : Decoder.ASCII2LANGUAGE) {
            assertEquals("At row " + i + ", length: " + chars.length, 36, chars.length);
            ++i;
        }
    }

    @Test
    public void tableUnicodeToAscii() {
        LOG.info("tableUnicodeToAscii");
        for (final Unicode2Ascii unicode2Ascii : Decoder.UNICODE2ASCII) {
            assertEquals("Error at: u" + Integer.toHexString(unicode2Ascii.min),
                    (int) unicode2Ascii.max - (int) unicode2Ascii.min, unicode2Ascii.convert.length() - 1);
        }
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void getInternationalGrid() {
        LOG.info("getInternationalGrid");
        final DataModel dataModel = DataModel.getInstance();
        final int world = Territory.AAA.getNumber();
        final int to = dataModel.getDataLastRecord(world);
        final int from = dataModel.getDataFirstRecord(world);
        for (int index = from; index <= to; index++) {
            final Boundary boundary = Boundary.createBoundaryForTerritoryRecord(index);
            LOG.info("{}: ({}, {}), ({}, {})", (index - from) + 1,
                    boundary.getLatMicroDegMin() / 1.0e6,
                    boundary.getLonMicroDegMin() / 1.0e6,
                    boundary.getLatMicroDegMax() / 1.0e6,
                    boundary.getLonMicroDegMax() / 1.0e6
            );
        }
    }

    @Test
    public void decodeToRectangle() throws Exception {
        LOG.info("decodeToRectangle");
        Point center = MapcodeCodec.decode("49.4V", Territory.NLD);
        Rectangle rectangle = MapcodeCodec.decodeToRectangle("49.4V", Territory.NLD);
        Point southWest = Point.fromDeg(52.37646900000124, 4.9084705);
        Point northEast = Point.fromDeg(52.37655900000124, 4.908616250000001);
        LOG.debug("decodeToRectangle: center={}. rectangle={}", center, rectangle);
        assertEquals("center", center, rectangle.getCenter());
        assertEquals("southWest", southWest, rectangle.getSouthWest());
        assertEquals("northEast", northEast, rectangle.getNorthEast());

        center = MapcodeCodec.decode("VHXGB.1J9J");
        rectangle = MapcodeCodec.decodeToRectangle("VHXGB.1J9J");
        southWest = Point.fromDeg(52.376483, 4.908505);
        northEast = Point.fromDeg(52.376525, 4.908566);
        LOG.debug("decodeToRectangle: center={}. rectangle={}", center, rectangle);
        assertEquals("center", center, rectangle.getCenter());
        assertEquals("southWest", southWest, rectangle.getSouthWest());
        assertEquals("northEast", northEast, rectangle.getNorthEast());

        center = MapcodeCodec.decode("VHXGB.ZPHH");
        rectangle = MapcodeCodec.decodeToRectangle("VHXGB.ZPHH");
        southWest = Point.fromDeg(52.370015, 4.963710);
        northEast = Point.fromDeg(52.370057, 4.963758);
        LOG.debug("decodeToRectangle: center={}. rectangle={}", center, rectangle);
        assertEquals("center", center, rectangle.getCenter());
        assertEquals("southWest", southWest, rectangle.getSouthWest());
        assertEquals("northEast", northEast, rectangle.getNorthEast());

        center = MapcodeCodec.decode("VHWK3.Z0HF");
        rectangle = MapcodeCodec.decodeToRectangle("VHWK3.Z0HF");
        southWest = Point.fromDeg(52.317869, 4.675245);
        northEast = Point.fromDeg(52.317881, 4.675293);
        LOG.debug("decodeToRectangle: center={}. rectangle={}", center, rectangle);
        assertEquals("center", center, rectangle.getCenter());
        assertEquals("southWest", southWest, rectangle.getSouthWest());
        assertEquals("northEast", northEast, rectangle.getNorthEast());
    }

    @Test
    public void decodeMapcodeWithTerritory() throws Exception {
        LOG.info("decodeMapcodeWithTerritory");
        final Point point = MapcodeCodec.decode("49.4V", Territory.NLD);
        assertEquals("decode latitude", 52376514, point.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point.getLonMicroDeg());
    }

    @Test
    public void decodeUpperLowercaseMapcode() throws Exception {
        LOG.info("decodeUpperLowercaseMapcode");
        final Point point1 = MapcodeCodec.decode("XXXXX.1234");
        assertEquals("decode latitude", 59596312, point1.getLatMicroDeg());
        assertEquals("decode longitude", 155931892, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("Xxxxx.1234");
        assertEquals("decode latitude", 59596312, point2.getLatMicroDeg());
        assertEquals("decode longitude", 155931892, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("xxxxx.1234");
        assertEquals("decode latitude", 59596312, point3.getLatMicroDeg());
        assertEquals("decode longitude", 155931892, point3.getLonMicroDeg());
    }

    @Test
    public void decodeFullMapcode() throws Exception {
        LOG.info("decodeFullMapcode");
        final Point point1 = MapcodeCodec.decode("NLD 49.4V");
        assertEquals("decode latitude", 52376514, point1.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point1.getLonMicroDeg());

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
        Point point = MapcodeCodec.decode("NLD 49.4V-K2");
        assertEquals("decode hi-precision latitude", 52376512, point.getLatMicroDeg());
        assertEquals("decode hi-precision longitude", 4908540, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-0");
        assertEquals("decode 8-precision latitude", 51999954, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 4999900, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-123");
        assertEquals("decode 8-precision latitude", 51999948, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 4999924, point.getLonMicroDeg());
        point = MapcodeCodec.decode("NLD P42.NB1-MVRGBD0S");
        assertEquals("decode 8-precision latitude", 51999999, point.getLatMicroDeg());
        assertEquals("decode 8-precision longitude", 5000000, point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis1() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis1");
        final Point point = MapcodeCodec.decode("\u0397\u03a0.\u03982-\u03a62", Territory.GRC);
        assertEquals("decodeUnicode latitude", 37971843, point.getLatMicroDeg());
        assertEquals("decodeUnicode longitude", 23726223,
                point.getLonMicroDeg());
    }

    @Test
    public void highPrecisionUnicodeAthensAcropolis2() throws Exception {
        LOG.info("highPrecisionUnicodeAthensAcropolis2");
        final Point point = MapcodeCodec.decode("GRC \u0397\u03a0.\u03982-\u03a62");
        assertEquals("decodeUnicode latitude", 37971843, point.getLatMicroDeg());
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
    public void unicodeMapcodeAthensAcropolis3() throws Exception {
        LOG.info("unicodeMapcodeAthensAcropolis3");
        final Point point = MapcodeCodec.decode("\u0393\u03a8\u039e \u0397\u03a0.\u03982");
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

    @Test
    public void validTerritory() throws Exception {
        LOG.info("validTerritory");
        final Point point0 = MapcodeCodec.decode("NLD 49.4V", null);
        assertEquals("decode latitude", 52376514, point0.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point0.getLonMicroDeg());

        final Point point1 = MapcodeCodec.decode("NLD 49.4V", Territory.NLD);
        assertEquals("decode latitude", 52376514, point1.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point1.getLonMicroDeg());

        final Point point2 = MapcodeCodec.decode("NLD 49.4V", Territory.USA);
        assertEquals("decode latitude", 52376514, point2.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point2.getLonMicroDeg());

        final Point point3 = MapcodeCodec.decode("NLD 49.4V");
        assertEquals("decode latitude", 52376514, point3.getLatMicroDeg());
        assertEquals("decode longitude", 4908543, point3.getLonMicroDeg());
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

    @Test(expected = UnknownMapcodeException.class)
    public void invalidMapcode2() throws Exception {
        LOG.info("invalidMapcode2");
        MapcodeCodec.decode("NLD L333.333", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidMapcode3() throws Exception {
        LOG.info("invalidMapcode3");
        MapcodeCodec.decode("NLD SHH.HHH", Territory.NLD);
    }

    @Test(expected = UnknownMapcodeException.class)
    public void invalidMapcode4() throws Exception {
        LOG.info("invalidMapcode4");
        MapcodeCodec.decode("NLD V11N.NZZ", Territory.NLD);
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
