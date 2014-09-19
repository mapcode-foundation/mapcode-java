/*
 * Copyright (C) 2014 Stichting Mapcode Foundation (http://www.mapcode.com)
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

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"ProhibitedExceptionDeclared", "OverlyBroadThrowsClause"})
public class ReferenceFileTest {
    private static final Logger LOG  = LoggerFactory.getLogger(ReferenceFileTest.class);
    public static final  Gson   GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    // Use: random/grid_1k.txt, random/grid_10k.txt, random/grid_100k.txt or random/grid_1m.txt.
    private static final String RANDOM_REFERENCE_FILE     = "/random_1k.txt";
    private static final String GRID_REFERENCE_FILE       = "/grid_1k.txt";
    private static final String BOUNDARIES_REFERENCE_FILE = "/grid_1k.txt";

    private static final int    LOG_LINE_EVERY = 25000;
    private static final double METERS_DELTA   = 10.0;

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkRandomReferenceRecords() throws Exception {
        LOG.info("checkRandomReferenceRecords");
        checkFile(RANDOM_REFERENCE_FILE);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkGridReferenceRecords() throws Exception {
        LOG.info("checkGridReferenceRecords");
        checkFile(GRID_REFERENCE_FILE);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void checkBoundariesReferenceRecords() throws Exception {
        LOG.info("checkBoundariesReferenceRecords");
        checkFile(BOUNDARIES_REFERENCE_FILE);
    }

    private void checkFile(@Nonnull final String fileName) throws Exception {

        boolean error = false;

        // Open data file.
        final InputStream inputStream = getClass().getResourceAsStream(fileName);
        assertNotNull(inputStream);
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //noinspection NestedTryStatement
            try {
                int i = 1;
                //noinspection InfiniteLoopStatement
                while (true) {
                    @Nonnull final ReferenceRec reference = getNextReferenceRecord(bufferedReader);

                    final boolean showLogLine = ((i % LOG_LINE_EVERY) == 0);
                    if (showLogLine) {
                        LOG.info("checkFile: #{}, file={}", i, fileName);
                        LOG.info("checkFile: lat/lon  = {}", reference.point);
                        LOG.info("checkFile: expected = {}", GSON.toJson(reference.mapcodes));
                    }

                    /**
                     * Check encode.
                     */

                    // Encode lat/lon to series of Mapcodes and check the resulting Mapcodes.
                    final List<MapcodeInfo> results = Mapcode.encode(
                        reference.point.getLatDeg(), reference.point.getLonDeg());
                    if (showLogLine) {
                        LOG.info("checkFile: actual   = {}", GSON.toJson(results));
                    }

                    // Check the number of Mapcodes.

                    // TODO: This check can only be enabled when the reference implementation and the
                    // Java version produce exactly the same number of code. For now, we will only
                    // log the offending Mapcodes as errors in the log file, but not fail the test.
                    //
                    // Check the size and order of the results with a single assertion.
                    //
                    // assertEquals("Encode #" + i + " incorrect number of results:" +
                    //         "\n  lat/lon  = " + reference.point +
                    //         "\n  expected = " + reference.mapcodes.size() + " results, " +
                    //         GSON.toJson(reference.mapcodes) +
                    //         "\n  actual   = " + results.size() + " results, " + GSON.toJson(results),
                    //     reference.mapcodes.size(), results.size());

                    // For every Mapcode in the result set, check if it is contained in the reference set.
                    for (final MapcodeInfo result : results) {
                        boolean found = false;
                        for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                            if (referenceMapcodeRec.territory.equals(result.getTerritory()) &&
                                referenceMapcodeRec.mapcode.equals(result.getMapcode())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {

                            // This does not fail the test, but rather produces an ERROR in the log file.
                            // It indicates a discrepancy in the C and Java implementations.
                            LOG.error("checkFile: Mapcode '{}' at {} is not in the reference file!",
                                result, reference.point);
                            error = true;

                        }
                    }

                    // For every Mapcode in the reference set, check if it is contained in the result set.
                    for (final MapcodeRec referenceMapcodeRec : reference.mapcodes) {
                        boolean found = false;
                        for (final MapcodeInfo result : results) {
                            if (referenceMapcodeRec.territory.equals(result.getTerritory()) &&
                                referenceMapcodeRec.mapcode.equals(result.getMapcode())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            final MapcodeInfo referenceMapcodeInfo =
                                new MapcodeInfo(referenceMapcodeRec.mapcode, referenceMapcodeRec.territory);
                            LOG.error("checkFile: Mapcode '{}' at {} is not produced by the decoder!",
                                referenceMapcodeInfo, reference.point);
                            error = true;

                        }
                    }

                    /**
                     * Check distance of decoded point to reference point.
                     */
                    for (final MapcodeRec mapcodeRec : reference.mapcodes) {
                        try {
                            final Point result = Mapcode.decode(mapcodeRec.mapcode, mapcodeRec.territory);
                            final double distanceMeters = Point.distanceInMeters(reference.point, result);
                            if (distanceMeters > METERS_DELTA) {
                                LOG.error("Mapcode {} {} was generated for point {}, but decodes to point {} which is {} meters from the original point.", mapcodeRec.territory, mapcodeRec.mapcode, reference.point, result, distanceMeters);
                                error = true;
                            }
                        } catch (UnknownMapcodeException unknownMapcodeException) {
                            LOG.error("Mapcode {} {} was generated for point {}, but cannot be decoded.", mapcodeRec.territory, mapcodeRec.mapcode, reference.point);
                            error = true;
                        }
                    }

                    if (showLogLine) {
                        LOG.info("");
                    }
                    ++i;
                }
            }
            finally {
                bufferedReader.close();
            }
        }
        catch (final EOFException ignored) {
            // OK.
        }
        catch (final IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (final IOException ignored) {
                // Ignore.
            }
        }
        assertTrue(!error);
    }

    private static class MapcodeRec {
        @Nonnull private final String    mapcode;
        @Nonnull private final Territory territory;

        public MapcodeRec(@Nonnull final String mapcode, @Nonnull final Territory territory) {
            this.mapcode = mapcode;
            this.territory = territory;
        }
    }

    private static class ReferenceRec {
        @Nonnull private final Point                 point;
        @Nonnull private final ArrayList<MapcodeRec> mapcodes;

        public ReferenceRec(@Nonnull final Point point, @Nonnull final ArrayList<MapcodeRec> mapcodes) {
            this.point = point;
            this.mapcodes = mapcodes;
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Nonnull
    private static String readNonEmptyLine(@Nonnull final BufferedReader bufferedReader) throws IOException {
        String line;
        do {
            line = bufferedReader.readLine();
            if (line == null) {
                throw new EOFException();
            }
        }
        while (line.isEmpty());
        return line;
    }

    @Nonnull
    private static ReferenceRec getNextReferenceRecord(@Nonnull final BufferedReader bufferedReader)
        throws IOException, UnknownTerritoryException {

        // Read first line of data file: <nr> <lat> <lon> <x> <y> <z>
        final String firstLine = readNonEmptyLine(bufferedReader);
        final String[] args = firstLine.split(" ");
        assertEquals(6, args.length);

        final int count = Integer.parseInt(args[0]);
        assertTrue((1 <= count) && (count <= 21));

        // Read lat/lon.
        final double latDeg = Double.parseDouble(args[1]);
        final double lonDeg = Double.parseDouble(args[2]);
        final Point point = Point.fromMicroDeg(Point.degToMicroDeg(latDeg), Point.degToMicroDeg(lonDeg));
        assertTrue((-90 <= point.getLatDeg()) && (point.getLatDeg() <= 90));
        assertTrue((-180 <= point.getLonDeg()) && (point.getLonDeg() <= 180));

        // Read Mapcodes: <territory> <mapcode>
        final ArrayList<MapcodeRec> mapcodeRecs = new ArrayList<MapcodeRec>();
        for (int i = 0; i < count; ++i) {
            final String line = readNonEmptyLine(bufferedReader);
            assertTrue(!line.isEmpty());

            final String[] mapcodeLine = line.split(" ");
            assertEquals(2, mapcodeLine.length);

            @Nonnull final Territory territory = Territory.fromString(mapcodeLine[0]);
            @Nonnull final String mapcode = mapcodeLine[1];
            final MapcodeRec mapcodeRec = new MapcodeRec(mapcode, territory);
            mapcodeRecs.add(mapcodeRec);
        }
        assertEquals(count, mapcodeRecs.size());
        final ReferenceRec referenceRec = new ReferenceRec(point, mapcodeRecs);
        return referenceRec;
    }
}
