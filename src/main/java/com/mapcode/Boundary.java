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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class handles the territory ractangles for mapcodes. 
 */
class Boundaries {
    private int minx, maxx, miny, maxy;

    /**
     * Public interface
     * Note that after construction, all calls are safe.
     */

    public static Boundaries getBoundaries(final int m) {
        Boundaries b = new Boundaries();
        b.minx = DataAccess.minx(m);
        b.miny = DataAccess.miny(m);
        b.maxx = DataAccess.maxx(m);
        b.maxy = DataAccess.maxy(m);
        return b;
    }

    public int getMinX() {
        return minx;
    }

    public int getMinY() {
        return miny;
    }

    public int getMaxX() {
        return maxx;
    }

    public int getMaxY() {
        return maxy;
    }

    public Boundaries extendBounds(final int xExtension, final int yExtension) {
        minx -= xExtension;
        miny -= yExtension;
        maxx += xExtension;
        maxy += yExtension;
        return this;
    }

    public boolean containsPoint(@Nonnull final Point p) {
        final int y = p.getLatMicroDeg();
        if ((miny > y) || (y >= maxy)) { return false; }
        final int x = p.getLonMicroDeg();
        // longitude boundaries can extend (slightly) outside the [-180,180) range
        if (x < minx) { return (minx <= x + 360000000) && (x + 360000000 < maxx); } 
        if (x >= maxx) { return (minx <= x - 360000000) && (x - 360000000 < maxx); }
        return true;
    }

    public String toString() {
        return "[" + (miny / 1000000.0) + ", " + (maxy / 1000000.0) + 
            "), [" + (minx / 1000000.0) + ", " + (maxx / 1000000.0) + ")";
    }
}
