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

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This enum defines all alphabets supported for mapcodes. Mapcodes can be safely converted between
 * alphabets and fed to the mapcode decoder in the regular ASCII Roman alphabet or any other.
 */
public enum Alphabet {
    ROMAN(0),
    GREEK(1),
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
    TIBETAN(13);

    /**
     * The numeric code is synonym for the alphanumeric code. It can be used in the decoder
     * to define a territory as well.
     */
    private final int code;

    private Alphabet(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Get an alphabet from a numeric code.
     *
     * @param code Numeric code.
     * @return Alphabet.
     * @throws UnknownAlphabetException Thrown if code out of range.
     */
    @Nonnull
    public static Alphabet fromCode(final int code) throws UnknownAlphabetException {
        if ((code >= 0) && (code < Alphabet.values().length)) {
            return Alphabet.values()[code];
        }
        throw new UnknownAlphabetException(code);
    }

    /**
     * Return alphabet from a string, which can be a numeric or alpha code.
     *
     * @param numericOrAlpha Alphabet. May be a numeric or alphanumeric code.
     * @return Alphabet.
     * @throws UnknownAlphabetException Thrown if incorrect numeric or alphanumeric code.
     */
    @Nonnull
    public static Alphabet fromString(@Nonnull final String numericOrAlpha) throws UnknownAlphabetException {
        checkNonnull("name", numericOrAlpha);
        final String trimmed = numericOrAlpha.trim().toUpperCase();
        try {
            return fromCode(Integer.valueOf(numericOrAlpha));
        } catch (final IllegalArgumentException ignored) {
            // Ignore. Re-try as alpha code.
        }
        try {
            return valueOf(trimmed);
        } catch (final IllegalArgumentException ignored) {
            throw new UnknownAlphabetException(trimmed);
        }
    }

    /**
     * Static checking of the static data structures.
     */
    static {
        int i = 0;
        for (final Alphabet alphabet : Alphabet.values()) {
            if (Alphabet.values()[i].code != i) {
                throw new ExceptionInInitializerError("Incorrect alphabet code: " + alphabet + ".code should be " + i);
            }
            ++i;
        }
    }
}