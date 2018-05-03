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

// ----------------------------------------------------------------------------------------------
// Package private implementation class. For internal use within the Mapcode implementation only.
// ----------------------------------------------------------------------------------------------

/**
 * Simple class to represent all the coordinates that would deliver a particular mapcode.
 */
class MapcodeZone {

    // TODO: Explain why you need these fractions and how they work exactly.
    // Longitudes in LonFractions ("1/3240 billionths").
    private double lonFractionMin;
    private double lonFractionMax;

    // Latitudes in LatFractions ("1/810 billionths").
    private double latFractionMin;
    private double latFractionMax;

    MapcodeZone(final double latFractionMin, final double latFractionMax,
                final double lonFractionMin, final double lonFractionMax) {
        this.latFractionMin = latFractionMin;
        this.latFractionMax = latFractionMax;
        this.lonFractionMin = lonFractionMin;
        this.lonFractionMax = lonFractionMax;
    }

    MapcodeZone(@Nonnull final MapcodeZone mapcodeZone) {
        this(mapcodeZone.latFractionMin, mapcodeZone.latFractionMax, mapcodeZone.lonFractionMin, mapcodeZone.lonFractionMax);
    }

    MapcodeZone() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    double getLonFractionMin() {
        return lonFractionMin;
    }

    double getLonFractionMax() {
        return lonFractionMax;
    }

    double getLatFractionMin() {
        return latFractionMin;
    }

    double getLatFractionMax() {
        return latFractionMax;
    }

    void setLonFractionMin(final double lonFractionMin) {
        this.lonFractionMin = lonFractionMin;
    }

    void setLonFractionMax(final double lonFractionMax) {
        this.lonFractionMax = lonFractionMax;
    }

    void setLatFractionMin(final double latFractionMin) {
        this.latFractionMin = latFractionMin;
    }

    void setLatFractionMax(final double latFractionMax) {
        this.latFractionMax = latFractionMax;
    }

    // TODO: Use of this method is unclear.
    // Generate upper and lower limits based on x and y, and delta's.
    void setFromFractions(final double latFraction, final double lonFraction,
                          final double latFractionDelta, final double lonFractionDelta) {
        assert (lonFractionDelta >= 0.0);
        assert (latFractionDelta != 0.0);
        lonFractionMin = lonFraction;
        lonFractionMax = lonFraction + lonFractionDelta;
        if (latFractionDelta < 0) {
            latFractionMin = latFraction + 1 + latFractionDelta;    // y + yDelta can NOT be represented.
            latFractionMax = latFraction + 1;                       // y CAN be represented.
        } else {
            latFractionMin = latFraction;
            latFractionMax = latFraction + latFractionDelta;
        }
    }

    boolean isEmpty() {
        return ((lonFractionMax <= lonFractionMin) || (latFractionMax <= latFractionMin));
    }

    // TODO: Explain what this is (geo point of mapcode).
    @Nonnull
    Point getCenter() {
        if (isEmpty()) {
            return Point.undefined();
        } else {
            final double latFrac = Math.floor((latFractionMin + latFractionMax) / 2.0);
            final double lonFrac = Math.floor((lonFractionMin + lonFractionMax) / 2.0);
            return Point.fromLatLonFractions(latFrac, lonFrac);
        }
    }

    // TODO: Explain when this is used. It clips a zone to an encompassing boundary.
    // Returns a non-empty intersection of a mapcode zone and a territory area.
    // Returns null if no such intersection exists.
    @Nonnull
    MapcodeZone restrictZoneTo(@Nonnull final Boundary area) {
        final MapcodeZone mapcodeZone = new MapcodeZone(latFractionMin, latFractionMax, lonFractionMin, lonFractionMax);
        final double latMin = area.getLatMicroDegMin() * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR;
        if (mapcodeZone.latFractionMin < latMin) {
            mapcodeZone.latFractionMin = latMin;
        }
        final double latMax = area.getLatMicroDegMax() * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR;
        if (mapcodeZone.latFractionMax > latMax) {
            mapcodeZone.latFractionMax = latMax;
        }
        if (mapcodeZone.latFractionMin < mapcodeZone.latFractionMax) {
            double lonMin = area.getLonMicroDegMin() * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR;
            double lonMax = area.getLonMicroDegMax() * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR;
            if ((lonMax < 0) && (mapcodeZone.lonFractionMin > 0)) {
                lonMin += (Point.MICRO_DEG_360 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
                lonMax += (Point.MICRO_DEG_360 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
            } else if ((lonMin > 1) && (mapcodeZone.lonFractionMax < 0)) {
                lonMin -= (Point.MICRO_DEG_360 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
                lonMax -= (Point.MICRO_DEG_360 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
            }
            if (mapcodeZone.lonFractionMin < lonMin) {
                mapcodeZone.lonFractionMin = lonMin;
            }
            if (mapcodeZone.lonFractionMax > lonMax) {
                mapcodeZone.lonFractionMax = lonMax;
            }
        }
        return mapcodeZone;
    }

    @Nonnull
    @Override
    public String toString() {
        return isEmpty() ? "empty" : ("[" + (latFractionMin / Point.LAT_TO_FRACTIONS_FACTOR) + ", " +
                (latFractionMax / Point.LAT_TO_FRACTIONS_FACTOR) +
                "), [" + (lonFractionMin / Point.LON_TO_FRACTIONS_FACTOR) + ", " +
                (lonFractionMax / Point.LON_TO_FRACTIONS_FACTOR) + ')');
    }
}
