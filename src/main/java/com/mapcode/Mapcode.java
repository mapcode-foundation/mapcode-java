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
 *
 * Note that the constructor will throw an {@link IllegalArgumentException} if the syntax of the mapcode
 * is not correct. It does not throw an {@link com.mapcode.UnknownMapcodeException}, because the mapcode
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
     *
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
     * Get the Mapcode string (without territory information) with a specified precision.
     * The returned mapcode includes a '-' separator and additional digits for precisions 1 and 2.
     *
     * The precision defines the size of a geographical area a single mapcode covers. This means It also defines
     * the maximum distance to the location, a (latitude, longitude) pair, that encoded to this mapcode.
     *
     * Precision 0: area is approx 20 x 20 meters; max. distance from original location less than 10 meters.
     *
     * Precision 1: area is approx 4 x 4 meters; max. distance from original location less than 2 meters.
     *
     * Precision 2: area is approx 0.8 x 0.8 meters; max. distance from original location less than 0.4 meters.
     *
     * The accuracy is slightly better than the figures above, but these figures are safe assumptions.
     *
     * @return Mapcode string.
     */
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

    // Deprecated alias for getMapcodePrecision().
    @Deprecated
    @Nonnull
    public String getMapcodePrecision0() {
        return mapcodePrecision0;
    }

    // Deprecated alias for getMapcodePrecision().
    @Deprecated
    @Nonnull
    public String getMapcodePrecision1() {
        return mapcodePrecision1;
    }

    // Deprecated alias for getMapcodePrecision().
    @Deprecated
    @Nonnull
    public String getMapcodeMediumPrecision() {
        return mapcodePrecision1;
    }

    // Deprecated alias for getMapcodePrecision().
    @Deprecated
    @Nonnull
    public String getMapcodePrecision2() {
        return mapcodePrecision2;
    }

    // Deprecated alias for getMapcodePrecision().
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
     *
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
     *
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
     *
     * Example:
     * Netherlands 49.4V           (regular code)
     * Netherlands 49.4V-K2        (high precision code)
     *
     * @param precision Precision specifier. Range: [0, 2].
     * @return Full international mapcode.
     */
    @Nonnull
    public String asInternationalFullName(final int precision) {
        return territory.getFullName() + ' ' + getMapcodePrecision(precision);
    }

    @Nonnull
    public String asInternationalFullName() {
        return territory.getFullName() + ' ' + mapcodePrecision0;
    }

    /**
     * Return the international mapcode as a shorter version using the ISO territory codes where possible.
     * International codes use a territory code "AAA".
     * The format of the code is:
     * short-territory-name mapcode
     *
     * Example:
     * NLD 49.4V                   (regular code)
     * NLD 49.4V-K2                (high-precision code)
     *
     * @param precision Precision specifier. Range: [0, 2].
     * @return Short-hand international mapcode.
     */
    @Nonnull
    public String asInternationalISO(final int precision) {
        return territory.toString() + ' ' + getMapcodePrecision(precision);
    }

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
        return Arrays.deepHashCode(new Object[]{mapcodePrecision0, mapcodePrecision1, mapcodePrecision2, territory});
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
        return mapcodePrecision0.equals(that.mapcodePrecision0) &&
                mapcodePrecision1.equals(that.mapcodePrecision1) &&
                mapcodePrecision2.equals(that.mapcodePrecision2) &&
                (this.territory.equals(that.territory));
    }
}
