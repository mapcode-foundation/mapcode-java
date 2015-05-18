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

    private final static int[] DECODE_CHARS = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
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

    private final static int[][] ASCII2LANGUAGE = {
            {0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f, 0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // roman
            {0x0391, 0x0392, 0x039e, 0x0394, 0x003f, 0x0395, 0x0393, 0x0397, 0x0399, 0x03a0, 0x039a, 0x039b, 0x039c, 0x039d, 0x039f, 0x03a1, 0x0398, 0x03a8, 0x03a3, 0x03a4, 0x003f, 0x03a6, 0x03a9, 0x03a7, 0x03a5, 0x0396, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // greek
            {0x0410, 0x0412, 0x0421, 0x0414, 0x0415, 0x0416, 0x0413, 0x041d, 0x0418, 0x041f, 0x041a, 0x041b, 0x041c, 0x0417, 0x041e, 0x0420, 0x0424, 0x042f, 0x0426, 0x0422, 0x042d, 0x0427, 0x0428, 0x0425, 0x0423, 0x0411, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // cyrillic
            {0x05d0, 0x05d1, 0x05d2, 0x05d3, 0x05e3, 0x05d4, 0x05d6, 0x05d7, 0x05d5, 0x05d8, 0x05d9, 0x05da, 0x05db, 0x05dc, 0x05e1, 0x05dd, 0x05de, 0x05e0, 0x05e2, 0x05e4, 0x05e5, 0x05e6, 0x05e7, 0x05e8, 0x05e9, 0x05ea, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // hebrew
            {0x0905, 0x0915, 0x0917, 0x0918, 0x090f, 0x091a, 0x091c, 0x091f, 0x003f, 0x0920, 0x0923, 0x0924, 0x0926, 0x0927, 0x003f, 0x0928, 0x092a, 0x092d, 0x092e, 0x0930, 0x092b, 0x0932, 0x0935, 0x0938, 0x0939, 0x0921, 0x0966, 0x0967, 0x0968, 0x0969, 0x096a, 0x096b, 0x096c, 0x096d, 0x096e, 0x096f}, // hindi
            {0x0d12, 0x0d15, 0x0d16, 0x0d17, 0x0d0b, 0x0d1a, 0x0d1c, 0x0d1f, 0x0d07, 0x0d21, 0x0d24, 0x0d25, 0x0d26, 0x0d27, 0x0d20, 0x0d28, 0x0d2e, 0x0d30, 0x0d31, 0x0d32, 0x0d09, 0x0d34, 0x0d35, 0x0d36, 0x0d38, 0x0d39, 0x0d66, 0x0d67, 0x0d68, 0x0d69, 0x0d6a, 0x0d6b, 0x0d6c, 0x0d6d, 0x0d6e, 0x0d6f}, // malay
            {0x10a0, 0x10a1, 0x10a3, 0x10a6, 0x10a4, 0x10a9, 0x10ab, 0x10ac, 0x10b3, 0x10ae, 0x10b0, 0x10b1, 0x10b2, 0x10b4, 0x10ad, 0x10b5, 0x10b6, 0x10b7, 0x10b8, 0x10b9, 0x10a8, 0x10ba, 0x10bb, 0x10bd, 0x10be, 0x10bf, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // Georgian
            {0x30a2, 0x30ab, 0x30ad, 0x30af, 0x30aa, 0x30b1, 0x30b3, 0x30b5, 0x30a4, 0x30b9, 0x30c1, 0x30c8, 0x30ca, 0x30cc, 0x30a6, 0x30d2, 0x30d5, 0x30d8, 0x30db, 0x30e1, 0x30a8, 0x30e2, 0x30e8, 0x30e9, 0x30ed, 0x30f2, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // Katakana
            {0x0e30, 0x0e01, 0x0e02, 0x0e04, 0x0e32, 0x0e07, 0x0e08, 0x0e09, 0x0e31, 0x0e0a, 0x0e11, 0x0e14, 0x0e16, 0x0e17, 0x0e0d, 0x0e18, 0x0e1a, 0x0e1c, 0x0e21, 0x0e23, 0x0e2c, 0x0e25, 0x0e27, 0x0e2d, 0x0e2e, 0x0e2f, 0x0e50, 0x0e51, 0x0e52, 0x0e53, 0x0e54, 0x0e55, 0x0e56, 0x0e57, 0x0e58, 0x0e59}, // Thai
            {0x0eb0, 0x0e81, 0x0e82, 0x0e84, 0x0ec3, 0x0e87, 0x0e88, 0x0e8a, 0x0ec4, 0x0e8d, 0x0e94, 0x0e97, 0x0e99, 0x0e9a, 0x0ec6, 0x0e9c, 0x0e9e, 0x0ea1, 0x0ea2, 0x0ea3, 0x0ebd, 0x0ea7, 0x0eaa, 0x0eab, 0x0ead, 0x0eaf, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // Laos
            {0x0556, 0x0532, 0x0533, 0x0534, 0x0535, 0x0538, 0x0539, 0x053a, 0x053b, 0x053d, 0x053f, 0x0540, 0x0541, 0x0543, 0x0555, 0x0547, 0x0548, 0x054a, 0x054d, 0x054e, 0x0545, 0x054f, 0x0550, 0x0551, 0x0552, 0x0553, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039}, // armenian
            {0x0985, 0x098c, 0x0995, 0x0996, 0x098f, 0x0997, 0x0999, 0x099a, 0x003f, 0x099d, 0x09a0, 0x09a1, 0x09a2, 0x09a3, 0x003f, 0x09a4, 0x09a5, 0x09a6, 0x09a8, 0x09aa, 0x0993, 0x09ac, 0x09ad, 0x09af, 0x09b2, 0x09b9, 0x09e6, 0x09e7, 0x09e8, 0x09e9, 0x09ea, 0x09eb, 0x09ec, 0x09ed, 0x09ee, 0x09ef}, // Bengali
            {0x0a05, 0x0a15, 0x0a17, 0x0a18, 0x0a0f, 0x0a1a, 0x0a1c, 0x0a1f, 0x003f, 0x0a20, 0x0a23, 0x0a24, 0x0a26, 0x0a27, 0x003f, 0x0a28, 0x0a2a, 0x0a2d, 0x0a2e, 0x0a30, 0x0a2b, 0x0a32, 0x0a35, 0x0a38, 0x0a39, 0x0a21, 0x0a66, 0x0a67, 0x0a68, 0x0a69, 0x0a6a, 0x0a6b, 0x0a6c, 0x0a6d, 0x0a6e, 0x0a6f}, // Gurmukhi
            {0x0f58, 0x0f40, 0x0f41, 0x0f42, 0x0f64, 0x0f44, 0x0f45, 0x0f46, 0x003f, 0x0f47, 0x0f4a, 0x0f4c, 0x0f4e, 0x0f4f, 0x003f, 0x0f51, 0x0f53, 0x0f54, 0x0f56, 0x0f5e, 0x0f65, 0x0f5f, 0x0f61, 0x0f62, 0x0f63, 0x0f66, 0x0f20, 0x0f21, 0x0f22, 0x0f23, 0x0f24, 0x0f25, 0x0f26, 0x0f27, 0x0f28, 0x0f29}, // Tibetan
    };

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
            final int offset = DECODE_CHARS[(int) result.charAt(0)];

            if (offset < (r * (p + 1))) {
                nrX = offset / (p + 1);
            } else {
                swapletters = (p == 1) && (mapcoderData.getCodex() == 22);
                nrX = r + ((offset - (r * (p + 1))) / p);
            }
        } else if ((mapcoderData.getCodex() != 21) && (a < 62)) {
            nrX = DECODE_CHARS[(int) result.charAt(0)];
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
            int v1 = DECODE_CHARS[(int) str.charAt(lastpos)];
            if (v1 < 0) {
                v1 = 31;
            }
            int v2 = DECODE_CHARS[(int) str.charAt(lastpos - 1)];
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
                    final int ve = DECODE_CHARS[(int) str.charAt(lastpos)];
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
                final int i = (int) str.charAt(v);
                if (DECODE_CHARS[i] < 0) {
                    return ""; // bad char!
                } else if (voweled && (DECODE_CHARS[(int) str.charAt(v)] > 9)) {
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

    static String encodeUTF16(final String string, final int alphabet) {
        final StringBuilder sb = new StringBuilder();
        for (char ch : string.toCharArray()) {
            ch = Character.toUpperCase(ch);
            if (ch > 'Z') {
                // Not in any valid range?
                sb.append('?');
            } else if (ch < 'A') {
                // Valid but not a letter (e.g. a dot, a space...).
                // Leave untranslated.
                sb.append(ch);
            } else {
                sb.append((char) ASCII2LANGUAGE[alphabet][(int) ch - (int) 'A']);
            }
        }
        return sb.toString();
    }

    static String encodeToAlphabetCode(final String mapcode, int alphabetCode) {
        if (ASCII2LANGUAGE[alphabetCode][4] == 0x003f) {
            if (mapcode.matches("^.*[EUeu].*")) {
                final String unpacked = aeuUnpack(mapcode);
                if (unpacked.isEmpty()) {
                    throw new AssertionError("encodeToAlphabetCode: cannot encode '" + mapcode +
                            "' to alphabet " + alphabetCode + ' ' + Alphabet.fromCode(alphabetCode));
                }
                final String packed = Encoder.aeuPack(unpacked, true);
                return encodeUTF16(packed, alphabetCode);
            }
        }
        return encodeUTF16(mapcode, alphabetCode);
    }

    @Nonnull
    private static Point decodeTriple(final String str) {
        //noinspection NumericCastThatLosesPrecision
        final byte c1 = (byte) DECODE_CHARS[(int) str.charAt(0)];
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
            if (DECODE_CHARS[c] < 0) {
                return -1;
            }
            value = (value * 31) + DECODE_CHARS[c];
        }
        return value;
    }

    @Nonnull
    private static Point add2res(final int y, final int x, final int dividerx4, final int dividery, final int ydirection, final String extrapostfix) {
        if (!extrapostfix.isEmpty()) {
            int c1 = (int) extrapostfix.charAt(0);
            c1 = DECODE_CHARS[c1];
            if (c1 < 0) {
                c1 = 0;
            } else if (c1 > 29) {
                c1 = 29;
            }
            final int y1 = c1 / 5;
            final int x1 = c1 % 5;
            int c2 = (extrapostfix.length() == 2) ? (int) extrapostfix.charAt(1) : 72; // 72='H'=code
            // 15=(3+2*6)
            c2 = DECODE_CHARS[c2];
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
