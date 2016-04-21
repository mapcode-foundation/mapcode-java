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
    private final String codePrecision8;    // Internally, codes are always stored at precision 8.

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
        final String ascii = convertStringToPlainAscii(code);
        if (containsTerritory(ascii)) {
            throw new IllegalArgumentException("Must not contain territory: " + code);
        }

        // Build codeUppercase with exactly eight precision digits.
        String codeUppercase = ascii.toUpperCase();
        final int hyphenPos = codeUppercase.indexOf('-');
        if (hyphenPos < 0) {
            codeUppercase = codeUppercase + "-K3000000";
        }
        else {
            final int extensionLength = codeUppercase.length() - 1 - hyphenPos;
            if (extensionLength < 8) {
                if ((extensionLength % 2) == 1) {
                    // Odd extension.
                    codeUppercase = codeUppercase + ("HH000000".substring(0, 8 - extensionLength));
                }
                else {
                    // Even extension.
                    codeUppercase = codeUppercase + ("K3000000".substring(0, 8 - extensionLength));
                }
            }
            else if (extensionLength > 8) {
                // Cut to 8 characters.
                codeUppercase = codeUppercase.substring(0, hyphenPos + 9);
            }
        }

        this.codePrecision8 = codeUppercase;
        this.territory = territory;
    }

    /**
     * Get the Mapcode string (without territory information) with standard precision.
     * The returned mapcode does not include the '-' separator and additional digits.
     *
     * A mapcode defines an area of approximately 10 x 10 meters (100 m2) and will decode
     * to the center of that area. On average, the original coordinate will be 3.6 meters
     * from this center: the average inaccuracy of a mapcode.
     *
     * @param alphabet Alphabet.
     * @return Mapcode string.
     */
    @Nonnull
    public String getCode(@Nullable final Alphabet alphabet) {
        return getCode(0, alphabet);
    }

    @Nonnull
    public String getCode() {
        return getCode(0, null);
    }

    /**
     * Get the mapcode code (without territory information) with a specified precision.
     * The returned mapcode includes a '-' separator and additional digits for precisions 1 to 8.
     *
     * The precision defines the size of a geographical area a single mapcode covers. This means It also defines
     * the maximum distance to the location, a (latitude, longitude) pair, that encoded to this mapcode.
     *
     * Precision 0: area is approx 10 x 10 meters (100 m2); max. distance from original location less than 7.5 meters.
     * Precision 1: area is approx 3.33 m2; max. distance from original location less than 1.5 meters.
     * Precision 1: area is approx 0.11 m2; max. distance from original location less than 0.4 meters.
     * etc. (each level reduces the area by a factor of 30)
     *
     * The accuracy is slightly better than the figures above, but these figures are safe assumptions.
     *
     * @param precision Precision. Range: 0..8.
     * @param alphabet  Alphabet.
     * @return Mapcode code.
     * @throws IllegalArgumentException Thrown if precision is out of range (must be in [0, 8]).
     */
    @Nonnull
    public String getCode(final int precision, @Nullable final Alphabet alphabet) {
        if (precision == 0) {
            return convertStringToAlphabet(codePrecision8.substring(0, codePrecision8.length() - 9), alphabet);
        }
        else if (precision <= 8) {
            return convertStringToAlphabet(codePrecision8.substring(0, (codePrecision8.length() - 8) + precision),
                alphabet);
        }
        else {
            throw new IllegalArgumentException("getCodePrecision: precision must be in [0, 8]");
        }
    }

    @Nonnull
    public String getCode(final int precision) throws IllegalArgumentException {
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
     * @param precision Precision specifier. Range: [0, 8].
     * @param alphabet  Alphabet.
     * @return Full international mapcode.
     * @throws IllegalArgumentException Thrown if precision is out of range (must be in [0, 8]).
     */
    @Nonnull
    public String getCodeWithTerritoryFullname(final int precision, @Nullable final Alphabet alphabet) throws IllegalArgumentException {
        return territory.getFullName() + ' ' + getCode(precision, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritoryFullname(final int precision) throws IllegalArgumentException {
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
     * @param precision Precision specifier. Range: [0, 8].
     * @param alphabet  Alphabet.
     * @return Short-hand international mapcode.
     * @throws IllegalArgumentException Thrown if precision is out of range (must be in [0, 8]).
     */
    @Nonnull
    public String getCodeWithTerritory(final int precision, @Nullable final Alphabet alphabet) throws IllegalArgumentException {
        return territory.toString() + ' ' + getCode(precision, alphabet);
    }

    @Nonnull
    public String getCodeWithTerritory(final int precision) throws IllegalArgumentException {
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
    static final String REGEX_TERRITORY      = "[\\p{L}\\p{N}]{2,3}+([-_][\\p{L}\\p{N}]{2,3}+)?";
    @Nonnull
    static final String REGEX_CODE_PREFIX    = "[\\p{L}\\p{N}]{2,5}+";
    @Nonnull
    static final String REGEX_CODE_POSTFIX   = "[\\p{L}\\p{N}]{2,4}+";
    @Nonnull
    static final String REGEX_CODE_PRECISION = "[-][\\p{L}\\p{N}&&[^zZ]]{1,8}+";

    /**
     * This patterns/regular expressions is used for checking mapcode format strings.
     * They've been made public to allow others to use the correct regular expressions as well.
     */
    @Nonnull
    public static final String REGEX_MAPCODE = '(' + REGEX_TERRITORY + "[ ]+)?" +
        REGEX_CODE_PREFIX + "[.]" + REGEX_CODE_POSTFIX + '(' + REGEX_CODE_PRECISION + ")?";

    @Nonnull
    static final Pattern PATTERN_MAPCODE   = Pattern.compile('^' + REGEX_MAPCODE + '$');
    @Nonnull
    static final Pattern PATTERN_TERRITORY = Pattern.compile('^' + REGEX_TERRITORY + ' ');
    @Nonnull
    static final Pattern PATTERN_PRECISION = Pattern.compile(REGEX_CODE_PRECISION + '$');

    /**
     * This method return the mapcode type, given a mapcode string. If the mapcode string has an invalid
     * format, an exception is thrown.
     *
     * Note that this method only checks the syntactic validity of the mapcode, the string format. It does not
     * check if the mapcode is really a valid mapcode representing a position on Earth.
     *
     * @param mapcode Mapcode (optionally with a territory).
     * @return Type of mapcode code format.
     * @throws UnknownPrecisionFormatException If precision format is incorrect.
     */
    public static int getPrecisionFormat(@Nonnull final String mapcode) throws UnknownPrecisionFormatException {

        // First, decode to ASCII.
        final String decodedMapcode = convertStringToPlainAscii(mapcode).toUpperCase();

        // Syntax needs to be OK.
        if (!PATTERN_MAPCODE.matcher(decodedMapcode).matches()) {
            throw new UnknownPrecisionFormatException(decodedMapcode + " is not a correctly formatted mapcode code; " +
                "the regular expression for the mapcode code syntax is: " + REGEX_MAPCODE);
        }

        // Precision part should be OK.
        final Matcher matcherPrecision = PATTERN_PRECISION.matcher(decodedMapcode);
        if (!matcherPrecision.find()) {
            return 0;
        }
        final int length = matcherPrecision.end() - matcherPrecision.start() - 1;
        assert (1 <= length) && (length <= 8);
        return length;
    }

    /**
     * This method provides a shortcut to checking if a mapcode string is formatted properly or not at all.
     *
     * @param mapcode Mapcode (optionally with a territory).
     * @return True if the mapcode format, the syntax, is correct. This does not mean the mapcode code is
     * actually a valid  mapcode representing a location on Earth.
     * @throws IllegalArgumentException If mapcode is null.
     */
    public static boolean isValidMapcodeFormat(@Nonnull final String mapcode) throws IllegalArgumentException {
        checkNonnull("mapcode", mapcode);
        try {
            // Throws an exception if the format is incorrect.
            getPrecisionFormat(mapcode.toUpperCase());
            return true;
        }
        catch (final UnknownPrecisionFormatException ignored) {
            return false;
        }
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
    private static final double[] PRECISION_0_MAX_OFFSET_METERS = {
        7.49,         // PRECISION_0: 7.49 meters or less       +/- 7.5 m
        1.39,         // PRECISION_1: 1.39 meters or less       +/- 1.4 m
        0.251,        // PRECISION_2: 25.1 cm or less           +/- 25 cm
        0.0462,       // PRECISION_3: 4.62 cm or less           +/- 5 cm
        0.00837,      // PRECISION_4: 8.37 mm or less           +/- 1 cm
        0.00154,      // PRECISION_5: 1.54 mm or less           +/- 2 mm
        0.000279,     // PRECISION_6: 279 micrometer or less    +/- 1/3 mm
        0.0000514,    // PRECISION_7: 51.4 micrometer or less   +/- 1/20 mm
        0.0000093     // PRECISION_8: 9.3 micrometer or less    +/- 1/100 mm
    };

    /**
     * Get a safe maximum for the distance between a decoded mapcode and its original
     * location used for encoding the mapcode. The actual accuracy (resolution) of mapcodes is
     * better than this, but these are safe values to use under normal circumstances.
     *
     * Do not make any other assumptions on these numbers than that mapcodes are never more off
     * by this distance.
     *
     * @param precision Precision of mapcode.
     * @return Maximum offset in meters.
     */
    public static double getSafeMaxOffsetInMeters(final int precision) {
        if ((precision < 0) || (precision > 8)) {
            throw new IllegalArgumentException("precision must be in [0, 8]");
        }
        return PRECISION_0_MAX_OFFSET_METERS[precision];
    }

    /**
     * Convert a string which potentially contains Unicode characters, to an ASCII variant.
     *
     * @param string Any string.
     * @return ASCII, non-Unicode string.
     */
    @Nonnull
    static String convertStringToPlainAscii(@Nonnull final String string) {
        return Decoder.decodeUTF16(string.toUpperCase());
    }

    /**
     * Convert a string into the same string using a different (or the same) alphabet.
     *
     * @param string   Any string.
     * @param alphabet Alphabet to convert to, may contain Unicode characters.
     * @return Converted mapcode.
     * @throws IllegalArgumentException Thrown if string has incorrect syntax or if the string cannot be encoded in
     *                                  the specified alphabet.
     */
    @Nonnull
    static String convertStringToAlphabet(@Nonnull final String string, @Nullable final Alphabet alphabet) throws IllegalArgumentException {
        return (alphabet != null) ? Decoder.encodeUTF16(string.toUpperCase(), alphabet.getNumber()) :
            string.toUpperCase();
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
        return Arrays.deepHashCode(new Object[]{codePrecision8, territory});
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mapcode)) {
            return false;
        }
        final Mapcode that = (Mapcode) o;
        return this.territory.equals(that.territory) &&
            this.codePrecision8.equals(that.codePrecision8);
    }
}
