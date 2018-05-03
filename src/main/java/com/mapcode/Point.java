/*
 * Copyright (C) 2014-2017, Stichting Mapcode Foundation (http://www.mapcode.com)
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
import java.util.Random;

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This class defines a class for lat/lon points.
 *
 * Internally, the class implements a fixed-point representation where a coordinate is expressed in
 * "fractions", of 1/3.240,000,000,000th of a degree. A double (an IEEE 754-1985 binary64) is just
 * sufficient to represent coordinates between -180 and +180 degrees in such fractions.
 * However, for applications that use micro-degrees a lot, the implementation below is more efficient.
 * It represent the fractions in pairs of integers, the first integer
 * representing 1/1,000,000th of degrees, the second representing the remainder.
 */
@SuppressWarnings("MagicNumber")
public final class Point {

    // Latitude and longitude ranges.
    public static final double LON_DEG_MIN = -180.0;
    public static final double LON_DEG_MAX = 180.0;
    public static final double LAT_DEG_MIN = -90.0;
    public static final double LAT_DEG_MAX = 90.0;

    // Conversion constants.
    public static final int DEG_TO_MICRO_DEG = 1000000;
    public static final int MICRO_DEG_90 = 90 * DEG_TO_MICRO_DEG;
    public static final int MICRO_DEG_180 = 180 * DEG_TO_MICRO_DEG;
    public static final int MICRO_DEG_360 = 360 * DEG_TO_MICRO_DEG;

    // Radius of Earth.
    public static final double EARTH_RADIUS_X_METERS = 6378137.0;
    public static final double EARTH_RADIUS_Y_METERS = 6356752.3;

    // Circumference of Earth.
    public static final double EARTH_CIRCUMFERENCE_X = EARTH_RADIUS_X_METERS * 2.0 * Math.PI;
    public static final double EARTH_CIRCUMFERENCE_Y = EARTH_RADIUS_Y_METERS * 2.0 * Math.PI;

    // Meters per degree latitude is fixed. For longitude: use factor * cos(midpoint of two degree latitudes).
    public static final double METERS_PER_DEGREE_LAT = EARTH_CIRCUMFERENCE_Y / 360.0;
    public static final double METERS_PER_DEGREE_LON_EQUATOR = EARTH_CIRCUMFERENCE_X / 360.0; // * cos(deg(lat)).

    /**
     * Create a point from lat/lon in degrees (may be precision!)
     *
     * @param latDeg Latitude in degrees. Range: [-90, 90].
     * @param lonDeg Longitude in degrees. Range: [-180, 180).
     * @return A defined point.
     */
    @Nonnull
    public static Point fromDeg(final double latDeg, final double lonDeg) {
        return new Point(latDeg, lonDeg);
    }

    /**
     * Public construction, from integer microdegrees (no loss of precision).
     *
     * @param latMicroDeg Latitude, in microdegrees.
     * @param lonMicroDeg Longitude, in microdegrees.
     * @return A defined point.
     */
    @Nonnull
    public static Point fromMicroDeg(final int latMicroDeg, final int lonMicroDeg) {
        final Point p = new Point();
        p.latMicroDeg = latMicroDeg;
        p.latFractionOnlyDeg = 0;
        p.lonMicroDeg = lonMicroDeg;
        p.lonFractionOnlyDeg = 0;
        p.defined = true;
        return p.wrap();
    }

    /**
     * Get the latitude in degrees (may lose precision).
     *
     * @return Latitude in degrees. No range is enforced.
     */
    public double getLatDeg() {
        assert defined;
        return (latMicroDeg / MICRODEG_TO_DEG_FACTOR) + (latFractionOnlyDeg / LAT_TO_FRACTIONS_FACTOR);
    }

    /**
     * Get the longitude in degrees (may lose precision).
     *
     * @return Longitude in degrees. No range is enforced.
     */
    public double getLonDeg() {
        assert defined;
        return (lonMicroDeg / MICRODEG_TO_DEG_FACTOR) + (lonFractionOnlyDeg / LON_TO_FRACTIONS_FACTOR);
    }

