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
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;

    private Boundary() {
        // Disabled.
    }

    // You have to use this factory method instead of a ctor.
    @Nonnull
    static Boundary createFromTerritoryRecord(final int territoryRecord) {
        Boundary boundary = new Boundary();
        boundary.minX = DataAccess.getMinX(territoryRecord);
        boundary.minY = DataAccess.getMinY(territoryRecord);
        boundary.maxX = DataAccess.getMaxX(territoryRecord);
        boundary.maxY = DataAccess.getMaxY(territoryRecord);
        return boundary;
    }

    int getMinX() {
        return minX;
    }

    int getMinY() {
        return minY;
    }

    int getMaxX() {
        return maxX;
    }

    int getMaxY() {
        return maxY;
    }

    @Nonnull
    Boundary extendBoundary(final int xExtension, final int yExtension) {
        minX -= xExtension;
        minY -= yExtension;
        maxX += xExtension;
        maxY += yExtension;
        return this;
    }

    boolean containsPoint(@Nonnull final Point p) {
        if (!p.isDefined()) {
            return false;
        }
        final int y = p.getLatMicroDeg();
        if ((minY > y) || (y >= maxY)) {
            return false;
        }
        final int x = p.getLonMicroDeg();
        // longitude boundaries can extend (slightly) outside the [-180,180) range
        if (x < minX) {
            return (minX <= (x + 360000000)) && ((x + 360000000) < maxX);
        }
        if (x >= maxX) {
            return (minX <= (x - 360000000)) && ((x - 360000000) < maxX);
        }
        return true;
    }

    @Nonnull
    public String toString() {
        return "[" + (minY / 1000000.0) + ", " + (maxY / 1000000.0) +
                "), [" + (minX / 1000000.0) + ", " + (maxX / 1000000.0) + ')';
    }
}
