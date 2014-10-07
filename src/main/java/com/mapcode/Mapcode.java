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
import java.util.Arrays;

/**
 * This class defines a single mapcode encoding result, including the mapcode itself and the
 * territory definition.
 */
public final class Mapcode {
    @Nonnull private final String    mapcode;
    @Nonnull private final String    mapcodePrecision1;
    @Nonnull private final String    mapcodePrecision2;
    @Nonnull private final Territory territory;

    public Mapcode(
        @Nonnull final String mapcode,
        @Nonnull final Territory territory) {
        this.mapcodePrecision2 = mapcode;
        if (mapcode.contains("-")) {
            this.mapcode = mapcode.substring(0, mapcode.length() - 3);
            this.mapcodePrecision1 = mapcode.substring(0, mapcode.length() - 1);
        }
        else {
            this.mapcode = mapcode;
            this.mapcodePrecision1 = mapcode;
        }
        this.territory = territory;
    }

    /**
     * Get the Mapcode string (without territory information) with standard precision.
     * The returned mapcode does not include the '-' separator and additional digits.
     *
     * The returned precision is approximately 5 meters. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 10 x 10 meters (100 m2).
     *
     * @return Mapcode string.
     */
    @Nonnull
    public String getMapcode() {
        return mapcode;
    }

    /**
     * Alias for {@link #getMapcode}.
     *
     * @return Mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision0() {
        return mapcode;
    }

    /**
     * Get the medium-precision mapcode string (without territory information).
     * The returned mapcode includes the '-' separator and 1 additional digit, if available.
     * If a medium precision code is not available, the regular mapcode is returned.
     *
     * The returned precision is approximately 1 meter. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 2 x 2 meters (4 m2).
     *
     * @return Medium precision mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision1() {
        return mapcodePrecision1;
    }

    /**
     * Deprecated alias for {@link #getMapcodePrecision1}.
     */
    @Deprecated
    @Nonnull
    public String getMapcodeMediumPrecision() {
        return mapcodePrecision1;
    }

    /**
     * Get the high-precision mapcode string (without territory information).
     * The returned mapcode includes the '-' separator and 2 additional digit2, if available.
     * If a high precision code is not available, the regular mapcode is returned.
     *
     * The returned precision is approximately 16 centimeters. The precision is defined as the maximum distance to the
     * (latitude, longitude) pair that encoded to this mapcode, which means the mapcode defines an area of
     * approximately 32 x 32 centimeters (0.1 m2).
     *
     * @return High precision mapcode string.
     */
    @Nonnull
    public String getMapcodePrecision2() {
        return mapcodePrecision2;
    }

    /**
     * Deprecated alias for {@see #getMapcodePrecision2}.
     */
    @Deprecated
    @Nonnull
    public String getMapcodeHighPrecision() {
        return mapcodePrecision2;
    }

    /**
     * Get the territory information.
     *
     * @return Territory information.
     */
    @Nonnull
    public Territory getTerritory() {
        return territory;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{mapcode, territory});
    }

    /**
     * Return the local mapcode string, potentially ambiguous.
     *
     * Example:
     * 49.4V
     *
     * @return Local mapcode.
     */
    @Nonnull
    public String asLocal() {
        return mapcode;
    }

    /**
     * Return the full international mapcode, including the full name of the territory and the Mapcode itself.
     * The format of the code is:
     * full-territory-name mapcode
     *
     * Example:
     * Netherlands 49.4V           (regular code)
     * Netherlands 49.4V-K2        (high precision code)
     *
     * @return Full international mapcode.
     */
    @Nonnull
    public String asInternationalFullName() {
        return territory.getFullName() + ' ' + mapcode;
    }

    /**
     * Return the international mapcode as a shorter version using the ISO territory codes where possible.
     * International codes use a territory code "AAA".
     * The format of the code is:
     * short-territory-name mapcode
     *
     * Example:
     * NLD 49.4V                   (regular code)
     * NLD 49.4V-K2                (high-precision code)
     *
     * @return Short-hand international mapcode.
     */
    @Nonnull
    public String asInternationalISO() {
        return territory.toString() + ' ' + mapcode;
    }

    @Nonnull
    @Override
    public String toString() {
        return asInternationalISO();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mapcode)) {
            return false;
        }
        final Mapcode that = (Mapcode) obj;
        return mapcode.equals(that.mapcode) && (this.territory.equals(that.territory));
    }
}
