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

// simple class to represent all the coordinates that would deliver a particular mapcode
class MapcodeZone {

    // Longitudes in LonFractions ("1/3240 billionths")
    private double fractionMinX;
    private double fractionMaxX;

    // Latitudes in LatFractions ("1/810 billionths").
    private double fractionMinY;
    private double fractionMaxY;

    // Construct an (empty) zone.
    public MapcodeZone() {
        setEmpty();
    }

    // Construct an (empty) zone.
    public void setEmpty() {
        fractionMinY = 0;
        fractionMaxY = 0;
        fractionMinX = 0;
        fractionMaxX = 0;
    }

    // Construct a copy of an existing zone.
    public MapcodeZone(@Nonnull final MapcodeZone zone) {
        copyFrom(zone);
    }

    // construct a copy of an existing zone
    public void copyFrom(@Nonnull final MapcodeZone other) {
        fractionMinY = other.fractionMinY;
        fractionMaxY = other.fractionMaxY;
        fractionMinX = other.fractionMinX;
        fractionMaxX = other.fractionMaxX;
    }

    @Nonnull
    static MapcodeZone empty() {
        return new MapcodeZone();
    }

    public double getFractionMinX() {
        return fractionMinX;
    }

    public double getFractionMaxX() {
        return fractionMaxX;
    }

    public double getFractionMinY() {
        return fractionMinY;
    }

    public double getFractionMaxY() {
        return fractionMaxY;
    }

    public void setFractionMinX(final double fractionMinX) {
        this.fractionMinX = fractionMinX;
    }

    public void setFractionMaxX(final double fractionMaxX) {
        this.fractionMaxX = fractionMaxX;
    }

    public void setFractionMinY(final double fractionMinY) {
        this.fractionMinY = fractionMinY;
    }

    public void setFractionMaxY(final double fractionMaxY) {
        this.fractionMaxY = fractionMaxY;
    }

    // Generate upper and lower limits based on x and y, and delta's.
    public void setFromFractions(
            final double fractionY, final double fractionX,
            final double fractionYDelta, final double fractionXDelta) {
        assert (fractionXDelta >= 0.0);
        assert (fractionYDelta != 0.0);
        fractionMinX = fractionX;
        fractionMaxX = fractionX + fractionXDelta;
        if (fractionYDelta < 0) {
            fractionMinY = fractionY + 1 + fractionYDelta;  // y + yDelta can NOT be represented.
            fractionMaxY = fractionY + 1;                   // y CAN be represented.
        } else {
            fractionMinY = fractionY;
            fractionMaxY = fractionY + fractionYDelta;
        }
    }

    public boolean isEmpty() {
        return ((fractionMaxX <= fractionMinX) || (fractionMaxY <= fractionMinY));
    }

    @Nonnull
    public Point getMidPoint() {
        if (isEmpty()) {
            return Point.undefined();
        } else {
            final double lat = Math.floor((fractionMinY + fractionMaxY) / 2);
            final double lon = Math.floor((fractionMinX + fractionMaxX) / 2);
            return Point.fromFractionDeg(lat, lon);
        }
    }

    // Returns a non-empty intersection of a mapcode zone and a territory area.
    // Returns null if no such intersection exists.
    @Nonnull
    public MapcodeZone restrictZoneTo(@Nonnull final Boundary area) {
        MapcodeZone z = new MapcodeZone(this);
        final double miny = area.getMinY() * Point.MICROLAT_TO_FRACTIONS_FACTOR;
        if (z.fractionMinY < miny) {
            z.fractionMinY = miny;
        }
        final double maxy = area.getMaxY() * Point.MICROLAT_TO_FRACTIONS_FACTOR;
        if (z.fractionMaxY > maxy) {
            z.fractionMaxY = maxy;
        }
        if (z.fractionMinY < z.fractionMaxY) {
            double minx = area.getMinX() * Point.MICROLON_TO_FRACTIONS_FACTOR;
            double maxx = area.getMaxX() * Point.MICROLON_TO_FRACTIONS_FACTOR;
            if ((maxx < 0) && (z.fractionMinX > 0)) {
                minx += (360000000 * Point.MICROLON_TO_FRACTIONS_FACTOR);
                maxx += (360000000 * Point.MICROLON_TO_FRACTIONS_FACTOR);
            } else if ((minx > 1) && (z.fractionMaxX < 0)) {
                minx -= (360000000 * Point.MICROLON_TO_FRACTIONS_FACTOR);
                maxx -= (360000000 * Point.MICROLON_TO_FRACTIONS_FACTOR);
            }
            if (z.fractionMinX < minx) {
                z.fractionMinX = minx;
            }
            if (z.fractionMaxX > maxx) {
                z.fractionMaxX = maxx;
            }
        }
        return z;
    }

    @Nonnull
    @Override
    public String toString() {
        return isEmpty() ? "empty" :
                ("[" + (fractionMinY / Point.LAT_TO_FRACTIONS_FACTOR) + ", " + (fractionMaxY / Point.LAT_TO_FRACTIONS_FACTOR) +
                        "), [" + (fractionMinX / Point.LON_TO_FRACTIONS_FACTOR) + ", " + (fractionMaxX / Point.LON_TO_FRACTIONS_FACTOR) + ')');
    }
}
