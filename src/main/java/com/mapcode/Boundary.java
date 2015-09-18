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
 * Package private implementation class. For internal use within the mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 * <p/>
 * This class handles the territory rectangles for mapcodes.
 */
class Boundary {
    private int lonMicroDegMin;
    private int lonMicroDegMax;
    private int latMicroDegMin;
    private int latMicroDegMax;

    private Boundary() {
        // Disabled.
    }

    // You have to use this factory method instead of a ctor.
    @Nonnull
    static Boundary createFromTerritoryRecord(final int territoryRecord) {
        final Boundary boundary = new Boundary();
        boundary.lonMicroDegMin = DataAccess.getLonMicroDegMin(territoryRecord);
        boundary.latMicroDegMin = DataAccess.getLatMicroDegMin(territoryRecord);
        boundary.lonMicroDegMax = DataAccess.getLonMicroDegMax(territoryRecord);
        boundary.latMicroDegMax = DataAccess.getLatMicroDegMax(territoryRecord);
        return boundary;
    }

    int getLonMicroDegMin() {
        return lonMicroDegMin;
    }

    int getLatMicroDegMin() {
        return latMicroDegMin;
    }

    int getLonMicroDegMax() {
        return lonMicroDegMax;
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

    boolean containsPoint(@Nonnull final Point p) {
        if (!p.isDefined()) {
            return false;
        }
        final int latMicroDeg = p.getLatMicroDeg();
        if ((latMicroDegMin > latMicroDeg) || (latMicroDeg >= latMicroDegMax)) {
            return false;
        }
        final int lonMicroDeg = p.getLonMicroDeg();
        // longitude boundaries can extend (slightly) outside the [-180,180) range
        if (lonMicroDeg < lonMicroDegMin) {
            return (lonMicroDegMin <= (lonMicroDeg + 360000000)) && ((lonMicroDeg + 360000000) < lonMicroDegMax);
        }
        if (lonMicroDeg >= lonMicroDegMax) {
            return (lonMicroDegMin <= (lonMicroDeg - 360000000)) && ((lonMicroDeg - 360000000) < lonMicroDegMax);
        }
        return true;
    }

    @Nonnull
    public String toString() {
        return "[" + (latMicroDegMin / 1000000.0) + ", " + (latMicroDegMax / 1000000.0) +
                "), [" + (lonMicroDegMin / 1000000.0) + ", " + (lonMicroDegMax / 1000000.0) + ')';
    }
}
