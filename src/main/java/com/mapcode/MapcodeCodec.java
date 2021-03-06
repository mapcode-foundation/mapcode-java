/*
 * Copyright (C) 2016-2021, Stichting Mapcode Foundation (http://www.mapcode.com)
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
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.mapcode.CheckArgs.checkDefined;
import static com.mapcode.CheckArgs.checkNonnull;
import static com.mapcode.Mapcode.getPrecisionFormat;

// ----------------------------------------------------------------------------------------------
// Package private implementation class. For internal use within the mapcode implementation only.
//----------------------------------------------------------------------------------------------

/**
 * This class is the external Java interface for encoding and decoding mapcodes.
 */
@SuppressWarnings("MagicNumber")
public final class MapcodeCodec {

    // Get direct access to the data model.
    private static final DataModel DATA_MODEL = DataModel.getInstance();

    private MapcodeCodec() {
        // Prevent instantiation.
    }

    // ------------------------------------------------------------------------------------------
    // Encoding latitude, longitude to mapcodes.
    // ------------------------------------------------------------------------------------------

    /**
     * Encode a lat/lon pair to a mapcode with territory information. This produces a non-empty list of mapcode,
     * with at the very least 1 mapcodes for the lat/lon, which is the "International" mapcode.
     *
     * The returned result list will always contain at least 1 mapcode, because every lat/lon pair can be encoded.
     *
     * The list is ordered in such a way that the last result is the international code. However, you cannot assume
     * that the first result is the shortest mapcode. If you want to use the shortest mapcode, use
     * {@link #encodeToShortest(double, double, Territory)}.
     *
     * The international code can be obtained from the list by using: "results.get(results.size() - 1)", or
     * you can use {@link #encodeToInternational(double, double)}, which is faster.
     *
     * @param latDeg Latitude, accepted range: -90..90.
     * @param lonDeg Longitude, accepted range: -180..180.
     * @return Non-empty, ordered list of mapcode information records, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static List<Mapcode> encode(final double latDeg, final double lonDeg)
            throws IllegalArgumentException {
        return encode(latDeg, lonDeg, null);
    }

    @Nonnull
    public static List<Mapcode> encode(@Nonnull final Point point)
            throws IllegalArgumentException {
        checkDefined("point", point);
        return encode(point.getLatDeg(), point.getLonDeg());
    }

    /**
     * Encode a lat/lon pair to a mapcode with territory information, for a specific territory. This produces a
     * potentially empty list of mapcodes (empty if the lat/lon does not fall within the territory for mapcodes).
     *
     * The returned result list will always contain at least 1 mapcode, because every lat/lon pair can be encoded.
     *
     * The list is ordered in such a way that the last result is the international code. However, you cannot assume
     * that the first result is the shortest mapcode. If you want to use the shortest mapcode, use
     * {@link #encodeToShortest(double, double, Territory)}.
     *
     * @param latDeg              Latitude, accepted range: -90..90 (limited to this range if outside).
     * @param lonDeg              Longitude, accepted range: -180..180 (wrapped to this range if outside).
     * @param restrictToTerritory Try to encode only within this territory, see {@link Territory}. May be null.
     * @return List of mapcode information records, see {@link Mapcode}. This list is empty if no
     * Mapcode can be generated for this territory matching the lat/lon.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     */
    @Nonnull
    public static List<Mapcode> encode(final double latDeg, final double lonDeg,
                                       @Nullable final Territory restrictToTerritory)
            throws IllegalArgumentException {
        final List<Mapcode> results = Encoder.encode(latDeg, lonDeg, restrictToTerritory, false);
        assert results != null;
        return results;
    }

    @Nonnull
    public static List<Mapcode> encode(@Nonnull final Point point,
                                       @Nullable final Territory restrictToTerritory)
            throws IllegalArgumentException {
        checkNonnull("point", point);
        return encode(point.getLatDeg(), point.getLonDeg(), restrictToTerritory);
    }

