/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import java.util.ArrayList;

public class MapcodeEncoder {

    private final static char[] encode_chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
            'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };

    public static ArrayList<String> master_encode(double orgy, double orgx, MapcodeTerritory encodeTerritory,
            boolean isrecursive, boolean stop_with_one_result, boolean allowworld) {
        return master_encode(orgy, orgx, encodeTerritory, isrecursive, stop_with_one_result, allowworld, null);
    }

    private static ArrayList<String> master_encode(double orgy, double orgx, MapcodeTerritory encodeTerritory,
            boolean isrecursive, boolean stop_with_one_result, boolean allowworld, MapcodeTerritory state_override) {
        Point pointToEncode = new Point(orgx, orgy);
        
        ArrayList<MapcodeSubArea> areas = MapcodeSubArea.getAreasForPoint(pointToEncode);

        ArrayList<String> results = new ArrayList<String>();
        
        if (orgy > 90)
            orgy -= 180;
        else if (orgy < -90)
            orgy += 180;
        if (orgx > 179.999999)
            orgx -= 360;
        else if (orgx < -180)
            orgx += 180;

        for (MapcodeSubArea mapcodeSubArea : areas) {
            if (encodeTerritory != null && mapcodeSubArea.getParentTerritory() != encodeTerritory) {
                continue;
            }
            
            MapcodeTerritory currentEncodeTerritory = mapcodeSubArea.getParentTerritory();
            
            if (currentEncodeTerritory == MapcodeTerritory.AAA && !allowworld && encodeTerritory != MapcodeTerritory.AAA) {
                continue;
            }
            
            int from = MapcoderDataAccess.dataFirstRecord(currentEncodeTerritory.getTerritoryCode());
            MapcoderData mapcoderData = new MapcoderData(from);
            if (mapcoderData.getFlags() == 0)
                continue;
            int upto = MapcoderDataAccess.dataLastRecord(currentEncodeTerritory.getTerritoryCode());

            
            int i = mapcodeSubArea.getSubAreaID();
            mapcoderData.dataSetup(i);
            if (mapcoderData.getCodex() < 54 && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                String r = "";
                MapcodeTerritory territoryParent = currentEncodeTerritory.getParent();
                if (mapcoderData.isUseless() && i == upto && territoryParent != null) {
                    
                    if (!isrecursive) {
                        state_override = currentEncodeTerritory;
                        results.addAll(master_encode(orgy, orgx, territoryParent, true, stop_with_one_result,
                                allowworld, state_override));
                        state_override = null;
                    }
                    
                    continue;
                } else if (mapcoderData.getPipeType() == 0 && !mapcoderData.isNameless()) {
                    if (!mapcoderData.isUseless() || results.size() != 0) {
                        // RESTRICTUSELESS : ignore! nothing was found yet
                        // in non-useless records!
                        r = encode_grid(i, pointToEncode, mapcoderData);
                    }
                } else if (mapcoderData.getPipeType() == 4) {
                    r = encode_grid(i, pointToEncode, mapcoderData);
                } else if (mapcoderData.isNameless()) { // auto-pipe 21/22
                    r = encode_nameless(pointToEncode, mapcoderData, i, from);
                } else { // pipe star, pipe plus
                    r = encode_starpipe(pointToEncode, mapcoderData, i);
                }

                if (r.length() > 4) {
                    r = aeu_pack(r);

                    MapcodeTerritory storecode = currentEncodeTerritory;
                    if (state_override != null)
                        storecode = state_override;

                    results.add(r + "/" + storecode.getTerritoryCode());

                    if (stop_with_one_result)
                        return results;

                }
            } // in rect
        }

        return results;
    }

