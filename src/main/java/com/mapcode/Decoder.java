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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class Decoder {
    private static final Logger LOG = LoggerFactory.getLogger(Decoder.class);

    private Decoder() {
        // Prevent instantiation.
    }

    // ----------------------------------------------------------------------
    // Method called from public Java API.
    // ----------------------------------------------------------------------

    @Nonnull
    static Point decode(@Nonnull final String argMapcode,
                        @Nonnull final Territory argTerritory)
            throws UnknownMapcodeException {
        LOG.trace("decode: mapcode={}, territory={}", argMapcode, argTerritory.name());

        String mapcode = argMapcode;
        Territory territory = argTerritory;

        // In case of error, result.isDefined() is false.
        Point result = Point.undefined();
        String extrapostfix = "";

        final int minpos = mapcode.indexOf('-');
        if (minpos > 0) {
            extrapostfix = decodeUTF16(mapcode.substring(minpos + 1).trim());
            if (extrapostfix.contains("Z")) {
                throw new UnknownMapcodeException("Invalid character Z");
            }
            mapcode = mapcode.substring(0, minpos);
        }

        mapcode = aeuUnpack(mapcode).trim();
        if (mapcode.isEmpty()) {
            return result; // failed to decode!
        }

        final int incodexlen = mapcode.length() - 1;

        // *** long codes in states are handled by the country
        if (incodexlen >= 9) {
            territory = Territory.AAA;
        } else {
            final Territory parentTerritory = territory.getParentTerritory();
            if (((incodexlen >= 8) && ((parentTerritory == Territory.USA) || (parentTerritory == Territory.CAN)
                    || (parentTerritory == Territory.AUS) || (parentTerritory == Territory.BRA)
                    || (parentTerritory == Territory.CHN) || (parentTerritory == Territory.RUS)))
                    || ((incodexlen >= 7) &&
                    ((parentTerritory == Territory.IND) || (parentTerritory == Territory.MEX)))) {

                territory = parentTerritory;
            }
        }

        final int ccode = territory.getTerritoryCode();

        final int from = DataAccess.dataFirstRecord(ccode);
        if (DataAccess.dataFlags(from) == 0) {
            return Point.undefined(); // this territory is not in the current data
        }
        final int upto = DataAccess.dataLastRecord(ccode);

        final int incodexhi = mapcode.indexOf('.');

        final Data mapcoderData = new Data();

        for (int i = from; i <= upto; i++) {
            mapcoderData.dataSetup(i);
            if ((mapcoderData.getPipeType() == 0) && !mapcoderData.isNameless()
                    && (mapcoderData.getCodexLen() == incodexlen) && (mapcoderData.getCodexHi() == incodexhi)) {

                result = decodeGrid(mapcode, mapcoderData.getMapcoderRect().getMinX(), mapcoderData.getMapcoderRect()
                                .getMinY(), mapcoderData.getMapcoderRect().getMaxX(), mapcoderData.getMapcoderRect().getMaxY(),
                        i, extrapostfix);
                // RESTRICTUSELESS
                if (mapcoderData.isUseless() && result.isDefined()) {
                    boolean fitssomewhere = false;
                    int j;
                    for (j = upto - 1; j >= from; j--) { // look in previous
                        // rects
                        mapcoderData.dataSetup(j);
                        if (mapcoderData.isUseless()) {
                            continue;
                        }
                        final int xdiv8 = Common.xDivider(mapcoderData.getMapcoderRect().getMinY(),
                                mapcoderData.getMapcoderRect().getMaxY()) / 4;
                        if (mapcoderData.getMapcoderRect().extendBounds(xdiv8, 60).containsPoint(result)) {
                            fitssomewhere = true;
                            break;
                        }
                    }
                    if (!fitssomewhere) {
                        result.setUndefined();
                    }
                }
                break;
            } else if ((mapcoderData.getPipeType() == 4) && ((mapcoderData.getCodexLen() + 1) == incodexlen)
                    && ((mapcoderData.getCodexHi() + 1) == incodexhi)
                    && (mapcoderData.getPipeLetter().charAt(0) == mapcode.charAt(0))) {
                result = decodeGrid(mapcode.substring(1), mapcoderData.getMapcoderRect().getMinX(), mapcoderData
                        .getMapcoderRect().getMinY(), mapcoderData.getMapcoderRect().getMaxX(), mapcoderData
                        .getMapcoderRect().getMaxY(), i, extrapostfix);
                break;
            } else if (mapcoderData.isNameless()
                    && (((mapcoderData.getCodex() == 21) && (incodexlen == 4) && (incodexhi == 2))
                    || ((mapcoderData.getCodex() == 22) && (incodexlen == 5) && (incodexhi == 3)) || ((mapcoderData
                    .getCodex() == 13) && (incodexlen == 5) && (incodexhi == 2)))) {
                result = decodeNameless(mapcode, i, extrapostfix, mapcoderData);
                break;
            } else if ((mapcoderData.getPipeType() > 4) && (incodexlen == (incodexhi + 3))
                    && ((mapcoderData.getCodexLen() + 1) == incodexlen)) {
                result = decodeStarpipe(mapcode, i, extrapostfix, mapcoderData);
                break;
            }
        }

        if (result.isDefined()) {
            if (result.getLonMicroDeg() > 180000000) {
                result = Point.fromMicroDeg(result.getLatMicroDeg(), result.getLonMicroDeg() - 360000000);
            } else if (result.getLonMicroDeg() < -180000000) {
                result = Point.fromMicroDeg(result.getLatMicroDeg(), result.getLonMicroDeg() + 360000000);
            }

            // LIMIT_TO_OUTRECT : make sure it fits the country
            if (ccode != CCODE_EARTH) {
                final SubArea mapcoderRect = SubArea.getArea(upto); // find
                // encompassing
                // rect
                final int xdiv8 = Common.xDivider(mapcoderRect.getMinY(), mapcoderRect.getMaxY()) / 4;
                // should be /8 but there's some extra margin
                if (!mapcoderRect.extendBounds(xdiv8, 60).containsPoint(result)) {
                    result.setUndefined(); // decodes outside the official territory
                    // limit
                }
            }
        }

        LOG.trace("decode: result=({}, {})",
                result.isDefined() ? result.getLatDeg() : Double.NaN,
                result.isDefined() ? result.getLonDeg() : Double.NaN);
        result = Point.restrictLatLon(result);
        return result;
    }

    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------

    private static final int CCODE_EARTH = 540;

    private final static int[] decode_chars = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, -2, 10, 11, 12, -3, 13, 14, 15,
            1, 16, 17, 18, 19, 20, 0, 21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1, -1, -1, -2, 10, 11,
            12, -3, 13, 14, 15, 1, 16, 17, 18, 19, 20, 0, 21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    private static class Unicode2Ascii {

        public final int min;
        public final int max;
        public final String convert;

        public Unicode2Ascii(final int min, final int max, @Nullable final String convert) {
            this.min = min;
            this.max = max;
            this.convert = convert;
        }
    }

    private final static Unicode2Ascii[] UNICODE2ASCII = {
            new Unicode2Ascii(0x0041, 0x005a, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"), // Roman
            new Unicode2Ascii(0x0391, 0x03a9, "ABGDFZHQIKLMNCOJP?STYVXRW"), // Greek
            new Unicode2Ascii(0x0410, 0x042f, "AZBGDEFNI?KLMHOJPCTYQXSVW????U?R"), // Cyrillic
            new Unicode2Ascii(0x05d0, 0x05ea, "ABCDFIGHJKLMNPQ?ROSETUVWXYZ"), // Hebrew
            new Unicode2Ascii(0x0905, 0x0939, "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QU?RS?T?V??W??XY"), // Hindi
            new Unicode2Ascii(0x0d07, 0x0d39, "I?U?E??????A??BCD??F?G??HOJ??KLMNP?????Q?RST?VWX?YZ"), // Malai
            new Unicode2Ascii(0x10a0, 0x10bf, "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"), // Georgisch
            new Unicode2Ascii(0x30a2, 0x30f2, "A?I?O?U?EB?C?D?F?G?H???J???????K??????L?M?N?????P??Q??R??S?????TV?????WX???Y????Z"), // Katakana
            new Unicode2Ascii(0x0e01, 0x0e32, "BC?D??FGHJ??O???K??L?MNP?Q?R????S?T?V?W????UXYZAIE"), // Thai
            new Unicode2Ascii(0x0e81, 0x0ec6, "BC?D??FG?H??J??????K??L?MN?P?Q??RST???V??WX?Y?ZA????????????U?????EI?O"), // Lao
            new Unicode2Ascii(0x0532, 0x0556, "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA"), // Armenian
            new Unicode2Ascii(0x0985, 0x09b9, "A??????B??E???U?CDF?GH??J??KLMNPQR?S?T?VW?X??Y??????Z"), // Bengali
            new Unicode2Ascii(0x0a05, 0x0a39, "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QU?RS?T?V??W??XY"), // Gurmukhi
            new Unicode2Ascii(0x0f40, 0x0f66, "BCD?FGHJ??K?L?MN?P?QR?S?A?????TV?WXYEUZ"), // Tibetan

            new Unicode2Ascii(0x0966, 0x096f, ""), // Hindi
            new Unicode2Ascii(0x0d66, 0x0d6f, ""), // Malai
            new Unicode2Ascii(0x0e50, 0x0e59, ""), // Thai
            new Unicode2Ascii(0x09e6, 0x09ef, ""), // Bengali
            new Unicode2Ascii(0x0a66, 0x0a6f, ""), // Gurmukhi
            new Unicode2Ascii(0x0f20, 0x0f29, ""), // Tibetan

            // lowercase variants: greek, georgisch
            new Unicode2Ascii(0x03B1, 0x03c9, "ABGDFZHQIKLMNCOJP?STYVXRW"), // Greek
            // lowercase
            new Unicode2Ascii(0x10d0, 0x10ef, "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"), // Georgisch lowercase
            new Unicode2Ascii(0x0562, 0x0586, "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA"), // Armenian
            // lowercase
            new Unicode2Ascii(0, 0, null)
    };

    @Nonnull
    private static Point decodeGrid(final String str, final int minx, final int miny, final int maxx, final int maxy,
                                    final int m, final String extrapostfix) {
        // for a well-formed result, and integer variables
        String result = str;
        int relx;
        int rely;
        final int codexlen = result.length() - 1; // length ex dot
        int dc = result.indexOf('.'); // dotposition

        if ((dc == 1) && (codexlen == 5)) {
            dc++;
            result = result.substring(0, 1) + result.charAt(2) + '.' + result.substring(3);
        }
        final int codexlow = codexlen - dc;
        final int codex = (10 * dc) + codexlow;

        final int divx;
        int divy;
        divy = DataAccess.smartDiv(m);
        if (divy == 1) {
            divx = Common.xSide[dc];
            divy = Common.ySide[dc];
        } else {
            divx = Common.nc[dc] / divy;
        }

        if ((dc == 4) && (divx == Common.xSide[4]) && (divy == Common.ySide[4])) {
            result = result.substring(0, 1) + result.charAt(2) + result.charAt(1) + result.substring(3);
        }

        int v = fastDecode(result);

        if ((divx != divy) && (codex > 24)) // D==6
        {
            final Point d = decode6(v, divx, divy);
            relx = d.getLonMicroDeg();
            rely = d.getLatMicroDeg();
        } else {
            relx = v / divy;
            rely = v % divy;
            rely = divy - 1 - rely;
        }

        final int ygridsize = (((maxy - miny) + divy) - 1) / divy;
        final int xgridsize = (((maxx - minx) + divx) - 1) / divx;

        rely = miny + (rely * ygridsize);
        relx = minx + (relx * xgridsize);

        final int dividery = ((ygridsize + Common.ySide[codexlow]) - 1) / Common.ySide[codexlow];
        final int dividerx = ((xgridsize + Common.xSide[codexlow]) - 1) / Common.xSide[codexlow];

        String rest = result.substring(dc + 1);

        // decoderelative (postfix vs rely,relx)
        final int difx;
        int dify;
        final int nrchars = rest.length();

        if (nrchars == 3) {
            final Point d = decodeTriple(rest);
            difx = d.getLonMicroDeg();
            dify = d.getLatMicroDeg();
        } else {
            if (nrchars == 4) {
                rest = String.valueOf(rest.charAt(0)) + rest.charAt(2) + rest.charAt(1) + rest.charAt(3);
            }
            v = fastDecode(rest);
            difx = v / Common.ySide[nrchars];
            dify = v % Common.ySide[nrchars];
        }

        dify = Common.ySide[nrchars] - 1 - dify;

        final int cornery = rely + (dify * dividery);
        final int cornerx = relx + (difx * dividerx);
        return add2res(cornery, cornerx, dividerx << 2, dividery, 1, extrapostfix);
    }

    @Nonnull
    private static Point decodeNameless(final String str, final int firstrec, final String extrapostfix,
                                        final Data mapcoderData) {
        String result = str;
        if (mapcoderData.getCodex() == 22) {
            result = result.substring(0, 3) + result.substring(4);
        } else {
            result = result.substring(0, 2) + result.substring(3);
        }

        int a = Common.countCityCoordinatesForCountry(mapcoderData.getCodex(), firstrec, firstrec);
        if (a < 2) {
            a = 1; // paranoia
        }

        final int p = 31 / a;
        final int r = 31 % a;
        int v = 0;
        int nrX;
        boolean swapletters = false;

        if ((mapcoderData.getCodex() != 21) && (a <= 31)) {
            final int offset = decode_chars[(int) result.charAt(0)];

            if (offset < (r * (p + 1))) {
                nrX = offset / (p + 1);
            } else {
                swapletters = (p == 1) && (mapcoderData.getCodex() == 22);
                nrX = r + ((offset - (r * (p + 1))) / p);
            }
        } else if ((mapcoderData.getCodex() != 21) && (a < 62)) {
            nrX = decode_chars[(int) result.charAt(0)];
            if (nrX < (62 - a)) {
                swapletters = mapcoderData.getCodex() == 22;
            } else {
                nrX = ((nrX + nrX) - 62) + a;
            }
        } else {
            // codex==21 || A>=62
            final int basePower = (mapcoderData.getCodex() == 21) ? (961 * 961) : (961 * 961 * 31);
            int basePowerA = basePower / a;
            if (a == 62) {
                basePowerA++;
            } else {
                basePowerA = 961 * (basePowerA / 961);
            }

            // decode and determine x
            v = fastDecode(result);
            nrX = v / basePowerA;
            v %= basePowerA;
        }

        if (swapletters && !Data.isSpecialShape(firstrec + nrX)) {
            result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
        }

        if ((mapcoderData.getCodex() != 21) && (a <= 31)) {
            v = fastDecode(result);
            if (nrX > 0) {
                v -= ((nrX * p) + ((nrX < r) ? nrX : r)) * 961 * 961;
            }
        } else if ((mapcoderData.getCodex() != 21) && (a < 62)) {
            v = fastDecode(result.substring(1));
            if ((nrX >= (62 - a)) && (v >= (16 * 961 * 31))) {
                v -= 16 * 961 * 31;
                nrX++;
            }
        }

        if (nrX > a) {
            return Point.undefined(); // return undefined (past end!)
        }
        mapcoderData.dataSetup(firstrec + nrX);

        int side = DataAccess.smartDiv(firstrec + nrX);
        int xSIDE = side;

        final int maxy = mapcoderData.getMapcoderRect().getMaxY();
        final int minx = mapcoderData.getMapcoderRect().getMinX();
        final int miny = mapcoderData.getMapcoderRect().getMinY();

        final int dx;
        final int dy;

        if (mapcoderData.isSpecialShape()) {
            xSIDE *= side;
            side = 1 + ((maxy - miny) / 90);
            xSIDE = xSIDE / side;

            final Point d = decode6(v, xSIDE, side);
            dx = d.getLonMicroDeg();
            dy = side - 1 - d.getLatMicroDeg();
        } else {
            dy = v % side;
            dx = v / side;
        }

        if (dx >= xSIDE) // else out-of-range!
        {
            return Point.undefined(); // return undefined (out of range!)
        }

        final int dividerx4 = Common.xDivider(miny, maxy); // 4 times too large!
        final int dividery = 90;

        final int cornerx = minx + ((dx * dividerx4) / 4); // FIRST multiply, THEN
        // divide!
        final int cornery = maxy - (dy * dividery);
        return add2res(cornery, cornerx, dividerx4, dividery, -1, extrapostfix);
    }

    @Nonnull
    private static Point decodeStarpipe(final String input, final int firstindex, final String extrapostfix,
                                        @Nonnull final Data mapcoderData) {
        // returns Point.isUndefined() in case or error
        int storageStart = 0;
        final int thiscodexlen = mapcoderData.getCodexLen();

        int value = fastDecode(input); // decode top (before dot)
        value *= 961 * 31;
        final Point triple = decodeTriple(input.substring(input.length() - 3));
        // decode bottom 3 chars

        int i;
        i = firstindex;
        while (true) {
            if (Data.calcCodexLen(i) != thiscodexlen) {
                return Point.undefined(); // return undefined
            }
            if (i > firstindex) {
                mapcoderData.dataSetup(i);
            }

            final int maxx = mapcoderData.getMapcoderRect().getMaxX();
            final int maxy = mapcoderData.getMapcoderRect().getMaxY();
            final int minx = mapcoderData.getMapcoderRect().getMinX();
            final int miny = mapcoderData.getMapcoderRect().getMinY();

            int h = ((maxy - miny) + 89) / 90;
            final int xdiv = Common.xDivider(miny, maxy);
            int w = ((((maxx - minx) * 4) + xdiv) - 1) / xdiv;

            h = 176 * (((h + 176) - 1) / 176);
            w = 168 * (((w + 168) - 1) / 168);

            int product = (w / 168) * (h / 176) * 961 * 31;

            final int goodRounder = (mapcoderData.getCodex() >= 23) ? (961 * 961 * 31) : (961 * 961);
            if (mapcoderData.getPipeType() == 8) {
                // *+
                product = ((((storageStart + product + goodRounder) - 1) / goodRounder) * goodRounder) - storageStart;
            }

            if ((value >= storageStart) && (value < (storageStart + product))) {
                // code belongs here?
                final int dividerx = (((maxx - minx) + w) - 1) / w;
                final int dividery = (((maxy - miny) + h) - 1) / h;

                value -= storageStart;
                value = value / (961 * 31);
                // PIPELETTER DECODE
                int vx = value / (h / 176);
                vx = (vx * 168) + triple.getLonMicroDeg();
                final int vy = ((value % (h / 176)) * 176) + triple.getLatMicroDeg();

                final int cornery = maxy - (vy * dividery);
                final int cornerx = minx + (vx * dividerx);

                /*
                 * Sri Lanka Defect (v1.1)
                 * {
                 *   int c1 = (zonedata == 0) ? -1 : decode_chars[(int) input .charAt(input.length() - 3)];
                 *   Point zd = addzonedata(cornery + (triple.getY() - 176) dividery,
                 *     cornerx - triple.getX() * dividerx, 176 * dividery, 168 * dividerx, c1, dividerx,
                 *     dividery);
                 *   cornery = zd.getY();
                 *   cornerx = zd.getX();
                 * }
                 */

                final Point retval = add2res(cornery, cornerx, dividerx << 2, dividery, -1, extrapostfix);

                return retval;
            }
            storageStart += product;
            i++;
        }
    }

    @Nonnull
    private static String aeuUnpack(final String argStr) {
        // unpack encoded into all-digit
        // (assume str already uppercase!), returns "" in case of error
        String str = decodeUTF16(argStr);
        boolean voweled = false;
        final int lastpos = str.length() - 1;
        int dotpos = str.indexOf('.');
        if ((dotpos < 2) || (lastpos < (dotpos + 2))) {
            return ""; // Error: no dot, or less than 2 letters before dot, or
        }
        // less than 2 letters after dot

        if (str.charAt(0) == 'A') { // v1.50
            int v1 = decode_chars[(int) str.charAt(lastpos)];
            if (v1 < 0) {
                v1 = 31;
            }
            int v2 = decode_chars[(int) str.charAt(lastpos - 1)];
            if (v2 < 0) {
                v2 = 31;
            }
            String s = String.valueOf(1000 + v1 + (32 * v2));
            str = s.charAt(1) + str.substring(1, lastpos - 1) + s.charAt(2) + s.charAt(3);
            voweled = true;
        } else if (str.charAt(0) == 'U') { // v.1.50 debug decoding of U+alldigitmapcode
            voweled = true;
            str = str.substring(1);
            dotpos--;
        } else {
            int v = str.charAt(lastpos - 1);
            if (v == 'A') {
                v = 0;
            } else if (v == 'E') {
                v = 34;
            } else if (v == 'U') {
                v = 68;
            } else {
                v = -1;
            }
            if (v >= 0) {
                final char e = str.charAt(lastpos);
                if (e == 'A') {
                    v += 31;
                } else if (e == 'E') {
                    v += 32;
                } else if (e == 'U') {
                    v += 33;
                } else {
                    final int ve = decode_chars[(int) str.charAt(lastpos)];
                    if (ve < 0) {
                        return "";
                    }
                    v += ve;
                }
                if (v >= 100) {
                    return "";
                }
                voweled = true;
                str = str.substring(0, lastpos - 1) + Data.ENCODE_CHARS[v / 10]
                        + Data.ENCODE_CHARS[v % 10];
            }
        }

        if ((dotpos < 2) || (dotpos > 5)) {
            return "";
        }

        for (int v = 0; v <= lastpos; v++) {
            if (v != dotpos) {
                if (decode_chars[(int) str.charAt(v)] < 0) {
                    return ""; // bad char!
                } else if (voweled && (decode_chars[(int) str.charAt(v)] > 9)) {
                    return ""; // nonodigit!
                }
            }
        }

        return str;
    }

    /**
     * This method decodes a Unicode string to ASCII. Package private for access by other modules.
     *
     * @param str Unicode string.
     * @return ASCII string.
     */
    static String decodeUTF16(final String str) {
        final StringBuilder asciibuf = new StringBuilder();
        for (int index = 0; index < str.length(); index++) {
            if (str.charAt(index) == '.') {
                asciibuf.append(str.charAt(index));
            } else if ((str.charAt(index) >= 1) && (str.charAt(index) <= 'z')) {
                // normal ascii
                asciibuf.append(str.charAt(index));
            } else {
                boolean found = false;
                for (int i = 0; UNICODE2ASCII[i].min != 0; i++) {
                    if ((str.charAt(index) >= UNICODE2ASCII[i].min)
                            && (str.charAt(index) <= UNICODE2ASCII[i].max)) {
                        String convert = UNICODE2ASCII[i].convert;
                        if (convert == null) {
                            convert = "0123456789";
                        }
                        asciibuf.append(convert.charAt(((int) str.charAt(index)) - UNICODE2ASCII[i].min));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    asciibuf.append('?');
                    break;
                }
            }
        }

        return asciibuf.toString();
    }

    @Nonnull
    private static Point decodeTriple(final String str) {
        //noinspection NumericCastThatLosesPrecision
        final byte c1 = (byte) decode_chars[(int) str.charAt(0)];
        final int x = fastDecode(str.substring(1));
        if (c1 < 24) {
            return Point.fromMicroDeg(((c1 / 6) * 34) + (x % 34), ((c1 % 6) * 28) + (x / 34));
        }
        return Point.fromMicroDeg((x % 40) + 136, (x / 40) + (24 * (c1 - 24)));
    }

    @Nonnull
    private static Point decode6(final int v, final int width, final int height) {
        int d = 6;
        int col = v / (height * 6);
        final int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            d = width - (maxcol * 6);
        }
        final int w = v - (col * height * 6);
        return Point.fromMicroDeg(height - 1 - (w / d), (col * 6) + (w % d));
    }

    // / lowest level encode/decode routines
    private static int fastDecode(final String code)
    // decode up to dot or EOS;
    // returns negative in case of error
    {
        int value = 0;
        int i;
        for (i = 0; i < code.length(); i++) {
            final int c = (int) code.charAt(i);
            if (c == 46) // dot!
            {
                return value;
            }
            if (decode_chars[c] < 0) {
                return -1;
            }
            value = (value * 31) + decode_chars[c];
        }
        return value;
    }

    @Nonnull
    private static Point add2res(final int y, final int x, final int dividerx4, final int dividery, final int ydirection, final String extrapostfix) {
        if (!extrapostfix.isEmpty()) {
            int c1 = (int) extrapostfix.charAt(0);
            c1 = decode_chars[c1];
            if (c1 < 0) {
                c1 = 0;
            } else if (c1 > 29) {
                c1 = 29;
            }
            final int y1 = c1 / 5;
            final int x1 = c1 % 5;
            int c2 = (extrapostfix.length() == 2) ? (int) extrapostfix.charAt(1) : 72; // 72='H'=code
            // 15=(3+2*6)
            c2 = decode_chars[c2];
            if (c2 < 0) {
                c2 = 0;
            } else if (c2 > 29) {
                c2 = 29;
            }
            final int y2 = c2 / 6;
            final int x2 = c2 % 6;

            final int extrax = ((((x1 * 12) + (2 * x2) + 1) * dividerx4) + 120) / 240;
            final int extray = ((((y1 * 10) + (2 * y2) + 1) * dividery) + 30) / 60;

            return Point.fromMicroDeg(y + (extray * ydirection), x + extrax);
        }
        return Point.fromMicroDeg(y + ((dividery / 2) * ydirection), x + (dividerx4 / 8));
    }
}
