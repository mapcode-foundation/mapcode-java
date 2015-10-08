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

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class the data class for Mapcode codex items.
 */
class Data {
    static final char[] ENCODE_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
            'A', 'E', 'U'
    };

    private Data() {
        // Disabled.
    }

    static boolean isNameless(final int territoryRecord) {
        return (DataAccess.getDataFlags(territoryRecord) & 64) != 0;
    }

    static boolean isSpecialShape(final int territoryRecord) {
        return (DataAccess.getDataFlags(territoryRecord) & 1024) != 0;
    }

    static int getTerritoryRecordType(final int territoryRecord) {
        return (DataAccess.getDataFlags(territoryRecord) >> 7) & 3; // 1=pipe 2=plus 3=star
    }

    static boolean isRestricted(final int territoryRecord) {
        return (DataAccess.getDataFlags(territoryRecord) & 512) != 0;
    }

    static int getCodex(final int territoryRecord) {
        final int codexflags = DataAccess.getDataFlags(territoryRecord) & 31;
        return (10 * (codexflags / 5)) + (codexflags % 5) + 1;
    }

    @Nonnull
    static String headerLetter(final int i) {
        final int flags = DataAccess.getDataFlags(i);
        if (((flags >> 7) & 3) == 1) {
            return Character.toString(ENCODE_CHARS[(flags >> 11) & 31]);
        }
        return "";
    }

    @Nonnull
    static Boundary getBoundary(final int territoryRecord) {
        return Boundary.createFromTerritoryRecord(territoryRecord);
    }
}
