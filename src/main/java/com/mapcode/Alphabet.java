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
import java.util.HashSet;
import java.util.Set;

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This enum defines all alphabets supported for mapcodes. Mapcodes can be safely converted between
 * alphabets and fed to the mapcode decoder.
 */
public enum Alphabet {
    ROMAN(0),
    GREEK(1),
    CYRILLIC(2),
    HEBREW(3),
    HINDI(4),
    MALAI(5),
    GEORGIAN(6),
    KATAKANA(7),
    THAI(8),
    LAO(9),
    ARMENIAN(10),
    BENGALI(11),
    GURMUKHI(12),
    TIBETAN(13);

    private final int code;

    private Alphabet(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nonnull
    public static Alphabet fromCode(final int code) throws UnknownAlphabetException {
        for (final Alphabet alphabet : values()) {
            if (alphabet.code == code) {
                return alphabet;
            }
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
        final String errorPrefix = "Initializing error: ";
        final Set<Integer> alphabetCodeList = new HashSet<>();

        for (final Alphabet alphabet : Alphabet.values()) {
            final int alphabetCode = alphabet.getCode();
            if ((alphabetCode < 0) || (alphabetCode >= Alphabet.values().length)) {
                throw new ExceptionInInitializerError(errorPrefix + "alphabet code out of range: " + alphabetCode);

            }
            if (alphabetCodeList.contains(alphabetCode)) {
                throw new ExceptionInInitializerError(errorPrefix + "non-unique alphabet code: " + alphabetCode);
            }
            alphabetCodeList.add(alphabet.getCode());
        }
        assert alphabetCodeList.size() == Alphabet.values().length;
    }
}