    /**
     * Encode a lat/lon pair to a list of mapcodes, like {@link #encode(double, double)}.
     * The result list is limited to those mapcodes that belong to the provided ISO 3166 country code, 2 characters.
     * For example, if you wish to restrict the list to Mexican mapcodes, use "MX". This would
     * produce a result list of mapcodes with territories that start with "MX-" (note that a
     * mapcode that starts with "MEX" is not returned in that case.)
     *
     * @param latDeg      Latitude, accepted range: -90..90.
     * @param lonDeg      Longitude, accepted range: -180..180.
     * @param countryISO2 ISO 3166 country code, 2 characters.
     * @return Possibly empty, ordered list of mapcode information records, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range, or if the ISO code is invalid.
     */
    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO2(final double latDeg, final double lonDeg,
                                                            @Nonnull final String countryISO2)
            throws IllegalArgumentException {
        checkNonnull("countryISO2", countryISO2);
        final String countryISO3 = Territory.fromCountryISO2(countryISO2).toString();
        final String prefix = countryISO2.toUpperCase() + '-';
        final List<Mapcode> mapcodes = encode(latDeg, lonDeg);
        final List<Mapcode> filtered = new ArrayList<Mapcode>();
        for (final Mapcode mapcode : mapcodes) {

            if (mapcode.getTerritory().toString().startsWith(prefix)) {
                // If the mapcode starts with the ISO 2 code, it's OK.
                filtered.add(mapcode);

            } else if (mapcode.getTerritory().toString().equals(countryISO3)) {

                // Otherwise, if it's the correct country ISO 3 code, it's also OK.
                filtered.add(mapcode);
            }
        }
        return filtered;
    }

    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO2(@Nonnull final Point point,
                                                            @Nonnull final String countryISO2)
            throws IllegalArgumentException {
        checkNonnull("point", point);
        return encodeRestrictToCountryISO2(point.getLatDeg(), point.getLonDeg(), countryISO2);
    }

    /**
     * Encode a lat/lon pair to a list of mapcodes, like {@link #encode(double, double)}.
     * The result list is limited to those mapcodes that belong to the provided ISO 3166 country code, 3 characters.
     * For example, if you wish to restrict the list to Mexican mapcodes, use "MEX". This would
     * produce a result list of mapcodes with territories that start with "MEX" (note that
     * mapcode that starts with "MX-" are not returned in that case.)
     *
     * @param latDeg      Latitude, accepted range: -90..90.
     * @param lonDeg      Longitude, accepted range: -180..180.
     * @param countryISO3 ISO 3166 country code, 3 characters.
     * @return Possibly empty, ordered list of mapcode information records, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range, or if the ISO code is invalid.
     */
    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO3(final double latDeg, final double lonDeg,
                                                            @Nonnull final String countryISO3)
            throws IllegalArgumentException {
        checkNonnull("countryISO3", countryISO3);
        return encodeRestrictToCountryISO2(latDeg, lonDeg, Territory.getCountryISO2FromISO3(countryISO3));
    }

    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO3(@Nonnull final Point point,
                                                            @Nonnull final String countryISO3)
            throws IllegalArgumentException {
        checkNonnull("point", point);
        return encodeRestrictToCountryISO3(point.getLatDeg(), point.getLonDeg(), countryISO3);
    }

    /**
     * Encode a lat/lon pair to a list of mapcodes, like {@link #encode(double, double)}.
     * The result list is limited to those mapcodes that belong to the provided ISO 3166 country code, 2 or 3
     * characters.
     *
     * @param latDeg     Latitude, accepted range: -90..90.
     * @param lonDeg     Longitude, accepted range: -180..180.
     * @param countryISO ISO 3166 country code, 2 or 3 characters.
     * @return Possibly empty, ordered list of mapcode information records, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range, or if the ISO code is invalid.
     */
    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO(final double latDeg, final double lonDeg,
                                                           @Nonnull final String countryISO)
            throws IllegalArgumentException {
        checkNonnull("countryISO", countryISO);
        List<Mapcode> mapcodes;
        try {
            mapcodes = encodeRestrictToCountryISO2(latDeg, lonDeg, countryISO);
        } catch (final IllegalArgumentException ignored) {
            mapcodes = encodeRestrictToCountryISO3(latDeg, lonDeg, countryISO);
        }
        return mapcodes;
    }

    @Nonnull
    public static List<Mapcode> encodeRestrictToCountryISO(@Nonnull final Point point,
                                                           @Nonnull final String countryISO)
            throws IllegalArgumentException {
        checkNonnull("point", point);
        return encodeRestrictToCountryISO(point.getLatDeg(), point.getLonDeg(), countryISO);
    }

    /**
     * Encode a lat/lon pair to its shortest mapcode with territory information.
     *
     * @param latDeg              Latitude, accepted range: -90..90.
     * @param lonDeg              Longitude, accepted range: -180..180.
     * @param restrictToTerritory Try to encode only within this territory, see {@link Territory}. Cannot be null.
     * @return Shortest mapcode, see {@link Mapcode}.
     * @throws IllegalArgumentException Thrown if latitude or longitude are out of range.
     * @throws UnknownMapcodeException  Thrown if no mapcode was found for the lat/lon matching the territory.
     */
    @Nonnull
    public static Mapcode encodeToShortest(final double latDeg, final double lonDeg,
                                           @Nonnull final Territory restrictToTerritory)
            throws IllegalArgumentException, UnknownMapcodeException {
        checkNonnull("restrictToTerritory", restrictToTerritory);

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results =
                Encoder.encode(latDeg, lonDeg, restrictToTerritory, /* Stop with one result: */ true);
        assert results != null;
        assert results.size() <= 1;
        if (results.isEmpty()) {
            throw new UnknownMapcodeException("No Mapcode for lat=" + latDeg + ", lon=" + lonDeg +
                    ", territory=" + restrictToTerritory);
        }
        return results.get(0);
    }

