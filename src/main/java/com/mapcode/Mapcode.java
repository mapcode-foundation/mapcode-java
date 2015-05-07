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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a single mapcode encoding result, including the mapcode itself and the
 * territory definition.
 * <p/>
 * Note that the constructor will throw an {@link IllegalArgumentException} if the syntax of the mapcode
 * is not correct. It does not throw an {@link UnknownMapcodeException}, because the mapcode
 * is not checked for validity, other than its syntax.
 */
public final class Mapcode {
    @Nonnull
    private final String mapcodePrecision0;
    @Nonnull
    private final String mapcodePrecision1;
    @Nonnull
    private final String mapcodePrecision2;
    @Nonnull
    private final Territory territory;

    public Mapcode(
            @Nonnull final String mapcode,
            @Nonnull final Territory territory) throws IllegalArgumentException {

        // Check mapcode format.
        if (!isValidMapcodeFormat(mapcode)) {
            throw new IllegalArgumentException(mapcode + " is not a correctly formatted mapcode; " +
                    "the regular expression for the mapcode syntax is: " + REGEX_MAPCODE_FORMAT);
        }

        this.mapcodePrecision2 = mapcode;
        if (mapcode.contains("-")) {
            this.mapcodePrecision0 = mapcode.substring(0, mapcode.length() - 3);
            this.mapcodePrecision1 = mapcode.substring(0, mapcode.length() - 1);
        } else {
            this.mapcodePrecision0 = mapcode;
            this.mapcodePrecision1 = mapcode;
        }
        this.territory = territory;
    }

    /**
     * Get the Mapcode string (without territory information) with standard precision.
     * The returned mapcode does not include the '-' separator and additional digits.
     * <p/>
     * The returned precision is approximately 5 meters. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 10 x 10 meters (100 m2).
     *
     * @return Mapcode string.
     */
    @Nonnull
    public String getMapcode() {
        return mapcodePrecision0;
    }

    /**
     * Alias for {@link #getMapcode}.
     *
     * @param precision Precision. Range: 0..2.
     * @return Mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision(final int precision) {
        switch (precision) {
            case 0:
                return mapcodePrecision0;
            case 1:
                return mapcodePrecision1;
            case 2:
                return mapcodePrecision2;
            default:
                throw new IllegalArgumentException("getMapcodePrecision: precision must be in [0..2]");
        }
    }

    /**
     * Alias for {@link #getMapcode}.
     *
     * @return Mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision0() {
        return mapcodePrecision0;
    }

    /**
     * Get the medium-precision mapcode string (without territory information).
     * The returned mapcode includes the '-' separator and 1 additional digit, if available.
     * If a medium precision code is not available, the regular mapcode is returned.
     * <p/>
     * The returned precision is approximately 1 meter. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 2 x 2 meters (4 m2).
     *
     * @return Medium precision mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision1() {
        return mapcodePrecision1;
    }

    /**
     * Deprecated alias for {@link #getMapcodePrecision1}.
     */
    @Deprecated
    @Nonnull
    public String getMapcodeMediumPrecision() {
        return mapcodePrecision1;
    }

    /**
     * Get the high-precision mapcode string (without territory information).
     * The returned mapcode includes the '-' separator and 2 additional digit2, if available.
     * If a high precision code is not available, the regular mapcode is returned.
     * <p/>
     * The returned precision is approximately 16 centimeters. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 32 x 32 centimeters (0.1 m2).
     *
     * @return High precision mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision2() {
        return mapcodePrecision2;
    }

    // Deprecated alias for {@see #getMapcodePrecision2}.
    @Deprecated
    @Nonnull
    public String getMapcodeHighPrecision() {
        return mapcodePrecision2;
    }

    /**
     * Get the territory information.
     *
     * @return Territory information.
     */
    @Nonnull
    public Territory getTerritory() {
        return territory;
    }

    /**
     * This enum describes the types of mapcodes available.
     */
    public enum MapcodeFormatType {
        MAPCODE_TYPE_INVALID,
        MAPCODE_TYPE_PRECISION_0,
        MAPCODE_TYPE_PRECISION_1,
        MAPCODE_TYPE_PRECISION_2,
    }

    /**
     * These patterns and matchers are used internally in this module to match mapcodes. They are
     * provided as statics to only compile these patterns once.
     */
    @Nonnull
    static final String REGEX_MAPCODE_FORMAT1 = "^[\\p{Alpha}\\p{Digit}]{2,5}+";
    @Nonnull
    static final String REGEX_MAPCODE_FORMAT2 = "[.][\\p{Alpha}\\p{Digit}]{2,5}+";
    @Nonnull
    static final String REGEX_MAPCODE_PRECISION = "[-][\\p{Alpha}\\p{Digit}&&[^zZ]]{1,2}+";

