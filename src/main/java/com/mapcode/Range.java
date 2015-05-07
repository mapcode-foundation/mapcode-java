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
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * ----------------------------------------------------------------------------------------------
 * Package private implementation class. For internal use within the Mapcode implementation only.
 * ----------------------------------------------------------------------------------------------
 *
 * This class contains a class for dealing with ranges of comparable items.
 */
class Range<T extends Comparable<T>> {
    @Nonnull private final T min;
    @Nonnull private final T max;

    Range(@Nonnull final T min, @Nonnull final T max) {
        this.min = min;
        this.max = max;
    }

    Range(@Nonnull final Range<T> range) {
        this.min = range.min;
        this.max = range.max;
    }

    @Nonnull
    T getMin() {
        return min;
    }

    @Nonnull
    T getMax() {
        return max;
    }

    boolean contains(@Nonnull final T value) {
        return (value.compareTo(min) >= 0) && (value.compareTo(max) <= 0);
    }

    boolean containsRange(@Nonnull final Range<T> range) {
        return (this.min.compareTo(range.getMin()) <= 0) && (this.max.compareTo(range.getMax()) >= 0);
    }

    boolean intersects(@Nonnull final Range<T> range) {
        return range.contains(min) || range.contains(max) || contains(range.max) || contains(range.min);
    }

    @Nullable
    Range<T> constrain(@Nonnull final Range<T> constrainingRange) {
        @Nonnull T newMin = this.min;
        @Nonnull T newMax = this.max;
        if (newMin.compareTo(constrainingRange.getMin()) < 0) {
            newMin = constrainingRange.getMin();
        }
        if (newMax.compareTo(constrainingRange.getMax()) > 0) {
            newMax = constrainingRange.getMax();
        }

        if (newMax.compareTo(newMin) <= 0) {
            return null;
        }

        return new Range<>(newMin, newMax);
    }

    @Nullable
    ArrayList<Range<T>> constrain(@Nonnull final ArrayList<Range<T>> constrainingRanges) {
        final ArrayList<Range<T>> resultRanges = new ArrayList<>();
        for (final Range<T> range : constrainingRanges) {
            final Range<T> constrainedRange = constrain(range);
            if (constrainedRange != null) {
                resultRanges.add(constrainedRange);
            }
        }
        if (!resultRanges.isEmpty()) {
            return resultRanges;
        }
        return null;
    }
}
