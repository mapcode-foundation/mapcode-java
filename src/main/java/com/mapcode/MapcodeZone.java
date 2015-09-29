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

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 * Simple class to represent all the coordinates that would deliver a particular mapcode.
 */
class MapcodeZone {

    // Longitudes in LonFractions ("1/3240 billionths").
    private double lonFractionMin;
    private double lonFractionMax;

    // Latitudes in LatFractions ("1/810 billionths").
    private double latFractionMin;
    private double latFractionMax;

    // Construct an (empty) zone.
    MapcodeZone() {
        setEmpty();
    }

    // Construct an (empty) zone.
    void setEmpty() {
        latFractionMin = 0;
        latFractionMax = 0;
        lonFractionMin = 0;
        lonFractionMax = 0;
    }

    // Construct a copy of an existing zone.
    MapcodeZone(@Nonnull final MapcodeZone zone) {
        copyFrom(zone);
    }

    // construct a copy of an existing zone
    void copyFrom(@Nonnull final MapcodeZone other) {
        latFractionMin = other.latFractionMin;
        latFractionMax = other.latFractionMax;
        lonFractionMin = other.lonFractionMin;
        lonFractionMax = other.lonFractionMax;
    }

    @Nonnull
    static MapcodeZone empty() {
        return new MapcodeZone();
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

    // Generate upper and lower limits based on x and y, and delta's.
    void setFromFractions(
            final double latFraction, final double lonFraction,
            final double latFractionDelta, final double lonFractionDelta) {
        assert (lonFractionDelta >= 0.0);
        assert (latFractionDelta != 0.0);
        lonFractionMin = lonFraction;
        lonFractionMax = lonFraction + lonFractionDelta;
        if (latFractionDelta < 0) {
            latFractionMin = latFraction + 1 + latFractionDelta;  // y + yDelta can NOT be represented.
            latFractionMax = latFraction + 1;                   // y CAN be represented.
        } else {
            latFractionMin = latFraction;
            latFractionMax = latFraction + latFractionDelta;
        }
    }

    boolean isEmpty() {
        return ((lonFractionMax <= lonFractionMin) || (latFractionMax <= latFractionMin));
    }

    @Nonnull
    Point getMidPoint() {
        if (isEmpty()) {
            return Point.undefined();
        } else {
            final double latFrac = Math.floor((latFractionMin + latFractionMax) / 2);
            final double lonFrac = Math.floor((lonFractionMin + lonFractionMax) / 2);
            return Point.fromLatLonFractions(latFrac, lonFrac);
        }
    }

    // Returns a non-empty intersection of a mapcode zone and a territory area.
    // Returns null if no such intersection exists.
    @Nonnull
    MapcodeZone restrictZoneTo(@Nonnull final Boundary area) {
        MapcodeZone z = new MapcodeZone(this);
        final double latMin = area.getLatMicroDegMin() * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR;
        if (z.latFractionMin < latMin) {
            z.latFractionMin = latMin;
        }
        final double latMax = area.getLatMicroDegMax() * Point.LAT_MICRODEG_TO_FRACTIONS_FACTOR;
        if (z.latFractionMax > latMax) {
            z.latFractionMax = latMax;
        }
        if (z.latFractionMin < z.latFractionMax) {
            double lonMin = area.getLonMicroDegMin() * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR;
            double lonMax = area.getLonMicroDegMax() * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR;
            if ((lonMax < 0) && (z.lonFractionMin > 0)) {
                lonMin += (360000000 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
                lonMax += (360000000 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
            } else if ((lonMin > 1) && (z.lonFractionMax < 0)) {
                lonMin -= (360000000 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
                lonMax -= (360000000 * Point.LON_MICRODEG_TO_FRACTIONS_FACTOR);
            }
            if (z.lonFractionMin < lonMin) {
                z.lonFractionMin = lonMin;
            }
            if (z.lonFractionMax > lonMax) {
                z.lonFractionMax = lonMax;
            }
        }
        return z;
    }

    @Nonnull
    @Override
    public String toString() {
        return isEmpty() ? "empty" :
                ("[" + (latFractionMin / Point.LAT_TO_FRACTIONS_FACTOR) + ", " + (latFractionMax / Point.LAT_TO_FRACTIONS_FACTOR) +
                        "), [" + (lonFractionMin / Point.LON_TO_FRACTIONS_FACTOR) + ", " + (lonFractionMax / Point.LON_TO_FRACTIONS_FACTOR) + ')');
    }
}
