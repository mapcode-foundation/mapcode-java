/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.ArrayList;

import org.junit.Test;

import com.mapcode.MapcodeDecoder;
import com.mapcode.MapcodeEncoder;
import com.mapcode.MapcodeTerritory;
import com.mapcode.Point;

public class MapcodeEncodeDecodeTest {
    
    private static final double EARTH_RADIUS = 6371005.076123;
    private static final int DEGREES = 180;
    private static final double DEG_TO_RAD = Math.PI / DEGREES;
    
    @Test
    public void EncodeDecode() {
        Random generator = new Random(0);
        for (int i = 0; i < 10000; i++) {
            double y = generator.nextDouble() * 90;
            if (generator.nextBoolean()) {
                y *=-1;
            }
            double x = generator.nextDouble() * 180;
            if (generator.nextBoolean()) {
                x *=-1;
            }
            Point encodeLocation = new Point(x, y);
            for (MapcodeTerritory territory : MapcodeTerritory.values()) {
                ArrayList<String> results = MapcodeEncoder.master_encode(y, x, territory, false, false, false);
                for (String result : results) {
                    result = result.substring(0,result.indexOf('/'));
                    Point decodeLocation = MapcodeDecoder.master_decode(result, territory);
                    
                    double delta = geoDistanceMeters(encodeLocation, decodeLocation);
                    String summaryString = "lat:" + encodeLocation.getLatitude() + " long:" + encodeLocation.getLongitude() + " country:" + territory.getFullName() + " mapcode:" + result + " delta:" + delta;
                    System.out.println(summaryString);
                    assertEquals(summaryString + " (delta=" + Double.toString(delta) + ")", delta < 10, true);                    
                }
            }
        }
    }

    private static double geoDistanceMeters(Point from, Point to) {
        double deltaLongitude = (to.getLongitudeDegrees() - from.getLongitudeDegrees()) * DEG_TO_RAD;
        double deltaLatitude = (to.getLatitudeDegrees() - from.getLatitudeDegrees()) * DEG_TO_RAD;
        double x = deltaLongitude * Math.cos((from.getLatitudeDegrees() + to.getLatitudeDegrees()) / 2 * DEG_TO_RAD);
        double y = deltaLatitude;
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    }

}
