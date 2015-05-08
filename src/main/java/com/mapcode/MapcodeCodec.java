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
import java.util.List;

import static com.mapcode.CheckArgs.checkNonnull;
import static com.mapcode.CheckArgs.checkRange;

/**
 * ----------------------------------------------------------------------------------------------
 * Mapcode public interface.
 * ----------------------------------------------------------------------------------------------
 * <p/>
 * This class is the external Java interface for encoding and decoding mapcodes.
 */
public final class MapcodeCodec {

    private MapcodeCodec() {
        // Prevent instantiation.
    }

    /**
     * ------------------------------------------------------------------------------------------
     * Encoding latitude, longitude to mapcodes.
     * ------------------------------------------------------------------------------------------
     */

    /**
     * Encode a lat/lon pair to a mapcode with territory information. This produces a non-empty list of mapcode,
     * with at the very least 1 mapcodes for the lat/lon, which is the "International" mapcode.
     * <p/>
     * The returned result list will always contain at least 1 mapcode, because every lat/lon pair can be encoded.
     * <p/>
     * The list is ordered in such a way that the first result contains the shortest mapcode (which is usually a
     * local mapcode). The last result contains the "International" or world-wide mapcode, which is always
     * unambiguous, even when used without a territory specification.
     * <p/>
     * The international code can be obtained from the list by using: "results.get(results.size() - 1)".
     *
     * @param latDeg Latitude, accepted range: -90..90.
     * @param lonDeg Longitude, accepted range: -180..180.
     * @return Non-empty, ordered list of mapcode information records, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static List<Mapcode> encode(
            final double latDeg,
            final double lonDeg) throws IllegalArgumentException {
        checkRange("latDeg", latDeg, Point.LAT_DEG_MIN, Point.LAT_DEG_MAX);
        checkRange("lonDeg", lonDeg, Point.LON_DEG_MIN, Point.LON_DEG_MAX);

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results = Encoder.encode(latDeg, lonDeg, null, false, false, true);
        assert results != null;
        assert results.size() >= 1;
        return results;
    }

    /**
     * Encode a lat/lon pair to a mapcode with territory information, for a specific territory. This produces a
     * potentially empty list of mapcodes (empty if the lat/lon does not fall within the territory for mapcodes).
     * <p/>
     * The returned result list will always contain at least 1 mapcode, because every lat/lon pair can be encoded.
     * <p/>
     * The list is ordered in such a way that the first result contains the shortest mapcode (which is usually a
     * local mapcode).
     *
     * @param latDeg              Latitude, accepted range: -90..90.
     * @param lonDeg              Longitude, accepted range: -180..180.
     * @param restrictToTerritory Try to encode only within this territory, see {@link Territory}. Cannot
     *                            be null.
     * @return List of mapcode information records, see {@link Mapcode}. This list is empty if no
     * Mapcode can be generated for this territory matching the lat/lon.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static List<Mapcode> encode(
            final double latDeg,
            final double lonDeg,
            @Nonnull final Territory restrictToTerritory) throws IllegalArgumentException {
        checkRange("latDeg", latDeg, Point.LAT_DEG_MIN, Point.LAT_DEG_MAX);
        checkRange("lonDeg", lonDeg, Point.LON_DEG_MIN, Point.LON_DEG_MAX);
        checkNonnull("restrictToTerritory", restrictToTerritory);

        // Call Mapcode encoder.
        @Nonnull final List<Mapcode> results =
                Encoder.encode(latDeg, lonDeg, restrictToTerritory, false, false, false);
        assert results != null;
        return results;
    }

    /**
     * Encode a lat/lon pair to its shortest mapcode without territory information. For a valid lat/lon pair, this will
     * always yield a mapcode.
     *
     * @param latDeg Latitude, accepted range: -90..90.
     * @param lonDeg Longitude, accepted range: -180..180.
     * @return Shortest mapcode (always exists), see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static Mapcode encodeToShortest(
            final double latDeg,
            final double lonDeg) throws IllegalArgumentException {
        checkRange("latDeg", latDeg, Point.LAT_DEG_MIN, Point.LAT_DEG_MAX);
        checkRange("lonDeg", lonDeg, Point.LON_DEG_MIN, Point.LON_DEG_MAX);

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results = Encoder.encode(latDeg, lonDeg, null, false, true, true);
        assert results != null;
        assert results.size() == 1;
        return results.get(0);
    }

    /**
     * Encode a lat/lon pair to its shortest mapcode with territory information.
     *
     * @param latDeg              Latitude, accepted range: -90..90.
     * @param lonDeg              Longitude, accepted range: -180..180.
     * @param restrictToTerritory Try to encode only within this territory, see {@link Territory}. Cannot
     *                            be null.
     * @return Shortest mapcode, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     * @throws UnknownMapcodeException  Thrown if no mapcode was found for the lat/lon matching the territory.
     */
    @Nonnull
    public static Mapcode encodeToShortest(
            final double latDeg,
            final double lonDeg,
            @Nonnull final Territory restrictToTerritory) throws IllegalArgumentException, UnknownMapcodeException {
        checkRange("latDeg", latDeg, Point.LAT_DEG_MIN, Point.LAT_DEG_MAX);
        checkRange("lonDeg", lonDeg, Point.LON_DEG_MIN, Point.LON_DEG_MAX);
        checkNonnull("restrictToTerritory", restrictToTerritory);

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results =
                Encoder.encode(latDeg, lonDeg, restrictToTerritory, false, true, false);
        assert results != null;
        assert results.size() <= 1;
        if (results.isEmpty()) {
            throw new UnknownMapcodeException("No Mapcode for lat=" + latDeg + ", lon=" + lonDeg +
                    ", territory=" + restrictToTerritory);
        }
        return results.get(0);
    }

