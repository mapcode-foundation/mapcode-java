/*
 * Copyright (C) 2014-2016 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This enum defines all alphabets supported for mapcodes. Note that an alphabet is different
 * from a language or locale. Note that the term alphabet was historically chosen. Script is a
 * more precise term.
 *
 * Mapcodes can be safely converted between alphabets/scripts and fed to the mapcode decoder
 * in the regular ASCII Roman alphabet or any other.
 *
 * The characters within an alphabet/script have been carefully chosen to resemble each other
 * as much as possible.
 */
public enum Alphabet {
    ROMAN(0),               // The numeric codes for alphabets are used by the implementation
    GREEK(1),               // of the mapcode library. Do not change them.
    CYRILLIC(2),
    HEBREW(3),
    HINDI(4),
    MALAY(5),
    GEORGIAN(6),
    KATAKANA(7),
    THAI(8),
    LAO(9),
    ARMENIAN(10),
    BENGALI(11),
    GURMUKHI(12),
    TIBETAN(13),
    ARABIC(14);

    /**
     * The numeric code is synonym for the alphanumeric code. Used in the decoder.
     */
    private final int number;

    /**
     * Private constructor.
     *
     * @param number Alphabet number, for internal use only.
     */
    Alphabet(final int number) {
        this.number = number;
    }

    /**
     * Get alphabet number. Package private, for internal use only.
     *
     * @return Numeric code.
     */
    int getNumber() {
        return number;
    }

    /**
     * Return alphabet from a string, which needs to be an alphanumeric code.
     *
     * @param alphaCode Alphabet, alphanumeric code.
     * @return Alphabet.
     * @throws UnknownAlphabetException Thrown if incorrect numeric or alphanumeric code.
     */
    @Nonnull
    public static Alphabet fromString(@Nonnull final String alphaCode) throws UnknownAlphabetException {
        checkNonnull("alphaCode", alphaCode);
        final String trimmed = alphaCode.trim().toUpperCase();
        try {
            return valueOf(trimmed);
        } catch (final IllegalArgumentException ignored) {
            throw new UnknownAlphabetException(trimmed);
        }
    }

    /**
     * Static consistency check of internal data structures.
     */
    static {
        int i = 0;
        for (final Alphabet alphabet : Alphabet.values()) {
            if (Alphabet.values()[i].number != i) {
                throw new ExceptionInInitializerError("Incorrect alphabet number: " + alphabet + ".number should be " + i);
            }
            ++i;
        }
    }
}