    /**
     * Get latitude as micro-degrees. Note that this looses precision beyond microdegrees!
     *
     * @return floor(Latitude in microdegrees)
     */
    public int getLatMicroDeg() {
        assert defined;
        return latMicroDeg;
    }

    /**
     * Get longitude as micro-degrees. Note that this looses precision beyond microdegrees!
     *
     * @return floor(Longitude in microdegrees)
     */
    public int getLonMicroDeg() {
        assert defined;
        return lonMicroDeg;
    }

    /**
     * Create a random point, uniformly distributed over the surface of the Earth.
     *
     * @param randomGenerator Random generator used to create a point.
     * @return Random point with uniform distribution over the sphere.
     */
    @Nonnull
    public static Point fromUniformlyDistributedRandomPoints(@Nonnull final Random randomGenerator) {
        checkNonnull("randomGenerator", randomGenerator);

        // Calculate uniformly distributed 3D point on sphere (radius = 1.0):
        // http://mathproofs.blogspot.co.il/2005/04/uniform-random-distribution-on-sphere.html
        final double unitRand1 = randomGenerator.nextDouble();
        final double unitRand2 = randomGenerator.nextDouble();
        final double theta0 = (2.0 * Math.PI) * unitRand1;
        final double theta1 = Math.acos(1.0 - (2.0 * unitRand2));
        final double x = Math.sin(theta0) * Math.sin(theta1);
        final double y = Math.cos(theta0) * Math.sin(theta1);
        final double z = Math.cos(theta1);

        // Convert Carthesian 3D point into lat/lon (radius = 1.0):
        // http://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates
        final double latRad = Math.asin(z);
        final double lonRad = Math.atan2(y, x);

        // Convert radians to degrees.
        assert !Double.isNaN(latRad);
        assert !Double.isNaN(lonRad);
        final double lat = latRad * (180.0 / Math.PI);
        final double lon = lonRad * (180.0 / Math.PI);
        return fromDeg(lat, lon);
    }

    /**
     * Calculate the distance between two points. This algorithm does not take the curvature of the Earth into
     * account, so it only works for small distance up to, say 200 km, and not too close to the poles.
     *
     * @param p1 Point 1.
     * @param p2 Point 2.
     * @return Straight distance between p1 and p2. Only accurate for small distances up to 200 km.
     */
    public static double distanceInMeters(@Nonnull final Point p1, @Nonnull final Point p2) {
        checkNonnull("p1", p1);
        checkNonnull("p2", p2);

        final Point from;
        final Point to;
        if (p1.getLonDeg() <= p2.getLonDeg()) {
            from = p1;
            to = p2;
        } else {
            from = p2;
            to = p1;
        }

        // Calculate mid point of 2 latitudes.
        final double avgLat = (from.getLatDeg() + to.getLatDeg()) / 2.0;

        final double deltaLatDeg = Math.abs(to.getLatDeg() - from.getLatDeg());
        final double deltaLonDeg360 = Math.abs(to.getLonDeg() - from.getLonDeg());
        final double deltaLonDeg = ((deltaLonDeg360 <= 180.0) ? deltaLonDeg360 : (360.0 - deltaLonDeg360));

        // Meters per longitude is fixed; per latitude requires * cos(avg(lat)).
        final double deltaXMeters = degreesLonToMetersAtLat(deltaLonDeg, avgLat);
        final double deltaYMeters = degreesLatToMeters(deltaLatDeg);


        // Calculate length through Earth. This is an approximation, but works fine for short distances.
        return Math.sqrt((deltaXMeters * deltaXMeters) + (deltaYMeters * deltaYMeters));
    }

    public static double degreesLatToMeters(final double latDegrees) {
        return latDegrees * METERS_PER_DEGREE_LAT;
    }

    public static double degreesLonToMetersAtLat(final double lonDegrees, final double lat) {
        return lonDegrees * METERS_PER_DEGREE_LON_EQUATOR * Math.cos(Math.toRadians(lat));
    }

