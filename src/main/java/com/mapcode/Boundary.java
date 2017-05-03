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

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class handles territory rectangles for mapcodes.
 */
class Boundary {
    private int lonMicroDegMin;     // Minimum longitude (in microdegrees). Inclusive.
    private int lonMicroDegMax;     // Maximum longitude (in microdegrees). Exclusive.
    private int latMicroDegMin;     // Minimum latitude (in microdegrees). Inclusive.
    private int latMicroDegMax;     // Minimum latitude (in microdegrees). Exclusive.

    private static final DataModel DATA_MODEL = DataModel.getInstance();

    private Boundary(
            final int lonMicroDegMin,
            final int lonMicroDegMax,
            final int latMicroDegMin,
            final int latMicroDegMax) {
        this.lonMicroDegMin = lonMicroDegMin;
        this.latMicroDegMin = latMicroDegMin;
        this.lonMicroDegMax = lonMicroDegMax;
        this.latMicroDegMax = latMicroDegMax;
    }

    // You have to use this factory method instead of a ctor.
    @Nonnull
    static Boundary createFromTerritoryRecord(final int territoryRecord) {
        return new Boundary(
                DATA_MODEL.getLonMicroDegMin(territoryRecord),
                DATA_MODEL.getLonMicroDegMax(territoryRecord),
                DATA_MODEL.getLatMicroDegMin(territoryRecord),
                DATA_MODEL.getLatMicroDegMax(territoryRecord));
    }

    int getLonMicroDegMin() {
        return lonMicroDegMin;
    }

    int getLonMicroDegMax() {
        return lonMicroDegMax;
    }

    int getLatMicroDegMin() {
        return latMicroDegMin;
    }

    int getLatMicroDegMax() {
        return latMicroDegMax;
    }

    @Nonnull
    Boundary extendBoundary(final int latMicroDegExtension, final int lonMicroDegExtension) {
        lonMicroDegMin -= lonMicroDegExtension;
        latMicroDegMin -= latMicroDegExtension;
        lonMicroDegMax += lonMicroDegExtension;
        latMicroDegMax += latMicroDegExtension;
        return this;
    }

    /**
     * Check if a point falls within a boundary. Note that the "min" values are inclusive for a boundary and
     * the "max" values are exclusive.\
     *
     * Note: Points at the exact North pole with latitude 90 are never part of a boundary.
     *
     * @param p Point to check.
     * @return True if the points falls within the boudary.
     */
    boolean containsPoint(@Nonnull final Point p) {
        if (!p.isDefined()) {
            return false;
        }
        final int latMicroDeg = p.getLatMicroDeg();
        if ((latMicroDegMin > latMicroDeg) || (latMicroDeg >= latMicroDegMax)) {
            return false;
        }
        final int lonMicroDeg = p.getLonMicroDeg();

        // Longitude boundaries can extend (slightly) outside the [-180,180) range
        if (lonMicroDeg < lonMicroDegMin) {
            return (lonMicroDegMin <= (lonMicroDeg + Point.MICRO_DEG_360)) && ((lonMicroDeg + Point.MICRO_DEG_360) < lonMicroDegMax);
        } else if (lonMicroDeg >= lonMicroDegMax) {
            return (lonMicroDegMin <= (lonMicroDeg - Point.MICRO_DEG_360)) && ((lonMicroDeg - Point.MICRO_DEG_360) < lonMicroDegMax);
        } else {
            return true;
        }
    }

    @Nonnull
    public String toString() {
        return "[" + (latMicroDegMin / Point.DEG_TO_MICRO_DEG) + ", " + (latMicroDegMax / Point.DEG_TO_MICRO_DEG) +
                "), [" + (lonMicroDegMin / Point.DEG_TO_MICRO_DEG) + ", " + (lonMicroDegMax / Point.DEG_TO_MICRO_DEG) + ')';
    }
}