    private static String encode_grid(int m, Point pointToEncode, MapcoderData mapcoderData) {
        int codex = mapcoderData.getCodex();
        int orgcodex = codex;
        if (codex == 14)
            codex = 23;
        int dc = codex / 10;
        int codexlow = codex % 10;
        int divx, divy = MapcoderDataAccess.smartdiv(m);
        if (divy == 1) {
            divx = MapCodeCommon.xside[dc];
            divy = MapCodeCommon.yside[dc];
        } else
            divx = MapCodeCommon.nc[dc] / divy;

        int ygridsize = (mapcoderData.getMapcoderRect().getMaxY() - mapcoderData.getMapcoderRect().getMinY() + divy - 1)
                / divy;
        int rely = pointToEncode.getLatitude() - mapcoderData.getMapcoderRect().getMinY();
        rely = rely / ygridsize;
        int xgridsize = (mapcoderData.getMapcoderRect().getMaxX() - mapcoderData.getMapcoderRect().getMinX() + divx - 1)
                / divx;

        int relx = pointToEncode.getLongitude() - mapcoderData.getMapcoderRect().getMinX();
        if (relx < 0) {
            pointToEncode = new Point(pointToEncode.getLongitude() + 360000000, pointToEncode.getLatitude());
            relx += 360000000;
        }
        if (relx < 0)
            return "";
        relx = relx / xgridsize;
        if (relx >= divx)
            return "";

        int v;
        if (divx != divy && codex > 24) // D==6
        {
            v = encode6(relx, rely, divx, divy);
        } else {
            v = relx * divy + divy - 1 - rely;
        }

        String result = fast_encode(v, dc);

        if (dc == 4 && divx == MapCodeCommon.xside[4] && divy == MapCodeCommon.yside[4])
            result = "" + result.charAt(0) + result.charAt(2) + result.charAt(1) + result.charAt(3);

        rely = mapcoderData.getMapcoderRect().getMinY() + rely * ygridsize;
        relx = mapcoderData.getMapcoderRect().getMinX() + relx * xgridsize;

        int dividery = (ygridsize + MapCodeCommon.yside[codexlow] - 1) / MapCodeCommon.yside[codexlow];
        int dividerx = (xgridsize + MapCodeCommon.xside[codexlow] - 1) / MapCodeCommon.xside[codexlow];

        result += '.';

        // encoderelative

        int nrchars = codexlow;

        int difx = pointToEncode.getLongitude() - relx;
        int dify = pointToEncode.getLatitude() - rely;
        difx = difx / dividerx;
        dify = dify / dividery;

        dify = MapCodeCommon.yside[nrchars] - 1 - dify;
        if (nrchars == 3) {
            result += encode_triple(difx, dify);
        } else {

            String postfix = fast_encode((difx) * MapCodeCommon.yside[nrchars] + dify, nrchars);
            if (nrchars == 4) {
                postfix = "" + postfix.charAt(0) + postfix.charAt(2) + postfix.charAt(1) + postfix.charAt(3);
            }
            result += postfix;
        }
        // encoderelative

        if (orgcodex == 14) {
            result = result.charAt(0) + "." + result.charAt(1) + result.substring(3);
        }

        return mapcoderData.getPipeLetter() + result;
    }

    private static String encode_starpipe(Point pointToEncode, MapcoderData mapcoderData, int thisindex) {
        String starpipe_result;
        int thiscodexlen = mapcoderData.getCodexlen();
        boolean done = false;
        int STORAGE_START = 0;

        // search back to first pipe star
        int firstindex = thisindex;
        while (MapcoderData.isStarpipe(firstindex - 1) && MapcoderData.codexLen(firstindex - 1) == thiscodexlen)
            firstindex--;

        starpipe_result = "";

        for (int i = firstindex;; i++) {
            if (MapcoderData.codexLen(i) != thiscodexlen) {
                return starpipe_result;
            }

            mapcoderData.dataSetup(i);
            if (!done) {
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

                if (i == thisindex && mapcoderData.getMapcoderRect().containsPoint(pointToEncode)) {
                    int dividerx = (maxx - minx + W - 1) / W;
                    int vx = (pointToEncode.getLongitude() - minx) / dividerx;
                    int dividery = (maxy - miny + H - 1) / H;
                    int vy = (maxy - pointToEncode.getLatitude()) / dividery;
                    int spx = vx % 168;
                    int spy = vy % 176;

                    vx = vx / 168;
                    vy = vy / 176;

                    // PIPELETTER ENCODE
                    int value = vx * (H / 176) + vy;

                    starpipe_result = fast_encode(STORAGE_START / (961 * 31) + value, mapcoderData.getCodexlen() - 2);
                    starpipe_result += ".";
                    starpipe_result += encode_triple(spx, spy);

                    done = true; // will be returned soon, but look for end
                                 // of pipes
                }
                STORAGE_START += product;

            } // !done
        } // for i
    }

