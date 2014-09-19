/*
 * Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import static com.mapcode.Common.GSON;
import static com.mapcode.Common.countCityCoordinatesForCountry;
import static com.mapcode.Common.getFirstNamelessRecord;
import static com.mapcode.Common.nc;
import static com.mapcode.Common.xDivider;
import static com.mapcode.Common.xSide;
import static com.mapcode.Common.ySide;

class Encoder {
    private static final Logger LOG = LoggerFactory.getLogger(Encoder.class);

    // ----------------------------------------------------------------------
    // Method called from public Java API.
    // ----------------------------------------------------------------------

    @Nonnull
    public static List<MapcodeInfo> encode(
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

    private final static char[] encode_chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
        'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'};

    @Nonnull
    private static List<MapcodeInfo> encode(double latDeg, double lonDeg, @Nullable final Territory territory,
        final boolean isRecursive, final boolean limitToOneResult, final boolean allowWorld,
        @Nullable Territory stateOverride) {
        LOG.trace("encode: latDeg={}, lonDeg={}, territory={}, isRecursive={}, limitToOneResult={}, allowWorld={}",
            latDeg, lonDeg, (territory == null) ? null : territory.name(), isRecursive, limitToOneResult, allowWorld);

        if (latDeg > 90) {
            latDeg -= 180;
        }
        else if (latDeg < -90) {
            latDeg += 180;
        }
        if (lonDeg > 179.999999) {
            lonDeg -= 360;
        }
        else if (lonDeg < -180) {
            lonDeg += 180;
        }

        final Point pointToEncode = Point.fromDeg(latDeg, lonDeg);
        final List<SubArea> areas = SubArea.getAreasForPoint(pointToEncode);
        final List<MapcodeInfo> results = new ArrayList<MapcodeInfo>();

        int lastbasesubareaID = -1;

        for (final SubArea subArea : areas) {
            if ((territory != null) && (subArea.getParentTerritory() != territory)) {
                continue;
            }

            final Territory currentEncodeTerritory = subArea.getParentTerritory();

            if (currentEncodeTerritory == Territory.AAA && !allowWorld &&
                territory != Territory.AAA) {
                continue;
            }

            final int from = DataAccess.dataFirstRecord(currentEncodeTerritory.getTerritoryCode());
            final Data mapcoderData = new Data(from);
            if (mapcoderData.getFlags() == 0) {
                continue;
            }
            final int upto = DataAccess.dataLastRecord(currentEncodeTerritory.getTerritoryCode());


            final int i = subArea.getSubAreaID();
            mapcoderData.dataSetup(i);
            if (mapcoderData.getCodex() < 54 && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                String mapcode = "";
                final Territory territoryParent = currentEncodeTerritory.getParentTerritory();
                if (mapcoderData.isUseless() && i == upto && territoryParent != null) {

                    if (!isRecursive) {
                        stateOverride = currentEncodeTerritory;
                        results.addAll(encode(latDeg, lonDeg, territoryParent, true, limitToOneResult,
                            allowWorld, stateOverride));
                        stateOverride = null;
                    }

                    continue;
                }
                if (mapcoderData.getPipeType() == 0 && !mapcoderData.isNameless()) {
                    if (!mapcoderData.isUseless() || lastbasesubareaID == from) {
                        mapcode = encodeGrid(i, pointToEncode, mapcoderData);
                    }
                }
                else if (mapcoderData.getPipeType() == 4) {
                    mapcode = encodeGrid(i, pointToEncode, mapcoderData);
                }
                else if (mapcoderData.isNameless()) { // auto-pipe 21/22
                    mapcode = encodeNameless(pointToEncode, mapcoderData, i, from);
                }
                else { // pipe star, pipe plus
                    mapcode = encodeStarpipe(pointToEncode, mapcoderData, i);
                }

                if (mapcode.length() > 4) {
                    mapcode = aeuPack(mapcode);

                    Territory encodeTerritory = currentEncodeTerritory;
                    if (stateOverride != null) {
                        encodeTerritory = stateOverride;
                    }

                    // Create new result.
                    final MapcodeInfo newResult = new MapcodeInfo(mapcode, encodeTerritory);

                    // The result should not be stored yet.
                    if (!results.contains(newResult)) {
                        results.add(newResult);
                    }
                    else {
                        // TODO: This should probably be simply an assertion instead.
                        LOG.error("encode: Duplicate results found, newResult={}, results={}",
                            GSON.toJson(newResult), GSON.toJson(results));
                    }

                    lastbasesubareaID = from;
                    if (limitToOneResult) {
                        return results;
                    }
                }
            } // in rect
        }
        LOG.trace("encode: isRecursive={}, results={}", isRecursive, GSON.toJson(results));
        LOG.trace("");
        return results;
    }

    private static String encodeGrid(final int m, Point pointToEncode, final Data mapcoderData) {
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
        }
        else {
            divx = nc[dc] / divy;
        }

        final int ygridsize =
            (mapcoderData.getMapcoderRect().getMaxY() - mapcoderData.getMapcoderRect().getMinY() + divy - 1)
                / divy;
        int rely = pointToEncode.getLatMicroDeg() - mapcoderData.getMapcoderRect().getMinY();
        rely = rely / ygridsize;
        final int xgridsize =
            (mapcoderData.getMapcoderRect().getMaxX() - mapcoderData.getMapcoderRect().getMinX() + divx - 1)
                / divx;

        int relx = pointToEncode.getLonMicroDeg() - mapcoderData.getMapcoderRect().getMinX();
        if (relx < 0) {
            pointToEncode =
                Point.fromMicroDeg(pointToEncode.getLatMicroDeg(), pointToEncode.getLonMicroDeg() + 360000000);
            relx += 360000000;
        } else if (relx > 360000000) {
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
        if (divx != divy && codex > 24) // D==6
        {
            v = encode6(relx, rely, divx, divy);
        }
        else {
            v = relx * divy + divy - 1 - rely;
        }

        String result = fastEncode(v, dc);

        if (dc == 4 && divx == xSide[4] && divy == ySide[4]) {
            result = String.valueOf(result.charAt(0)) + result.charAt(2) + result.charAt(1) + result.charAt(3);
        }

        rely = mapcoderData.getMapcoderRect().getMinY() + rely * ygridsize;
        relx = mapcoderData.getMapcoderRect().getMinX() + relx * xgridsize;

        final int dividery = (ygridsize + ySide[codexlow] - 1) / ySide[codexlow];
        final int dividerx = (xgridsize + xSide[codexlow] - 1) / xSide[codexlow];

        result += '.';

        // encoderelative

        final int nrchars = codexlow;

        int difx = pointToEncode.getLonMicroDeg() - relx;
        int dify = pointToEncode.getLatMicroDeg() - rely;
        difx = difx / dividerx;
        dify = dify / dividery;

        dify = ySide[nrchars] - 1 - dify;
        if (nrchars == 3) {
            result += encodeTriple(difx, dify);
        }
        else {

            String postfix = fastEncode((difx) * ySide[nrchars] + dify, nrchars);
            if (nrchars == 4) {
                postfix = String.valueOf(postfix.charAt(0)) + postfix.charAt(2) + postfix.charAt(1) + postfix.charAt(3);
            }
            result += postfix;
        }
        // encoderelative

        if (orgcodex == 14) {
            result = result.charAt(0) + "." + result.charAt(1) + result.substring(3);
        }

        return mapcoderData.getPipeLetter() + result;
    }

    private static String encodeStarpipe(final Point pointToEncode, final Data mapcoderData, final int thisindex) {
        final StringBuilder starpipe_result = new StringBuilder();
        final int thiscodexlen = mapcoderData.getCodexLen();
        boolean done = false;
        int storageStart = 0;

        // search back to first pipe star
        int firstindex = thisindex;
        while (Data.calcStarPipe(firstindex - 1) && Data.calcCodexLen(firstindex - 1) == thiscodexlen) {
            firstindex--;
        }

        for (int i = firstindex; ; i++) {
            if (Data.calcCodexLen(i) != thiscodexlen) {
                return starpipe_result.toString();
            }

            mapcoderData.dataSetup(i);
            if (!done) {
                final int maxx = mapcoderData.getMapcoderRect().getMaxX();
                final int maxy = mapcoderData.getMapcoderRect().getMaxY();
                final int minx = mapcoderData.getMapcoderRect().getMinX();
                final int miny = mapcoderData.getMapcoderRect().getMinY();

                int h = (maxy - miny + 89) / 90;
                final int xdiv = xDivider(miny, maxy);
                int w = ((maxx - minx) * 4 + xdiv - 1) / xdiv;

                h = 176 * ((h + 176 - 1) / 176);
                w = 168 * ((w + 168 - 1) / 168);

                int product = (w / 168) * (h / 176) * 961 * 31;

                final int goodRounder = mapcoderData.getCodex() >= 23 ? 961 * 961 * 31 : 961 * 961;
                if (mapcoderData.getPipeType() == 8) // *+
                {
                    product = ((storageStart + product + goodRounder - 1) / goodRounder) * goodRounder - storageStart;
                }

                if (i == thisindex && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                    final int dividerx = (maxx - minx + w - 1) / w;
                    int vx = (pointToEncode.getLonMicroDeg() - minx) / dividerx;
                    final int dividery = (maxy - miny + h - 1) / h;
                    int vy = (maxy - pointToEncode.getLatMicroDeg()) / dividery;
                    final int spx = vx % 168;
                    final int spy = vy % 176;

                    vx = vx / 168;
                    vy = vy / 176;

                    // PIPELETTER ENCODE
                    final int value = vx * (h / 176) + vy;

                    starpipe_result.append(fastEncode(storageStart / (961 * 31) + value,
                        mapcoderData.getCodexLen() - 2));
                    starpipe_result.append('.');
                    starpipe_result.append(encodeTriple(spx, spy));

                    done = true; // will be returned soon, but look for end
                    // of pipes
                }
                storageStart += product;

            } // !done
        } // for i
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

            if (mapcoderData.getCodex() != 21 && a <= 31) {
                storage_offset = (nrX * p + (nrX < r ? nrX : r)) * (961 * 961);
            }
            else if (mapcoderData.getCodex() != 21 && a < 62) {
                if (nrX < 62 - a) {
                    storage_offset = nrX * 961 * 961;
                }
                else {
                    storage_offset = (62 - a + (nrX - 62 + a) / 2) * 961 * 961;
                    if (((nrX + a) & 1) != 0) {
                        storage_offset += 16 * 961 * 31;
                    }
                }
            }
            else {
                final int basePower = (mapcoderData.getCodex() == 21) ? 961 * 961 : 961 * 961 * 31;
                int basePowerA = basePower / a;
                if (a == 62) {
                    basePowerA++;
                }
                else {
                    basePowerA = 961 * (basePowerA / 961);
                }

                storage_offset = nrX * basePowerA;
            }

            int side = DataAccess.smartDiv(index);
            final int orgSide = side;
            int xSide = side;
            if (mapcoderData.isSpecialShape()) {
                xSide *= side;
                side = 1 + (maxy - miny) / 90;
                xSide = xSide / side;
            }

            final int dividerx4 = xDivider(miny, maxy);
            // 4 times too large

            final int dx = (4 * (x - minx)) / dividerx4;
            // div with floating point value

            final int dividery = 90;
            final int dy = (maxy - y) / dividery;
            int v = storage_offset;
            if (mapcoderData.isSpecialShape()) {
                v += encode6(dx, side - 1 - dy, xSide, side);
            }
            else {
                v += dx * side + dy;
            }

            String result = fastEncode(v, mapcoderData.getCodexLen() + 1);

            if (mapcoderData.getCodexLen() == 3) {
                result = result.substring(0, 2) + '.' + result.substring(2);
            }
            else if (mapcoderData.getCodexLen() == 4) {
                if (mapcoderData.getCodex() == 22 && a < 62 && orgSide == 961 && !mapcoderData.isSpecialShape()) {
                    result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
                }
                if (mapcoderData.getCodex() == 13) {
                    result = result.substring(0, 2) + '.' + result.substring(2);
                }
                else {
                    result = result.substring(0, 3) + '.' + result.substring(3);
                }
            }

            return result;
        }
        return "";
    }

    private static String aeuPack(String r) {
        int dotpos = -9;
        int rlen = r.length();
        int d;
        String rest = "";
        for (d = 0; d < rlen; d++) {
            if (r.charAt(d) < '0' || r.charAt(d) > '9') // not digit?
            {
                if (r.charAt(d) == '.' && dotpos < 0) // first dot?
                {
                    dotpos = d;
                }
                else if (r.charAt(d) == '-') {
                    rest = r.substring(d);
                    r = r.substring(0, d);
                    rlen = d;
                }
                else {
                    return r; // not alldigit (or multiple dots)
                }
            }
        }

        if (rlen - 2 > dotpos) {
            // does r have a dot, AND at least 2 chars
            // after the dot?
            final int v = (((int) r.charAt(rlen - 2)) - 48) * 10 + ((int) r.charAt(rlen - 1)) - 48;
            final int last = v % 34;
            final char[] vowels = {'A', 'E', 'U'};
            r = r.substring(0, rlen - 2) + vowels[v / 34] + (last < 31 ? encode_chars[last] : vowels[last - 31]);
        }
        return r + rest;
    }

    private static String fastEncode(int value, int nrchars) {
        StringBuffer result = new StringBuffer();
        while (nrchars > 0) {
            nrchars--;
            result.insert(0, encode_chars[value % 31]);
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
            d = width - maxcol * 6;
        }
        return height * 6 * col + (height - 1 - y) * d + x - col * 6;
    }

    private static String encodeTriple(final int difx, final int dify) {
        if (dify < 4 * 34) {
            return encode_chars[difx / 28 + 6 * (dify / 34)] + fastEncode((difx % 28) * 34 + dify % 34, 2);
        }
        else {
            return encode_chars[difx / 24 + 24] + fastEncode((difx % 24) * 40 + dify - 136, 2);
        }
    }
}
