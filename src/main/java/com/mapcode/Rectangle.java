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
import java.util.Arrays;

/**
 * This class defines a geospatial rectangle, defined by a South/West and a North/East
 * point. The rectangle is undefined if either of the corner points are undefined.
 */
public class Rectangle {

    private final Point southWest;
    private final Point northEast;

    Rectangle(@Nonnull final Point southWest, @Nonnull final Point northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }

    @Nonnull
    public Point getSouthWest() {
        return southWest;
    }

    @Nonnull
    public Point getNorthEast() {
        return northEast;
    }

   @Nonnull
    public Point getCenter() {
        if (!isDefined()) {
            return Point.undefined();
        }
        final double centerLat = ((southWest.getLatDeg() + northEast.getLatDeg()) / 2.0);
        final double centerLon = ((southWest.getLonDeg() + northEast.getLonDeg()) / 2.0);
        return Point.fromDeg(centerLat, centerLon);
    }

    boolean isDefined() {
        return southWest.isDefined() && northEast.isDefined();
    }

    @Nonnull
    @Override
    public String toString() {
        return "[" + southWest + ", " + northEast + ']';
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{southWest, northEast});
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rectangle)) {
            return false;
        }
        final Rectangle that = (Rectangle) obj;
        return (this.southWest.equals(that.southWest) &&
                this.northEast.equals(that.northEast));
    }
}
