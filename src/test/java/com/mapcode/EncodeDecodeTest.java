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
    private static final Gson GSON =
            new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private static final int NUMBER_OF_POINTS = 5000;
    private static final int LOG_LINE_EVERY = 100;

    @Test
    public void encodeDecodeTestFixedSeed() throws Exception {
        LOG.info("encodeDecodeTestFixedSeed");
        doEncodeDecode(1431977987367L);
    }

    @Test
    public void encodeDecodeTestRandomSeed() throws Exception {
        LOG.info("encodeDecodeTestRandomSeed");
        final long seed = System.currentTimeMillis();
        LOG.info("encodeDecodeTestRandomSeed: seed={}", seed);
        doEncodeDecode(seed);
    }

    private static void doEncodeDecode(final long seed) throws UnknownMapcodeException {
        final Random randomGenerator = new Random(seed);
        double maxDistancePrecision0Meters = 0.0;
        double maxDistancePrecision1Meters = 0.0;
        double maxDistancePrecision2Meters = 0.0;
        for (int i = 0; i < NUMBER_OF_POINTS; i++) {
            boolean showLogLine = ((i % LOG_LINE_EVERY) == 0);

            // Encode location.
            final Point encode = Point.fromUniformlyDistributedRandomPoints(randomGenerator);
            final double latDeg = encode.getLatDeg();
            final double lonDeg = encode.getLonDeg();

            // Check local and international codes.
            final Mapcode resultInternational = MapcodeCodec.encodeToInternational(latDeg, lonDeg);

            // Check encodeToShortest and encodeToInternational.
            final List<Mapcode> resultsAll = MapcodeCodec.encode(latDeg, lonDeg);
            assertTrue(!resultsAll.isEmpty());
            assertEquals("encodeToInternational failed, result=" + resultsAll,
                    resultsAll.get(resultsAll.size() - 1), resultInternational);

            // Every point must have a Mapcode.
            boolean found = false;

            // Walk through the list in reverse order to get International first.
            for (final Territory territory : Territory.values()) {
                final List<Mapcode> resultsLimited = MapcodeCodec.encode(latDeg, lonDeg, territory);
                for (final Mapcode result : resultsLimited) {
                    found = true;
                    if (showLogLine) {
                        LOG.info("encodeDecodeTest: #{}/{}, encode={}, {} {} --> results={}",
                                i, NUMBER_OF_POINTS, latDeg, lonDeg, territory, GSON.toJson(resultsLimited));
                    }

                    // Check if the territory matches.
                    assertEquals(territory, result.getTerritory());

                    // Check max distance.
                    final String mapcodePrecision0 = result.getMapcodePrecision(0);
                    final String mapcodePrecision1 = result.getMapcodePrecision(1);
                    final String mapcodePrecision2 = result.getMapcodePrecision(2);

                    final Point decodeLocationPrecision0 = MapcodeCodec.decode(mapcodePrecision0, territory);
                    final Point decodeLocationPrecision1 = MapcodeCodec.decode(mapcodePrecision1, territory);
                    final Point decodeLocationPrecision2 = MapcodeCodec.decode(mapcodePrecision2, territory);

                    final double distancePrecision0Meters = Point.distanceInMeters(encode, decodeLocationPrecision0);
                    final double distancePrecision1Meters = Point.distanceInMeters(encode, decodeLocationPrecision1);
                    final double distancePrecision2Meters = Point.distanceInMeters(encode, decodeLocationPrecision2);

                    maxDistancePrecision0Meters = Math.max(maxDistancePrecision0Meters, distancePrecision0Meters);
                    maxDistancePrecision1Meters = Math.max(maxDistancePrecision1Meters, distancePrecision1Meters);
                    maxDistancePrecision2Meters = Math.max(maxDistancePrecision2Meters, distancePrecision2Meters);

                    assertTrue("distancePrecision0Meters=" + distancePrecision0Meters + " >= " + Mapcode.PRECISION_0_MAX_DELTA_METERS,
                            distancePrecision0Meters < Mapcode.PRECISION_0_MAX_DELTA_METERS);
                    assertTrue("distancePrecision1Meters=" + distancePrecision1Meters + " >= " + Mapcode.PRECISION_1_MAX_DELTA_METERS,
                            distancePrecision1Meters < Mapcode.PRECISION_1_MAX_DELTA_METERS);
                    assertTrue("distancePrecision2Meters=" + distancePrecision2Meters + " >= " + Mapcode.PRECISION_2_MAX_DELTA_METERS,
                            distancePrecision2Meters < Mapcode.PRECISION_2_MAX_DELTA_METERS);

                    // Check conversion from/to alphabets.
                    for (final Alphabet alphabet : Alphabet.values()) {
                        final String converted = Mapcode.convertToAlphabet(mapcodePrecision2, alphabet);
                        final String reverted = Mapcode.convertToAscii(converted);
                        assertEquals("alphabet=" + alphabet + ", original=" + mapcodePrecision2 +
                                ", converted=" + converted + ", reverted=" + reverted, mapcodePrecision2, reverted);
                    }

                    if (showLogLine) {
                        LOG.info("encodeDecodeTest: #{}/{}, result={}, mapcode={}, territory={} --> " +
                                        "lat={}, lon={}; delta={}", i, NUMBER_OF_POINTS,
                                result, mapcodePrecision0, territory.getFullName(), decodeLocationPrecision0.getLatDeg(),
                                decodeLocationPrecision0.getLonDeg(), distancePrecision0Meters);
                        LOG.info("");
                    }
                    showLogLine = false;
                }
            }
            assertTrue(found);
        }
        LOG.info("encodeDecodeTest: maximum distances, precision 0, 1, 2: {}, {}, {} meters, ",
                maxDistancePrecision0Meters, maxDistancePrecision1Meters, maxDistancePrecision2Meters);
    }
}
