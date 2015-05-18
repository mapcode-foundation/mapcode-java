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

    public final int code;

    private Alphabet(final int code) {
        this.code = code;
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

    public static class UnknownAlphabetException extends RuntimeException {
        final int code;

        public UnknownAlphabetException(final int code) {
            super();
            this.code = code;
        }
    }
}