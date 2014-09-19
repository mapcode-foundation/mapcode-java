/*******************************************************************************
 * Copyright (c) 2014 Stichting Mapcode Foundation
 * For terms of use refer to http://www.mapcode.com/downloads.html
 ******************************************************************************/
package com.mapcode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mapcode.Point;

public class PointTest {
    @Test
    public void invalidPoint() {
        Point point = new Point();
        assertEquals("Invalid point", false, point.isDefined());
    }

    @Test
    public void validPoint() {
        Point point = new Point(1, 2);
        assertEquals("Valid point", true, point.isDefined());
    }

    @Test
    public void invalidatedPoint() {
        Point point = new Point(1, 2);
        point.clear();
        assertEquals("Invalidated point", false, point.isDefined());
    }

    @Test
    public void pointStored() {
        Point point = new Point(1, 2);
        assertEquals("X correct", 1, point.getLongitude());
        assertEquals("Y correct", 2, point.getLatitude());
    }
}
