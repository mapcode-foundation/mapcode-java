/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public class MapcoderData {
    private int flags;

    public int getFlags() {
        return flags;
    }

    private int codex;

    public int getCodex() {
        return codex;
    }

    private int codexlo;

    private int codexhi;

    public int getCodexHi() {
        return codexhi;
    }

    private int codexlen;

    public int getCodexlen() {
        return codexlen;
    }

    private boolean nameless;

    public boolean isNameless() {
        return nameless;
    }

    private boolean useless;

    public boolean isUseless() {
        return useless;
    }

    private boolean specialShape;

    public boolean isSpecialShape() {
        return specialShape;
    }

    private int pipetype;

    public int getPipeType() {
        return pipetype;
    }

    private String pipeLetter;

    public String getPipeLetter() {
        return pipeLetter;
    }

    private boolean starpipe;

    public boolean isStarpipe() {
        return starpipe;
    }

    private MapcodeSubArea mapcoderRect;

    public MapcodeSubArea getMapcoderRect() {
        return mapcoderRect;
    }

    public final static char[] encode_chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
            'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };

    public MapcoderData(int i) {
        dataSetup(i);
    }

    public MapcoderData() {
    }

    public void dataSetup(int i) {
        flags = MapcoderDataAccess.data_flags(i);
        codexhi = _codexhi(flags);
        codexlo = _codexlo(flags);
        codexlen = _codexlen(codexhi, codexlo);
        codex = _codex(codexhi, codexlo);
        nameless = isNameless(i);
        useless = (flags & 512) != 0;
        specialShape = isSpecialShape(i);
        pipetype = (flags >> 5) & 12; // 4=pipe 8=plus 12=star
        if (pipetype == 4) {
            pipeLetter = Character.toString(encode_chars[(flags >> 11) & 31]);
        } else {
            pipeLetter = "";
        }
        if (codex == 21 && !nameless) {
            codex++;
            codexlo++;
            codexlen++;
        }
        starpipe = isStarpipe(i);
        mapcoderRect = MapcodeSubArea.getArea(i);
    }

    public static boolean isNameless(int i) {
        return (MapcoderDataAccess.data_flags(i) & 64) != 0;
    }

    public static boolean isSpecialShape(int i) {
        return (MapcoderDataAccess.data_flags(i) & 1024) != 0;
    }

    public static int codex(int i) {
        int flags = MapcoderDataAccess.data_flags(i);
        return _codex(_codexhi(flags), _codexlo(flags));
    }

    public static int codexLen(int i) {
        int flags = MapcoderDataAccess.data_flags(i);
        return _codexlen(_codexhi(flags), _codexlo(flags));
    }

    public static boolean isStarpipe(int i) {
        return (MapcoderDataAccess.data_flags(i) & (8 << 5)) != 0;
    }

    private static int _codexhi(int flags) {
        return (flags & 31) / 5;
    }

    private static int _codexlo(int flags) {
        return (flags & 31) % 5 + 1;
    }

    private static int _codex(int codexhi, int codexlo) {
        return 10 * codexhi + codexlo;
    }

    private static int _codexlen(int codexhi, int codexlo) {
        return codexhi + codexlo;
    }
}
