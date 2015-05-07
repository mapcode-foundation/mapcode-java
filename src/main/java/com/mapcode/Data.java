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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class the data class for Mapcode codex items.
 */
class Data {
    static final char[] ENCODE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
            'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'};

    private int flags;
    private int codex;
    private int codexLo;
    private int codexHi;
    private int codexLen;
    private boolean nameless;
    private boolean useless;
    private boolean specialShape;
    private int pipeType;
    @Nullable
    private String pipeLetter;
    private boolean starPipe;
    @Nullable
    private SubArea mapcoderRect;
    private boolean initialized;

    int getFlags() {
        assert initialized;
        return flags;
    }

    int getCodex() {
        assert initialized;
        return codex;
    }

    int getCodexLo() {
        assert initialized;
        return codexLo;
    }

    int getCodexHi() {
        assert initialized;
        return codexHi;
    }

    int getCodexLen() {
        assert initialized;
        return codexLen;
    }

    boolean isNameless() {
        assert initialized;
        return nameless;
    }

    boolean isUseless() {
        assert initialized;
        return useless;
    }

    boolean isSpecialShape() {
        assert initialized;
        return specialShape;
    }

    int getPipeType() {
        assert initialized;
        return pipeType;
    }

    @Nonnull
    String getPipeLetter() {
        assert initialized;
        assert pipeLetter != null;
        return pipeLetter;
    }

    boolean isStarPipe() {
        assert initialized;
        return starPipe;
    }

    @Nonnull
    SubArea getMapcoderRect() {
        assert initialized;
        assert mapcoderRect != null;
        return mapcoderRect;
    }

    Data(final int i) {
        dataSetup(i);
    }

    Data() {
        initialized = false;
    }

    void dataSetup(final int i) {
        flags = DataAccess.dataFlags(i);
        codexHi = calcCodexHi(flags);
        codexLo = calcCodexLo(flags);
        codexLen = calcCodexLen(codexHi, codexLo);
        codex = calcCodex(codexHi, codexLo);
        nameless = isNameless(i);
        useless = (flags & 512) != 0;
        specialShape = isSpecialShape(i);
        pipeType = (flags >> 5) & 12; // 4=pipe 8=plus 12=star
        if (pipeType == 4) {
            pipeLetter = Character.toString(ENCODE_CHARS[(flags >> 11) & 31]);
        } else {
            pipeLetter = "";
        }
        if ((codex == 21) && !nameless) {
            codex++;
            codexLo++;
            codexLen++;
        }
        starPipe = calcStarPipe(i);
        mapcoderRect = SubArea.getArea(i);
        initialized = true;
    }

    static boolean isNameless(final int i) {
        return (DataAccess.dataFlags(i) & 64) != 0;
    }

    static boolean isSpecialShape(final int i) {
        return (DataAccess.dataFlags(i) & 1024) != 0;
    }

    static int calcCodex(final int i) {
        final int flags = DataAccess.dataFlags(i);
        return calcCodex(calcCodexHi(flags), calcCodexLo(flags));
    }

    static int calcCodexLen(final int i) {
        final int flags = DataAccess.dataFlags(i);
        return calcCodexLen(calcCodexHi(flags), calcCodexLo(flags));
    }

    static boolean calcStarPipe(final int i) {
        return (DataAccess.dataFlags(i) & (8 << 5)) != 0;
    }

    private static int calcCodexHi(final int flags) {
        return (flags & 31) / 5;
    }

    private static int calcCodexLo(final int flags) {
        return ((flags & 31) % 5) + 1;
    }

    private static int calcCodex(final int codexhi, final int codexlo) {
        return (10 * codexhi) + codexlo;
    }

    private static int calcCodexLen(final int codexhi, final int codexlo) {
        return codexhi + codexlo;
    }
}