    // mid-level encode/decode
    private static String encode_nameless(Point pointToEncode, MapcoderData mapcoderData, int index, int firstcode)
    // returns "" in case of (argument) error
    {
        int first_nameless_record = MapCodeCommon.get_first_nameless_record(mapcoderData.getCodex(), index, firstcode);
        int A = MapCodeCommon.count_city_coordinates_for_country(mapcoderData.getCodex(), index, firstcode);
        int p = 31 / A;
        int r = 31 % A;
        int X = index - first_nameless_record;

        int maxy = mapcoderData.getMapcoderRect().getMaxY();
        int minx = mapcoderData.getMapcoderRect().getMinX();
        int miny = mapcoderData.getMapcoderRect().getMinY();

        int x = pointToEncode.getLongitude();
        int y = pointToEncode.getLatitude();

        if (A > 1) {
            int storage_offset = 0;

            if (mapcoderData.getCodex() != 21 && A <= 31) {
                storage_offset = (X * p + (X < r ? X : r)) * (961 * 961);
            } else if (mapcoderData.getCodex() != 21 && A < 62) {
                if (X < 62 - A) {
                    storage_offset = X * 961 * 961;
                } else {
                    storage_offset = (62 - A + (X - 62 + A) / 2) * 961 * 961;
                    if (((X + A) & 1) != 0) {
                        storage_offset += 16 * 961 * 31;
                    }
                }
            } else {
                int BASEPOWER = (mapcoderData.getCodex() == 21) ? 961 * 961 : 961 * 961 * 31;
                int BASEPOWERA = BASEPOWER / A;
                if (A == 62)
                    BASEPOWERA++;
                else
                    BASEPOWERA = 961 * (BASEPOWERA / 961);

                storage_offset = X * BASEPOWERA;
            }

            int SIDE = MapcoderDataAccess.smartdiv(index);
            int orgSIDE = SIDE;
            int xSIDE = SIDE;
            if (mapcoderData.isSpecialShape()) {
                xSIDE *= SIDE;
                SIDE = 1 + (maxy - miny) / 90;
                xSIDE = xSIDE / SIDE;
            }

            int dividerx4 = MapCodeCommon.x_divider(miny, maxy);
            // 4 times too large

            int dx = (4 * (x - minx)) / dividerx4;
            // div with floating point value

            int dividery = 90;
            int dy = (maxy - y) / dividery;
            int v = storage_offset;
            if (mapcoderData.isSpecialShape())
                v += encode6(dx, SIDE - 1 - dy, xSIDE, SIDE);
            else
                v += dx * SIDE + dy;

            String result = fast_encode(v, mapcoderData.getCodexlen() + 1);

            if (mapcoderData.getCodexlen() == 3) {
                result = result.substring(0, 2) + '.' + result.substring(2);
            } else if (mapcoderData.getCodexlen() == 4) {
                if (mapcoderData.getCodex() == 22 && A < 62 && orgSIDE == 961 && !mapcoderData.isSpecialShape())
                    result = result.substring(0, 2) + result.charAt(3) + result.charAt(2) + result.charAt(4);
                if (mapcoderData.getCodex() == 13)
                    result = result.substring(0, 2) + '.' + result.substring(2);
                else
                    result = result.substring(0, 3) + '.' + result.substring(3);
            }

            return result;
        }
        return "";
    }

    private static String aeu_pack(String r) {
        int dotpos = -9;
        int rlen = r.length();
        int d;
        String rest = "";
        for (d = 0; d < rlen; d++)
            if (r.charAt(d) < '0' || r.charAt(d) > '9') // not digit?
                if (r.charAt(d) == '.' && dotpos < 0) // first dot?
                    dotpos = d;
                else if (r.charAt(d) == '-') {
                    rest = r.substring(d);
                    r = r.substring(0, d);
                    rlen = d;
                } else
                    return r; // not alldigit (or multiple dots)

        if (rlen - 2 > dotpos) { // does r have a dot, AND at least 2 chars
                                 // after the dot?
            int v = (((int) r.charAt(rlen - 2)) - 48) * 10 + ((int) r.charAt(rlen - 1)) - 48;
            int last = v % 34;
            char[] vowels = { 'A', 'E', 'U' };
            r = r.substring(0, rlen - 2) + vowels[v / 34] + (last < 31 ? encode_chars[last] : vowels[last - 31]);
        }
        return r + rest;
    }

    private static String fast_encode(int value, int nrchars) {
        String result = "";
        while (nrchars-- > 0) {
            result = encode_chars[value % 31] + result;
            value = value / 31;
        }
        return result;
    }

    private static int encode6(int x, int y, int width, int height) {
        int D = 6;
        int col = x / 6;
        int maxcol = (width - 4) / 6;
        if (col >= maxcol) {
            col = maxcol;
            D = width - maxcol * 6;
        }
        return height * 6 * col + (height - 1 - y) * D + x - col * 6;
    }
    
    private static String encode_triple(int difx, int dify) {
        if (dify < 4 * 34)
            return encode_chars[difx / 28 + 6 * (dify / 34)] + fast_encode((difx % 28) * 34 + dify % 34, 2);
        else
            return encode_chars[difx / 24 + 24] + fast_encode((difx % 24) * 40 + dify - 136, 2);
    }

}
