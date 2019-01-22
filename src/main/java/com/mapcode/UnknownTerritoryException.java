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

/**
 * This runtime exception is thrown for unknown territory codes. It is specifically a runtime exception, because
 * it (more or less) resembles an IllegalArgumentException, which is also a RuntimeException.
 */
public final class UnknownTerritoryException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public UnknownTerritoryException(@Nonnull final String message) {
        super(message);
        assert message != null;
        this.code = null;
    }

    public UnknownTerritoryException(final int code) {
        super();
        this.code = code;
    }

    @Nullable
    public Integer getCode() {
        return code;
    }
}
