/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public class MapcodeDecoder {
    
    private static final int ccode_earth = 540;
    
    @SuppressWarnings("CPD-START")
    private final static byte[] decode_chars = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, -2, 10, 11, 12, -3, 13, 14, 15,
            1, 16, 17, 18, 19, 20, 0, 21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1, -1, -1, -2, 10, 11,
            12, -3, 13, 14, 15, 1, 16, 17, 18, 19, 20, 0, 21, 22, 23, 24, 25, -4, 26, 27, 28, 29, 30, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
    @SuppressWarnings("CPD-END")
    
    public static Point master_decode(String mapcode, MapcodeTerritory mapcodeTerritory)
    // in case of error, result.isDefined() is false
    {
        Point result = new Point();
        String extrapostfix = "";

        int minpos = mapcode.indexOf('-');
        if (minpos > 0) {
            extrapostfix = mapcode.substring(minpos + 1).trim();
            mapcode = mapcode.substring(0, minpos);
        }

        mapcode = aeu_unpack(mapcode).trim();
        if (mapcode == "")
            return result; // failed to decode!

        int incodexlen = mapcode.length() - 1;

        // *** long codes in states are handled by the country
        if (incodexlen >= 9)
            mapcodeTerritory = MapcodeTerritory.AAA;
        else {
            MapcodeTerritory parentTerritory = mapcodeTerritory.getParent();
            if (incodexlen >= 8 && (parentTerritory == MapcodeTerritory.USA || parentTerritory == MapcodeTerritory.CAN
                    || parentTerritory == MapcodeTerritory.AUS || parentTerritory == MapcodeTerritory.BRA
                    || parentTerritory == MapcodeTerritory.CHN || parentTerritory == MapcodeTerritory.RUS)
                    || incodexlen >= 7 && (parentTerritory == MapcodeTerritory.IND || parentTerritory == MapcodeTerritory.MEX)) {

                mapcodeTerritory = parentTerritory;
            }
        }

        int ccode = mapcodeTerritory.getTerritoryCode();

        int from = MapcoderDataAccess.dataFirstRecord(ccode);
        if (MapcoderDataAccess.data_flags(from) == 0) {
            return new Point(); // this territory is not in the current data
        }
        int upto = MapcoderDataAccess.dataLastRecord(ccode);

        int incodexhi = mapcode.indexOf('.');

        MapcoderData mapcoderData = new MapcoderData();

        for (int i = from; i <= upto; i++) {
            mapcoderData.dataSetup(i);
            if (mapcoderData.getPipeType() == 0 && !mapcoderData.isNameless()
                    && mapcoderData.getCodexlen() == incodexlen && mapcoderData.getCodexHi() == incodexhi) {

                result = decode_grid(mapcode, mapcoderData.getMapcoderRect().getMinX(), mapcoderData.getMapcoderRect()
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
                        int xdiv8 = MapCodeCommon.x_divider(mapcoderData.getMapcoderRect().getMinY(), mapcoderData.getMapcoderRect().getMaxY()) / 4;
                        if (mapcoderData.getMapcoderRect().extendBounds(xdiv8, 45).containsPoint(result)) {
                            fitssomewhere = true;
                            break;
                        }
                    }
                    if (!fitssomewhere)
                        result.clear();
                }
                break;
            } else if (mapcoderData.getPipeType() == 4 && mapcoderData.getCodexlen() + 1 == incodexlen
                    && mapcoderData.getCodexHi() + 1 == incodexhi
                    && mapcoderData.getPipeLetter().charAt(0) == mapcode.charAt(0)) {
                result = decode_grid(mapcode.substring(1), mapcoderData.getMapcoderRect().getMinX(), mapcoderData
                        .getMapcoderRect().getMinY(), mapcoderData.getMapcoderRect().getMaxX(), mapcoderData
                        .getMapcoderRect().getMaxY(), i, extrapostfix);
                break;
            } else if (mapcoderData.isNameless()
                    && (mapcoderData.getCodex() == 21 && incodexlen == 4 && incodexhi == 2
                            || mapcoderData.getCodex() == 22 && incodexlen == 5 && incodexhi == 3 || mapcoderData
                            .getCodex() == 13 && incodexlen == 5 && incodexhi == 2)) {
                result = decode_nameless(mapcode, i, extrapostfix, mapcoderData);
                break;
            } else if (mapcoderData.getPipeType() > 4 && incodexlen == incodexhi + 3
                    && mapcoderData.getCodexlen() + 1 == incodexlen) {
                result = decode_starpipe(mapcode, i, extrapostfix, mapcoderData);
                break;
            }
        }

        if (result.isDefined()) {
            if (result.getLongitude() > 180000000) {
                result = new Point(result.getLongitude() - 360000000, result.getLatitude());
            }

            // LIMIT_TO_OUTRECT : make sure it fits the country
            if (ccode != ccode_earth) {
                MapcodeSubArea mapcoderRect = MapcodeSubArea.getArea(upto); // find
                                                                    // encompassing
                                                                    // rect
                int xdiv8 = MapCodeCommon.x_divider(mapcoderRect.getMinY(), mapcoderRect.getMaxY()) / 4; // should
                                                                                           // be
                                                                                           // /8
                                                                                           // but
                                                                                           // there's
                                                                                           // some
                                                                                           // extra
                                                                                           // margin
                if (!mapcoderRect.extendBounds(xdiv8, 45).containsPoint(result)) {
                    result.clear(); // decodes outside the official territory
                                    // limit
                }
            }
        }

        return result;
    } // master_decode

    private static Point decode_grid(String result, int minx, int miny, int maxx, int maxy, int m, String extrapostfix) // for
    // a
    // well-formed
    // result,
    // and
    // integer
    // variables
    {
        int relx, rely;
        int codexlen = result.length() - 1; // length ex dot
        int dc = result.indexOf('.'); // dotposition

        if (dc == 1 && codexlen == 5) {
            dc++;
            result = result.substring(0, 1) + result.charAt(2) + '.' + result.substring(3);
        }
        int codexlow = codexlen - dc;
        int codex = 10 * dc + codexlow;

        int divx, divy;
        divy = MapcoderDataAccess.smartdiv(m);
        if (divy == 1) {
            divx = MapCodeCommon.xside[dc];
            divy = MapCodeCommon.yside[dc];
        } else {
            divx = MapCodeCommon.nc[dc] / divy;
        }

        if (dc == 4 && divx == MapCodeCommon.xside[4] && divy == MapCodeCommon.yside[4]) {
            result = result.substring(0, 1) + result.charAt(2) + result.charAt(1) + result.substring(3);
        }

        int v = fast_decode(result);

        if (divx != divy && codex > 24) // D==6
        {
            Point d = decode6(v, divx, divy);
            relx = d.getLongitude();
            rely = d.getLatitude();
        } else {
            relx = v / divy;
            rely = v % divy;
            rely = divy - 1 - rely;
        }

        int ygridsize = (maxy - miny + divy - 1) / divy;
        int xgridsize = (maxx - minx + divx - 1) / divx;

        rely = miny + rely * ygridsize;
        relx = minx + relx * xgridsize;

        int dividery = (ygridsize + MapCodeCommon.yside[codexlow] - 1) / MapCodeCommon.yside[codexlow];
        int dividerx = (xgridsize + MapCodeCommon.xside[codexlow] - 1) / MapCodeCommon.xside[codexlow];

        String rest = result.substring(dc + 1);

        // decoderelative (postfix vs rely,relx)
        int difx;
        int dify;
        int nrchars = rest.length();

        if (nrchars == 3) {
            Point d = decode_triple(rest);
            difx = d.getLongitude();
            dify = d.getLatitude();
        } else {
            if (nrchars == 4)
                rest = "" + rest.charAt(0) + rest.charAt(2) + rest.charAt(1) + rest.charAt(3);
            v = fast_decode(rest);
            difx = v / MapCodeCommon.yside[nrchars];
            dify = v % MapCodeCommon.yside[nrchars];
        }

        dify = MapCodeCommon.yside[nrchars] - 1 - dify;

        int cornery = rely + dify * dividery;
        int cornerx = relx + difx * dividerx;
        return add2res(cornery, cornerx, dividerx << 2, dividery, 1, extrapostfix);
    }

    private static Point decode_nameless(String result, int firstrec, String extrapostfix, MapcoderData mapcoderData) {
        if (mapcoderData.getCodex() == 22) {
            result = result.substring(0, 3) + result.substring(4);
        } else {
            result = result.substring(0, 2) + result.substring(3);
        }

        int A = MapCodeCommon.count_city_coordinates_for_country(mapcoderData.getCodex(), firstrec, firstrec);
        if (A < 2)
            A = 1; // paranoia

        int p = 31 / A;
        int r = 31 % A;
        int v = 0;
        int X = -1;
        boolean swapletters = false;

        if (mapcoderData.getCodex() != 21 && A <= 31) {
            int offset = decode_chars[(int) result.charAt(0)];

            if (offset < r * (p + 1)) {
                X = offset / (p + 1);
            } else {
                swapletters = p == 1 && mapcoderData.getCodex() == 22;
                X = r + (offset - r * (p + 1)) / p;
            }
        } else if (mapcoderData.getCodex() != 21 && A < 62) {
            X = decode_chars[(int) result.charAt(0)];
            if (X < (62 - A)) {
                swapletters = mapcoderData.getCodex() == 22;
            } else {
                X = X + X - 62 + A;
            }
        } else // codex==21 || A>=62
        {
            int BASEPOWER = (mapcoderData.getCodex() == 21) ? 961 * 961 : 961 * 961 * 31;
            int BASEPOWERA = BASEPOWER / A;
            if (A == 62)
                BASEPOWERA++;
            else
                BASEPOWERA = 961 * (BASEPOWERA / 961);

            // decode and determine x
            v = fast_decode(result);
            X = v / BASEPOWERA;
            v %= BASEPOWERA;
        }

        if (swapletters && !MapcoderData.isSpecialShape(firstrec + X)) {
            result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
        }

        if (mapcoderData.getCodex() != 21 && A <= 31) {
            v = fast_decode(result);
            if (X > 0) {
                v -= (X * p + (X < r ? X : r)) * 961 * 961;
            }
        } else if (mapcoderData.getCodex() != 21 && A < 62) {
            v = fast_decode(result.substring(1));
            if (X >= (62 - A) && v >= (16 * 961 * 31)) {
                v -= 16 * 961 * 31;
                X++;
            }
        }

        if (X > A)
            return new Point(); // return undefined (past end!)
        mapcoderData.dataSetup(firstrec + X);

        int SIDE = MapcoderDataAccess.smartdiv(firstrec + X);
        int xSIDE = SIDE;

        int maxy = mapcoderData.getMapcoderRect().getMaxY();
        int minx = mapcoderData.getMapcoderRect().getMinX();
        int miny = mapcoderData.getMapcoderRect().getMinY();

        int dx, dy;

        if (mapcoderData.isSpecialShape()) {
            xSIDE *= SIDE;
            SIDE = 1 + (maxy - miny) / 90;
            xSIDE = xSIDE / SIDE;

            Point d = decode6(v, xSIDE, SIDE);
            dx = d.getLongitude();
            dy = SIDE - 1 - d.getLatitude();
        } else {
            dy = v % SIDE;
            dx = v / SIDE;
        }

        if (dx >= xSIDE) // else out-of-range!
            return new Point(); // return undefined (out of range!)

        int dividerx4 = MapCodeCommon.x_divider(miny, maxy); // 4 times too large!
        int dividery = 90;

        int cornerx = minx + (dx * dividerx4) / 4; // FIRST multiply, THEN
                                                   // divide!
        int cornery = maxy - dy * dividery;
        return add2res(cornery, cornerx, dividerx4, dividery, -1, extrapostfix);
    } // decode_nameless

    private static Point decode_starpipe(String input, int firstindex, String extrapostfix, MapcoderData mapcoderData) // returns
    // Point.isUndefined()
    // in
    // case
    // or
    // error
    {
        int STORAGE_START = 0;
        int thiscodexlen = mapcoderData.getCodexlen();

        int value = fast_decode(input); // decode top (before dot)
        value *= 961 * 31;
        Point triple = decode_triple(input.substring(input.length() - 3)); // decode
        // bottom
        // 3
        // chars

        int i;
        for (i = firstindex;; i++) {
            if (MapcoderData.codexLen(i) != thiscodexlen) {
                return new Point(); // return undefined
            }
            if (i > firstindex) {
                mapcoderData.dataSetup(i);
            }

            int maxx = mapcoderData.getMapcoderRect().getMaxX();
            int maxy = mapcoderData.getMapcoderRect().getMaxY();
            int minx = mapcoderData.getMapcoderRect().getMinX();
            int miny = mapcoderData.getMapcoderRect().getMinY();

            int H = (maxy - miny + 89) / 90;
            int xdiv = MapCodeCommon.x_divider(miny, maxy);
            int W = ((maxx - minx) * 4 + xdiv - 1) / xdiv;

            H = 176 * ((H + 176 - 1) / 176);
            W = 168 * ((W + 168 - 1) / 168);

            int product = (W / 168) * (H / 176) * 961 * 31;

            int GOODROUNDER = mapcoderData.getCodex() >= 23 ? 961 * 961 * 31 : 961 * 961;
            if (mapcoderData.getPipeType() == 8) // *+
                product = ((STORAGE_START + product + GOODROUNDER - 1) / GOODROUNDER) * GOODROUNDER - STORAGE_START;

            if (value >= STORAGE_START && value < STORAGE_START + product) // code
            // belongs
            // here?
            {
                int dividerx = (maxx - minx + W - 1) / W;
                int dividery = (maxy - miny + H - 1) / H;

                value -= STORAGE_START;
                value = value / (961 * 31);
                // PIPELETTER DECODE
                int vx = value / (H / 176);
                vx = vx * 168 + triple.getLongitude();
                int vy = (value % (H / 176)) * 176 + triple.getLatitude();

                int cornery = maxy - vy * dividery;
                int cornerx = minx + vx * dividerx;

                /*
                 * Sri Lanka Defect (v1.1) { int c1 = (zonedata == 0) ? -1 :
                 * decode_chars[(int) input .charAt(input.length() - 3)]; Point
                 * zd = addzonedata(cornery + (triple.getY() - 176) dividery,
                 * cornerx - triple.getX() * dividerx, 176 * dividery, 168 *
                 * dividerx, c1, dividerx, dividery); cornery = zd.getY();
                 * cornerx = zd.getX(); }
                 */

                Point retval = add2res(cornery, cornerx, dividerx << 2, dividery, -1, extrapostfix);
                if (retval.getLongitude() < minx || retval.getLongitude() >= maxx || retval.getLatitude() < miny || retval.getLatitude() > maxy)
                    retval.clear(); // return undefined
                return retval;
            }
            STORAGE_START += product;
        }
    } // decode_starpipe

    private static String aeu_unpack(String str)
    // unpack encoded into all-digit
    // (assume str already uppercase!), returns "" in case of error
    {
        boolean voweled = false;
        int lastpos = str.length() - 1;
        int dotpos = str.indexOf('.');
        if (dotpos < 2 || lastpos < dotpos + 2)
            return ""; // Error: no dot, or less than 2 letters before dot, or
            // less than 2 letters after dot

        if (str.charAt(0) == 'A') {
            voweled = true;
            str = str.substring(1);
            dotpos--;
        } else {
            int v = str.charAt(lastpos - 1);
            if (v == 'A')
                v = 0;
            else if (v == 'E')
                v = 34;
            else if (v == 'U')
                v = 68;
            else
                v = -1;
            if (v >= 0) {
                char e = str.charAt(lastpos);
                if (e == 'A')
                    v += 31;
                else if (e == 'E')
                    v += 32;
                else if (e == 'U')
                    v += 33;
                else {
                    int ve = decode_chars[(int) str.charAt(lastpos)];
                    if (ve < 0)
                        return "";
                    v += ve;
                }
                if (v >= 100)
                    return "";
                voweled = true;
                str = str.substring(0, lastpos - 1) + MapcoderData.encode_chars[v / 10]
                        + MapcoderData.encode_chars[v % 10];
            }
        }

        if (dotpos < 2 || dotpos > 5)
            return "";

        for (int v = 0; v <= lastpos; v++)
            if (v != dotpos)
                if (decode_chars[(int) str.charAt(v)] < 0)
                    return ""; // bad char!
                else if (voweled && decode_chars[(int) str.charAt(v)] > 9)
                    return ""; // nonodigit!

        return str;
    }

    private static Point decode_triple(String str) {
        final byte c1 = decode_chars[(int) str.charAt(0)];
        final int x = fast_decode(str.substring(1));
        if (c1 < 24) {
            return new Point((c1 % 6) * 28 + x / 34, c1 / 6 * 34 + x % 34);
        }
        return new Point(x / 40 + 24 * (c1 - 24), x % 40 + 136);
    }

    private static Point decode6(int v, int width, int height) {
        int D = 6;
        int col = v / (height * 6);
        int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            D = width - maxcol * 6;
        }
        int w = v - col * height * 6;
        return new Point(col * 6 + w % D, height - 1 - w / D);
    }

    // / lowest level encode/decode routines
    private static int fast_decode(String code)
    // decode up to dot or EOS;
    // returns negative in case of error
    {
        int value = 0;
        int i;
        for (i = 0; i < code.length(); i++) {
            int c = (int) code.charAt(i);
            if (c == 46) // dot!
                return value;
            if (decode_chars[c] < 0)
                return -1;
            value = value * 31 + decode_chars[c];
        }
        return value;
    }
    
    private static Point add2res(int y, int x, int dividerx4, int dividery, int ydirection, String extrapostfix) {
        if (extrapostfix.length() > 0) {
            int c1 = (int) extrapostfix.charAt(0);
            c1 = decode_chars[c1];
            if (c1 < 0)
                c1 = 0;
            else if (c1 > 29)
                c1 = 29;
            int y1 = c1 / 5;
            int x1 = c1 % 5;
            int c2 = (extrapostfix.length() == 2) ? (int) extrapostfix.charAt(1) : 72; // 72='H'=code
                                                                                       // 15=(3+2*6)
            c2 = decode_chars[c2];
            if (c2 < 0)
                c2 = 0;
            else if (c2 > 29)
                c2 = 29;
            int y2 = c2 / 6;
            int x2 = c2 % 6;

            int extrax = ((x1 * 12 + 2 * x2 + 1) * dividerx4 + 120) / 240;
            int extray = ((y1 * 10 + 2 * y2 + 1) * dividery + 30) / 60;

            return new Point(x + extrax, y + extray * ydirection);
        }
        return new Point(x + dividerx4 / 8, y + (dividery / 2) * ydirection);
    }

}
