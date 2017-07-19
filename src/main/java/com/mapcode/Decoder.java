/*
 * Copyright (C) 2014-2017, Stichting Mapcode Foundation (http://www.mapcode.com)
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

import static com.mapcode.Boundary.createBoundaryForTerritoryRecord;

class Decoder {
    private static final Logger LOG = LoggerFactory.getLogger(Decoder.class);

    private static final char GREEK_CAPITAL_ALPHA = '\u0391';

    private static final DataModel dataModel = DataModel.getInstance();

    private Decoder() {
        // Prevent instantiation.
    }

    // ----------------------------------------------------------------------
    // Method called from public Java API.
    // ----------------------------------------------------------------------

    @Nonnull
    static MapcodeZone decodeToMapcodeZone(@Nonnull final String argMapcode,
                                           @Nonnull final Territory argTerritory)
            throws UnknownMapcodeException {
        LOG.trace("decode: mapcode={}, territory={}", argMapcode, argTerritory.name());

        String mapcode = argMapcode;
        Territory territory = argTerritory;

        String precisionPostfix = "";
        final int positionOfDash = mapcode.indexOf('-');
        if (positionOfDash > 0) {
            precisionPostfix = decodeUTF16(mapcode.substring(positionOfDash + 1).trim());
            if (precisionPostfix.contains("Z")) {
                throw new UnknownMapcodeException("Invalid character Z, mapcode=" + argMapcode + ", territory=" + argTerritory);
            }

            // Cut the precision postfix from the mapcode.
            mapcode = mapcode.substring(0, positionOfDash);
        }
        assert !mapcode.contains("-");

        // TODO: What does AEU unpack do?
        mapcode = aeuUnpack(mapcode).trim();
        if (mapcode.isEmpty()) {
            // TODO: Is this a useful log message?
            LOG.debug("decode: Failed to aeuUnpack {}", argMapcode);
            throw new UnknownMapcodeException("Failed to AEU unpack, mapcode=" + argMapcode + ", territory=" + argTerritory);
        }

        // TODO: What does incodexlen mean?
        final int codexLen = mapcode.length() - 1;

        // *** long codes in states are handled by the country
        if (codexLen >= 9) {
            // International codes are 9 characters.
            assert codexLen == 9;
            territory = Territory.AAA;
        } else {
            final Territory parentTerritory = territory.getParentTerritory();
            if (((codexLen >= 8) && ((parentTerritory == Territory.USA) || (parentTerritory == Territory.CAN)
                    || (parentTerritory == Territory.AUS) || (parentTerritory == Territory.BRA)
                    || (parentTerritory == Territory.CHN) || (parentTerritory == Territory.RUS)))
                    || ((codexLen >= 7) &&
                    ((parentTerritory == Territory.IND) || (parentTerritory == Territory.MEX)))) {

                territory = parentTerritory;
            }
        }

        final int territoryNumber = territory.getNumber();

        final int fromTerritoryRecord = dataModel.getDataFirstRecord(territoryNumber);
        final int uptoTerritoryRecord = dataModel.getDataLastRecord(territoryNumber);

        // Determine the codex pattern as 2-digits: length-of-left-part * 10 + length-of-right-part.
        final int positionOfDot = mapcode.indexOf('.');
        final int codex = (positionOfDot * 10) + (codexLen - positionOfDot);

        MapcodeZone mapcodeZone = new MapcodeZone();
        for (int territoryRecord = fromTerritoryRecord; territoryRecord <= uptoTerritoryRecord; territoryRecord++) {
            final int codexOfTerritory = Data.getCodex(territoryRecord);
            final Boundary boundaryOfTerritory = createBoundaryForTerritoryRecord(territoryRecord);
            if (Data.getTerritoryRecordType(territoryRecord) == Data.TERRITORY_RECORD_TYPE_NONE) {

                if (Data.isNameless(territoryRecord)) {
                    // i = nameless
                    if (((codexOfTerritory == 21) && (codex == 22)) ||
                            ((codexOfTerritory == 22) && (codex == 32)) ||
                            ((codexOfTerritory == 13) && (codex == 23))) {
                        mapcodeZone = decodeNameless(mapcode, territoryRecord, precisionPostfix);
                        break;
                    }
                } else {

                    // i = grid without headerletter
                    if ((codexOfTerritory == codex) ||
                            ((codex == 22) && (codexOfTerritory == 21))) {

                        mapcodeZone = decodeGrid(mapcode,
                                boundaryOfTerritory.getLonMicroDegMin(), boundaryOfTerritory.getLatMicroDegMin(),
                                boundaryOfTerritory.getLonMicroDegMax(), boundaryOfTerritory.getLatMicroDegMax(),
                                territoryRecord, precisionPostfix);

                        // first of all, make sure the zone fits the country
                        mapcodeZone = mapcodeZone.restrictZoneTo(createBoundaryForTerritoryRecord(uptoTerritoryRecord));

                        if (Data.isRestricted(territoryRecord) && !mapcodeZone.isEmpty()) {
                            int nrZoneOverlaps = 0;
                            int j;
                            final Point result = mapcodeZone.getCenter();
                            // see if midpoint of mapcode zone is in any sub-area...
                            for (j = territoryRecord - 1; j >= fromTerritoryRecord; j--) {
                                if (!Data.isRestricted(j)) {
                                    if (createBoundaryForTerritoryRecord(j).containsPoint(result)) {
                                        nrZoneOverlaps++;
                                        break;
                                    }
                                }
                            }

                            if (nrZoneOverlaps == 0) {
                                // see if mapcode zone OVERLAPS any sub-area...
                                MapcodeZone zfound = new MapcodeZone();
                                for (j = fromTerritoryRecord; j < territoryRecord; j++) { // try all smaller rectangles j
                                    if (!Data.isRestricted(j)) {
                                        final MapcodeZone z = mapcodeZone.restrictZoneTo(createBoundaryForTerritoryRecord(j));
                                        if (!z.isEmpty()) {
                                            nrZoneOverlaps++;
                                            if (nrZoneOverlaps == 1) {
                                                // first fit! remember...
                                                zfound = new MapcodeZone(z);
                                            } else { // nrZoneOverlaps > 1
                                                // more than one hit
                                                break; // give up!
                                            }
                                        }
                                    }
                                }
                                if (nrZoneOverlaps == 1) { // intersected exactly ONE sub-area?
                                    mapcodeZone = new MapcodeZone(zfound); // use the intersection found...
                                }
                            }

                            if (nrZoneOverlaps == 0) {
                                mapcodeZone = new MapcodeZone();
                            }
                        }
                        break;
                    }
                }
            } else if (Data.getTerritoryRecordType(territoryRecord) == Data.TERRITORY_RECORD_TYPE_PIPE) {
                // i = grid with headerletter
                if ((codex == (codexOfTerritory + 10)) &&
                        (Data.headerLetter(territoryRecord).charAt(0) == mapcode.charAt(0))) {
                    mapcodeZone = decodeGrid(mapcode.substring(1),
                            boundaryOfTerritory.getLonMicroDegMin(), boundaryOfTerritory.getLatMicroDegMin(),
                            boundaryOfTerritory.getLonMicroDegMax(), boundaryOfTerritory.getLatMicroDegMax(),
                            territoryRecord, precisionPostfix);
                    break;
                }
            } else {
                assert (Data.getTerritoryRecordType(territoryRecord) == Data.TERRITORY_RECORD_TYPE_PLUS) ||
                        (Data.getTerritoryRecordType(territoryRecord) == Data.TERRITORY_RECORD_TYPE_STAR);
                // i = autoheader
                if (((codex == 23) && (codexOfTerritory == 22)) ||
                        ((codex == 33) && (codexOfTerritory == 23))) {
                    mapcodeZone = decodeAutoHeader(mapcode, territoryRecord, precisionPostfix);
                    break;
                }
            }
        }

        mapcodeZone = mapcodeZone.restrictZoneTo(createBoundaryForTerritoryRecord(uptoTerritoryRecord));
        LOG.trace("decode: zone={}", mapcodeZone);
        return mapcodeZone;
    }

    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------

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
            // Character:   A         B         C         D         E         F         G         H         I         J        K          L         M         N         O         P         Q         R         S         T         U         V         W         X         Y         Z         0         1         2         3         4         5         6         7         8         9
            /* Roman    */ {'\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048', '\u0049', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', '\u0059', '\u005a', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Roman
            /* Greek    */ {'\u0391', '\u0392', '\u039e', '\u0394', '\u0388', '\u0395', '\u0393', '\u0397', '\u0399', '\u03a0', '\u039a', '\u039b', '\u039c', '\u039d', '\u039f', '\u03a1', '\u0398', '\u03a8', '\u03a3', '\u03a4', '\u0389', '\u03a6', '\u03a9', '\u03a7', '\u03a5', '\u0396', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Greek
            /* Cyrillic */ {'\u0410', '\u0412', '\u0421', '\u0414', '\u0415', '\u0416', '\u0413', '\u041d', '\u0418', '\u041f', '\u041a', '\u041b', '\u041c', '\u0417', '\u041e', '\u0420', '\u0424', '\u042f', '\u0426', '\u0422', '\u042d', '\u0427', '\u0428', '\u0425', '\u0423', '\u0411', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Cyrillic
            /* Hebrew   */ {'\u05d0', '\u05d1', '\u05d2', '\u05d3', '\u05e3', '\u05d4', '\u05d6', '\u05d7', '\u05d5', '\u05d8', '\u05d9', '\u05da', '\u05db', '\u05dc', '\u05e1', '\u05dd', '\u05de', '\u05e0', '\u05e2', '\u05e4', '\u05e5', '\u05e6', '\u05e7', '\u05e8', '\u05e9', '\u05ea', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Hebrew
            /* Devanag. */ {'\u0905', '\u0915', '\u0917', '\u0918', '\u090f', '\u091a', '\u091c', '\u091f', MISSCODE, '\u0920', '\u0923', '\u0924', '\u0926', '\u0927', MISSCODE, '\u0928', '\u092a', '\u092d', '\u092e', '\u0930', '\u092b', '\u0932', '\u0935', '\u0938', '\u0939', '\u092c', '\u0966', '\u0967', '\u0968', '\u0969', '\u096a', '\u096b', '\u096c', '\u096d', '\u096e', '\u096f'}, // Devanagiri
            /* Malay    */ {'\u0d12', '\u0d15', '\u0d16', '\u0d17', '\u0d0b', '\u0d1a', '\u0d1c', '\u0d1f', '\u0d07', '\u0d21', '\u0d24', '\u0d25', '\u0d26', '\u0d27', '\u0d20', '\u0d28', '\u0d2e', '\u0d30', '\u0d31', '\u0d32', '\u0d09', '\u0d34', '\u0d35', '\u0d36', '\u0d38', '\u0d39', '\u0d66', '\u0d67', '\u0d68', '\u0d69', '\u0d6a', '\u0d6b', '\u0d6c', '\u0d6d', '\u0d6e', '\u0d6f'}, // Malay
            /* Georgian */ {'\u10a0', '\u10a1', '\u10a3', '\u10a6', '\u10a4', '\u10a9', '\u10ab', '\u10ac', '\u10b3', '\u10ae', '\u10b0', '\u10b1', '\u10b2', '\u10b4', '\u10ad', '\u10b5', '\u10b6', '\u10b7', '\u10b8', '\u10b9', '\u10a8', '\u10ba', '\u10bb', '\u10bd', '\u10be', '\u10bf', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Georgian
            /* Katakana */ {'\u30a2', '\u30ab', '\u30ad', '\u30af', '\u30aa', '\u30b1', '\u30b3', '\u30b5', '\u30a4', '\u30b9', '\u30c1', '\u30c8', '\u30ca', '\u30cc', '\u30a6', '\u30d2', '\u30d5', '\u30d8', '\u30db', '\u30e1', '\u30a8', '\u30e2', '\u30e8', '\u30e9', '\u30ed', '\u30f2', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Katakana
            /* Thai     */ {'\u0e30', '\u0e01', '\u0e02', '\u0e04', '\u0e32', '\u0e07', '\u0e08', '\u0e09', '\u0e31', '\u0e0a', '\u0e11', '\u0e14', '\u0e16', '\u0e17', '\u0e0d', '\u0e18', '\u0e1a', '\u0e1c', '\u0e21', '\u0e23', '\u0e2c', '\u0e25', '\u0e27', '\u0e2d', '\u0e2e', '\u0e2f', '\u0e50', '\u0e51', '\u0e52', '\u0e53', '\u0e54', '\u0e55', '\u0e56', '\u0e57', '\u0e58', '\u0e59'}, // Thai
            /* Laos     */ {'\u0eb0', '\u0e81', '\u0e82', '\u0e84', '\u0ec3', '\u0e87', '\u0e88', '\u0e8a', '\u0ec4', '\u0e8d', '\u0e94', '\u0e97', '\u0e99', '\u0e9a', '\u0ec6', '\u0e9c', '\u0e9e', '\u0ea1', '\u0ea2', '\u0ea3', '\u0ebd', '\u0ea7', '\u0eaa', '\u0eab', '\u0ead', '\u0eaf', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Laos
            /* Armenian */ {'\u0556', '\u0532', '\u0533', '\u0534', '\u0535', '\u0538', '\u0539', '\u053a', '\u053b', '\u053d', '\u053f', '\u0540', '\u0541', '\u0543', '\u0555', '\u0547', '\u0548', '\u054a', '\u054d', '\u054e', '\u0545', '\u054f', '\u0550', '\u0551', '\u0552', '\u0553', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Armenian
            /* Bengali  */ {'\u099c', '\u0998', '\u0995', '\u0996', '\u09ae', '\u0997', '\u0999', '\u099a', '\u09ab', '\u099d', '\u09a0', '\u09a1', '\u09a2', '\u09a3', '\u099e', '\u09a4', '\u09a5', '\u09a6', '\u09a8', '\u09aa', '\u099f', '\u09ac', '\u09ad', '\u09af', '\u09b2', '\u09b9', '\u09e6', '\u09e7', '\u09e8', '\u09e9', '\u09ea', '\u09eb', '\u09ec', '\u09ed', '\u09ee', '\u09ef'}, // Bengali/Assamese
            /* Gurmukhi */ {'\u0a05', '\u0a15', '\u0a17', '\u0a18', '\u0a0f', '\u0a1a', '\u0a1c', '\u0a1f', MISSCODE, '\u0a20', '\u0a23', '\u0a24', '\u0a26', '\u0a27', MISSCODE, '\u0a28', '\u0a2a', '\u0a2d', '\u0a2e', '\u0a30', '\u0a2b', '\u0a32', '\u0a35', '\u0a38', '\u0a39', '\u0a21', '\u0a66', '\u0a67', '\u0a68', '\u0a69', '\u0a6a', '\u0a6b', '\u0a6c', '\u0a6d', '\u0a6e', '\u0a6f'}, // Gurmukhi
            /* Tibetan  */ {'\u0f58', '\u0f40', '\u0f41', '\u0f42', '\u0f64', '\u0f44', '\u0f45', '\u0f46', MISSCODE, '\u0f47', '\u0f49', '\u0f55', '\u0f50', '\u0f4f', MISSCODE, '\u0f51', '\u0f53', '\u0f54', '\u0f56', '\u0f5e', '\u0f60', '\u0f5f', '\u0f61', '\u0f62', '\u0f63', '\u0f66', '\u0f20', '\u0f21', '\u0f22', '\u0f23', '\u0f24', '\u0f25', '\u0f26', '\u0f27', '\u0f28', '\u0f29'}, // Tibetan
            /* Arabic   */ {'\u0628', '\u062a', '\u062d', '\u062e', '\u062B', '\u062f', '\u0630', '\u0631', '\u0627', '\u0632', '\u0633', '\u0634', '\u0635', '\u0636', '\u0647', '\u0637', '\u0638', '\u0639', '\u063a', '\u0641', '\u0642', '\u062C', '\u0644', '\u0645', '\u0646', '\u0648', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Arabic
            /* Korean   */ {'\u1112', '\u1100', '\u1102', '\u1103', '\u1166', '\u1105', '\u1107', '\u1109', '\u1175', '\u1110', '\u1111', '\u1161', '\u1162', '\u1163', '\u110b', '\u1164', '\u1165', '\u1167', '\u1169', '\u1172', '\u1174', '\u110c', '\u110e', '\u110f', '\u116d', '\u116e', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Korean
            /* Burmese  */ {'\u1005', '\u1000', '\u1001', '\u1002', '\u1013', '\u1003', '\u1004', '\u101a', '\u101b', '\u1007', '\u100c', '\u100d', '\u100e', '\u1010', '\u101d', '\u1011', '\u1012', '\u101e', '\u1014', '\u1015', '\u1016', '\u101f', '\u1017', '\u1018', '\u100f', '\u101c', '\u1040', '\u1041', '\u1042', '\u1043', '\u1044', '\u1045', '\u1046', '\u1047', '\u1048', '\u1049'}, // Burmese
            /* Khmer    */ {'\u1789', '\u1780', '\u1781', '\u1782', '\u1785', '\u1783', '\u1784', '\u1787', '\u179a', '\u1788', '\u178a', '\u178c', '\u178d', '\u178e', '\u179c', '\u1791', '\u1792', '\u1793', '\u1794', '\u1795', '\u179f', '\u1796', '\u1798', '\u179b', '\u17a0', '\u17a2', '\u17e0', '\u17e1', '\u17e2', '\u17e3', '\u17e4', '\u17e5', '\u17e6', '\u17e7', '\u17e8', '\u17e9'}, // Khmer
            /* Sinhalese*/ {'\u0d85', '\u0d9a', '\u0d9c', '\u0d9f', '\u0d89', '\u0da2', '\u0da7', '\u0da9', '\u0dc2', '\u0dac', '\u0dad', '\u0daf', '\u0db1', '\u0db3', '\u0dc5', '\u0db4', '\u0db6', '\u0db8', '\u0db9', '\u0dba', '\u0d8b', '\u0dbb', '\u0dbd', '\u0dc0', '\u0dc3', '\u0dc4', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Sinhalese
            /* Thaana   */ {'\u0794', '\u0780', '\u0781', '\u0782', '\u0797', '\u0783', '\u0784', '\u0785', '\u07a4', '\u0786', '\u0787', '\u0788', '\u0789', '\u078a', '\u0796', '\u078b', '\u078c', '\u078d', '\u078e', '\u078f', '\u079c', '\u0790', '\u0791', '\u0792', '\u0793', '\u07b1', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Thaana
            /* Chinese  */ {'\u3123', '\u3105', '\u3108', '\u3106', '\u3114', '\u3107', '\u3109', '\u310a', '\u311e', '\u310b', '\u310c', '\u310d', '\u310e', '\u310f', '\u3120', '\u3115', '\u3116', '\u3110', '\u3111', '\u3112', '\u3113', '\u3129', '\u3117', '\u3128', '\u3118', '\u3119', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Chinese
            /* Tifinagh */ {'\u2D49', '\u2D31', '\u2D33', '\u2D37', '\u2D53', '\u2D3C', '\u2D3D', '\u2D40', '\u2D4F', '\u2D43', '\u2D44', '\u2D45', '\u2D47', '\u2D4D', '\u2D54', '\u2D4E', '\u2D55', '\u2D56', '\u2D59', '\u2D5A', '\u2D62', '\u2D5B', '\u2D5C', '\u2D5F', '\u2D61', '\u2D63', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Tifinagh (BERBER)
            /* Tamil    */ {'\u0b99', '\u0b95', '\u0b9a', '\u0b9f', '\u0b86', '\u0ba4', '\u0ba8', '\u0baa', '\u0ba9', '\u0bae', '\u0baf', '\u0bb0', '\u0bb2', '\u0bb5', '\u0b9e', '\u0bb4', '\u0bb3', '\u0bb1', '\u0b85', '\u0b88', '\u0b93', '\u0b89', '\u0b8e', '\u0b8f', '\u0b90', '\u0b92', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Tamil (digits 0xBE6-0xBEF)
            /* Amharic  */ {'\u121B', '\u1260', '\u1264', '\u12F0', '\u121E', '\u134A', '\u1308', '\u1200', '\u12A0', '\u12E8', '\u12AC', '\u1208', '\u1293', '\u1350', '\u12D0', '\u1354', '\u1240', '\u1244', '\u122C', '\u1220', '\u12C8', '\u1226', '\u1270', '\u1276', '\u1338', '\u12DC', '\u1372', '\u1369', '\u136a', '\u136b', '\u136c', '\u136d', '\u136e', '\u136f', '\u1370', '\u1371'}, // Amharic (digits 1372|1369-1371)
            /* Telugu   */ {'\u0C1E', '\u0C15', '\u0C17', '\u0C19', '\u0C2B', '\u0C1A', '\u0C1C', '\u0C1F', '\u0C1B', '\u0C20', '\u0C21', '\u0C23', '\u0C24', '\u0C25', '\u0C16', '\u0C26', '\u0C27', '\u0C28', '\u0C2A', '\u0C2C', '\u0C2D', '\u0C2E', '\u0C30', '\u0C32', '\u0C33', '\u0C35', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Telugu
            /* Odia     */ {'\u0B1D', '\u0B15', '\u0B16', '\u0B17', '\u0B23', '\u0B18', '\u0B1A', '\u0B1C', '\u0B2B', '\u0B1F', '\u0B21', '\u0B22', '\u0B24', '\u0B25', '\u0B20', '\u0B26', '\u0B27', '\u0B28', '\u0B2A', '\u0B2C', '\u0B39', '\u0B2E', '\u0B2F', '\u0B30', '\u0B33', '\u0B38', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Odia
            /* Kannada  */ {'\u0C92', '\u0C95', '\u0C96', '\u0C97', '\u0C8E', '\u0C99', '\u0C9A', '\u0C9B', '\u0C85', '\u0C9C', '\u0CA0', '\u0CA1', '\u0CA3', '\u0CA4', '\u0C89', '\u0CA6', '\u0CA7', '\u0CA8', '\u0CAA', '\u0CAB', '\u0C87', '\u0CAC', '\u0CAD', '\u0CB0', '\u0CB2', '\u0CB5', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}, // Kannada
            /* Gujarati */ {'\u0AB3', '\u0A97', '\u0A9C', '\u0AA1', '\u0A87', '\u0AA6', '\u0AAC', '\u0A95', '\u0A8F', '\u0A9A', '\u0A9F', '\u0AA4', '\u0AAA', '\u0AA0', '\u0A8D', '\u0AB0', '\u0AB5', '\u0A9E', '\u0AAE', '\u0AAB', '\u0A89', '\u0AB7', '\u0AA8', '\u0A9D', '\u0AA2', '\u0AAD', '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037', '\u0038', '\u0039'}  // Gujarati
    };

    private final static Unicode2Ascii[] UNICODE2ASCII = {
            /* Roman    */ new Unicode2Ascii('\u0041', '\u005a', "ABCDEFGHIJKLMNOPQRSTUVWXYZ"),                                                        // Roman
            /* Greek    */ new Unicode2Ascii('\u0388', '\u03a9', "EU???????ABGDFZHQIKLMNCOJP?STYVXRW"),                                                // Greek
            /* Cyrillic */ new Unicode2Ascii('\u0410', '\u042f', "AZBGDEFNI?KLMHOJPCTYQXSVW????U?R"),                                                  // Cyrillic
            /* Hebrew   */ new Unicode2Ascii('\u05d0', '\u05ea', "ABCDFIGHJKLMNPQ?ROSETUVWXYZ"),                                                       // Hebrew
            /* Devanag. */ new Unicode2Ascii('\u0905', '\u0939', "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QUZRS?T?V??W??XY"),                             // Devanagiri
            /* Malay    */ new Unicode2Ascii('\u0d07', '\u0d39', "I?U?E??????A??BCD??F?G??HOJ??KLMNP?????Q?RST?VWX?YZ"),                               // Malai
            /* Georgian */ new Unicode2Ascii('\u10a0', '\u10bf', "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"),                                                  // Georgian
            /* Katakana */ new Unicode2Ascii('\u30a2', '\u30f2', "A?I?O?U?EB?C?D?F?G?H???J???????K??????L?M?N?????P??Q??R??S?????TV?????WX???Y????Z"), // Katakana
            /* Thai     */ new Unicode2Ascii('\u0e01', '\u0e32', "BC?D??FGHJ??O???K??L?MNP?Q?R????S?T?V?W????UXYZAIE"),                                // Thai
            /* Laos     */ new Unicode2Ascii('\u0e81', '\u0ec6', "BC?D??FG?H??J??????K??L?MN?P?Q??RST???V??WX?Y?ZA????????????U?????EI?O"),            // Lao
            /* Armenian */ new Unicode2Ascii('\u0532', '\u0556', "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA"),                                             // Armenian
            /* Bengali  */ new Unicode2Ascii('\u0995', '\u09b9', "CDFBGH?AJOUKLMNPQR?S?TIVWEX??Y??????Z"),                                             // Bengali/Assamese
            /* Gurmukhi */ new Unicode2Ascii('\u0a05', '\u0a39', "A?????????E?????B?CD?F?G??HJZ?KL?MNP?QU?RS?T?V??W??XY"),                             // Gurmukhi
            /* Tibetan  */ new Unicode2Ascii('\u0f40', '\u0f66', "BCD?FGHJ?K?????NMP?QRLS?A?????TVUWXYE?Z"),                                           // Tibetan
            /* Arabic   */ new Unicode2Ascii('\u0627', '\u0648', "IA?BEVCDFGHJKLMNPQRS??????TU?WXYOZ"),                                                // Arabic

            /* Devanag. */ new Unicode2Ascii('\u0966', '\u096f', ""), // Devanagari digits
            /* Malai    */ new Unicode2Ascii('\u0d66', '\u0d6f', ""), // Malayalam digits
            /* Thai     */ new Unicode2Ascii('\u0e50', '\u0e59', ""), // Thai digits
            /* Bengali  */ new Unicode2Ascii('\u09e6', '\u09ef', ""), // Bengali digits
            /* Gurmukhi */ new Unicode2Ascii('\u0a66', '\u0a6f', ""), // Gurmukhi digits
            /* Tibetan  */ new Unicode2Ascii('\u0f20', '\u0f29', ""), // Tibetan digits
            /* Burmese  */ new Unicode2Ascii('\u1040', '\u1049', ""), // Burmese digits
            /* Khmer    */ new Unicode2Ascii('\u17e0', '\u17e9', ""), // Khmer digits
            /* Tamil    */ new Unicode2Ascii('\u0be6', '\u0bef', ""), // Tamil digits
            /* Amharic  */ new Unicode2Ascii('\u1369', '\u1372', "1234567890"), // Amharic digits [1-9][0]

            /* Korean   */ new Unicode2Ascii('\u1100', '\u1175', "B?CD?F?G?H?OV?WXJKA??????????????????????????????????????????????????????????????????????????????LMNPQER?S???YZ???T?UI"), // Korean
            /* Burmese  */ new Unicode2Ascii('\u1000', '\u101f', "BCDFGA?J????KLMYNPQESTUWX?HIZORV"),                                                  // Burmese
            /* Khmer    */ new Unicode2Ascii('\u1780', '\u17a2', "BCDFGE?HJAK?LMN??PQRSTV?W?IXO??UY?Z"),                                               // Khmer
            /* Sinhalese*/ new Unicode2Ascii('\u0d85', '\u0dc5', "A???E?U??????????????B?C??D??F????G?H??JK?L?M?NP?Q?RSTV?W??X?IYZO"),                 // Sinhalese
            /* Thaana   */ new Unicode2Ascii('\u0780', '\u07b1', "BCDFGHJKLMNPQRSTVWXYA?OE????U???????I????????????Z"),                                // Thaana
            /* Chinese  */ new Unicode2Ascii('\u3105', '\u3129', "BDFCGHJKLMNRSTUEPQWYZ????I?O??A????XV"),                                             // Chinese
            /* Tifinagh */ new Unicode2Ascii('\u2d31', '\u2d63', "B?C???D????FG??H??JKL?M?A???NPI???EOQR??STVW??X?YUZ"),                               // Tifinagh
            /* Tamil    */ new Unicode2Ascii('\u0b85', '\u0bb5', "SE?TV????WXY?ZU?B???AC???OD????F???GIH???JKLRMQPN"),                                 // Tamil
            /* Amharic  */ new Unicode2Ascii('\u1200', '\u1354', "H???????L??????????????????A??E?T?????V?????S???????????????????Q???R???????????????????????????B???C???????????W?????X????????????????????????????M????????????I???????????K???????????????????????????U???????O???????????Z???????????J???????D???????????????????????G???????????????????????????????????????????????Y?????????????????F?????N???P"), // Amharic
            /* Telugu   */ new Unicode2Ascii('\u0c15', '\u0c35', "BOC?DFIG?AHJK?LMNPQR?SETUV?W?XY?Z"), // Telugu
            /* Odia     */ new Unicode2Ascii('\u0b15', '\u0b39', "BCDF?G?HA?JOKLEMNPQR?SIT?VWX??Y????ZU"), // Odia
            /* Kannada  */ new Unicode2Ascii('\u0c85', '\u0cb5', "I?U?O????E???A??BCD?FGHJ???KL?MN?PQR?STVW??X?Y??Z"), // Kannada
            /* Gujarati */ new Unicode2Ascii('\u0a87', '\u0ab7', "E?U???O?I?????H?B??J?CXRKNDY?L?F?W?MTGZS?P??A?Q?V"), // Gujarati

            // Lowercase variants:
            /* Greek    */ new Unicode2Ascii('\u03ad', '\u03c9', "EU??ABGDFZHQIKLMNCOJP?STYVXRW"),
            /* Georgian */ new Unicode2Ascii('\u10d0', '\u10ef', "AB?CE?D?UF?GHOJ?KLMINPQRSTVW?XYZ"),
            /* Armenian */ new Unicode2Ascii('\u0562', '\u0586', "BCDE??FGHI?J?KLM?N?U?PQ?R??STVWXYZ?OA")
    };

    @Nonnull
    private static MapcodeZone decodeGrid(
            @Nonnull final String str,
            final int minx,
            final int miny,
            final int maxx,
            final int maxy,
            final int m,
            @Nonnull final String extrapostfix) {
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
        divy = dataModel.getSmartDiv(m);
        if (divy == 1) {
            divx = Common.X_SIDE[prelen];
            divy = Common.Y_SIDE[prelen];
        } else {
            divx = Common.NC[prelen] / divy;
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

        final int yp = Common.Y_SIDE[postlen];
        final int dividery = ((ygridsize + yp) - 1) / yp;
        final int xp = Common.X_SIDE[postlen];
        final int dividerx = ((xgridsize + xp) - 1) / xp;

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

        final Point pt = Point.fromMicroDeg(cornery, cornerx);
        if (!(createBoundaryForTerritoryRecord(m).containsPoint(pt))) {
            LOG.info("decodeGrid: Failed decodeGrid({}): {} not in {}", str, pt, createBoundaryForTerritoryRecord(m));
            return new MapcodeZone(); // already out of range
        }

        final int decodeMaxx = ((relx + xgridsize) < maxx) ? (relx + xgridsize) : maxx;
        final int decodeMaxy = ((rely + ygridsize) < maxy) ? (rely + ygridsize) : maxy;
        return decodeExtension(cornery, cornerx, dividerx << 2, dividery, extrapostfix,
                0, decodeMaxy, decodeMaxx); // grid
    }

    @Nonnull
    private static MapcodeZone decodeNameless(
            @Nonnull final String str,
            final int firstrec,
            @Nonnull final String extrapostfix) {
        String result = str;
        final int codexm = Data.getCodex(firstrec);
        if (codexm == 22) {
            result = result.substring(0, 3) + result.substring(4);
        } else {
            result = result.substring(0, 2) + result.substring(3);
        }

        final int a = Common.countCityCoordinatesForCountry(codexm, firstrec, firstrec);

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

        if (nrX > a) {  // past end!
            return new MapcodeZone();
        }

        final int territoryRecord = firstrec + nrX;

        int side = dataModel.getSmartDiv(territoryRecord);
        int xSIDE = side;

        final Boundary boundary = createBoundaryForTerritoryRecord(territoryRecord);
        final int maxx = boundary.getLonMicroDegMax();
        final int maxy = boundary.getLatMicroDegMax();
        final int minx = boundary.getLonMicroDegMin();
        final int miny = boundary.getLatMicroDegMin();

        final int dx;
        final int dy;

        if (Data.isSpecialShape(territoryRecord)) {
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
            LOG.error("decodeGrid: Failed, decodeNameless({}): dx {} > xSIDE {}", str, dx, xSIDE);
            return new MapcodeZone(); // return undefined (out of range!)
        }

        final int dividerx4 = Common.xDivider(miny, maxy); // 4 times too large!
        final int dividery = 90;

        final int cornerx = minx + ((dx * dividerx4) / 4);
        final int cornery = maxy - (dy * dividery);
        return decodeExtension(cornery, cornerx, dividerx4, -dividery, extrapostfix,
                ((dx * dividerx4) % 4), miny, maxx); // nameless
    }

    @Nonnull
    private static MapcodeZone decodeAutoHeader(
            final String input,
            final int m,
            @Nonnull final String extrapostfix) {
        // returns Point.isUndefined() in case or error
        int storageStart = 0;
        final int codexm = Data.getCodex(m);

        int value = decodeBase31(input); // decode top (before dot)
        value *= 961 * 31;
        final Point triple = decodeTriple(input.substring(input.length() - 3));
        // decode bottom 3 chars

        int i;
        i = m;
        while (true) {
            if ((Data.getTerritoryRecordType(i) < Data.TERRITORY_RECORD_TYPE_PLUS) || (Data.getCodex(i) != codexm)) {
                LOG.error("decodeGrid: Failed, decodeAutoHeader({}): out of {} records", input, codexm);
                return new MapcodeZone(); // return undefined
            }

            final int maxx = createBoundaryForTerritoryRecord(i).getLonMicroDegMax();
            final int maxy = createBoundaryForTerritoryRecord(i).getLatMicroDegMax();
            final int minx = createBoundaryForTerritoryRecord(i).getLonMicroDegMin();
            final int miny = createBoundaryForTerritoryRecord(i).getLatMicroDegMin();

            int h = ((maxy - miny) + 89) / 90;
            final int xdiv = Common.xDivider(miny, maxy);
            int w = ((((maxx - minx) * 4) + xdiv) - 1) / xdiv;

            h = 176 * (((h + 176) - 1) / 176);
            w = 168 * (((w + 168) - 1) / 168);

            int product = (w / 168) * (h / 176) * 961 * 31;

            if (Data.getTerritoryRecordType(i) == Data.TERRITORY_RECORD_TYPE_PLUS) {
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

                if ((cornerx < minx) || (cornerx >= maxx) || (cornery < miny) || (cornery > maxy)) {
                    LOG.error("decodeGrid: Failed, decodeAutoHeader({}): corner {}, {} out of bounds", input, cornery, cornerx);
                    return new MapcodeZone(); // corner out of bounds
                }

                return decodeExtension(cornery, cornerx, dividerx << 2, -dividery, extrapostfix,
                        0, miny, maxx); // autoheader
            }
            storageStart += product;
            i++;
        }
    }

    @Nonnull
    private static String aeuUnpack(@Nonnull final String argStr) {
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
    @Nonnull
    static String decodeUTF16(@Nonnull final String mapcode) {
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

        if (isAbjadScript(mapcode)) {
            return convertFromAbjad(result);
        } else {
            return result;
        }
    }

    @Nonnull
    static String encodeUTF16(
            @Nonnull final String mapcodeInput,
            final int alphabetCode) throws IllegalArgumentException {

        final String mapcode;
        if ((alphabetCode == Alphabet.GREEK.getNumber()) ||
                (alphabetCode == Alphabet.HEBREW.getNumber()) ||
                (alphabetCode == Alphabet.KOREAN.getNumber()) ||
                (alphabetCode == Alphabet.ARABIC.getNumber())) {
            mapcode = convertToAbjad(mapcodeInput);
        } else {
            mapcode = mapcodeInput;
        }

        final String mapcodeToEncode;
        if ((alphabetCode == Alphabet.GREEK.getNumber()) && ((mapcode.indexOf('E') != -1) || (mapcode.indexOf('U') != -1))) {
            final String unpacked = aeuUnpack(mapcode);
            if (unpacked.isEmpty()) {
                throw new IllegalArgumentException("encodeToAlphabetCode: cannot encode '" + mapcode +
                        "' to alphabet " + alphabetCode);
            }
            mapcodeToEncode = Encoder.aeuPack(unpacked, true);
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
    private static Point decodeTriple(@Nonnull final String str) {
        final int c1 = DECODE_CHARS[(int) str.charAt(0)];
        final int x = decodeBase31(str.substring(1));
        if (c1 < 24) {
            return Point.fromMicroDeg(((c1 / 6) * 34) + (x % 34), ((c1 % 6) * 28) + (x / 34));
        }
        return Point.fromMicroDeg((x % 40) + 136, (x / 40) + (24 * (c1 - 24)));
    }

    @Nonnull
    private static Point decodeSixWide(
            final int v,
            final int width,
            final int height) {
        final int d;
        int col = v / (height * 6);
        final int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            d = width - (maxcol * 6);
        } else {
            d = 6;
        }
        final int w = v - (col * height * 6);
        return Point.fromMicroDeg(height - 1 - (w / d), (col * 6) + (w % d));
    }

    // / lowest level encode/decode routines
    // decode up to dot or EOS;
    // returns negative in case of error
    private static int decodeBase31(@Nonnull final String code) {
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
    private static MapcodeZone decodeExtension(
            final int y,
            final int x,
            final int dividerx0,
            final int dividery0,
            @Nonnull final String extrapostfix,
            final int lon_offset4,
            final int extremeLatMicroDeg,
            final int maxLonMicroDeg) {
        final MapcodeZone mapcodeZone = new MapcodeZone();
        double dividerx4 = (double) dividerx0;
        double dividery = (double) dividery0;
        double processor = 1;
        int lon32 = 0;
        int lat32 = 0;
        boolean odd = false;
        int idx = 0;
        // decode up to 8 characters
        final int len = (extrapostfix.length() > 8) ? 8 : extrapostfix.length();
        while (idx < len) {
            int c1 = (int) extrapostfix.charAt(idx);
            idx++;
            c1 = DECODE_CHARS[c1];
            if ((c1 < 0) || (c1 == 30)) {
                LOG.error("decodeGrid; Failed, decodeExtension({}): illegal c1 {}", extrapostfix, c1);
                return new MapcodeZone();
            }
            final int y1 = c1 / 5;
            final int x1 = c1 % 5;
            final int y2;
            final int x2;
            if (idx < len) {
                int c2 = (int) extrapostfix.charAt(idx);
                idx++;
                c2 = DECODE_CHARS[c2];
                if ((c2 < 0) || (c2 == 30)) {
                    LOG.error("decodeGrid: Failed, decodeExtension({}): illegal c2 {}", extrapostfix, c2);
                    return new MapcodeZone();
                }
                y2 = c2 / 6;
                x2 = c2 % 6;
            } else {
                odd = true;
                y2 = 0;
                x2 = 0;
            }

            processor *= 30;
            lon32 = (lon32 * 30) + (x1 * 6) + x2;
            lat32 = (lat32 * 30) + (y1 * 5) + y2;
        }

        while (processor < Point.MAX_PRECISION_FACTOR) {
            dividerx4 *= 30;
            dividery *= 30;
            processor *= 30;
        }

        final double lon4 = (x * 4 * Point.MAX_PRECISION_FACTOR) + (lon32 * dividerx4) + (lon_offset4 * Point.MAX_PRECISION_FACTOR);
        final double lat1 = (y * Point.MAX_PRECISION_FACTOR) + (lat32 * dividery);

        // determine the range of coordinates that are encode to this mapcode
        if (odd) { // odd
            mapcodeZone.setFromFractions(lat1, lon4, 5 * dividery, 6 * dividerx4);
        } else { // not odd
            mapcodeZone.setFromFractions(lat1, lon4, dividery, dividerx4);
        } // not odd

        // FORCE_RECODE - restrict the coordinate range to the extremes that were provided
        if (mapcodeZone.getLonFractionMax() > (maxLonMicroDeg * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR)) {
            mapcodeZone.setLonFractionMax(maxLonMicroDeg * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
        }
        if (dividery >= 0) {
            if (mapcodeZone.getLatFractionMax() > (extremeLatMicroDeg * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR)) {
                mapcodeZone.setLatFractionMax(extremeLatMicroDeg * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR);
            }
        } else {
            if (mapcodeZone.getLatFractionMin() < (extremeLatMicroDeg * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR)) {
                mapcodeZone.setLatFractionMin(extremeLatMicroDeg * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR);
            }
        }
        return mapcodeZone;
    }

    private static boolean isAbjadScript(@Nonnull final String argStr) {
        for (final char ch : argStr.toCharArray()) {
            final int c = (int) ch;
            if ((c >= 0x0628) && (c <= 0x0649)) {
                return true; // Arabic
            }
            if ((c >= 0x05d0) && (c <= 0x05ea)) {
                return true; // Hebrew
            }
            if ((c >= 0x388) && (c <= 0x3C9)) {
                return true; // Greek uppercase and lowecase
            }
            if (((c >= 0x1100) && (c <= 0x1174)) || ((c >= 0xad6c) && (c <= 0xd314))) {
                return true; // Korean
            }
        }
        return false;
    }

    @Nonnull
    private static String convertFromAbjad(@Nonnull final String mapcode) {
        // split into prefix, s, postfix
        int p = mapcode.lastIndexOf(' ');
        if (p < 0) {
            p = 0;
        } else {
            p++;
        }
        final String prefix = mapcode.substring(0, p);
        final String remainder = mapcode.substring(p);
        final String postfix;
        final int h = remainder.indexOf('-');
        final String s;
        if (h > 0) {
            postfix = remainder.substring(h);
            s = aeuUnpack(remainder.substring(0, h));
        } else {
            postfix = "";
            s = aeuUnpack(remainder);
        }

        final int len = s.length();
        final int dot = s.indexOf('.');
        if ((dot < 2) || (dot > 5)) {
            return mapcode;
        }
        final int form = (10 * dot) + (len - dot - 1);

        String newstr = "";
        if (form == 23) {
            final int c = (DECODE_CHARS[(int) s.charAt(3)] * 8) + (DECODE_CHARS[(int) s.charAt(4)] - 18);
            if ((c >= 0) && (c < 31)) {
                newstr = s.substring(0, 2) + '.' + Data.ENCODE_CHARS[c] + s.charAt(5);
            }
        } else if (form == 24) {
            final int c = (DECODE_CHARS[(int) s.charAt(3)] * 8) + (DECODE_CHARS[(int) s.charAt(4)] - 18);
            if ((c >= 32) && (c < 63)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c - 32] + '.' + s.charAt(5) + s.charAt(6);
            } else if ((c >= 0) && (c < 31)) {
                newstr = s.substring(0, 2) + '.' + Data.ENCODE_CHARS[c % 31] + s.charAt(5) + s.charAt(6);
            }
        } else if (form == 34) {
            final int c = (DECODE_CHARS[(int) s.charAt(2)] * 10) + (DECODE_CHARS[(int) s.charAt(5)] - 7);
            if ((c >= 0) && (c < 31)) {
                newstr = s.substring(0, 2) + '.' + Data.ENCODE_CHARS[c] + s.charAt(4) + s.charAt(6) + s.charAt(7);
            } else if ((c >= 31) && (c < 62)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c - 31] + '.' + s.charAt(4) + s.charAt(6) + s.charAt(7);
            } else if ((c >= 62) && (c < 93)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c - 62] + s.charAt(4) + '.' + s.charAt(6) + s.charAt(7);
            }
        } else if (form == 35) {
            final int c = ((DECODE_CHARS[(int) s.charAt(2)] * 8) + (DECODE_CHARS[(int) s.charAt(6)] - 18));
            if ((c >= 32) && (c < 63)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c - 32] + s.charAt(4) + '.' + s.charAt(5) + s.charAt(7) + s.charAt(8);
            } else if ((c >= 0) && (c < 31)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c] + '.' + s.charAt(4) + s.charAt(5) + s.charAt(7) + s.charAt(8);
            }
        } else if (form == 45) {
            final int c = (DECODE_CHARS[(int) s.charAt(2)] * 100) + (DECODE_CHARS[(int) s.charAt(5)] * 10) + (DECODE_CHARS[(int) s.charAt(8)] - 39);
            if ((c >= 0) && (c < 961)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c / 31] + s.charAt(3) + '.' + s.charAt(6) + s.charAt(7) + s.charAt(9) + Data.ENCODE_CHARS[c % 31];
            }
        } else if (form == 55) {
            final int c = (DECODE_CHARS[(int) s.charAt(2)] * 100) + (DECODE_CHARS[(int) s.charAt(6)] * 10) + (DECODE_CHARS[(int) s.charAt(9)] - 39);
            if ((c >= 0) && (c < 961)) {
                newstr = s.substring(0, 2) + Data.ENCODE_CHARS[c / 31] + s.charAt(3) + s.charAt(4) + '.' + s.charAt(7) + s.charAt(8) + s.charAt(10) + Data.ENCODE_CHARS[c % 31];
            }
        }

        if (newstr.isEmpty()) {
            return mapcode;
        }
        return prefix + Encoder.aeuPack(newstr, false) + postfix;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Nonnull
    private static String convertToAbjad(@Nonnull final String mapcode) {
        String str;
        final String rest;
        final int h = mapcode.indexOf('-');
        if (h > 0) {
            rest = mapcode.substring(h);
            str = aeuUnpack(mapcode.substring(0, h));
        } else {
            rest = "";
            str = aeuUnpack(mapcode);
        }

        final int len = str.length();
        final int dot = str.indexOf('.');
        if ((dot < 2) || (dot > 5)) {
            return mapcode;
        }
        final int form = (10 * dot) + (len - dot - 1);

        // see if >2 non-digits in a row
        int inarow = 0;
        for (final char ch : str.toCharArray()) {
            if (ch != '.') {
                inarow++;
                if ((ch >= '0') && (ch <= '9')) {
                    inarow = 0;
                } else if (inarow > 2) {
                    break;
                }
            }
        }

        if ((inarow < 3) && ((form == 22) || (form == 32) || (form == 33) || (form == 42) || (form == 43) || (form == 44) || (form == 54))) {
            // no need to do anything
            return mapcode;
        }
        // determine the code of the second non-digit character (before or after the dot)
        int c = DECODE_CHARS[(int) str.charAt(2)];
        if (c < 0) {
            c = DECODE_CHARS[(int) str.charAt(3)];
        }
        if (c < 0) {
            return mapcode; // bad character
        }

        // create 2 or 3 new digits
        final char c1;
        final char c2;
        final char c3;
        if (form >= 44) {
            c = (c * 31) + DECODE_CHARS[(int) str.charAt(len - 1)] + 39;
            if ((c < 39) || (c > 999)) {
                return mapcode; // out of range (last character must be bad)
            }
            c1 = Data.ENCODE_CHARS[c / 100];
            c2 = Data.ENCODE_CHARS[((c % 100) / 10)];
            c3 = Data.ENCODE_CHARS[c % 10];
        } else if (len == 7) {
            if (form == 24) {
                c += 7;
            } else if (form == 33) {
                c += 38;
            } else if (form == 42) {
                c += 69;
            }
            c1 = Data.ENCODE_CHARS[c / 10];
            c2 = Data.ENCODE_CHARS[c % 10];
            c3 = '?';
        } else {
            c1 = Data.ENCODE_CHARS[2 + (c / 8)];
            c2 = Data.ENCODE_CHARS[2 + (c % 8)];
            c3 = '?';
        }

        // re-order the characters
        if (form == 22) {
            str = str.substring(0, 2) + '.' + c1 + c2 + str.charAt(4);
        } else if (form == 23) {
            str = str.substring(0, 2) + '.' + c1 + c2 + str.charAt(4) + str.charAt(5);
        } else if (form == 32) {
            str = str.substring(0, 2) + '.' + (char) ((int) c1 + 4) + c2 + str.charAt(4) + str.charAt(5);
        } else if (form == 24) {
            str = str.substring(0, 2) + c1 + '.' + str.charAt(4) + c2 + str.charAt(5) + str.charAt(6);
        } else if (form == 33) {
            str = str.substring(0, 2) + c1 + '.' + str.charAt(4) + c2 + str.charAt(5) + str.charAt(6);
        } else if (form == 42) {
            str = str.substring(0, 2) + c1 + '.' + str.charAt(3) + c2 + str.charAt(5) + str.charAt(6);
        } else if (form == 43) {
            str = str.substring(0, 2) + (char) ((int) c1 + 4) + '.' + str.charAt(3) + str.charAt(5) + c2 + str.charAt(6) + str.charAt(7);
        } else if (form == 34) {
            str = str.substring(0, 2) + c1 + '.' + str.charAt(4) + str.charAt(5) + c2 + str.charAt(6) + str.charAt(7);
        } else if (form == 44) {
            str = str.substring(0, 2) + c1 + str.charAt(3) + '.' + c2 + str.charAt(5) + str.charAt(6) + c3 + str.charAt(7);
        } else if (form == 54) {
            str = str.substring(0, 2) + c1 + str.charAt(3) + str.charAt(4) + '.' + c2 + str.charAt(6) + str.charAt(7) + c3 + str.charAt(8);
        } else {
            // not a valid mapcode form
            return mapcode;
        }
        return Encoder.aeuPack(str + rest, false);
    }
}
