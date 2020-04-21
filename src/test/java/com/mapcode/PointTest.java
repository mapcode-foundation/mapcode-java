/*
 * Copyright (C) 2016-2020, Stichting Mapcode Foundation (http://www.mapcode.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapcode;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class PointTest {
    private static final Logger LOG = LoggerFactory.getLogger(PointTest.class);
    private static final double DELTA = 0.000001;

    @Test
    public void invalidPoint() {
        LOG.info("invalidPoint");
        final Point point = Point.undefined();
        assertFalse("Undefined point", point.isDefined());
    }

    @Test
    public void validPoint() {
        LOG.info("validPoint");
        final Point point = Point.fromMicroDeg(2, 1);
        assertTrue("Valid point", point.isDefined());

        assertEquals(0, Point.fromDeg(0, 0).getLatMicroDeg());
        assertEquals(0, Point.fromDeg(0, 0).getLonMicroDeg());

        assertEquals(Point.MICRO_DEG_90, Point.fromDeg(90, 0).getLatMicroDeg());
        assertEquals(-Point.MICRO_DEG_90, Point.fromDeg(-90, 0).getLatMicroDeg());

        assertEquals(-Point.MICRO_DEG_180, Point.fromDeg(0, 180).getLonMicroDeg());
        assertEquals(-Point.MICRO_DEG_180, Point.fromDeg(0, 180).getLonMicroDeg());
    }

    @Test
    public void invalidatedPoint() {
        LOG.info("invalidatedPoint");
        final Point point = Point.fromMicroDeg(2, 1);
        point.setUndefined();
        assertFalse("Cleared point", point.isDefined());
    }

    @Test
    public void pointStored() {
        LOG.info("pointStored");
        final Point point = Point.fromMicroDeg(2, 1);
        assertEquals("Lon correct", 1, point.getLonMicroDeg());
        assertEquals("Lat correct", 2, point.getLatMicroDeg());
    }

    @Test
    public void testDegreesLatToMeters() {
        LOG.info("testDegreesLatToMeters");

        assertEquals(0, Double.compare(0, Point.degreesLatToMeters(0)));
        assertEquals(0, Double.compare(Point.METERS_PER_DEGREE_LAT / 2.0, Point.degreesLatToMeters(0.5)));
        assertEquals(0, Double.compare(Point.METERS_PER_DEGREE_LAT, Point.degreesLatToMeters(1)));
        assertEquals(0, Double.compare(Point.METERS_PER_DEGREE_LAT * 90, Point.degreesLatToMeters(90)));
        assertEquals(0, Double.compare(-Point.METERS_PER_DEGREE_LAT * 90, Point.degreesLatToMeters(-90)));
    }

    @Test
    public void testDegreesLonToMeters() {
        LOG.info("testDegreesLonToMeters");

        assertEquals(0, Double.compare(0, Point.degreesLonToMetersAtLat(0, 0)));
        assertEquals(0,
                Double.compare(Point.METERS_PER_DEGREE_LON_EQUATOR / 2.0, Point.degreesLonToMetersAtLat(0.5, 0)));
        assertEquals(0,
                Double.compare(Point.METERS_PER_DEGREE_LON_EQUATOR, Point.degreesLonToMetersAtLat(1, 0)));
        assertEquals(0,
                Double.compare(Point.METERS_PER_DEGREE_LON_EQUATOR * 180, Point.degreesLonToMetersAtLat(180, 0)));
        assertEquals(0,
                Double.compare(-Point.METERS_PER_DEGREE_LON_EQUATOR * 180, Point.degreesLonToMetersAtLat(-180, 0)));
        assertTrue(Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR / 2.0) -
                Point.degreesLonToMetersAtLat(1, 60)) < DELTA);
        assertTrue(Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR / 2.0) -
                Point.degreesLonToMetersAtLat(1, -60)) < DELTA);
    }

    @Test
    public void testMetersToDegreesLon() {
        LOG.info("testMetersToDegreesLon()");

        assertEquals(0, Double.compare(0, Point.metersToDegreesLonAtLat(0, 0)));
        assertEquals(0,
                Double.compare(0.5, Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR / 2, 0)));
        assertEquals(0,
                Double.compare(1, Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR, 0)));
        assertEquals(0,
                Double.compare(180, Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR * 180, 0)));
        assertEquals(0, Double.compare(-180,
                Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR * -180, 0)));
        assertTrue(
                Math.abs(2.0 - Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR, 60)) < DELTA);
        assertTrue(
                Math.abs(2.0 - Point.metersToDegreesLonAtLat(Point.METERS_PER_DEGREE_LON_EQUATOR, -60)) < DELTA);
    }

    @Test
    public void testDistanceInMeters() {
        LOG.info("testDistanceInMeters");

        double d = Point.distanceInMeters(
                Point.fromMicroDeg(-500000, 0), Point.fromMicroDeg(500000, 0));
        double delta = Math.abs(Point.METERS_PER_DEGREE_LAT - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(80000000, 0), Point.fromMicroDeg(81000000, 0));
        delta = Math.abs(Point.METERS_PER_DEGREE_LAT - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(59000000, 0), Point.fromMicroDeg(61000000, 0));
        delta = Math.abs((Point.METERS_PER_DEGREE_LAT * 2) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 1000000), Point.fromMicroDeg(0, 0));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 2000000), Point.fromMicroDeg(0, 0));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR * 2) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 2000000), Point.fromMicroDeg(0, -1000000));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR * 3) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -1000000), Point.fromMicroDeg(0, 0));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -1000000), Point.fromMicroDeg(0, 1000000));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR * 2) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -1000000), Point.fromMicroDeg(0, 2000000));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR * 3) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -500000), Point.fromMicroDeg(0, 500000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 80000000), Point.fromMicroDeg(0, 81000000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(60000000, 80000000), Point.fromMicroDeg(60000000, 81000000));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR / 2.0) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -1000000), Point.fromMicroDeg(0, 1000000));
        delta = Math.abs((Point.METERS_PER_DEGREE_LON_EQUATOR * 2) - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 1000000), Point.fromMicroDeg(0, 2000000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 178000000), Point.fromMicroDeg(0, 179000000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 179000000), Point.fromMicroDeg(0, Point.MICRO_DEG_180));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, 179500000), Point.fromMicroDeg(0, -179500000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -179500000), Point.fromMicroDeg(0, 179500000));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        d = Point.distanceInMeters(
                Point.fromMicroDeg(0, -179000000), Point.fromMicroDeg(0, -Point.MICRO_DEG_180));
        delta = Math.abs(Point.METERS_PER_DEGREE_LON_EQUATOR - d);
        LOG.debug("testDistanceInMeters: d={}, delta={}", d, delta);
        assertTrue(delta <= DELTA);

        assertTrue(Point.distanceInMeters(Point.fromDeg(0.0, 180.0), Point.fromDeg(0.0, -179.999977)) < 10.0);
        assertTrue(Point.distanceInMeters(Point.fromDeg(0.0, -179.999977), Point.fromDeg(0.0, 180.0)) < 10.0);
    }

    @Test
    public void testWrap() {
        LOG.info("testWrap");
        for (int i = 0; i < 5; ++i) {
            assertEquals(Point.fromDeg(0.0, (i * 360) + 0.0), Point.fromDeg(0.0, 0.0).wrap());

            assertEquals(Point.fromDeg(-90.0, (i * 360) + 0.0), Point.fromDeg(-90.0, 0.0).wrap());
            assertEquals(Point.fromDeg(-89.99, (i * 360) + 0.0), Point.fromDeg(-89.99, 0.0).wrap());
            assertEquals(Point.fromDeg(-90.01, (i * 360) + 0.0), Point.fromDeg(-90.0, 0.0).wrap());

            assertEquals(Point.fromDeg(90.0, (i * 360) + 0.0), Point.fromDeg(90.0, 0.0).wrap());
            assertEquals(Point.fromDeg(89.99, (i * 360) + 0.0), Point.fromDeg(89.99, 0.0).wrap());
            assertEquals(Point.fromDeg(90.01, (i * 360) + 0.0), Point.fromDeg(90.0, 0.0).wrap());

            assertEquals(Point.fromDeg(0.0, (i * 360) - 180.0), Point.fromDeg(0.0, -180.0).wrap());
            assertEquals(Point.fromDeg(0.0, (i * 360) - 179.99), Point.fromDeg(0.0, -179.99).wrap());
            assertEquals(Point.fromDeg(0.0, (i * 360) - 180.01), Point.fromDeg(0.0, 179.99).wrap());

            assertEquals(Point.fromDeg(0.0, (i * 360) + 180.0), Point.fromDeg(0.0, -180.0).wrap());
            assertEquals(Point.fromDeg(0.0, (i * 360) + 179.99), Point.fromDeg(0.0, 179.99).wrap());
            assertEquals(Point.fromDeg(0.0, (i * 360) + 180.01), Point.fromDeg(0.0, -179.99).wrap());
        }

        // Has caused a failure once:
        assertPointOK(Point.fromDeg(1.0e307, 1.0e307));
        assertPointOK(Point.fromDeg(Double.MIN_VALUE, Double.MIN_VALUE));
        assertPointOK(Point.fromDeg(Double.MAX_VALUE, Double.MAX_VALUE));
        assertPointOK(Point.fromDeg(8.988465674311579e307, 6.964924833637635e307));
        assertPointOK(Point.fromDeg(-8.139756926185767E307, 8.15622502365209E307));
    }

    private static void assertPointOK(final Point p) {
        assertTrue("Lat = " + p.getLatDeg(), p.getLatDeg() >= -90);
        assertTrue("Lat = " + p.getLatDeg(), p.getLatDeg() <= 90);
        assertTrue("Lon = " + p.getLonDeg(), p.getLonDeg() >= -180);
        assertTrue("Lon = " + p.getLonDeg(), p.getLonDeg() < 180);
    }
}
