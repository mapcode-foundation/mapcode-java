/*
 * Copyright (C) 2014-2016 Stichting Mapcode Foundation (http://www.mapcode.com)
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

    @SuppressWarnings("BusyWait")
    private static void doEncodeDecode(final long seed) throws InterruptedException {

        // Keep error count and create thread pool.
        final AtomicInteger errors = new AtomicInteger(0);
        final AtomicInteger tasks = new AtomicInteger(0);

        final int cores = Runtime.getRuntime().availableProcessors();
        final int threads = Math.min(cores * 2, 16);
        LOG.info("encodeDecodeTest: Starting {} threads on {} (hyperthreaded) cores...", threads, cores);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        final Random randomGenerator = new Random(seed);
        for (int i = 0; i < NUMBER_OF_POINTS; i++) {

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

            executor.execute(new Runnable() {

                @Override
                public void run() {
                    final int count = tasks.incrementAndGet();
                    if ((count % LOG_LINE_EVERY) == 0) {
                        LOG.info("encodeDecodeTest: #{}/{}", count, NUMBER_OF_POINTS);
                    }

                    // Walk through the list in reverse order to get International first.
                    for (final Territory territory : Territory.values()) {
                        final List<Mapcode> resultsLimited = MapcodeCodec.encode(latDeg, lonDeg, territory);
                        for (final Mapcode mapcode : resultsLimited) {

                            // Check if the territory matches.
                            assertEquals(territory, mapcode.getTerritory());

                            // Check max distance at every nrDigits, and verify encode(decode(m))=m
                            for (int nrDigits = 0; nrDigits <= 8; nrDigits++) {
                                final String codePrecision = mapcode.getCode(nrDigits);
                                final Point decodeLocation;
                                try {
                                    decodeLocation = MapcodeCodec.decode(codePrecision, territory);
                                } catch (final UnknownMapcodeException e) {
                                    LOG.error("FAILED {} Decode({} {}) generated from ({}, {}) in {}", count, territory, codePrecision, latDeg, lonDeg);
                                    LOG.error("encodeDecodeTest: Unknown mapcode exception", e);
                                    errors.getAndIncrement();
                                    continue;
                                }

                                final double distance = Point.distanceInMeters(encode, decodeLocation);
                                if (distance >= Mapcode.getSafeMaxOffsetInMeters(nrDigits)) {
                                    LOG.error("encodeDecodeTest: " + mapcode + " digits = " + nrDigits + " distance = " + distance + " >= " + Mapcode.getSafeMaxOffsetInMeters(nrDigits));
                                    errors.getAndIncrement();
                                } else {
                                    // check that decode can be encoded back to original
                                    final List<Mapcode> recodedMapcodes = MapcodeCodec.encode(decodeLocation, territory);
                                    {
                                        boolean found = false;
                                        for (final Mapcode candidate : recodedMapcodes) {
                                            if (codePrecision.equals(candidate.getCode(nrDigits))) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            // perhaps it was inherited from the parent?
                                            final Territory parentTerritory = territory.getParentTerritory();
                                            if (parentTerritory != null) {
                                                final List<Mapcode> recodedMapcodesFromParent = MapcodeCodec.encode(decodeLocation, parentTerritory);
                                                for (final Mapcode candidate : recodedMapcodesFromParent) {
                                                    if (codePrecision.equals(candidate.getCode(nrDigits))) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!found) {
                                                if (!MapcodeCodec.isNearMultipleBorders(decodeLocation, territory)) { // but should be found!
                                                    LOG.error("Re-encode{} of {} failed for {} {} from ({},{})", nrDigits, decodeLocation, territory, codePrecision, latDeg, lonDeg);
                                                    errors.getAndIncrement();
                                                    for (final Mapcode candidate : recodedMapcodes) {
                                                        LOG.info(" * candidate: {}", candidate.getCode(nrDigits));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Check conversion from/to alphabets.
                            for (final Alphabet alphabet : Alphabet.values()) {
                                final String mapcodeAlphabet = mapcode.getCode(alphabet);
                                final String mapcodeAscii = Mapcode.convertStringToPlainAscii(mapcodeAlphabet);
                                if (!mapcode.getCode(0).equals(mapcodeAscii)) {
                                    LOG.error("encodeDecodeTest: " + mapcode + " alphabet=" + alphabet + ", original=" + mapcode.getCode(0) +
                                            ", mapcodeAlphabet=" + mapcodeAlphabet + ", mapcodeAscii=" + mapcodeAscii);
                                    errors.incrementAndGet();
                                }
                            }
                        }
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        assertEquals("Found errors", 0, errors.get());
        LOG.info("encodeDecodeTest: Executed {} tasks", tasks);
    }
}
