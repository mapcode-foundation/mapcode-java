/*
 * Copyright (C) 2014-2019, Stichting Mapcode Foundation (http://www.mapcode.com)
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

import static com.mapcode.Mapcode.getPrecisionFormat;

// ----------------------------------------------------------------------------------------------
// Package private implementation class. For internal use within the mapcode implementation only.
// ----------------------------------------------------------------------------------------------

/**
 * This class provides a number of helper methods to check (runtime) arguments.
 */
final class CheckArgs {

    private CheckArgs() {
        // Prevent instantiation.
    }

    static void checkNonnull(@Nonnull final String param, @Nullable final Object obj)
            throws IllegalArgumentException {
        if (obj == null) {
            throw new IllegalArgumentException("Parameter " + param + " should not be null");
        }
    }

    static void checkDefined(@Nonnull final String param, @Nonnull final Point point)
            throws IllegalArgumentException {
        checkNonnull(param, point);
        if (!point.isDefined()) {
            throw new IllegalArgumentException("Parameter " + param + " must be defined");
        }
    }

    static void checkMapcodeCode(@Nonnull final String param, @Nullable final String code)
            throws IllegalArgumentException {
        checkNonnull(param, code);

        // Throws an exception if the format is incorrect.
        getPrecisionFormat(code);
    }
}
