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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class EncodeDecodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(EncodeDecodeTest.class);
    private static final Gson GSON =
            new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private static final int NUMBER_OF_POINTS = 5000;
    private static final int LOG_LINE_EVERY = 500;


    @Test
    public void encodeDecodeTestFixedSeed() throws Exception {
        final long seed = 1431977987367L;
        LOG.info("encodeDecodeTestFixedSeed: seed={}", seed);
        doEncodeDecode(seed);
    }

    @Test
    public void encodeDecodeTestRandomSeed() throws Exception {
        final long seed = System.currentTimeMillis();
        LOG.info("encodeDecodeTestRandomSeed: seed={}", seed);
        doEncodeDecode(seed);
    }

    private static void doEncodeDecode(final long seed) throws InterruptedException {

        // Keep error count and create thread pool.
        final AtomicInteger errors = new AtomicInteger(0);
        final int threads = Runtime.getRuntime().availableProcessors();
        LOG.info("encodeDecodeTest: Starting {} threads...", threads);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        final Random randomGenerator = new Random(seed);
        for (int i = 0; i < NUMBER_OF_POINTS; i++) {
            if ((i % LOG_LINE_EVERY) == 0) {
                LOG.info("encodeDecodeTest: #{}/{}", i, NUMBER_OF_POINTS);
            }

            // Encode location.
            final Point encode = Point.fromUniformlyDistributedRandomPoints(randomGenerator);
            final double latDeg = encode.getLatDeg();
            final double lonDeg = encode.getLonDeg();

            // Check local and international codes.
            final Mapcode mapcodeInternational = MapcodeCodec.encodeToInternational(latDeg, lonDeg);

            // Check encodeToShortest and encodeToInternational.
            final List<Mapcode> resultsAll = MapcodeCodec.encode(latDeg, lonDeg);
            assertTrue(!resultsAll.isEmpty());
            assertEquals("encodeToInternational failed, result=" + resultsAll,
                    resultsAll.get(resultsAll.size() - 1), mapcodeInternational);

            // Walk through the list in reverse order to get International first.
            for (final Territory territory : Territory.values()) {
                executor.execute(() -> {
                    try {
                        final List<Mapcode> resultsLimited = MapcodeCodec.encode(latDeg, lonDeg, territory);
                        for (final Mapcode mapcode : resultsLimited) {

                            // Check if the territory matches.
                            assertEquals(territory, mapcode.getTerritory());

                            // Check max distance.
                            final String codePrecision0 = mapcode.getCode(0);
                            final String codePrecision1 = mapcode.getCode(1);
                            final String codePrecision2 = mapcode.getCode(2);

                            final Point decodeLocationPrecision0 = MapcodeCodec.decode(codePrecision0, territory);
                            final Point decodeLocationPrecision1 = MapcodeCodec.decode(codePrecision1, territory);
                            final Point decodeLocationPrecision2 = MapcodeCodec.decode(codePrecision2, territory);

                            final double distancePrecision0Meters = Point.distanceInMeters(encode, decodeLocationPrecision0);
                            final double distancePrecision1Meters = Point.distanceInMeters(encode, decodeLocationPrecision1);
                            final double distancePrecision2Meters = Point.distanceInMeters(encode, decodeLocationPrecision2);

                            if (distancePrecision0Meters >= Mapcode.getSafeMaxOffsetInMeters(0)) {
                                LOG.error("encodeDecodeTest: " + mapcode + " distancePrecision0Meters = " + distancePrecision0Meters + " >= " + Mapcode.getSafeMaxOffsetInMeters(0));
                                errors.getAndIncrement();
                            }
                            if (distancePrecision1Meters >= Mapcode.getSafeMaxOffsetInMeters(1)) {
                                LOG.error("encodeDecodeTest: " + mapcode + " distancePrecision1Meters = " + distancePrecision1Meters + " >= " + Mapcode.getSafeMaxOffsetInMeters(1));
                                errors.getAndIncrement();
                            }
                            if (distancePrecision2Meters >= Mapcode.getSafeMaxOffsetInMeters(2)) {
                                LOG.error("encodeDecodeTest: " + mapcode + " distancePrecision2Meters = " + distancePrecision2Meters + " >= " + Mapcode.getSafeMaxOffsetInMeters(2));
                                errors.getAndIncrement();
                            }

                            // Check conversion from/to alphabets.
                            for (final Alphabet alphabet : Alphabet.values()) {
                                final String mapcodeAlphabet = mapcode.getCode(alphabet);
                                final String mapcodeAscii = Mapcode.convertStringToPlainAscii(mapcodeAlphabet);
                                if (!codePrecision0.equals(mapcodeAscii)) {
                                    LOG.error("encodeDecodeTest: " + mapcode + " alphabet=" + alphabet + ", original=" + codePrecision0 +
                                            ", mapcodeAlphabet=" + mapcodeAlphabet + ", mapcodeAscii=" + mapcodeAscii);
                                }
                            }
                        }
                    } catch (final Exception e) {
                        LOG.error("encodeDecodeTest: Unexpected exception: ", e);
                        errors.getAndIncrement();
                    }
                });
            }
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        assertEquals("Found errors", 0, errors.get());
    }
}