    public static double metersToDegreesLonAtLat(final double eastMeters, final double lat) {
        return (eastMeters / METERS_PER_DEGREE_LON_EQUATOR) / Math.cos(Math.toRadians(lat));
    }

    @Nonnull
    @Override
    public String toString() {
        return defined ? ("(" + getLatDeg() + ", " + getLonDeg() + ')') : "undefined";
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{latMicroDeg, lonMicroDeg, latFractionOnlyDeg, lonFractionOnlyDeg, defined});
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }
        final Point that = (Point) obj;
        return (this.latMicroDeg == that.latMicroDeg) &&
                (this.lonMicroDeg == that.lonMicroDeg) &&
                (this.latFractionOnlyDeg == that.latFractionOnlyDeg) &&
                (this.lonFractionOnlyDeg == that.lonFractionOnlyDeg) &&
                (this.defined == that.defined);
    }

    // -----------------------------------------------------------------------
    // (Package) private data and methods.
    // -----------------------------------------------------------------------

    // Constants to convert between Degrees, MicroDegrees and Fractions
    static final double MICRODEG_TO_DEG_FACTOR = 1000000.0;
    static final double MAX_PRECISION_FACTOR = 810000.0;
    static final double LAT_MICRODEG_TO_FRACTIONS_FACTOR = MAX_PRECISION_FACTOR;
    static final double LON_MICRODEG_TO_FRACTIONS_FACTOR = MAX_PRECISION_FACTOR * 4;
    static final double LAT_TO_FRACTIONS_FACTOR = MICRODEG_TO_DEG_FACTOR * LAT_MICRODEG_TO_FRACTIONS_FACTOR;
    static final double LON_TO_FRACTIONS_FACTOR = MICRODEG_TO_DEG_FACTOR * LON_MICRODEG_TO_FRACTIONS_FACTOR;

    private int latMicroDeg;            // Whole nr of MICRODEG_TO_DEG_FACTOR.
    private int lonMicroDeg;            // Whole nr of MICRODEG_TO_DEG_FACTOR.
    private int latFractionOnlyDeg;     // Whole nr of LAT_TO_FRACTIONS_FACTOR, relative to latMicroDeg.
    private int lonFractionOnlyDeg;     // Whole nr of LON_TO_FRACTIONS_FACTOR, relative to lonMicroDeg.

    /**
     * Points can be "undefined" within the mapcode implementation, but never outside of that.
     * Any methods creating or setting undefined points must be package private and external
     * interfaces must never pass undefined points to callers.
     */
    private boolean defined;

    /**
     * Private constructors.
     */
    private Point() {
        defined = false;
    }

    /**
     * Public construction, from floating point degrees (potentially lossy).
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private Point(final double latDeg, final double lonDeg) {

        double lat = latDeg + 90;
        if (lat < 0) {
            lat = 0;
        } else if (lat > 180) {
            lat = 180;
        }

        // Rounding factor.
        final double fractionRounding = 0.1;

        // Lat now [0..180].
        lat = lat * LAT_TO_FRACTIONS_FACTOR;
        double latFractionOnly = Math.floor(lat + fractionRounding);
        latMicroDeg = (int) (latFractionOnly / LAT_MICRODEG_TO_FRACTIONS_FACTOR);
        latFractionOnly = latFractionOnly - ((double) latMicroDeg * LAT_MICRODEG_TO_FRACTIONS_FACTOR);
        latFractionOnlyDeg = (int) latFractionOnly;
        latMicroDeg = latMicroDeg - MICRO_DEG_90;

        // Math.floor has limited precision for really large values, so we need to limit the lon explicitly.
        double lon = Math.min(360.0, Math.max(0.0, lonDeg - (360.0 * Math.floor(lonDeg / 360.0))));
        if (Double.compare(lon, 360.0) == 0) {
            lon = 0.0;
        }

        // Lon now in [0..360>.
        lon = lon * LON_TO_FRACTIONS_FACTOR;
        double lonFractionOnly = Math.floor(lon + fractionRounding);
        lonMicroDeg = (int) (lonFractionOnly / LON_MICRODEG_TO_FRACTIONS_FACTOR);
        lonFractionOnly = lonFractionOnly - ((double) lonMicroDeg * LON_MICRODEG_TO_FRACTIONS_FACTOR);
        lonFractionOnlyDeg = (int) lonFractionOnly;

        // Wrap lonMicroDeg from [0..360> to [-180..180).
        if (lonMicroDeg >= MICRO_DEG_180) {
            lonMicroDeg = lonMicroDeg - MICRO_DEG_360;
        }

        defined = true;
    }

    /**
     * Get the the longitude "fractions", which is a whole number of 1/LON_TO_FRACTIONS_FACTOR-th
     * degrees versus the millionths of degrees.
     */
    int getLonFraction() {
        assert defined;
        return lonFractionOnlyDeg;
    }

    /**
     * Get the the latitude "fractions", which is a whole number of 1/LAT_TO_FRACTIONS_FACTOR-th
     * degrees versus the millionths of degrees
     */
    int getLatFraction() {
        assert defined;
        return latFractionOnlyDeg;
    }

    /**
     * Package private construction, from integer fractions (no loss of precision).
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Nonnull
    static Point fromLatLonFractions(final double latFraction, final double lonFraction) {
        final Point p = new Point();
        p.latMicroDeg = (int) Math.floor(latFraction / LAT_MICRODEG_TO_FRACTIONS_FACTOR);
        p.latFractionOnlyDeg = (int) (latFraction - (LAT_MICRODEG_TO_FRACTIONS_FACTOR * p.latMicroDeg));
        p.lonMicroDeg = (int) Math.floor(lonFraction / LON_MICRODEG_TO_FRACTIONS_FACTOR);
        p.lonFractionOnlyDeg = (int) (lonFraction - (LON_MICRODEG_TO_FRACTIONS_FACTOR * p.lonMicroDeg));
        p.defined = true;
        return p.wrap();
    }

    static int degToMicroDeg(final double deg) {
        //noinspection NumericCastThatLosesPrecision
        return (int) Math.floor(deg * MICRODEG_TO_DEG_FACTOR);
    }

    static double microDegToDeg(final int microDeg) {
        return ((double) microDeg) / MICRODEG_TO_DEG_FACTOR;
    }

    @Nonnull
    Point wrap() {
        if (defined) {
            // Cut latitude to [-90, 90].
            if (latMicroDeg < -MICRO_DEG_90) {
                latMicroDeg = -MICRO_DEG_90;
                latFractionOnlyDeg = 0;
            }
            if (latMicroDeg > MICRO_DEG_90) {
                latMicroDeg = MICRO_DEG_90;
                latFractionOnlyDeg = 0;
            }
            // Map longitude to [-180, 180). Values outside this range are wrapped to this range.
            lonMicroDeg %= MICRO_DEG_360;
            if (lonMicroDeg >= MICRO_DEG_180) {
                lonMicroDeg -= MICRO_DEG_360;
            } else if (lonMicroDeg < -MICRO_DEG_180) {
                lonMicroDeg += MICRO_DEG_360;
            }
        }
        return this;
    }

    /**
     * Create an undefined points. No latitude or longitude can be obtained from it.
     * Only within the mapcode implementation points can be undefined, so this methods is package private.
     *
     * @return Undefined points.
     */
    @Nonnull
    static Point undefined() {
        return new Point();
    }

    /**
     * Set a point to be undefined, invalidating the latitude and longitude.
     * Only within the mapcode implementation points can be undefined, so this methods is package private.
     */
    void setUndefined() {
        defined = false;
    }

    /**
     * Return whether the point is defined or not.
     * Only within the mapcode implementation points can be undefined, so this methods is package private.
     *
     * @return True if defined. If false, no lat/lon is available.
     */
    boolean isDefined() {
        return defined;
    }
}
