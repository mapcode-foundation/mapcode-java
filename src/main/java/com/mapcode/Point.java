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
import java.util.Random;

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * This class defines a class for lat/lon points.
 */
public class Point {

    // Latitude and longitude ranges.
    public static final double LON_DEG_MIN = -180.0;
    public static final double LON_DEG_MAX = 180.0;
    public static final double LAT_DEG_MIN = -90.0;
    public static final double LAT_DEG_MAX = 90.0;

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
     * Create a point from lat/lon in degrees.
     *
     * @param latDeg Longitude in degrees.
     * @param lonDeg Latitude in degrees.
     * @return A defined point.
     */
    @Nonnull
    public static Point fromDeg(final double latDeg, final double lonDeg) {
        return new Point(latDeg, lonDeg);
    }

    @Nonnull
    public static Point fromPoint(@Nonnull final Point point) {
        Point p = new Point();
        p.lat32 = point.lat32;
        p.lon32 = point.lon32;
        p.fraclat = point.fraclat;
        p.fraclon = point.fraclon;
        p.defined = point.defined;
        return p;
    }

    // Constants to handle fractions without floating point error accumulation
    private static final double MICRODEG_TO_DEG_FACTOR = 1000000.0;
    public static final double MAX_PRECISION_FACTOR = 810000.0;
    private static final double FRACLON_PRECISION_FACTOR = 3240000.0;
    private static final double FRACLAT_PRECISION_FACTOR = 810000.0;
    private static final double MICROLAT_MAX_PRECISION_FACTOR = (MICRODEG_TO_DEG_FACTOR * FRACLAT_PRECISION_FACTOR);
    private static final double MICROLON_MAX_PRECISION_FACTOR = (MICRODEG_TO_DEG_FACTOR * FRACLON_PRECISION_FACTOR);

    /**
     * Get the latitude in degrees.
     *
     * @return Latitude in degrees. No range is enforced.
     */
    public double getLatDeg() {
        assert defined;
        return (lat32 / MICRODEG_TO_DEG_FACTOR) + (fraclat / MICROLAT_MAX_PRECISION_FACTOR);
    }

    /**
     * Get the longitude in degrees.
     *
     * @return Longitude in degrees. No range is enforced.
     */
    public double getLonDeg() {
        assert defined;
        return (lon32 / MICRODEG_TO_DEG_FACTOR) + (fraclon / MICROLON_MAX_PRECISION_FACTOR);
    }


    /**
     * Returns "fractions", which is a whole number of 1/MICROLON_MAX_PRECISION_FACTORth degrees versus the millionths of degrees
     */
    public double LonFractions() {
        assert defined;
        return fraclon;
    }
    /**
     * Returns "fractions", which is a whole number of 1/MICROLAT_MAX_PRECISION_FACTORth degrees versus the millionths of degrees
     */
    public double LatFractions() {
        assert defined;
        return fraclat;
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

        final double latDeg1 = p1.getLatDeg();
        final double latDeg2 = p2.getLatDeg();
        double lonDeg1 = p1.getLonDeg();
        double lonDeg2 = p2.getLonDeg();

        if (lonDeg1 < 0 && lonDeg2 > 1) { lonDeg1 += 360; }
        if (lonDeg2 < 0 && lonDeg1 > 1) { lonDeg2 += 360; }

        // Calculate mid point of 2 latitudes.
        final double avgLat = (p1.getLatDeg() + p2.getLatDeg()) / 2.0;

        final double deltaLatDeg = latDeg1 - latDeg2;
        final double deltaLonDeg = lonDeg1 - lonDeg2;

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
        return Arrays.hashCode(new Object[]{getLatDeg(), getLonDeg(), defined});
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
        return (this.lat32 == that.lat32) &&
               (this.lon32 == that.lon32) &&
               (Double.compare(this.fraclat, that.fraclat) == 0) &&
               (Double.compare(this.fraclon, that.fraclon) == 0) &&
               (this.defined == that.defined);
    }

