/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.mapcode.MapcodeParentTerritory;
import com.mapcode.MapcodeTerritory;

public class MapcodeTerritoryTest {

    @Test
    public void emptyCode() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("");
        assertEquals("EmptyCode", null, mapcodeTerritory);
    }

    @Test
    public void disambiguateMN() {
        MapcodeTerritory mapcodeTerritory1 = MapcodeTerritory.fromString("IND-MN");
        MapcodeTerritory mapcodeTerritory2 = MapcodeTerritory.fromString("MN", MapcodeParentTerritory.IND);
        MapcodeTerritory mapcodeTerritory3 = MapcodeTerritory.fromString("MN", MapcodeParentTerritory.USA);
        assertEquals("DisambiguateMN - Same", mapcodeTerritory1, mapcodeTerritory2);
        assertNotEquals("DisambiguateMN - Different", mapcodeTerritory2, mapcodeTerritory3);
    }
}
