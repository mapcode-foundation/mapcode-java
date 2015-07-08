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
import java.util.ArrayList;
import java.util.List;

import static com.mapcode.Common.*;

class Encoder {
    private static final Logger LOG = LoggerFactory.getLogger(Encoder.class);

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
            final boolean isRecursive,
            final boolean stopWithOneResult,
            final boolean allowWorld) {

        return encode(latDeg, lonDeg, territory, isRecursive, stopWithOneResult, allowWorld, null);
    }

    // ----------------------------------------------------------------------
    // Private methods.
    // ----------------------------------------------------------------------

    private final static char[] ENCODE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
            'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z', 'A', 'E', 'U'};

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static List<Mapcode> encode(final double argLatDeg, final double argLonDeg,
                                        @Nullable final Territory territory, final boolean isRecursive, final boolean limitToOneResult, final boolean allowWorld,
                                        @Nullable final Territory argStateOverride) {
        LOG.trace("encode: latDeg={}, lonDeg={}, territory={}, isRecursive={}, limitToOneResult={}, allowWorld={}",
                argLatDeg, argLonDeg, (territory == null) ? null : territory.name(), isRecursive, limitToOneResult,
                allowWorld);

        final double latDeg = Point.mapToLat(argLatDeg);
        final double lonDeg = Point.mapToLon(argLonDeg);
        Territory stateOverride = argStateOverride;

        final Point pointToEncode = Point.fromDeg(latDeg, lonDeg);
        final List<SubArea> areas = SubArea.getAreasForPoint(pointToEncode);
        final List<Mapcode> results = new ArrayList<Mapcode>();

        int lastbasesubareaID = -1;

        for (final SubArea subArea : areas) {
            if ((territory != null) && (subArea.getParentTerritory() != territory)) {
                continue;
            }

            final Territory currentEncodeTerritory = subArea.getParentTerritory();

            if ((currentEncodeTerritory == Territory.AAA) && !allowWorld &&
                    (territory != Territory.AAA)) {
                continue;
            }

            final int from = DataAccess.dataFirstRecord(currentEncodeTerritory.getNumber());
            final Data mapcoderData = new Data(from);
            if (mapcoderData.getFlags() == 0) {
                continue;
            }
            final int upto = DataAccess.dataLastRecord(currentEncodeTerritory.getNumber());


            final int i = subArea.getSubAreaID();
            mapcoderData.dataSetup(i);
            if ((mapcoderData.getCodex() < 54) && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                String mapcode = "";
                final Territory territoryParent = currentEncodeTerritory.getParentTerritory();
                if (mapcoderData.isUseless() && (i == upto) && (territoryParent != null)) {

                    if (!isRecursive) {
                        stateOverride = currentEncodeTerritory;
                        results.addAll(encode(latDeg, lonDeg, territoryParent, true, limitToOneResult,
                                allowWorld, stateOverride));
                        stateOverride = null;
                    }

                    continue;
                }
                if ((mapcoderData.getPipeType() == 0) && !mapcoderData.isNameless()) {
                    if (!mapcoderData.isUseless() || (lastbasesubareaID == from)) {
                        mapcode = encodeGrid(i, pointToEncode, mapcoderData);
                    }
                } else if (mapcoderData.getPipeType() == 4) {
                    mapcode = encodeGrid(i, pointToEncode, mapcoderData);
                } else if (mapcoderData.isNameless()) { // auto-pipe 21/22
                    mapcode = encodeNameless(pointToEncode, mapcoderData, i, from);
                } else { // pipe star, pipe plus
                    mapcode = encodeStarpipe(pointToEncode, mapcoderData, i);
                }

                if (mapcode.length() > 4) {
                    mapcode = aeuPack(mapcode, false);

                    Territory encodeTerritory = currentEncodeTerritory;
                    if (stateOverride != null) {
                        encodeTerritory = stateOverride;
                    }

                    // Create new result.
                    final Mapcode newResult = new Mapcode(mapcode, encodeTerritory);

                    // The result should not be stored yet.
                    if (!results.contains(newResult)) {
                        if (limitToOneResult) {
                            results.clear();
                        }
                        results.add(newResult);
                    } else {
                        LOG.error("encode: Duplicate results found, newResult={}, results={} items",
                                newResult.getCodeWithTerritory(), results.size());
                    }

                    lastbasesubareaID = from;
                    if (limitToOneResult) {
                        return results;
                    }
                }
            }
        }
        LOG.trace("encode: isRecursive={}, results={} items", isRecursive, results.size());
        LOG.trace("");
        return results;
    }

    private static String addPostfix(final int extrax4, final int extray, final int dividerx4, final int dividery) {
        final int gx = ((30 * extrax4) / dividerx4);
        final int gy = ((30 * extray) / dividery);
        final int x1 = (gx / 6);
        final int y1 = (gy / 5);
        String s = "-" + ENCODE_CHARS[((y1 * 5) + x1)];
        final int x2 = (gx % 6);
        final int y2 = (gy % 5);
        s += ENCODE_CHARS[((y2 * 6) + x2)];
        return s;
    }

    private static String encodeGrid(final int m, final Point point, final Data mapcoderData) {
        Point pointToEncode = point;
        int codex = mapcoderData.getCodex();
        final int orgcodex = codex;
        if (codex == 14) {
            codex = 23;
        }
        final int dc = codex / 10;
        final int codexlow = codex % 10;
        final int divx;
        int divy = DataAccess.smartDiv(m);
        if (divy == 1) {
            divx = xSide[dc];
            divy = ySide[dc];
        } else {
            divx = nc[dc] / divy;
        }

        final int ygridsize =
                (((mapcoderData.getMapcoderRect().getMaxY() - mapcoderData.getMapcoderRect().getMinY()) + divy) - 1)
                        / divy;
        int rely = pointToEncode.getLatMicroDeg() - mapcoderData.getMapcoderRect().getMinY();
        rely = rely / ygridsize;
        final int xgridsize =
                (((mapcoderData.getMapcoderRect().getMaxX() - mapcoderData.getMapcoderRect().getMinX()) + divx) - 1)
                        / divx;

        int relx = pointToEncode.getLonMicroDeg() - mapcoderData.getMapcoderRect().getMinX();
        if (relx < 0) {
            pointToEncode =
                    Point.fromMicroDeg(pointToEncode.getLatMicroDeg(), pointToEncode.getLonMicroDeg() + 360000000);
            relx += 360000000;
        } else if (relx >= 360000000) {
            pointToEncode =
                    Point.fromMicroDeg(pointToEncode.getLatMicroDeg(), pointToEncode.getLonMicroDeg() - 360000000);
            relx -= 360000000;
        }
        if (relx < 0) {
            return "";
        }
        relx = relx / xgridsize;
        if (relx >= divx) {
            return "";
        }

        final int v;
        if ((divx != divy) && (codex > 24)) // D==6
        {
            v = encode6(relx, rely, divx, divy);
        } else {
            v = ((relx * divy) + divy) - 1 - rely;
        }

        String result = fastEncode(v, dc);

        if ((dc == 4) && (divx == xSide[4]) && (divy == ySide[4])) {
            result = String.valueOf(result.charAt(0)) + result.charAt(2) + result.charAt(1) + result.charAt(3);
        }

        rely = mapcoderData.getMapcoderRect().getMinY() + (rely * ygridsize);
        relx = mapcoderData.getMapcoderRect().getMinX() + (relx * xgridsize);

        final int dividery = ((ygridsize + ySide[codexlow]) - 1) / ySide[codexlow];
        final int dividerx = ((xgridsize + xSide[codexlow]) - 1) / xSide[codexlow];

        result += '.';

        // encoderelative

        final int nrchars = codexlow;

        int difx = pointToEncode.getLonMicroDeg() - relx;
        int dify = pointToEncode.getLatMicroDeg() - rely;

        final int extrax = difx % dividerx;
        final int extray = dify % dividery;

        difx = difx / dividerx;
        dify = dify / dividery;

        dify = ySide[nrchars] - 1 - dify;
        if (nrchars == 3) {
            result += encodeTriple(difx, dify);
        } else {

            String postfix = fastEncode(((difx) * ySide[nrchars]) + dify, nrchars);
            if (nrchars == 4) {
                postfix = String.valueOf(postfix.charAt(0)) + postfix.charAt(2) + postfix.charAt(1) + postfix.charAt(3);
            }
            result += postfix;
        }
        // encoderelative

        if (orgcodex == 14) {
            result = result.charAt(0) + "." + result.charAt(1) + result.substring(3);
        }

        result += addPostfix(extrax << 2, extray, dividerx << 2, dividery); // for encodeGrid

        return mapcoderData.getPipeLetter() + result;
    }

    private static String encodeStarpipe(final Point pointToEncode, final Data mapcoderData, final int thisindex) {
        final StringBuilder starpipe_result = new StringBuilder();
        final int thiscodexlen = mapcoderData.getCodexLen();
        boolean done = false;
        int storageStart = 0;

        // search back to first pipe star
        int firstindex = thisindex;
        while (Data.calcStarPipe(firstindex - 1) && (Data.calcCodexLen(firstindex - 1) == thiscodexlen)) {
            firstindex--;
        }

        int i = firstindex;
        while (true) {
            if (Data.calcCodexLen(i) != thiscodexlen) {
                return starpipe_result.toString();
            }

            mapcoderData.dataSetup(i);
            if (!done) {
                final int maxx = mapcoderData.getMapcoderRect().getMaxX();
                final int maxy = mapcoderData.getMapcoderRect().getMaxY();
                final int minx = mapcoderData.getMapcoderRect().getMinX();
                final int miny = mapcoderData.getMapcoderRect().getMinY();

                int h = ((maxy - miny) + 89) / 90;
                final int xdiv = xDivider(miny, maxy);
                int w = ((((maxx - minx) * 4) + xdiv) - 1) / xdiv;

                h = 176 * (((h + 176) - 1) / 176);
                w = 168 * (((w + 168) - 1) / 168);

                int product = (w / 168) * (h / 176) * 961 * 31;

                final int goodRounder = (mapcoderData.getCodex() >= 23) ? (961 * 961 * 31) : (961 * 961);
                if (mapcoderData.getPipeType() == 8) // *+
                {
                    product =
                            ((((storageStart + product + goodRounder) - 1) / goodRounder) * goodRounder) - storageStart;
                }

                if ((i == thisindex) && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                    final int dividerx = (((maxx - minx) + w) - 1) / w;
                    int vx = (pointToEncode.getLonMicroDeg() - minx) / dividerx;
                    final int extrax = (pointToEncode.getLonMicroDeg() - minx) % dividerx;
                    final int dividery = (((maxy - miny) + h) - 1) / h;
                    int vy = (maxy - pointToEncode.getLatMicroDeg()) / dividery;
                    final int extray = (maxy - pointToEncode.getLatMicroDeg()) % dividery;
                    final int spx = vx % 168;
                    final int spy = vy % 176;

                    vx = vx / 168;
                    vy = vy / 176;

                    // PIPELETTER ENCODE
                    final int value = (vx * (h / 176)) + vy;

                    starpipe_result.append(fastEncode((storageStart / (961 * 31)) + value,
                            mapcoderData.getCodexLen() - 2));
                    starpipe_result.append('.');
                    starpipe_result.append(encodeTriple(spx, spy));

                    starpipe_result.append(
                            addPostfix(extrax << 2, extray, dividerx << 2, dividery)); // for encodeStarpipe
                    done = true; // will be returned soon, but look for end
                    // of pipes
                }
                storageStart += product;
            }
            i++;
        }
    }

    // mid-level encode/decode
    private static String encodeNameless(final Point pointToEncode, final Data mapcoderData, final int index, final int firstcode)
    // returns "" in case of (argument) error
    {
        final int first_nameless_record =
                getFirstNamelessRecord(mapcoderData.getCodex(), index, firstcode);
        final int a = countCityCoordinatesForCountry(mapcoderData.getCodex(), index, firstcode);
        final int p = 31 / a;
        final int r = 31 % a;
        final int nrX = index - first_nameless_record;

        final int maxy = mapcoderData.getMapcoderRect().getMaxY();
        final int minx = mapcoderData.getMapcoderRect().getMinX();
        final int miny = mapcoderData.getMapcoderRect().getMinY();

        final int x = pointToEncode.getLonMicroDeg();
        final int y = pointToEncode.getLatMicroDeg();

        if (a > 1) {
            int storage_offset;

            if ((mapcoderData.getCodex() != 21) && (a <= 31)) {
                storage_offset = ((nrX * p) + ((nrX < r) ? nrX : r)) * (961 * 961);
            } else if ((mapcoderData.getCodex() != 21) && (a < 62)) {
                if (nrX < (62 - a)) {
                    storage_offset = nrX * 961 * 961;
                } else {
                    storage_offset = ((62 - a) + (((nrX - 62) + a) / 2)) * 961 * 961;
                    if (((nrX + a) & 1) != 0) {
                        storage_offset += 16 * 961 * 31;
                    }
                }
            } else {
                final int basePower = (mapcoderData.getCodex() == 21) ? (961 * 961) : (961 * 961 * 31);
                int basePowerA = basePower / a;
                if (a == 62) {
                    basePowerA++;
                } else {
                    basePowerA = 961 * (basePowerA / 961);
                }

                storage_offset = nrX * basePowerA;
            }

            int side = DataAccess.smartDiv(index);
            final int orgSide = side;
            int xSide = side;
            if (mapcoderData.isSpecialShape()) {
                xSide *= side;
                side = 1 + ((maxy - miny) / 90);
                xSide = xSide / side;
            }

            final int dividerx4 = xDivider(miny, maxy);
            // 4 times too large

            final int dx = (4 * (x - minx)) / dividerx4;
            // div with floating point value

            final int extrax4 = ((x - minx) * 4) - (dx * dividerx4); // like modulus, but with floating point value

            final int dividery = 90;
            final int dy = (maxy - y) / dividery;
            final int extray = (maxy - y) % dividery;
            int v = storage_offset;
            if (mapcoderData.isSpecialShape()) {
                v += encode6(dx, side - 1 - dy, xSide, side);
            } else {
                v += (dx * side) + dy;
            }

            String result = fastEncode(v, mapcoderData.getCodexLen() + 1);

            if (mapcoderData.getCodexLen() == 3) {
                result = result.substring(0, 2) + '.' + result.substring(2);
            } else if (mapcoderData.getCodexLen() == 4) {
                if ((mapcoderData.getCodex() == 22) && (a < 62) && (orgSide == 961) && !mapcoderData.isSpecialShape()) {
                    result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
                }
                if (mapcoderData.getCodex() == 13) {
                    result = result.substring(0, 2) + '.' + result.substring(2);
                } else {
                    result = result.substring(0, 3) + '.' + result.substring(3);
                }
            }
            result += addPostfix(extrax4, extray, dividerx4, dividery); // for encodeNameless

            return result;
        }
        return "";
    }

    static String aeuPack(final String argStr, final boolean argShort) {
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
                    return str; // not alldigit (or multiple dots)
                }
            }
        }

        if ((rlen - 2) > dotpos) {
            // does r have a dot, AND at least 2 chars
            // after the dot?
            if (argShort) {
                final int v = ((((int) str.charAt(0)) - 48) * 100) + ((((int) str.charAt(rlen - 2)) - 48) * 10) + (((int) str.charAt(rlen - 1)) - 48);
                return 'A' + str.substring(1, rlen - 2) + ENCODE_CHARS[v / 32] + ENCODE_CHARS[v % 32] + rest;
            } else {
                final int v = (((((int) str.charAt(rlen - 2)) - 48) * 10) + ((int) str.charAt(rlen - 1))) - 48;
                str = str.substring(0, rlen - 2) + ENCODE_CHARS[31 + (v / 34)] + ENCODE_CHARS[v % 34];
            }
        }
        return str + rest;
    }

    private static String fastEncode(final int argValue, final int argNrChars) {
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

    private static int encode6(final int x, final int y, final int width, final int height) {
        int d = 6;
        int col = x / 6;
        final int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            d = width - (maxcol * 6);
        }
        return ((height * 6 * col) + ((height - 1 - y) * d) + x) - (col * 6);
    }

    private static String encodeTriple(final int difx, final int dify) {
        if (dify < (4 * 34)) {
            return ENCODE_CHARS[((difx / 28) + (6 * (dify / 34)))] + fastEncode(((difx % 28) * 34) + (dify % 34), 2);
        } else {
            return ENCODE_CHARS[((difx / 24) + 24)] + fastEncode((((difx % 24) * 40) + dify) - 136, 2);
        }
    }
}
