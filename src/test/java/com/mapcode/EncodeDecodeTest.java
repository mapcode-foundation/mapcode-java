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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class EncodeDecodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(EncodeDecodeTest.class);

    private static final int    NUMBER_OF_POINTS              = 1000;
    private static final int    LOG_LINE_EVERY                = 500;
    private static final double ALLOWED_DISTANCE_DELTA_METERS = 10.0;
    public static final  Gson   GSON                          =
        new GsonBuilder().serializeSpecialFloatingPointValues().create();

    @Test
    public void encodeDecodeTestFixedSeed() throws Exception {
        LOG.info("encodeDecodeTestFixedSeed");
        doEncodeDecode(12345678);
    }

    @Test
    public void encodeDecodeTestRandomSeed() throws Exception {
        LOG.info("encodeDecodeTestRandomSeed");
        doEncodeDecode(System.currentTimeMillis());
    }

    private static void doEncodeDecode(final long seed) throws UnknownMapcodeException {
        final Random randomGenerator = new Random(seed);
        for (int i = 0; i < NUMBER_OF_POINTS; i++) {
            boolean showLogLine = ((i % LOG_LINE_EVERY) == 0);

            // Every point must have a Mapcode.
            boolean found = false;
            final Point encode = Point.fromUniformlyDistributedRandomPoints(randomGenerator);

            // Walk through the list in reverse order to get International first.
            for (final Territory territory : Territory.values()) {

                // Encode location.
                final double latDeg = encode.getLatDeg();
                final double lonDeg = encode.getLonDeg();

                final List<Mapcode> results = MapcodeCodec.encode(latDeg, lonDeg, territory);
                for (final Mapcode result : results) {
                    found = true;
                    if (showLogLine) {
                        LOG.info("encodeDecodeTest: #{}/{}, encode={}, {} {} --> results={}",
                            i, NUMBER_OF_POINTS, latDeg, lonDeg, territory, GSON.toJson(results));
                    }

                    // Decode location, up to '/'.
                    final String mapcode = result.getMapcode();

                    // Check if the territory matches.
                    assertEquals(territory, result.getTerritory());

                    final Point decodeLocation = MapcodeCodec.decode(mapcode, territory);
                    final double distanceMeters = Point.distanceInMeters(encode, decodeLocation);

                    if (showLogLine) {
                        LOG.info("encodeDecodeTest: #{}/{}, result={}, mapcode={}, territory={} --> " +
                                "lat={}, lon={}; delta={}", i, NUMBER_OF_POINTS,
                            result, mapcode, territory.getFullName(), decodeLocation.getLatDeg(),
                            decodeLocation.getLonDeg(), distanceMeters);
                        LOG.info("");
                    }

                    // Check if the distance is not too great.
                    assertTrue("distanceMeters=" + distanceMeters + " >= " + ALLOWED_DISTANCE_DELTA_METERS,
                        distanceMeters < ALLOWED_DISTANCE_DELTA_METERS);
                    showLogLine = false;
                }
            }
            assertTrue(found);
        }
    }
}
