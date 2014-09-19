/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

public class Point {
    private final static int UNDEFINED = 361361361;

    private int longitude;

    public int getLongitude() {
        return longitude;
    }
    
    public double getLongitudeDegrees() {
        return decamicrodegreesToDegrees(longitude);
    }

    private int latitude;

    public int getLatitude() {
        return latitude;
    }
    
    public double getLatitudeDegrees() {
        return decamicrodegreesToDegrees(latitude);
    }

    public Point(int longitudeDecamicroDegrees, int latitudeDecamicroDegrees) {
        this.longitude = longitudeDecamicroDegrees;
        this.latitude = latitudeDecamicroDegrees;
    }
    
    public Point(double longitudeDegrees, double latitudeDegrees) {
        this.longitude = degreesToDecamicroDegrees(longitudeDegrees);
        this.latitude = degreesToDecamicroDegrees(latitudeDegrees);
    }

    public Point() {
        longitude = UNDEFINED;
    }

    public void clear() {
        longitude = UNDEFINED;
    }

    public boolean isDefined() {
        return longitude != UNDEFINED;
    }
    
    private int degreesToDecamicroDegrees(double degrees) {
        return (int) Math.round(degrees * 1000000);
    }
    
    private float decamicrodegreesToDegrees(int decamicrodegrees) {
        return (float) decamicrodegrees / 1000000;
    }
}
