/*
 * Copyright (C) 2016-2020, Stichting Mapcode Foundation (http://www.mapcode.com)
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

import javax.annotation.Nonnull;

// ----------------------------------------------------------------------------------------------
// Package private implementation class. For internal use within the Mapcode implementation only.
// ----------------------------------------------------------------------------------------------

/**
 * This class the data class for Mapcode codex items.
 */
@SuppressWarnings("MagicNumber")
final class Data {

    // TODO: Need explanation what this is and how this is used.
    static final char[] ENCODE_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',           // Numerals.
            'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M',           // Consonants.
            'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
            'A', 'E', 'U'                                               // Vowels.
    };

    // Get direct access to the data model.
    private static final DataModel DATA_MODEL = DataModel.getInstance();

    private Data() {
        // Disabled.
    }

    // TODO: Explain what these values are. Can we make this an enum instead (safer)?
    static final int TERRITORY_RECORD_TYPE_NONE = 0;
    static final int TERRITORY_RECORD_TYPE_PIPE = 1;
    static final int TERRITORY_RECORD_TYPE_PLUS = 2;
    static final int TERRITORY_RECORD_TYPE_STAR = 3;

    // TODO: Need to explain what "nameless" means and what a territoryRecord is (different from territoryNumber).
    static boolean isNameless(final int territoryRecord) {
        assert (0 <= territoryRecord) && (territoryRecord < DATA_MODEL.getNrTerritoryRecords());
        return (DATA_MODEL.getDataFlags(territoryRecord) & 64) != 0;
    }

    // TODO: Need to explain what "special shape" means.
    static boolean isSpecialShape(final int territoryRecord) {
        assert (0 <= territoryRecord) && (territoryRecord < DATA_MODEL.getNrTerritoryRecords());

        // TODO: The "magic" of binary operators and bit shifting should be in class DataModel, not here.
        return (DATA_MODEL.getDataFlags(territoryRecord) & 1024) != 0;
    }

    // TODO: Explain what territory record types are. Can they be an enum instead?
    static int getTerritoryRecordType(final int territoryRecord) {
        assert (0 <= territoryRecord) && (territoryRecord < DATA_MODEL.getNrTerritoryRecords());
        return (DATA_MODEL.getDataFlags(territoryRecord) >> 7) & 3; // 1=pipe 2=plus 3=star
    }

    // TODO: Explain what "restricted" means.
    static boolean isRestricted(final int territoryRecord) {
        assert (0 <= territoryRecord) && (territoryRecord < DATA_MODEL.getNrTerritoryRecords());
        return (DATA_MODEL.getDataFlags(territoryRecord) & 512) != 0;
    }

    static int getCodex(final int territoryRecord) {
        assert (0 <= territoryRecord) && (territoryRecord < DATA_MODEL.getNrTerritoryRecords());
        final int codexflags = DATA_MODEL.getDataFlags(territoryRecord) & 31;
        return (10 * (codexflags / 5)) + (codexflags % 5) + 1;
    }

    // TODO: What does this method do? What is parameter i (rename)?
    @Nonnull
    static String headerLetter(final int i) {
        final int flags = DATA_MODEL.getDataFlags(i);

        // TODO: The "magic" of how to interpret flags must be in DataModel, not here.
        if (((flags >> 7) & 3) == 1) {
            return Character.toString(ENCODE_CHARS[(flags >> 11) & 31]);
        }
        return "";
    }
}
