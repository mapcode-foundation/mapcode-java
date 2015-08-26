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

class Decoder {
    private static final Logger LOG = LoggerFactory.getLogger(Decoder.class);

    private static final char GREEK_CAPITAL_ALPHA = 'Α';

    private Decoder() {
        // Prevent instantiation.
    }

    // ----------------------------------------------------------------------
    // Method called from public Java API.
    // ----------------------------------------------------------------------

    @SuppressWarnings("ConstantConditions")
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

        final int ccode = territory.getNumber();

        final int from = DataAccess.dataFirstRecord(ccode);
        if (DataAccess.dataFlags(from) == 0) { // no data for this territory?
            return Point.undefined(); // this territory is not in the current data
        }
        final int upto = DataAccess.dataLastRecord(ccode);

        final int incodexhi = mapcode.indexOf('.');
        final int incodex = (incodexhi * 10) + (incodexlen - incodexhi);

        for (int i = from; i <= upto; i++) {
            final int codexi = Data.calcCodex(i);
            if (Data.recType(i) == 0) {
                if (Data.isNameless(i)) {
                    // i = nameless
                    if (((codexi == 21) && (incodex == 22)) ||
                        ((codexi == 22) && (incodex == 32)) || 
                        ((codexi == 13) && (incodex == 23))) {
                            result = decodeNameless(mapcode, i, extrapostfix);
                            break;
                    }
                } else {
                    // i = grid without headerletter                    
                    if ((codexi == incodex) || ((incodex == 22) && (codexi == 21))) {
                        result = decodeGrid(mapcode, 
                                Data.getBoundaries(i).getMinX(), Data.getBoundaries(i).getMinY(), 
                                Data.getBoundaries(i).getMaxX(), Data.getBoundaries(i).getMaxY(),
                                i, extrapostfix);
                
                        if (Data.isRestricted(i) && result.isDefined()) {
                            boolean fitssomewhere = false;
                            int j;
                            for (j = upto - 1; j >= from; j--) {
                                if (!Data.isRestricted(j)) {
                                  final int xdiv8 = Common.xDivider(Data.getBoundaries(j).getMinY(),
                                          Data.getBoundaries(j).getMaxY()) / 4;
                                  if (Data.getBoundaries(j).extendBounds(xdiv8, 60).containsPoint(result)) {
                                      fitssomewhere = true;
                                      break;
                                  }
                                }
                            }
                            if (!fitssomewhere) {
                                result.setUndefined();
                            }
                        }
                        break;
                    }
                }
            } else if (Data.recType(i) == 1) {
                // i = grid with headerletter
                if ((incodex == codexi + 10) && (Data.headerLetter(i).charAt(0) == mapcode.charAt(0))) {
                        result = decodeGrid(mapcode.substring(1), 
                            Data.getBoundaries(i).getMinX(), Data.getBoundaries(i).getMinY(), 
                            Data.getBoundaries(i).getMaxX(), Data.getBoundaries(i).getMaxY(), 
                            i, extrapostfix);
                    break;
                }
            }
            else {
                // i = autoheader
                if (((incodex == 23) && (codexi == 22)) || ((incodex == 33) && (codexi == 23))) {
                    result = decodeAutoHeader(mapcode, i, extrapostfix);
                    break;
                }
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
                final SubArea mapcoderRect = Data.getBoundaries(upto); // find
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
        return result.wrap();
    }

    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------

    private static final int CCODE_EARTH = 540;

    private final static int[] DECODE_CHARS = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,
            -1, -2, 10, 11, 12, -3, 13, 14, 15, 1, 16, 17, 18, 19, 20, 0,
            21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1, -1,
            -1, -2, 10, 11, 12, -3, 13, 14, 15, 1, 16, 17, 18, 19, 20, 0,
            21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static class Unicode2Ascii {

        public final char min;
        public final char max;
        @Nonnull
        public final String convert;

        public Unicode2Ascii(final char min, final char max, @Nonnull final String convert) {
            this.min = min;
            this.max = max;
            this.convert = convert;
        }
    }

    // Special character '?' indicating missing character in alphabet.
    private static final char MISSCODE = '?';

    private final static char[][] ASCII2LANGUAGE = {
            // Character:   A       B       C       D       E       F       G       H       I       J      K        L       M       N       O       P       Q       R       S       T       U       V       W       X       Y       Z       0       1       2       3       4       5       6       7       8       9
            /* Roman    */ {'\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048', '\u0049', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059', '\u005a', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Roman
            /* Greek    */ {'\u0391', '\u0392', '\u039e', '\u0394', MISSCODE, '\u0395', '\u0393', '\u0397', '\u0399', '\u03a0', '\u039a', '\u039b', '\u039c', '\u039d', '\u039f', '\u03a1', '\u0398', '\u03a8', '\u03a3', '\u03a4', MISSCODE, '\u03a6', '\u03a9', '\u03a7', '\u03a5', '\u0396', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Greek
            /* Cyrillic */ {'\u0410', '\u0412', '\u0421', '\u0414', '\u0415', '\u0416', '\u0413', '\u041d', '\u0418', '\u041f', '\u041a', '\u041b', '\u041c', '\u0417', '\u041e', '\u0420', '\u0424', '\u042f', '\u0426', '\u0422', '\u042d', '\u0427', '\u0428', '\u0425', '\u0423', '\u0411', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Cyrillic
            /* Hebrew   */ {'\u05d0', '\u05d1', '\u05d2', '\u05d3', '\u05e3', '\u05d4', '\u05d6', '\u05d7', '\u05d5', '\u05d8', '\u05d9', '\u05da', '\u05db', '\u05dc', '\u05e1', '\u05dd', '\u05de', '\u05e0', '\u05e2', '\u05e4', '\u05e5', '\u05e6', '\u05e7', '\u05e8', '\u05e9', '\u05ea', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Hebrew
            /* Hindi    */ {'\u0905', '\u0915', '\u0917', '\u0918', '\u090f', '\u091a', '\u091c', '\u091f', MISSCODE, '\u0920', '\u0923', '\u0924', '\u0926', '\u0927', MISSCODE, '\u0928', '\u092a', '\u092d', '\u092e', '\u0930', '\u092b', '\u0932', '\u0935', '\u0938', '\u0939', '\u0921', '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e', '\u096f'}, // Hindi
            /* Malay    */ {'\u0d12', '\u0d15', '\u0d16', '\u0d17', '\u0d0b', '\u0d1a', '\u0d1c', '\u0d1f', '\u0d07', '\u0d21', '\u0d24', '\u0d25', '\u0d26', '\u0d27', '\u0d20', '\u0d28', '\u0d2e', '\u0d30', '\u0d31', '\u0d32', '\u0d09', '\u0d34', '\u0d35', '\u0d36', '\u0d38', '\u0d39', '\u0d66', '\u0d67', '\u0d68', '\u0d69', '\u0d6a', '\u0d6b', '\u0d6c', '\u0d6d', '\u0d6e', '\u0d6f'}, // Malay
            /* Georgian */ {'\u10a0', '\u10a1', '\u10a3', '\u10a6', '\u10a4', '\u10a9', '\u10ab', '\u10ac', '\u10b3', '\u10ae', '\u10b0', '\u10b1', '\u10b2', '\u10b4', '\u10ad', '\u10b5', '\u10b6', '\u10b7', '\u10b8', '\u10b9', '\u10a8', '\u10ba', '\u10bb', '\u10bd', '\u10be', '\u10bf', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Georgian
            /* Katakana */ {'\u30a2', '\u30ab', '\u30ad', '\u30af', '\u30aa', '\u30b1', '\u30b3', '\u30b5', '\u30a4', '\u30b9', '\u30c1', '\u30c8', '\u30ca', '\u30cc', '\u30a6', '\u30d2', '\u30d5', '\u30d8', '\u30db', '\u30e1', '\u30a8', '\u30e2', '\u30e8', '\u30e9', '\u30ed', '\u30f2', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Katakana
            /* Thai     */ {'\u0e30', '\u0e01', '\u0e02', '\u0e04', '\u0e32', '\u0e07', '\u0e08', '\u0e09', '\u0e31', '\u0e0a', '\u0e11', '\u0e14', '\u0e16', '\u0e17', '\u0e0d', '\u0e18', '\u0e1a', '\u0e1c', '\u0e21', '\u0e23', '\u0e2c', '\u0e25', '\u0e27', '\u0e2d', '\u0e2e', '\u0e2f', '\u0e50', '\u0e51', '\u0e52', '\u0e53', '\u0e54', '\u0e55', '\u0e56', '\u0e57', '\u0e58', '\u0e59'}, // Thai
            /* Laos     */ {'\u0eb0', '\u0e81', '\u0e82', '\u0e84', '\u0ec3', '\u0e87', '\u0e88', '\u0e8a', '\u0ec4', '\u0e8d', '\u0e94', '\u0e97', '\u0e99', '\u0e9a', '\u0ec6', '\u0e9c', '\u0e9e', '\u0ea1', '\u0ea2', '\u0ea3', '\u0ebd', '\u0ea7', '\u0eaa', '\u0eab', '\u0ead', '\u0eaf', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Laos
            /* Armenian */ {'\u0556', '\u0532', '\u0533', '\u0534', '\u0535', '\u0538', '\u0539', '\u053a', '\u053b', '\u053d', '\u053f', '\u0540', '\u0541', '\u0543', '\u0555', '\u0547', '\u0548', '\u054a', '\u054d', '\u054e', '\u0545', '\u054f', '\u0550', '\u0551', '\u0552', '\u0553', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Armenian
            /* Bengali  */ {'\u0985', '\u098c', '\u0995', '\u0996', '\u098f', '\u0997', '\u0999', '\u099a', MISSCODE, '\u099d', '\u09a0', '\u09a1', '\u09a2', '\u09a3', MISSCODE, '\u09a4', '\u09a5', '\u09a6', '\u09a8', '\u09aa', '\u0993', '\u09ac', '\u09ad', '\u09af', '\u09b2', '\u09b9', '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee', '\u09ef'}, // Bengali
            /* Gurmukhi */ {'\u0a05', '\u0a15', '\u0a17', '\u0a18', '\u0a0f', '\u0a1a', '\u0a1c', '\u0a1f', MISSCODE, '\u0a20', '\u0a23', '\u0a24', '\u0a26', '\u0a27', MISSCODE, '\u0a28', '\u0a2a', '\u0a2d', '\u0a2e', '\u0a30', '\u0a2b', '\u0a32', '\u0a35', '\u0a38', '\u0a39', '\u0a21', '\u0a66', '\u0a67', '\u0a68', '\u0a69', '\u0a6a', '\u0a6b', '\u0a6c', '\u0a6d', '\u0a6e', '\u0a6f'}, // Gurmukhi
            /* Tibetan  */ {'\u0f58', '\u0f40', '\u0f41', '\u0f42', '\u0f64', '\u0f44', '\u0f45', '\u0f46', MISSCODE, '\u0f47', '\u0f4a', '\u0f4c', '\u0f4e', '\u0f4f', MISSCODE, '\u0f51', '\u0f53', '\u0f54', '\u0f56', '\u0f5e', '\u0f65', '\u0f5f', '\u0f61', '\u0f62', '\u0f63', '\u0f66', '\u0f20', '\u0f21', '\u0f22', '\u0f23', '\u0f24', '\u0f25', '\u0f26', '\u0f27', '\u0f28', '\u0f29'}, // Tibetan
    };

    private final static Unicode2Ascii[] UNICODE2ASCII = {
            /* Roman    */ new Unicode2Ascii('\u0041', '\u005a', "ABCDEFGHIJKLMNOPQRSTUVWXYZ"),                                                        // Roman
            /* Greek    */ new Unicode2Ascii('\u0391', '\u03a9', "ABGDFZHQIKLMNCOJP?STYVXRW"),                                                         // Greek
            /* Cyrillic */ new Unicode2Ascii('\u0410', '\u042f', "AZBGDEFNI?KLMHOJPCTYQXSVW????U?R"),                                                  // Cyrillic
            /* Hebrew   */ new Unicode2Ascii('\u05d0', '\u05ea', "ABCDFIGHJKLMNPQ?ROSETUVWXYZ"),                                                       // Hebrew
            /* Hindi    */ new Unicode2Ascii('\u0905', '\u0939', "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QU?RS?T?V??W??XY"),                             // Hindi
            /* Malay    */ new Unicode2Ascii('\u0d07', '\u0d39', "I?U?E??????A??BCD??F?G??HOJ??KLMNP?????Q?RST?VWX?YZ"),                               // Malai
            /* Georgian */ new Unicode2Ascii('\u10a0', '\u10bf', "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"),                                                  // Georgian
            /* Katakana */ new Unicode2Ascii('\u30a2', '\u30f2', "A?I?O?U?EB?C?D?F?G?H???J???????K??????L?M?N?????P??Q??R??S?????TV?????WX???Y????Z"), // Katakana
            /* Thai     */ new Unicode2Ascii('\u0e01', '\u0e32', "BC?D??FGHJ??O???K??L?MNP?Q?R????S?T?V?W????UXYZAIE"),                                // Thai
            /* Laos     */ new Unicode2Ascii('\u0e81', '\u0ec6', "BC?D??FG?H??J??????K??L?MN?P?Q??RST???V??WX?Y?ZA????????????U?????EI?O"),            // Lao
            /* Armenian */ new Unicode2Ascii('\u0532', '\u0556', "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA"),                                             // Armenian
            /* Bengali  */ new Unicode2Ascii('\u0985', '\u09b9', "A??????B??E???U?CDF?GH??J??KLMNPQR?S?T?VW?X??Y??????Z"),                             // Bengali
            /* Gurmukhi */ new Unicode2Ascii('\u0a05', '\u0a39', "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QU?RS?T?V??W??XY"),                             // Gurmukhi
            /* Tibetan  */ new Unicode2Ascii('\u0f40', '\u0f66', "BCD?FGHJ??K?L?MN?P?QR?S?A?????TV?WXYEUZ"),                                           // Tibetan

            /* Hindi    */ new Unicode2Ascii('\u0966', '\u096f', ""),
            /* Malai    */ new Unicode2Ascii('\u0d66', '\u0d6f', ""),
            /* Thai     */ new Unicode2Ascii('\u0e50', '\u0e59', ""),
            /* Bengali  */ new Unicode2Ascii('\u09e6', '\u09ef', ""),
            /* Gurmukhi */ new Unicode2Ascii('\u0a66', '\u0a6f', ""),
            /* Tibetan  */ new Unicode2Ascii('\u0f20', '\u0f29', ""),

            // Lowercase variants:
            /* Greek    */ new Unicode2Ascii('\u03B1', '\u03c9', "ABGDFZHQIKLMNCOJP?STYVXRW"),
            /* Georgian */ new Unicode2Ascii('\u10d0', '\u10ef', "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"),
            /* Armenian */ new Unicode2Ascii('\u0562', '\u0586', "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA")
    };

    @Nonnull
    private static Point decodeGrid(final String str, final int minx, final int miny, final int maxx, final int maxy,
                                    final int m, final String extrapostfix) {
        // for a well-formed result, and integer variables
        String result = str;
        int relx;
        int rely;
        final int codexlen = result.length() - 1; // length ex dot
        int prelen = result.indexOf('.'); // dotposition

        if ((prelen == 1) && (codexlen == 5)) {
            prelen++;
            result = result.substring(0, 1) + result.charAt(2) + '.' + result.substring(3);
        }
        final int postlen = codexlen - prelen;

        final int divx;
        int divy;
        divy = DataAccess.smartDiv(m);
        if (divy == 1) {
            divx = Common.xSide[prelen];
            divy = Common.ySide[prelen];
        } else {
            divx = Common.nc[prelen] / divy;
        }

        if ((prelen == 4) && (divx == 961) && (divy == 961)) {
            result = result.substring(0, 1) + result.charAt(2) + result.charAt(1) + result.substring(3);
        }

        int v = decodeBase31(result);

        if ((divx != divy) && (prelen > 2)) // D==6
        {
            final Point d = decodeSixWide(v, divx, divy);
            relx = d.getLonMicroDeg();
            rely = d.getLatMicroDeg();
        } else {
            relx = v / divy;
            rely = divy - 1 - (v % divy);
        }

        final int ygridsize = (((maxy - miny) + divy) - 1) / divy;
        final int xgridsize = (((maxx - minx) + divx) - 1) / divx;

        rely = miny + (rely * ygridsize);
        relx = minx + (relx * xgridsize);

        final int yp = Common.ySide[postlen];
        final int dividery = (ygridsize + yp - 1) / yp;
        final int xp = Common.xSide[postlen];
        final int dividerx = (xgridsize + xp - 1) / xp;

        String rest = result.substring(prelen + 1);

        // decoderelative (postfix vs rely,relx)
        final int difx;
        int dify;

        if (postlen == 3) {
            final Point d = decodeTriple(rest);
            difx = d.getLonMicroDeg();
            dify = d.getLatMicroDeg();
        } else {
            if (postlen == 4) {
                rest = String.valueOf(rest.charAt(0)) + rest.charAt(2) + rest.charAt(1) + rest.charAt(3);
            }
            v = decodeBase31(rest);
            difx = v / yp;
            dify = v % yp;
        }

        dify = yp - 1 - dify;

        final int cornery = rely + (dify * dividery);
        final int cornerx = relx + (difx * dividerx);

        return decodeExtension(cornery, cornerx, dividerx << 2, dividery, 1, extrapostfix);
    }

    @Nonnull
    private static Point decodeNameless(final String str, final int firstrec, final String extrapostfix) {
        String result = str;
        final int codexm = Data.calcCodex(firstrec);
        if (codexm == 22) {
            result = result.substring(0, 3) + result.substring(4);
        } else {
            result = result.substring(0, 2) + result.substring(3);
        }

        int a = Common.countCityCoordinatesForCountry(codexm, firstrec, firstrec);

        final int p = 31 / a;
        final int r = 31 % a;
        int v = 0;
        int nrX;
        boolean swapletters = false;

        if ((codexm != 21) && (a <= 31)) {
            final int offset = DECODE_CHARS[(int) result.charAt(0)];

            if (offset < (r * (p + 1))) {
                nrX = offset / (p + 1);
            } else {
                swapletters = (p == 1) && (codexm == 22);
                nrX = r + ((offset - (r * (p + 1))) / p);
            }
        } else if ((codexm != 21) && (a < 62)) {
            nrX = DECODE_CHARS[(int) result.charAt(0)];
            if (nrX < (62 - a)) {
                swapletters = codexm == 22;
            } else {
                nrX = ((nrX + nrX) - 62) + a;
            }
        } else {
            // codex==21 || A>=62
            final int basePower = (codexm == 21) ? (961 * 961) : (961 * 961 * 31);
            int basePowerA = basePower / a;
            if (a == 62) {
                basePowerA++;
            } else {
                basePowerA = 961 * (basePowerA / 961);
            }

            // decode and determine x
            v = decodeBase31(result);
            nrX = v / basePowerA;
            v %= basePowerA;
        }

        if (swapletters && !Data.isSpecialShape(firstrec + nrX)) {
            result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
        }

        if ((codexm != 21) && (a <= 31)) {
            v = decodeBase31(result);
            if (nrX > 0) {
                v -= ((nrX * p) + ((nrX < r) ? nrX : r)) * 961 * 961;
            }
        } else if ((codexm != 21) && (a < 62)) {
            v = decodeBase31(result.substring(1));
            if ((nrX >= (62 - a)) && (v >= (16 * 961 * 31))) {
                v -= 16 * 961 * 31;
                nrX++;
            }
        }

        final int m = firstrec + nrX;

        int side = DataAccess.smartDiv(m);
        int xSIDE = side;

        final int maxy = Data.getBoundaries(m).getMaxY();
        final int minx = Data.getBoundaries(m).getMinX();
        final int miny = Data.getBoundaries(m).getMinY();

        final int dx;
        final int dy;

        if (Data.isSpecialShape(m)) {
            xSIDE *= side;
            side = 1 + ((maxy - miny) / 90);
            xSIDE = xSIDE / side;

            final Point d = decodeSixWide(v, xSIDE, side);
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

        final int cornerx = minx + ((dx * dividerx4) / 4);
        final int cornery = maxy - (dy * dividery);
        return decodeExtension(cornery, cornerx, dividerx4, dividery, -1, extrapostfix);
    }

    @Nonnull
    private static Point decodeAutoHeader(final String input, final int m, final String extrapostfix) {
        // returns Point.isUndefined() in case or error
        int storageStart = 0;
        final int codexm = Data.calcCodex(m);

        int value = decodeBase31(input); // decode top (before dot)
        value *= 961 * 31;
        final Point triple = decodeTriple(input.substring(input.length() - 3));
        // decode bottom 3 chars

        int i;
        i = m;
        while (true) {
            if ((Data.recType(i)<2) || (Data.calcCodex(i) != codexm)) {
                return Point.undefined(); // return undefined
            }

            final int maxx = Data.getBoundaries(i).getMaxX();
            final int maxy = Data.getBoundaries(i).getMaxY();
            final int minx = Data.getBoundaries(i).getMinX();
            final int miny = Data.getBoundaries(i).getMinY();

            int h = ((maxy - miny) + 89) / 90;
            final int xdiv = Common.xDivider(miny, maxy);
            int w = ((((maxx - minx) * 4) + xdiv) - 1) / xdiv;

            h = 176 * (((h + 176) - 1) / 176);
            w = 168 * (((w + 168) - 1) / 168);

            int product = (w / 168) * (h / 176) * 961 * 31;

            if (Data.recType(i) == 2) {
                final int goodRounder = (codexm >= 23) ? (961 * 961 * 31) : (961 * 961);
                product = ((((storageStart + product + goodRounder) - 1) / goodRounder) * goodRounder) - storageStart;
            }

            if ((value >= storageStart) && (value < (storageStart + product))) {
                // code belongs here?
                final int dividerx = (((maxx - minx) + w) - 1) / w;
                final int dividery = (((maxy - miny) + h) - 1) / h;

                value -= storageStart;
                value = value / (961 * 31);
                
                int vx = value / (h / 176);
                vx = (vx * 168) + triple.getLonMicroDeg();
                final int vy = ((value % (h / 176)) * 176) + triple.getLatMicroDeg();

                final int cornery = maxy - (vy * dividery);
                final int cornerx = minx + (vx * dividerx);

                if (cornerx < minx || cornerx >= maxx || cornery < miny || cornery > maxy) {
                    return Point.undefined(); // corner out of bounds
                }

                return decodeExtension(cornery, cornerx, dividerx << 2, dividery, -1, extrapostfix);
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
            final String s = String.valueOf(1000 + v1 + (32 * v2));
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
     * @param mapcode Unicode string.
     * @return ASCII string.
     */
    static String decodeUTF16(final String mapcode) {
        String result;
        final StringBuilder asciiBuf = new StringBuilder();
        for (final char ch : mapcode.toCharArray()) {
            if (ch == '.') {
                asciiBuf.append(ch);
            } else if ((ch >= 1) && (ch <= 'z')) {
                // normal ascii
                asciiBuf.append(ch);
            } else {
                boolean found = false;
                for (final Unicode2Ascii unicode2Ascii : UNICODE2ASCII) {
                    if ((ch >= unicode2Ascii.min) && (ch <= unicode2Ascii.max)) {
                        final int pos = ((int) ch) - (int) unicode2Ascii.min;
                        asciiBuf.append(unicode2Ascii.convert.charAt(pos));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    asciiBuf.append('?');
                    break;
                }
            }
        }
        result = asciiBuf.toString();

        // Repack if this was a Greek 'alpha' code. This will have been converted to a regular 'A' after one iteration.
        if (mapcode.startsWith(String.valueOf(GREEK_CAPITAL_ALPHA))) {
            final String unpacked = aeuUnpack(result);
            if (unpacked.isEmpty()) {
                throw new AssertionError("decodeUTF16: cannot decode " + mapcode);
            }
            result = Encoder.aeuPack(unpacked, false);
        }
        return result;
    }

    static String encodeUTF16(final String mapcode, final int alphabetCode) throws IllegalArgumentException {
        final String mapcodeToEncode;
        if (ASCII2LANGUAGE[alphabetCode][4] == MISSCODE) {

            // Alphabet does not contain 'E' (Greek).
            if (mapcode.matches("^.*[EU].*")) {
                final String unpacked = aeuUnpack(mapcode);
                if (unpacked.isEmpty()) {
                    throw new IllegalArgumentException("encodeToAlphabetCode: cannot encode '" + mapcode +
                            "' to alphabet " + alphabetCode);
                }
                mapcodeToEncode = Encoder.aeuPack(unpacked, true);
            } else {
                mapcodeToEncode = mapcode;
            }
        } else {
            mapcodeToEncode = mapcode;
        }
        final StringBuilder sb = new StringBuilder();
        for (char ch : mapcodeToEncode.toCharArray()) {
            ch = Character.toUpperCase(ch);
            if (ch > 'Z') {
                // Not in any valid range?
                sb.append('?');
            } else if (ch < 'A') {
                // Valid but not a letter (e.g. a dot, a space...). Leave untranslated.
                sb.append(ch);
            } else {
                sb.append(ASCII2LANGUAGE[alphabetCode][(int) ch - (int) 'A']);
            }
        }
        return sb.toString();
    }

    @Nonnull
    private static Point decodeTriple(final String str) {
        final int c1 = DECODE_CHARS[(int) str.charAt(0)];
        final int x = decodeBase31(str.substring(1));
        if (c1 < 24) {
            return Point.fromMicroDeg(((c1 / 6) * 34) + (x % 34), ((c1 % 6) * 28) + (x / 34));
        }
        return Point.fromMicroDeg((x % 40) + 136, (x / 40) + (24 * (c1 - 24)));
    }

    @Nonnull
    private static Point decodeSixWide(final int v, final int width, final int height) {
        int d;
        int col = v / (height * 6);
        final int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            d = width - (maxcol * 6);
        }
        else {
            d = 6;
        }
        final int w = v - (col * height * 6);
        return Point.fromMicroDeg(height - 1 - (w / d), (col * 6) + (w % d));
    }

    // / lowest level encode/decode routines
    // decode up to dot or EOS;
    // returns negative in case of error
    private static int decodeBase31(final String code) {
        int value = 0;
        for (final char c : code.toCharArray()) {
            if (c == '.') {
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
    private static Point decodeExtension(final int y, final int x, final int dividerx4, final int dividery, final int ydirection, final String extrapostfix) {
        if (!extrapostfix.isEmpty()) {
            int c1 = (int) extrapostfix.charAt(0);
            c1 = DECODE_CHARS[c1];
            if (c1 < 0 || c1 == 30) {
                return Point.undefined();
            }
            final int y1 = c1 / 5;
            final int x1 = c1 % 5;
            int c2 = (extrapostfix.length() == 2) ? (int) extrapostfix.charAt(1) : 72;
            c2 = DECODE_CHARS[c2];
            if (c2 < 0 || c2 == 30) {
                return Point.undefined();
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
