/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mapcode.MapcodeDecoder;
import com.mapcode.MapcodeTerritory;
import com.mapcode.Point;

public class MapcodeDecoderTest {
    @Test
    public void decodeTomTomOffice() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("49.4V", mapcodeTerritory);
        assertEquals("decodeTomTomOffice latitude", point.getLatitude(), 52376514);
        assertEquals("decodeTomTomOffice longitude", point.getLongitude(), 4908542);
    }

    @Test
    public void invalidDotLocation1() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("4.94V", mapcodeTerritory);
        assertEquals("invalidDotLocation1", point.isDefined(), false);
    }

    @Test
    public void invalidDotLocation2() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("494.V", mapcodeTerritory);
        assertEquals("invalidDotLocation2", point.isDefined(), false);
    }
    
    @Test
    public void invalidDotLocation3() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("494V49.4V", mapcodeTerritory);
        assertEquals("invalidDotLocation3", point.isDefined(), false);
    }
    
    @Test
    public void invalidDotLocation4() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("494.V494V", mapcodeTerritory);
        assertEquals("invalidDotLocation3", point.isDefined(), false);
    }

    @Test
    public void invalidNoDot() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("494V", mapcodeTerritory);
        assertEquals("invalidNoDot", point.isDefined(), false);
    }
    
    @Test
    public void highPrecisionTomTomOffice() {
        MapcodeTerritory mapcodeTerritory = MapcodeTerritory.fromString("NLD");
        Point point = MapcodeDecoder.master_decode("49.4V-K2", mapcodeTerritory);
        assertEquals("decodeTomTomOffice hi-precision latitude", point.getLatitude(), 52376512);
        assertEquals("decodeTomTomOffice hi-precision longitude", point.getLongitude(), 4908540);
    }
}
