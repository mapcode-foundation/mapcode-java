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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mapcode.Boundary.createBoundaryForTerritoryRecord;
import static com.mapcode.Common.*;

 // ----------------------------------------------------------------------------------------------
 // Package private implementation class. For internal use within the Mapcode implementation only.
 // ----------------------------------------------------------------------------------------------

/**
 * This class contains encoder for mapcodes.
 */
@SuppressWarnings({"MagicNumber", "StringConcatenationMissingWhitespace"})
final class Encoder {
    private static final Logger LOG = LoggerFactory.getLogger(Encoder.class);

    // Get direct access to data model singleton.
    private static final DataModel DATA_MODEL = DataModel.getInstance();

    private Encoder() {
        // Prevent instantiation.
    }

    // ----------------------------------------------------------------------
    // Method called from public Java API.
    // ----------------------------------------------------------------------

    @Nonnull
    static List<Mapcode> encode(
            final double latDeg,
            final double lonDeg,
            @Nullable final Territory territory,
            final boolean limitToOneResult) {

        return encode(latDeg, lonDeg, territory, limitToOneResult, null);
    }

    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------

    private final static char[] ENCODE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
            'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z', 'A', 'E', 'U'};

    @Nonnull
    private static List<Mapcode> encode(
            final double argLatDeg,
            final double argLonDeg,
            @Nullable final Territory territory,
            final boolean limitToOneResult,
            @Nullable final Territory argStateOverride) {
        LOG.trace("encode: latDeg={}, lonDeg={}, territory={}, limitToOneResult={}",
                argLatDeg, argLonDeg, (territory == null) ? null : territory.name(), limitToOneResult);

        final Point pointToEncode = Point.fromDeg(argLatDeg, argLonDeg);
        final List<Mapcode> results = new ArrayList<Mapcode>();
        int lastBaseSubTerritoryNumber = -1;

        // Determine whether to walk through all records, or just for one (given) territory.
        final int firstTerritoryRecord = (territory != null) ? territory.getNumber() : 0;
        final int lastTerritoryRecord = (territory != null) ? territory.getNumber() : Territory.AAA.getNumber();
        for (int territoryRecord = firstTerritoryRecord; territoryRecord <= lastTerritoryRecord; territoryRecord++) {

            // Check if the point to encode is covered by the last data record.
            final int firstSubTerritoryRecord = DATA_MODEL.getDataLastRecord(territoryRecord);
            if (createBoundaryForTerritoryRecord(firstSubTerritoryRecord).containsPoint(pointToEncode)) {

                final int lastSubTerritoryRecord = DATA_MODEL.getDataFirstRecord(territoryRecord);
                final Territory currentEncodeTerritory = Territory.fromNumber(territoryRecord);

                for (int subTerritoryRecord = lastSubTerritoryRecord; subTerritoryRecord <= firstSubTerritoryRecord; subTerritoryRecord++) {

                    // Check if the point to encode is contained within the boundary.
                    if (createBoundaryForTerritoryRecord(subTerritoryRecord).containsPoint(pointToEncode)) {

                        // All fine, proceed with creating a mapcode.
                        String mapcode = "";
                        if (Data.isNameless(subTerritoryRecord)) {
                            mapcode = encodeNameless(pointToEncode, subTerritoryRecord, lastSubTerritoryRecord);

                        } else if (Data.getTerritoryRecordType(subTerritoryRecord) > Data.TERRITORY_RECORD_TYPE_PIPE) {
                            mapcode = encodeAutoHeader(pointToEncode, subTerritoryRecord);

                        } else if ((subTerritoryRecord == firstSubTerritoryRecord) &&
                                (currentEncodeTerritory.getParentTerritory() != null)) {
                            results.addAll(encode(argLatDeg, argLonDeg, currentEncodeTerritory.getParentTerritory(),
                                    limitToOneResult, currentEncodeTerritory));
                            continue;

                        } else if (!Data.isRestricted(subTerritoryRecord) || (lastBaseSubTerritoryNumber == lastSubTerritoryRecord)) {
                            if (Data.getCodex(subTerritoryRecord) < 54) {
                                mapcode = encodeGrid(subTerritoryRecord, pointToEncode);
                            }
                        } else {
                            // Skip this record.
                        }

                        // Check if we created a mapcode.
                        if (!mapcode.isEmpty()) {
                            assert mapcode.length() > 4;
                            mapcode = aeuPack(mapcode, false);

                            final Territory encodeTerritory = (argStateOverride != null) ?
                                    argStateOverride : currentEncodeTerritory;

                            // Create new result.
                            final Mapcode newResult = new Mapcode(mapcode, encodeTerritory);

                            // The result should not be stored yet.
                            if (results.contains(newResult)) {
                                LOG.error("encode: Duplicate results found, newResult={}, results={} items",
                                        newResult.getCodeWithTerritory(), results.size());
                            } else {

                                // Remove existing results (if there was a parent territory).
                                if (limitToOneResult) {
                                    results.clear();
                                }
                                results.add(newResult);
                            }

                            lastBaseSubTerritoryNumber = lastSubTerritoryRecord;

                            // Stop if we only need a single result anyway.
                            if (limitToOneResult) {
                                return results;
                            }
                        }
                    }
                }
            }
        }
        LOG.trace("encode: results={} items", results.size());
        LOG.trace("");
        return results;
    }

    @Nonnull
    private static String encodeExtension(
            final Point pointToEncode,
            final int extrax4,
            final int extray,
            final int dividerx4,
            final int dividery,
            final int ydirection) {
        int extraDigits = 8; // always generate 8 digits

        double factorx = Point.MAX_PRECISION_FACTOR * dividerx4;
        double factory = Point.MAX_PRECISION_FACTOR * dividery;
        double valx = (Point.MAX_PRECISION_FACTOR * extrax4) + pointToEncode.getLonFraction();
        double valy = (Point.MAX_PRECISION_FACTOR * extray) + (ydirection * pointToEncode.getLatFraction());

        final StringBuilder sb = new StringBuilder();
        sb.append('-');

        while (true) {
            factorx /= 30;
            //noinspection NumericCastThatLosesPrecision
            final int gx = (int) (valx / factorx);

            factory /= 30;
            //noinspection NumericCastThatLosesPrecision
            final int gy = (int) (valy / factory);

            sb.append(ENCODE_CHARS[((gy / 5) * 5) + (gx / 6)]);
            --extraDigits;
            if (extraDigits == 0) {
                break;
            }

            sb.append(ENCODE_CHARS[(((gy % 5) * 6) + (gx % 6))]);
            --extraDigits;
            if (extraDigits == 0) {
                break;
            }

            valx -= factorx * gx;
            valy -= factory * gy;
        }
        return sb.toString();
    }

    @Nonnull
    private static String encodeGrid(
            final int territoryNumber,
            @Nonnull final Point pointToEncode) {
        int codexm = Data.getCodex(territoryNumber);
        final int orgcodex = codexm;
        if (codexm == 21) {
            codexm = 22;
        } else if (codexm == 14) {
            codexm = 23;
        }
        final int prelen = codexm / 10;
        final int postlen = codexm % 10;
        final int divx;
        int divy = DATA_MODEL.getSmartDiv(territoryNumber);
        if (divy == 1) {
            divx = X_SIDE[prelen];
            divy = Y_SIDE[prelen];
        } else {
            divx = NC[prelen] / divy;
        }

        final int minx = createBoundaryForTerritoryRecord(territoryNumber).getLonMicroDegMin();
        final int miny = createBoundaryForTerritoryRecord(territoryNumber).getLatMicroDegMin();
        final int maxx = createBoundaryForTerritoryRecord(territoryNumber).getLonMicroDegMax();
        final int maxy = createBoundaryForTerritoryRecord(territoryNumber).getLatMicroDegMax();

        final int ygridsize = (((maxy - miny) + divy) - 1) / divy;
        int rely = pointToEncode.getLatMicroDeg() - miny;
        rely = rely / ygridsize;

        final int xgridsize = (((maxx - minx) + divx) - 1) / divx;
        int x = pointToEncode.getLonMicroDeg();
        int relx = x - minx;
        if (relx < 0) {
            x += Point.MICRO_DEG_360;
            relx += Point.MICRO_DEG_360;
        } else if (relx >= Point.MICRO_DEG_360) {
            x -= Point.MICRO_DEG_360;
            relx -= Point.MICRO_DEG_360;
        }
        if (relx < 0) {
            return "";
        }
        relx = relx / xgridsize;
        if ((relx >= divx) || (rely >= divy)) {
            return "";
        }

        final int v;
        if ((divx != divy) && (prelen > 2)) {
            v = encodeSixWide(relx, rely, divx, divy);
        } else {
            v = ((relx * divy) + divy) - 1 - rely;
        }

        String result = encodeBase31(v, prelen);

        if ((prelen == 4) && (divx == 961) && (divy == 961)) {
            result = String.valueOf(result.charAt(0)) + result.charAt(2) + result.charAt(1) + result.charAt(3);
        }

        rely = miny + (rely * ygridsize);
        relx = minx + (relx * xgridsize);

        final int dividery = ((ygridsize + Y_SIDE[postlen]) - 1) / Y_SIDE[postlen];
        final int dividerx = ((xgridsize + X_SIDE[postlen]) - 1) / X_SIDE[postlen];

        result += '.';

        int difx = x - relx;
        int dify = pointToEncode.getLatMicroDeg() - rely;

        final int extrax = difx % dividerx;
        final int extray = dify % dividery;

        difx = difx / dividerx;
        dify = dify / dividery;

        dify = Y_SIDE[postlen] - 1 - dify;
        if (postlen == 3) {
            result += encodeTriple(difx, dify);
        } else {

            String postfix = encodeBase31(((difx) * Y_SIDE[postlen]) + dify, postlen);
            if (postlen == 4) {
                postfix = String.valueOf(postfix.charAt(0)) + postfix.charAt(2) + postfix.charAt(1) + postfix.charAt(3);
            }
            result += postfix;
        }

        if (orgcodex == 14) {
            result = result.charAt(0) + "." + result.charAt(1) + result.substring(3);
        }

        result += encodeExtension(pointToEncode, extrax << 2, extray, dividerx << 2, dividery, 1); // grid

        return Data.headerLetter(territoryNumber) + result;
    }

    @Nonnull
    private static String encodeAutoHeader(
            @Nonnull final Point pointToEncode,
            final int territoryRecord) {
        final StringBuilder stringBuilder = new StringBuilder();
        final int codexm = Data.getCodex(territoryRecord);
        int storageStart = 0;

        // search back to first pipe star
        int firstindex = territoryRecord;
        while ((Data.getTerritoryRecordType(firstindex - 1) > Data.TERRITORY_RECORD_TYPE_PIPE) && (Data.getCodex(firstindex - 1) == codexm)) {
            firstindex--;
        }

        int i = firstindex;
        while (true) {

            final int maxx = createBoundaryForTerritoryRecord(i).getLonMicroDegMax();
            final int maxy = createBoundaryForTerritoryRecord(i).getLatMicroDegMax();
            final int minx = createBoundaryForTerritoryRecord(i).getLonMicroDegMin();
            final int miny = createBoundaryForTerritoryRecord(i).getLatMicroDegMin();

            int h = ((maxy - miny) + 89) / 90;
            final int xdiv = xDivider(miny, maxy);
            int w = ((((maxx - minx) * 4) + xdiv) - 1) / xdiv;

            h = 176 * (((h + 176) - 1) / 176);
            w = 168 * (((w + 168) - 1) / 168);

            int product = (w / 168) * (h / 176) * 961 * 31;

            if (Data.getTerritoryRecordType(i) == Data.TERRITORY_RECORD_TYPE_PLUS) // plus pipe
            {
                final int goodRounder = (codexm >= 23) ? (961 * 961 * 31) : (961 * 961);
                product = ((((storageStart + product + goodRounder) - 1) / goodRounder) * goodRounder) - storageStart;
            }

            if (i == territoryRecord) {
                final int dividerx = (((maxx - minx) + w) - 1) / w;
                final int vx = (pointToEncode.getLonMicroDeg() - minx) / dividerx;
                final int extrax = (pointToEncode.getLonMicroDeg() - minx) % dividerx;

                final int dividery = (((maxy - miny) + h) - 1) / h;
                int vy = (maxy - pointToEncode.getLatMicroDeg()) / dividery;
                int extray = (maxy - pointToEncode.getLatMicroDeg()) % dividery;

                int value = (vx / 168) * (h / 176);
                if ((extray == 0) && (pointToEncode.getLatFraction() > 0)) {
                    vy--;
                    extray += dividery;
                }
                value += (vy / 176);

                final int codexlen = (codexm / 10) + (codexm % 10);
                stringBuilder.append(encodeBase31((storageStart / (961 * 31)) + value, codexlen - 2));
                stringBuilder.append('.');
                stringBuilder.append(encodeTriple(vx % 168, vy % 176));

                stringBuilder.append(
                        encodeExtension(pointToEncode, extrax << 2, extray, dividerx << 2, dividery, -1)); // AutoHeader
                return stringBuilder.toString();
            }

            storageStart += product;
            i++;
        }
    }

    @Nonnull
    private static String encodeNameless(
            @Nonnull final Point pointToEncode,
            final int territoryRecord,
            final int firstTerritoryRecord) {
        // mid-level encode/decode
        // returns "" in case of (argument) error
        final int codexm = Data.getCodex(territoryRecord);
        final int codexlen = (codexm / 10) + (codexm % 10);
        final int firstNamelessRecord = getFirstNamelessRecord(codexm, territoryRecord, firstTerritoryRecord);
        final int a = countCityCoordinatesForCountry(codexm, territoryRecord, firstTerritoryRecord);
        final int p = 31 / a;
        final int r = 31 % a;
        final int nrX = territoryRecord - firstNamelessRecord;

        int storage_offset;

        if ((codexm != 21) && (a <= 31)) {
            storage_offset = ((nrX * p) + ((nrX < r) ? nrX : r)) * (961 * 961);
        } else if ((codexm != 21) && (a < 62)) {
            if (nrX < (62 - a)) {
                storage_offset = nrX * 961 * 961;
            } else {
                storage_offset = ((62 - a) + (((nrX - 62) + a) / 2)) * 961 * 961;
                if (((nrX + a) & 1) != 0) {
                    storage_offset += 16 * 961 * 31;
                }
            }
        } else {
            final int basePower = (codexm == 21) ? (961 * 961) : (961 * 961 * 31);
            int basePowerA = basePower / a;
            if (a == 62) {
                basePowerA++;
            } else {
                basePowerA = 961 * (basePowerA / 961);
            }

            storage_offset = nrX * basePowerA;
        }

        int side = DATA_MODEL.getSmartDiv(territoryRecord);
        final int orgSide = side;
        int xSide = side;

        final int maxy = createBoundaryForTerritoryRecord(territoryRecord).getLatMicroDegMax();
        final int minx = createBoundaryForTerritoryRecord(territoryRecord).getLonMicroDegMin();
        final int miny = createBoundaryForTerritoryRecord(territoryRecord).getLatMicroDegMin();

        final int dividerx4 = xDivider(miny, maxy);
        final int xFracture = pointToEncode.getLonFraction() / 810000;
        final int dminx = pointToEncode.getLonMicroDeg() - minx;
        final int dx = ((4 * dminx) + xFracture) / dividerx4;
        final int extrax4 = (4 * dminx) - (dx * dividerx4); // like modulus, but with floating point value

        final int dividery = 90;
        final int dmaxy = maxy - pointToEncode.getLatMicroDeg();
        int dy = dmaxy / dividery;
        int extray = dmaxy % dividery;

        if ((extray == 0) && (pointToEncode.getLatFraction() > 0)) {
            dy--;
            extray += dividery;
        }

        int v = storage_offset;
        if (Data.isSpecialShape(territoryRecord)) {
            xSide *= side;
            side = 1 + ((maxy - miny) / 90);
            xSide = xSide / side;
            v += encodeSixWide(dx, side - 1 - dy, xSide, side);
        } else {
            v += (dx * side) + dy;
        }

        String result = encodeBase31(v, codexlen + 1);

        if (codexlen == 3) {
            result = result.substring(0, 2) + '.' + result.substring(2);
        } else if (codexlen == 4) {
            if ((codexm == 22) && (a < 62) && (orgSide == 961) && !Data.isSpecialShape(territoryRecord)) {
                result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
            }
            if (codexm == 13) {
                result = result.substring(0, 2) + '.' + result.substring(2);
            } else {
                result = result.substring(0, 3) + '.' + result.substring(3);
            }
        }
        result += encodeExtension(pointToEncode, extrax4, extray, dividerx4, dividery, -1); // for encodeNameless

        return result;
    }

    @Nonnull
    static String aeuPack(
            @Nonnull final String argStr,
            final boolean argShort) {
        // add vowels to prevent all-digit mapcodes
        String str = argStr;
        int dotpos = -9;
        int rlen = str.length();
        int d;
        String rest = "";
        for (d = 0; d < rlen; d++) {
            if ((str.charAt(d) < '0') || (str.charAt(d) > '9')) // not digit?
            {
                if ((str.charAt(d) == '.') && (dotpos < 0)) // first dot?
                {
                    dotpos = d;
                } else if (str.charAt(d) == '-') {
                    rest = str.substring(d);
                    str = str.substring(0, d);
                    rlen = d;
                } else {
                    return str; // not all-digit (or multiple dots)
                }
            }
        }

        if ((rlen - 2) > dotpos) {
            // does r have a dot, AND at least 2 chars after the dot?
            if (argShort) { // use A only
                final int v = ((((int) str.charAt(0)) - 48) * 100) + ((((int) str.charAt(rlen - 2)) - 48) * 10) + (((int) str.charAt(rlen - 1)) - 48);
                return 'A' + str.substring(1, rlen - 2) + ENCODE_CHARS[v / 32] + ENCODE_CHARS[v % 32] + rest;
            } else { // use A, E and U
                final int v = (((((int) str.charAt(rlen - 2)) - 48) * 10) + ((int) str.charAt(rlen - 1))) - 48;
                str = str.substring(0, rlen - 2) + ENCODE_CHARS[31 + (v / 34)] + ENCODE_CHARS[v % 34];
            }
        }
        return str + rest;
    }

    @Nonnull
    private static String encodeBase31(
            final int argValue,
            final int argNrChars) {
        int value = argValue;
        int nrChars = argNrChars;
        final StringBuilder result = new StringBuilder();
        while (nrChars > 0) {
            nrChars--;
            result.insert(0, ENCODE_CHARS[value % 31]);
            value = value / 31;
        }
        return result.toString();
    }

    private static int encodeSixWide(
            final int x,
            final int y,
            final int width,
            final int height) {
        final int d;
        int col = x / 6;
        final int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            d = width - (maxcol * 6);
        } else {
            d = 6;
        }
        return ((height * 6 * col) + ((height - 1 - y) * d) + x) - (col * 6);
    }

    @Nonnull
    private static String encodeTriple(
            final int difx,
            final int dify) {
        if (dify < (4 * 34)) {
            return ENCODE_CHARS[((difx / 28) + (6 * (dify / 34)))] + encodeBase31(((difx % 28) * 34) + (dify % 34), 2);
        } else {
            return ENCODE_CHARS[((difx / 24) + 24)] + encodeBase31((((difx % 24) * 40) + dify) - 136, 2);
        }
    }
}