    /**
     * This patterns/regular expressions is used for checking mapcode format strings.
     * They've been made public to allow others to use the correct regular expressions as well.
     */
    @Nonnull
    public static final String REGEX_MAPCODE_FORMAT =
            REGEX_MAPCODE_FORMAT1 + REGEX_MAPCODE_FORMAT2 + '(' + REGEX_MAPCODE_PRECISION + ")?$";

    @Nonnull
    private static final Pattern PATTERN_MAPCODE_FORMAT =
            Pattern.compile(REGEX_MAPCODE_FORMAT, Pattern.UNICODE_CHARACTER_CLASS);
    @Nonnull
    private static final Pattern PATTERN_MAPCODE_PRECISION =
            Pattern.compile(REGEX_MAPCODE_PRECISION, Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * This method return the mapcode type, given a mapcode string. If the mapcode string has an invalid
     * format, {@link MapcodeFormatType#MAPCODE_TYPE_INVALID} is returned. If another value is returned,
     * the precision of the mapcode is given.
     * <p/>
     * Note that this method only checks the syntactic validity of the mapcode, the string format. It does not
     * check if the mapcode is really a valid mapcode representing a position on Earth.
     *
     * @param mapcode Mapcode string.
     * @return Type of mapcode format, or {@link MapcodeFormatType#MAPCODE_TYPE_INVALID} if not valid.
     */
    @Nonnull
    public static MapcodeFormatType getMapcodeFormatType(@Nonnull final String mapcode) {

        // First, decode to ASCII.
        final String decodedMapcode = convertToAscii(mapcode);

        // Syntax needs to be OK.
        if (!PATTERN_MAPCODE_FORMAT.matcher(decodedMapcode).matches()) {
            return MapcodeFormatType.MAPCODE_TYPE_INVALID;
        }

        // Precision part should be OK.
        final Matcher matcherMapcodePrecision = PATTERN_MAPCODE_PRECISION.matcher(decodedMapcode);
        if (!matcherMapcodePrecision.find()) {
            return MapcodeFormatType.MAPCODE_TYPE_PRECISION_0;
        }
        final int length = matcherMapcodePrecision.end() - matcherMapcodePrecision.start();
        assert (2 <= length) && (length <= 3);
        if (length == 2) {
            return MapcodeFormatType.MAPCODE_TYPE_PRECISION_1;
        }
        return MapcodeFormatType.MAPCODE_TYPE_PRECISION_2;
    }

    /**
     * This method provides a shortcut to checking if a mapcode string is formatted properly or not at all.
     *
     * @param mapcode Mapcode string.
     * @return True if the mapcode format, the syntax, is correct. This does not mean the mapcode is actually a valid
     * mapcode representing a location on Earth.
     */
    public static boolean isValidMapcodeFormat(@Nonnull final String mapcode) {
        return getMapcodeFormatType(mapcode) != MapcodeFormatType.MAPCODE_TYPE_INVALID;
    }

    /**
     * Convert a mapcode which potentially contains Unicode characters, to an ASCII veriant.
     *
     * @param mapcode Mapcode, with optional Unicode characters.
     * @return ASCII, non-Unicode string.
     */
    @Nonnull
    public static String convertToAscii(@Nonnull final String mapcode) {
        return Decoder.decodeUTF16(mapcode);
    }

    /**
     * Return the local mapcode string, potentially ambiguous.
     * <p/>
     * Example:
     * 49.4V
     *
     * @return Local mapcode.
     */
    @Nonnull
    public String asLocal() {
        return mapcodePrecision0;
    }

    /**
     * Return the full international mapcode, including the full name of the territory and the Mapcode itself.
     * The format of the code is:
     * full-territory-name mapcode
     * <p/>
     * Example:
     * Netherlands 49.4V           (regular code)
     * Netherlands 49.4V-K2        (high precision code)
     *
     * @return Full international mapcode.
     */
    @Nonnull
    public String asInternationalFullName() {
        return territory.getFullName() + ' ' + mapcodePrecision0;
    }

    /**
     * Return the international mapcode as a shorter version using the ISO territory codes where possible.
     * International codes use a territory code "AAA".
     * The format of the code is:
     * short-territory-name mapcode
     * <p/>
     * Example:
     * NLD 49.4V                   (regular code)
     * NLD 49.4V-K2                (high-precision code)
     *
     * @return Short-hand international mapcode.
     */
    @Nonnull
    public String asInternationalISO() {
        return territory.toString() + ' ' + mapcodePrecision0;
    }

    @Nonnull
    @Override
    public String toString() {
        return asInternationalISO();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{mapcodePrecision0, territory});
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mapcode)) {
            return false;
        }
        final Mapcode that = (Mapcode) obj;
        return mapcodePrecision0.equals(that.mapcodePrecision0) && (this.territory.equals(that.territory));
    }
}
