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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mapcode.CheckArgs.checkMapcodeCode;
import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This class defines a single mapcode encoding result, including the alphanumeric code and the
 * territory definition.
 *
 * On terminology, mapcode territory and mapcode code:
 *
 * In written form. a mapcode is defined as an alphanumeric code, optionally preceded by a
 * territory code.
 *
 * For example: "NLD 49.4V" is a mapcode, but "49.4V" is a mapcode as well, The latter is called
 * a "local" mapcode, because it is not internationally unambiguous unless preceded by a territory
 * code.
 *
 * For "NLD 49.4V" the "NLD"-part is called "the territory" and the "49.4V"-part is called
 * "the code" (which are both part of "the mapcode").
 *
 * This distinction between "territory" and "code" in a mapcode is why the interface of this class
 * has been changed from version 1.50.0 to reflect this terminology.
 *
 * On alphabets:
 *
 * Mapcode codes can be represented in different alphabets. Note that an alphabet is something else
 * than a locale or a language. The supported alphabets for mapcodes are listed in {@link Alphabet}.
 *
 * Mapcode objects provide methods to obtain the mapcode code in a specific alphabet. By default,
 * the {@link Alphabet#ROMAN} is used.
 */
public final class Mapcode {

    @Nonnull
    private final Territory territory;

    @Nonnull
    private final String codePrecision0;    // No additional precision characters.

    @Nonnull
    private final String codePrecision1;    // One precision character suffix.

    @Nonnull
    private final String codePrecision2;    // Two precision characters suffix.

    /**
     * Create a mapcode object. Normally, mapcodes are created be encoding a lat/lon pair
     * using {@link MapcodeCodec#encode(double, double)} rather than creating them yourself.
     *
     * Note that it is possible to create invalid mapcodes this way, which are syntactically
     * correct.
     *
     * Note that the constructor will throw an {@link IllegalArgumentException} if the syntax of the mapcode
     * is not correct. The mapcode is not checked for validity, other than its syntax.
     *
     * @param code      Code of mapcode.
     * @param territory Territory.
     * @throws IllegalArgumentException Thrown if syntax not valid or if the mapcode string contains
     *                                  territory information.
     */
    public Mapcode(
            @Nonnull final String code,
            @Nonnull final Territory territory) throws IllegalArgumentException {

        checkMapcodeCode("code", code);
        if (containsTerritory(code)) {
            throw new IllegalArgumentException("code cannot territory information: " + code);
        }
        final String codeUppercase = code.toUpperCase();
        this.codePrecision2 = codeUppercase;
        if (codeUppercase.contains("-")) {
            this.codePrecision0 = codeUppercase.substring(0, codeUppercase.length() - 3);
            this.codePrecision1 = codeUppercase.substring(0, codeUppercase.length() - 1);
        } else {
            this.codePrecision0 = codeUppercase;
            this.codePrecision1 = codeUppercase;
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
     * @param alphabet Alphabet.
     * @return Mapcode string.
     */
    @Nonnull
    public String getCode(@Nullable final Alphabet alphabet) {
        return convertMapcodeToAlphabet(codePrecision0, alphabet);
    }

    @Nonnull
    public String getCode() {
        return convertMapcodeToAlphabet(codePrecision0, null);
    }

    /**
     * Get the mapcode code (without territory information) with a specified precision.
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
     * @param precision Precision. Range: 0..2.
     * @param alphabet  Alphabet.
     * @return Mapcode code.
     */
    @Nonnull
    public String getCode(final int precision, @Nullable final Alphabet alphabet) {
        switch (precision) {
            case 0:
                return convertMapcodeToAlphabet(codePrecision0, alphabet);
            case 1:
                return convertMapcodeToAlphabet(codePrecision1, alphabet);
            case 2:
                return convertMapcodeToAlphabet(codePrecision2, alphabet);
            default:
                throw new IllegalArgumentException("getCodePrecision: precision must be in [0, 2]");
        }
    }

    @Nonnull
    public String getCode(final int precision) {
        return getCode(precision, null);
    }

    /**
     * Return the full international mapcode, including the full name of the territory and the mapcode code itself.
     * The format of the string is:
     * full-territory-name cde
     *
     * Example:
     * Netherlands 49.4V           (regular code)
     * Netherlands 49.4V-K2        (high precision code)
     *
     * @param precision Precision specifier. Range: [0, 2].
     * @param alphabet  Alphabet.
     * @return Full international mapcode.
     */
    @Nonnull
    public String getCodeWithTerritoryFullname(final int precision, @Nullable final Alphabet alphabet) {
        return territory.getFullName() + ' ' + getCode(precision, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritoryFullname(final int precision) {
        return getCodeWithTerritoryFullname(precision, null);
    }

    @Nonnull
    public String getCodeWithTerritoryFullname(@Nullable final Alphabet alphabet) {
        return getCodeWithTerritoryFullname(0, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritoryFullname() {
        return getCodeWithTerritoryFullname(0, null);
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
     * @param alphabet  Alphabet.
     * @return Short-hand international mapcode.
     */
    @Nonnull
    public String getCodeWithTerritory(final int precision, @Nullable final Alphabet alphabet) {
        return territory.toString() + ' ' + getCode(precision, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritory(final int precision) {
        return getCodeWithTerritory(precision, null);
    }

    @Nonnull
    public String getCodeWithTerritory(@Nonnull final Alphabet alphabet) {
        return getCodeWithTerritory(0, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritory() {
        return getCodeWithTerritory(0, null);
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
     * These patterns and matchers are used internally in this module to match mapcodes. They are
     * provided as statics to only compile these patterns once.
     */
    @Nonnull
    static final String REGEX_TERRITORY = "[\\p{Alpha}\\p{Digit}]{2,3}+([-_][\\p{Alpha}\\p{Digit}]{2,3}+)?";
    @Nonnull
    static final String REGEX_CODE_PART = "[\\p{Alpha}\\p{Digit}]{2,5}+";
    @Nonnull
    static final String REGEX_CODE_PRECISION = "[-][\\p{Alpha}\\p{Digit}&&[^zZ]]{1,2}+";

    /**
     * This patterns/regular expressions is used for checking mapcode format strings.
     * They've been made public to allow others to use the correct regular expressions as well.
     */
    @Nonnull
    public static final String REGEX_MAPCODE = '(' + REGEX_TERRITORY + "[ ]+)?" +
            REGEX_CODE_PART + "[.]" + REGEX_CODE_PART + '(' + REGEX_CODE_PRECISION + ")?";

    @Nonnull
    static final Pattern PATTERN_MAPCODE =
            Pattern.compile('^' + REGEX_MAPCODE + '$', Pattern.UNICODE_CHARACTER_CLASS);
    @Nonnull
    static final Pattern PATTERN_TERRITORY =
            Pattern.compile('^' + REGEX_TERRITORY + ' ', Pattern.UNICODE_CHARACTER_CLASS);
    @Nonnull
    static final Pattern PATTERN_PRECISION =
            Pattern.compile(REGEX_CODE_PRECISION + '$', Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * This enum describes the types of available mapcodes (as returned by {@link #getMapcodeFormatType(String)}.
     */
    public enum FormatType {
        PRECISION_0,
        PRECISION_1,
        PRECISION_2,
        INVALID;

        public static FormatType fromPrecision(final int precision) {
            switch (precision) {
                case 0:
                    return PRECISION_0;
                case 1:
                    return PRECISION_1;
                case 2:
                    return PRECISION_2;
                default:
                    return INVALID;
            }
        }
    }

    /**
     * This method return the mapcode type, given a mapcode string. If the mapcode string has an invalid
     * format, {@link FormatType#INVALID} is returned. If another value is returned,
     * the precision of the mapcode is given.
     *
     * Note that this method only checks the syntactic validity of the mapcode, the string format. It does not
     * check if the mapcode is really a valid mapcode representing a position on Earth.
     *
     * @param mapcode Mapcode (optionally with a territory).
     * @return Type of mapcode code format, or {@link FormatType#INVALID} if not valid.
     * @throws IllegalArgumentException If mapcode has incorrect syntax.
     */
    @Nonnull
    public static FormatType getMapcodeFormatType(@Nonnull final String mapcode) throws IllegalArgumentException {

        // First, decode to ASCII.
        final String decodedMapcode = convertMapcodeToPlainAscii(mapcode.toUpperCase());

        // Syntax needs to be OK.
        if (!PATTERN_MAPCODE.matcher(decodedMapcode).matches()) {
            return FormatType.INVALID;
        }

        // Precision part should be OK.
        final Matcher matcherPrecision = PATTERN_PRECISION.matcher(decodedMapcode);
        if (!matcherPrecision.find()) {
            return FormatType.PRECISION_0;
        }
        final int length = matcherPrecision.end() - matcherPrecision.start();
        assert (2 <= length) && (length <= 3);
        if (length == 2) {
            return FormatType.PRECISION_1;
        }
        return FormatType.PRECISION_2;
    }

    /**
     * This method provides a shortcut to checking if a mapcode string is formatted properly or not at all.
     *
     * @param mapcode Mapcode (optionally with a territory_).
     * @return True if the mapcode format, the syntax, is correct. This does not mean the mapcode code is
     * actually a valid  mapcode representing a location on Earth.
     * @throws IllegalArgumentException If mapcode is null.
     */
    public static boolean isValidMapcodeFormat(@Nonnull final String mapcode) throws IllegalArgumentException {
        checkNonnull("mapcode", mapcode);
        return getMapcodeFormatType(mapcode.toUpperCase()) != FormatType.INVALID;
    }

    /**
     * Returns whether the mapcode contains territory information or not.
     *
     * @param mapcode Mapcode string, optionally with territory information.
     * @return True if mapcode contains territory information.
     * @throws IllegalArgumentException If mapcode has incorrect syntax.
     */
    public static boolean containsTerritory(@Nonnull final String mapcode) throws IllegalArgumentException {
        checkMapcodeCode("mapcode", mapcode);
        return PATTERN_TERRITORY.matcher(mapcode.toUpperCase().trim()).find();
    }

    /**
     * This array defines the safe maximum offset between a decoded mapcode and its original
     * location used for encoding the mapcode.
     */
    private static final double[] PRECISION_0_MAX_OFFSET_METERS = {10.0, 2.0, 0.4};

    /**
     * Get a safe maximum for the distance between a decoded mapcode and its original
     * location used for encoding the mapcode. The actual accuracy (resolution) of mapcodes is slightly
     * better than this, but these are safe values to use under normal circumstances.
     *
     * @param precision Precision of mapcode.
     * @return Maximum offset in meters.
     */
    public static double getSafeMaxOffsetInMeters(final int precision) {
        if ((precision < 0) || (precision > 2)) {
            throw new IllegalArgumentException("precision must be in [0, 2]");
        }
        return PRECISION_0_MAX_OFFSET_METERS[precision];
    }

    /**
     * Convert a mapcode which potentially contains Unicode characters, to an ASCII variant.
     *
     * @param mapcode Mapcode (optionally with a territory), with optional Unicode characters.
     * @return ASCII, non-Unicode string.
     */
    @Nonnull
    static String convertMapcodeToPlainAscii(@Nonnull final String mapcode) {
        // Cannot call: checkMapcodeCode() - recursive.
        return Decoder.decodeUTF16(mapcode.toUpperCase());
    }

    /**
     * Convert a mapcode into the same mapcode using a different (or the same) alphabet.
     *
     * @param mapcode  Mapcode (optionally with a territory) to be converted.
     * @param alphabet Alphabet to convert to, may contain Unicode characters.
     * @return Converted mapcode.
     * @throws IllegalArgumentException If mapcode has incorrect syntax.
     */
    @Nonnull
    static String convertMapcodeToAlphabet(@Nonnull final String mapcode, @Nullable final Alphabet alphabet) throws IllegalArgumentException {
        checkMapcodeCode("mapcode", mapcode);
        return (alphabet != null) ? Decoder.encodeUTF16(mapcode.toUpperCase(), alphabet.code) : mapcode.toUpperCase();
    }

    /**
     * This method is defined as returning the mapcode code including its territory,
     * with normal precision (precision 0).
     *
     * @return Mapcode, including territory and code. Plain ASCII, non-Unicode.
     */
    @Nonnull
    @Override
    public String toString() {
        return getCodeWithTerritory();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{codePrecision0, codePrecision1, codePrecision2, territory});
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mapcode)) {
            return false;
        }
        final Mapcode that = (Mapcode) obj;
        return codePrecision0.equals(that.codePrecision0) &&
                codePrecision1.equals(that.codePrecision1) &&
                codePrecision2.equals(that.codePrecision2) &&
                (this.territory.equals(that.territory));
    }

    /**
     * ----------------------------------------------------------------------
     * Deprecated methods.
     * ----------------------------------------------------------------------
     *
     * Important: these methods will potentially be removed from the interface in later releases.
     * It is advised to migrate to the newer variants.
     */

    /**
     * Deprecated. Replaced with {@link #getCode}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcode() {
        return getCode();
    }

    /**
     * Deprecated. Replaced with {@link #getCode}.
     *
     * @param precision Deprecated.
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodePrecision(final int precision) {
        return getCode(precision);
    }

    /**
     * Deprecated. Replaced with {@link #getCode(int)}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodePrecision0() {
        return codePrecision0;
    }

    /**
     * Deprecated. Replaced with {@link #getCode(int)}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodePrecision1() {
        return codePrecision1;
    }

    /**
     * Deprecated. Replaced with {@link #getCode(int)}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodeMediumPrecision() {
        return codePrecision1;
    }

    /**
     * Deprecated. Replaced with {@link #getCode(int)}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodePrecision2() {
        return codePrecision2;
    }

    /**
     * Deprecated. Replaced with {@link #getCode(int)}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String getMapcodeHighPrecision() {
        return codePrecision2;
    }

    /**
     * Deprecated. Replaced with {@link #getCode()}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String asLocal() {
        return getCode();
    }

    /**
     * Deprecated. Replaced with {@link #getCodeWithTerritoryFullname()}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String asInternationalFullName() {
        return getCodeWithTerritoryFullname();
    }

    /**
     * Deprecated. Replaced with {@link #getCodeWithTerritory()}.
     *
     * @return Deprecated.
     */
    @Deprecated
    @Nonnull
    public String asInternationalISO() {
        return getCodeWithTerritory();
    }
}
