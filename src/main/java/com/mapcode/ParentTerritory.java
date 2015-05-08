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
 * Mapcode public interface.
 * ----------------------------------------------------------------------------------------------
 * <p/>
 * This class defines "parent territories" for territories that have multiple territory codes.
 */
public enum ParentTerritory {
    IND(Territory.IND),
    AUS(Territory.AUS),
    BRA(Territory.BRA),
    USA(Territory.USA),
    MEX(Territory.MEX),
    CAN(Territory.CAN),
    RUS(Territory.RUS),
    CHN(Territory.CHN),
    ATA(Territory.ATA);

    @Nonnull
    public Territory getTerritory() {
        return territory;
    }

    @Nonnull
    private final Territory territory;

    private ParentTerritory(@Nonnull final Territory territory) {
        this.territory = territory;
    }
}