    @Nonnull
    public static Mapcode encodeToShortest(@Nonnull final Point point,
                                           @Nonnull final Territory restrictToTerritory)
            throws IllegalArgumentException, UnknownMapcodeException {
        checkDefined("point", point);
        return encodeToShortest(point.getLatDeg(), point.getLonDeg(), restrictToTerritory);
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
    public static Mapcode encodeToInternational(final double latDeg, final double lonDeg)
            throws IllegalArgumentException {

        // Call mapcode encoder.
        @Nonnull final List<Mapcode> results = encode(latDeg, lonDeg, Territory.AAA);
        assert results != null;
        assert results.size() >= 1;
        return results.get(results.size() - 1);
    }

    @Nonnull
    public static Mapcode encodeToInternational(@Nonnull final Point point)
            throws IllegalArgumentException {
        checkDefined("point", point);
        return encodeToInternational(point.getLatDeg(), point.getLonDeg());
    }

    // ------------------------------------------------------------------------------------------
    // Decoding mapcodes back to latitude, longitude.
    // ------------------------------------------------------------------------------------------
    //

    /**
     * Decode a mapcode to a Point. The decoding process may fail for local mapcodes,
     * because no territory context is supplied (world-wide).
     *
     * The accepted format is:
     * {mapcode}
     * {territory-code} {mapcode}
     *
     * @param mapcode Mapcode.
     * @return Point corresponding to mapcode.
     * @throws UnknownMapcodeException         Thrown if the mapcode has the correct syntax,
     *                                         but cannot be decoded into a point.
     * @throws UnknownPrecisionFormatException Thrown if the precision format is incorrect.
     * @throws IllegalArgumentException        Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Point decode(@Nonnull final String mapcode)
            throws UnknownMapcodeException, IllegalArgumentException, UnknownPrecisionFormatException {
        return decode(mapcode, Territory.AAA);
    }

    /**
     * Decode a mapcode to a Point. A reference territory is supplied for disambiguation (only used if applicable).
     *
     * The accepted format is:
     * {mapcode}
     * {territory-code} {mapcode}
     *
     * Note that if a territory-code is supplied in the string, it takes preferences over the parameter.
     *
     * @param mapcode                 Mapcode.
     * @param defaultTerritoryContext Default territory context for disambiguation purposes. May be null.
     * @return Point corresponding to mapcode. Latitude range: -90..90, longitude range: -180..180.
     * @throws UnknownMapcodeException         Thrown if the mapcode has the right syntax, but cannot be decoded into a point.
     * @throws UnknownPrecisionFormatException Thrown if the precision format is incorrect.
     * @throws IllegalArgumentException        Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Point decode(@Nonnull final String mapcode, @Nullable final Territory defaultTerritoryContext)
            throws UnknownMapcodeException, IllegalArgumentException, UnknownPrecisionFormatException {
        checkNonnull("mapcode", mapcode);

        final MapcodeZone mapcodeZone = decodeToMapcodeZone(mapcode, defaultTerritoryContext);
        if (mapcodeZone.isEmpty()) {
            throw new UnknownMapcodeException("Unknown mapcode, mapcode=" + mapcode + ", territoryContext=" + defaultTerritoryContext);
        }
        return mapcodeZone.getCenter();
    }

    /**
     * Decode a mapcode to a Rectangle, which defines the valid zone for a mapcode. The boundaries of the
     * mapcode zone are inclusive for the South and West borders and exclusive for the North and East borders.
     * This is essentially the same call as a 'decode', except it returns a rectangle, rather than its center point.
     *
     * @param mapcode Mapcode.
     * @return Rectangle Mapcode zone. South/West borders are inclusive, North/East borders exclusive.
     * @throws UnknownMapcodeException         Thrown if the mapcode has the correct syntax,
     *                                         but cannot be decoded into a point.
     * @throws UnknownPrecisionFormatException Thrown if the precision format is incorrect.
     * @throws IllegalArgumentException        Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Rectangle decodeToRectangle(@Nonnull final String mapcode)
            throws UnknownMapcodeException, IllegalArgumentException, UnknownPrecisionFormatException {
        return decodeToRectangle(mapcode, Territory.AAA);
    }

    /**
     * Decode a mapcode to a Rectangle, which defines the valid zone for a mapcode. The boundaries of the
     * mapcode zone are inclusive for the South and West borders and exclusive for the North and East borders.
     * This is essentially the same call as a 'decode', except it returns a rectangle, rather than its center point.
     *
     * @param mapcode                 Mapcode.
     * @param defaultTerritoryContext Default territory context for disambiguation purposes. May be null.
     * @return Rectangle Mapcode zone. South/West borders are inclusive, North/East borders exclusive.
     * @throws UnknownMapcodeException         Thrown if the mapcode has the correct syntax,
     *                                         but cannot be decoded into a point.
     * @throws UnknownPrecisionFormatException Thrown if the precision format is incorrect.
     * @throws IllegalArgumentException        Thrown if arguments are null, or if the syntax of the mapcode is incorrect.
     */
    @Nonnull
    public static Rectangle decodeToRectangle(@Nonnull final String mapcode, @Nullable final Territory defaultTerritoryContext)
            throws UnknownMapcodeException, IllegalArgumentException, UnknownPrecisionFormatException {
        checkNonnull("mapcode", mapcode);
        final MapcodeZone mapcodeZone = decodeToMapcodeZone(mapcode, defaultTerritoryContext);
        final Point southWest = Point.fromLatLonFractions(mapcodeZone.getLatFractionMin(), mapcodeZone.getLonFractionMin());
        final Point northEast = Point.fromLatLonFractions(mapcodeZone.getLatFractionMax(), mapcodeZone.getLonFractionMax());
        final Rectangle rectangle = new Rectangle(southWest, northEast);
        assert rectangle.isDefined();
        return rectangle;
    }

    /**
     * Is coordinate near multiple territory borders?
     *
     * @param point     Latitude/Longitude in degrees.
     * @param territory Territory.
     * @return true Iff the coordinate is near more than one territory border (and thus encode(decode(M)) may not produce M).
     */
    public static boolean isNearMultipleBorders(@Nonnull final Point point, @Nonnull final Territory territory) {
        checkDefined("point", point);
        if (territory != Territory.AAA) {
            final int territoryNumber = territory.getNumber();
            if (territory.getParentTerritory() != null) {
                // There is a parent! check its borders as well...
                if (isNearMultipleBorders(point, territory.getParentTerritory())) {
                    return true;
                }
            }
            int nrFound = 0;
            final int fromTerritoryRecord = DATA_MODEL.getDataFirstRecord(territoryNumber);
            final int uptoTerritoryRecord = DATA_MODEL.getDataLastRecord(territoryNumber);
            for (int territoryRecord = uptoTerritoryRecord; territoryRecord >= fromTerritoryRecord; territoryRecord--) {
                if (!Data.isRestricted(territoryRecord)) {
                    final Boundary boundary = Boundary.createBoundaryForTerritoryRecord(territoryRecord);
                    final int xdiv8 = Common.xDivider(boundary.getLatMicroDegMin(), boundary.getLatMicroDegMax()) / 4;
                    if (boundary.extendBoundary(60, xdiv8).containsPoint(point)) {
                        if (!boundary.extendBoundary(-60, -xdiv8).containsPoint(point)) {
                            nrFound++;
                            if (nrFound > 1) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Nonnull
    private static MapcodeZone decodeToMapcodeZone(@Nonnull final String mapcode, @Nullable final Territory defaultTerritoryContext)
            throws UnknownMapcodeException, IllegalArgumentException {
        checkNonnull("mapcode", mapcode);
        String mapcodeClean = Mapcode.convertStringToPlainAscii(mapcode.trim()).toUpperCase();

        // Determine territory from mapcode.
        final Territory territory;
        final Matcher matcherTerritory = Mapcode.PATTERN_TERRITORY.matcher(mapcodeClean);
        if (matcherTerritory.find()) {

            // Use the territory code from the string.
            final String territoryName = mapcodeClean.substring(matcherTerritory.start(), matcherTerritory.end()).trim();
            try {
                territory = Territory.fromString(territoryName);
            } catch (final UnknownTerritoryException ignored) {
                throw new UnknownMapcodeException("Wrong territory code: " + territoryName);
            }

            // Cut off the territory part.
            mapcodeClean = mapcodeClean.substring(matcherTerritory.end()).trim();
        } else {

            // No territory code was supplied in the string, use specified territory context parameter.
            territory = (defaultTerritoryContext != null) ? defaultTerritoryContext : Territory.AAA;
        }

        // Throws an exception if the format is incorrect.
        getPrecisionFormat(mapcodeClean);
        return Decoder.decodeToMapcodeZone(mapcodeClean, territory);
    }
}
