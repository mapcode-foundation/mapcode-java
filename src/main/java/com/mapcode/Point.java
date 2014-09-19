/*
 * Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)
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
import java.util.Random;

import static com.mapcode.CheckArgs.checkNonnull;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class defines a class for lat/lon points.
 */
public class Point {

    // Latitude and longitude ranges.
    public static final double LON_DEG_MIN = -180.0;
    public static final double LON_DEG_MAX = 180.0;
    public static final double LAT_DEG_MIN = -90.0;
    public static final double LAT_DEG_MAX = 90.0;

    public static final int LON_MICRODEG_MIN = degToMicroDeg(LON_DEG_MIN);
    public static final int LON_MICRODEG_MAX = degToMicroDeg(LON_DEG_MAX);
    public static final int LAT_MICRODEG_MIN = degToMicroDeg(LAT_DEG_MIN);
    public static final int LAT_MICRODEG_MAX = degToMicroDeg(LAT_DEG_MAX);

    public static final double MICRODEG_TO_DEG_FACTOR = 1000000.0;

    // Radius of Earth.
    public static final double EARTH_RADIUS_X_METERS = 6378137.0;
    public static final double EARTH_RADIUS_Y_METERS = 6356752.3;

    // Circumference of Earth.
    public static final double EARTH_CIRCUMFERENCE_X = EARTH_RADIUS_X_METERS * 2.0 * Math.PI;
    public static final double EARTH_CIRCUMFERENCE_Y = EARTH_RADIUS_Y_METERS * 2.0 * Math.PI;

    // Meters per degree latitude is fixed. For longitude: use factor * cos(midpoint of two degree latitudes).
    public static final double METERS_PER_DEGREE_LAT         = EARTH_CIRCUMFERENCE_Y / 360.0;
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

    /**
     * Create a point from lat/lon in micro-degrees (i.e. degrees * 1,000,000).
     *
     * @param latMicroDeg Longitude in microdegrees.
     * @param lonMicroDeg Latitude in microdegrees.
     * @return A defined point.
     */
    @Nonnull
    public static Point fromMicroDeg(final int latMicroDeg, final int lonMicroDeg) {
        return new Point(microDegToDeg(latMicroDeg), microDegToDeg(lonMicroDeg));
    }

    /**
     * Get the latitude in microdegrees.
     *
     * @return Latitude in microdegrees. No range is enforced.
     */
    public int getLatMicroDeg() {
        assert defined;
        return degToMicroDeg(latDeg);
    }

    /**
     * Get the longitude in microdegrees.
     *
     * @return Longitude in microdegrees. No range is enforced.
     */
    public int getLonMicroDeg() {
        assert defined;
        return degToMicroDeg(lonDeg);
    }

    /**
     * Get the latitude in degrees.
     *
     * @return Latitude in degrees. No range is enforced.
     */
    public double getLatDeg() {
        assert defined;
        return latDeg;
    }

    /**
     * Get the longitude in degrees.
     *
     * @return Longitude in degrees. No range is enforced.
     */
    public double getLonDeg() {
        assert defined;
        return lonDeg;
    }

    @Override
    @Nonnull
    public String toString() {
        return "(" + latDeg + ", " + lonDeg + ')';
    }

    public static int degToMicroDeg(final double deg) {
        //noinspection NumericCastThatLosesPrecision
        return (int) Math.round(deg * MICRODEG_TO_DEG_FACTOR);
    }

    public static double microDegToDeg(final int microDeg) {
        return ((double) microDeg) / MICRODEG_TO_DEG_FACTOR;
    }

    @Nonnull
    public static Point restrictLatLon(@Nonnull final Point point) {
        if (!point.defined) {
            return undefined();
        }
        final double latDeg = Math.max(Math.min(LAT_DEG_MAX, point.getLatDeg()), LAT_DEG_MIN);
        final double lonDeg = Math.max(Math.min(LON_DEG_MAX, point.getLonDeg()), LON_DEG_MIN);
        return new Point(latDeg, lonDeg);
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
        final double lat = Double.isNaN(latRad) ? 90.0 : (latRad * (180.0 / Math.PI));
        final double lon = Double.isNaN(lonRad) ? 180.0 : (lonRad * (180.0 / Math.PI));
        return fromMicroDeg(degToMicroDeg(lat), degToMicroDeg(lon));
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
        if (p1.lonDeg <= p2.lonDeg) {
            from = p1;
            to = p2;
        }
        else {
            from = p2;
            to = p1;
        }

        // Calculate mid point of 2 latitudes.
        final double avgLat = from.latDeg + ((to.latDeg - from.latDeg) / 2.0);

        final double deltaLonDeg360 = Math.abs(to.lonDeg - from.lonDeg);
        final double deltaLonDeg = ((deltaLonDeg360 <= 180.0) ? deltaLonDeg360 : (360.0 - deltaLonDeg360));
        final double deltaLatDeg = Math.abs(to.latDeg - from.latDeg);

        // Meters per longitude is fixed; per latitude requires * cos(avg(lat)).
        final double deltaXMeters = degreesLonToMetersAtLat(deltaLonDeg, avgLat);
        final double deltaYMeters = degreesLatToMeters(deltaLatDeg);

        // Calculate length through Earth. This is an approximation, but works fine for short distances.
        final double lenMeters = Math.sqrt((deltaXMeters * deltaXMeters) + (deltaYMeters * deltaYMeters));
        return lenMeters;
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

    /**
     * Private data.
     */
    private double latDeg;     // Latitude, normal range -90..90, but not enforced.
    private double lonDeg;     // Longitude, normal range -180..180, but not enforced.

    /**
     * Points can be "undefined" within the Mapcode implementation, but never outside of that.
     * Any methods creating or setting undefined points must be package private and external
     * interfaces must never pass undefined points to callers.
     */
    private boolean defined;

    /**
     * Private constructors.
     */
    private Point() {
        latDeg = Double.NaN;
        lonDeg = Double.NaN;
        defined = false;
    }

    private Point(final double latDeg, final double lonDeg) {
        // These assertions are not valid, as the ranges cannot be enforced currently:
        // assert (LON_DEG_MIN <= lonDeg) && (lonDeg <= LON_DEG_MAX) : "lon [-180..180]: " + lonDeg;
        // assert (LAT_DEG_MIN <= latDeg) && (latDeg <= LAT_DEG_MAX) : "lat [-90..90]: " + latDeg;
        this.latDeg = latDeg;
        this.lonDeg = lonDeg;
        this.defined = true;
    }

    /**
     * Package private methods. Only used in the Mapcode implementation modules.
     */

    /**
     * Create an undefined points. No latitude or longitude can be obtained from it.
     * Only within the Mapcode implementation points can be undefined, so this methods is package private.
     *
     * @return Undefined points.
     */
    @Nonnull
    static Point undefined() {
        return new Point();
    }

    /**
     * Set a point to be undefined, invalidating the latitude and longitude.
     * Only within the Mapcode implementation points can be undefined, so this methods is package private.
     */
    void setUndefined() {
        latDeg = Double.NaN;
        lonDeg = Double.NaN;
        defined = false;
    }

    /**
     * Return whether the point is defined or not.
     * Only within the Mapcode implementation points can be undefined, so this methods is package private.
     *
     * @return True if defined. If false, no lat/lon is available.
     */
    boolean isDefined() {
        return defined;
    }
}