    /**
     * Encode a lat/lon pair to its unambiguous, international mapcode.
     *
     * @param latDeg Latitude, accepted range: -90..90.
     * @param lonDeg Longitude, accepted range: -180..180.
     * @return International unambiguous mapcode (always exists), see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static Mapcode encodeToInternational(
            final double latDeg,
            final double lonDeg) throws IllegalArgumentException {
        checkRange("latDeg", latDeg, Point.LAT_DEG_MIN, Point.LAT_DEG_MAX);
        checkRange("lonDeg", lonDeg, Point.LON_DEG_MIN, Point.LON_DEG_MAX);

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results = encode(latDeg, lonDeg, Territory.AAA);
        assert results != null;
        assert results.size() >= 1;
        return results.get(results.size() - 1);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * Decoding mapcodes back to latitude, longitude.
     * ------------------------------------------------------------------------------------------
     */

    /**
     * Decode a mapcode to a Point. The decoding process may fail for local mapcodes,
     * because no territory context is supplied (world-wide).
     * <p/>
     * The accepted format is:
     * {mapcode}
     * {territory-code} {mapcode}
     *
     * @param mapcode Mapcode.
     * @return Point corresponding to mapcode.
     * @throws UnknownMapcodeException  Thrown if the mapcode has the correct syntax,
     *                                  but cannot be decoded into a point.
     * @throws IllegalArgumentException Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Point decode(
            @Nonnull final String mapcode) throws UnknownMapcodeException, IllegalArgumentException {
        checkNonnull("mapcode", mapcode);
        String mapcodeTrimmed = mapcode.trim();
        final int space = mapcodeTrimmed.indexOf(' ');
        final Territory territory;
        if ((space > 0) && (mapcodeTrimmed.length() > space)) {

            // Get territory from mapcode.
            final String territoryName = mapcodeTrimmed.substring(0, space).trim();
            try {
                territory = Territory.fromString(territoryName);
            } catch (final UnknownTerritoryException ignored) {
                throw new UnknownMapcodeException("Wrong territory code: " + territoryName);
            }
            mapcodeTrimmed = mapcode.substring(space + 1).trim();
        } else {
            territory = Territory.AAA;
        }
        if (!Mapcode.isValidMapcodeFormat(mapcodeTrimmed)) {
            throw new IllegalArgumentException(mapcode + " is not a correctly formatted mapcode; " +
                    "the regular expression for the mapcode syntax is: " + Mapcode.REGEX_MAPCODE_FORMAT);
        }
        return decode(mapcodeTrimmed, territory);
    }

    /**
     * Decode a mapcode to a Point. A reference territory is supplied for disambiguation (only used if applicable).
     * <p/>
     * The accepted format is:
     * {mapcode}        (note that a territory code is not allowed here)
     *
     * @param mapcode          Mapcode.
     * @param territoryContext Territory for disambiguation purposes.
     * @return Point corresponding to mapcode. Latitude range: -90..90, longitude range: -180..180.
     * @throws UnknownMapcodeException  Thrown if the mapcode has the right syntax, but cannot be decoded into a point.
     * @throws IllegalArgumentException Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Point decode(
            @Nonnull final String mapcode,
            @Nonnull final Territory territoryContext) throws UnknownMapcodeException, IllegalArgumentException {
        checkNonnull("mapcode", mapcode);
        checkNonnull("territoryContext", territoryContext);
        final String mapcodeTrimmed = mapcode.trim();
        if (!Mapcode.isValidMapcodeFormat(mapcodeTrimmed)) {
            throw new IllegalArgumentException(mapcode + " is not a correctly formatted mapcode; " +
                    "the regular expression for the mapcode syntax is: " + Mapcode.REGEX_MAPCODE_FORMAT);
        }

        @Nonnull final Point point = Decoder.decode(mapcodeTrimmed, territoryContext);
        assert point != null;

        // Points can only be undefined within the mapcode implementation. Throw an exception here if undefined.
        if (!point.isDefined()) {
            throw new UnknownMapcodeException("Unknown Mapcode: " + mapcodeTrimmed +
                    ", territoryContext=" + territoryContext);
        }
        assert (Point.LAT_DEG_MIN <= point.getLatDeg()) && (point.getLatDeg() <= Point.LAT_DEG_MAX) : point.getLatDeg();
        assert (Point.LON_DEG_MIN <= point.getLonDeg()) && (point.getLonDeg() <= Point.LON_DEG_MAX) : point.getLonDeg();
        return point;
    }
}
