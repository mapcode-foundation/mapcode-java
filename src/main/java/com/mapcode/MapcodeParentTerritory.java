/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public enum MapcodeParentTerritory {
    IND(MapcodeTerritory.IND),
    AUS(MapcodeTerritory.AUS),
    BRA(MapcodeTerritory.BRA),
    USA(MapcodeTerritory.USA),
    MEX(MapcodeTerritory.MEX),
    CAN(MapcodeTerritory.CAN),
    RUS(MapcodeTerritory.RUS),
    CHN(MapcodeTerritory.CHN),
    ATA(MapcodeTerritory.ATA);
    
    private MapcodeTerritory mapcodeTerritory;
    
    private MapcodeParentTerritory(MapcodeTerritory mapcodeTerritory)
    {
        this.mapcodeTerritory = mapcodeTerritory;
    }
    
    public MapcodeTerritory toMapCodeTerritory() {
        return mapcodeTerritory;
    }
}
