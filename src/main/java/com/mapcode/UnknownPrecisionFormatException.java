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
 * This runtime exception is thrown for unknown alphabet codes. It is specifically a runtime exception, because
 * it (more or less) resembles an IllegalArgumentException, which is also a RuntimeException.
 */
public final class UnknownPrecisionFormatException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private final Integer precision;

    public UnknownPrecisionFormatException(@Nonnull final String message) {
        super(message);
        assert message != null;
        this.precision = null;
    }

    public UnknownPrecisionFormatException(@Nonnull final String message, final int precision) {
        super(message);
        assert message != null;
        this.precision = precision;
    }

    public UnknownPrecisionFormatException(final int precision) {
        super();
        this.precision = precision;
    }
}