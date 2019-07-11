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

/**
 * This runtime exception is thrown is decoding fails for an unknown reason.
 * The exception is not exposed externally.
 */
final class UnknownDecodeException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    UnknownDecodeException(@Nonnull final String message) {
        super(message);
        assert message != null;
    }
}