    /**
     * Private data.
     */
    private int lat32;     // whole nr of MICRODEG_TO_DEG_FACTOR
    private int lon32;     // whole nr of MICRODEG_TO_DEG_FACTOR
    private double fraclat; // whole nr of MICROLAT_MAX_PRECISION_FACTOR, relative to lat32
    private double fraclon; // whole nr of MICROLON_MAX_PRECISION_FACTOR, relative to lon32

    public void setMaxLatToMicroDeg(final int maxMicroLat) {
        if ( (lat32 > maxMicroLat) || ((lat32 == maxMicroLat) && (fraclat>0)) ) {
          lat32 = maxMicroLat-1;
          fraclat = FRACLAT_PRECISION_FACTOR - 1;
        }
    }

    public void setMaxLonToMicroDeg(final int maxMicroLon) {
        if ( (lon32 > maxMicroLon) || ((lon32 == maxMicroLon) && (fraclon>0)) ) {
          lon32 = maxMicroLon-1;
          fraclon = FRACLON_PRECISION_FACTOR - 1;
        }
    }

    public void setMinLatToMicroDeg(final int minMicroLat) {
        if (lat32 < minMicroLat) {
            lat32 = minMicroLat;
            fraclat = 0;
        }
    }

    public void setMinLonToMicroDeg(final int minMicroLon) {
        if (lon32 < minMicroLon) {
            lon32 = minMicroLon;
            fraclon = 0;
        }
    }

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

    private Point(final double latDeg, final double lonDeg) {

        double lat = latDeg + 90;
        if (lat < 0) { lat = 0; } else if (lat > 180) { lat = 180; }
        // lat now [0..180]
        lat *= MICROLAT_MAX_PRECISION_FACTOR;
        fraclat  = Math.floor(lat + 0.1);
        double f = fraclat / FRACLAT_PRECISION_FACTOR;
        lat32 = (int) f;
        fraclat  -= ((double) lat32 * FRACLAT_PRECISION_FACTOR);
        lat32 -= 90000000;

        double lon = lonDeg - (360.0 * Math.floor(lonDeg / 360)); // lon now in [0..360>
        lon *= MICROLON_MAX_PRECISION_FACTOR;
        fraclon = Math.floor(lon + 0.1);
        f = fraclon / FRACLON_PRECISION_FACTOR;
        lon32 = (int)f;
        fraclon -= ((double) lon32 * FRACLON_PRECISION_FACTOR);
        if (lon32 >= 180000000) { lon32 -= 360000000; }

        defined = true;
    }

    /**
     * Package private methods. Only used in the mapcode implementation modules.
     */
    static final int LON_MICRODEG_MIN = degToMicroDeg(LON_DEG_MIN);
    static final int LON_MICRODEG_MAX = degToMicroDeg(LON_DEG_MAX);
    static final int LAT_MICRODEG_MIN = degToMicroDeg(LAT_DEG_MIN);
    static final int LAT_MICRODEG_MAX = degToMicroDeg(LAT_DEG_MAX);

    @Nonnull
    static Point fromMicroDeg(final int latMicroDeg, final int lonMicroDeg) {
        Point p = new Point();
        p.lat32 = latMicroDeg;
        p.lon32 = lonMicroDeg;
        p.fraclat = 0;
        p.fraclon = 0;
        p.defined = true;
        return p;
    }

    /**
     * Get latitude as micro-degrees. Note that this looses precision beyond microdegrees!
     *
     * @return Latitude in microdegrees (may loose precision if latitude has higher precision than microdegrees).
     */
    int getLatMicroDeg() {
        assert defined;
        return lat32;
    }

    /**
     * Get longitude as micro-degrees. Note that this looses precision beyond microdegrees!
     *
     * @return Longitude in microdegrees (may loose precision if longitude has higher precision than microdegrees).
     */
    int getLonMicroDeg() {
        assert defined;
        return lon32;
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
            if (lat32 < -90000000) { lat32 = -90000000; fraclat=0; }
            if (lat32 >  90000000) { lat32 =  90000000; fraclat=0; }
            // Map longitude to [-180, 180). Values outside this range are wrapped to this range.
            if (lon32 < -180000000 || lon32 >= 180000000 ) {
              lon32 -= 360 * (lon32 / 360); // [0..360)
              if (lon32 >= 180000000) { lon32 -= 360000000; } // [-180,180)
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
